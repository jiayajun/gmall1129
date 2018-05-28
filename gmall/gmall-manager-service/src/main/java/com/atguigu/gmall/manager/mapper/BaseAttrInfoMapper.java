package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
//获取说有的平台属性
 List<BaseAttrInfo> selectAttrInfoList(Long catalog3Id);

 List<BaseAttrInfo> selectAttrInfoListByValueIds(@Param("valueIds") String valueIds);
}
