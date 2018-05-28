package com.atguigu.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.handler.CartCookieHandler;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;

import com.atguigu.gmall.service.ManagerService;
import org.apache.catalina.startup.RealmRuleSet;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 吃力购物车业务类
 */
@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Reference
    ManagerService managerService;

    @Autowired
    CartCookieHandler cartCookieHandler;



    //页面跳转
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, CartInfo cartInfo, HttpServletResponse response) {

        SkuInfo skuInfo = managerService.getSkuInfo(cartInfo.getSkuId());

        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());//图片
        cartInfo.setSkuName(skuInfo.getSkuName());//名字
        cartInfo.setCartPrice(skuInfo.getPrice());//价格
        //从页面获取用户id
        String userId = (String) request.getAttribute("userId");
        //判断用户id是否为空决定用户是否登录
        //登录状态
        if (userId != null) {
            //如果状态为登录则将userID设置为上面的对象
            cartInfo.setUserId(userId);
            cartService.addToCart(cartInfo);

        } else {
            //未登录状态
            cartCookieHandler.addToCart(request,response,cartInfo);

        }
        //将对象放到请求域中
        request.setAttribute("cartInfo", cartInfo);

        return "success";
    }


    //购物车列表
    @GetMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String getCartList(HttpServletRequest request,HttpServletResponse response){
        //判断是否登录的依据是否能从域中或区域到用户id
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = null;

        //登录后将未登录时的cookie数据更新到数据库和缓存中
        List<CartInfo> cartInfoListCookie = cartCookieHandler.getCartList(request);


        if (userId!=null){
            if (cartInfoListCookie==null||cartInfoListCookie.size()==0){
                cartInfoList = cartService.getCartList(userId);
            }else{
                //登录后合并数据
                cartInfoList =  cartService.mergeToCart(cartInfoListCookie,userId);
                cartCookieHandler.deleteCartList(request,response);
            }


        }else {
            cartInfoList = cartInfoListCookie;
        }
        request.setAttribute("cartList",cartInfoList);

        return "cartList";
    }

    /**
     * 处理购物车中的购物项的选中状态
     */
    public String chackCart(HttpServletRequest request){
        //获取域中存的值
        String userId = (String)request.getAttribute("userId");
        String skuId = (String)request.getAttribute("skuId");
        String isChecked = (String)request.getAttribute("isChecked");
        //处理
        cartService.chackCart(skuId,userId,isChecked);

        return "success";

    }

}
