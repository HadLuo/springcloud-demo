package com.uc.framework.login;

import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uc.framework.logger.Logs;
import com.uc.framework.login.User.UserType;
import com.uc.framework.obj.Result;
import com.uc.framework.web.Rpc;

/**
 * 
 * title: 比邻 请求头解析器
 *
 * @author HadLuo
 * @date 2020-10-9 9:29:19
 */
public class BearerHeaderParser extends AbstractHeaderParser {
    /** 根据token获取比邻用户（商家、操作员）基础信息 */
    private static final String Url_UserInfoByToken = "http://neighbour-business-extend/fnCommonBusi/userInfoByToken";

    public BearerHeaderParser(String value) {
        super(value);
    }

    @Override
    public User parse() {
        if (StringUtils.isEmpty(getValue())) {
            return null;
        }
        Result<User> result = userInfoByToken(getValue());
        if (result.isSuccess() && result.getData() != null) {
            Logs.e(getClass(), "login>>" + JSON.toJSONString(result.getData()));
            User user = result.getData();
            user.registerType(UserType.Bearer);
            return user;
        }
        return null;
    }

    /**
     * 
     * title: 根据token获取比邻用户（商家、操作员）基础信息
     * 
     * @param dto
     * @return
     * @author HadLuo 2020-9-9 11:46:38
     */
    @SuppressWarnings("unchecked")
    public static Result<User> userInfoByToken(String token) {
        ResultBody resultBody = Rpc.get(Url_UserInfoByToken + "?token=" + token, ResultBody.class);
        if (resultBody.getCode() == 0) {
            JSONObject ret = (JSONObject) resultBody.getData();
            if (ret == null) {
                return Result.ok();
            }
            return Result.ok(ret.toJavaObject(User.class));
        } else {
            return (Result<User>) unZeroCode(resultBody, token, Url_UserInfoByToken);
        }
    }

    /***
     * title: 非 0 错误码处理
     * 
     * @param resultBody
     * @param dto
     * @param url
     * @author HadLuo 2020-9-9 11:38:34
     */
    private static Result<?> unZeroCode(ResultBody resultBody, Object dto, String url) {
        Logs.e(LoginInterceptor.class, "【业务异常】>>url=" + url + ",req=" + JSON.toJSONString(dto) + ",res="
                + JSON.toJSONString(resultBody));
        return Result.err(resultBody.getCode(), resultBody.getMessage());
    }
}
