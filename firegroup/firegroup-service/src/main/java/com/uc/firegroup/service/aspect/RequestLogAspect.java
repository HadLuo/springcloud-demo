package com.uc.firegroup.service.aspect;

import com.alibaba.fastjson.JSON;
import com.uc.framework.UUIDUtils;
import com.uc.framework.logger.Logs;
import com.uc.framework.login.User;
import com.uc.framework.login.UserThreadLocal;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author: Ryan.yuan
 * @time: 2020/9/30/030 9:24
 */
@Aspect
@Component
public class RequestLogAspect {

    ThreadLocal<String> traceIdThreadLocal = new ThreadLocal<String>();


    // 统一切点,对com.hangtian.admin.controller及其子包中所有的类的所有方法切面
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void Pointcut() {
    }

    @Before("Pointcut()")
    public void before(JoinPoint joinPoint) {
        try {
            Object args[] = joinPoint.getArgs();
            MethodSignature sig = (MethodSignature) joinPoint.getSignature();
            Method method = sig.getMethod();
            if (null != method.getDeclaringClass().getName() && null != method.getName() && null != args && args.length > 0) {
                //生成traceId
                traceIdThreadLocal.set(UUIDUtils.getUUID32());
                User user = UserThreadLocal.get();
                String userName = user == null ? "null" : user.getUsername();
                Logs.e(getClass(), String.format("traceId:%s,userName:%s,class:%s, method:%s, 请求参数：%s", traceIdThreadLocal.get(), userName, method.getDeclaringClass().getName(), method.getName(), JSON.toJSONString(args)));
            }
        } catch (Exception e) {

        }
    }

    @AfterReturning(value = "Pointcut()", returning = "rvt")
    public void after(JoinPoint joinPoint, Object rvt) {
        try {
            MethodSignature sig1 = (MethodSignature) joinPoint.getSignature();
            Method method1 = sig1.getMethod();
            if (null != rvt && null != method1.getDeclaringClass()) {
                Logs.e(getClass(), String.format("traceId:%s,返回数据：%s", traceIdThreadLocal.get(),JSON.toJSONString(rvt)));
            }
        } catch (Exception e) {

        } finally {
            traceIdThreadLocal.remove();
        }
    }


    @AfterThrowing(throwing = "ex", pointcut = "Pointcut()")
    public void doRecoveryActions(Throwable ex) throws Throwable{
        Logs.e(getClass(), String.format("请求出现异常,traceId:%s", traceIdThreadLocal.get()),ex);
        throw ex;
    }
}
