package com.uc.firegroup.api.pojo;

import java.util.Date;

public class PlayGroupRelation {
    private Integer playGroupRelationId;

    private Integer playId;

    private String wxGroupId;

    private Date createTime;

    private Integer isDelete;

    private Integer state;

    private String failMessage;

    public PlayGroupRelation(Integer playGroupRelationId, Integer playId, String wxGroupId, Date createTime, Integer isDelete, Integer state, String failMessage) {
        this.playGroupRelationId = playGroupRelationId;
        this.playId = playId;
        this.wxGroupId = wxGroupId;
        this.createTime = createTime;
        this.isDelete = isDelete;
        this.state = state;
        this.failMessage = failMessage;
    }

    public PlayGroupRelation() {
        super();
    }

    public Integer getPlayGroupRelationId() {
        return playGroupRelationId;
    }

    public void setPlayGroupRelationId(Integer playGroupRelationId) {
        this.playGroupRelationId = playGroupRelationId;
    }

    public Integer getPlayId() {
        return playId;
    }

    public void setPlayId(Integer playId) {
        this.playId = playId;
    }

    public String getWxGroupId() {
        return wxGroupId;
    }

    public void setWxGroupId(String wxGroupId) {
        this.wxGroupId = wxGroupId == null ? null : wxGroupId.trim();
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage == null ? null : failMessage.trim();
    }
}