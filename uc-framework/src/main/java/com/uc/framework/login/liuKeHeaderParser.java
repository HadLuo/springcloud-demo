package com.uc.framework.login;

import com.alibaba.fastjson.JSON;
import com.uc.framework.login.User.UserType;

/**
 * 
 * title: 留客请求头解析器
 *
 * @author HadLuo
 * @date 2020-10-9 9:29:19
 */
public class liuKeHeaderParser extends AbstractHeaderParser {

    public liuKeHeaderParser(String value) {
        super(value);
    }

    @Override
    public User parse() {
        User user = JSON.parseObject(getValue(), User.class);
        user.registerType(UserType.liuKe);
        return user;
    }

}
