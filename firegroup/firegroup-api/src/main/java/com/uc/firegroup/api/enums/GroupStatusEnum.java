package com.uc.firegroup.api.enums;

public enum  GroupStatusEnum {
    ACTIVE(1,"正在使用"),
    STOP(2,"暂停服务待续费"),
    END(3,"停止服务"),
    INIT(4,"初始化状态");

    /** 键值 */
    private Integer key;
    /** 值 */
    private String value;

    GroupStatusEnum(Integer key, String value) {
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
