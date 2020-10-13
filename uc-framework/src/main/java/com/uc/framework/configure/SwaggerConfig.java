package com.uc.framework.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.common.base.Predicates;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 
 * Swagger 文档 配置
 * 
 * @author HadLuo
 * @date 2020-9-4 8:47:43
 */
@SuppressWarnings("deprecation")
@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurerAdapter {
    @Value("${spring.application.name}")
    private String name;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars*")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any()).paths(Predicates.not(PathSelectors.regex("/error.*"))).build()
                .apiInfo(apiInfo()).groupName(name);

    }

    /**
     * apiInfo 定义基本文档信息
     * 
     * @return
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("暖宝群接口文档").description("").license("License Version 1.0")
                .version("1.0").build();
    }
}