package com.atguigu.gmall.bean;

import org.apache.ibatis.javassist.bytecode.LineNumberAttribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 传入参数
 */
public class SkuInfoEsParam implements Serializable {

    String keyword;

    String catalog3Id;

    int pageNo=1;//页码

    int pageSize=20;//总页数

    List<String> valueIdList=new ArrayList<>();

    String[] valueIds;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<String> getValueIdList() {
        return valueIdList;
    }

    public void setValueIdList(List<String> valueIdList) {
        this.valueIdList = valueIdList;
    }

    public String[] getValueIds() {
        return valueIds;
    }

    public void setValueIds(String[] valueIds) {
        this.valueIds = valueIds;
    }

    public void changeArray2List(){
        for (int i = 0; i < valueIds.length; i++) {
            String valueId = valueIds[i];
            valueIdList.add(valueId);
        }


    }

}
