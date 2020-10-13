package com.uc.framework;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class Numbers {

    /**
     * 
     * title: 判断 枚举值是否正确
     *
     * @param <T>
     * @param val
     * @param ins
     * @return true-没有在枚举值里面,枚举值错误 false-枚举值正确
     * @author HadLuo 2020-9-15 13:54:15
     */
    public static <T> boolean assertInEnums(T val, Object... ins) {
        if (val == null) {
            return true;
        }
        if (ins == null) {
            return false;
        }
        boolean in = false;
        for (Object t : ins) {
            if (t.equals(val)) {
                in = true;
                break;
            }
        }
        if (in) {
            return false;
        }
        return true;
    }

    /***
     * 计算 sql 的 分页 信息 ( LIMIT #offset#,#limit# )
     * 
     * @return [0]-offset [1]-limit
     * @author HadLuo 2018年6月12日 新建
     */
    public static Pair<Integer, Integer> getDBIndexPos(Integer pageIndex, Integer pageSize) {
        if (pageSize == null) {
            pageSize = 10;
        }
        if (pageIndex == null || pageIndex < 0) {
            pageIndex = 0;
        }
        return Pair.of(pageIndex * pageSize, pageSize);
    }

    /***
     * 根据 分页信息计算 redis zrange的下标取值信息
     * 
     * @param pageIndex 当前页，从0开始
     * @param pageSize 每页大小
     * @return <startPos,endPos>
     * @author HadLuo 2019年6月10日 新建
     */
    public static Pair<Integer, Integer> getZrangeIndexPos(Integer pageIndex, Integer pageSize) {
        if (pageIndex == null || pageSize == null) {
            return Pair.of(0, -1);
        }
        if (pageIndex == 0 && pageSize == Integer.MAX_VALUE) {
            return Pair.of(0, -1);
        }
        int start = pageIndex * pageSize;
        int end = start + pageSize - 1;
        return Pair.of(start, end);
    }

    /***
     * 字符串转int。 转出异常时，直接返回默认值
     * 
     * @param str
     * @param defaultVal
     * @return
     * @author HadLuo 2019年5月23日 新建
     */
    public static int toInt(String resource, int defaultVal) {
        if (StringUtils.isEmpty(resource)) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(resource);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    /***
     * 字符串转boolean。 转出异常时，直接返回默认值
     * 
     * @param str
     * @param defaultVal
     * @return
     * @author HadLuo 2019年5月23日 新建
     */
    public static boolean toBoolean(String resource, boolean defaultVal) {
        if (StringUtils.isEmpty(resource)) {
            return defaultVal;
        }
        try {
            return Boolean.parseBoolean(resource);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    /***
     * 字符串转double。 转出异常时，直接返回默认值
     * 
     * @param str
     * @param defaultVal
     * @return
     * @author HadLuo 2019年6月12日 新建
     */
    public static double toDouble(String resource, double defaultVal) {
        if (StringUtils.isEmpty(resource)) {
            return defaultVal;
        }
        try {
            return Double.parseDouble(resource);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    /***
     * 计算和
     * 
     * @param bs
     * @return
     * @author HadLuo 2019年8月2日 新建
     */
    public static BigDecimal plus(List<BigDecimal> bs) {
        if (CollectionUtils.isEmpty(bs)) {
            return new BigDecimal("0");
        }
        BigDecimal total = new BigDecimal("0");
        for (BigDecimal cur : bs) {
            total = total.add(cur);
        }
        return total;
    }

    /***
     * 随机 [min,max]之间的一个值(包括min,max)
     * 
     * @param min
     * @param max
     * @return
     * @author HadLuo 2019年8月5日 新建
     */
    public static int random(int min, int max) {
        if (min == max) {
            return min;
        }
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return s;
    }

}
