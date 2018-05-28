

package com.atguigu.gmall.utils;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.stereotype.Component;

import javax.jms.Connection;

import javax.jms.JMSException;

/**
 * 消息队列工具类
 */
@Component
public class ActiveMQUtil {
        //创建连接工厂
    PooledConnectionFactory pooledConnectionFactory;
    //初始化连接
    public void init(String brokerUrl){

        //获取mq工厂对象
        ActiveMQConnectionFactory factory =
                new ActiveMQConnectionFactory(
                        //默认user & password
                        ActiveMQConnectionFactory.DEFAULT_USER
                        ,ActiveMQConnectionFactory.DEFAULT_PASSWORD,brokerUrl);
        //加入连接池
        pooledConnectionFactory = new PooledConnectionFactory(factory);
        //心跳检测 出现异常重新连接
        pooledConnectionFactory.setReconnectOnException(true);
        //设置最大连接数
        pooledConnectionFactory.setMaxConnections(15);
        pooledConnectionFactory.setExpiryTimeout(100000);


    }



    //获取连接

    public Connection getConn(){
        Connection connection = null;
        try {
            connection = pooledConnectionFactory.createConnection();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return connection;
    }

}
