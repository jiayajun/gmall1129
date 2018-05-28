package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManagerService;
import com.atguigu.gmall.service.mapper.CartInfoMapper;
import com.atguigu.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;


@Service
public class CartServiceImpl implements CartService{

    //注入mapper
    @Autowired
    CartInfoMapper cartInfoMapper;
    @Reference
    ManagerService managerService;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 添加购物项
     * @param cartInfo
     */
    public CartInfo addToCart(CartInfo cartInfo){


        CartInfo cartInfo1Query = new CartInfo();
        cartInfo1Query.setUserId(cartInfo.getUserId());
        cartInfo1Query.setSkuId(cartInfo.getSkuId());
        //1判断数据库是否有相同数据   如果有数据累加   如果没有就加一条
        CartInfo cartInfoExists = cartInfoMapper.selectOne(cartInfo1Query);
            //数据库中有相同数据时，num累加
        if (cartInfoExists!=null){

                //数据库中的数据和新添加的数据个数累加
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum()+cartInfo.getSkuNum());
            cartInfo.setSkuPrice(cartInfoExists.getCartPrice());//价格
            cartInfo.setImgUrl(cartInfoExists.getImgUrl());//图片
            cartInfo.setSkuName(cartInfoExists.getSkuName());

         //饭后最新数据
            cartInfoMapper.updateByPrimaryKey(cartInfoExists);
        }else{
            //数据库不存在是插入新数据
            SkuInfo skuInfo = managerService.getSkuInfo(cartInfo.getSkuId());
            //更新相关数据
            cartInfo.setSkuPrice(skuInfo.getPrice());//价格
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());//图片
            cartInfo.setSkuName(skuInfo.getSkuName());

