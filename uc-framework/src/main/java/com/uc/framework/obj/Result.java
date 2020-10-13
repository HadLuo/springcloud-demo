package com.uc.framework.obj;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 前后端通用返回
 * 
 * @author HadLuo
 *
 */
@ApiModel("服务器统一返回结构")
public class Result<T> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3840493468596409723L;
    @ApiModelProperty("0操作成功 非0操作失败")
    private int code;
    @ApiModelProperty("操作失败的信息")
    private String message;
    @ApiModelProperty("返回的数据体")
    private T data;

    private Result() {
    }

    public boolean successful() {
        return code == 0 ? true : false;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setData(data);
        return r;
    }

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        return r;
    }

    public static <T> Result<T> err() {
        Result<T> r = new Result<>();
        r.setCode(-1);
        return r;
    }

    public static <T> Result<T> err(String errMsg) {
        Result<T> r = new Result<>();
        r.setCode(-1);
        r.setMessage(errMsg);
        return r;
    }

    public static <T> Result<T> err(int errCode, String errMsg) {
        Result<T> r = new Result<>();
        r.setCode(errCode);
        r.setMessage(errMsg);
        return r;
    }

    public boolean isSuccess(){
        return this.getCode() == 0;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

}
