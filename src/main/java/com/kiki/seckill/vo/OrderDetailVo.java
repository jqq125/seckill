package com.kiki.seckill.vo;

import com.kiki.seckill.pojo.OrderInfo;

public class OrderDetailVo {
    private GoodsVo goodsVo;
    private OrderInfo order;
    public GoodsVo getGoodsVo() {
        return goodsVo;
    }
    public void setGoodsVo(GoodsVo goodsVo) {
        this.goodsVo = goodsVo;
    }
    public OrderInfo getOrder() {
        return order;
    }
    public void setOrder(OrderInfo order) {
        this.order = order;
    }

}
