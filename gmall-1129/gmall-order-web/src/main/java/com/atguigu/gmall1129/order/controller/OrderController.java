package com.atguigu.gmall1129.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.*;
import com.atguigu.gmall1129.config.LoginRequire;
import com.atguigu.gmall1129.enums.OrderStatus;
import com.atguigu.gmall1129.enums.ProcessStatus;
import com.atguigu.gmall1129.service.CartService;
import com.atguigu.gmall1129.service.ManageService;
import com.atguigu.gmall1129.service.OrderService;
import com.atguigu.gmall1129.service.UserService;
import com.atguigu.gmall1129.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * @param
 * @return
 */

@Controller
public class OrderController {


    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    ManageService manageService;

    @GetMapping("trade")
    @LoginRequire(autoRedirect = true,debugUser = "1")
    public String trade(  HttpServletRequest request){
        String userId = (String)request.getAttribute("userId");

        List<UserAddress> userAddressList=userService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);

        List<CartInfo> cartCheckedList = cartService.getCartChecked(userId);
        request.setAttribute("cartCheckedList",cartCheckedList);
        BigDecimal orderTotalAmount=new BigDecimal("0");
        for (CartInfo cartInfo : cartCheckedList) {
            BigDecimal totalAmount = cartInfo.getTotalAmount();
            orderTotalAmount = orderTotalAmount.add(totalAmount);

        }
        request.setAttribute("orderTotalAmount",orderTotalAmount);
      //生成流水号 防止重复提交
        String tradeNo = orderService.genTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);

        return "trade";
    }

    @PostMapping("submitOrder")
    @LoginRequire(autoRedirect = true,debugUser = "1")
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId = (String)request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");

        orderInfo.setUserId(userId);
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);


        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        //1 验证页面有效性
        boolean ifTradeNoExists=false;
        if(tradeNo!=null){
              ifTradeNoExists = orderService.verifyTradeNo(userId, tradeNo);
        }
        if(tradeNo==null||!ifTradeNoExists){
            String errMsg= "结算页面已失效，请重新下单。";
            request.setAttribute("errMsg",errMsg);
            return "tradeFail";
        }

        //2 、验价
        for (OrderDetail orderDetail : orderDetailList) {

            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            if(skuInfo.getPrice().compareTo(orderDetail.getOrderPrice())!=0){
                String errMsg= "您购买的商品["+skuInfo.getSkuName()+"]价已发生变动，请重新下单。";
                cartService.loadCartCache(userId);
                request.setAttribute("errMsg",errMsg);
                return "tradeFail";
            }
            orderDetail.setSkuName(skuInfo.getSkuName());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());

            // 3验库存
            String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuInfo.getId() + "&num=" + orderDetail.getSkuNum());
            if(!"1".equals(result)){
                String errMsg= "您购买的商品["+skuInfo.getSkuName()+"]价已缺货，请重新下单。";
                request.setAttribute("errMsg",errMsg);
                return "tradeFail";
            }


        }



        //1 、验价 2 验库存 3 保存
        String orderId =orderService.saveOrder(orderInfo);
        orderService.delTradeNo(userId);
        cartService.delCartChecked(userId);
        return "redirect://payment.gmall.com/index?orderId="+orderId;

    }
}
