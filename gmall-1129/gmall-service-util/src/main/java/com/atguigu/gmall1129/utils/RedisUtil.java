package com.atguigu.gmall1129.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @param
 * @return
 */
public class RedisUtil {

     JedisPool jedisPool;

     int timeout=500;

     public void initJedisPool(String host,int port){
         JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
         jedisPoolConfig.setMaxTotal(20);
         jedisPoolConfig.setMinIdle(5);
         jedisPoolConfig.setMaxIdle(10);
         jedisPoolConfig.setBlockWhenExhausted(true);
         jedisPoolConfig.setMaxWaitMillis(300);
         jedisPoolConfig.setTestOnBorrow(true);

         jedisPool=new JedisPool(jedisPoolConfig,host,port,timeout);
     }

     public Jedis getJedis(){
         Jedis jedis = jedisPool.getResource();
         return jedis;
     }


}
