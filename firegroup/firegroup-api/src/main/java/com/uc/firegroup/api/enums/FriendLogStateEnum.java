package com.uc.firegroup.api.enums;

public enum FriendLogStateEnum {
     SEND_MSG(1,"已发送加好友申请"),
     ACCEPTED(2,"好友申请已接受"),
     DELETE(3,"记录已删除");

    /** 键值 */
    private Integer key;
    /** 值 */
    private String value;

    FriendLogStateEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
