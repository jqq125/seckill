package com.kiki.seckill.controller;


import com.kiki.seckill.common.result.CodeMsg;
import com.kiki.seckill.common.result.Result;

import com.kiki.seckill.redis.RedisService;
import com.kiki.seckill.service.SeckillUserService;
import com.kiki.seckill.service.UserService;
import com.kiki.seckill.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Controller
@RequestMapping("/login")
public class LoginController {
    @Autowired
    UserService userService;
    @Autowired
    RedisService redisService;
    @Autowired
    SeckillUserService seckillUserService;
    //slf4j
    private static Logger log=(Logger) LoggerFactory.getLogger(Logger.class);

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";// 返回页面login
    }

    @RequestMapping("/do_login") // 作为异步操作
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {// 0代表成功
        log.info(loginVo.toString());
        CodeMsg cm = seckillUserService.login(response, loginVo);
        if (cm.getCode() == 0) {
            return Result.success(true);
        } else {
            return Result.error(cm);
        }
    }


    //使用JSR303校验
    @RequestMapping("/do_login_test")//作为异步操作
    @ResponseBody
    public Result<String> doLogintest(HttpServletResponse response, @Valid LoginVo loginVo) {//0代表成功
        String token=seckillUserService.loginString(response,loginVo);
        return Result.success(token);
    }

}
