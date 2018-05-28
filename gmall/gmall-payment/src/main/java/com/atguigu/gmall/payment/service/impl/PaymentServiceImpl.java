package com.atguigu.gmall.payment.service.impl;



import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.utils.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;


    /**
     * 保存支付信息
     * @param paymentInfo
     */
    public void savePaymentInfo(PaymentInfo paymentInfo){paymentInfoMapper.insertSelective(paymentInfo);

    }

    /**
     * 获取PaymentInfo
     * @return
     */
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery){
     return    paymentInfoMapper.selectOne(paymentInfoQuery);


    }

    /***
     * 根据outTradeNo 更新支付信息
     * @param outTradeNo
     * @param paymentInfo
     */
    public void updatePaymentInfoByOutTradeNo(String outTradeNo , PaymentInfo paymentInfo){
        Example example=new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);

        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);

    }
    /**
     * 测试消息队列
     */

    public void sendPaymentResult(String orderId){
        Connection connection = activeMQUtil.getConn();

        try {

            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");

            MapMessage mapMessage=new ActiveMQMapMessage();

            mapMessage.setString("orderId",orderId);

            mapMessage.setString("result","success");

            MessageProducer producer = session.createProducer(paymentResultQueue);
            producer.send(mapMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }



}
