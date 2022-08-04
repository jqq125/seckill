package com.kiki.seckill.service;

import com.kiki.seckill.dao.OrderDao;
import com.kiki.seckill.pojo.OrderInfo;
import com.kiki.seckill.pojo.SeckillOrder;
import com.kiki.seckill.pojo.SeckillUser;
import com.kiki.seckill.redis.RedisService;
import com.kiki.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    /**
     * 代码1.0
     * 根据用户userId和goodsId判断是否有者条订单记录，有则返回此纪录
     * @param id
     * @param goodsId
     * @return
     */
    public SeckillOrder getSeckillOrderByUserIdAndGoodsId(Long userId, Long goodsId) {
        return orderDao.getSeckillOrderByUserIdAndGoodsId(userId,goodsId);
    }

    /**
     * 生成订单,事务
     * @param user
     * @param goodsvo
     * @return
     */
    @Transactional
    public OrderInfo createOrder(SeckillUser user, GoodsVo goodsvo) {
        //1.生成order_info
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setDeliveryAddrId(0L);//long类型 private Long deliveryAddrId;   L
        orderInfo.setCreateDate(new Date());
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goodsvo.getId());
        //秒杀价格
        orderInfo.setGoodsPrice(goodsvo.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        //订单状态  ---0-新建未支付  1-已支付  2-已发货  3-已收货
        orderInfo.setOrderStatus(0);
        //用户id
        orderInfo.setUserId(user.getId());
        //返回orderId
        //long orderId=
        orderDao.insert(orderInfo);
        //2.生成Seckill_order
        SeckillOrder seckillorder =new SeckillOrder();
        seckillorder.setGoodsId(goodsvo.getId());
        //将订单id传给秒杀订单里面的订单orderid
        seckillorder.setOrderId(orderInfo.getId());
        seckillorder.setUserId(user.getId());
        orderDao.insertSeckillOrder(seckillorder);
        return orderInfo;
    }
    public OrderInfo getOrderByOrderId(long orderId) {
        return orderDao.getOrderByOrderId(orderId);
    }

    /**
     * 生成订单,事务,同时写入到缓存
     * @param user
     * @param goodsvo
     * @return
     */
    @Transactional
    public OrderInfo createOrder_Cache(SeckillUser user, GoodsVo goodsvo) {
        //1.生成order_info
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setDeliveryAddrId(0L);//long类型 private Long deliveryAddrId;   L
        orderInfo.setCreateDate(new Date());
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goodsvo.getId());
        //秒杀价格
        orderInfo.setGoodsPrice(goodsvo.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        //订单状态  ---0-新建未支付  1-已支付  2-已发货  3-已收货
        orderInfo.setOrderStatus(0);
        //用户id
        orderInfo.setUserId(user.getId());
        //返回orderId
        long orderId=orderDao.insert(orderInfo);
        System.out.println("-----orderId:"+orderId);

        OrderInfo orderquery=orderDao.selectorderInfo(user.getId(), goodsvo.getId());
        long orderIdquery=orderquery.getId();
        System.out.println("-----orderIdquery:"+orderIdquery);

        //2.生成miaosha_order
        SeckillOrder miaoshaorder =new SeckillOrder();
        miaoshaorder.setGoodsId(goodsvo.getId());
        //将订单id传给秒杀订单里面的订单orderid
        miaoshaorder.setOrderId(orderIdquery);
        miaoshaorder.setUserId(user.getId());
        orderDao.insertSeckillOrder(miaoshaorder);
        //set(KeyPrefix prefix,String key,T value)   设置缓存数据。
        redisService.set(OrderKey.getSeckillOrderByUidAndGid, ""+user.getId()+"_"+goodsvo.getId(), miaoshaorder);
        return orderInfo;
    }



    /**
     * 代码2.0
     * 做一个优化，不用每次都去查数据库
     * 生成订单的时候，将订单同时写入到缓存里面去。
     */


    /**
     * 判断是否秒杀到某商品，即去miaosha_order里面去查找是否有记录userId和goodsId的一条数据。
     * 根据用户userId和goodsId判断是否有者条订单记录，有则返回此纪录
     *
     * @param id
     * @param goodsId
     * @return
     */
    public SeckillOrder getSeckillOrderByUserIdAndGoodsId_Cache(Long userId, Long goodsId) {
        //1.先去缓存里面取得
        SeckillOrder morder=redisService.get(OrderKey.getSeckillOrderByUidAndGid, ""+userId+"_"+goodsId, SeckillOrder.class);
        return morder;
        //return orderDao.getMiaoshaOrderByUserIdAndCoodsId(userId,goodsId);
    }
}
