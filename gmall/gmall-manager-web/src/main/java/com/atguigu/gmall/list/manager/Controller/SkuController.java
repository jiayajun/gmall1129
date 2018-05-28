package com.atguigu.gmall.list.manager.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.ManagerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class SkuController {
    @Reference
    ManagerService managerService;


    //保存大数据
    @RequestMapping(value = "saveSku", method = RequestMethod.POST)
    @ResponseBody
    public String saveSkuInfo(SkuInfo skuInfo) {
        managerService.saveSkuInfo(skuInfo);
        return "success";
    }

    //ES传输数据
    @PostMapping("onSale")
    @ResponseBody
    public String onSeal(@RequestParam("skuId") String skuId){
        managerService.onSale(skuId);

        return "success";
    }
}
