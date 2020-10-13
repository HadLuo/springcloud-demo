package com.uc.framework.login;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import com.uc.framework.logger.Logs;
import com.uc.framework.logger.alert.AlertContext;
import com.uc.framework.obj.Result;

/**
 * login登录拦截器
 * 
 * @author HadLuo
 * @date 2020-9-2 15:44:38
 */
@Component
@RefreshScope
public class LoginInterceptor implements HandlerInterceptor {

    private static final int UnkonwLoginCode = 10011;
    private static final String UnkonwLoginMsg = "登录异常";
    /** 商户 id */
    @Value(value = "${token}")
    public String token;
    @Value(value = "${env}")
    public String env;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        boolean outter = false;
        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            Login login = ((HandlerMethod) handler).getMethodAnnotation(Login.class);
            if (login == null) {
                return true;
            }
            outter = true;
        }
        if (!outter) {
            // 外部接口 或者 资源
            return true;
        }
        if (Logs.isDev()) {
            // 开发环境
            User user = new DebugHeaderParser(token).parse();
            if (user == null) {
                sendJsonMessage(response, Result.err(UnkonwLoginCode, UnkonwLoginMsg));
                Logs.e(getClass(), "login>>parse ex");
                return false;
            }
            UserThreadLocal.set(user);
            return true;
        } else {
            // 正式环境 ,测试
            try {
                HeaderParser parser = AbstractHeaderParser.resolver(request);
                if (parser == null) {
                    sendJsonMessage(response, Result.err(UnkonwLoginCode, UnkonwLoginMsg));
                    Logs.e(getClass(), "login>>resolver ex");
                    return false;
                }
                User user = parser.parse();
                if (user == null) {
                    sendJsonMessage(response, Result.err(UnkonwLoginCode, UnkonwLoginMsg));
                    Logs.e(getClass(), "login>>parse ex");
                    return false;
                }
                UserThreadLocal.set(user);
                return true;
            } catch (Throwable e) {
                Logs.e(getClass(), "[登录拦截器异常]>>" + request, e);
                AlertContext.robot().alert("[登录拦截器异常]>>" + request, e);
            }
            sendJsonMessage(response, Result.err(UnkonwLoginCode, UnkonwLoginMsg));
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        UserThreadLocal.remove();
    }

    public static void sendJsonMessage(HttpServletResponse response, Object obj) throws Exception {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.print(JSON.toJSONString(obj));
        writer.close();
        response.flushBuffer();
    }
}
