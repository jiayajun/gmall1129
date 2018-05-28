package com.atguigu.gmall.usermanager.controller;

import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
   @Autowired
    UserService userService;

    /**
     * 添加一个员工
     * @param userinfo
     * @return
     */
    @PostMapping("/user")
    public String addUserinfo(UserInfo userinfo){

         userService.addUserInfo(userinfo);
        return "success";
    }

    /**
     * 查询所有员工
     * @return
     */
    @GetMapping("/users")
    public List<UserInfo> users(){

        return  userService.users();
    }

    /**
     * 查询单个userinfo
     * @param id
     * @return
     */
    @GetMapping("/getuser")
    public UserInfo getUserinfo(@RequestParam String id){

        return  userService.getUserInfo(id);


    }

    /**
     * 删除员工地方法
     */
    @RequestMapping("/deleteUser")
    public void delereUser(@RequestParam("id") String id){
        userService.deleteUser(id);
    }

    /**
     * 修改员工方法
     */
    @RequestMapping(value = "/update",method = RequestMethod.POST)
    public void update(UserInfo userinfo){
        userService.updateUser(userinfo);

}



}
