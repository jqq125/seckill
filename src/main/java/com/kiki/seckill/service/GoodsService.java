package com.kiki.seckill.service;

import com.kiki.seckill.dao.GoodsDao;
import com.kiki.seckill.pojo.SeckillGoods;
import com.kiki.seckill.redis.RedisService;
import com.kiki.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {
    public static final String COOKIE1_NAME_TOKEN="token";

    @Autowired
    GoodsDao goodsDao;
    @Autowired
    RedisService redisService;

    public List<GoodsVo> getGoodsVoList() {
        return goodsDao.getGoodsVoList();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }



    public void reduceStock(GoodsVo goodsvo) {
        SeckillGoods goods=new SeckillGoods();
        goods.setGoodsId(goodsvo.getId());
        //goods.setStockCount(goodsvo.getGoodsStock()-1);  sql里面去运算
        //goodsDao.reduceStock(goods.getGoodsId());
        goodsDao.reduceStock(goods);
    }
    /**
     * 考虑有可能下单失败的情况,下单失败那么就不去
     */
    public boolean reduceStock1(GoodsVo goodsvo) {
        SeckillGoods goods=new SeckillGoods();
        goods.setGoodsId(goodsvo.getId());
        //goods.setStockCount(goodsvo.getGoodsStock()-1);  sql里面去运算
        //goodsDao.reduceStock(goods.getGoodsId());
        int ret=goodsDao.reduceStock1(goods);
        System.out.println("reduceStock1:"+ret);
        return ret>0;
        //return true;
    }

}
