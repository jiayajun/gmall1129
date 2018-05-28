package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    public List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(Long spuId);
}
