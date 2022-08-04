package com.kiki.seckill.controller;

import com.kiki.seckill.common.result.CodeMsg;
import com.kiki.seckill.common.result.Result;
import com.kiki.seckill.pojo.OrderInfo;
import com.kiki.seckill.pojo.SeckillUser;

import com.kiki.seckill.service.GoodsService;
import com.kiki.seckill.service.OrderService;
import com.kiki.seckill.service.SeckillUserService;
import com.kiki.seckill.vo.GoodsVo;
import com.kiki.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;


    //@NeedLogin
    /**
     * 		@NeedLogin使用一个拦截器，不用每次都去判断user是否为空，在拦截器里面user为空，直接返回某页面。
     * @param model
     * @param user
     * @param orderId
     * @return
     */
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, SeckillUser user, @RequestParam("orderId") long orderId) {
        if(user==null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order=orderService.getOrderByOrderId(orderId);
        if(order==null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        //订单存在的情况
        long goodsId=order.getGoodsId();
        GoodsVo gVo=goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo oVo=new OrderDetailVo();
        oVo.setGoodsVo(gVo);
        oVo.setOrder(order);
        return Result.success(oVo);//返回页面login
    }

}
