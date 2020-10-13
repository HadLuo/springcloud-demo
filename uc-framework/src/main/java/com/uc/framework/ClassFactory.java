package com.uc.framework;

import java.util.concurrent.ConcurrentHashMap;

import com.uc.framework.natives.ReflectUtils;

/***
 * 带缓存的 类构造工厂
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年3月27日 新建
 */
public final class ClassFactory<T> {
    // volatile 防止指令重排 ,导致构造可能没有执行
    private volatile static ConcurrentHashMap<String, Object> mapper = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz) {
        String nameKey = clazz.getName();
        Object object = (T) mapper.get(nameKey);
        if (object == null) {
            synchronized (ClassFactory.class) {
                if (object == null) {
                    object = ReflectUtils.newInstanceWithEx(clazz);
                    mapper.putIfAbsent(nameKey, object);
                }
            }
        }
        return (T) object;
    }
}
