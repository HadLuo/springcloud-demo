package com.uc.framework.logger.alert;

import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.uc.framework.StackTraces;

/***
 * 告警抽象
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年4月29日 新建
 */
public abstract class AbstractAlert implements Alert {
    @Override
    public void alert(String message, Throwable e) {
        if (e != null) {
            alert(message + "\n" + StackTraces.convertString(e, 500));
        } else {
            alert(message);
        }
    }

    @Override
    public void alert(String message, Throwable e, Object... params) {
        StringBuilder sb;
        if (!StringUtils.isEmpty(message)) {
            sb = new StringBuilder(message + ",参数:");
        } else {
            sb = new StringBuilder("参数:");
        }
        if (params != null && params.length > 0) {
            for (Object obj : params) {
                sb.append("[");
                sb.append(JSON.toJSONString(obj));
                sb.append("],");
            }
        }
        if (sb.toString().endsWith(",")) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        alert(sb.toString(), e);
    }

    /**
     * 功能是否可用
     * 
     * @param key
     * @return true=可用 false-不可用
     * @author HadLuo 2020年5月16日 新建
     */
    public boolean enable() {
        // Method[] ms = AbstractAlert.class.getDeclaredMethods();
        // String[] names = new String[ms.length];
        // for (int i = 0; i < ms.length; i++) {
        // names[i] = ms[i].getName();
        // }
        // // 过滤当前方法 ，返回的是 ： 调用alert的类.方法名
        // //
        // 如：com.yunji.admin.controller.activitypool.ScheduleLaunchController.test
        // String caller = StackTraces.getCallerInfo(names);
        // if (StringUtils.isEmpty(caller)) {
        // return true;
        // }
        // //
        // alert.enable.com.yunji.admin.controller.activitypool.ScheduleLaunchController.test
        // int ret = RedisProperties.getInt("alert.enable." + caller, 0);
        // if (ret == 0) {
        // return true;
        // }
        // return false;
        return true;
    }

}
