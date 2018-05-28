package com.atguigu.gmall.bean;

import org.junit.jupiter.api.Test;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

public class BaseAttrInfo implements Serializable {



    @Id
    @Column
    //主键生成策略
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;




    /**
     * 与数据库无关的用此注解
     */
    @Transient

    List<BaseAttrValue> attrValueList;


    public List<BaseAttrValue> getAttrValueList() {
        return attrValueList;
    }

    public void setAttrValueList(List<BaseAttrValue> attrValueList) {
        this.attrValueList = attrValueList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }


}
