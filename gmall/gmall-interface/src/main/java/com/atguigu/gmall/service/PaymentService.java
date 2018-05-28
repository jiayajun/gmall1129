package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {


    /**
     * 保存支付信息
     * @param paymentInfo
     */
    public void savePaymentInfo(PaymentInfo paymentInfo);
    /**
     * 获取PaymentInfo
     * @return
     */
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    /***
     * 根据outTradeNo 更新支付信息
     * @param outTradeNo
     * @param paymentInfo
     */
    public void updatePaymentInfoByOutTradeNo(String outTradeNo , PaymentInfo paymentInfo);


    /**
     * 测试消息队列
     */

    public void sendPaymentResult(String orderId);
}
