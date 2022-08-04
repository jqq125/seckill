package com.kiki.seckill.controller;

import com.kiki.seckill.common.result.CodeMsg;
import com.kiki.seckill.common.result.Result;
import com.kiki.seckill.pojo.OrderInfo;
import com.kiki.seckill.pojo.SeckillOrder;
import com.kiki.seckill.pojo.SeckillUser;
import com.kiki.seckill.rabbitmq.MQSender;
import com.kiki.seckill.rabbitmq.SeckillMessage;
import com.kiki.seckill.redis.RedisService;
import com.kiki.seckill.redis.key.AccessKey;
import com.kiki.seckill.redis.key.GoodsKey;
import com.kiki.seckill.service.GoodsService;
import com.kiki.seckill.service.OrderService;
import com.kiki.seckill.service.SeckillService;
import com.kiki.seckill.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@RequestMapping("/seckill")
@Controller
public class SeckillController implements InitializingBean {
    @Autowired
    GoodsService goodsService;
    @Autowired
    RedisService redisService;
    @Autowired
    SeckillService seckillService;
    @Autowired
    OrderService orderService;

    @Autowired
    MQSender mqSender;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodslist=goodsService.getGoodsVoList();
        if(goodslist==null) {
            return;
        }
        for(GoodsVo goods:goodslist) {
            //如果不是null的时候，将库存加载到redis里面去 prefix---GoodsKey:gs ,	 key---商品id,	 value
            redisService.set(GoodsKey.getSeckillGoodsStock, ""+goods.getId(), goods.getStockCount());
        }
    }

    @RequestMapping("/do_seckill")
    public String toList(Model model, SeckillUser user, @RequestParam("goodsId") Long goodsId) {
        model.addAttribute("user", user);
        //如果用户为空，则返回至登录页面
        if(user==null){
            return "login";
        }
        GoodsVo goodsvo=goodsService.getGoodsVoByGoodsId(goodsId);
        //判断商品库存，库存大于0，才进行操作，多线程下会出错
        int  stockcount=goodsvo.getStockCount();
        if(stockcount<=0) {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
            model.addAttribute("errorMessage", CodeMsg.SECKILL_OVER_ERROR);
            return "seckill_fail";
        }
        //判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
        SeckillOrder order=orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(),goodsId);
        if(order!=null) {//重复下单
            model.addAttribute("errorMessage", CodeMsg.REPEATE_SECKILL);
            return "seckill_fail";
        }
        //可以秒杀，原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
        OrderInfo orderinfo=seckillService.seckill(user,goodsvo);
        //如果秒杀成功，直接跳转到订单详情页上去。
        model.addAttribute("orderinfo", orderinfo);
        model.addAttribute("goods", goodsvo);
        return "order_detail";//返回页面login
    }


 //--------------------------页面优化--------------------------------------------------------------------

    /**
     *
     * 做了页面静态化的，直接返回订单的信息
     * @param model
     * @param user
     * @param goodsId
     * @return
     *
     * 不能是GET请求，GET，
     */
    //POST请求
    @PostMapping(value="/do_seckill_ajax")
    @ResponseBody
    public Result<OrderInfo> doSeckill(Model model,SeckillUser user,@RequestParam(value="goodsId",defaultValue="0") long goodsId) {
        model.addAttribute("user", user);
        System.out.println("do_seckill_ajax");
        System.out.println("goodsId:"+goodsId);
        //如果用户为空，则返回至登录页面
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        GoodsVo goodsvo=goodsService.getGoodsVoByGoodsId(goodsId);
        //判断商品库存，库存大于0，才进行操作，多线程下会出错
        int  stockcount=goodsvo.getStockCount();
        if(stockcount<=0) {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
            //model.addAttribute("errorMessage", CodeMsg.SECKILL_OVER_ERROR);
            return Result.error(CodeMsg.SECKILL_OVER_ERROR);
        }
        //判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
        SeckillOrder order=orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(),goodsId);
        if(order!=null) {//重复下单
            //model.addAttribute("errorMessage", CodeMsg.REPEATE_SECKILL);
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }
        //可以秒杀，原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
        OrderInfo orderinfo=seckillService.seckill(user,goodsvo);
        //如果秒杀成功，直接跳转到订单详情页上去。
        model.addAttribute("orderinfo", orderinfo);
        model.addAttribute("goods", goodsvo);
        return Result.success(orderinfo);
    }



    //----------------------------------秒杀接口优化1-------------------------------------------------------------------

    /**
     * 优化后的
     * 563.1899076368552
     * 做缓存+消息队列
     * 1.系统初始化，把商品库存数量加载到Redis上面来。
     * 2.收到请求，Redis预减库存。
     * 3.请求入队，立即返回排队中。
     * 4.请求出队，生成订单，减少库存（事务）。
     * 5.客户端轮询，是否秒杀成功。
     *
     * 不能是GET请求，GET
     */
    //POST请求
    @PostMapping("/{path}/do_seckill_ajaxcache")
    @ResponseBody
    public Result<Integer> doSeckillCache(Model model,SeckillUser user,
                                          @RequestParam(value="goodsId",defaultValue="0") long goodsId,
                                          @PathVariable("path")String path) {
        model.addAttribute("user", user);
        //1.如果用户为空，则返回至登录页面
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //验证path,去redis里面取出来然后验证。
        boolean check=seckillService.checkPath(user,goodsId,path);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEAGAL);
        }
        //内存标记，减少对redis的访问 localMap.put(goodsId,false);
