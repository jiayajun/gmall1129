package com.atguigu.gmall.bean;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

public class BaseAttrValueEx extends BaseAttrValue implements Serializable {


    private String wholeName;
    private String cancelUrlParam;


    public String getWholeName() {
        return wholeName;
    }

    public void setWholeName(String wholeName) {
        this.wholeName = wholeName;
    }

    public String getCancelUrlParam() {
        return cancelUrlParam;
    }

    public void setCancelUrlParam(String cancelUrlParam) {
        this.cancelUrlParam = cancelUrlParam;
    }
}
