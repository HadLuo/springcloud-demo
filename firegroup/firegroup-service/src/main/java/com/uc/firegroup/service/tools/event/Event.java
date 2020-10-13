package com.uc.firegroup.service.tools.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事件组件
 * 
 * @author HadLuo
 * @date 2020-9-2 16:43:17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Event {

    /***
     * 
     * title: 事件类型的值
     *
     * @return
     * @author HadLuo 2020-9-17 13:52:34
     */
    public int value();
}
