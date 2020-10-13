package com.uc.framework.natives;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.StringUtils;

public class Beans {

    /**
     * 
     * title: 实例化 对象， 并给 字段付初始值
     *
     * @param beanClass
     * @param initFileds
     * @author HadLuo 2020-9-14 10:47:15
     */
    public static <T> T instance(Class<T> beanClass, Map<String, Object> initFileds) {
        if (beanClass == null) {
            return null;
        }

        T obj = instance(beanClass, initFileds);
        if (initFileds == null || initFileds.isEmpty()) {
            return obj;
        }
        for (String fieldName : initFileds.keySet()) {
            setField(obj, fieldName, initFileds.get(fieldName));
        }
        return obj;
    }

    /***
     * 
     * title: 给字段设置 值
     *
     * @param bean
     * @param fName
     * @param fVal
     * @author HadLuo 2020-9-14 10:55:07
     */
    public static void setField(Object bean, String fName, Object fVal) {
        if (bean == null || StringUtils.isEmpty(fName)) {
            return;
        }
        try {
            Field f = bean.getClass().getDeclaredField(fName);
            f.setAccessible(true);
            try {
                f.set(bean, fVal);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("[反射异常]>>claszz=" + bean.getClass().getName() + ",fName=" + fName,
                        e);
            }
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException("[反射异常]>>claszz=" + bean.getClass().getName() + ",fName=" + fName, e);
        }

    }

    /**
     * 
     * title: 实例化
     *
     * @param <T>
     * @param beanClass
     * @return
     * @author HadLuo 2020-9-14 10:55:18
     */
    public static <T> T instance(Class<T> beanClass) {
        if (beanClass == null) {
            return null;
        }
        try {
            return beanClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("[反射异常]>>claszz=" + beanClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T, S> T toView(S s, Class<T> c) {
        if (s == null) {
            return null;
        }
        T t = (T) ReflectUtils.newInstance(c);
        BeanUtils.copyProperties(s, t);
        return t;
    }

    public static <T, S> List<T> toView(List<S> sList, Class<T> c) {
        if (sList == null) {
            return null;
        }
        List<T> tList = new ArrayList<>();
        for (int i = 0; i < sList.size(); i++) {
            tList.add(toView(sList.get(i), c));
        }
        return tList;
    }

}
