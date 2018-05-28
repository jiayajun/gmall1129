package com.atguigu.gmall.utils;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
public class RedisUtil {

    private  JedisPool jedisPool;


//初始化连接池
    public void initPool(String host,int port ){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        //最大连既然数
        poolConfig.setMaxTotal(200);
        //最大空闲数
        poolConfig.setMaxIdle(30);
        //最小空闲数
        poolConfig.setMinIdle(5);
        //并发时是否等待
        poolConfig.setBlockWhenExhausted(true);
        //超时时间
        poolConfig.setMaxWaitMillis(10*1000);
        //测试连接是否正常
        poolConfig.setTestOnBorrow(true);
        //创建连接池对象                                失效时间
        jedisPool=new JedisPool(poolConfig,host,port,20*1000);

        System.out.println(jedisPool.toString());
    }
    //获取连接
    public Jedis getJedis(){
        initPool("192.168.111.130",6379);

        Jedis jedis = jedisPool.getResource();

        return jedis;
    }

}
