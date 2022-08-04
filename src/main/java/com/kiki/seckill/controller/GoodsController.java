package com.kiki.seckill.controller;

import com.alibaba.druid.util.StringUtils;
import com.kiki.seckill.common.result.Result;
import com.kiki.seckill.pojo.SeckillUser;
import com.kiki.seckill.redis.RedisService;
import com.kiki.seckill.redis.key.GoodsKey;
import com.kiki.seckill.service.SeckillUserService;

import com.kiki.seckill.service.GoodsService;
import com.kiki.seckill.vo.GoodsDetailVo;
import com.kiki.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;


import javax.jws.Oneway;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    GoodsService goodsService;

/*    *//*
    * v1.0版
     *//*
    @RequestMapping("/to_list")
    public String toList(Model model, @CookieValue(value= SeckillUserService.COOKIE1_NAME_TOKEN)String cookieToken,
                          HttpServletResponse response) {
        //通过取到cookie，首先取@RequestParam没有再去取@CookieValue
        if(StringUtils.isEmpty(cookieToken)) {
            return "login";//返回到登录界面
        }
        String token=cookieToken;
        SeckillUser user=seckillUserService.getByToken(token,response);
        model.addAttribute("user", user);
        return "goods_list";//返回页面login
    }


    *//** v2.0版,未作页面缓存
    * 1000*10
    * QPS:390.9/sec
    * *//*
    @RequestMapping("/to_list")
    public String toList(Model model,SeckillUser user) {
        model.addAttribute("user", user);
        //查询商品列表
        List<GoodsVo> goodsList= goodsService.getGoodsVoList();
        model.addAttribute("goodsList", goodsList);
        return "goods_list";//返回页面login
    }


    *//**
     * 未作页面缓存
     * @param model
     * @param user
     * @param goodsId
     * @return
     *//*

*//*
    *//**//**
     * 之前的版本  1.0  未作user的参数，即未作UserArgumentResolver时调用的detail请求
     * @param model
     * @param cookieToken
     * @param response
     * @return
     *//*

    @RequestMapping("/to_detail1")
    public String toDetail(Model model, @CookieValue(value=SeckillUserService.COOKIE1_NAME_TOKEN)String cookieToken
            , HttpServletResponse response) {
        //通过取到cookie，首先取@RequestParam没有再去取@CookieValue
        if(StringUtils.isEmpty(cookieToken)) {
            return "login";//返回到登录界面
        }
        String token=cookieToken;
        System.out.println("goods-token:"+token);
        System.out.println("goods-cookieToken:"+cookieToken);
        SeckillUser user=seckillUserService.getByToken(token,response);
        model.addAttribute("user", user);
        return "goods_list";//返回页面login
    }

    *//*
    * v2.0
    * *//*


    @RequestMapping("/to_detail/{goodsId}")
    public String toDetail(Model model,SeckillUser user,@PathVariable("goodsId")long goodsId) {//id一般用snowflake算法
        model.addAttribute("user", user);
        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);
        //既然是秒杀，还要传入秒杀开始时间，结束时间等信息
        long start=goods.getStartDate().getTime();
        long end=goods.getEndDate().getTime();
        long now=System.currentTimeMillis();
        //秒杀状态量
        int status=0;
        //开始时间倒计时
        int remailSeconds=0;
        //查看当前秒杀状态
        if(now<start) {//秒杀还未开始，--->倒计时
            status=0;
            remailSeconds=(int) ((start-now)/1000);  //毫秒转为秒
        }else if(now>end){ //秒杀已经结束
            status=2;
            remailSeconds=-1;  //毫秒转为秒
        }else {//秒杀正在进行
            status=1;
            remailSeconds=0;  //毫秒转为秒
        }
        model.addAttribute("status", status);
        model.addAttribute("remailSeconds", remailSeconds);
        return "goods_detail";//返回页面login
    }


    */

    //----------------------------页面优化1---------------------------------------------------------------------
    @Autowired
    RedisService redisService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;


    /*
    * v3.0版：页面缓存,做页面缓存的list页面，防止同一时间巨大访问到达数据库，如果缓存时间过长，数据及时性就不高。
    * step1:取缓存（缓存里面存的是html)
    * step2:手动渲染模板
    * step3:结果输出（直接输出html代码）
    * */
    @RequestMapping(value="/to_list",produces="text/html")
    @ResponseBody
    public String toListCache(Model model,SeckillUser user,HttpServletRequest request,
                              HttpServletResponse response) {
        // 1.取缓存
        // public <T> T get(KeyPrefix prefix,String key,Class<T> data)
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        model.addAttribute("user", user);
        //1.查询商品列表
        List<GoodsVo> goodsList= goodsService.getGoodsVoList();
        model.addAttribute("goodsList", goodsList);
        //2.手动渲染  使用模板引擎	templateName:模板名称 	String templateName="goods_list";
        IWebContext ctx=new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html=thymeleafViewResolver.getTemplateEngine().process("goods_list.html", ctx);
        //保存至缓存
        if(!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsList, "", html);//key---GoodsKey:gl---缓存goodslist这个页面
        }
        return html;
        //return "goods_list";//返回页面login
    }


    /**
     * 做了页面缓存的to_detail商品详情页。
     * 做了页面缓存  URL缓存  ""+goodsId  不同的url进行缓存redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value="/to_detail/{goodsId}")  //produces="text/html"
    @ResponseBody
    public String toDetailCachehtml(Model model,SeckillUser user,
                                    HttpServletRequest request,HttpServletResponse response,@PathVariable("goodsId")long goodsId) {//id一般用snowflake算法
        // 1.取缓存
        // public <T> T get(KeyPrefix prefix,String key,Class<T> data)
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);//不同商品页面不同的详情
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        //缓存中没有，则将业务数据取出，放到缓存中去。
        model.addAttribute("user", user);
        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);
        //既然是秒杀，还要传入秒杀开始时间，结束时间等信息
        long start=goods.getStartDate().getTime();
        long end=goods.getEndDate().getTime();
        long now=System.currentTimeMillis();
        //秒杀状态量
        int status=0;
        //开始时间倒计时
        int remailSeconds=0;
        //查看当前秒杀状态
        if(now<start) {//秒杀还未开始，--->倒计时
            status=0;
            remailSeconds=(int) ((start-now)/1000);  //毫秒转为秒
        }else if(now>end){ //秒杀已经结束
            status=2;
            remailSeconds=-1;  //毫秒转为秒
        }else {//秒杀正在进行
            status=1;
            remailSeconds=0;  //毫秒转为秒
        }
        model.addAttribute("status", status);
        model.addAttribute("remailSeconds", remailSeconds);

        // 2.手动渲染 使用模板引擎 templateName:模板名称 String templateName="goods_detail";
        IWebContext ctx = new WebContext(request, response, request.getServletContext(),
                request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail.html", ctx);
        // 将渲染好的html保存至缓存
        if (!StringUtils.isEmpty(html)) {
            redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
        }
        return html;//html是已经渲染好的html文件
        //return "goods_detail";//返回页面login
    }

    //-----------------------------页面优化2------------------------------------------------------

    /**
     * 作页面静态化的商品详情
     * 页面存的是html
     * 动态数据通过接口从服务端获取
     * @param model
     * @param user
     * @param goodsId
     * @return
    */
    @RequestMapping(value="/detail/{goodsId}")  //produces="text/html"
    @ResponseBody
    public Result<GoodsDetailVo> toDetail_staticPage(Model model, SeckillUser user, @PathVariable("goodsId")long goodsId) {//id一般用snowflake算法
        System.out.println("页面静态化/detail/{goodsId}");
        model.addAttribute("user", user);
        GoodsVo goodsVo=goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goodsVo);
        //既然是秒杀，还要传入秒杀开始时间，结束时间等信息
        long start=goodsVo.getStartDate().getTime();
        long end=goodsVo.getEndDate().getTime();
        long now=System.currentTimeMillis();
        //秒杀状态量
        int status=0;
        //开始时间倒计时
        int remailSeconds=0;
        //查看当前秒杀状态
        if(now<start) {//秒杀还未开始，--->倒计时
            status=0;
            remailSeconds=(int) ((start-now)/1000);  //毫秒转为秒
        }else if(now>end){ //秒杀已经结束
            status=2;
            remailSeconds=-1;  //毫秒转为秒
        }else {//秒杀正在进行
            status=1;
            remailSeconds=0;  //毫秒转为秒
        }
        model.addAttribute("status", status);
        model.addAttribute("remailSeconds", remailSeconds);
        GoodsDetailVo gdVo=new GoodsDetailVo();
        gdVo.setGoodsVo(goodsVo);
        gdVo.setStatus(status);
        gdVo.setRemailSeconds(remailSeconds);
        gdVo.setUser(user);
        //将数据填进去，传至页面
        return Result.success(gdVo);
    }


}
