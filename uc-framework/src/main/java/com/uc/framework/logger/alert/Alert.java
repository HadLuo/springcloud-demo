package com.uc.framework.logger.alert;

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
public interface Alert {

    /***
     * 邮件告警类型
     */
    public static final byte Type_Email = 0;
    /***
     * 企业微信机器人告警类型
     */
    public static final byte Type_Robot = 1;

    public void alert(String message);

    public void alert(String message, Throwable e);

    public void alert(String message, Throwable e, Object... params);

}
