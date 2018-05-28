package com.atguigu.gmall1129.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall1129.bean.SkuInfo;
import com.atguigu.gmall1129.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @param
 * @return
 */

@Controller
public class SkuController {

    @Reference
    ManageService manageService;



    @PostMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "success";

    }

    @PostMapping("onSale")
    @ResponseBody
    public String onSale(@RequestParam("skuId") String skuId){
        manageService.onSale(skuId);
        return "success";
    }

}
