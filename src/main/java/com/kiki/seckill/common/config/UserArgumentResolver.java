package com.kiki.seckill.common.config;

import com.alibaba.druid.util.StringUtils;
import com.kiki.seckill.pojo.SeckillUser;
import com.kiki.seckill.service.SeckillUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*
* 解析前端传来的cookie
* */

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    SeckillUserService seckillUserService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //返回参数的类型
        Class<?> clazz=parameter.getParameterType();
        return clazz== SeckillUser.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request=webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response=webRequest.getNativeResponse(HttpServletResponse.class);
        String paramToken=request.getParameter(SeckillUserService.COOKIE1_NAME_TOKEN);
        System.out.println("@UserArgumentResolver-resolveArgument  paramToken:"+paramToken);

        //获取cookie
        String cookieToken=getCookieValue(request,SeckillUserService.COOKIE1_NAME_TOKEN);
        System.out.println("@UserArgumentResolver-resolveArgument  cookieToken:"+cookieToken);

        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken))
        {
            return null;
        }
        String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        System.out.println("goods-token:"+token);
        System.out.println("goods-cookieToken:"+cookieToken);
        SeckillUser user=seckillUserService.getByToken(token,response);
        System.out.println("@UserArgumentResolver--------user:"+user);
        //去取得已经保存的user，因为在用户登录的时候,user已经保存到threadLocal里面了，因为拦截器首先执行，然后才是取得参数
        //SeckillUser user=UserContext.getUser();
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
