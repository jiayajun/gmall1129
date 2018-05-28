package com.atguigu.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manager.constant.RedisConst;
import com.atguigu.gmall.manager.mapper.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManagerService;
import com.atguigu.gmall.utils.RedisUtil;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.apache.catalina.startup.RealmRuleSet;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.keyvalue.AbstractKeyValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;


import javax.imageio.ImageTranscoder;
import javax.print.attribute.standard.MediaSize;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class ManagerServiceImpl implements ManagerService {
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


    Map ooMaper = new HashMap<>();


    public List<BaseCatalog1> getCataLog1List() {
        List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();


        for (int i = 0; i < 50000; i++) {
            ooMaper.put(new Random().nextInt(10000000), baseCatalog1List);
        }


        return baseCatalog1List;

    }

    public List<BaseCatalog2> getCataLog2List(String catalog1Id) {
        BaseCatalog2 baseCatalog2Query = new BaseCatalog2();
        baseCatalog2Query.setCatalog1Id(catalog1Id);

        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2Query);
        return baseCatalog2List;

    }

    public List<BaseCatalog3> getCataLog3List(String catalog2Id) {
        BaseCatalog3 baseCatalog3Query = new BaseCatalog3();
        baseCatalog3Query.setCatalog2Id(catalog2Id);

        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3Query);
        return baseCatalog3List;

    }


    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();

        baseAttrInfo.setCatalog3Id(catalog3Id);

        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoList(Long.parseLong(catalog3Id));
        //1循环遍历  次数 操作消耗


        return baseAttrInfoList;
    }


    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        baseAttrInfoMapper.insertSelective(baseAttrInfo);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }

    }


    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfoQuery = new SpuInfo();
        spuInfoQuery.setCatalog3Id(catalog3Id);

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfoQuery);

        return spuInfoList;

    }


    public List<BaseSaleAttr> getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectAll();
        return baseSaleAttrList;
    }

    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insertSelective(spuInfo);

        //保存前先清空原来
        SpuImage spuImageDel = new SpuImage();
        spuImageDel.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImageDel);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }

        SpuSaleAttr spuSaleAttrDel = new SpuSaleAttr();
        spuSaleAttrDel.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttrDel);

        SpuSaleAttrValue spuSaleAttrValueDel = new SpuSaleAttrValue();
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


    public List<SpuSaleAttr> getSaleAttrList(String spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSaleAttrInfoList(Long.parseLong(spuId));

        return spuSaleAttrList;
    }

    public List<SpuSaleAttr> getSaleAttrListBySku(String spuId, String skuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSaleAttrInfoListBySku(Long.parseLong(spuId), Long.parseLong(skuId));
        return spuSaleAttrList;
    }


    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImageQuery = new SpuImage();
        spuImageQuery.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImageMapper.select(spuImageQuery);
        return spuImageList;
    }


    public void saveSkuInfo(SkuInfo skuInfo) {
        skuInfoMapper.insertSelective(skuInfo);

        SkuImage skuImageDel = new SkuImage();
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


    public SkuInfo getSkuInfo(String skuId) {



        //调用方法创建连接
        Jedis jedis = redisUtil.getJedis();
        try {


            //1.查询缓存
            //①拼接一可skuKey（键）
            String skuIdKey = RedisConst.SKU_PREFIX + skuId + RedisConst.SKU_SUFFIX;
            //②获取sku值
            String skuInfoJson = jedis.get(skuIdKey);
            //2.直接命中返回查询结果
            if (skuInfoJson != null && skuInfoJson.length() > 0) {
                //如果返回为empty也返回null
                if ("empty".equals(skuInfoJson)) {

                    return null;

                }
                System.err.println(Thread.currentThread().getName() + "缓存未命中！");
                //降json串反序列化为对象
                SkuInfo skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                //将序列化后的对象返回
                return skuInfo;

            } else {
                System.err.println(Thread.currentThread().getName() + "命中");

                //(高并发情况下redis出现问题解决办法如下)
                //获取锁的key
                String lockKey = RedisConst.SKU_PREFIX + skuId + RedisConst.LOCK;
                //判断当前线程是否占有锁并且尝试去获取
                String ifLock = jedis.set(lockKey, "locked", "NX", "EX", 13);
                //判断当前线程是否拿到锁
                if (ifLock == null) {
                    System.err.println(Thread.currentThread().getName() + "未获得分布式锁，开始自旋！");
                    //认为设置休眠时间
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //设置挺悬(继续调用此方法)
                    return getSkuInfo(skuId);


                } else {
                    System.err.println(Thread.currentThread().getName() + "缓存已命中！！！！！！！！！！！！！！！！！！！");
                    //3没有直接命中，查询数据库，缓存备份，并返回结果
                    //未命中查询数据库
                    SkuInfo skuInfoDB = getSkuInfoDB(skuId);
                    //如果数据库中的查询结果为空则返回empty并保存到缓存中
                    if (skuInfoDB == null) {
                        jedis.setex(skuIdKey, RedisConst.SKU_OUTTIME, "empty");

                    } else {
                        //将对象序列化到缓存中
                        String skuJsonNew = JSON.toJSONString(skuInfoDB);
                        jedis.setex(skuIdKey, RedisConst.SKU_OUTTIME, skuJsonNew);
                    }

                    //返回之前关闭连接
                    jedis.close();
                    //返回查询到的值
                    return skuInfoDB;

                }

            }
        } catch (JedisConnectionException j) {
            //抛出异常
            j.printStackTrace();
        }
        //如果redis出现异常则直接查询数据库提高可用性（访问量较少是可用）
        return getSkuInfoDB(skuId);

    }

    //数据库查询方法
    public SkuInfo getSkuInfoDB(String skuId) {


        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);


        SkuImage skuImageQuery = new SkuImage();
        skuImageQuery.setSkuId(skuInfo.getId());
        List<SkuImage> skuImageList = skuImageMapper.select(skuImageQuery);

        skuInfo.setSkuImageList(skuImageList);

        SkuAttrValue skuAttrValue = new SkuAttrValue();

        skuAttrValue.setSkuId(skuId);
        //获取平台属性列表
        List<SkuAttrValue> skuAttrValueList =
                skuAttrValueMapper.select(skuAttrValue);

        skuInfo.setSkuAttrValueList(skuAttrValueList);


        return skuInfo;


    }

    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(Long.parseLong(spuId));

        return skuSaleAttrValues;
    }

    //
    public void onSale(String skuId) {

        SkuInfo skuInfo = getSkuInfo(skuId);

        SkuInfoEs skuInfoEs = new SkuInfoEs();
        //阿帕奇从后面拷贝到前面
        try {
            BeanUtils.copyProperties(skuInfoEs, skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


        //存放平台属性值的id
        List<SkuAttrValueEs> skuAttrValueEsList = new ArrayList<>();

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();


        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            SkuAttrValueEs skuAttrValueEs = new SkuAttrValueEs();

            skuAttrValueEs.setValueId(skuAttrValue.getValueId());

            skuAttrValueEsList.add(skuAttrValueEs);
        }
        skuInfoEs.setSkuAttrValueListEs(skuAttrValueEsList);

        listService.saveSkuInfoEs(skuInfoEs);

    }

    public List<BaseAttrInfo> getAttrInfoList(List valueIdsList){
        //利用工具类的方法实现参数之间用逗号分隔
        String valueIds = StringUtils.join(valueIdsList, ',');
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoListByValueIds(valueIds);

        return baseAttrInfoList;
    }






}
