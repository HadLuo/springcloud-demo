package com.uc.firegroup.api.request;

import java.io.Serializable;

public class FriendLogRequest  implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 7250671790926412744L;

    /**
     * 日期 只保存年月日
     */
    private String friendDate;

    /**
     * 微信ID
     */
    private String wxId;


    public String getFriendDate() {
        return friendDate;
    }

    public void setFriendDate(String friendDate) {
        this.friendDate = friendDate;
    }

    public String getWxId() {
        return wxId;
    }

    public void setWxId(String wxId) {
        this.wxId = wxId;
    }
}
