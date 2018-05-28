package com.atguigu.gmall1129.service;

import com.atguigu.gmall1129.bean.OrderInfo;
import com.atguigu.gmall1129.enums.ProcessStatus;

import java.util.List;

/**
 * @param
 * @return
 */
public interface OrderService {

    public  String saveOrder(OrderInfo orderInfo);

    public String genTradeNo(String userId);

    public boolean verifyTradeNo(String userId,String tradeNo);

    public void delTradeNo(String userId );

    public OrderInfo getOrderInfo(String orderId);

    public void updateStatus(String orderId, ProcessStatus processStatus);

    public void sendOrderResult(String orderId);

    public List<OrderInfo> checkExpireOrder();

    public void handleExpireOrder(OrderInfo orderInfo);
}
