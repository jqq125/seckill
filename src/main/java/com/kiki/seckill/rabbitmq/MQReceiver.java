package com.kiki.seckill.rabbitmq;

import com.kiki.seckill.pojo.SeckillOrder;
import com.kiki.seckill.pojo.SeckillUser;
import com.kiki.seckill.redis.RedisService;
import com.kiki.seckill.service.GoodsService;
import com.kiki.seckill.service.OrderService;
import com.kiki.seckill.service.SeckillService;
import com.kiki.seckill.service.SeckillUserService;
import com.kiki.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//接收者
@Service
public class MQReceiver {
    @Autowired
    GoodsService goodsService;
    @Autowired
    RedisService redisService;
    @Autowired
    SeckillUserService seckillUserService;
    //作为秒杀功能事务的Service
    @Autowired
    SeckillService seckillService;
    @Autowired
    OrderService orderService;

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);


    @RabbitListener(queues = MQConfig.SECKILL_QUEUE)//指明监听的是哪一个queue
    public void receiveSeckill(String message) {
        log.info("receiveSeckill message:" + message);
        //通过string类型的message还原成bean
        //拿到了秒杀信息之后。开始业务逻辑秒杀，
        SeckillMessage mm = RedisService.stringToBean(message, SeckillMessage.class);
        SeckillUser user = mm.getUser();
        long goodsId = mm.getGoodsId();
        GoodsVo goodsvo = goodsService.getGoodsVoByGoodsId(goodsId);
        int stockcount = goodsvo.getStockCount();
        //1.判断库存不足
        if (stockcount <= 0) {//失败			库存至临界值1的时候，此时刚好来了加入10个线程，那么库存就会-10
            //model.addAttribute("errorMessage", CodeMsg.SECKILL_OVER_ERROR);
            return;
        }
        //2.判断这个秒杀订单形成没有，判断是否已经秒杀到了，避免一个账户秒杀多个商品
        SeckillOrder order = orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (order != null) {// 重复下单
            // model.addAttribute("errorMessage", CodeMsg.REPEATE_SECKILL);
            return;
        }
        //原子操作：1.库存减1，2.下订单，3.写入秒杀订单--->是一个事务
        //SeckillService.Seckill(user,goodsvo);
        seckillService.seckill2(user, goodsvo);

    }
}



