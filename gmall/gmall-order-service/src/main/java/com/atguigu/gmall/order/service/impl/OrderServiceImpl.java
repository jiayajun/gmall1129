package com.atguigu.gmall.order.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.utils.ActiveMQUtil;
import com.atguigu.gmall.utils.RedisUtil;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;


@Service
public class OrderServiceImpl implements OrderService{
        @Autowired
        OrderInfoMapper orderInfoMapper;
        @Autowired
        OrderDetailMapper orderDetailMapper;
        @Autowired
        RedisUtil redisUtil;
        @Autowired
        ActiveMQUtil activeMQUtil;


    /**
     * 保存购买项
     */

    public String saveOrder(OrderInfo orderInfo){
        orderInfo.sumTotalAmount();//总金额
        orderInfo.setCreateTime(new Date());//创建时间
        orderInfo.setExpireTime(DateUtils.addDays(new Date(),1));//订单有效期
        //设置流水号
        String outTradeNo = "ATGUIGU-"+System.currentTimeMillis()+"-"+orderInfo.getUserId();
        orderInfo.setOutTradeNo(outTradeNo);

        orderInfoMapper.insertSelective(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();
    }

    /**
     * 生成tradNo
     * @return
     */
    public String getToken(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradNoKey = "user:"+userId+":tradNo";
        //生成UUID
        String tradeNo = UUID.randomUUID().toString();
        jedis.setex(tradNoKey,600,tradeNo);
        jedis.close();

        return tradeNo;
    }

    /**
     * 对比tradNo
     *防止表单重复提交
     * @return
     */
    public boolean verifyTradeNo(String userId,String tradeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradNoKey = "user:"+userId+":tradNo";
        String tradeNoValue = jedis.get(tradNoKey);
        jedis.close();
        if (tradeNoValue!=null&&tradeNoValue.equals(tradeNo)){
            return true;//
        }
        return false;
    }

    /**
     * 销毁tradeNo
     * @param userId
     */
    public void deleteTrsdeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradNoKey = "user:"+userId+":tradNo";
        jedis.del(tradNoKey);
        jedis.close();


    }

    //获取请求参数
    public OrderInfo getOrderInfo(String orderId){
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        OrderDetail orderDetailQuery = new OrderDetail();
        orderDetailQuery.setOrderId(orderInfo.getId());
        List<OrderDetail> select = orderDetailMapper.select(orderDetailQuery);
        orderInfo.setOrderDetailList(select);
        return orderInfo;

    }

    /**
     * 更新订单状态
     * @param orderId
     * @param processStatus
     */
    public void updateStatus(String orderId, ProcessStatus processStatus){

        OrderInfo orderInfo4Upt=new OrderInfo();
        orderInfo4Upt.setId(orderId);
        orderInfo4Upt.setProcessStatus(processStatus);
        orderInfo4Upt.setOrderStatus(processStatus.getOrderStatus());

        orderInfoMapper.updateByPrimaryKeySelective(orderInfo4Upt);
    }

    public void sendOrderResult(String orderId){
        OrderInfo orderInfo = getOrderInfo(orderId);
        //装配数据
        Map orderMap = initWareMap(orderInfo);
        //转json
        String wareOrderJson = JSON.toJSONString(orderMap);

        //发送消息
        Connection conn = activeMQUtil.getConn();
        try {
            Session session = conn.createSession(true, Session.SESSION_TRANSACTED);
            Queue orderResultQueue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(orderResultQueue);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText(wareOrderJson);

            producer.send(textMessage);
            session.commit();
            session.close();
            conn.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
    /**
     * 装配订单数据
     * @param orderInfo
     * @return
     */
     public Map initWareMap(OrderInfo orderInfo){
        Map orderMap=new HashMap();

        orderMap.put("orderId",orderInfo.getId());
        orderMap.put("consignee",orderInfo.getConsignee());
        orderMap.put("consigneeTel",orderInfo.getConsigneeTel());
        orderMap.put("orderComment",orderInfo.getOrderComment());
        orderMap.put("orderBody",orderInfo.getOrderSubject());
        orderMap.put("deliveryAddress",orderInfo.getDeliveryAddress());
        orderMap.put("paymentWay","2");
        List<Map> detailList=new ArrayList<>(orderInfo.getOrderDetailList().size());
        List<OrderDetail> orderDetailList= orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap =new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);
        }

        orderMap.put("details",detailList);

        return  orderMap;


    }



}
