package com.uc.firegroup.api.enums;

/**
 * @author: Ryan.yuan
 * @time: 2020/9/17/017 11:24
 */
public enum SendStateEnum {

    WAIT_SEND(0,"待发送"),
    SUCCESS(1,"发送成功"),
    FAIL(2,"发送失败");

    /** 键值 */
    private Integer key;
    /** 值 */
    private String value;

    SendStateEnum(Integer key, String value) {
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
