package com.uc.firegroup.api.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Table(name = "t1") // 指定表名
public class MerchantApiBo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6067166698015400190L;

    @Id
    private Integer n1;

    private String n2;

    private Date n3;

    public Integer getN1() {
        return n1;
    }

    public void setN1(Integer n1) {
        this.n1 = n1;
    }

    public String getN2() {
        return n2;
    }

    public void setN2(String n2) {
        this.n2 = n2;
    }

    public Date getN3() {
        return n3;
    }

    public void setN3(Date n3) {
        this.n3 = n3;
    }

}