//		boolean over=localMap.get(goodsId);
//		//在容量满的时候，那么就打标记为true
//		if(over) {
//			return Result.error(CodeMsg.SECKILL_OVER_ERROR);
//		}
        //2.预减少库存，减少redis里面的库存
        long stock=redisService.decr(GoodsKey.getSeckillGoodsStock,""+goodsId);
        //3.判断减少数量1之后的stock，区别于查数据库时候的stock<=0
        if(stock<0) {
            return Result.error(CodeMsg.SECKILL_OVER_ERROR);
        }
        //4.判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
        SeckillOrder order = orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (order != null) {// 重复下单
            // model.addAttribute("errorMessage", CodeMsg.REPEATE_SECKILL);
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }
        //5.正常请求，入队，发送一个秒杀message到队列里面去，入队之后客户端应该进行轮询。
        SeckillMessage mms=new SeckillMessage();
        mms.setUser(user);
        mms.setGoodsId(goodsId);
        mqSender.sendSeckillMessage(mms);
        //返回0代表排队中
        return Result.success(0);
    }

    //--------------------------------秒杀接口优化2------------------------------------------------------------

    /**
     * 客户端做一个轮询，查看是否成功与失败，失败了则不用继续轮询。
     * 秒杀成功，返回订单的Id。
     * 库存不足直接返回-1。
     * 排队中则返回0。
     * 查看是否生成秒杀订单。
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> doSeckillResult(Model model, SeckillUser user,
                                        @RequestParam(value = "goodsId", defaultValue = "0") long goodsId) {
        long result=seckillService.getSeckillResult(user.getId(),goodsId);
        System.out.println("轮询 result："+result);
        return Result.success(result);
    }

    //------------------------------------安全优化2：生成验证码--------------------------------------------------


    /**
     * 生成图片验证码
     */
    //
    @RequestMapping("/vertifyCode")
    @ResponseBody
    public Result<String> getVertifyCode(Model model, SeckillUser user,
                                         @RequestParam("goodsId") Long goodsId, HttpServletResponse response) {
        System.out.println("验证码加载中");
        model.addAttribute("user", user);
        //如果用户为空，则返回至登录页面
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        BufferedImage img=seckillService.createSeckillVertifyCode(user, goodsId);
        try {
            OutputStream out=response.getOutputStream();
            ImageIO.write(img,"JPEG", out);
            out.flush();
            out.close();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SECKILL_FAIL);
        }
    }

    //------------------------------------安全优化3：生成动态url地址----------------------------------------------

    /**
     * 获取秒杀的path,并且验证验证码的值是否正确
     */
    //@AccessLimit(seconds=5,maxCount=5,needLogin=true)
    //加入注解，实现拦截功能，进而实现限流功能
    //@AccessLimit(seconds=5,maxCount=5,needLogin=true)
    @RequestMapping(value ="/getPath")
    @ResponseBody
    public Result<String> getSeckillPath(HttpServletRequest request, Model model, SeckillUser user,
                                         @RequestParam("goodsId") Long goodsId,
                                         @RequestParam(value="vertifyCode",defaultValue="0") int vertifyCode) {
        model.addAttribute("user", user);
        //如果用户为空，则返回至登录页面
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //限制访问次数
        String uri=request.getRequestURI();
        String key=uri+"_"+user.getId();
        //限定key5s之内只能访问5次
        Integer count=redisService.get(AccessKey.access, key, Integer.class);
        if(count==null) {
            redisService.set(AccessKey.access, key, 1);
        }else if(count<5) {
            redisService.incr(AccessKey.access, key);
        }else {//超过5次
            return Result.error(CodeMsg.ACCESS_LIMIT);
        }

        //验证验证码
        boolean check=seckillService.checkVCode(user, goodsId,vertifyCode );
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEAGAL);
        }
        System.out.println("通过!");
        //生成一个随机串
        String path=seckillService.createSeckillPath(user,goodsId);
        System.out.println("@SeckillController-toseckillPath-path:"+path);
        return Result.success(path);
    }


}
