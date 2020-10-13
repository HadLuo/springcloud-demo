package com.uc.framework.login;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractHeaderParser implements HeaderParser {
    static final String Flag_Bearer = "Bearer";
    static final String Flag_LiuKe = "LiuKe";
    private String value;

    public AbstractHeaderParser(String value) {
        super();
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static HeaderParser resolver(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders("Authorization");
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            if (value.startsWith(Flag_Bearer)) {
                String authHeaderValue = value.substring(Flag_Bearer.length()).trim();
                int commaIndex = authHeaderValue.indexOf(',');
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex);
                }
                return new BearerHeaderParser(authHeaderValue);
            } else if (value.startsWith(Flag_LiuKe)) {
                String authHeaderValue = value.substring(Flag_LiuKe.length()).trim();
                int commaIndex = authHeaderValue.indexOf(',');
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex);
                }
                return new liuKeHeaderParser(authHeaderValue);
            }
        }
        return null;
    }

}
