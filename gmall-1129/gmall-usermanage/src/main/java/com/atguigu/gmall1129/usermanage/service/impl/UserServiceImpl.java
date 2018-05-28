package com.atguigu.gmall1129.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.UserAddress;
import com.atguigu.gmall1129.bean.UserInfo;
import com.atguigu.gmall1129.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall1129.usermanage.mapper.UserInfoMapper;
import com.atguigu.gmall1129.service.UserService;
import com.atguigu.gmall1129.utils.RedisUtil;
import org.apache.catalina.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @param
 * @return
 */

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAddressMapper userAddressMapper;


    @Autowired
    RedisUtil redisUtil;

    public  void addUserInfo(UserInfo userInfo){
        userInfoMapper.insertSelective(userInfo);
    }

    public UserInfo getUserInfo(String id){
        //UserInfo userInfo = userInfoMapper.selectByPrimaryKey(id);

/*         UserInfo userInfoQuery=new UserInfo();
       userInfoQuery.setLoginName("zhangchen");

        UserInfo userInfo = userInfoMapper.selectOne(userInfoQuery);*/

        Example example=new Example(UserInfo.class);
        example.createCriteria().andLike("name","%晨%");

        UserInfo userInfo = userInfoMapper.selectOneByExample(example);
        return userInfo;

    }


    public List<UserInfo>  getUserList(UserInfo userInfo){
        List<UserInfo> userInfos = userInfoMapper.selectAll();
        return userInfos;
    }


    public void updateUser(UserInfo userInfo){
        Example example=new Example(UserInfo.class);
        example.createCriteria().andEqualTo("nickName","tingting");

        userInfoMapper.updateByExample(userInfo,example);

    }


    public List<UserAddress> getUserAddressList(String userId){
        UserAddress userAddressQuery=new UserAddress();

        userAddressQuery.setUserId(userId);

        List<UserAddress> userAddressList = userAddressMapper.select(userAddressQuery);

        return userAddressList;
    }


    public UserInfo login(UserInfo userInfo){
        //1 、密码转义
             ;
        String md5Hex = DigestUtils.md5Hex(userInfo.getPasswd());
        userInfo.setPasswd(md5Hex);

        //2 、 用户名+密码去查数据库
        UserInfo userInfoLogin = userInfoMapper.selectOne(userInfo);
        if(userInfoLogin==null){
            return null;
        }

        //3 、 保存到缓存中
        Jedis jedis = redisUtil.getJedis();
        String userInfoKey="user:"+userInfoLogin.getId()+":info";
        String userInfoJson= JSON.toJSONString(userInfoLogin);

        jedis.setex(userInfoKey,3600,userInfoJson);
        jedis.close();
//       返回结果
        return userInfoLogin;
    }

    public boolean verify(String userId){
        Jedis jedis = redisUtil.getJedis();
        String userInfoKey="user:"+userId+":info";
        Boolean exists = jedis.exists(userInfoKey);
        if(exists) {
            jedis.expire(userInfoKey, 1800);
        }
        jedis.close();
        return exists;
    }



}
