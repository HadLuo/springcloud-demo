package com.uc.firegroup.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.uc.firegroup.api.IBilinService;
import com.uc.firegroup.api.IPlayInfoService;
import com.uc.firegroup.api.IPlayPushService;
import com.uc.firegroup.api.enums.PlayTypeEnum;
import com.uc.firegroup.api.pojo.*;
import com.uc.firegroup.api.request.*;
import com.uc.firegroup.api.response.GroupAllByMerchantRequest;
import com.uc.firegroup.api.response.PlayInfoDetailResponse;
import com.uc.firegroup.api.response.TimingPlayPageResponse;
import com.uc.firegroup.api.response.TriggerPlayPageResponse;
import com.uc.firegroup.service.factory.PlayInfoFactory;
import com.uc.firegroup.service.inner.PushTaskService;
import com.uc.firegroup.service.inner.chat.AbstractChatPushProssor;
import com.uc.firegroup.service.inner.chat.JobChatPushProcessor;
import com.uc.firegroup.service.mapper.*;
import com.uc.firegroup.service.strategy.PlayInfoValidateStrategy;
import com.uc.firegroup.service.strategy.PushTaskHandleStrategy;
import com.uc.framework.Constants;
import com.uc.framework.collection.ListTools;
import com.uc.framework.db.PageInfo;
import com.uc.framework.logger.Logs;
import com.uc.framework.logger.alert.AlertContext;
import com.uc.framework.login.UserThreadLocal;
import com.uc.framework.natives.Classes;
import com.uc.framework.obj.Result;
import com.uc.framework.thread.AsyncTask;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Api(tags = "剧本相关接口")
public class PlayInfoServiceImpl implements IPlayInfoService {
    @Value(value = "${identity}")
    private String identity;

    @Autowired
    private PlayMessageMapper playMessageMapper;
    @Autowired
    private GroupInfoMapper groupInfoMapper;
    @Autowired
    private PlayInfoMapper playInfoMapper;
    @Autowired
    private PlayRobotConfigMapper playRobotConfigMapper;
    @Autowired
    PushTaskMapper pushTaskMapper;
    @Resource
    private IPlayPushService playPushService;
    @Autowired
    PushTaskService pushTaskService;
    @Autowired
    PushTaskHandleStrategy handleStrategy;
    @Autowired
    IBilinService bilinService;
    @Autowired
    TaskInfoMapper taskInfoMapper;
    @Autowired
    PlayMessagePushMapper playMessagePushMapper;
    /***
     *
     * title: 解析 得到 具体 的 群 id
     *
     * @param request
     * @return
     * @author HadLuo 2020-9-18 11:06:47
     */
    private List<String> resolveGroupIds(PlayInfoRequest request) {
        List<String> pushGroupIds = Lists.newArrayList();
        if (request.getPushTargetType() == 1) {
            // 按任务推送
            for (String taskId : StringUtils.split(request.getPushTargetId(), ",")) {
                // 查询任务下的群
                List<GroupInfo> groups = groupInfoMapper
                        .selectListByTaskIds(Lists.newArrayList(Integer.parseInt(taskId)));
                // 统计群数量
                if (!CollectionUtils.isEmpty(groups)) {
                	for(GroupInfo group : groups) {
                		pushGroupIds.add(group.getWxGroupId());
                	}
                }
            }
        } else {
            // 指定群聊推送 , 直接 取群id
            pushGroupIds.addAll(Arrays.asList(request.getPushTargetId().split(",")));
        }
        return pushGroupIds;
    }

