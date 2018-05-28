package com.atguigu.gmall.list.manager.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.service.ManagerService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrInfoController {

    //注入managerService
    @Reference
    ManagerService managerService;




    @GetMapping("attrListPage")
    public String attrListPage(){
        return "attrListPage";
    }

    @GetMapping("attrList")
    @ResponseBody
    public String getAttrList(HttpServletRequest httpServletRequest){
        String catalog3Id = httpServletRequest.getParameter("catalog3Id");
        List<BaseAttrInfo> attrInfoList = managerService.getAttrInfoList(catalog3Id);
        String baseAttrInfoJson = JSON.toJSONString(attrInfoList);
        return baseAttrInfoJson;
    }


    @GetMapping("attrListForSku")
    @ResponseBody
    public List<BaseAttrInfo> getAttrListForSku(HttpServletRequest httpServletRequest){
        String catalog3Id = httpServletRequest.getParameter("catalog3Id");
        List<BaseAttrInfo> attrInfoList = managerService.getAttrInfoList(catalog3Id);
        return  attrInfoList;
    }

    @PostMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo){
        managerService.saveAttrInfo(baseAttrInfo);
        return "success";
    }
}
