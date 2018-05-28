package com.atguigu.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;

import javax.jms.*;

/**
 * 测试生产端
 */
public class TestProduser {
    public static void main(String[] args) throws JMSException {
    //获取mq工厂对象
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory(
                        //默认user & password
                        ActiveMQConnectionFactory.DEFAULT_USER
                        ,ActiveMQConnectionFactory.DEFAULT_PASSWORD,
                        //地址
                        "tcp://192.168.111.130:61616");
        //获取连接
        Connection connection1 = activeMQConnectionFactory.createConnection();
        //开启连接
        connection1.start();
        //前面为true后面必须为0   获取会话
        Session session = connection1.createSession(true, Session.SESSION_TRANSACTED);
        //创建队列
        Queue queue = session.createQueue("TEST_QUEUE");//起名字为 测试消息队列
        //创建生产者
        MessageProducer producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);//是否持久化
        //创建要发sing的消息
        ActiveMQTextMessage message = new ActiveMQTextMessage();
        message.setText("我爱李艳丽");
        //发送消息

        producer.send(message);
        session.commit();

        connection1.close();
        session.close();


    }
}
