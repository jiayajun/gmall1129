package com.atguigu.gmall.passport.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;

import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Value("${passport.key}")
    String passportKey;

    //注入用户逻辑处理service
    @Reference
    UserService userService;


    //进入登录页面
    @GetMapping("index")
    public String index(HttpServletRequest request) {
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);

        return "index";
    }

    //登录操作
    @PostMapping("login")
    @ResponseBody//返回结果使用ResbonseBody注解
    public String login(UserInfo userInfo, HttpServletRequest request) {
        //调用登录方法(返回是否登录)
         UserInfo isLogin = userService.login(userInfo);

        if (isLogin==null) {

            return "fail";
        }
        //签发令牌
        //构建map
        Map userMap = new HashMap();
        userMap.put("userId",isLogin.getId());
        userMap.put("nickName",isLogin.getNickName());

        System.out.println(userMap);

        //获取头文件
        String remoteAddr = request.getHeader("x-forwarded-for");
        request.getRemoteAddr();

        String token = JwtUtil.encode(passportKey, userMap, remoteAddr);

        System.out.println(token);
        return token;
    }

    //认证
    @GetMapping("/verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String currentIp =  request.getParameter("currentIp");
        //检查token
        Map userMap = JwtUtil.decode(token, passportKey,currentIp);
        //非空验证
        if (userMap==null){
            return "fail";

        }
        //获取用户id
        String userId = (String) userMap.get("userId");
        //查询后台登录信息
     boolean bo =  userService.verify(userId);
     if (bo) {
         //查询成功返回成功标志
         return "success";
     }else {
         //失败返回首页
         return "fail";
     }
    }


}
