package com.uc.firegroup.service.tools.msg;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * title : 消息类型 {@link com.uc.external.bilin.CallbackCode#resource}
 * 
 * 
 * 
 * @author HadLuo
 * @date 2020-9-2 16:43:17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Type {

    /**
     * title: 消息类型 {@link com.uc.external.bilin.CallbackCode#resource}
     * 
     * 
     * 
     * @return
     * @author HadLuo 2020-9-12 14:07:59
     */
    public int value();

    /**
     * 
     * title: 消息体的 类型
     *
     * @return
     * @author HadLuo 2020-9-14 15:50:16
     */
    public Class<?> clazz() default Object.class;
}
