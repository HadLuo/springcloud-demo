package com.uc.framework.login;

import java.util.Base64;
import javax.servlet.http.Cookie;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.uc.framework.logger.Logs;
import com.uc.framework.redis.RedisHandler;

public class DebugHeaderParser extends AbstractHeaderParser {
    private static final String Login_Cookie_Name = "token";

    public DebugHeaderParser(String value) {
        super(value);
        // TODO Auto-generated constructor stub
    }

    @Override
    public User parse() {
        // 开发环境
        Cookie cookie = new Cookie(Login_Cookie_Name, getValue());
        // get tocken from redis
        String userInfoBase64 = (String) RedisHandler.get("token_user:" + cookie.getValue());
        if (StringUtils.isEmpty(userInfoBase64)) {
            Logs.e(getClass(), "[login exception]>> key=" + "token_user:" + cookie.getValue() + ",value="
                    + userInfoBase64);
            return null;
        }
        // parse base64 token => get user object
        try {
            byte[] asBytes = Base64.getDecoder().decode(userInfoBase64);
            String str = new String(asBytes, "utf-8");
            Logs.e(getClass(), "login>>" + str);
            return JSON.parseObject(str, User.class);
        } catch (Exception e) {
            Logs.e(getClass(),
                    "login validate error,token=" + cookie.getValue() + ",redis token=" + userInfoBase64, e);
        }
        return null;
    }

}
