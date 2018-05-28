package com.atguigu.gmall.list.manager.Controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ManagerService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ManagerController {
    @Reference
    ManagerService managerService;

    @GetMapping("index")
    public String index(){
        return "index";
    }


    @GetMapping("catalog1List")
    @ResponseBody
    public String getCatalog1List(){
        List<BaseCatalog1> cataLog1List = managerService.getCataLog1List();
        String catalog1Json = JSON.toJSONString(cataLog1List);
        return catalog1Json;
    }


    @GetMapping("catalog2List")
    @ResponseBody
    public String getCatalog2List(HttpServletRequest request){
        String catalog1Id = request.getParameter("catalog1Id");
        List<BaseCatalog2> cataLog2List = managerService.getCataLog2List(catalog1Id);
        String catalog2Json = JSON.toJSONString(cataLog2List);
        return catalog2Json;
    }


    @GetMapping("catalog3List")
    @ResponseBody
    public String getCatalog3List(HttpServletRequest request){
        String catalog2Id = request.getParameter("catalog2Id");
        List<BaseCatalog3> cataLog3List = managerService.getCataLog3List(catalog2Id);
        String catalog3Json = JSON.toJSONString(cataLog3List);
        return catalog3Json;
    }


    @GetMapping("baseSaleAttrList")
    @ResponseBody
    public String getBaseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = managerService.getBaseSaleAttrList();
        String baseSaleAttrJson = JSON.toJSONString(baseSaleAttrList);

        return baseSaleAttrJson;
    }
}
