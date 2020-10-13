package com.uc.framework.login;

import java.io.Serializable;

/**
 * title : 比邻 接口 通用返回值
 * 
 * @author HadLuo
 * @date 2020-9-9 8:45:33
 */
public class ResultBody implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8764950957720900826L;

    private int code;

    private Object data;

    private Object extra;

    private String message;

    private String path;

    private long timestamp;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ResultBody [code=" + code + ", data=" + data + ", extra=" + extra + ", message=" + message
                + ", path=" + path + ", timestamp=" + timestamp + "]";
    }

}
