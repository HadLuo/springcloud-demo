package com.uc.firegroup.api.pojo;

import java.util.Date;

public class CapitalRelation {
    private Integer capitalRelationId;

    private String orderNo;

    private String wxGroupId;

    private Integer robotNum;

    private Date createTime;

    private Integer isDelete;

    public CapitalRelation(Integer capitalRelationId, String orderNo, String wxGroupId, Integer robotNum, Date createTime, Integer isDelete) {
        this.capitalRelationId = capitalRelationId;
        this.orderNo = orderNo;
        this.wxGroupId = wxGroupId;
        this.robotNum = robotNum;
        this.createTime = createTime;
        this.isDelete = isDelete;
    }

    public CapitalRelation() {
        super();
    }

    public Integer getCapitalRelationId() {
        return capitalRelationId;
    }

    public void setCapitalRelationId(Integer capitalRelationId) {
        this.capitalRelationId = capitalRelationId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo == null ? null : orderNo.trim();
    }

    public String getWxGroupId() {
        return wxGroupId;
    }

    public void setWxGroupId(String wxGroupId) {
        this.wxGroupId = wxGroupId == null ? null : wxGroupId.trim();
    }

    public Integer getRobotNum() {
        return robotNum;
    }

    public void setRobotNum(Integer robotNum) {
        this.robotNum = robotNum;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}