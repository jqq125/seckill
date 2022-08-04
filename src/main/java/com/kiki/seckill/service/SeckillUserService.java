package com.kiki.seckill.service;

import com.alibaba.druid.util.StringUtils;
import com.kiki.seckill.common.exception.GlobalException;
import com.kiki.seckill.common.result.CodeMsg;
import com.kiki.seckill.common.result.Result;
import com.kiki.seckill.common.utils.MD5Util;
import com.kiki.seckill.common.utils.UUIDUtil;
import com.kiki.seckill.common.utils.ValidatorUtil;
import com.kiki.seckill.dao.SeckillUserDao;
import com.kiki.seckill.pojo.OrderInfo;
import com.kiki.seckill.pojo.SeckillUser;
import com.kiki.seckill.redis.RedisService;
import com.kiki.seckill.redis.key.SeckillUserKey;
import com.kiki.seckill.vo.GoodsVo;
import com.kiki.seckill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserService {
    public static final String COOKIE1_NAME_TOKEN="token";

    @Autowired
    SeckillUserDao seckillUserDao;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;
    /**
     * 根据id取得对象，先去缓存中取
     * @param id
     * @return
     */
    public SeckillUser getById(long id) {
        //1.取缓存	---先根据id来取得缓存
        SeckillUser user=redisService.get(SeckillUserKey.getById, ""+id, SeckillUser.class);
        //能在缓存中拿到
        if(user!=null) {
            return user;
        }
        //2.缓存中拿不到，那么就去取数据库
        user=seckillUserDao.getById(id);
        //3.设置缓存
        if(user!=null) {
            redisService.set(SeckillUserKey.getById, ""+id, user);
        }
        return user;
    }

    public String loginString(HttpServletResponse response,LoginVo loginVo) {
        if(loginVo==null) {
            return CodeMsg.SERVER_ERROR.getMsg();
        }
        //经过了依次MD5的密码
        String mobile=loginVo.getMobile();
        String formPass=loginVo.getPassword();
        //判断手机号是否存在
        SeckillUser user=getById(Long.parseLong(mobile));
        //查询不到该手机号的用户
        if(user==null) {
            return CodeMsg.MOBILE_NOTEXIST.getMsg();
        }
        //手机号存在的情况，验证密码，获取数据库里面的密码与salt去验证
        //111111--->e5d22cfc746c7da8da84e0a996e0fffa
        String dbPass=user.getPwd();
        String dbSalt=user.getSalt();
        System.out.println("dbPass:"+dbPass+"   dbSalt:"+dbSalt);
        //验证密码，计算二次MD5出来的pass是否与数据库一致
        String tmppass=MD5Util.formPassToDBPass(formPass, dbSalt);
        System.out.println("formPass:"+formPass);
        System.out.println("tmppass:"+tmppass);
        if(!tmppass.equals(dbPass)) {
            return CodeMsg.PASSWORD_ERROR.getMsg();
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(user,token,response);
        return token;
    }



    public CodeMsg login(HttpServletResponse response,LoginVo loginVo) {
        if(loginVo==null) {
            return CodeMsg.SERVER_ERROR;
        }
        //经过了依次MD5的密码
        String mobile=loginVo.getMobile();
        String formPass=loginVo.getPassword();
        //判断手机号是否存在
        SeckillUser user=getById(Long.parseLong(mobile));
        //查询不到该手机号的用户
        if(user==null) {
            return CodeMsg.MOBILE_NOTEXIST;
        }
        //手机号存在的情况，验证密码，获取数据库里面的密码与salt去验证
        //111111--->e5d22cfc746c7da8da84e0a996e0fffa
        String dbPass=user.getPwd();
        String dbSalt=user.getSalt();
        //验证密码，计算二次MD5出来的pass是否与数据库一致
        String tmppass= MD5Util.formPassToDBPass(formPass, dbSalt);
        if(!tmppass.equals(dbPass)) {
            return CodeMsg.PASSWORD_ERROR;
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(user,token,response);
        return CodeMsg.SUCCESS;
    }
    /**
     * 添加或者叫做更新cookie
     */
    public void addCookie(SeckillUser user,String token,HttpServletResponse response) {
        // 可以用老的token，不用每次都生成cookie，可以用之前的
        System.out.println("uuid:" + token);
        // 将token写到cookie当中，然后传递给客户端
        // 此token对应的是哪一个用户,将我们的私人信息存放到一个第三方的缓存中
        // prefix:SeckillUserKey.token key:token value:用户的信息 -->以后拿到了token就知道对应的用户信息。
        // SeckillUserKey_tk+token-->value
        redisService.set(SeckillUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE1_NAME_TOKEN, token);
        // 设置cookie的有效期，与session有效期一致
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        // 设置网站的根目录
        cookie.setPath("/");
        // 需要写到response中
        response.addCookie(cookie);
    }
    /**
     * 从缓存里面取得值，取得value
     */
    public SeckillUser getByToken(String token,HttpServletResponse response) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        SeckillUser user=redisService.get(SeckillUserKey.token, token,SeckillUser.class);
        // 再次请求时候，延长有效期 重新设置缓存里面的值，使用之前cookie里面的token
        if(user!=null) {
            addCookie(user,token,response);
        }
        return user;
    }

    /**
     * 秒杀，原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
     * 返回生成的订单
     * @param user
     * @param goodsvo
     * @return
     */
    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goodsvo) {
        //1.减少库存,即更新库存
        goodsService.reduceStock(goodsvo);//考虑减少库存失败的时候，不进行写入订单
        //2.下订单,其中有两个订单: order_info   miaosha_order
        OrderInfo orderinfo=orderService.createOrder(user,goodsvo);
        return orderinfo;
    }







}
