package com.kiki.seckill.rabbitmq;

import com.kiki.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//发送者
@Service
public class MQSender {
    private static Logger log= LoggerFactory.getLogger(MQSender.class);
    @Autowired
    RedisService redisService;
    @Autowired
    AmqpTemplate amqpTemplate;
    /**
     * 发送秒杀信息，使用derict模式的交换机。（包含秒杀用户信息，秒杀商品id）
     */
    public void sendSeckillMessage(SeckillMessage mmessage) {
        // 将对象转换为字符串
        String msg = RedisService.beanToString(mmessage);
        log.info("send message:" + msg);
        // 第一个参数队列的名字，第二个参数发出的信息
        amqpTemplate.convertAndSend(MQConfig.SECKILL_QUEUE, msg);
    }
}
