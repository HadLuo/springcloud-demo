package com.uc.firegroup.api.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class ReceivedPersonMsgVo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4358208520725218927L;

    /**
     * 接受人微信ID
     */
    public String toWxId;

    /**
     * 发送人微信i
     */
    public String fromWxId;

    public String msgId;

    public MsgInfo msgInfo;

    
    public static class MsgInfo{
        public String vcDesc;

        public Integer nVoiceTime;

        /**
         * 内容实体
         */
        public String msgContent;

        public String getVcDesc() {
            return vcDesc;
        }

        public void setVcDesc(String vcDesc) {
            this.vcDesc = vcDesc;
        }

        public Integer getnVoiceTime() {
            return nVoiceTime;
        }

        public void setnVoiceTime(Integer nVoiceTime) {
            this.nVoiceTime = nVoiceTime;
        }

        public String getMsgContent() {
            return msgContent;
        }

        public void setMsgContent(String msgContent) {
            this.msgContent = msgContent;
        }
    }

    public String getToWxId() {
        return toWxId;
    }

    public void setToWxId(String toWxId) {
        this.toWxId = toWxId;
    }

    public String getFromWxId() {
        return fromWxId;
    }

    public void setFromWxId(String fromWxId) {
        this.fromWxId = fromWxId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public MsgInfo getMsgInfo() {
        return msgInfo;
    }

    public void setMsgInfo(MsgInfo msgInfo) {
        this.msgInfo = msgInfo;
    }
}
