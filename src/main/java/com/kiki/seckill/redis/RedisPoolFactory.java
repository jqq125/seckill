package com.kiki.seckill.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/*
* RedisPoolFactory 通过配置文件，生成Jedis连接池（配置），方便在RedisService中调用。
*
* */
@Service
public class RedisPoolFactory {
    @Autowired
    RedisConfig redisConfig;

    @Bean   //JedisPool的实例注入到spring容器里面
    public JedisPool JedisPoolFactory() {
        JedisPoolConfig poolConfig=new JedisPoolConfig();
        System.out.println("redisConfig.getPoolMaxldle():"+redisConfig.getPoolMaxldle());
        System.out.println("redisConfig.getPoolMaxTotal():"+redisConfig.getPoolMaxTotal());
        System.out.println("redisConfig.getPoolMaxWait():"+redisConfig.getPoolMaxWait());
        System.out.println("redisConfig.getPassword():"+redisConfig.getPassword());
        poolConfig.setMaxIdle(redisConfig.getPoolMaxldle());
        poolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
        poolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait()*1000);//s-->ms
        //因为我们使用的是s（秒）来配置的，而源码使用的是ms（毫秒），所以转换一下
        JedisPool jp=new JedisPool(poolConfig,redisConfig.getHost(),redisConfig.getPort(),
                redisConfig.getTimeout()*1000,redisConfig.getPassword(),0);
        return jp;
    }
}
