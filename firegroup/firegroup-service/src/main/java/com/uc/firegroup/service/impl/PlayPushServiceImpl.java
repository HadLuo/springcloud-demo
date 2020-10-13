package com.uc.firegroup.service.impl;

import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.PersonalWxInfoDTO;
import com.uc.external.bilin.res.PersonalInfoVO;
import com.uc.firegroup.api.IPlayPushService;
import com.uc.firegroup.api.enums.PlayTypeEnum;
import com.uc.firegroup.api.enums.PushStateEnum;
import com.uc.firegroup.api.enums.PushTargetTypeEnum;
import com.uc.firegroup.api.enums.SendStateEnum;
import com.uc.firegroup.api.pojo.*;
import com.uc.firegroup.api.request.PlayPushCreateRequest;
import com.uc.firegroup.api.request.PlayPushGroupRecordRequest;
import com.uc.firegroup.api.request.TimingPushRecordPageRequest;
import com.uc.firegroup.api.request.TriggerPushRecordPageRequest;
import com.uc.firegroup.api.response.*;
import com.uc.firegroup.service.mapper.*;
import com.uc.framework.db.PageInfo;
import com.uc.framework.logger.alert.AlertContext;
import com.uc.framework.login.UserThreadLocal;
import com.uc.framework.obj.BusinessException;
import com.uc.framework.obj.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Ryan.yuan
 * @time: 2020/9/16 17:31
 */
@RestController
public class PlayPushServiceImpl implements IPlayPushService {

    @Resource
    private PlayInfoMapper playInfoMapper;
    @Resource
    private GroupInfoMapper groupInfoMapper;
    @Resource
    private TaskInfoMapper taskInfoMapper;
    @Resource
    private PlayMessagePushMapper playMessagePushMapper;
    @Resource
    private PlayMessagePushDetailMapper playMessagePushDetailMapper;
    @Resource
    private PlayMessageMapper playMessageMapper;
    @Value(value = "${identity}")
    private String identity;

