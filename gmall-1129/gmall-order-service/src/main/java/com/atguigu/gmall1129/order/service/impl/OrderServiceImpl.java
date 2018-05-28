package com.atguigu.gmall1129.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.OrderDetail;
import com.atguigu.gmall1129.bean.OrderInfo;
import com.atguigu.gmall1129.bean.PaymentInfo;
import com.atguigu.gmall1129.enums.PaymentStatus;
import com.atguigu.gmall1129.enums.ProcessStatus;
import com.atguigu.gmall1129.order.mapper.OrderDetailMapper;
import com.atguigu.gmall1129.order.mapper.OrderInfoMapper;
import com.atguigu.gmall1129.service.OrderService;
import com.atguigu.gmall1129.service.PaymentService;
import com.atguigu.gmall1129.utils.ActiveMQUtil;
import com.atguigu.gmall1129.utils.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

/**
 * @param
 * @return
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;


    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;


    @Reference
    PaymentService paymentService;

    public  String saveOrder(OrderInfo orderInfo){
        orderInfo.sumTotalAmount();
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(DateUtils.addDays(new Date(),1));

        String outTradeNo="ATGUIGU-"+System.currentTimeMillis()+"-"+orderInfo.getUserId();
        orderInfo.setOutTradeNo(outTradeNo);

        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();

    }


    //1 生成tradeNo  2 验证tradeNo 3 销毁tradeNo
    public String genTradeNo(String userId){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeNo";
        String tradeNo = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey,600,tradeNo);

        jedis.close();

        return tradeNo;
    }


    public boolean verifyTradeNo(String userId,String tradeNo){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeNo";
        String tradeNoExpected = jedis.get(tradeNoKey);
        jedis.close();
        if(tradeNoExpected!=null&&tradeNoExpected.equals(tradeNo)){
            return true;
        }
        return false;

    }

    public void delTradeNo(String userId ){
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey="user:"+userId+":tradeNo";
        jedis.del(tradeNoKey);
        jedis.close();

    }

    public OrderInfo getOrderInfo(String orderId){
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        OrderDetail orderDetailQuery=new OrderDetail();
        orderDetailQuery.setOrderId(orderInfo.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetailQuery);
        orderInfo.setOrderDetailList(orderDetailList);
        return orderInfo;

    }


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

    public List<OrderInfo> checkExpireOrder(){

        Example example =new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("processStatus",ProcessStatus.UNPAID.name()).andLessThan("expireTime",new Date());

        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        return  orderInfoList;
    }


    public void handleExpireOrder(OrderInfo orderInfo){
        ///
/*        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        System.out.println("处理订单："+orderInfo.getId());
        PaymentInfo paymentInfoQuery = new PaymentInfo();
        paymentInfoQuery.setOrderId(orderInfo.getId());
        PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
        if(paymentInfo==null){
            return ;
        }
        if(paymentInfo.getPaymentStatus()== PaymentStatus.PAID){
            System.out.println("订单已支付："+orderInfo.getId());
            updateStatus(orderInfo.getId(),ProcessStatus.PAID);
            //发送库存
        }else{
            System.out.println("订单未支付,关闭订单："+orderInfo.getId());
            updateStatus(orderInfo.getId(),ProcessStatus.CLOSED);
        }

    }
}
