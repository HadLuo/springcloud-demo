package com.uc.firegroup.api.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "t_friend_log")
public class FriendLog implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6331889900882724999L;

    /**
     * 主键ID
     */
    @Id
    private Integer friendLogId;

    /**
     * 日期 只保存年月日
     */
    private Date friendDate;

    /**
     * 主加水军当天已加好友次数 默认为0
     */
    private Integer fromAddNum;

    /**
     * 主加水军当天被加好友次数 默认为0
     */
    private Integer fromAddedNum;

    /**
     * 主加水军微信ID
     */
    private String fromWxId;

    /**
     * 被加水军微信ID
     */
    private String toWxId;

    /**
     * 被加水军当天已加好友次数 默认为0
     */
    private Integer toAddNum;

    /**
     * 被加水军当天被加好友次数 默认为0
     */
    private Integer toAddedNum;

    /**
     * 主加水军已连续加好友或者被加好友天数 默认为1
     */
    private Integer fromDays;

    /**
     * 被加水军已连续加好友或者被加好友天数 默认为1
     */
    private Integer toDays;

    /**
     * 1.已发送加好友申请 2.好友申请已接受 3.记录已删除 默认为1
     */
    private Integer state;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 好友申请接受时间
     */
    private Date modifyTime;

    public FriendLog(Integer friendLogId, Date friendDate, Integer fromAddNum, Integer fromAddedNum, String fromWxId, String toWxId, Integer toAddNum, Integer toAddedNum, Integer fromDays, Integer toDays, Integer state, Date createTime, Date modifyTime) {
        this.friendLogId = friendLogId;
        this.friendDate = friendDate;
        this.fromAddNum = fromAddNum;
        this.fromAddedNum = fromAddedNum;
        this.fromWxId = fromWxId;
        this.toWxId = toWxId;
        this.toAddNum = toAddNum;
        this.toAddedNum = toAddedNum;
        this.fromDays = fromDays;
        this.toDays = toDays;
        this.state = state;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
    }

    public FriendLog() {
        super();
    }

    public Integer getFriendLogId() {
        return friendLogId;
    }

    public void setFriendLogId(Integer friendLogId) {
        this.friendLogId = friendLogId;
    }

    public Date getFriendDate() {
        return friendDate;
    }

    public void setFriendDate(Date friendDate) {
        this.friendDate = friendDate;
    }

    public Integer getFromAddNum() {
        return fromAddNum;
    }

    public void setFromAddNum(Integer fromAddNum) {
        this.fromAddNum = fromAddNum;
    }

    public Integer getFromAddedNum() {
        return fromAddedNum;
    }

    public void setFromAddedNum(Integer fromAddedNum) {
        this.fromAddedNum = fromAddedNum;
    }

    public String getFromWxId() {
        return fromWxId;
    }

    public void setFromWxId(String fromWxId) {
        this.fromWxId = fromWxId == null ? null : fromWxId.trim();
    }

    public String getToWxId() {
        return toWxId;
    }

    public void setToWxId(String toWxId) {
        this.toWxId = toWxId == null ? null : toWxId.trim();
    }

    public Integer getToAddNum() {
        return toAddNum;
    }

    public void setToAddNum(Integer toAddNum) {
        this.toAddNum = toAddNum;
    }

    public Integer getToAddedNum() {
        return toAddedNum;
    }

    public void setToAddedNum(Integer toAddedNum) {
        this.toAddedNum = toAddedNum;
    }

    public Integer getFromDays() {
        return fromDays;
    }

    public void setFromDays(Integer fromDays) {
        this.fromDays = fromDays;
    }

    public Integer getToDays() {
        return toDays;
    }

    public void setToDays(Integer toDays) {
        this.toDays = toDays;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}