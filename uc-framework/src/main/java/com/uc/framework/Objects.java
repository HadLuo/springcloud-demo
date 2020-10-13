package com.uc.framework;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import org.springframework.util.CollectionUtils;

/****
 * Object对象工具
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2018年8月8日 新建
 */
public final class Objects {
    /***
     * 反射获取 对象的 字段值
     * 
     * @param target
     * @param fName
     * @return
     * @author HadLuo 2019年7月1日 新建
     */
    public static Object getFieldVal(Object target, String fName) {
        if (null == target) {
            return null;
        }
        try {
            Field fActivityFlag = target.getClass().getDeclaredField(fName);
            fActivityFlag.setAccessible(true);
            return fActivityFlag.get(target);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 反射设置 对象的 字段值
     * 
     * @param target
     * @param fName
     * @param val
     * @author HadLuo 2019年7月1日 新建
     */
    public static void setFieldVal(Object target, String fName, Object val) {
        if (null == target) {
            return;
        }
        try {
            Field fActivityFlag = target.getClass().getDeclaredField(fName);
            fActivityFlag.setAccessible(true);
            fActivityFlag.set(target, val);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static boolean isMoreThenTwo(String val) {
        if (val.contains(".")) {
            val = val.substring(val.indexOf(".") + 1);
            if (val.length() > 2) {
                return true;
            }
        }
        return false;
    }

    /***
     * 四舍五入
     * 
     * @param number
     * @return
     * @author HadLuo 2019年4月9日 新建
     */
    public static double roundOff2(Number number) {
        if (number == null) {
            return 0d;
        }
        // 判断是否是 2位小数,是的话不处理
        DecimalFormat df1 = new DecimalFormat("0.00");
        if (number instanceof BigDecimal) {
            if (isMoreThenTwo(((BigDecimal) number).doubleValue() + "")) {
                // 不止二位小数 ， 四舍五入
                return ((BigDecimal) number).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
            } else {
                return ((BigDecimal) number).doubleValue();
            }
        } else {
            if (number instanceof Double) {
                if (isMoreThenTwo(((Double) number).doubleValue() + "")) {
                    // 不止二位小数 ， 四舍五入
                    String str = df1.format(new BigDecimal(number.doubleValue()));
                    return Double.parseDouble(str);
                } else {
                    return ((Double) number);
                }
            } else if (number instanceof Float) {
                if (isMoreThenTwo(((Float) number).doubleValue() + "")) {
                    // 不止二位小数 ， 四舍五入
                    String str = df1.format(new BigDecimal(number.floatValue()));
                    return Double.parseDouble(str);
                } else {
                    return ((Float) number).doubleValue();
                }
            } else {
                String str = df1.format(new BigDecimal(number.intValue()));
                return Double.parseDouble(str);
            }
        }
    }

    public static boolean isNullOrZero(Integer val) {
        if (val == null || val == 0) {
            return true;
        }
        return false;
    }

    /***
     * 对象为null ， 设置一个 默认值
     * 
     * @param targetVal
     * @param defaultVal
     * @return
     * @author HadLuo 2018年8月8日 新建
     */
    public static <T> T wrapNull(T targetVal, T defaultVal) {
        if (targetVal == null) {
            return defaultVal;
        }
        return targetVal;
    }

    public static String wrapNull(String targetVal) {
        return wrapNull(targetVal, "");
    }

    /***
     * 判断integer为空， 设置默认0
     * 
     * @param targetVal
     * @return
     * @author HadLuo 2018年8月8日 新建
     */
    public static Integer wrapNull(Integer targetVal) {
        return wrapNull(targetVal, 0);
    }

    /***
     * 判断double为空， 设置默认0
     * 
     * @param targetVal
     * @return
     * @author HadLuo 2018年8月8日 新建
     */
    public static Double wrapNull(Double targetVal) {
        return wrapNull(targetVal, 0d);
    }

    /***
     * 根据条件过滤 集合 中的 数据
     * 
     * @param src
     * @param filter
     * @author HadLuo 2018年9月18日 新建
     */
    public static <T> void filter(Collection<T> src, Filter<T> filter) {
        if (filter == null || CollectionUtils.isEmpty(src)) {
            return;
        }
        List<T> temp = new ArrayList<T>();
        for (T data : src) {
            if (filter.doFilter(data)) {
                temp.add(data);
            }
        }
        for (T remove : temp) {
            src.remove(remove);
        }
    }

    public static interface Filter<T> {
        public boolean doFilter(T cur);
    }

    public static void gc(List<?> list) {
        if (list != null) {
            list.clear();
        }
    }

    /***
     * 从集合的数据中随机一个
     * 
     * @param <T>
     * @param datas
     * @return
     * @author HadLuo 2020年2月11日 新建
     */
    public static <T> Pair<Integer, T> randomForList(List<T> datas) {
        if (CollectionUtils.isEmpty(datas)) {
            return Pair.of(null, null);
        }
        if (datas.size() == 1) {
            return Pair.of(0, datas.get(0));
        }
        int idx = new Random().nextInt(datas.size());
        return Pair.of(idx, datas.get(idx));
    }

}
