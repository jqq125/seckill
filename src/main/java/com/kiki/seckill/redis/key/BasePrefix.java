package com.kiki.seckill.redis.key;

/*
* */
public abstract class BasePrefix implements KeyPrefix{
    private int expireSeconds;
    private String prefix;
    //构造方法
    public BasePrefix() {
    }
    public BasePrefix(String prefix) {
        //this(0, prefix);//默认使用0，不会过期
        this.expireSeconds=0;
        this.prefix=prefix;
    }
    public BasePrefix(int expireSeconds,String prefix) {//覆盖了默认的构造函数
        this.expireSeconds=expireSeconds;
        this.prefix=prefix;
    }

    //默认为0代表永不过期
    public int expireSeconds() {
        return expireSeconds;
    }
    //前缀为:类名+prefix
    public String getPrefix() {
        String className=getClass().getSimpleName();
        return className+"_"+prefix;
    }
}
