package com.uc.framework.natives;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.StringUtils;
import com.google.common.collect.Maps;
import com.uc.framework.logger.Logs;

public class Classes {

    private static final String Alias = "[反射异常]";

    /***
     * 把map对象转换为javabean
     * 
     * @param <T>
     * @param map
     * @param beantype
     * @return
     * @throws Exception
     * @author HadLuo 2020年4月29日 新建
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    public static <T> T mapToJavaBean(Map<String, Object> map, Class<T> beantype) throws Exception {
        if (map == null || beantype == null) {
            return null;
        }
        T object = beantype.newInstance();// 创建对象
        // 获取类的属性描述器
        BeanInfo beaninfo = Introspector.getBeanInfo(beantype, Object.class);
        // 获取类的属性集
        PropertyDescriptor[] pro = beaninfo.getPropertyDescriptors();
        for (PropertyDescriptor property : pro) {
            // 获取属性的名字
            String name = property.getName();
            Object value = map.get(name);// 得到属性name在map中对应的value。
            Method set = property.getWriteMethod();// 得到属性的set方法
            // 接下来将map的value转换为属性的value
            set.invoke(object, value);// 执行set方法
        }
        return object;
    }

    /***
     * 将javabean转换为map
     * 
     * @param bean
     * @return
     * @throws Exception
     * @author HadLuo 2020年4月29日 新建
     */
    public static Map<String, Object> javaBeanToMap(Object bean) throws Exception {
        if (bean == null) {
            return Maps.newHashMap();
        }
        Map<String, Object> map = new HashMap<>();
        // 获取类的属性描述器
        BeanInfo beaninfo = Introspector.getBeanInfo(bean.getClass(), Object.class);
        // 获取类的属性集
        PropertyDescriptor[] pro = beaninfo.getPropertyDescriptors();
        for (PropertyDescriptor property : pro) {
            String key = property.getName();// 得到属性的name
            Method get = property.getReadMethod();
            Object value = get.invoke(bean);// 执行get方法得到属性的值
            map.put(key, value);
        }
        return map;
    }

    /***
     * 加载class
     * 
     * @param className
     * @return
     * @author HadLuo 2020年5月22日 新建
     */
    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (Throwable e) {
            Logs.e(Classes.class, Alias + ">>className=" + className, e);
        }
        return null;
    }

    public static <T> T newInstance(Class<T> clazz) {
        if (null == clazz) {
            return null;
        }
        try {
            return clazz.newInstance();
        } catch (Throwable e) {
            Logs.e(Classes.class, Alias + ">>clazz=" + clazz, e);
        }
        return null;
    }

    /***
     * 获取字段 的 值
     * 
     * @param instance
     * @param fName
     * @return
     * @author HadLuo 2020年4月29日 新建
     */
    public static Object resolveFieldVal(Object instance, String fName) {
        if (null == instance || StringUtils.isEmpty(fName)) {
            return null;
        }
        Class<?> clazz = instance.getClass();
        try {
            Field field = clazz.getDeclaredField(fName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            Logs.e(Classes.class, Alias + ">>fName=" + fName + ",class=" + clazz.getSimpleName(), e);
        }
        return null;
    }

    /***
     * 设置字段的值
     * 
     * @param instance
     * @param fName
     * @return
     * @author HadLuo 2020年4月29日 新建
     */
    public static void injectFieldVal(Object instance, String fName, Object fValue) {
        if (null == instance || StringUtils.isEmpty(fName)) {
            return;
        }
        Class<?> clazz = instance.getClass();
        try {
            Field field = clazz.getDeclaredField(fName);
            if (field == null) {
                return;
            }
            field.setAccessible(true);
            field.set(instance, fValue);
        } catch (Throwable e) {
            Logs.e(Classes.class, Alias + ">>fName=" + fName + ",clazz=" + clazz.getName(), e);
        }
    }

    /**
     * 设置字段的值 (字段可能按在父类里面)
     * 
     * @param instance
     * @param fName
     * @param fValue
     * @author HadLuo 2020年5月22日 新建
     */
    public static void injectFieldValWithSuper(Object instance, String fName, Object fValue) {
        Class<?> tempClass = instance.getClass();
        while (tempClass != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
            try {
                Field field = tempClass.getDeclaredField(fName);
                if (field == null) {
                    continue;
                }
                field.setAccessible(true);
                field.set(instance, fValue);
                return;
            } catch (Exception e) {
            }
            tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
        }
    }

    /***
     * 查询某个 field ，没有则返回null
     * 
     * @param clazz
     * @param fName
     * @return
     * @author HadLuo 2020年5月11日 新建
     */
    public static Field selectField(Class<?> clazz, String fName) {
        if (clazz == null || fName == null) {
            return null;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fName)) {
                return field;
            }
        }
        return null;
    }

    /***
     * 查询某个 field ，没有则返回null
     * 
     * @param clazz
     * @param fName
     * @return
     * @author HadLuo 2020年5月11日 新建
     */
    public static Field selectFieldWithSuper(Class<?> clazz, String fName) {
        if (clazz == null || fName == null) {
            return null;
        }
        Class<?> tempClass = clazz;
        while (tempClass != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
            try {
                Field field = tempClass.getDeclaredField(fName);
                if (field != null) {
                    return field;
                }
            } catch (Exception e) {
            }
            tempClass = tempClass.getSuperclass(); // 得到父类,然后赋给自己
        }
        return null;
    }

    /**
     * 融合两个bean （将source bean对象的字段A 拷贝到target bean对象的字段A里面，前提是target里面有的字段 ）
     * 
     * @param source 要取的字段值的bean
     * @param target 要设置字段值的bean
     * @author HadLuo 2020年5月6日 新建
     */
    public static void mergeBean(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        Field field = null;
        Class<?> targetClass = target.getClass();
        for (Field targetField : targetClass.getDeclaredFields()) {
            if (Modifier.isStatic(targetField.getModifiers())) {
                // 不支持静态 复制
                continue;
            }
            Object val = null;
            try {
                field = Classes.selectFieldWithSuper(source.getClass(), targetField.getName());
                if (field == null) {
                    continue;
                }
                field.setAccessible(true);
                val = field.get(source);
                if (val == null) {
                    // 值为空，
                    continue;
                }
                if (!val.getClass().equals(targetField.getType())) {
                    // 类型不一致
                    continue;
                }
                Classes.injectFieldVal(target, targetField.getName(), val);
            } catch (Throwable e) {
            }
        }
    }

}
