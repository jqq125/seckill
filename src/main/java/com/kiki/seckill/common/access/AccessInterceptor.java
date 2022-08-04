package com.kiki.seckill.common.access;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.kiki.seckill.common.result.CodeMsg;
import com.kiki.seckill.common.result.Result;
import com.kiki.seckill.pojo.SeckillUser;
import com.kiki.seckill.redis.RedisService;
import com.kiki.seckill.redis.key.AccessKey;
import com.kiki.seckill.service.SeckillUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    SeckillUserService seckillUserService;
    @Autowired
    RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if(handler instanceof HandlerMethod) {
            //先去取得用户做判断
            SeckillUser user=getUser(request,response);

            System.out.println("@AccessInterceptor---user"+user);
            //将user保存下来
            UserContext.setUser(user);
            HandlerMethod hm=(HandlerMethod)handler;
            AccessLimit aclimit=hm.getMethodAnnotation(AccessLimit.class);
            //无该注解的时候，那么就不进行拦截操作
            if(aclimit==null) {
                return true;
            }
            //获取参数
            int seconds=aclimit.seconds();
            int maxCount=aclimit.maxCount();
            boolean needLogin=aclimit.needLogin();
            String key=request.getRequestURI();
            System.out.println("------------："+key);
            if(needLogin) {
                if(user==null) {
                    //需要给客户端一个提示
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                //需要的登录
                key+="_"+user.getId();
            }else {//不需要登录
                //不需要操作
            }
            //限制访问次数
            String uri=request.getRequestURI();
            //String key=uri+"_"+user.getId();
            //限定key5s之内只能访问5次，动态设置有效期
            AccessKey akey=AccessKey.expire(seconds);
            Integer count=redisService.get(akey, key, Integer.class);
            if(count==null) {
                redisService.set(akey, key, 1);
            }else if(count<maxCount) {
                redisService.incr(akey, key);
            }else {//超过5次
                //Result.error(CodeMsg.ACCESS_LIMIT);
                render(response,CodeMsg.ACCESS_LIMIT);
                //结果给前端
                return false;
            }
        }
        return super.preHandle(request, response, handler);
    }

    private void render(HttpServletResponse response, CodeMsg cm) throws IOException {
        //指定输出的编码格式，避免乱码
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out=response.getOutputStream();
        String jres= JSON.toJSONString(Result.error(cm));
        out.write(jres.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private SeckillUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken=request.getParameter(seckillUserService.COOKIE1_NAME_TOKEN);
        String cookieToken=getCookieValue(request,seckillUserService.COOKIE1_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken))
        {
            return null;
        }
        String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        SeckillUser user=seckillUserService.getByToken(token,response);
        return user;
    }
    public String getCookieValue(HttpServletRequest request, String cookie1NameToken) {//COOKIE1_NAME_TOKEN-->"token"
        //遍历request里面所有的cookie
        Cookie[] cookies=request.getCookies();
        if(cookies!=null) {
            for(Cookie cookie :cookies) {
                if(cookie.getName().equals(cookie1NameToken)) {
                    System.out.println("getCookieValue:"+cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        System.out.println("No getCookieValue!");
        return null;
    }
}