package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManagerService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.standard.expression.OrExpression;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class OrderController {
    @Reference
    private UserService userService;
    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    ManagerService managerService;

    @GetMapping("/trade")
    @LoginRequire
    public String trade(HttpServletRequest request) {
        //获取用户id、
        String userId = (String) request.getAttribute("userId");
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList", userAddressList);

        List<CartInfo> cartCheckedList = cartService.getCartChecked(userId);
        request.setAttribute("cartCheckedList", cartCheckedList);

        //总金额
        BigDecimal orderTotalaAmount = new BigDecimal("0");
        for (CartInfo cartInfo : cartCheckedList) {
            BigDecimal totalAmount = cartInfo.getTotalAmount();
            orderTotalaAmount = orderTotalaAmount.add(totalAmount);
        }
        //放到域中
        request.setAttribute("orderTotalaAmount", orderTotalaAmount);

        return "reade";
    }


    /**
     * 下单方法
     */

    @PostMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request) {

        //保存
        String userId = (String) request.getAttribute("userId");//获取登录用户的id

        orderInfo.setUserId(userId);
        orderInfo.setOrderStatus(OrderStatus.UNPAID);//初始化为未支付状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);//初始化为未支付状态


        //获取页面的tradeNo
        String tradeNoWeb = (String) request.getParameter("tradeNo");

        //生成token
        String tradeNo = orderService.getToken(userId);
        request.setAttribute("tradeNo", tradeNo);


        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //验证页面有效性
        boolean ifTradeNoExists = false;
        if (tradeNoWeb != null) {

            //对比token
            ifTradeNoExists = orderService.verifyTradeNo(userId, tradeNoWeb);
        }
        if (tradeNoWeb == null || !ifTradeNoExists) {
            String errMsg = "亲，结算页面失效，请重新下单";
            //将错误消息返回页面
            request.setAttribute("errMsg", errMsg);
            return "tradeFail";
        }
        //验证价格
        for (OrderDetail orderDetail : orderDetailList) {

            SkuInfo skuInfo = managerService.getSkuInfo(orderDetail.getSkuId());
            if (skuInfo.getPrice().compareTo(orderDetail.getOrderPrice()) != 0) {
                String errMsg = "亲，手慢了，您的商品【" + skuInfo.getSkuName() + "】价格发生变动请重新下单";
                cartService.loadCartCache(userId);//重新加载缓存
                //将错误消息返回页面
                request.setAttribute("errMsg", errMsg);
                return "tradeFail";
            }
            orderDetail.setSkuName(skuInfo.getSkuName());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());

            //验证库存

            String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuInfo.getId() + "&num=" + orderDetail.getSkuName());


            if (!"1".equals(result)) {
                if (skuInfo.getPrice().compareTo(orderDetail.getOrderPrice()) != 0) {
                    String errMsg = "亲，手慢了，您的商品【" + skuInfo.getSkuName() + "】没货了，下次光临";
                    //将错误消息返回页面
                    request.setAttribute("errMsg", errMsg);
                    return "tradeFail";

                }
            }

        }
            String orderId = orderService.saveOrder(orderInfo);//保存
            orderService.deleteTrsdeNo(userId);//保存成功后删除tradeNo
            cartService.delCartChecked(userId);
            //重定向到支付页面
            return "redirect://payment.gmall.com/index?orderId=" + orderId;



    }



}

