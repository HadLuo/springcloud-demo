package com.uc.framework.login;

/**
 * title: 登录拦截器
 * 
 * @author HadLuo
 * @date 2020-9-2 17:01:12
 */
public class UserThreadLocal {

    private static final ThreadLocal<User> threadlocal = new ThreadLocal<User>();

    public static void set(User user) {
        threadlocal.set(user);
    }

    public static User get() {
        return threadlocal.get();
    }

    public static void remove() {
        threadlocal.remove();
    }
}
