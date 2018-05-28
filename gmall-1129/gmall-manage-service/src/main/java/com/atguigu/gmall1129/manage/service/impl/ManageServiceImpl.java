package com.atguigu.gmall1129.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall1129.bean.*;
import com.atguigu.gmall1129.manage.constant.RedisConst;
import com.atguigu.gmall1129.manage.mapper.*;
import com.atguigu.gmall1129.service.ListService;
import com.atguigu.gmall1129.service.ManageService;
import com.atguigu.gmall1129.utils.RedisUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @param
 * @return
 */


@Service
public class ManageServiceImpl  implements ManageService{
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

   @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
   BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
   SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
   SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;


    @Reference
    ListService listService;




    Map ooMaper=new HashMap<>();



    public List<BaseCatalog1> getCataLog1List(){
        List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();


        for (int i = 0; i <50000 ; i++) {
            ooMaper.put(new Random().nextInt(10000000),baseCatalog1List);
        }


        return  baseCatalog1List;

    }

    public List<BaseCatalog2> getCataLog2List(String catalog1Id){
        BaseCatalog2 baseCatalog2Query=new BaseCatalog2();
        baseCatalog2Query.setCatalog1Id(catalog1Id);

        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2Query);
        return  baseCatalog2List;

    }

    public List<BaseCatalog3> getCataLog3List(String catalog2Id){
        BaseCatalog3 baseCatalog3Query=new BaseCatalog3();
        baseCatalog3Query.setCatalog2Id(catalog2Id);

        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3Query);
        return  baseCatalog3List;

    }


    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id){
        BaseAttrInfo baseAttrInfo=new BaseAttrInfo();

        baseAttrInfo.setCatalog3Id(catalog3Id);

        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoList(  Long.parseLong(catalog3Id));
    //1循环遍历  次数 操作消耗




        return  baseAttrInfoList;
    }


    public void saveAttrInfo(BaseAttrInfo baseAttrInfo){
        baseAttrInfoMapper.insertSelective(baseAttrInfo);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }

    }


    public List<SpuInfo> getSpuList(String catalog3Id){
        SpuInfo spuInfoQuery=new SpuInfo();
        spuInfoQuery.setCatalog3Id(catalog3Id);

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfoQuery);

        return spuInfoList;

    }


    public List<BaseSaleAttr> getBaseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectAll();
        return baseSaleAttrList;
    }

    public void saveSpuInfo(SpuInfo spuInfo){
        spuInfoMapper.insertSelective(spuInfo);

        //保存前先清空原来
        SpuImage spuImageDel=new SpuImage();
        spuImageDel.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImageDel);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }

        SpuSaleAttr spuSaleAttrDel=new SpuSaleAttr();
        spuSaleAttrDel.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttrDel);

        SpuSaleAttrValue spuSaleAttrValueDel=new SpuSaleAttrValue();
        spuSaleAttrValueDel.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValueDel);

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);


            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                 spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }

        }
    }


    public List<SpuSaleAttr> getSaleAttrList(String spuId){
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSaleAttrInfoList(Long.parseLong(spuId));
        return spuSaleAttrList;
    }

    public List<SpuSaleAttr> getSaleAttrListBySku(String spuId,String skuId){
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSaleAttrInfoListBySku(Long.parseLong(spuId),Long.parseLong(skuId));
        return spuSaleAttrList;
    }



    public List<SpuImage> getSpuImageList(String spuId){
        SpuImage spuImageQuery = new SpuImage();
        spuImageQuery.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImageMapper.select(spuImageQuery);
        return spuImageList;
    }


    public  void saveSkuInfo(SkuInfo skuInfo){
        skuInfoMapper.insertSelective(skuInfo);

        SkuImage skuImageDel= new SkuImage();
        skuImageDel.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImageDel);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(skuImage);
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }


        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }


    }


    public  SkuInfo getSkuInfo(String skuId){
        try {
            Jedis jedis = redisUtil.getJedis();
            //先查询缓存
            String skuKey = RedisConst.SKU_PREFIX + skuId + RedisConst.SKU_SUFFIX;

            String skuInfoJson = jedis.get(skuKey);
            System.err.println( Thread.currentThread().getName()+ "开始查询");
            //缓存如果命中，直接返回结果
            if (skuInfoJson != null && skuInfoJson.length() > 0) {
                if("empty".equals(skuInfoJson)){
                    return null;
                }

                System.err.println( Thread.currentThread().getName()+ "已命中");
                SkuInfo skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                jedis.close();
                return skuInfo;

            } else {
                System.err.println( Thread.currentThread().getName()+ "未命中");
                //先检查是否能获得锁，同时尝试获得锁
                String skuLockKey = RedisConst.SKU_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                String ifLocked = jedis.set(skuLockKey, "locked", "NX", "EX", 10);
                if(ifLocked==null){
                    System.err.println( Thread.currentThread().getName()+ "未未获得锁，开始自旋");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //线程自旋
                    return  getSkuInfo(skuId);
                }else{
                    System.err.println( Thread.currentThread().getName()+ "已获得锁，开始查询数据库");
                    //未命中查询数据库
                    SkuInfo skuInfoDB = getSkuInfoDB(skuId);
                    if(skuInfoDB==null){
                        jedis.setex(skuKey, RedisConst.SKU_TIMEOUT, "empty");
                    }else {
                        //保存一份到缓存
                        String skuInfoJsonNew = JSON.toJSONString(skuInfoDB);
                        jedis.setex(skuKey, RedisConst.SKU_TIMEOUT, skuInfoJsonNew);
                    }
                    jedis.close();
                    return skuInfoDB;
                }

            }
        }catch (JedisConnectionException e){
            e.printStackTrace();
        }
        return getSkuInfoDB(  skuId);

    }



    public SkuInfo getSkuInfoDB(String skuId){


        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);


        SkuImage skuImageQuery= new SkuImage();
        skuImageQuery.setSkuId(skuInfo.getId());
        List<SkuImage> skuImageList = skuImageMapper.select(skuImageQuery);

        skuInfo.setSkuImageList(skuImageList);


        SkuAttrValue skuAttrValue=new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);

        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;
    }


    public List<SkuSaleAttrValue>   getSkuSaleAttrValueListBySpu(String spuId){
        List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(Long.parseLong(spuId));

        return  skuSaleAttrValues;
    }

    public void onSale(String skuId){

        SkuInfo skuInfo = getSkuInfo(skuId);

        SkuInfoEs skuInfoEs = new SkuInfoEs();

        try {
            BeanUtils.copyProperties(skuInfoEs,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        List<SkuAttrValueEs> skuAttrValueEsList=new ArrayList<>();

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            SkuAttrValueEs skuAttrValueEs = new SkuAttrValueEs();
            skuAttrValueEs.setValueId(skuAttrValue.getValueId());
            skuAttrValueEsList.add(skuAttrValueEs);
        }

        skuInfoEs.setSkuAttrValueListEs(skuAttrValueEsList);

        listService.saveSkuInfoEs(skuInfoEs);//异步 //跨模块写操作 //解耦合

    }

    public List<BaseAttrInfo> getAttrInfoList(List valueIdsList){
        String valueIds = StringUtils.join(valueIdsList, ',');

        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoListByValueIds(valueIds);
        return  baseAttrInfoList;
    }



}
