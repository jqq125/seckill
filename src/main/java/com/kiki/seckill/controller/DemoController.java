package com.kiki.seckill.controller;

import com.kiki.seckill.common.exception.GlobalException;
import com.kiki.seckill.common.result.CodeMsg;
import com.kiki.seckill.common.result.Result;
import com.kiki.seckill.common.utils.RedisConcurrentTestUtil;
import com.kiki.seckill.pojo.User;
import com.kiki.seckill.redis.RedisService;
import com.kiki.seckill.redis.key.UserKey;
import com.kiki.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/demo")
public class DemoController {
    @RequestMapping("/index")
    public String getIndex(){
        return "index"; //返回templates/index.html,需要thymeleaf的支持
    }

    @RequestMapping("/")
    @ResponseBody
    public String home() {
        return "hello world";
    }
    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello() {//0代表成功
        return Result.success("hello sss");
    }
    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError() {//0代表成功
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    /*
     * @responseBody注解的作用是将controller的方法返回的对象通过适当的转换器转换为指定的格式之后，写入到response对象的body区，通常用来返回JSON数据或者是XML数据
     * 需要注意: 在使用此注解之后不会再走视图处理器，而是直接将数据写入到输入流中，他的效果等同于通过response对象输出指定格式的数据。
     */
    @RequestMapping("/thymeleaf")	//用thymeleaf返回模板，用String返回!!!
    public String helloThymeleaf(Model model) {//0代表成功
        model.addAttribute("name", "kiki");
        return "hello";//他会从配置文件里面去找
    }

    @Autowired
    UserService userService;

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {//0代表成功
        User user=userService.getById(1);
        System.out.println("res:"+user.getName());
        return Result.success(user);
    }

    @Autowired
    RedisService redisService;
    /**
     *避免key被不同类的数据覆盖
     *使用Prefix前缀-->不同类别的缓存，用户、部门、
     */
    @RequestMapping("/redis/setbyid")
    @ResponseBody
    public Result<Boolean> redisSetById() {//0代表成功
        User user=new User(1,"1111");
        boolean f=redisService.set(UserKey.getById,""+1,user);
        return Result.success(true);
    }

    @RequestMapping("/redis/getbyid")
    @ResponseBody
    public Result<User> redisGetById() {//0代表成功
        User res=redisService.get(UserKey.getById,""+1,User.class);
        //redisService.get("key1",String.class);
        //System.out.println("res:"+userService.tx());
        return Result.success(res);
    }


    @RequestMapping("/redis/setbyname")
    @ResponseBody
    public Result<Boolean> redisSetByName() {//0代表成功
        User user=new User(2,"2222");
        boolean f=redisService.set(UserKey.getByName,"2222",user);
        return Result.success(true);
    }
    @RequestMapping("/redis/getbyname")
    @ResponseBody
    public Result<User> redisGetByName() {//0代表成功
        User res=redisService.get(UserKey.getByName,"2222",User.class);
        //redisService.get("key1",String.class);
        //System.out.println("res:"+userService.tx());
        return Result.success(res);
    }

    //--------------------redis多线程测试-------------------------------

    @Autowired
    RedisConcurrentTestUtil redisConcurrentTestUtil;
    @RequestMapping("/redis/testConcurrent")
    @ResponseBody
    public Result<Boolean> redisTestConcurrent(){
        redisConcurrentTestUtil.test();
        return Result.success(true);
    }







}
