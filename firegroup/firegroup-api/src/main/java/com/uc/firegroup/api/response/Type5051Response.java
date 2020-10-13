package com.uc.firegroup.api.response;

import java.io.Serializable;
import lombok.Data;

@Data
public class Type5051Response implements Serializable {
    private static final long serialVersionUID = 1327100716263344572L;

    private String toWxId;
    //微信群id
    private String vcChatRoomId;
    /* 谁说的话 */
    private String fromWxId;
    private String msgId;
    private String vcChatRoomSerialNo;
    private MsgInfo msgInfo;

    @Data
    public static class MsgInfo implements Serializable {
        private static final long serialVersionUID = 2455464514872389306L;
        private String vcDesc;
        private int nVoiceTime;
        private String msgContent;
        private int nMsgNum;
        private String vcHref;
        private String vcTitle;
        private int nMsgType;
    }

}
