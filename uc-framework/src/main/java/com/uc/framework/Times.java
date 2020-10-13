package com.uc.framework;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/***
 * 线程安全的 时间 操作 工具
 *
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年3月19日 新建
 */
public class Times {

    private static final String STANDDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /***
     * 确保线程安全
     */
    private static ThreadLocal<DateFormat> threadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(STANDDARD_FORMAT);
        }
    };

    /***
     * 判断 start 到 end 是否间隔 大于 hours 小时
     *
     * @param start
     * @param end
     * @param hours
     * @return
     * @author HadLuo 2019年3月20日 新建
     */
    public static boolean isMoreThanHours(Date start, Date end, int hours) {
        long cha = end.getTime() - start.getTime();
        double result = cha * 1.0 / (1000 * 60 * 60);
        if (result <= hours) {
            return false; // 说明小于24小时
        } else {
            return true;
        }
    }

    /***
     * title: 判断 start 到 end 是否间隔 大于 seconds 秒
     *
     * @param start
     * @param end
     * @param hours
     * @return
     * @author HadLuo 2019年3月20日 新建
     */
    public static boolean isMoreThanSeconds(Date start, Date end, int seconds) {
        long cha = end.getTime() - start.getTime();
        double result = cha * 1.0 / (1000);
        if (result <= seconds) {
            return false; // 说明小于24小时
        } else {
            return true;
        }
    }

    public static Object convertFormat(Object date) {
        Date temp;
        if (date instanceof Date) {
            temp = (Date) date;
        } else {
            return date;
        }
        return format(temp);
    }

    /***
     * 格式化时间
     *
     * @param date
     * @return
     * @author HadLuo 2018年6月15日 新建
     */
    public static Date parse(String source) {
        try {
            return sdf().parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * 格式化时间
     *
     * @param date
     * @return
     * @author HadLuo 2018年6月15日 新建
     */
    public static String format(Date date) {
        if (date == null) {
            return "";
        }
        return sdf().format(date);
    }

    private static DateFormat sdf() {
        DateFormat sdf = threadLocal.get();
        if (null == sdf) {
            sdf = new SimpleDateFormat(STANDDARD_FORMAT);
            threadLocal.set(sdf);
        }
        return sdf;
    }

    /***
     * 格式化时间
     *
     * @param date
     * @return
     * @author HadLuo 2018年6月15日 新建
     */
    public static String format(Date date, String partten) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(partten).format(date);
    }

    /**
     * 获取指定日期往前(num<0)或者往后的日期(num>0)
     *
     * @param num
     * @return
     */
    public static String getDay(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);

        return sdf().format(calendar.getTime());
    }

    /**
     * 获取指定日期往前(num<0)或者往后的日期(num>0)
     *
     * @param num
     * @return
     */
    public static Date getDayReturnDate(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    /**
     * 获取指定日期往前(num<0)或者往后的日期(num>0)
     *
     * @param num
     * @return
     */
    public static Date getMin(Date date, int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, min);
        return calendar.getTime();
    }

    /**
     * 获取指定日期往前(num<0)或者往后的日期(num>0)
     *
     * @param num
     * @return
     */
    public static Date getSecond(Date date, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, second);
        return calendar.getTime();
    }

    /**
     * 获取指定日期往前(hour<0)或者往后的日期(hour>0)
     *
     * @param num
     * @return
     */
    public static String getHourFormatString(Date date, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hour);
        return sdf().format(calendar.getTime());
    }

    /**
     * 获取指定日期往前(hour<0)或者往后的日期(hour>0)
     *
     * @param num
     * @return
     */
    public static Date getHourFormatDate(Date date, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hour);
        return calendar.getTime();
    }

    /***
     * 是否过了当前时间
     *
     * @param time
     * @return true-过了 false-没有
     * @author HadLuo 2019年3月27日 新建
     */
    public static boolean passNow(Date time) {
        if (time == null) {
            return false;
        }
        if (time.getTime() <= System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    /**
     * 获取指定日期 00:00:00
     * @return
     */
    public static Date getFirstDate(Date date){
        Calendar day = Calendar.getInstance();
        day.setTime(date);
        day.set(Calendar.HOUR_OF_DAY,0);
        day.set(Calendar.MINUTE,0);
        day.set(Calendar.SECOND,0);
        day.set(Calendar.MILLISECOND,0);
        return day.getTime();
    }

    /**
     * 获取指定日期 23:59:59
     * @return
     */
    public static Date getLastDate(Date date){
        Calendar day = Calendar.getInstance();
        day.setTime(date);
        day.set(Calendar.HOUR_OF_DAY,23);
        day.set(Calendar.MINUTE,59);
        day.set(Calendar.SECOND,59);
        day.set(Calendar.MILLISECOND,999);
        return day.getTime();
    }
    
    /***
     * 
     * title: 判断 两个时间的  时分秒  是否当前时间内   
     *
     * @param start
     * @param end
     * @return
     * @author HadLuo 2020-9-24 11:46:30
     */
    public static boolean betweenHMS(Date start , Date end) {
        @SuppressWarnings("deprecation")
        long s1 = start.getHours()*3600+start.getMinutes()*60 + start.getSeconds() ;
        @SuppressWarnings("deprecation")
        long s2 = end.getHours()*3600+end.getMinutes()*60 + end.getSeconds() ;
        Date now = new Date();
        @SuppressWarnings("deprecation")
        long s3 = now.getHours()*3600+now.getMinutes()*60 + now.getSeconds() ;
        if(s3 >= s1 && s3 <= s2) {
            return true ;
        }
        return false ;
    }
    
    /***
     * 
     * title: 获取当前时间到凌晨0点的秒差
     *
     * @return
     * @author HadLuo 2020-9-25 8:38:37
     */
    public static int getSecondsTobeforedawn() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        // 改成这样就好了
        cal.set(Calendar.HOUR_OF_DAY, 0);      
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return (int) ((cal.getTimeInMillis() - System.currentTimeMillis()) / 1000);
    }
}