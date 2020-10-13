package com.uc.firegroup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.uc.firegroup.service.config.Env;
import com.uc.firegroup.service.inner.chat.AbstractChatPushProssor;
import com.uc.firegroup.service.inner.chat.JobChatPushProcessor;
import com.uc.firegroup.service.inner.chat.KeyWordsChatPushProcessor;
import com.uc.framework.App;
import com.uc.framework.chat.context.ChatProcessor;
import com.uc.framework.logger.Logs;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * main函数 主入口
 * 
 * @author HadLuo
 * @date 2020-9-8 15:17:17
 */
@EnableFeignClients(basePackages = "com.uc") // 开启 FeignClients
@SpringBootApplication(scanBasePackages = "com.uc")
@EnableDiscoveryClient // 开启注册中心服务发现
@EnableAspectJAutoProxy
public class MainApplication implements ApplicationRunner {
    @Autowired
    Env env;

    public static void main(String[] args) {
        // 本地开发需加上环境： NACOS_CONFIG_ADDR = http://172.21.16.184:8848
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Logs.registerEnv(Env.env());
        System.err.println();
        System.err.println("||===================================================||");
        System.err.println("||***************************************************||");
        System.err.println("||******************暖群宝***************************||");
        System.err.println("||***************************************************||");
        System.err.println("||*****************" + Logs.envAlias() + "**************************||");
        System.err.println("\tenv：" + Env.env() + " " + Logs.envAlias());
        System.err.println("\tidentity：" + Env.identity());
        System.err.println("\tidc：" + Env.idc());
        System.err.println("\tmerchatId：" + Env.merchatId());
        System.err.println("\tkeyWordsPushDelay：" + Env.keyWordsPushDelay());
        System.err.println("||===================================================||");
        System.err.println("||===================================================||");
    }

}
