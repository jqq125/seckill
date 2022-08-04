package com.kiki.seckill.service;

import com.kiki.seckill.redis.key.BasePrefix;

/**
 * 暂时不设置过期时间
 * @author 17996
 *
 */
public class OrderKey extends BasePrefix {

    public OrderKey(String prefix) {
        super(prefix);
    }
    public static OrderKey getSeckillOrderByUidAndGid=new OrderKey("seckill_uidgid");

}
