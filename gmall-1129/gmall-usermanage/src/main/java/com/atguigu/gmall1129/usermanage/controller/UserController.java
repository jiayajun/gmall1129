package com.atguigu.gmall1129.usermanage.controller;


import com.atguigu.gmall1129.bean.UserInfo;
import com.atguigu.gmall1129.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @param
 * @return
 */

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/user")
    public String addUser(UserInfo userInfo){
        userService.addUserInfo(userInfo);
        return "success";
    }

    @GetMapping("/user")
    public UserInfo getUserInfo(@RequestParam("id") String id){
        UserInfo userInfo = userService.getUserInfo(id);
        return userInfo;
    }


    @RequestMapping("/users")
    public ResponseEntity<List<UserInfo>> getUserList(UserInfo userInfo){
        List<UserInfo> userInfoList = userService.getUserList(userInfo);
        return ResponseEntity.ok().body(userInfoList);
    }




    @RequestMapping(value = "/user" ,method = RequestMethod.PUT)
    public    ResponseEntity<Void> update(UserInfo userInfo){
        userService.updateUser(userInfo);
        return ResponseEntity.ok().build();
    }

/*    @RequestMapping(value = "/user" ,method = RequestMethod.DELETE)
    public    ResponseEntity<Void> delete(UserInfo userInfo){
        userManageService.delete(userInfo);
        return ResponseEntity.ok().build();
    }*/




}
