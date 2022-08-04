package com.kiki.seckill.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



//作用 声明当前类是一个配置类,相当于一个Spring的XML配置文件,与@Bean配
//@Configuration标注在类上，相当于把该类作为spring的xml配置文件中的<beans>，作用为：配置spring容器(应用上下文)
@Configuration
public class MQConfig {
    public static final String QUEUE="queue";
    public static final String SECKILL_QUEUE="seckill.queue";

    public static final String TOPIC_QUEUE1="topic.queue1";
    public static final String TOPIC_QUEUE2="topic.queue2";
    public static final String HEADER_QUEUE="header.queue";
    public static final String TOPIC_EXCHANGE="topic.exchange";
    public static final String FANOUT_EXCHANGE="fanout.exchange";
    public static final String HEADER_EXCHANGE="header.exchange";
    public static final String ROUTINIG_KEY1="topic.key1";
    public static final String ROUTINIG_KEY2="topic.#";
    /**
     * Direct模式，交换机Exchange:
     * 发送者，将消息往外面发送的时候，并不是直接投递到队列里面去，而是先发送到交换机上面，然后由交换机发送数据到queue上面去，
     * 做了依次路由。
     */
    @Bean
    public Queue queue() {
        //名称，是否持久化
        return new Queue(QUEUE,true);
    }

    @Bean
    public Queue seckillqueue() {
        //名称，是否持久化
        return new Queue(SECKILL_QUEUE,true);
    }
}
