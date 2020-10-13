package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel("发消息实体")
@Data
public class SendWxMsgRequest implements Serializable {
    private String fromWxId;

    private String toWxId;

    private String msgContent;

    private String merchantId;

    public String getFromWxId() {
        return fromWxId;
    }

    public void setFromWxId(String fromWxId) {
        this.fromWxId = fromWxId;
    }

    public String getToWxId() {
        return toWxId;
    }

    public void setToWxId(String toWxId) {
        this.toWxId = toWxId;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }
}