    @Override
    @Transactional
    public Result<?> create(PlayInfoRequest request) {
        // 校验数据
        Result<?> r = PlayInfoValidateStrategy.validate(request);
        if (!r.successful()) {
            return r;
        }
        // 剧本 要推送的 群 id 集合
        List<String> pushGroupIds = resolveGroupIds(request);
        if (request.getPushTimeType() != 1 && CollectionUtils.isEmpty(pushGroupIds)) {
            return Result.err("请选择需要推送的群");
        }
        // 构造 剧本 bean
        PlayInfo playInfo = PlayInfoFactory.create(request, pushGroupIds.size());
        // 插入剧本信息
        int ret = playInfoMapper.insert(playInfo);
        if (ret <= 0) {
            AlertContext.robot()
                    .alert("t_play_info表插入数据失败,ret=" + ret + ",bo=" + JSON.toJSONString(playInfo));
            return Result.err(Constants.ServerErrorHint);
        }
        int playId = playInfo.getPlayId();
        // 插入发言人设置 表
        List<PlayRobotConfig> configs = request.getPlayRobotConfigs();
        for (PlayRobotConfig config : configs) {
            config.setPlayId(playId);
            config.onInit();
            ret = playRobotConfigMapper.insert(config);
            if (ret <= 0) {
                AlertContext.robot()
                        .alert("t_play_robot_config表插入数据失败,ret=" + ret + ",bo=" + JSON.toJSONString(config));
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();// 手动回滚事务
                return Result.err(Constants.ServerErrorHint);
            }
        }
        // 插入剧本 消息 表
        List<PlayMessage> messages = request.getMessages();
        for (PlayMessage msg : messages) {
            msg.setPlayId(playId);
            msg.onInit();
            ret = playMessageMapper.insert(msg);
            if (ret <= 0) {
                AlertContext.robot()
                        .alert("t_play_message表插入数据失败,ret=" + ret + ",bo=" + JSON.toJSONString(msg));
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();// 手动回滚事务
                return Result.err(Constants.ServerErrorHint);
            }
            if (playInfo.getState() == 1) {
                // 不是草稿状态 , 插入 具体要推送的任务
                playInfo.setPushGroupIds(pushGroupIds);
                pushTaskService.createPushTask(playInfo, msg, configs,
                        UserThreadLocal.get().getMerchatId());
            }
        }
        // 定时触发,事务提交后生成推送信息
        if (playInfo.getPlayType() == PlayTypeEnum.TIMING.getKey() && playInfo.getPushTimeType() == 2) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    AsyncTask.execute(() -> {
                        try {
                            PlayPushCreateRequest playPushCreateRequest = new PlayPushCreateRequest();
                            playPushCreateRequest.setPlayId(playId);
                            Result<Void> result = playPushService.createPush(playPushCreateRequest);
                            if (!result.successful()) {
                                AlertContext.robot()
                                        .alert("剧本调用createPush生成Push信息失败,原因:" + result.getMessage());
                            }
                        } catch (Exception e) {
                            Logs.e(PlayInfoServiceImpl.class, "剧本调用createPush生成Push信息失败,playId=" + playId, e);
                            AlertContext.robot().alert("剧本调用createPush生成Push信息失败,playId=" + playId);
                        }
                    });
                }
            });
        }
        Logs.admin("创建剧本", request, null);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result<?> update(PlayInfoUpdateRequest updateRequest) {
        // 校验数据
        if (updateRequest == null || updateRequest.getPlayId() == null) {
            return Result.err("请求参数错误");
        }
        Result<?> r = PlayInfoValidateStrategy.validate(updateRequest);
        if (!r.successful()) {
            return r;
        }
        Integer playId = updateRequest.getPlayId();
        PlayInfo dbPlayInfo = this.playInfoMapper.selectByPrimaryKey(playId);
        if (dbPlayInfo == null || dbPlayInfo.getPushTimeType() == 2) {
            return Result.err("此剧本不支持修改操作");
        }
        // 剧本 要推送的 群 id 集合
        List<String> pushGroupIds = resolveGroupIds(updateRequest);
        if (updateRequest.getPushTimeType() != 1 && CollectionUtils.isEmpty(pushGroupIds)) {
            return Result.err("请选择需要推送的群");
        }
        // 构造剧本 bean,进行修改操作
        PlayInfo playInfo = PlayInfoFactory.create(updateRequest, pushGroupIds.size());
        this.playInfoMapper.updateByPrimaryKeySelective(playInfo);

        // ------删除旧发言人信息,重新插入
        // 1.删除旧发言人信息
        this.playRobotConfigMapper.deleteByPlayId(playId);
        // 2.重新插入
        List<PlayRobotConfig> configs = updateRequest.getPlayRobotConfigs();
        for (PlayRobotConfig config : configs) {
            config.setPlayId(playId);
            config.onInit();
            playRobotConfigMapper.insert(config);
        }

        // -------删除旧剧本消息，重新插入
        // 1.删除旧发言人信息
        this.playMessageMapper.deleteByPlayId(playId);
        // 2.重新插入
        List<PlayMessage> messages = updateRequest.getMessages();
        for (PlayMessage msg : messages) {
            msg.setPlayId(playId);
            msg.onInit();
            playMessageMapper.insert(msg);
        }
        if (playInfo.getState() == 1) {
            // 待推送状态 , 插入 具体要推送的任务
            playInfo.setPushGroupIds(pushGroupIds);
            pushTaskService.updatePushTask(playInfo, UserThreadLocal.get().getMerchatId());
        }
        // 定时触发,事务提交后生成推送信息
        if (playInfo.getPlayType() == PlayTypeEnum.TIMING.getKey() && playInfo.getPushTimeType() == 2) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    AsyncTask.execute(() -> {
                        try {
                            PlayPushCreateRequest playPushCreateRequest = new PlayPushCreateRequest();
                            playPushCreateRequest.setPlayId(updateRequest.getPlayId());
                            Result<Void> result = playPushService.createPush(playPushCreateRequest);
                            
                            
                            
                            if (!result.successful()) {
                                AlertContext.robot()
                                        .alert("剧本调用createPush生成Push信息失败,原因:" + result.getMessage());
                            }
                        } catch (Exception e) {
                            Logs.e(PlayInfoServiceImpl.class, "剧本调用createPush生成Push信息失败,playId=" + playId, e);
                            AlertContext.robot().alert("剧本调用createPush生成Push信息失败,playId=" + playId);
                        }
                    });
                }
            });
        }
        Logs.admin("修改剧本", updateRequest, null);
        return Result.ok();
    }

    @Override
    public Result<PageInfo<GroupAllByMerchantRequest>> selectGroupInfos(Integer pageIndex, Integer pageSize,String groupName,Integer taskId) {
        // 调用任务那边查询 所有群 信息
        int total = groupInfoMapper.selectCountLikeGroupAllByMerchant(UserThreadLocal.get().getMerchatId(), pageIndex, pageSize,groupName,taskId);
        if(total <= 0) {
            return Result.ok(new PageInfo<>(0, Collections.emptyList()));
        }
        List<GroupAllByMerchantRequest> datas = groupInfoMapper
                .selectLikeGroupAllByMerchant(UserThreadLocal.get().getMerchatId(), pageIndex, pageSize,groupName,taskId);
        return Result.ok(new PageInfo<>(total, datas));
    }

    @Override
    public Result<PageInfo<SelectTaskInfoRequest>> selectTaskInfos(Integer pageIndex, Integer pageSize,String taskName) {
        List<SelectTaskInfoRequest> results = Lists.newArrayList();
        TaskInfoRequest rq = new TaskInfoRequest();
        rq.setTaskName(taskName);
        rq.setCreateId(UserThreadLocal.get().getMerchatId());
        rq.setPageIndex(pageIndex);
        rq.setPageSize(pageSize);
        int count = taskInfoMapper.selectPageListCount(rq) ;
        if(count == 0){
            return Result.ok(new PageInfo<>(0, Collections.EMPTY_LIST));
        }
        List<TaskInfo> tasks = taskInfoMapper.selectPageList(rq);
        // 将 tasks 的 taskId 抽取成另外 一个集合
        List<Integer> ids = ListTools.extract(tasks, (item) -> item.getTaskId());
        // 查询 下面的群
        List<GroupInfo> groups = groupInfoMapper.selectListByTaskIds(ids);
        // 按照 taskId来分组
        Map<Integer, List<GroupInfo>> map = ListTools.group(groups, (item) -> item.getTaskId());
        for (TaskInfo task : tasks) {
            SelectTaskInfoRequest r = new SelectTaskInfoRequest();
            results.add(r);
            r.setTaskId(task.getTaskId());
            r.setTaskName(task.getTaskName());
            // 获取任务下的群
            List<GroupInfo> groupList = map.get(task.getTaskId());
            if (CollectionUtils.isEmpty(groupList)) {
                continue;
            }
            List<GroupAllByMerchantRequest> lamr = Lists.newArrayList();
            r.setGroups(lamr);
            for (GroupInfo g : groupList) {
                GroupAllByMerchantRequest gmr = new GroupAllByMerchantRequest();
                Classes.mergeBean(g, gmr);
                lamr.add(gmr);
            }
        }
        return Result.ok(new PageInfo<SelectTaskInfoRequest>(count, results));
    }

    @Override
    public Result<List<GroupAllByMerchantRequest>> createBeforeValidateGroup(String groupIds,String taskIds,
            Integer personNum) {
        if(StringUtils.isEmpty(groupIds) && StringUtils.isEmpty(taskIds)) {
            return Result.ok(Collections.emptyList());
        }
        List<GroupInfo> groups  = Lists.newArrayList();
        if(!StringUtils.isEmpty(groupIds)) {
            for (String groupId : groupIds.split(",")) {
                GroupInfo info = groupInfoMapper.selectInfoByWxGroupId(groupId);
                if(info != null) {
                    groups.add(info);
                }
            }
        }
        if(!StringUtils.isEmpty(taskIds)) {
            for(String taskId: taskIds.split(",")) {
                groups.addAll(groupInfoMapper.selectGroupListByTaskId(Integer.parseInt(taskId)));
            }
        }
       
        List<GroupAllByMerchantRequest> results = Lists.newArrayList();
        for(GroupInfo info : groups) {
            GroupAllByMerchantRequest r = new GroupAllByMerchantRequest();
            Classes.mergeBean(info, r);
            r.setTaskName(taskInfoMapper.selectByPrimaryKey(r.getTaskId()).getTaskName());
            // 状态：1正在使用 2 暂停服务待续费 3停止服务
            if(info.getState() == 2) {
                r.setNormalRobots(0);
                r.setExceptionRobots(0);
                r.setCause("群待续费,暂停服务");
            }else if(info.getState() == 3) {
                r.setNormalRobots(0);
                r.setExceptionRobots(0);
                r.setCause("群停止服务");
            }else {
                // 查询群内总水军数
                List<String> wxIds = handleStrategy.selectAllRobotsWxids(info.getWxGroupId());
                // 总水军数
                int total = wxIds.size();
                // 离线 异常 水军数
                int exception = 0;
                for (String wxId : wxIds) {
                    if (!bilinService.robotHasEnable(wxId).getLeft()) {
                        // 机器人 离线或者 冻结
                        exception++;
                    }
                }
                // 正常水军数
                r.setNormalRobots(total - exception);
                r.setExceptionRobots(exception);
                if (personNum > r.getNormalRobots()) {
                    r.setCause("缺少" + (personNum - r.getNormalRobots()) + "个水军充当发言人");
                }
                r.setGapRobotNum(Math.abs((personNum - r.getNormalRobots())));
//                if(total < personNum) {
//                    r.setGapRobotNum(Math.abs(personNum - total));
//                }
            }
            if(!StringUtils.isEmpty(r.getCause())) {
                results.add(r);
            }
        }
        return Result.ok(results);
    }

    @Override
    public Result<PageInfo<TimingPlayPageResponse>> findTimingPlayPage(TimingPlayPageRequest pageRequest) {
        if (pageRequest == null || pageRequest.getPage() == null || pageRequest.getRows() == null) {
            return Result.err("请求参数错误");
        }
        //设置商户Id
        pageRequest.setCreateId(UserThreadLocal.get().getMerchatId());
        int totalCount = this.playInfoMapper.selectTimingPlayPageCount(pageRequest);
        if (totalCount == 0) {
            return Result.ok(new PageInfo<>(0, Collections.EMPTY_LIST));
        }
        List<PlayInfo> playInfoList = this.playInfoMapper.selectTimingPlayPage(pageRequest);
        return Result.ok(new PageInfo<>(totalCount, playInfoList.stream().map(playInfo -> {
            TimingPlayPageResponse response = new TimingPlayPageResponse();
            response.setPlayId(playInfo.getPlayId());
            response.setPlayName(playInfo.getPlayName());
            response.setPlayTime(playInfo.getPlayTime());
            response.setRobotNum(playInfo.getRobotNum());
            response.setContentNum(playInfo.getContentNum());
            response.setGroupNum(playInfo.getGroupNum());
            response.setState(playInfo.getState());
            response.setPushTime(playInfo.getPushTime());
            response.setCreateName(playInfo.getCreateName());
            response.setCreateTime(playInfo.getCreateTime());
            return response;
        }).collect(Collectors.toList())));
    }

    @Override
    public Result<PageInfo<TriggerPlayPageResponse>> findTriggerPlayPage(TriggerPlayPageRequest pageRequest) {
        if (pageRequest == null || pageRequest.getPage() == null || pageRequest.getRows() == null) {
            return Result.err("请求参数错误");
        }
        //设置商户Id
        pageRequest.setCreateId(UserThreadLocal.get().getMerchatId());
        int totalCount = this.playInfoMapper.selectTriggerPlayPageCount(pageRequest);
        if (totalCount == 0) {
            return Result.ok(new PageInfo<>(0, Collections.EMPTY_LIST));
        }
        List<PlayInfo> playInfoList = this.playInfoMapper.selectTriggerPlayPage(pageRequest);
        return Result.ok(new PageInfo<>(totalCount, playInfoList.stream().map(playInfo -> {
            TriggerPlayPageResponse response = new TriggerPlayPageResponse();
            response.setPlayId(playInfo.getPlayId());
            response.setPlayName(playInfo.getPlayName());
            response.setPlayTime(playInfo.getPlayTime());
            response.setRobotNum(playInfo.getRobotNum());
            response.setContentNum(playInfo.getContentNum());
            response.setGroupNum(playInfo.getGroupNum());
            // 获取触发信息(时间段)
            PlayInfoRequest.KeyWordsRule keyWordsRule = JSONObject.parseObject(playInfo.getPlayKeywordRule(),
                    PlayInfoRequest.KeyWordsRule.class);
            if (keyWordsRule != null) {
                response.setTriggerStartTime(keyWordsRule.getStartTime());
                response.setTriggerEndTime(keyWordsRule.getEndTime());
            }
            response.setTriggerState(playInfo.getIsStart());
            response.setState(playInfo.getState());
            response.setCreateName(playInfo.getCreateName());
            response.setCreateTime(playInfo.getCreateTime());
            return response;
        }).collect(Collectors.toList())));
    }

    @Override
    public Result<PlayInfoDetailResponse> findPlayDetail(Integer playId) {
        if (playId == null) {
            return Result.err("请求参数为空");
        }
        PlayInfo playInfo = this.playInfoMapper.selectByPrimaryKey(playId);
        if (playInfo == null) {
            return Result.err("找不到对应的剧本信息");
        }
        // ----------包装返回体
        PlayInfoDetailResponse response = new PlayInfoDetailResponse();
        response.setPlayName(playInfo.getPlayName());
        response.setPlayType(playInfo.getPlayType());
        response.setPlayTime(playInfo.getPlayTime());
        response.setContentNum(playInfo.getContentNum());
        response.setPlayKeywordRule(playInfo.getPlayKeywordRule());
        // 查询剧本消息并设置
        List<PlayMessage> playMessageList = this.playMessageMapper.selectListByPlayId(playId);
        response.setPlayMessageList(playMessageList.stream().map(pm -> {
            PlayInfoDetailResponse.PlayMessage result = new PlayInfoDetailResponse.PlayMessage();
            result.setRobotNickname(pm.getRobotNickname());
            result.setIntervalTime(pm.getIntervalTime());
            result.setPlayErrorType(pm.getPlayErrorType());
            result.setMessageContentObj(JSONObject.parseObject(pm.getMessageContent(),
                    PlayInfoDetailResponse.PlayMessage.ContentJson.class));
            result.setCallAll(pm.getCallAll());
            result.setMessageSort(pm.getMessageSort());
            return result;
        }).collect(Collectors.toList()));
        // 查询剧本发言人信息并设置
        List<PlayRobotConfig> playRobotConfigList = this.playRobotConfigMapper.selectListByPlayId(playId);
        response.setPlayRobotConfigList(playRobotConfigList.stream().map(prc -> {
            PlayInfoDetailResponse.PlayRobotConfig result = new PlayInfoDetailResponse.PlayRobotConfig();
            result.setRobotNickname(prc.getRobotNickname());
            result.setRobotConfigType(prc.getRobotConfigType());
            result.setClearWxId(prc.getClearWxId());
            result.setBackupWxId(prc.getBackupWxId());
            return result;
        }).collect(Collectors.toList()));
        response.setPushTime(playInfo.getPushTime());
        response.setPushTargetType(playInfo.getPushTargetType());
        response.setPushTargetId(playInfo.getPushTargetId());
        response.setPushTimeType(playInfo.getPushTimeType());
        return Result.ok(response);
    }

    @Override
    public Result<?> operation(Integer playId, Integer op) {
        PlayInfo info = playInfoMapper.selectByPrimaryKey(playId);
        if (info == null) {
            return Result.err("剧本不存在，id=" + playId);
        }
        if (op == 0) {
            // 启用剧本
            info.setIsStart(1);
        } else if (op == 1) {
            // 禁用
            info.setIsStart(2);
        } else if (op == 2) {
            // 刪除
            info.setIsDelete(1);
        } else if (op == 3) {
            // 取消推送
            info.setState(3);
            // 删除 t_play_message_push 表
            List<PlayMessage> messages = playMessageMapper.selectListByPlayId(playId) ;
            if(!CollectionUtils.isEmpty(messages)) {
                for(PlayMessage play : messages) {
                    play.setIsDelete(1);
                    playMessageMapper.updateByPrimaryKey(play) ;
                }
            }
        }
        playInfoMapper.updateByPrimaryKey(info);
        Logs.admin("剧本的启用，禁用，删除，取消", "playId="+playId+",op="+op, null);
        return Result.ok();
    }

    @Override
    public Result<?> pauseOrStart(Integer playId, String groupWxId, Integer op) {
        PlayInfo info = playInfoMapper.selectByPrimaryKey(playId);
        if (info == null) {
            return Result.err("剧本不存在，id=" + playId);
        }
        PlayMessagePush playMessagePush = playMessagePushMapper.selectOneByIds(playId, groupWxId);
        if(op == 0) {
            // 人工 暂停 群剧本
            AbstractChatPushProssor.getProssor(info).pause(playId, groupWxId);
            if(playMessagePush != null) {
                playMessagePush.setPushState(4);
                playMessagePush.setModifyTime(new Date());
                playMessagePushMapper.updateByPrimaryKey(playMessagePush);
            }
        } else if (op == 1) {
//            String key = "keywords.push.oneday." + playId + "." + groupWxId;
            AbstractChatPushProssor.getProssor(info).onResume(playId, groupWxId);
            
//            if(SysPushOpRedisModel.isPause(groupWxId, playId)) {
//                //系统暂停, 继续
//                SysPushOpRedisModel.resume(groupWxId, playId);
//                if(info.getPlayType() == 2) {
//                    // 删除 关键词 一天一次 风控
//                    App.getBean(RedisLock.class).unlock(key, key);
//                }
//            }
//            if(AdaminPushOpRedisModel.isPause(groupWxId, playId)) {
//                // 人工 继续
//                AdaminPushOpRedisModel.resume(groupWxId, playId);
//                if(info.getPlayType() == 2) {
//                    // 删除 关键词 一天一次 风控
//                    App.getBean(RedisLock.class).unlock(key, key);
//                }
//            }
        	if(playMessagePush != null) {
        		playMessagePush.setPushState(2);
        		playMessagePush.setModifyTime(new Date());
        		playMessagePushMapper.updateByPrimaryKey(playMessagePush);
        	}
        }
        Logs.admin("剧本关联群的暂停，继续", "playId="+playId+",groupWxId="+groupWxId+",op="+op, null);
        return Result.ok();
    }
    
    @Override
    public Result<?> batchPauseOrStart(List<BatchPlayPauseResume> bodys) {
        for(BatchPlayPauseResume r : bodys) {
            pauseOrStart(r.getPlayId(), r.getGroupWxId(), r.getOp());
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result<?> playBatchDel(List<Integer> playIdList) {
        if(CollectionUtils.isEmpty(playIdList)){
            return Result.err("剧本Id集合为空");
        }
        for (Integer playId : playIdList) {
            Result<?> operationResult = this.operation(playId, 2);
            if(!operationResult.isSuccess()){
                return operationResult;
            }
        }
        return Result.ok();
    }
}
