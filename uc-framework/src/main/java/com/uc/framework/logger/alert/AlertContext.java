package com.uc.framework.logger.alert;

import com.uc.framework.ClassFactory;

/***
 * 提报告警 组件 <br>
 * 1.支持线上动态 打开或者关闭 某个方法级别的告警 <br>
 * 2.支持发送企业微信或者邮件告警 <br>
 * 3.支持动态修改告警地址 <br>
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年4月28日 新建
 */
public class AlertContext implements Alert {
    private static final AlertContext context = new AlertContext();

    private volatile Alert alert = null;

    private AlertContext() {
    }

    /***
     * 获取机器人告警context
     * 
     * @return
     * @author HadLuo 2020年4月29日 新建
     */
    public static AlertContext robot() {
        return getContext(Alert.Type_Robot);
    }

    /***
     * 获取邮件告警
     * 
     * @return
     * @author HadLuo 2020年5月16日 新建
     */
    public static AlertContext email() {
        return getContext(Alert.Type_Email);
    }

    /***
     * 获取告警 context
     * 
     * @param type 告警类型 @see {@link Alert}
     * @return
     * @author HadLuo 2020年4月29日 新建
     */
    private static AlertContext getContext(byte type) {
        Alert loclAlert = context.alert;
        if (loclAlert == null) {
            if (Alert.Type_Email == type) {
                loclAlert = ClassFactory.newInstance(EmailAlert.class);
            } else if (Alert.Type_Robot == type) {
                loclAlert = ClassFactory.newInstance(RobotAlert.class);
            } else {
                throw new IllegalArgumentException("type=" + type + " value not allow");
            }
        }
        context.alert = loclAlert;
        return context;
    }

    @Override
    public void alert(String message) {
        alert.alert(message);
    }

    @Override
    public void alert(String message, Throwable e) {
        alert.alert(message, e);
    }

    @Override
    public void alert(String message, Throwable e, Object... params) {
        alert.alert(message, e, params);
    }

}
