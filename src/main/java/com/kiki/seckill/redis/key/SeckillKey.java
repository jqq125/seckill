package com.kiki.seckill.redis.key;

public class SeckillKey extends BasePrefix{
    //考虑页面缓存有效期比较短
    public SeckillKey(int expireSeconds,String prefix) {
        super(expireSeconds,prefix);
    }
    public static SeckillKey isGoodsOver=new SeckillKey(0,"go");
    //有效期60s
    public static SeckillKey getSeckillPath=new SeckillKey(60,"mp");
    //验证码   300s有效期
    public static SeckillKey getSeckillVertifyCode=new SeckillKey(300,"vc");


}
