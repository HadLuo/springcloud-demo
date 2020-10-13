package com.uc.firegroup.api;

import com.uc.firegroup.api.request.PlayPushCreateRequest;
import com.uc.firegroup.api.request.PlayPushGroupRecordRequest;
import com.uc.firegroup.api.request.TimingPushRecordPageRequest;
import com.uc.firegroup.api.request.TriggerPushRecordPageRequest;
import com.uc.firegroup.api.response.*;
import com.uc.framework.db.PageInfo;
import com.uc.framework.login.Login;
import com.uc.framework.obj.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 剧本推送信息服务
 * @author: Ryan.yuan
 * @time: 2020/9/16/016 15:59
 */
@FeignClient(name = Constant.instanceName, decode404 = true)
@Api(tags = "剧本推送记录接口")
public interface IPlayPushService {


    /**
     * 创建推送
     * @param createRequest
     * @return
     */
    @PostMapping("/playPush/createPush")
    //@ApiOperation(value = "创建推送任务")
    @ApiIgnore
    Result<Void> createPush(@RequestBody PlayPushCreateRequest createRequest);


    /**
     * 查询定时推送记录
     * @param pageRequest
     * @return
     */
    @ApiOperation(value = "定时推送记录查询")
    @PostMapping("/playPush/timingRecord")
    @Login
    Result<PageInfo<PlayTimingPushRecordResponse>> findTimingPushRecordPage(@RequestBody TimingPushRecordPageRequest pageRequest);

    /**
     * 查询触发推送记录
     * @param pageRequest
     * @return
     */
    @ApiOperation(value = "触发推送记录查询")
    @PostMapping("/playPush/triggerRecord")
    @Login
    Result<PageInfo<PlayTriggerPushRecordResponse>> findTriggerPushRecordPage(@RequestBody TriggerPushRecordPageRequest pageRequest);

    /**
     * 查询群推送记录
     * @param request
     * @return
     */
    @ApiOperation(value = "群推送记录查询")
    @PostMapping("/playPush/groupPlayPushRecordPage")
    @Login
    Result<PageInfo<PlayPushGroupRecordResponse>> findGroupPlayPushRecordPage(@RequestBody PlayPushGroupRecordRequest request);


    /**
     * 通过推送Id查询推送任务消息信息
     * @param pushId
     * @return
     */
    @ApiOperation(value = "通过推送Id查询推送任务消息信息")
    @GetMapping("/playPush/pushMessageDetail")
    @Login
    Result<List<PlayPushMessageDetailResponse>> findPushMessageDetail(Integer pushId);

    /**
     * 通过推送Id查询推送任务机器人分配信息
     * @param pushId
     * @return
     */
    @ApiOperation(value = "通过推送Id查询推送任务机器人分配信息")
    @GetMapping("/playPush/pushRobotAllocatedDetail")
    @Login
    Result<List<PlayPushRobotAllocatedResponse>> findPushRobotAllocatedDetail(Integer pushId);

}
