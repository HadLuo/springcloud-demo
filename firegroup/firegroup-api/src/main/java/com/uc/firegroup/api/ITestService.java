package com.uc.firegroup.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uc.framework.login.Login;
import com.uc.framework.obj.Result;

/**
 * 这是一个helloword api接口
 * 
 * @author HadLuo
 * @date 2020-9-3 11:04:43
 */
@FeignClient(name = Constant.instanceName, decode404 = true)
public interface ITestService {

    @GetMapping("/test/test")
    @Login // 此接口 代表 要登录
    Result<?> test(@RequestParam("msg") String msg);

    /**
     * 测试kafka
     */
    @GetMapping("/kafka")
    @Login
    void sendMsg();
    
    @GetMapping("/testRedis")
    void testRedis();
    
    @GetMapping("/testInGroup")
    @Login // 此接口 代表 要登录
    void testInGroup();
    
    // 测试 新增剧本
    @GetMapping("/testInsertPlay")
    void testInsertPlay();
    // 测试 定时推送消息
    @GetMapping("/testPush")
    void testPush();
    
    @GetMapping("/kaiqun")
    void kaiqun();
    
    @GetMapping("/q")
    void q();
    
    @GetMapping("/ack")
    void ack(@RequestParam("id")String id);
}
