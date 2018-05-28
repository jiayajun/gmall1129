package com.atguigu.gmall1129.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.BaseAttrInfo;
import com.atguigu.gmall1129.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @param
 * @return
 */

@Controller
public class AttrInfoController {

    @Reference
    ManageService manageService;


    @GetMapping("attrListPage")
    public String attrListPage(){
        return "attrListPage";
    }

    @GetMapping("attrList")
    @ResponseBody
    public String getAttrList(HttpServletRequest httpServletRequest){
        String catalog3Id = httpServletRequest.getParameter("catalog3Id");
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(catalog3Id);
        String baseAttrInfoJson = JSON.toJSONString(attrInfoList);
        return baseAttrInfoJson;
    }


    @GetMapping("attrListForSku")
    @ResponseBody
    public List<BaseAttrInfo> getAttrListForSku(HttpServletRequest httpServletRequest){
        String catalog3Id = httpServletRequest.getParameter("catalog3Id");
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(catalog3Id);
        return  attrInfoList;
    }

    @PostMapping("saveAttrInfo")
    @ResponseBody
     public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
           manageService.saveAttrInfo(baseAttrInfo);
           return "success";
    }
}
