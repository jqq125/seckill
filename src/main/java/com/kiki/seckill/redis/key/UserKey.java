package com.kiki.seckill.redis.key;

public class UserKey extends BasePrefix{
    public UserKey(String prefix) {
        //调用父类中声明的构造函数，必须在子类构造器的首行声明
        super(prefix);
    }
    public static UserKey getById=new UserKey("id");
    public static UserKey getByName=new UserKey("name");
}

