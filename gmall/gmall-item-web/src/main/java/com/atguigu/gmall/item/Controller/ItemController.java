package com.atguigu.gmall.item.Controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManagerService;
//import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
    @Reference
    ManagerService managerService;

    @Reference
    ListService listService;

//去往用户界面
    @GetMapping("{skuId}.html")
    public  String  getItem(@PathVariable("skuId") String skuId, HttpServletRequest request){
        System.out.println(skuId);
        SkuInfo skuInfo = managerService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);

        List<SpuSaleAttr> saleAttrList = managerService.getSaleAttrListBySku(skuInfo.getSpuId(),skuId);

        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = managerService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        String valueIdString="";
        Map valueIds_skuId_Map= new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            if(valueIdString.length()>0){
                valueIdString+="|";
            }
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
            valueIdString+=skuSaleAttrValue.getSaleAttrValueId();

            if((i+1)<skuSaleAttrValueListBySpu.size() ) {
                SkuSaleAttrValue skuSaleAttrValueNext = skuSaleAttrValueListBySpu.get(i + 1);
                if (!skuSaleAttrValueNext.getSkuId().equals(skuSaleAttrValue.getSkuId())) {
                    valueIds_skuId_Map.put(valueIdString, skuSaleAttrValue.getSkuId());
                    valueIdString = "";
                }
            }else{
                valueIds_skuId_Map.put(valueIdString, skuSaleAttrValue.getSkuId());
                valueIdString = "";
            }


        }
        String valueIdsSkuIdJson = JSON.toJSONString(valueIds_skuId_Map);

        request.setAttribute("valueIdsSkuIdJson",valueIdsSkuIdJson);
        request.setAttribute("saleAttrList",saleAttrList);

        //添加热度评分排序
        listService.countHotScore(skuId);

        return "item";
    }
}

