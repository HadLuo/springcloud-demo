package com.uc.firegroup.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.*;
import com.uc.external.bilin.res.BooleanResultVo;
import com.uc.external.bilin.res.GroupMermberVO;
import com.uc.external.bilin.res.ResultBody;
import com.uc.firegroup.api.Constant;
import com.uc.firegroup.api.IBilinService;
import com.uc.firegroup.api.IFireGroupConfigService;
import com.uc.firegroup.api.IPullFriendService;
import com.uc.firegroup.api.pojo.*;
import com.uc.firegroup.api.request.OperationRobotRequest;
import com.uc.firegroup.api.request.SendWxMsgRequest;
import com.uc.firegroup.api.response.IncomeGroupCallBackResponse;
import com.uc.firegroup.service.mapper.*;
import com.uc.framework.logger.Logs;
import com.uc.framework.obj.Result;
import com.uc.framework.redis.RedisHandler;
import com.uc.framework.web.Rpc;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class IPullFriendServiceImpl implements IPullFriendService {
    @Autowired
    private RobotInfoMapper robotInfoMapper;
    @Autowired
    private FriendRelationMapper friendRelationMapper;
    @Autowired
    private IFireGroupConfigService fireGroupConfigService;
    @Autowired
    RobotGroupRelationMapper robotGroupRelationMapper;
    @Autowired
    GroupInfoMapper groupInfoMapper;
    @Autowired
    TaskUserMapper taskUserMapper;
    @Autowired
    TaskInfoMapper taskInfoMapper;
    @Autowired
    IBilinService bilinService;
    @Value(value = "${identity}")
    private String identity;
    public final static String Seal_Robot_No_Money= "SealRobotNoMoney";
    @Autowired
    private MqMessageInfoMapper mqMessageInfoMapper;

    @Override
    public void pullFriend(IncomeGroupCallBackResponse response) {
        Logs.e(getClass(),"4505回调进入------------------------------------"+JSON.toJSONString(response));
        //被加人微信ID
        String fromWxId =response.getWxId();
        //主加人微信ID
        String toWxId =  response.getVcFriendSerialNo();
        //判断被加人是否是机器人
        RobotInfo fromInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
        Logs.e(getClass(),"fromInfo------------------------------------"+JSON.toJSONString(fromInfo));
        if (fromInfo == null){
            return;
        }
        //获取配置信息
        FireGroupConfig fireGroupConfig = this.fireGroupConfigService.selectFireGroupConfig();
        //根据群ID获取到所属的任务号
        GroupInfo groupInfo = groupInfoMapper.selectInfoByWxGroupId(response.getVcChatRoomSerialNo());
        Logs.e(getClass(),"groupInfo------------------------------------"+JSON.toJSONString(groupInfo));
        if (groupInfo == null){
            Logs.e(getClass(), "邀请群异常！无法根据群ID找到群信息" + JSON.toJSONString(response));
            return;
        }
        //根据任务号找到绑定的用户微信号
        TaskUser taskUser = taskUserMapper.selectUserByTaskId(groupInfo.getTaskId());
        TaskInfo taskInfo = taskInfoMapper.selectByTaskId(groupInfo.getTaskId());
        Logs.e(getClass(),"taskUser------------------------------------"+JSON.toJSONString(taskUser));
        //判断是否已经入群
        int num = robotGroupRelationMapper.selectCountByWxIdGroupId(fromWxId,response.getVcChatRoomSerialNo());
        Logs.e(getClass(),"num------------------------------------"+num);
        if (num >0){
            Logs.e(getClass(), "该机器人已经入群" + JSON.toJSONString(response));
            return;
        }
        //查询当前群组已有多少水军数量
        Integer robotCount  = robotGroupRelationMapper.selectRobotCountByGroupId(response.getVcChatRoomSerialNo());
        SendWxMsgRequest sendWxMsgRequest = new SendWxMsgRequest();
        if (taskUser != null){
            //用户微信ID
            String userWxId = taskUser.getWxId();
            //小助手微信ID
            String robotWxId = taskUser.getRobotWxId();
            sendWxMsgRequest.setToWxId(userWxId);
            RobotInfo info = robotInfoMapper.selectRobotByWxId(robotWxId);
            sendWxMsgRequest.setFromWxId(robotWxId);
            sendWxMsgRequest.setMerchantId(info.getMerchantId());
        }

        //获取RedisKey标识 是否是封号补水军
        String redisKey = RedisHandler.get(Seal_Robot_No_Money+response.getVcChatRoomSerialNo());
        //这是封号补的水军 不需要扣钱
        if (redisKey == null){
            //扣款
            DeductMercBalanceInputDTO inputDTO = new DeductMercBalanceInputDTO();
            inputDTO.setAmount(fireGroupConfig.getRobotDayMoney().doubleValue());
            inputDTO.setBusiType("SBT_002");
            inputDTO.setMerchantId(taskInfo.getCreateId());
            inputDTO.setRemarks("暖群宝邀请水军入群");
            Result<?> result1 = Urls.deductMercBalance(inputDTO);
            if (result1.getCode() != 0){
                Logs.e(getClass(),"扣款失败，邀请好友已退群"+JSON.toJSONString(result1)+"请求参数："+JSON.toJSONString(inputDTO));
                //将购买好友数量修改成当前水军数量
                GroupInfo infos = new GroupInfo();
                infos.setRobotNum(robotCount);
                infos.setGroupId(groupInfo.getGroupId());
                groupInfoMapper.updateByPrimaryKeySelective(infos);
                if (sendWxMsgRequest != null && sendWxMsgRequest.getToWxId() != null){
                    sendWxMsgRequest.setMsgContent("扣款失败"+result1.getMessage());
                    bilinService.sendWxMsg(sendWxMsgRequest);
                }
                //扣款失败让水军自己退群
                MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
                baseGpDTO.setIdentity(identity);
                baseGpDTO.setMerchatId(taskInfo.getCreateId());
                baseGpDTO.setWxId(fromWxId);
                baseGpDTO.setVcGroupId(response.getVcChatRoomSerialNo());
                //调用主动退群接口
                Result<BooleanResultVo> resultVoResult = Urls.deleteAndLeaveGroup(baseGpDTO);
                if (resultVoResult.getCode() != 0){
                    Logs.e(getClass(), "机器人退群失败"+JSON.toJSONString(resultVoResult));
                }
                return;
            }
        }else {
            Logs.e(getClass(),"群ID"+response.getVcChatRoomSerialNo()+"");
            Integer sealCount  = Integer.valueOf(redisKey) - 1;
            RedisHandler.del(Seal_Robot_No_Money+response.getVcChatRoomSerialNo());
            if (sealCount > 0){
            RedisHandler.set(Seal_Robot_No_Money+response.getVcChatRoomSerialNo(),sealCount);
            }
        }
        //插入当前邀请成功的水军号关联信息
        //新增水军与群绑定关系
        Date time = new Date();
        RobotGroupRelation robotGroupRelation = new RobotGroupRelation();
        robotGroupRelation.setWxGroupId(response.getVcChatRoomSerialNo());
        robotGroupRelation.setRobotWxId(fromWxId);
        robotGroupRelation.setIncomeGroupTime(time);
        robotGroupRelation.setCreateTime(time);
        robotGroupRelation.setModifyTime(time);
        robotGroupRelation.setIsDelete(0);
        //在群内
        robotGroupRelation.setState(1);
        robotGroupRelation.setRobotGroupName("");
        //插入记录
        robotGroupRelationMapper.insertSelective(robotGroupRelation);
        RobotInfo fromRobotInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
        //水军入群数量加一
        robotInfoMapper.updateGroupNum(1,fromRobotInfo.getRobotId());
        //将插入的水军计数加一
        RedisHandler.incr(Constant.RedisAddRobotNumKey+response.getVcChatRoomSerialNo());
        //获取计数
        String redisAddRobotNum = RedisHandler.get(Constant.RedisAddRobotNumKey+response.getVcChatRoomSerialNo());
        if (redisAddRobotNum == null){
            //设置redis计数
            RedisHandler.set(Constant.RedisAddRobotNumKey+response.getVcChatRoomSerialNo(),1);
            redisAddRobotNum = "1";
        }
        Logs.e(getClass(),"------------------------------------打印redis计数"+redisAddRobotNum);
        //获取当前购买水军数量 判断是不是已经足够数量
        if (Integer.valueOf(redisAddRobotNum).intValue()>= groupInfo.getLastBuyRobotNum()){
            if (sendWxMsgRequest != null && sendWxMsgRequest.getToWxId() != null){
                sendWxMsgRequest.setMsgContent("群"+groupInfo.getWxGroupName()+"已经完成机器人拉群！");
                bilinService.sendWxMsg(sendWxMsgRequest);
            }
            Logs.e(getClass(),"------------------------------------校验返回");
            //如果够数量了 删掉redisKey
            RedisHandler.del(Constant.RedisAddRobotNumKey+response.getVcChatRoomSerialNo());
            return;
        }
        Boolean isSend = true;
        //根据查询获取当前在群的水军号按照入群时间排序
        List<RobotGroupRelation> robotGroupRelations = robotGroupRelationMapper.selectRobotByGroupId(response.getVcChatRoomSerialNo());
        //循环List
        for (int i = robotGroupRelations.size()-1;i>=0;i--){
            Logs.e(getClass(),"循环次数 i="+i);
            String pullWxId = robotGroupRelations.get(i).getRobotWxId();
            RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(pullWxId);
            Logs.e(getClass(),"机器人"+robotInfo.getWxNick()+"开始拉人---------------------------------------------------");
            PullFreInGroupChatDTO pullFreInGroupChatDTO = new PullFreInGroupChatDTO();
            pullFreInGroupChatDTO.setIdentity(identity);
            pullFreInGroupChatDTO.setMerchatId(robotInfo.getMerchantId());
            pullFreInGroupChatDTO.setVcGroupId(response.getVcChatRoomSerialNo());
            pullFreInGroupChatDTO.setWxId(pullWxId);
            //获取该机器人的好友数
            List<FriendRelation> friendRelations = friendRelationMapper.selectRelationAllByWxId(pullWxId);
            //如果这个机器人好友数量不存在 则把当前这个redis链路删掉换下一个
            if (friendRelations == null || friendRelations.size() == 0){
               continue;
            }
            List<String> friends = new ArrayList<>();
            for (int j=0; j < friendRelations.size();j++) {
                Logs.e(getClass(),"循环次数 j="+j);
                String friendWxId = "";
                //查询好友微信号对应的信息
                if (friendRelations.get(j).getFromWxId().equals(fromWxId)) {
                    friendWxId = friendRelations.get(j).getToWxId();
                } else {
                    friendWxId = friendRelations.get(j).getFromWxId();
                }
                RobotInfo friendInfo = robotInfoMapper.selectRobotByWxId(friendWxId);
                Logs.e(getClass(),"机器人"+friendInfo.getWxNick()+"开始判断---------------------------------------------------");
                if (friendInfo == null) {
                    continue;
                }
                //判断这个账号的状态是否是正常
                if (friendInfo.getLoginState() != 1){
                    continue;
                }
                //如果已经入群 则换下一个
                Integer  count  = robotGroupRelationMapper.selectCountByWxIdGroupId(friendWxId,response.getVcChatRoomSerialNo());
                if (count > 0){
                    continue;
                }
                //如果已经大于等于最大入群数量 直接换下一个
                if (friendInfo.getGroupNum().intValue() >= fireGroupConfig.getRobotGroupCount().intValue()) {
                    continue;
                }
                //如果今天入群次数已达上限 直接换下一个
                Calendar cal = Calendar.getInstance();
                Date beginDate = cal.getTime();
                SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, 1);
                Date endDate = c.getTime();
                Integer logSize = robotGroupRelationMapper.selectCountByWxIdDate(friendWxId, sp.format(beginDate), sp.format(endDate));
                if (logSize == null) {
                    logSize = 0;
                }
                //剩余入群次数
                Integer number = fireGroupConfig.getRobotDayCount() - logSize;
                Logs.e(getClass(),"打印剩余入群次数"+number);
                if (number.intValue() <= 0) {
                    continue;
                }
                friends.add(friendWxId);
                pullFreInGroupChatDTO.setFreWxIds(friends);
                Logs.e(getClass(),"机器人"+friendInfo.getWxNick()+"判断通过---------------------------------------------------");
                Result<BooleanResultVo> booleanResultVoResult = Urls.pullFreInGroupChat(pullFreInGroupChatDTO);
                try {
                    //添加MQ日志记录
                    MqMessageInfo messageInfo = new MqMessageInfo();
                    messageInfo.setCreateTime(new Date());
                    messageInfo.setMessageReq(JSON.toJSONString(pullFreInGroupChatDTO));
                    messageInfo.setMessageRes(JSON.toJSONString(booleanResultVoResult));
                    messageInfo.setMessageState(1);
                    messageInfo.setMessageType(4505);
                    messageInfo.setToWxId(friendWxId);
                    messageInfo.setReqGroupId(groupInfo.getWxGroupId());
                    messageInfo.setSendWxId(fromWxId);
                    if (booleanResultVoResult.getData() != null){
                        messageInfo.setMessageOptId(booleanResultVoResult.getData().getOptSerNo());
                    }
                    mqMessageInfoMapper.insertSelective(messageInfo);
                }catch (Exception e){
                    Logs.e(getClass(), "机器人拉好友入群mq日志记录异常！请求参数：" + JSON.toJSONString(pullFreInGroupChatDTO) + "返回参数：" + JSON.toJSONString(booleanResultVoResult));
                    e.printStackTrace();
                }
                //调用个人号订阅
                GroupOnCallbackDTO dto = new GroupOnCallbackDTO();
                dto.setIdentity(identity);
                dto.setItemIds(friends);
                ResultBody resultBody = Rpc.post("http://neighbour-message/partner/callback/personal/on", dto,
                        ResultBody.class);
                Logs.e(getClass(), "机器人拉好友入群1！请求参数：" + JSON.toJSONString(pullFreInGroupChatDTO) + "返回参数：" + JSON.toJSONString(booleanResultVoResult));
                if (booleanResultVoResult.getCode() != 0) {
                    friends = new ArrayList<>();
                    Logs.e(getClass(),"------------------------------------123");
                    Logs.e(getClass(), "机器人拉好友入群失败！请求参数：" + JSON.toJSONString(pullFreInGroupChatDTO) + "返回参数：" + JSON.toJSONString(booleanResultVoResult));
                    continue;
                }else {
                    return;
                }
            }
        }
        //如果到最后还没有人邀请 那么提示用户
        if (isSend){
            Logs.e(getClass(),"------------------------------------123kgkfsfds");
            if (sendWxMsgRequest != null && sendWxMsgRequest.getToWxId() != null){
                sendWxMsgRequest.setMsgContent("当前小助手进群任务过多，请添加其他助手执行后续部署任务，避免入群失败。\n！");
                bilinService.sendWxMsg(sendWxMsgRequest);
            }
            //如果够数量了 删掉redisKey
            RedisHandler.del(Constant.RedisAddRobotNumKey+response.getVcChatRoomSerialNo());
            //将购买好友数量修改成当前水军数量
            GroupInfo infos = new GroupInfo();
            infos.setRobotNum(robotCount);
            infos.setGroupId(groupInfo.getGroupId());
            groupInfoMapper.updateByPrimaryKeySelective(infos);
            bilinService.sendWxMsg(sendWxMsgRequest);
        }
    }

    @Override
    public Result<Void> addFriend(OperationRobotRequest request) {
        for (Integer groupId:request.getGroupIds()) {
            GroupInfo groupInfo = groupInfoMapper.selectByPrimaryKey(groupId);
            if (groupInfo.getState() != 1){
                continue;
            }
            //获取群内所有水军的信息
            List<RobotGroupRelation> list = robotGroupRelationMapper.selectRobotByGroupId(groupInfo.getWxGroupId());
            if (list == null) {
                Logs.e(getClass(),"拉取好友失败，群内已经没有水军！请添加小助手！");
                continue;
            }
            //对所有水军的好友数量进行排序
            Collections.sort(list, new Comparator<RobotGroupRelation>() {
                @Override
                public int compare(RobotGroupRelation o1, RobotGroupRelation o2) {
                    //获取当前水军的好友数量
                    RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(o1.getRobotWxId());
                    Integer o1num = robotInfo.getFriendNum();
                    RobotInfo robotInfo2 = robotInfoMapper.selectRobotByWxId(o2.getRobotWxId());
                    Integer o2num = robotInfo2.getFriendNum();
                    return o2num.compareTo(o1num);
                }
            });
            Result result = addFriend(list,groupInfo);
            Logs.e(getClass(),"啦好友返回值"+JSON.toJSONString(result));
            if (result.getCode() != 0){
                return Result.err(-1,result.getMessage());
            }
            //插入最近新增好友数量
            groupInfo.setLastBuyRobotNum(request.getAddGroupNum());
            groupInfoMapper.updateByPrimaryKeySelective(groupInfo);
            //插入总购买数量
            groupInfoMapper.updateRobotNum(groupInfo.getWxGroupId(),request.getAddGroupNum());
        }
        return Result.ok();
    }

    private Result addFriend(List<RobotGroupRelation> list,GroupInfo groupInfo) {
        for (int i = 0; i < list.size(); i++) {
            //优先取好友数量最多的去拉好友入群
            RobotGroupRelation robotGroupRelation = list.get(i);
            String fromWxId = robotGroupRelation.getRobotWxId();
            //获取该机器人的好友数
            List<FriendRelation> friendRelations = friendRelationMapper.selectRelationAllByWxId(fromWxId);
            //如果这个机器人好友数量不存在 则换成下一个
            if (friendRelations == null || friendRelations.size() == 0) {
                Logs.e(getClass(),"微信ID"+fromWxId+"无法邀请好友！没有好友关系！");
                continue;
            }
            RobotInfo fromRobotInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
            PullFreInGroupChatDTO pullFreInGroupChatDTO = new PullFreInGroupChatDTO();
            pullFreInGroupChatDTO.setIdentity(identity);
            pullFreInGroupChatDTO.setMerchatId(fromRobotInfo.getMerchantId());
            pullFreInGroupChatDTO.setVcGroupId(groupInfo.getWxGroupId());
            pullFreInGroupChatDTO.setWxId(fromWxId);
            List<String> friends = new ArrayList<>();
            //获取配置信息
            FireGroupConfig fireGroupConfig = this.fireGroupConfigService.selectFireGroupConfig();
            for (int j = 0; j < friendRelations.size(); j++) {
                String friendWxId = "";
                //查询好友微信号对应的信息
                if (friendRelations.get(j).getFromWxId().equals(fromWxId)) {
                    friendWxId = friendRelations.get(j).getToWxId();
                } else {
                    friendWxId = friendRelations.get(j).getFromWxId();
                }
                RobotInfo friendInfo = robotInfoMapper.selectRobotByWxId(friendWxId);
                //判断这个账号的状态是否是正常
                if (friendInfo.getLoginState() != 1){
                   continue;
                }
                if (friendInfo == null) {
                    continue;
                }
                //如果已经入群 则换下一个
                Integer count = robotGroupRelationMapper.selectCountByWxIdGroupId(friendWxId, groupInfo.getWxGroupId());
                if (count > 0) {
                    continue;
                }
                //如果已经大于等于最大入群数量 直接换下一个
                if (friendInfo.getGroupNum().intValue() >= fireGroupConfig.getRobotGroupCount().intValue()) {
                    continue;
                }

                //如果今天入群次数已达上限 直接换下一个
                Calendar cal = Calendar.getInstance();
                Date beginDate = cal.getTime();
                SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd");
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, 1);
                Date endDate = c.getTime();
                Integer logSize = robotGroupRelationMapper.selectCountByWxIdDate(friendWxId, sp.format(beginDate), sp.format(endDate));
                if (logSize == null) {
                    logSize = 0;
                }
                //剩余入群次数
                Integer number = fireGroupConfig.getRobotDayCount() - logSize;
                if (number.intValue() <= 0) {
                    continue;
                }
                friends.add(friendWxId);
                pullFreInGroupChatDTO.setFreWxIds(friends);
                //调用个人号订阅
                GroupOnCallbackDTO groupOnCallbackDTO =  new GroupOnCallbackDTO();
                groupOnCallbackDTO.setIdentity(identity);
                groupOnCallbackDTO.setItemIds(friends);
                ResultBody resultBody = Rpc.post("http://neighbour-message/partner/callback/personal/on", groupOnCallbackDTO,
                        ResultBody.class);
                Result<BooleanResultVo> booleanResultVoResult = Urls.pullFreInGroupChat(pullFreInGroupChatDTO);
                try {
                    //添加MQ日志记录
                    MqMessageInfo info = new MqMessageInfo();
                    info.setCreateTime(new Date());
                    info.setMessageReq(JSON.toJSONString(pullFreInGroupChatDTO));
                    info.setMessageRes(JSON.toJSONString(booleanResultVoResult));
                    info.setMessageState(1);
                    info.setToWxId(friendWxId);
                    info.setMessageType(4505);
                    info.setReqGroupId(groupInfo.getWxGroupId());
                    info.setSendWxId(fromWxId);
                    if (booleanResultVoResult.getData() != null){
                        info.setMessageOptId(booleanResultVoResult.getData().getOptSerNo());
                    }
                    mqMessageInfoMapper.insertSelective(info);
                }catch (Exception e){
                    Logs.e(getClass(), "机器人拉好友入群mq日志记录异常！请求参数：" + JSON.toJSONString(pullFreInGroupChatDTO) + "返回参数：" + JSON.toJSONString(booleanResultVoResult));
                    e.printStackTrace();
                }
                Logs.e(getClass(), "机器人拉好友入群！请求参数：" + JSON.toJSONString(pullFreInGroupChatDTO) + "返回参数：" + JSON.toJSONString(booleanResultVoResult));
                if (booleanResultVoResult.getCode() != 0) {
                    friends = new ArrayList<>();
                    Logs.e(getClass(), "机器人拉好友入群失败！请求参数：" + JSON.toJSONString(pullFreInGroupChatDTO) + "返回参数：" + JSON.toJSONString(booleanResultVoResult));
                    continue;
                } else {
                    return Result.ok();
                }
            }
        }
        return Result.err(-1,"群"+groupInfo.getWxGroupName()+"拉取好友失败,当前小助手繁忙！");
    }

}
