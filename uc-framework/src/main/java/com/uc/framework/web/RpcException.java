package com.uc.framework.web;

import com.alibaba.fastjson.JSON;

/**
 * 远程接口调用 异常
 * 
 * @author HadLuo
 * @date 2020-9-8 17:31:31
 */
public class RpcException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 7222293751725556400L;

    /** 异常状态码 */
    private int statusCode;

    public RpcException(String traceId, String url, Object request, int statusCode, String methodName) {
        super("【rpc " + methodName.toUpperCase() + " call exception " + traceId + "】>>url=" + url
                + (request == null ? "" : ",request=" + JSON.toJSONString(request)));
        this.statusCode = statusCode;
    }

    public RpcException(String traceId, String url, Object request, int statusCode, String methodName,
            Throwable e) {
        super("【rpc " + methodName.toUpperCase() + " call exception " + traceId + "】>>url=" + url
                + (request == null ? "" : ",request=" + JSON.toJSONString(request)), e);
        this.statusCode = statusCode;
    }

    public RpcException(String traceId, String url, int statusCode, String methodName) {
        this(traceId, url, null, statusCode, methodName);
    }

    public int getStatusCode() {
        return statusCode;
    }
}
