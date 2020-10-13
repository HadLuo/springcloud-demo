package com.uc.firegroup.api;

import io.swagger.annotations.Api;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.uc.framework.login.Login;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;

/***
 * 
 * title: 所有的定时器 都放在这里
 *
 * @author HadLuo
 * @date 2020-9-19 15:35:59
 */
@FeignClient(name = Constant.instanceName, decode404 = true)
@Api(tags = "定时任务服务")
public interface IJobService {

    /**
     * 
     * title: 扫描定时推送的剧本 消息 , 定时器一分执行一次
     *
     * @author HadLuo 2020-9-16 13:42:56
     */
    @ApiOperation("扫描定时推送的剧本消息 , 定时器一分执行一次")
    @PostMapping("/job/scanTimePush")
    public void scanTimePush();

    /**
     * 机器人加好友服务
     * @author 鲁志学 2020年9月09日
     * @return
     */
    @PostMapping("/robotAddFriend")
    public void robotAddFriendJob();

    /**
     * 机器人账号信息更新
     * @author 袁哲 2020年9月19号
     * @return
     */
    @PostMapping("/job/robotInfoUpdate")
    void robotInfoUpdate();
    
    
    /**
     * 
     * title: 测试数据
     *
     * @author HadLuo 2020-9-16 13:42:56
     */
    @GetMapping("/job/initData")
    @Login
    public void initData();

    /**
     * 商家退群定时任务
     */
    @PostMapping("/job/robotQuitGroupJob")
    void robotQuitGroupJob();

    /**
     * 用户每日群消费处理
     */
    @PostMapping("/job/custConsumeHandle")
    void custConsumeHandle();

}
