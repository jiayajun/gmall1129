package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.SkuInfoEs;
import com.atguigu.gmall.bean.SkuInfoEsParam;
import com.atguigu.gmall.bean.SkuInfoEsResult;

public interface ListService {
    //保存SkuInfoEs
    public void saveSkuInfoEs(SkuInfoEs skuInfoEs);


    public SkuInfoEsResult searchSkuInfoList(SkuInfoEsParam skuInfoParam);

    public void countHotScore(String skuId);
}
