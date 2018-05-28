package com.atguigu.gmall.manager.constant;

public class RedisConst {
    //前缀
    public static final String SKU_PREFIX = "sku:";
    //后缀
    public static final String SKU_SUFFIX = ":info";
    //失效时间
    public static final int  SKU_OUTTIME = 6000;
    //锁名称
    public static final String LOCK = "lock";
}
