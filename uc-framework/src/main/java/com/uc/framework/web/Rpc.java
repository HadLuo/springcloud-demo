package com.uc.framework.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.uc.framework.App;
import com.uc.framework.Ids;
import com.uc.framework.logger.Logs;
import com.uc.framework.logger.alert.AlertContext;

/**
 * 远程调用工具
 * 
 * @author HadLuo
 * @date 2020-9-8 16:53:48
 */
public final class Rpc {
    private Rpc() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get 远程调用 有返回值
     * 
     * @param <T>
     * @param url http://SERVICE-NAME/auth-api/v1/inn/getInnRandomId?channel={channel}&id={id}
     * @param responseType 返回值 的 类型
     * @return
     * @author HadLuo 2020-9-8 17:41:40
     */
    public static <T> T get(String url, Class<T> responseType) {
        RestTemplate restTemplate = App.getBean(RestTemplate.class);
        if (restTemplate == null) {
            Logs.e(Rpc.class, "not found RestTemplate Bean");
        }
        String traceId = Ids.getId();
        ResponseEntity<T> responseEntity = null;
        try {
            Logs.e(Rpc.class, "[RPC start " + traceId + " ]>>url=" + url + ",method=GET");
            responseEntity = restTemplate.getForEntity(url, responseType);
            Logs.e(Rpc.class, "[RPC end " + traceId + " ]>>url=" + url + ",method=GET,res="
                    + JSON.toJSONString(responseEntity));
            if (responseEntity == null) {
                return null;
            }
        } catch (Throwable e) {
            exception(traceId, url, resolveCode(responseEntity), "GET", null, e);
        }
        if (responseEntity != null && responseEntity.getStatusCode() != HttpStatus.OK) {
            exception(traceId, url, responseEntity.getStatusCodeValue(), "GET", null);
        }
        return responseEntity.getBody();
    }

    private static <T> int resolveCode(ResponseEntity<T> responseEntity) {
        if (responseEntity != null) {
            return responseEntity.getStatusCodeValue();
        }
        return -1;
    }

    /**
     * Get 远程调用 无返回值
     * 
     * @param <T>
     * @param url http://SERVICE-NAME/auth-api/v1/inn/getInnRandomId?channel={channel}&id={id}
     * @param responseType 返回值 的 类型
     * @return
     * @author HadLuo 2020-9-8 17:41:40
     */
    public static void get(String url) {
        RestTemplate restTemplate = App.getBean(RestTemplate.class);
        if (restTemplate == null) {
            Logs.e(Rpc.class, "not found RestTemplate Bean");
        }
        String traceId = Ids.getId();
        ResponseEntity<?> responseEntity = null;
        try {
            Logs.e(Rpc.class, "[RPC start " + traceId + " ]>>url=" + url + ",method=GET");
            responseEntity = restTemplate.getForEntity(url, null);
            Logs.e(Rpc.class, "[RPC end " + traceId + " ]>>url=" + url + ",method=GET,res="
                    + JSON.toJSONString(responseEntity));
            if (responseEntity == null) {
                return;
            }
        } catch (Throwable e) {
            exception(traceId, url, resolveCode(responseEntity), "GET", null, e);
        }
        if (responseEntity != null && responseEntity.getStatusCode() != HttpStatus.OK) {
            exception(traceId, url, responseEntity.getStatusCodeValue(), "GET", null);
        }
    }

    /**
     * Post 远程调用 有返回值
     * 
     * @param <T>
     * @param url http://SERVICE-NAME/auth-api/v1/inn/getInnRandomId?channel={channel}&id={id}
     * @param request 请求参数
     * @param responseType 返回值 类型
     * @return
     * @author HadLuo 2020-9-8 17:53:49
     */
    public static <T> T post(String url, Object request, Class<T> responseType) {
        RestTemplate restTemplate = App.getBean(RestTemplate.class);
        if (restTemplate == null) {
            Logs.e(Rpc.class, "not found RestTemplate Bean");
        }
        String traceId = Ids.getId();
        ResponseEntity<T> responseEntity = null;
        try {
            Logs.e(Rpc.class, "[RPC start " + traceId + " ]>>url=" + url + ",method=POST,req="
                    + JSON.toJSONString(request));
            responseEntity = restTemplate.postForEntity(url, request, responseType);
            Logs.e(Rpc.class, "[RPC end " + traceId + " ]>>url=" + url + ",method=POST,req="
                    + JSON.toJSONString(request) + ",res=" + JSON.toJSONString(responseEntity));
            if (responseEntity == null) {
                return null;
            }
        } catch (Throwable e) {
            exception(traceId, url, resolveCode(responseEntity), "POST", request, e);
        }
        if (responseEntity != null && responseEntity.getStatusCode() != HttpStatus.OK) {
            exception(traceId, url, responseEntity.getStatusCodeValue(), "POST", request);
        }
        return responseEntity.getBody();
    }

    /**
     * Post 远程调用 无返回值
     * 
     * @param <T>
     * @param url http://SERVICE-NAME/auth-api/v1/inn/getInnRandomId?channel={channel}&id={id}
     * @param request 请求参数
     * @param responseType 返回值 类型
     * @return
     * @author HadLuo 2020-9-8 17:53:49
     */
    public static <T> void post(String url, Object request) {
        RestTemplate restTemplate = App.getBean(RestTemplate.class);
        if (restTemplate == null) {
            Logs.e(Rpc.class, "not found RestTemplate Bean");
        }
        ResponseEntity<?> responseEntity = null;
        String traceId = Ids.getId();
        try {
            Logs.e(Rpc.class, "[RPC start " + traceId + " ]>>url=" + url + ",method=POST,req="
                    + JSON.toJSONString(request));
            responseEntity = restTemplate.postForEntity(url, request, null);
            Logs.e(Rpc.class, "[RPC end " + traceId + " ]>>url=" + url + ",method=POST,req="
                    + JSON.toJSONString(request) + ",res=" + JSON.toJSONString(responseEntity));
            if (responseEntity == null) {
                return;
            }
        } catch (Exception e) {
            exception(traceId, url, resolveCode(responseEntity), "POST", request, e);
        }
        if (responseEntity != null && responseEntity.getStatusCode() != HttpStatus.OK) {
            exception(traceId, url, responseEntity.getStatusCodeValue(), "POST", request);
        }
    }

    private static void exception(String traceId, String url, int code, String methodName, Object request) {
        RpcException rpcException = new RpcException(traceId, url, request, code, methodName);
        Logs.e(Rpc.class, rpcException.getMessage(), rpcException);
        throw rpcException;
    }

    private static void exception(String traceId, String url, int code, String methodName, Object request,
            Throwable e) {
        RpcException rpcException = new RpcException(traceId, url, request, code, methodName, e);
        Logs.e(Rpc.class, rpcException.getMessage(), e);
        AlertContext.robot().alert(rpcException.getMessage(), e);
        throw rpcException;
    }
}
