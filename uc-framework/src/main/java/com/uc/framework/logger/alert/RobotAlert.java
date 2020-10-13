package com.uc.framework.logger.alert;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.util.StringUtils;
import com.uc.framework.Systems;
import com.uc.framework.logger.Logs;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/***
 * 企业微信告警
 * 
 * @author HadLuo
 * @since JDK1.7
 * @history 2020年4月28日 新建
 */
public class RobotAlert extends AbstractAlert {

    private static String Default_Webwork_Url = "https://oapi.dingtalk.com/robot/send?access_token=698f5a68965391f1a5c008634fc0c27502edfe446b4cb7be962080f1f458f897";

    private static final String Msg = "{\r\n" + "            \"msgtype\": \"markdown\",\r\n"
            + "            \"markdown\": {\r\n" + "    \"title\":\"a\" ,\r\n           \"text\": %s\r\n"
            + "            }\r\n" + "        }";

    @Override
    public void alert(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        msg = "【uc-" + Systems.getLocalIP() +" "+ Logs.envAlias()+ "】" + msg;
        try {
            if (!enable()) {
                return;
            }
            String message = msg;
            message = message.replaceAll("\"", "");
            msg = "\"<font color='#ff0000'>%s</font>\"";
            msg = String.format(msg, message);
            msg = String.format(Msg, msg);
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)// 设置连接超时时间
                    .readTimeout(20, TimeUnit.SECONDS)// 设置读取超时时间
                    .build();
            MediaType contentType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(contentType, msg);
            Request request = new Request.Builder().url(getWebworkUrl()).post(body)
                    .addHeader("cache-control", "no-cache").build();
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getWebworkUrl() {
        return Default_Webwork_Url;
    }

//    public static void main(String[] args) {
//        AlertContext.robot().alert("业务异常，比邻接口wxId为空！", new NullPointerException());
//    }

}
