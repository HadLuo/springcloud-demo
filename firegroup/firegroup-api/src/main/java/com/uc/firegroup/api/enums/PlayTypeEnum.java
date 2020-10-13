package com.uc.firegroup.api.enums;

public enum PlayTypeEnum {
    TIMING(1,"定时推送"),
    TRIGGER(2,"话术触发");

    /** 键值 */
    private Integer key;
    /** 值 */
    private String value;

    PlayTypeEnum(Integer key, String value) {
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
