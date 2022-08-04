package com.kiki.seckill.vo;

import com.kiki.seckill.pojo.Goods;

import java.util.Date;

//将Goods表和seckillGoods表合并
public class GoodsVo extends Goods {
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
    private Double seckillPrice;


    public Double getSeckillPrice() {
        return seckillPrice;
    }
    public void setSeckillPrice(Double seckillPrice) {
        this.seckillPrice = seckillPrice;
    }
    public Integer getStockCount() {
        return stockCount;
    }
    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
