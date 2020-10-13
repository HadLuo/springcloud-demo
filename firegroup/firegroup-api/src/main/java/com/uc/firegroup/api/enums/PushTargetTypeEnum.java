package com.uc.firegroup.api.enums;

public enum PushTargetTypeEnum {
    TASK(1,"任务推送"),
    GROUP(2,"群推送");

    /** 键值 */
    private Integer key;
    /** 值 */
    private String value;

    PushTargetTypeEnum(Integer key, String value) {
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
