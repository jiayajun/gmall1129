package com.atguigu.gmall.usermanager.service.serviceimpl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.usermanager.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanager.mapper.UserMapper;

import com.atguigu.gmall.utils.RedisUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.assertj.core.api.FactoryBasedNavigableListAssert;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper usermapper;
    @Autowired
  RedisUtil redisUtil;



    /**
     * 添加员工
     * @param userinfo
     */
    public void addUserInfo(UserInfo userinfo){
        usermapper.insert(userinfo);

    }
    /**
     * 查询所有员工
     */

    @Override
    public List<UserInfo> users() {
        return usermapper.selectAll();
    }

    /**
     * 查询的那个员工
     * @param id
     * @return
     */
    @Override
    public UserInfo getUserInfo(String id){

        return usermapper.selectByPrimaryKey(id);

    }





    @Override
    public List<UserInfo> getUserList(UserInfo userInfo) {
        List<UserInfo> userInfos = usermapper.selectAll();
        return userInfos;
    }



    /**
     * 删除员工
     * @param id
     */
    @Override
    public void deleteUser(String id) {
        usermapper.deleteByPrimaryKey(id);
    }
    /**
     * 修改员工方法
     */
    @Override
    public void updateUser(UserInfo userinfo) {
        usermapper.updateByPrimaryKeySelective(userinfo);
    }


    /**
     * 引入用户收货地址
     */
    @Autowired
    private UserAddressMapper userAddressMapper;

    /**
     * 查询用户收货地址
     */
    public List<UserAddress> getUserAddressList(String userId){
        UserAddress userAddressQuery = new UserAddress();

       String id = userAddressQuery.setId(userId);

       return  userAddressMapper.select(userAddressQuery);



    }

    //处理用户登录方法
    public UserInfo login(UserInfo userInfo){

        //1.密码转义
            //渠道转义后的密码
            String md5Hex = DigestUtils.md5Hex(userInfo.getPasswd());
            //将转义后的密码赋给数据库中要比较的密码
        userInfo.setPasswd(md5Hex);

        //2.验证数据库
            //返回登录后的用户
             UserInfo selectLogin = usermapper.selectOne(userInfo);

             //非空验证
        if (selectLogin==null){
            return null;

        }

        //3.保存缓存
        Jedis jedis = redisUtil.getJedis();
        //拼接存入值的key
        String userInfoKey = "user"+selectLogin.getId()+":"+"info";
        //转换为json串
        String userInfoJson = JSON.toJSONString(selectLogin);

        jedis.setex(userInfoKey,3600,userInfoJson);

        return selectLogin;


    }

    //验证方法
    public boolean verify(String userId){
        Jedis jedis = redisUtil.getJedis();
        String userInfoKey = "user"+userId+":"+"info";

        Boolean exists = jedis.exists(userInfoKey);
        if (exists) {

            jedis.expire(userInfoKey, 1800);
        }
        return exists;

    }

}
