package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;

import java.util.Map;

public interface OrderService {

    /**
     * 保存购买项
     */
    public String saveOrder(OrderInfo orderInfo);
    /**
     * 生成tradNo
     * @return
     */
    public String getToken(String userId);

    /**
     * 对比tradNo
     *防止表单重复提交
     * @return
     */
    public boolean verifyTradeNo(String userId,String tradeNo);

    /**
     * 销毁tradeNo
     * @param userId
     */
    public void deleteTrsdeNo(String userId);

    //获取请求参数
    public OrderInfo getOrderInfo(String orderId);
    /**
     * 更新订单状态
     * @param orderId
     * @param processStatus
     */
    public void updateStatus(String orderId, ProcessStatus processStatus);
    public void sendOrderResult(String orderId);
    public Map initWareMap(OrderInfo orderInfo);
}
