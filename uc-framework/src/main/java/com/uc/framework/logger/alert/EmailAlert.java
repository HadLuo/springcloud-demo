package com.uc.framework.logger.alert;

/***
 * 邮件告警
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年4月29日 新建
 */
public class EmailAlert extends AbstractAlert {

    @Override
    public void alert(String message) {
        try {
            if (!enable()) {
                return;
            }
//            Emails.asynSendEmail(RedisProperties.getString("config.alert.emailer",
//                    Constants.DEFAULT_EMAIL_RECEIVE_EXCEPTION), "系统代码异常", message, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
