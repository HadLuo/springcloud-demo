package com.uc.firegroup.api;

import com.uc.firegroup.api.request.*;
import com.uc.firegroup.api.response.GroupAllByMerchantRequest;
import com.uc.firegroup.api.response.PlayInfoDetailResponse;
import com.uc.firegroup.api.response.TimingPlayPageResponse;
import com.uc.firegroup.api.response.TriggerPlayPageResponse;
import com.uc.framework.db.PageInfo;
import com.uc.framework.login.Login;
import com.uc.framework.obj.Result;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 
 * title: 剧本 服务
 *
 * @author HadLuo
 * @date 2020-9-14 9:12:52
 */
@FeignClient(name = Constant.instanceName, decode404 = true)
public interface IPlayInfoService {
    /**
     * 
     * title: 创建剧本选择群聊弹框查询群信息
     *
     * @param request
     * @return
     * @author HadLuo 2020-9-15 14:09:09
     */
    @ApiOperation(value = "创建剧本前 选择群聊弹框查询群信息，")
    @GetMapping("/play/selectGroupInfos")
    @Login
    public Result<PageInfo<GroupAllByMerchantRequest>> selectGroupInfos( @ApiParam("当前页") @RequestParam("pageIndex") Integer pageIndex,
            @ApiParam("页大小") @RequestParam("pageSize") Integer pageSize,
            @ApiParam("群名称") @RequestParam("groupName") String groupName,
            @ApiParam("任务id") @RequestParam(value = "taskId",required = false) Integer taskId);

    /**
     * 
     * title: 创建剧本选择任务 弹框查询信息
     *
     * @param request
     * @return
     * @author HadLuo 2020-9-15 14:09:09
     */
    @ApiOperation(value = "创建剧本前 选择任务弹框查询任务信息，")
    @GetMapping("/play/selectTaskInfos")
    @Login
    public Result<PageInfo<SelectTaskInfoRequest>> selectTaskInfos( @ApiParam("当前页") @RequestParam("pageIndex") Integer pageIndex,
            @ApiParam("页大小") @RequestParam("pageSize") Integer pageSize ,
            @ApiParam("任务名称") @RequestParam("taskName") String taskName);

    /**
     * 
     * title: 创建剧本 校验
     *
     * @param request
     * @return
     * @author HadLuo 2020-9-15 14:09:09
     */
    @ApiOperation(value = "创建剧本前校验提示异常群")
    @GetMapping("/play/createBeforeValidateGroup")
    @Login
    public Result<List<GroupAllByMerchantRequest>> createBeforeValidateGroup( @ApiParam("以逗号分隔的群id 字符串") @RequestParam("groupIds") String groupIds,
            @ApiParam("以逗号分隔的任务id 字符串") @RequestParam("taskIds") String taskIds,
            @ApiParam("发言人数量") @RequestParam("personNum") Integer personNum);

    /**
     * 
     * title: 创建剧本
     *
     * @param request
     * @return
     * @author HadLuo 2020-9-15 14:09:09
     */
    @ApiOperation(value = "创建剧本")
    @PostMapping("/play/create")
    @Login
    public Result<?> create(@RequestBody PlayInfoRequest request);

    @ApiOperation(value = "修改剧本")
    @PostMapping("/play/update")
    @Login
    Result<?> update(@RequestBody PlayInfoUpdateRequest updateRequest);

    @Login
    @ApiOperation(value = "定时剧本分页查询")
    @PostMapping("/play/timingPlayPage")
    Result<PageInfo<TimingPlayPageResponse>> findTimingPlayPage(
            @RequestBody TimingPlayPageRequest pageRequest);

    @Login
    @ApiOperation(value = "触发剧本分页查询")
    @PostMapping("/play/triggerPlayPage")
    Result<PageInfo<TriggerPlayPageResponse>> findTriggerPlayPage(
            @RequestBody TriggerPlayPageRequest pageRequest);

    @Login
    @ApiOperation(value = "查看剧本详情")
    @GetMapping("/play/detail")
    Result<PlayInfoDetailResponse> findPlayDetail(Integer playId);
    

    @Login
    @ApiOperation(value = "剧本的启用，禁用，删除，取消")
    @GetMapping("/play/operation")
    Result<?> operation( @ApiParam("剧本id") @RequestParam("playId") Integer playId
            ,@ApiParam("0-启用 1-禁用 2-删除 3-取消") @RequestParam("op") Integer op);
    
    @Login
    @ApiOperation(value = "剧本关联群的暂停，继续")
    @GetMapping("/play/pause")
    Result<?> pauseOrStart( @ApiParam("剧本id") @RequestParam("playId") Integer playId,
            @ApiParam("微信群id") @RequestParam("groupWxId") String groupWxId,
            @ApiParam("0-暂停 1-继续") @RequestParam("op") Integer op);

    @Login
    @ApiOperation(value = "批量剧本关联群的批量 暂停，继续")
    @PostMapping("/play/batchPauseOrStart")
    Result<?> batchPauseOrStart( @RequestBody List<BatchPlayPauseResume> bodys);

    @Login
    @ApiOperation("剧本批量删除")
    @PostMapping("/play/playBatchDel")
    Result<?> playBatchDel(@RequestBody List<Integer> playIdList);
}
