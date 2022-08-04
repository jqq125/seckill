package com.kiki.seckill.dao;

import com.kiki.seckill.pojo.SeckillGoods;
import com.kiki.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface GoodsDao {
    //两个查询
    @Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.seckill_price from seckill_goods mg left join goods g on mg.goods_id=g.id")
    public List<GoodsVo> getGoodsVoList();
    @Select("select g.*,mg.stock_count,mg.start_date,mg.end_date,mg.seckill_price from seckill_goods mg left join goods g on mg.goods_id=g.id where g.id=#{goodsId}")
    public GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    //stock_count>0的时候才去更新，数据库本身会有锁，那么就不会在数据库中同时多个线程更新一条记录，使用数据库特性来保证超卖的问题
    @Update("update seckill_goods set stock_count=stock_count-1 where goods_id=#{goodsId} and stock_count>0")
    public void reduceStock(SeckillGoods goods);

    @Update("update seckill_goods set stock_count=stock_count-1 where goods_id=#{goodsId} and stock_count>0")
    public int reduceStock1(SeckillGoods goods);
}
