package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    /**
     * 获取cookie中的值
     * @param userId
     * @return
     */
    public List<CartInfo> getCartChecked(String userId);
    /**
     * 登录时购物车中数据
     * @param cartInfo
     * @return
     */
    public CartInfo addToCart(CartInfo cartInfo);


    /**
     * 购物车数据列表
     * @param userId
     * @return
     */
    public List<CartInfo> getCartList(String userId);

    /**
     * 加载缓存
     *
     * @param userId
     * @return
     */
    public  List<CartInfo>  loadCartCache(String userId);

    /**
     * cookie中的数据合登陆后并到数据库
     */
    public List<CartInfo> mergeToCart(List<CartInfo> cartInfoListCookie,String userId);

    /**
     * 处理购物车的购物项是否被选中
     */
    public void chackCart(String skuId,String userId,String isChecked);

    /**
     * 删除购物车中的选项中项
     */
    public void delCartChecked(String userId);
}
