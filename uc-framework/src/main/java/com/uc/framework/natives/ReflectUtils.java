package com.uc.framework.natives;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ReflectUtils {
    /**
     * void(V).
     */
    public static final char JVM_VOID = 'V';

    /**
     * boolean(Z).
     */
    public static final char JVM_BOOLEAN = 'Z';

    /**
     * byte(B).
     */
    public static final char JVM_BYTE = 'B';

    /**
     * char(C).
     */
    public static final char JVM_CHAR = 'C';

    /**
     * double(D).
     */
    public static final char JVM_DOUBLE = 'D';

    /**
     * float(F).
     */
    public static final char JVM_FLOAT = 'F';

    /**
     * int(I).
     */
    public static final char JVM_INT = 'I';

    /**
     * long(J).
     */
    public static final char JVM_LONG = 'J';

    /**
     * short(S).
     */
    public static final char JVM_SHORT = 'S';

    public static String getName(Class<?> c) {
        if (c.isArray()) {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append("[]");
                c = c.getComponentType();
            } while (c.isArray());

            return c.getName() + sb.toString();
        }
        return c.getName();
    }

    public static String getDescWithoutMethodName(Method m) {
        StringBuilder ret = new StringBuilder();
        ret.append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
            ret.append(getDesc(parameterTypes[i]));
        ret.append(')').append(getDesc(m.getReturnType()));
        return ret.toString();
    }

    public static String getDesc(Class<?> c) {
        StringBuilder ret = new StringBuilder();

        while (c.isArray()) {
            ret.append('[');
            c = c.getComponentType();
        }

        if (c.isPrimitive()) {
            String t = c.getName();
            if ("void".equals(t))
                ret.append(JVM_VOID);
            else if ("boolean".equals(t))
                ret.append(JVM_BOOLEAN);
            else if ("byte".equals(t))
                ret.append(JVM_BYTE);
            else if ("char".equals(t))
                ret.append(JVM_CHAR);
            else if ("double".equals(t))
                ret.append(JVM_DOUBLE);
            else if ("float".equals(t))
                ret.append(JVM_FLOAT);
            else if ("int".equals(t))
                ret.append(JVM_INT);
            else if ("long".equals(t))
                ret.append(JVM_LONG);
            else if ("short".equals(t))
                ret.append(JVM_SHORT);
        } else {
            ret.append('L');
            ret.append(c.getName().replace('.', '/'));
            ret.append(';');
        }
        return ret.toString();
    }

    public static String getDesc(final Constructor<?> c) {
        StringBuilder ret = new StringBuilder("(");
        Class<?>[] parameterTypes = c.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++)
            ret.append(getDesc(parameterTypes[i]));
        ret.append(')').append('V');
        return ret.toString();
    }

    /***
     * 获取 非静态，公有方法
     * 
     * @param clazz
     * @return
     * @author HadLuo 2017年11月20日 新建
     */
    public static List<Method> getPublicMethod(Class<?> clazz) {
        List<Method> ms = new ArrayList<Method>();
        for (Method m : clazz.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) {
                ms.add(m);
            }
        }
        return ms;
    }

    /***
     * 执行方法
     * 
     * @param target
     * @param method
     * @param args
     * @author HadLuo 2017年10月25日 新建
     */
    public static Object invoke(Object target, Method method, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 判断 a是否是 b的 子类
     * 
     * @param a
     * @param b
     * @return
     * @author HadLuo 2017年10月24日 新建
     */
    public static boolean isChild(Class<?> a, Class<?> b) {
        return b.isAssignableFrom(a);
    }

    /***
     * 获取 此类，及其超类的 所有字段
     * 
     * @return
     * @author HadLuo 2017年10月25日 新建
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<Field>();
        while (clazz != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass(); // 得到父类,然后赋给自己
        }
        return fieldList;
    }

    /***
     * 获取 此类，及其超类的 所有字段
     * 
     * @return
     * @author HadLuo 2017年10月25日 新建
     */
    public static List<Field> getAllFieldsByAnnotation(Class<?> clazz,
            Class<? extends Annotation> annotationClass) {
        List<Field> fieldList = new ArrayList<Field>();
        while (clazz != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).

            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(annotationClass)) {
                    fieldList.add(f);
                }
            }
            clazz = clazz.getSuperclass(); // 得到父类,然后赋给自己
        }
        return fieldList;
    }

    /***
     * 获取 此类及其超类 有 对应注解 的字段
     * 
     * @return
     * @author HadLuo 2017年10月25日 新建
     */
    public static Annotation getFieldAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        while (clazz != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).

            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(annotationClass)) {
                    return f.getAnnotation(annotationClass);
                }
            }

            clazz = clazz.getSuperclass(); // 得到父类,然后赋给自己
        }
        return null;
    }

    /***
     * 获取 此类，及其超类的 所有方法
     * 
     * @return
     * @author HadLuo 2017年10月25日 新建
     */
    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Method m = null;
        while (clazz != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
            try {
                m = clazz.getDeclaredMethod(name, parameterTypes);
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            if (m != null) {
                break;
            }
            clazz = clazz.getSuperclass(); // 得到父类,然后赋给自己
        }
        return m;
    }

    /***
     * 如果字段配置了 注解 就注入此字段值 （包括超类字段）
     * 
     * @param clazz 目标类
     * @param shouldbeClass 字段的类型
     * @param annotationClass 字段添加的注解
     * @param target 目标对象
     * @param val 注入的值
     * @author HadLuo 2017年11月1日 新建
     */
    public static void autowiredField(Class<?> clazz, Class<?> shouldbeClass,
            Class<? extends Annotation> annotationClass, Object target, Object val) {
        Field m = null;
        while (clazz != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
            try {

                for (Field ms : clazz.getDeclaredFields()) {
                    if (ms.isAnnotationPresent(annotationClass)
                            && ReflectUtils.isChild(ms.getType(), shouldbeClass)) {
                        m = clazz.getDeclaredField(ms.getName());
                        break;
                    }
                }
            } catch (Exception e) {
                // TODO
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            if (m != null) {
                break;
            }
            clazz = clazz.getSuperclass(); // 得到父类,然后赋给自己
        }
        if (m != null) {
            ReflectUtils.setFieldValue(m, target, val);
        }
    }

    /***
     * 查找 配置了 注解 的方法 , 范围包括超类
     * 
     * @param annotationClass
     * @param clazz
     * @param parameterTypes
     * @return
     * @author HadLuo 2017年11月1日 新建
     */
    public static Method getMethodByAnnotation(Class<? extends Annotation> annotationClass, Class<?> clazz,
            Class<?>... parameterTypes) {
        Method m = null;
        while (clazz != null) {// 当父类为null的时候说明到达了最上层的父类(Object类).
            try {

                for (Method ms : clazz.getDeclaredMethods()) {
                    if (ms.isAnnotationPresent(annotationClass)) {
                        m = ms;
                        break;
                    }
                }

            } catch (Exception e) {
                // TODO
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            if (m != null) {
                break;
            }
            clazz = clazz.getSuperclass(); // 得到父类,然后赋给自己
        }
        return m;
    }

    /***
     * 构造 实例
     * 
     * @param clazz
     * @return
     * @author HadLuo 2017年10月25日 新建
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <T> T newInstance(Class<T> clazz) {
        Constructor<T> c;
        try {
            c = clazz.getConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    public static <T> T newInstanceWithEx(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e1) {
            Constructor<T> c;
            try {
                c = clazz.getConstructor();
                c.setAccessible(true);
                return c.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e1);
            }
        }
    }

    public static Class<?> forClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 为字段 设置值
     * 
     * @param field
     * @param target
     * @param fieldVal
     * @throws RuntimeException
     * @author HadLuo 2017年10月25日 新建
     */
    public static void setFieldValue(Field field, Object target, Object fieldVal) throws RuntimeException {
        field.setAccessible(true);
        try {
            Class<?> type = field.getType();
            if (type.equals(Boolean.class) || type.equals(boolean.class)) {
                field.setBoolean(target, toBoolean(fieldVal));
                return;
            }
            if (type.equals(Byte.class) || type.equals(byte.class)) {
                field.setByte(target, toByte(fieldVal));
                return;
            }
            if (type.equals(Character.class) || type.equals(char.class)) {
                field.setChar(target, toChar(fieldVal));
                return;
            }
            if (type.equals(Double.class) || type.equals(double.class)) {
                field.setDouble(target, toDouble(fieldVal));
                return;
            }
            if (type.equals(Float.class) || type.equals(float.class)) {
                field.setFloat(target, toFloat(fieldVal));
                return;
            }
            if (type.equals(Integer.class) || type.equals(int.class)) {
                field.setInt(target, toInt(fieldVal));
                return;
            }
            if (type.equals(Long.class) || type.equals(long.class)) {
                field.setLong(target, toLong(fieldVal));
                return;
            }
            if (type.equals(Short.class) || type.equals(short.class)) {
                field.setShort(target, toShort(fieldVal));
                return;
            }
            field.set(target, fieldVal);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 从包package中获取所有的Class
     * 
     * @param pack
     * @return
     */
    public static Set<Class<?>> getClasses(String pack) {
        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // // 如果是jar包文件
                    // // 定义一个JarFile
                    // JarFile jar;
                    // try {
                    // // 获取jar
                    // jar = ((JarURLConnection)
                    // url.openConnection()).getJarFile();
                    // // 从此jar包 得到一个枚举类
                    // Enumeration<JarEntry> entries = jar.entries();
                    // // 同样的进行循环迭代
                    // while (entries.hasMoreElements()) {
                    // // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                    // JarEntry entry = entries.nextElement();
                    // String name = entry.getName();
                    // // 如果是以/开头的
                    // if (name.charAt(0) == '/') {
                    // // 获取后面的字符串
                    // name = name.substring(1);
                    // }
                    // // 如果前半部分和定义的包名相同
                    // if (name.startsWith(packageDirName)) {
                    // int idx = name.lastIndexOf('/');
                    // // 如果以"/"结尾 是一个包
                    // if (idx != -1) {
                    // // 获取包名 把"/"替换成"."
                    // packageName = name.substring(0, idx).replace('/', '.');
                    // }
                    // // 如果可以迭代下去 并且是一个包
                    // if ((idx != -1) || recursive) {
                    // // 如果是一个.class文件 而且不是目录
                    // if (name.endsWith(".class") && !entry.isDirectory()) {
                    // // 去掉后面的".class" 获取真正的类名
                    // String className = name.substring(packageName.length() +
                    // 1,
                    // name.length() - 6);
                    // try {
                    // // 添加到classes
                    // classes.add(Class.forName(packageName + '.' +
                    // className));
                    // } catch (ClassNotFoundException e) {
                    // // log
                    // // .error("添加用户自定义视图类错误 找不到此类的.class文件");
                    // e.printStackTrace();
                    // }
                    // }
                    // }
                    // }
                    // }
                    // } catch (IOException e) {
                    // // log.error("在扫描用户定义视图时从jar包获取文件出错");
                    // e.printStackTrace();
                    // }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     * 
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath,
            final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(),
                        recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    // classes.add(Class.forName(packageName + '.' +
                    // className));
                    // 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                    classes.add(Thread.currentThread().getContextClassLoader()
                            .loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    // log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                    e.printStackTrace();
                }
            }
        }
    }

    public static int toInt(Object object) {
        Class<?> type = object.getClass();
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return (Integer) object;
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return ((Double) object).intValue();
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return ((Float) object).intValue();
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return ((Long) object).intValue();
        }
        if (type.equals(short.class) || type.equals(Short.class)) {
            return ((Short) object).intValue();
        }
        return (Integer) object;
    }

    public static long toLong(Object object) {
        Class<?> type = object.getClass();
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return ((Integer) object).longValue();
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return ((Double) object).longValue();
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return ((Float) object).longValue();
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return (Long) object;
        }
        if (type.equals(short.class) || type.equals(Short.class)) {
            return ((Short) object).longValue();
        }
        return (Long) object;
    }

    public static short toShort(Object object) {
        Class<?> type = object.getClass();
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return ((Integer) object).shortValue();
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return ((Double) object).shortValue();
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return ((Float) object).shortValue();
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return ((Long) object).shortValue();
        }
        if (type.equals(short.class) || type.equals(Short.class)) {
            return (Short) object;
        }
        return (Short) object;
    }

    public static double toDouble(Object object) {
        Class<?> type = object.getClass();
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return ((Integer) object).doubleValue();
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return (Double) object;
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return ((Float) object).doubleValue();
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return ((Long) object).doubleValue();
        }
        if (type.equals(short.class) || type.equals(Short.class)) {
            return ((Short) object).doubleValue();
        }
        return (Double) object;
    }

    public static float toFloat(Object object) {
        Class<?> type = object.getClass();
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return ((Integer) object).floatValue();
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return ((Double) object).floatValue();
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return (Float) object;
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return ((Long) object).floatValue();
        }
        if (type.equals(short.class) || type.equals(Short.class)) {
            return ((Short) object).floatValue();
        }
        return (Float) object;
    }

    public static boolean toBoolean(Object object) {
        Class<?> type = object.getClass();
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return (Boolean) object;
        }
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return ((Integer) object).equals(0) ? false : true;
        }
        return (Boolean) object;
    }

    public static byte toByte(Object object) {
        Class<?> type = object.getClass();
        if (type.equals(byte.class) || type.equals(Byte.class)) {
            return (Byte) object;
        }
        return (Byte) object;
    }

    public static char toChar(Object object) {
        Class<?> type = object.getClass();
        if (type.equals(char.class) || type.equals(Character.class)) {
            return (Character) object;
        }
        return (Character) object;
    }
}