            cartInfoMapper.insertSelective(cartInfo);
        }

        //缓存处理
        //将数据存入redis缓存中使用（hash便于精准定位）
        //确定存入的key
        String cartKey = "user:"+cartInfo.getUserId()+":cart";

        Jedis jedis = redisUtil.getJedis();
        String cartInfoJson = jedis.hget(cartKey, cartInfo.getSkuId());
        CartInfo cartInfoExistsRedis = JSON.parseObject(cartInfoJson, CartInfo.class);
        //非空检验
        if (cartInfoExistsRedis!=null){
            //存入缓存的数据总量累加（缓存中有数据书 累加）
            cartInfoExistsRedis.setSkuNum(cartInfoExistsRedis.getSkuNum()+cartInfo.getSkuNum());
            String cartInfoNewJson = JSON.toJSONString(cartInfoExistsRedis);
            //将数据序列化回去
            jedis.hset(cartKey,cartInfo.getSkuId(),cartInfoNewJson);

        }else{
            //缓存中不存在时 加入
            String cartInfoNewJson = JSON.toJSONString(cartInfo);

            jedis.hset(cartKey,cartInfo.getSkuId(),cartInfoNewJson);
            jedis.close();
        }
        return cartInfo;
    }

    /**
     * 购物车数据列表
     * @param userId
     * @return
     */

    public List<CartInfo> getCartList(String userId){
        //获取缓存中的数据  如果吗没有  查询数据库  并且存到缓存中一份

        //查询缓存
        Jedis jedis = redisUtil.getJedis();

        String cartKey = "user:"+userId+":cart";
        List<CartInfo> cartInfoList = new ArrayList<>();//创建新集合
        List<String> cartJsonList = jedis.hvals(cartKey);
        //如果有值就转换为对象并获取（反序列化）
        if (cartJsonList!=null&&cartJsonList.size()>0) {
            for (String cartJson : cartJsonList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                //存入新集合
                cartInfoList.add(cartInfo);
            }
        }else {

           cartInfoList = loadCartCache(userId);

        }
        //排序(快排)
        cartInfoList.sort(new Comparator<CartInfo>() {
            @Override
            //倒序
            public int compare(CartInfo o1, CartInfo o2) {
                return o2.getId().compareTo(o1.getImgUrl());
            }
        });
        return cartInfoList;


    }


    /**
     * 加载缓存
     *
     * @param userId
     * @return
     */
    public  List<CartInfo>  loadCartCache(String userId){
        List<CartInfo> cartList = cartInfoMapper.selectCartInfoWithSkuPrice(Long.parseLong(userId));

        Map cartMap = new HashMap();
        if (cartList!=null&&cartList.size()>0){
            for (CartInfo cartInfo : cartList) {
                String cartJson = JSON.toJSONString(cartInfo);
                cartMap.put(cartInfo.getSkuId(),cartJson);
            }

        }
        Jedis jedis = redisUtil.getJedis();
        String cartKey = "user:"+userId+":cart";
        jedis.hmset(cartKey,cartMap);
        Long ttl = jedis.ttl(cartKey);
        jedis.expire(cartKey,ttl.intValue());
jedis.close();
        return cartList;
    }


    /**
     * cookie中的数据合登陆后并到数据库
     */
    public List<CartInfo> mergeToCart(List<CartInfo> cartInfoListCookie,String userId){
        //获取后天数据
        final List<CartInfo> cartInfoListExists = cartInfoMapper.selectCartInfoWithSkuPrice(Long.parseLong(userId));
        //数据库与cookie进行匹配 成功则相加数据   否则插入新数据
        if (cartInfoListExists!=null&&cartInfoListExists.size()>0){
            for (CartInfo cartInfoCookie : cartInfoListCookie) {
                boolean ifExists = false;
                for (CartInfo cartInfoExist : cartInfoListExists) {
                    //对比
                    if (cartInfoCookie.getSkuId().equals(cartInfoExist.getSkuId())){
                        //相同时  数量相加
                        cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+cartInfoCookie.getSkuNum());
                        //更新数据库
                        cartInfoMapper.updateByPrimaryKey(cartInfoExist);
                        //返回true
                        ifExists = true;
                    }
                }
                //数据库中没有和cookie匹配的值
                if (!ifExists){
                    //将登陆后的用户id设置到cookie中
                    cartInfoCookie.setUserId(userId);
                    //将cookie中的数据添加到数据库中
                    cartInfoMapper.insertSelective(cartInfoCookie);
                }
            }
        }
        //添加到缓存(调用上面的方法)重新加载
        List<CartInfo> cartInfoList = loadCartCache(userId);
        //返回缓存中的数据
        return cartInfoList;


    }

    /**
     * 处理购物车的购物项是否被选中
     */
    public void chackCart(String skuId,String userId,String isChecked){
        //写入选中状态
        Jedis jedis = redisUtil.getJedis();
        String cartKey = "user:"+userId+":cart";
        String cartJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);//改变选中状态
        String cartInfoNewJson = JSON.toJSONString(cartInfo);
        jedis.hset(cartKey,skuId,cartInfoNewJson);
        //添加或者删除勾选状态
        String checkedKey = "user:"+userId+":checked";
        if ("1".equals(isChecked)){

            jedis.hset(checkedKey,skuId,cartInfoNewJson);
        }else {
            //删除
            jedis.hdel(checkedKey,skuId);
        }
        jedis.close();

    }

    /**
     * 获取cookie中的值
     * @param userId
     * @return
     */
    public List<CartInfo> getCartChecked(String userId){
        //制作key
        String checkedKey = "user:"+userId+":checked";
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckJsonList = jedis.hvals(checkedKey);

        List<CartInfo> cartInfoList = new ArrayList<>(cartCheckJsonList.size());
        for (String cartCheckJson : cartCheckJsonList) {
            CartInfo cartInfo = JSON.parseObject(cartCheckJson, CartInfo.class);
            cartInfoList.add(cartInfo);
        }
        jedis.close();
        return cartInfoList;
    }

    /**
     * 删除购物车中的选项中项
     */
    public void delCartChecked(String userId){
        //制作key
        String checkedKey = "user:"+userId+":checked";
        String cartKey = "user:"+userId+":cart";
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        Set<String> skuIdSet = jedis.hkeys(checkedKey);
        for (String skuId : skuIdSet) {
            CartInfo cartInfoQuery = new CartInfo();
            cartInfoQuery.setUserId(userId);
            cartInfoMapper.delete(cartInfoQuery);
         jedis.hdel(cartKey,skuId);
        }
         jedis.del(checkedKey);

        jedis.close();

    }



}