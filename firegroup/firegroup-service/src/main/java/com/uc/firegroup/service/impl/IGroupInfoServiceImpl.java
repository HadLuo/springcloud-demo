package com.uc.firegroup.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.*;
import com.uc.external.bilin.res.BooleanResultVo;
import com.uc.external.bilin.res.GroupBaseInfoVO;
import com.uc.external.bilin.res.PersonalInfoVO;
import com.uc.external.bilin.res.QueryOpenedGroupVO;
import com.uc.firegroup.api.IFireGroupConfigService;
import com.uc.firegroup.api.IGroupInfoService;
import com.uc.firegroup.api.IPlayPushService;
import com.uc.firegroup.api.IPullFriendService;
import com.uc.firegroup.api.enums.GroupStatusEnum;
import com.uc.firegroup.api.enums.PushStateEnum;
import com.uc.firegroup.api.pojo.*;
import com.uc.firegroup.api.request.*;
import com.uc.firegroup.api.response.GroupInfoResponse;
import com.uc.firegroup.api.response.GroupPlayPushCountResponse;
import com.uc.firegroup.api.response.RobotInfoGroupResponse;
import com.uc.firegroup.service.mapper.*;
import com.uc.framework.Times;
import com.uc.framework.db.PageInfo;
import com.uc.framework.logger.Logs;
import com.uc.framework.login.User;
import com.uc.framework.login.UserThreadLocal;
import com.uc.framework.natives.Beans;
import com.uc.framework.obj.Result;
import com.uc.framework.redis.RedisHandler;
import com.uc.framework.redis.queue.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class IGroupInfoServiceImpl implements IGroupInfoService {
    @Autowired
    private GroupInfoMapper groupInfoMapper;
    @Autowired
    private RobotInfoMapper robotInfoMapper;
    @Value(value = "${identity}")
    private String identity;
    @Autowired
    private RobotGroupRelationMapper robotGroupRelationMapper;
    @Autowired
    private FriendRelationMapper friendRelationMapper;
    @Autowired
    private IFireGroupConfigService fireGroupConfigService;
    @Autowired
    private TaskInfoMapper taskInfoMapper;
    @Autowired
    private IPlayPushService playPushService;
    @Autowired
    private PlayMessagePushMapper playMessagePushMapper;
    @Autowired
    private IPullFriendService pullFriendService;
    public final static String Seal_Robot_No_Money= "SealRobotNoMoney";
    public final static String ADD_FRIEND_LIST_REDIS_KEY= "PullFriendListRedisKey";

    @Override
    public Result<PageInfo<GroupInfoResponse>> selectGroupInfoList(GroupInfoRequest request) {
        if (request.getTaskId() ==  null){
            return Result.err(-1,"参数异常！任务ID不能为空！");
        }
        PageInfo<GroupInfoResponse> pageInfo = new PageInfo<>();
        //查询总数
        Integer count = groupInfoMapper.selectPageListCount(request);
        pageInfo.setTotal(count);
        if (count > 0){
            //查询列表
            List<GroupInfo> groupInfos = groupInfoMapper.selectPageList(request);
            List<GroupInfoResponse> responses = new ArrayList<>();
            //封装返回参数
            for (GroupInfo groupInfo:groupInfos){
                GroupInfoResponse  response= Beans.toView(groupInfo,GroupInfoResponse.class);
                //获取群成员信息 调用比邻的接口获取
                QueryBaseGroupDTO baseGroupDTO = new QueryBaseGroupDTO();
                baseGroupDTO.setIdentity(identity);
                baseGroupDTO.setGroupId(groupInfo.getWxGroupId());
                baseGroupDTO.setWxId(groupInfo.getOpenRobotWxId());
                Result<GroupBaseInfoVO> groupResult = Urls.queryGroupMermber(baseGroupDTO);
                Logs.e(getClass(),"打印比邻获取群成员信息接口req"+JSON.toJSONString(baseGroupDTO)+"返回值："+JSON.toJSONString(groupResult));
                if (groupResult.getCode() == 0 && groupResult.getData() != null ){
                    response.setGroupPersonNum(groupResult.getData().getGroupMembers());
                    groupInfo.setWxGroupName(groupResult.getData().getVcGroupName());
                    GroupInfo info = new GroupInfo();
                    info.setGroupId(groupInfo.getGroupId());
                    info.setWxGroupName(groupResult.getData().getVcGroupName());
                    groupInfoMapper.updateByPrimaryKeySelective(info);
                }
                if (response.getOpenRobotWxId() != null){
                    //获取开通号信息
                    RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(response.getOpenRobotWxId());
                    //能查到开通号信息代表是机器人 查不到代表是个人号
                    if (robotInfo != null){
                        response.setOpenRobotNickName(robotInfo.getWxNick());
                        response.setOpenRobotWxAcc(robotInfo.getWxAcc());
                        response.setOpenRobotImg(robotInfo.getHeadImage());
                        response.setOpenRobotWxId(robotInfo.getWxId());
                        response.setOpenRobotType(1);
                    }else {
                        PersonalWxInfoDTO dto = new PersonalWxInfoDTO();
                        dto.setIdentity(identity);
                        dto.setWxIds(Lists.newArrayList(response.getOpenRobotWxId()));
                        Result<List<PersonalInfoVO>> result = Urls.queryPersonalInfo(dto);
                        if (result.getCode() == 0 && result.getData() != null){
                            response.setOpenRobotNickName(result.getData().get(0).getWxNick());
                            response.setOpenRobotWxAcc(result.getData().get(0).getWxAcc());
                            response.setOpenRobotImg(result.getData().get(0).getWxImgUrl());
                            response.setOpenRobotWxId(result.getData().get(0).getWxId());
                            response.setOpenRobotType(2);
                        }
                    }
                }
                //获取水军数量
                Integer robotCount = robotGroupRelationMapper.selectRobotCountByGroupId(groupInfo.getWxGroupId());
                if (groupInfo.getState() != GroupStatusEnum.END.getKey()){
                    response.setNormalRobotNum(robotCount);
                    response.setRobotNum(robotCount);
                }else {
                    response.setNormalRobotNum(0);
                    response.setRobotNum(0);
                }
                GroupInfo info = groupInfoMapper.selectInfoByWxGroupId(response.getWxGroupId());
                if (info != null && info.getRobotNum().intValue() != robotCount){
                    info.setRobotNum(robotCount);
                    groupInfoMapper.updateByPrimaryKeySelective(info);
                }
                //获取剧本信息
                Result<GroupPlayPushCountResponse>  responseResult =  findWxGroupPlayPushCount(groupInfo.getWxGroupId());
                if (responseResult.getCode() == 0 && responseResult.getData() != null){
                    //今日发送剧本数量
                    response.setPlayDayNum(responseResult.getData().getTodayFinishPushCount());
                    //今日总剧本数量
                    response.setPlayDaySum(responseResult.getData().getTodayPushTotalCount());
                    //待执行剧本数量
                    response.setPlayNum(responseResult.getData().getWaitPushTotalCount());
                }else {
                    response.setPlayDayNum(0);
                    response.setPlayDaySum(0);
                    response.setPlayNum(0);
                }
                responses.add(response);
            }
            pageInfo.setDatas(responses);
        }
        return Result.ok(pageInfo);
    }

    @Override
    public Result<Void> addRobotByGroup(OperationRobotRequest request) {
        if (request.getAddGroupNum() < 1){
            return Result.err("购买数量不能小于1");
        }
        Result result = pullFriendService.addFriend(request);
        return result;
    }

    @Override
    public Result<Void> rmRobotByGroup(OperationRobotRequest request) {
        if (request.getRmGroupNum() == null){
            return Result.err("移除数量不能为空！");
        }
        if (request.getRmGroupNum() < 1){
            return Result.err("移除数量不能小于1");
        }
        Logs.e(getClass(),"移除好友参数："+JSON.toJSONString(request));
        for (Integer groupId:request.getGroupIds()){
            if (request.getGroupIds() == null){
                continue;
            }
            //获取群具体信息
            GroupInfo groupInfo = groupInfoMapper.selectByPrimaryKey(groupId);
            //获取群内所有水军的信息
            List<RobotGroupRelation> list = robotGroupRelationMapper.selectRobotByGroupId(groupInfo.getWxGroupId());
            if (list == null) {
                continue;
            }
            Integer count = 0;
            //循环水军信息进行退群操作
            for (RobotGroupRelation robotGroupRelation:list){
                RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(robotGroupRelation.getRobotWxId());
                //判断是否开通号 如果开通号那么换一个
                if (groupInfo.getOpenRobotWxId() != null && groupInfo.getOpenRobotWxId().equals(robotInfo.getWxId())){
                    continue;
                }
                if (robotInfo == null){
                    continue;
                }
                //修改机器人在群内信息
                robotGroupRelation.setState(3);
                robotGroupRelationMapper.updateByPrimaryKeySelective(robotGroupRelation);
                count++;
                //如果大于需要退群的数量 则结束退群
                if (count >= request.getRmGroupNum()){
                    break;
                }
            }
        }
        return Result.ok();
    }

    @Override
    public Result<Void> moveGroupTask(MoveGroupTaskRequest request) {
        if (request.getTaskId()  == null){
            return Result.err(-1,"参数异常！");
        }
        TaskInfo taskInfo = taskInfoMapper.selectByTaskId(request.getTaskId());
        if (taskInfo == null || taskInfo.getIsDelete() == 1){
            return Result.err(-1,"转移的任务ID不存在");
        }
        for (Integer groupId:request.getGroupIds()){
            GroupInfo groupInfo = groupInfoMapper.selectByPrimaryKey(groupId);
            groupInfo.setTaskId(request.getTaskId());
            groupInfoMapper.updateByPrimaryKeySelective(groupInfo);
        }
        return Result.ok();
    }

    @Override
    public Result<Void> stopGroup(List<Integer> groupIds) {
        Logs.e(getClass(),"停止服务参数："+JSON.toJSONString(groupIds));
        for (Integer groupId:groupIds){
            GroupInfo groupInfo = groupInfoMapper.selectByPrimaryKey(groupId);
            if (groupInfo == null || groupInfo.getState() == 3){
                continue;
            }
            groupInfo.setState(GroupStatusEnum.END.getKey());
            groupInfo.setModifyTime(new Date());
            groupInfoMapper.updateByPrimaryKeySelective(groupInfo);
            //获取群内所有水军的信息
            List<RobotGroupRelation> list = robotGroupRelationMapper.selectRobotByGroupId(groupInfo.getWxGroupId());
            if (list == null) {
                continue;
            }
            //循环水军信息进行退群操作
            for (RobotGroupRelation robotGroupRelation:list){
                RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(robotGroupRelation.getRobotWxId());
                if (robotInfo == null){
                    continue;
                }
                //修改机器人在群内信息
                robotGroupRelation.setState(3);
                robotGroupRelationMapper.updateByPrimaryKeySelective(robotGroupRelation);

            }
            TaskInfo taskInfo = taskInfoMapper.selectByTaskId(groupInfo.getTaskId());
            RobotInfo info = robotInfoMapper.selectRobotByWxId(groupInfo.getOpenRobotWxId());
            if (info != null){
                //调用取消开通接口
                MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
                baseGpDTO.setMerchatId(taskInfo.getCreateId());
                baseGpDTO.setVcGroupId(groupInfo.getWxGroupId());
                baseGpDTO.setWxId(groupInfo.getOpenRobotWxId());
                baseGpDTO.setIdentity(identity);
                Result<BooleanResultVo> result = Urls.robotGroupCancel(baseGpDTO);
                Logs.e(getClass(),"群注销！请求参数："+JSON.toJSONString(baseGpDTO)+"返回参数："+JSON.toJSONString(result));
                if (result.getCode() != 0){
                    Logs.e(getClass(),"群注销失败！请求参数："+JSON.toJSONString(baseGpDTO)+"返回参数："+JSON.toJSONString(result));
                }
            }
        }
        return Result.ok();
    }

    @Override
    public Result<Void> groupRenew(List<Integer> groupIds) {
        if (groupIds == null){
            return Result.err(-1,"参数异常！");
        }
        Logs.e(getClass(),"一键续费参数"+JSON.toJSONString(groupIds));
        FireGroupConfig fireGroupConfig = fireGroupConfigService.selectFireGroupConfig();
        for (Integer groupId:groupIds){
            //获取群信息
            GroupInfo groupInfo = groupInfoMapper.selectByPrimaryKey(groupId);
            //获取任务信息
            TaskInfo taskInfo = taskInfoMapper.selectByTaskId(groupInfo.getTaskId());
            //判断状态是否是待续费
            if (groupInfo.getState() != GroupStatusEnum.STOP.getKey()){
                continue;
            }
            //查询当前群组已有多少水军数量
            Integer robotCount  = robotGroupRelationMapper.selectRobotCountByGroupId(groupInfo.getWxGroupId());
            //扣除费用
            Double money = robotCount*fireGroupConfig.getRobotDayMoney().doubleValue();
            if (money.doubleValue() == 0){
                continue;
            }
            //扣款
            DeductMercBalanceInputDTO inputDTO = new DeductMercBalanceInputDTO();
            inputDTO.setAmount(money);
            inputDTO.setBusiType("SBT_002");
            inputDTO.setMerchantId(taskInfo.getCreateId());
            inputDTO.setRemarks("暖群宝群续费");
            Result<?> result1 = Urls.deductMercBalance(inputDTO);
            if (result1.getCode() == 0){
                //扣款成功修改群状态
                groupInfo.setState(GroupStatusEnum.ACTIVE.getKey());
                groupInfoMapper.updateByPrimaryKeySelective(groupInfo);
           }
        }
        return Result.ok();
    }


    @Override
    public Result<PageInfo<RobotInfoGroupResponse>> selectRobotByGroupIdList(RobotInfoGroupRequest request) {
        if (request.getWxGroupId() == null){
            return Result.err("参数异常！");
        }
        PageInfo<RobotInfoGroupResponse> pageInfo = new PageInfo<>();
        GroupInfo groupInfo = groupInfoMapper.selectInfoByWxGroupId(request.getWxGroupId());
        if (groupInfo ==  null){
            return Result.ok(pageInfo);
        }
        request.setCreateTime(groupInfo.getCreateTime());
        //获取群内所有水军的数量
        Integer count = robotGroupRelationMapper.selectRobotAllByGroupIdCount(request);
        pageInfo.setTotal(count);
        if (count == 0){
            return Result.ok(pageInfo);
        }
        //先查询群内所有的水军信息包括已经退群的水军
        List<RobotInfoGroupResponse> responses = robotGroupRelationMapper.selectRobotAllByGroupId(request);

        if (groupInfo.getState() == GroupStatusEnum.END.getKey()){
            return Result.ok(pageInfo);
        }
        for (RobotInfoGroupResponse response:responses){
           response.setGroupId(groupInfo.getGroupId());
        }
        pageInfo.setDatas(responses);
        return Result.ok(pageInfo);
    }

    @Override
    public Result<List<RobotInfoGroupResponse>> selectOpenRobotGroupList(RobotInfoGroupRequest request) {
        if (request.getWxGroupId() == null){
            return Result.err("参数异常！");
        }

        List<RobotInfoGroupResponse> responses = robotGroupRelationMapper.selectRobotByName(request);
        GroupInfo info = groupInfoMapper.selectInfoByWxGroupId(request.getWxGroupId());
        if (info == null){
            return Result.ok();
        }
        if (info.getState() == GroupStatusEnum.END.getKey()){
            return Result.ok();
        }
        TaskInfo taskInfo = this.taskInfoMapper.selectByTaskId(info.getTaskId());
        //获取商家下所有个人号
        MerchatWxDTO  dto = new MerchatWxDTO();
        dto.setIdentity(identity);
        dto.setMerchatId(taskInfo.getCreateId());
        Result<List<String>> listResult = Urls.getMerchantAvailableWx(dto);
        if (listResult.getCode() == 0 || listResult.getData() != null){
            for (String wxId:listResult.getData()){
                //判断是否在群内
                PersonalWxInfoDTO personalWxInfoDTO = new PersonalWxInfoDTO();
                personalWxInfoDTO.setIdentity(identity);
                personalWxInfoDTO.setWxIds(Lists.newArrayList(wxId));
                Result<List<PersonalInfoVO>> vo = Urls.queryPersonalInfo(personalWxInfoDTO);
                if (vo.getCode() == 0 || vo.getData() != null){
                    PersonalInfoVO personalInfoVO = vo.getData().get(0);
                    RobotInfoGroupResponse response = new RobotInfoGroupResponse();
                    response.setRobotNick(personalInfoVO.getWxNick());
                    response.setRobotWxAcc(personalInfoVO.getWxAcc());
                    //个人号
                    response.setOpenRobotType(2);
                    if ("0".equals(personalInfoVO.getIsClose()) && "1".equals(personalInfoVO.getIsOnline())) {
                        response.setLoginState(1);
                    }else {
                        response.setLoginState(2);
                    }
                    response.setHeadImage(personalInfoVO.getWxImgUrl());
                }
            }
        }
        for (RobotInfoGroupResponse response:responses){
            if (info.getOpenRobotWxId() == null){
                response.setIsOpenGroup(0);
                continue;
            }
            //获取群组开通号微信ID 判断是否开通号
            if (info.getOpenRobotWxId().equals(response.getRobotWxId())){
                response.setIsOpenGroup(1);
            }else {
                response.setIsOpenGroup(0);
            }
            if (response.getOpenRobotType() == null || response.getOpenRobotType() != 2){
                response.setOpenRobotType(1);
            }
            response.setGroupId(info.getGroupId());
        }
        return Result.ok(responses);
    }

    @Override
    public Result<Void> setOpenGroupRobot(OpenGroupRequest request) {
        if (request.getWxGroupId() == null){
            return Result.err("参数错误");
        }
        if (request.getRobotWxId() == null){
            return Result.err("参数错误");
        }
        String wxGroupId = request.getWxGroupId();
        String robotWxId = request.getRobotWxId();
        GroupInfo groupInfo = groupInfoMapper.selectInfoByWxGroupId(wxGroupId);
        if (groupInfo == null){
            return Result.err("当前群组异常！无法切换");
        }
        TaskInfo taskInfo = taskInfoMapper.selectByTaskId(groupInfo.getTaskId());
        //获取开通号
        //判断是否已经开群
        QueryOpenedGroupDTO openedGroupDTO = new QueryOpenedGroupDTO();
        openedGroupDTO.setIdentity(identity);
        openedGroupDTO.setMerchatId(taskInfo.getCreateId());
        openedGroupDTO.setParams(Lists.newArrayList(groupInfo.getWxGroupId()));
        Result<List<QueryOpenedGroupVO>> queryOpenedGroupVOResult = Urls.queryOpenedGroup(openedGroupDTO);
        Logs.e(getClass(),"打印查询开通信息日志：req:"+JSON.toJSONString(openedGroupDTO)+"res:"+JSON.toJSONString(queryOpenedGroupVOResult));
        if (queryOpenedGroupVOResult.getCode() == 0 && queryOpenedGroupVOResult.getData() != null && queryOpenedGroupVOResult.getData().size() != 0){
            String openRobotWxId = queryOpenedGroupVOResult.getData().get(0).getWxId();
            RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(openRobotWxId);
            if (robotInfo == null){
                return Result.err("无法切换开通号！");
            }
            //已经开通需要先调用群关闭接口
            MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
            baseGpDTO.setMerchatId(robotInfo.getMerchantId());
            baseGpDTO.setVcGroupId(wxGroupId);
            baseGpDTO.setWxId(groupInfo.getOpenRobotWxId());
            baseGpDTO.setIdentity(identity);
            Result<BooleanResultVo> result = Urls.robotGroupCancel(baseGpDTO);
            if (result.getCode() != 0){
                return Result.err("原开群账号关群失败"+result.getMessage());
            }
        }
        //用新账号进行开群操作
        //先用微信号查询是否是商户个人账号
        RobotInfo info = robotInfoMapper.selectRobotByWxId(robotWxId);
        MerchantBaseGpDTO dto = new MerchantBaseGpDTO();
        //个人账号
        if (info == null){
            User user = UserThreadLocal.get();
            dto.setMerchatId(user.getMerchatId());
        }else {
            dto.setMerchatId(info.getMerchantId());
        }
        dto.setIdentity(identity);
        dto.setWxId(robotWxId);
        dto.setVcGroupId(wxGroupId);
        Result<BooleanResultVo> booleanResultVoResult = Urls.robotGroupOpen(dto);
        //如果开群失败 则抛出异常 还原原来账号的开群信息
        if (booleanResultVoResult.getCode() != 0 && groupInfo.getOpenRobotWxId() != null){
            RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(groupInfo.getOpenRobotWxId());
            MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
            baseGpDTO.setMerchatId(robotInfo.getMerchantId());
            baseGpDTO.setVcGroupId(wxGroupId);
            baseGpDTO.setWxId(groupInfo.getOpenRobotWxId());
            baseGpDTO.setIdentity(identity);
            Result<BooleanResultVo> result = Urls.robotGroupOpen(baseGpDTO);
            return Result.err("新账号开群失败"+result.getMessage());
        }
        //开群成功替换群信息的开群账号
        groupInfo.setOpenRobotWxId(robotWxId);
        groupInfoMapper.updateByPrimaryKeySelective(groupInfo);
        return  Result.ok();
    }

    @Override
    public Result<Void> sealRobot(List<String> wxRobotIds) {
        Logs.e(getClass(),"打印需要执行封号逻辑的微信ID"+JSON.toJSONString(wxRobotIds));
        Map<String,Integer> map = new HashMap<>();
        for (String wxRobotId:wxRobotIds){
            //查询水军微信号对应的群信息
            List<RobotGroupRelation> robotGroupRelations = robotGroupRelationMapper.selectGroupByRobotId(wxRobotId);
            if (robotGroupRelations == null || robotGroupRelations.size() == 0) {
                continue;
            }
            for (RobotGroupRelation robotGroupRelation : robotGroupRelations) {
                GroupInfo groupInfo = groupInfoMapper.selectInfoByWxGroupId(robotGroupRelation.getWxGroupId());
                if (groupInfo == null){
                    continue;
                }
                //先将水军在群状态改成已退群
                robotGroupRelationMapper.updateStateByRobotId(robotGroupRelation.getRobotWxId());
                RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(robotGroupRelation.getRobotWxId());
                //水军在群数量减少
                robotInfoMapper.updateGroupNum(-1,robotInfo.getRobotId());
                //已购水军数量减少
                groupInfoMapper.updateRobotNum(robotGroupRelation.getWxGroupId(),-1);
                Integer count = map.get(groupInfo.getWxGroupId());
                if (count == null){
                   count = 0;
                }
                map.put(groupInfo.getWxGroupId(),count+1);
            }
        }
        Logs.e(getClass(),"打印封号需要补的群信息:"+JSON.toJSONString(map));
        for(String key : map.keySet()){
            GroupInfo groupInfo = groupInfoMapper.selectInfoByWxGroupId(key);
            if (groupInfo == null){
                continue;
            }
            //调用邀请好友
            OperationRobotRequest robotRequest = new OperationRobotRequest();
            Integer count = map.get(key);
            //存入不要扣钱的标识
            RedisHandler.set(Seal_Robot_No_Money+key,count);
            robotRequest.setAddGroupNum(count);
            robotRequest.setGroupIds(Lists.newArrayList(groupInfo.getGroupId()));
            addRobotByGroup(robotRequest);
        }
        return Result.ok();
    }

    public Result<List<GroupInfo>> findListByTaskIds(List<String> wxGroupIds) {
        if(CollectionUtils.isEmpty(wxGroupIds)){
            return Result.err("请求微信群唯一标识集合为空");
        }
        return Result.ok(this.groupInfoMapper.selectListByWxGroupIds(wxGroupIds));
    }

    @Override
    public Result<Void> insertRobots(List<InsertRobotRequest> request) {
        if (request == null){
            return Result.err(-1,"参数异常！");
        }
        for (InsertRobotRequest robotRequest:request){
            OperationRobotRequest operationRobotRequest = new OperationRobotRequest();
            GroupInfo groupInfo = groupInfoMapper.selectInfoByWxGroupId(robotRequest.getWxGroupId());
            if (groupInfo == null){
                continue;
            }
            operationRobotRequest.setGroupIds(Lists.newArrayList(groupInfo.getGroupId()));
            operationRobotRequest.setAddGroupNum(robotRequest.getAddRobotNum());
            addRobotByGroup(operationRobotRequest);
        }

        return Result.ok();
    }

    private Result<GroupPlayPushCountResponse> findWxGroupPlayPushCount(String wxGroupId) {
        if(StringUtils.isBlank(wxGroupId)){
            return Result.err("群唯一标识为空");
        }
        Date nowDate = new Date();//当前时间
        List<PlayMessagePush> playMessagePushList = this.playMessagePushMapper.findListByWxGroupIdAndPushDate(wxGroupId, Times.getFirstDate(nowDate),Times.getLastDate(nowDate));

        int todayFinishPushCount = 0;
        int todayPushTotalCount = 0;
        for (PlayMessagePush playMessagePush : playMessagePushList) {
            todayPushTotalCount++;
            if(playMessagePush.getPushState() == PushStateEnum.WAIT_SEND.getKey())
                todayFinishPushCount++;
        }
        //查询某个群待推送的剧本数
        int waitPushPlayCount = this.playMessagePushMapper.findWaitPushCountByWxGroupId(wxGroupId);

        return Result.ok(new GroupPlayPushCountResponse(todayFinishPushCount,todayPushTotalCount,waitPushPlayCount));
    }
}
