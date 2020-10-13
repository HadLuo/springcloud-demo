package com.uc.firegroup.api.enums;

/**
 * 推送状态枚举
 * @author: Ryan.yuan
 * @time: 2020/9/17/017 10:10
 */
public enum PushStateEnum {

    WAIT_SEND(1,"待发送"),
    ING(2,"进行中"),
    FINISH(3,"已结束"),
    USER_STOP(4,"人工暂停"),
    PUSH_FAIL(5,"推送失败"),
    SYSTEM_STOP(6,"系统暂停");

    /** 键值 */
    private Integer key;
    /** 值 */
    private String value;

    PushStateEnum(Integer key, String value) {
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
