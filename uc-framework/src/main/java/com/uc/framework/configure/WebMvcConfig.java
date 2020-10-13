package com.uc.framework.configure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.uc.framework.App;
import com.uc.framework.login.LoginInterceptor;

/**
 * web配置
 * 
 * @author HadLuo
 * @date 2020-9-2 15:52:58
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * add 自定义的拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(App.getBean(LoginInterceptor.class));
    }

}
