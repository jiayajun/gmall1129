package com.atguigu.gmall.list.manager.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ManagerService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class SpuController {

    @Reference
    ManagerService managerService;

    @GetMapping("spuListPage")

    public String spuListPage(){
        return "spuListPage";
    }

    @GetMapping("spuList")
    @ResponseBody
    public String spuList(HttpServletRequest request){
        String catalog3Id = request.getParameter("catalog3Id");
        List<SpuInfo> spuInfoList= managerService.getSpuList(catalog3Id);
        String spuJson = JSON.toJSONString(spuInfoList);
        return spuJson;
    }

    @PostMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(SpuInfo spuInfo){
        managerService.saveSpuInfo(spuInfo);

        return "success";
    }


    @GetMapping("saleAttrListForSku")
    @ResponseBody
    public List<SpuSaleAttr> getSaleAttrList(HttpServletRequest request){
        String spuId = request.getParameter("spuId");
        List<SpuSaleAttr> saleAttrList = managerService.getSaleAttrList(spuId);
        return saleAttrList;
    }


    @GetMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> getSpuImageList(HttpServletRequest request){
        String spuId = request.getParameter("spuId");
        List<SpuImage> spuImageList= managerService.getSpuImageList(spuId);
        return spuImageList;
    }
}
