package com.atguigu.gmall.utils;


import com.atguigu.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisConfig{

    //获取配置文件中的地址
    @Value("${spring.server.host:disabled}")
    String redisHost;
    //获取配置文件中的端口号
    @Value("${spring.redis.port:0}")
    int redisProt;


    /**
     * 创建连接对象
     * @return
     */
    @Bean
    public RedisUtil getRedisUtils(){
        //如果是默认的返回null
        if (redisHost.equals("disabled")){
            return null;
        }
        //否则创建一个连接返回
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.initPool(redisHost,redisProt);
        return redisUtil;

    }


}
