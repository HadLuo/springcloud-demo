package com.uc.framework.natives;

import java.lang.reflect.Method;

/***
 * 
 * title: 方法级别 包装器
 *
 * @author HadLuo
 * @date 2020-9-17 13:55:33
 */
public class MethodLevelWrapper {

    private Method method;
    private Class<?> clazz;
    private Class<?>[] paremeters;

    public static MethodLevelWrapper getWrapper(Method method, Class<?> clazz) {
        MethodLevelWrapper w = new MethodLevelWrapper();
        w.method = method;
        w.clazz = clazz;
        w.paremeters = method.getParameterTypes();
        return w;
    }

    public static MethodLevelWrapper getWrapper(String methodName, Class<?> clazz,
            Class<?>... parameterTypes) {
        MethodLevelWrapper w = new MethodLevelWrapper();
        try {
            w.method = clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException();
        }
        w.clazz = clazz;
        w.paremeters = parameterTypes;
        return w;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Class<?>[] getParemeters() {
        return paremeters;
    }
}
