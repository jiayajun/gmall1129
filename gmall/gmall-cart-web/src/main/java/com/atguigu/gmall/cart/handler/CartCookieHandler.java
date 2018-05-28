package com.atguigu.gmall.cart.handler;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.utils.CookieUtil;
import org.apache.catalina.connector.Response;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.generator.FalseMethodPlugin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 将数据存入到cookie缓存中
 */
@Component
public class CartCookieHandler {

    public void addToCart(HttpServletRequest request, HttpServletResponse response, CartInfo cartInfo) {

        //设置失效时间
        int cartCokieMaxage = 3600 * 7 * 24;
        //使用cookie工具类获取cookie                               是否要编码取决于传入数据是否有中文
        //多个
        String cartListJson = CookieUtil.getCookieValue(request, "cart", true);
        //将json串转换为列表
        List<CartInfo> cartInfoList = JSON.parseArray(cartListJson, CartInfo.class);
        boolean ifExists = false;
        if (cartInfoList != null && cartInfoList.size() > 0) {
            for (CartInfo cartInfoExists : cartInfoList) {
                //同一个用户数量相加
                if (cartInfoExists.getSkuId().equals(cartInfo.getSkuId())) {
                    cartInfoExists.setSkuNum(cartInfoExists.getSkuNum() + cartInfo.getSkuNum());
                    ifExists = true;
                }
            }
        } else {
            //如果列表为空创建一个新的列表
            cartInfoList = new ArrayList<>();

        }
        if (!ifExists) {
            cartInfoList.add(cartInfo);
        }
        String cartInfoListNewJson = JSON.toJSONString(cartInfoList);

        CookieUtil.setCookie(request, response, "cart", cartInfoListNewJson, cartCokieMaxage, true);

    }

    /**
     * 查询列表方法
     */

    public List<CartInfo> getCartList(HttpServletRequest request) {
        //使用cookie工具类获取cookie                               是否要编码取决于传入数据是否有中文
        //多个
        String cartListJson = CookieUtil.getCookieValue(request, "cart", true);
        //将json串转换为列表
        List<CartInfo> cartInfoList = JSON.parseArray(cartListJson, CartInfo.class);

        return cartInfoList;
    }

    /**
     * 清空cookie中的值
     */
    public void deleteCartList(HttpServletRequest request,HttpServletResponse response) {
        //向cookie中设置一个空值
        CookieUtil.setCookie(request,response,"cart",null,0,false);


    }


}
