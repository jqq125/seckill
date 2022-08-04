package com.kiki.seckill.common.utils;

import java.util.UUID;

public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");//去掉原生的"-"，因为原生会带有"-"
    }
}
