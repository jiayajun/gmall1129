package com.atguigu.gmall.service;



import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {


    public  void addUserInfo(UserInfo userInfo);

    public UserInfo getUserInfo(String id);

    public List<UserInfo> getUserList(UserInfo userInfo);

    public void updateUser(UserInfo userInfo);
    public void deleteUser(String id);


    public List<UserAddress> getUserAddressList(String userId);
    public List<UserInfo> users();

    public UserInfo login(UserInfo userInfo);//登录方法

    public boolean verify(String userId);//登录验证
}

