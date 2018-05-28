package com.atguigu.gmall.payment.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;

import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Reference
    OrderService orderService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    AlipayClient alipayClient;


    /**
     * 去往支付页面
     * @param orderId
     * @param request
     * @return
     */
    @GetMapping("index")
    public String index(@RequestParam String orderId, HttpServletRequest request){
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());//设置总价格
        request.setAttribute("orderId",orderInfo.getId());//设置订单编号

        //返回首页
        return "index";

    }


    /**
     * 调用支付接口付款
     * @return
     */
    @PostMapping("/alipay/submit")
    @ResponseBody
    public ResponseEntity<String> alipaySubmit(HttpServletRequest request, HttpServletResponse response){
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        if (orderInfo==null){
            return ResponseEntity.badRequest().build();//返回错误的请求消息
        }
        //保存支付相关信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());//创建时间
        paymentInfo.setOrderId(orderId);//订单编号
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());//数量
        paymentInfo.setSubject(orderInfo.getOrderSubject());

        paymentService.savePaymentInfo(paymentInfo);

        //组织调用alipay
        AlipayTradeAppPayRequest alipayTradeAppPayRequest = new AlipayTradeAppPayRequest();
        alipayTradeAppPayRequest.setReturnUrl(AlipayConfig.return_order_url);
        alipayTradeAppPayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map bizMap= new HashMap<>();

        bizMap.put("out_true_no",orderInfo.getOutTradeNo());
        bizMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizMap.put("total_amount",orderInfo.getTotalAmount().toString());
        bizMap.put("subject",orderInfo.getOrderSubject());
        String bizContent = JSON.toJSONString(bizMap);
        String from=null;
        try {
             from = alipayClient.pageExecute(alipayTradeAppPayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

       response.setContentType("text/html;charset=utf-8" );

        return ResponseEntity.ok(from);

    }

    /**
     * 跳转到支付页面
     * @return
     */
    @GetMapping("/alipay/callback/retuen")
    public String callbackRetuen(){
        return "redirect://order.gmall.com/list";

    }

    /**
     * 发送回执
     */
    @PostMapping(value="/alipay/callback/notify")
    @ResponseBody
    public String callbackNotify(@RequestParam Map<String,String> paramMap) {
        System.out.println(" ----------callbackstart 支付宝开始回调"+paramMap.toString() );
        //验证签名
        boolean isCheckPass=false;
        try {

            isCheckPass = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(!isCheckPass){
            System.out.println(" ----------验签不通过！！"  );
            return "验签不通过！！";
        }
        System.out.println(" ----------验签通过！！"  );
        //验证成功标志
        String trade_status = paramMap.get("trade_status");
        if("TRADE_SUCCESS".equals(trade_status)){
            //检查当前支付状态
            String outTradeNo = paramMap.get("out_trade_no");
            PaymentInfo paymentInfoQuery=new PaymentInfo();
            paymentInfoQuery.setOutTradeNo(outTradeNo);
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
            if (paymentInfo==null) {
                return "error: not exists out_trade_no:"+outTradeNo;
            }
            System.out.println("检查是否已处理= " +outTradeNo  );
            if(paymentInfo.getPaymentStatus()==PaymentStatus.PAID){
                //如果已经处理过了 就直接返回成功标志
                System.out.println(" 已处理= " +outTradeNo  );
                return "success";
            }else {
                //先更新支付状态
                System.out.println(" 未处理，更新状态= " +outTradeNo  );
                PaymentInfo paymentInfo4Upt=new PaymentInfo();
                paymentInfo4Upt.setPaymentStatus(PaymentStatus.PAID);
                paymentInfo4Upt.setCallbackTime(new Date());
                String alipayTredeno = paramMap.get("trede_no");
                paymentInfo4Upt.setAlipayTradeNo(alipayTredeno);
                paymentInfo.setCallbackContent(paramMap.toString());
                paymentService.updatePaymentInfoByOutTradeNo(outTradeNo,paymentInfo4Upt);

                //发送通知给订单

                return  "success";

            }

        }
        return "fail";

    }

    /**
     * 测试消息队列
     */
    @PostMapping("/sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(String orderId){
        paymentService.sendPaymentResult(orderId);
        return "success";
    }





}
