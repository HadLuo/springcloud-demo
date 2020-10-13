package com.uc.firegroup.api.pojo;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "t_play_message_push_detail")
public class PlayMessagePushDetail {
    @Id
    private Integer id;

    private Integer playMsgPushId;

    private String robotNickname;

    private String wxId;

    private String wxAcc;

    private String wxNickname;

    private String wxImgUrl;

    private Integer accSource;

    private Integer callAll;

    private Integer intervalTime;

    private Integer playErrorType;

    private String messageContent;

    private Integer messageSort;

    private Integer sendState;

    private Date createTime;

    private Date modifyTime;

    private Integer isDelete;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlayMsgPushId() {
        return playMsgPushId;
    }

    public void setPlayMsgPushId(Integer playMsgPushId) {
        this.playMsgPushId = playMsgPushId;
    }

    public String getRobotNickname() {
        return robotNickname;
    }

    public void setRobotNickname(String robotNickname) {
        this.robotNickname = robotNickname == null ? null : robotNickname.trim();
    }

    public String getWxAcc() {
        return wxAcc;
    }

    public void setWxAcc(String wxAcc) {
        this.wxAcc = wxAcc == null ? null : wxAcc.trim();
    }

    public String getWxNickname() {
        return wxNickname;
    }

    public void setWxNickname(String wxNickname) {
        this.wxNickname = wxNickname == null ? null : wxNickname.trim();
    }

    public String getWxImgUrl() {
        return wxImgUrl;
    }

    public void setWxImgUrl(String wxImgUrl) {
        this.wxImgUrl = wxImgUrl == null ? null : wxImgUrl.trim();
    }

    public Integer getAccSource() {
        return accSource;
    }

    public void setAccSource(Integer accSource) {
        this.accSource = accSource;
    }

    public Integer getCallAll() {
        return callAll;
    }

    public void setCallAll(Integer callAll) {
        this.callAll = callAll;
    }

    public Integer getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(Integer intervalTime) {
        this.intervalTime = intervalTime;
    }

    public Integer getPlayErrorType() {
        return playErrorType;
    }

    public void setPlayErrorType(Integer playErrorType) {
        this.playErrorType = playErrorType;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent == null ? null : messageContent.trim();
    }

    public Integer getMessageSort() {
        return messageSort;
    }

    public void setMessageSort(Integer messageSort) {
        this.messageSort = messageSort;
    }

    public Integer getSendState() {
        return sendState;
    }

    public void setSendState(Integer sendState) {
        this.sendState = sendState;
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

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public String getWxId() {
        return wxId;
    }

    public void setWxId(String wxId) {
        this.wxId = wxId;
    }
}