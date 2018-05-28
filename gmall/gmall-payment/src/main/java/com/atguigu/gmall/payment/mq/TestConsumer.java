package com.atguigu.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.xml.soap.Text;

/**
 * 测试消费端
 */
public class TestConsumer {
    public static void main(String[] args) throws JMSException {
        //获取mq工厂对象
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory(
                        //默认user & password
                        ActiveMQConnectionFactory.DEFAULT_USER
                        , ActiveMQConnectionFactory.DEFAULT_PASSWORD,
                        //地址
                        "tcp://192.168.111.130:61616");
        //获取连接
        Connection connection1 = activeMQConnectionFactory.createConnection();
        //开启连接
        connection1.start();
        //前面为false后面必须不为0   获取会话
        Session session = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE);//1自动签收，2手动签收
        //创建队列
        Queue queue = session.createQueue("TEST_QUEUE");//起名字为 测试消息队列
        //创建消费者
        MessageConsumer consumer = session.createConsumer(queue);
        //将消费者实现持久化
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                String text1;
                try {
                    text1 = textMessage.getText();
                    System.out.println(text1);
                } catch (JMSException e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