    @Override
    @Transactional
    public Result<Void> createPush(PlayPushCreateRequest createRequest) {
        //参数校验
        if (createRequest == null) {
            return Result.err("请求参数为空");
        }
        //查询剧本信息
        PlayInfo playInfo = playInfoMapper.selectByPrimaryKey(createRequest.getPlayId());
        if (playInfo == null) {
            return Result.err("没有找到对应的剧本信息");
        }
        try {
            if (PlayTypeEnum.TRIGGER.getKey().equals(playInfo.getPlayType())) {//关键词触发
                if (StringUtils.isBlank(createRequest.getTriggerKeyword())) {
                    return Result.err("触发关键词未传入");
                }
                if (createRequest.getWxGroupId() == null) {
                    return Result.err("触发群Id未传入");
                }
                //关键词触发创建
                triggerPushCreate(createRequest, playInfo);
            } else if (PlayTypeEnum.TIMING.getKey().equals(playInfo.getPlayType())) { //定时触发
                //定时触发创建
                timingPushCreate(createRequest, playInfo);
            }
        } catch (BusinessException bex) {
            AlertContext.robot()
                    .alert(bex.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//手动回滚事务
            return Result.err("程序内部错误");
        }
        return Result.ok();
    }

    //触发推送
    private void triggerPushCreate(PlayPushCreateRequest request, PlayInfo playInfo) {
        //推送头信息
        PlayMessagePush playMessagePush = new PlayMessagePush();
        playMessagePush.setPlayId(playInfo.getPlayId());
        playMessagePush.setPlayName(playInfo.getPlayName());

        //查询群信息
        GroupInfo groupInfo = groupInfoMapper.selectByWxGroupId(request.getWxGroupId());
        if (groupInfo == null) {
            throw new BusinessException("无对应的群信息,groupId:" + request.getWxGroupId());
        }
        playMessagePush.setWxGroupId(groupInfo.getWxGroupId());
        playMessagePush.setWxGroupName(groupInfo.getWxGroupName());

        //任务推送,找出触发群的任务信息
        TaskInfo taskInfo = taskInfoMapper.selectByTaskId(groupInfo.getTaskId());
        if (taskInfo == null) {
            throw new BusinessException("找不到群对应任务信息,groupId:" + request.getWxGroupId());
        }
        playMessagePush.setTaskId(taskInfo.getTaskId());
        playMessagePush.setTaskName(taskInfo.getTaskName());

        //其他属性设置
        playMessagePush.setMerchatId(playInfo.getCreateId());
        playMessagePush.setTriggerKeyword(request.getTriggerKeyword());
        playMessagePush.setRobotNum(playInfo.getRobotNum());
        playMessagePush.setContentNum(playInfo.getContentNum());
        playMessagePush.setPushState(PushStateEnum.ING.getKey());
        playMessagePush.setPlayType(PlayTypeEnum.TRIGGER.getKey());
        playMessagePush.setPushTime(new Date());
        //推送任务插入
        playMessagePushMapper.insertSelective(playMessagePush);
        //插入推送详情信息
        insertPushDetail(playInfo.getPlayId(), Arrays.asList(playMessagePush.getId()));
    }

    //定时推送
    public void timingPushCreate(PlayPushCreateRequest request, PlayInfo playInfo) {
        //-----获取剧本配置的任务旗下群信息和指定群信息
        List<GroupInfo> groupInfoList = null;
        if (playInfo.getPushTargetType() == PushTargetTypeEnum.TASK.getKey()) {
            String[] taskIdArr = StringUtils.split(playInfo.getPushTargetId(),',');
            //查询任务下群信息
            groupInfoList = this.groupInfoMapper.selectListByTaskIds(Arrays.stream(taskIdArr).map(Integer::parseInt).collect(Collectors.toList()));
        } else {
            String[] groupIdArr = StringUtils.split(playInfo.getPushTargetId(),',');
            groupInfoList = this.groupInfoMapper.selectListByGroupIds(Arrays.asList(groupIdArr));
        }
        if(CollectionUtils.isEmpty(groupInfoList)){
            throw new BusinessException("无对应的群信息,playId:" + playInfo.getPlayId());
        }

        //需要推送的群集合构造
        List<PlayMessagePush> playMessagePushList = new ArrayList<>();
        Map<Integer, TaskInfo> taskInfoMap = new HashMap<>(); //任务信息缓存,减少Db查询次数
        for (GroupInfo groupInfo : groupInfoList) {
            PlayMessagePush playMessagePush = new PlayMessagePush();
            playMessagePush.setPlayId(playInfo.getPlayId());
            playMessagePush.setPlayName(playInfo.getPlayName());
            playMessagePush.setWxGroupId(groupInfo.getWxGroupId());
            playMessagePush.setWxGroupName(groupInfo.getWxGroupName());
            //任务信息获取
            TaskInfo taskInfo = taskInfoMap.get(groupInfo.getTaskId());
            if (taskInfo == null) {
                if ((taskInfo = this.taskInfoMapper.selectByTaskId(groupInfo.getTaskId())) == null) {
                    throw new BusinessException("没有找到群对应的任务信息,groupId:" + groupInfo.getGroupId());
                } else {
                    taskInfoMap.put(groupInfo.getTaskId(), taskInfo);
                }
            }
            playMessagePush.setMerchatId(playInfo.getCreateId());
            playMessagePush.setTaskId(taskInfo.getTaskId());
            playMessagePush.setTaskName(taskInfo.getTaskName());
            playMessagePush.setRobotNum(playInfo.getRobotNum());
            playMessagePush.setContentNum(playInfo.getContentNum());
            playMessagePush.setPushState(PushStateEnum.WAIT_SEND.getKey());
            playMessagePush.setPushTime(playInfo.getPushTime());
            playMessagePush.setPlayType(PlayTypeEnum.TIMING.getKey());
            playMessagePushList.add(playMessagePush);
        }
        //推送任务插入
        playMessagePushMapper.batchInsert(playMessagePushList);
        //插入推送详情信息
        insertPushDetail(playInfo.getPlayId(), playMessagePushList.stream().map(e -> e.getId()).collect(Collectors.toList()));
    }

    //插入推送详情信息
    private void insertPushDetail(Integer playId, List<Integer> pushId) {
        //查询剧本对应的消息
        List<PlayMessage> playMessageList = playMessageMapper.selectListByPlayId(playId);
        if (CollectionUtils.isEmpty(playMessageList)) {
            throw new BusinessException("找不到对应的剧本消息,playId:" + playId);
        }
        List<PlayMessagePushDetail> pushDetailList = new ArrayList<>();
        for (Integer pId : pushId) {
            for (PlayMessage playMessage : playMessageList) {
                PlayMessagePushDetail playMessagePushDetail = new PlayMessagePushDetail();
                playMessagePushDetail.setPlayMsgPushId(pId);
                playMessagePushDetail.setRobotNickname(playMessage.getRobotNickname());
                playMessagePushDetail.setCallAll(playMessage.getCallAll());
                playMessagePushDetail.setIntervalTime(playMessage.getIntervalTime());
                playMessagePushDetail.setPlayErrorType(playMessage.getPlayErrorType());
                playMessagePushDetail.setMessageContent(playMessage.getMessageContent());
                playMessagePushDetail.setMessageSort(playMessage.getMessageSort());
                playMessagePushDetail.setSendState(SendStateEnum.WAIT_SEND.getKey());
                pushDetailList.add(playMessagePushDetail);
            }
        }
        playMessagePushDetailMapper.batchInsert(pushDetailList);
    }

    @Override
    public Result<PageInfo<PlayTimingPushRecordResponse>> findTimingPushRecordPage(TimingPushRecordPageRequest pageRequest) {
        if (pageRequest == null || pageRequest.getPage() == null || pageRequest.getRows() == null) {
            return Result.err("请求参数错误");
        }
        if(pageRequest.getPlayId() == null){
            return Result.err("剧本Id为空");
        }

        Integer totalCount = this.playMessagePushMapper.findTimingPushPageCount(pageRequest);
        if(totalCount == 0){
            return Result.ok(new PageInfo<>(0,Collections.EMPTY_LIST));
        }

        List<PlayMessagePush> playMessagePushList = this.playMessagePushMapper.findTimingPushPageList(pageRequest);
        return Result.ok(new PageInfo<>(totalCount,playMessagePushList.stream().map(p -> {
            PlayTimingPushRecordResponse response = new PlayTimingPushRecordResponse();
            response.setWxGroupId(p.getWxGroupId());
            response.setGroupName(p.getWxGroupName());
            response.setTaskName(p.getTaskName());
            response.setPushNum(this.playMessagePushDetailMapper.selectFinishCountByPushId(p.getId()));
            response.setPushTime(p.getPushTime());
            response.setPushState(p.getPushState());
            response.setPushId(p.getId());
            response.setPushFailReason(p.getPushFailReason());
            return response;
        }).collect(Collectors.toList())));
    }

    @Override
    public Result<PageInfo<PlayTriggerPushRecordResponse>> findTriggerPushRecordPage(TriggerPushRecordPageRequest pageRequest) {
        if (pageRequest == null || pageRequest.getPage() == null || pageRequest.getRows() == null) {
            return Result.err("请求参数错误");
        }

        //设置商户Id
        pageRequest.setCreateId(UserThreadLocal.get().getMerchatId());
        Integer totalCount = this.playMessagePushMapper.findTriggerPushPageCount(pageRequest);
        if(totalCount == 0){
            return Result.ok(new PageInfo<>(0,Collections.EMPTY_LIST));
        }

        List<PlayMessagePush> playMessagePushList = this.playMessagePushMapper.findTriggerPushPageList(pageRequest);
        return Result.ok(new PageInfo<>(totalCount,playMessagePushList.stream().map(p -> {
            PlayTriggerPushRecordResponse response = new PlayTriggerPushRecordResponse();
            response.setPushTime(p.getPushTime());
            response.setTriggerKeyword(p.getTriggerKeyword());
            response.setPlayId(p.getPlayId());
            response.setPlayName(p.getPlayName());
            response.setRobotNum(p.getRobotNum());
            response.setContentNum(p.getContentNum());
            response.setWxGroupId(p.getWxGroupId());
            response.setGroupName(p.getWxGroupName());
            response.setTaskName(p.getTaskName());
            response.setPushId(p.getId());
            response.setPushNum(this.playMessagePushDetailMapper.selectFinishCountByPushId(p.getId()));
            response.setPushState(p.getPushState());
            response.setPushFailReason(p.getPushFailReason());
            return response;
        }).collect(Collectors.toList())));
    }

    @Override
    public Result<PageInfo<PlayPushGroupRecordResponse>> findGroupPlayPushRecordPage(PlayPushGroupRecordRequest pageRequest) {
        if (pageRequest == null || pageRequest.getPage() == null || pageRequest.getRows() == null) {
            return Result.err("请求参数错误");
        }

        Integer totalCount = this.playMessagePushMapper.findGroupPlayPushPageCount(pageRequest);
        if(totalCount == 0){
            return Result.ok(new PageInfo<>(0,Collections.EMPTY_LIST));
        }
        List<PlayMessagePush> playMessagePushList = this.playMessagePushMapper.findGroupPlayPushPageList(pageRequest);
        return Result.ok(new PageInfo<>(totalCount,playMessagePushList.stream().map(p -> {
            PlayPushGroupRecordResponse response = new PlayPushGroupRecordResponse();
            response.setPushTime(p.getPushTime());
            response.setPlayName(p.getPlayName());
            response.setPlayId(p.getPlayId());
            response.setPushId(p.getId());
            response.setWxGroupId(pageRequest.getWxGroupId());
            response.setPushState(p.getPushState());
            return response;
        }).collect(Collectors.toList())));
    }

    @Override
    public Result<List<PlayPushMessageDetailResponse>> findPushMessageDetail(Integer pushId) {
        if (pushId == null) {
            return Result.err("推送id为空");
        }
        List<PlayMessagePushDetail> pushDetailList = this.playMessagePushDetailMapper.selectListByPushId(pushId);
        Collections.sort(pushDetailList, Comparator.comparing(PlayMessagePushDetail::getMessageSort));
        return Result.ok(pushDetailList.stream().map(p -> {
            PlayPushMessageDetailResponse response = new PlayPushMessageDetailResponse();
            response.setRobotNickname(p.getRobotNickname());
            response.setIntervalTime(p.getIntervalTime());
            response.setPlayErrorType(p.getPlayErrorType());
            response.setMessageSort(p.getMessageSort());
            response.setMessageContent(p.getMessageContent());
            response.setCallAll(p.getCallAll());
            response.setSendState(p.getSendState());
            return response;
        }).collect(Collectors.toList()));
    }

    @Override
    public Result<List<PlayPushRobotAllocatedResponse>> findPushRobotAllocatedDetail(Integer pushId) {
        if (pushId == null) {
            return Result.err("推送id为空");
        }
        List<PlayMessagePushDetail> pushDetailList = this.playMessagePushDetailMapper.selectListByPushId(pushId);

        //查询各个微信号状态
        Map<String, Boolean> wxListOnlineMap = getWxListOnlineMap(pushDetailList.stream().map(p -> p.getWxId()).collect(Collectors.toList()));

        HashSet<String> robotNicknameSet = new HashSet<>();//重复发言人过滤
        return Result.ok(pushDetailList.stream().filter(p ->
                p.getSendState() != SendStateEnum.WAIT_SEND.getKey() && robotNicknameSet.add(p.getRobotNickname())
        ).map(playMessagePushDetail -> {
            PlayPushRobotAllocatedResponse response = new PlayPushRobotAllocatedResponse();
            response.setRobotNickname(playMessagePushDetail.getRobotNickname());
            response.setWxNickname(playMessagePushDetail.getWxNickname());
            response.setWxImgUrl(playMessagePushDetail.getWxImgUrl());
            response.setWxAcc(playMessagePushDetail.getWxAcc());
            response.setAccSource(playMessagePushDetail.getAccSource());
            response.setWxState(wxListOnlineMap.getOrDefault(playMessagePushDetail.getWxId(), false) ? 1 : 0);
            return response;
        }).collect(Collectors.toList()));
    }


    //通过微信Ids 获取在线标识Map
    private Map<String,Boolean> getWxListOnlineMap(List<String> wxIds){
        PersonalWxInfoDTO queryDto = new PersonalWxInfoDTO();
        queryDto.setWxIds(wxIds);
        queryDto.setIdentity(identity);
        Result<List<PersonalInfoVO>> result = Urls.queryPersonalInfo(queryDto);
        if(result.isSuccess()){
            return result.getData().stream().collect(Collectors.toMap(PersonalInfoVO::getWxId,p->StringUtils.equals(p.getIsOnline(),"1")));
        }
        return Collections.EMPTY_MAP;
    }
}
