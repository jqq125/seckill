package com.kiki.seckill.common.access;

import com.kiki.seckill.pojo.SeckillUser;

public class UserContext {
    private static ThreadLocal<SeckillUser> userHolder=new ThreadLocal<SeckillUser>();

    public static void setUser(SeckillUser user) {
        userHolder.set(user);
    }

    public static SeckillUser getUser() {
        return userHolder.get();
    }
}
