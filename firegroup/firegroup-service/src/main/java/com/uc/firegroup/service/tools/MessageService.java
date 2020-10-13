package com.uc.firegroup.service.tools;

import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.*;
import com.uc.external.bilin.res.*;
import com.uc.firegroup.api.*;
import com.uc.firegroup.api.enums.GroupStatusEnum;
import com.uc.firegroup.api.pojo.*;
import com.uc.firegroup.api.request.OperationRobotRequest;
import com.uc.firegroup.api.request.SendWxMsgRequest;
import com.uc.firegroup.api.response.*;
import com.uc.firegroup.service.inner.chat.AbstractChatPushProssor;
import com.uc.firegroup.service.inner.chat.JobChatPushProcessor;
import com.uc.firegroup.service.inner.chat.KeyWordsChatPushProcessor;
import com.uc.firegroup.service.mapper.*;
import com.uc.firegroup.service.tools.msg.MsgThreadLocals;
import com.uc.framework.App;
import com.uc.framework.obj.Result;
import com.uc.framework.redis.RedisHandler;
import com.uc.framework.redis.queue.MessageListener;
import com.uc.framework.redis.queue.DelayQueue;
import com.uc.framework.redis.queue.Task;
import com.uc.framework.web.Rpc;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.fastjson.JSON;
import com.uc.firegroup.service.tools.msg.Message;
import com.uc.firegroup.service.tools.msg.Type;
import com.uc.framework.logger.Logs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sun.rmi.runtime.Log;

import java.text.SimpleDateFormat;
import java.util.*;

@Message
public class MessageService {
    @Autowired
    IRobotFriendService robotFriendService;
    @Autowired
    RobotInfoMapper robotInfoMapper;
    @Autowired
    IBilinService bilinService;
    @Autowired
    TaskInfoMapper taskInfoMapper;
    @Autowired
    TaskUserMapper taskUserMapper;
    @Autowired
    GroupInfoMapper groupInfoMapper;
    @Autowired
    RobotGroupRelationMapper robotGroupRelationMapper;
    @Autowired
    FriendRelationMapper friendRelationMapper;
    @Autowired
    IFireGroupConfigService fireGroupConfigService;
    @Autowired
    private IPullFriendService pullFriendService;
    @Value(value = "${identity}")
    private String identity;

    //每天入群扫码次数
    public String redis_scan_code  = "redis_scan_code";

    //当前是否能扫码
    public String redis_is_scan = "redis_is_scan";

    //扫码间隔时间
    public String redis_is_scan_time = "redis_is_scan_time";

    //每天入群扫码计数
    public String redis_scan_code_count  = "redis_scan_code_count";






    /**
     * title: 3002 添加好友结果回调
     *
     * @param msg kafka消息内容
     * @author HadLuo 2020-9-12 14:23:10
     */
    @Type(value = 3002, clazz = AddFriendCallbackVo.class)
    public void addFriendResult(AddFriendCallbackVo msg) {
        Logs.e(getClass(), "添加好友成功" + JSON.toJSONString(msg));

    }

    /**
     * title: 5050 私聊消息回调 (不包括有下载的)
     *
     * @param msg kafka消息内容
     * @author HadLuo 2020-9-12 14:23:10
     */
    @Type(value = 5050, clazz = ReceivedPersonMsgVo.class)
    public void receivedPersonMsg(ReceivedPersonMsgVo msg) {
        String msgContent = msg.getMsgInfo().getMsgContent();
        String merchantId = MsgThreadLocals.get().getMerchantId();
        SendWxMsgRequest sendWxMsgRequest = new SendWxMsgRequest();
        // 机器人微信号
        sendWxMsgRequest.setFromWxId(msg.getToWxId());
        // 用户微信号
        sendWxMsgRequest.setToWxId(msg.getFromWxId());
        sendWxMsgRequest.setMerchantId(merchantId);
        // 如果robotInfo为空 代表是机器人给用户发消息的回调 直接返回
        RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(msg.getToWxId());
        if (robotInfo == null) {
            return;
        }
        // 获取用户下所有任务信息
        List<TaskInfo> taskInfos = taskInfoMapper.queryAllList();
        if (taskInfos.size() == 0) {
            sendWxMsgRequest.setMsgContent("无法查询到创建的任务信息！");
            bilinService.sendWxMsg(sendWxMsgRequest);
            return;
        }
        Integer count = 0;
        for (TaskInfo taskInfo : taskInfos) {
            if (taskInfo.getVerificationCode().equals(msgContent)) {
                // 添加绑定记录
                TaskUser taskUser = new TaskUser();
                taskUser.setTaskId(taskInfo.getTaskId());
                taskUser.setMerchantId(taskInfo.getCreateId());
                taskUser.setWxId(msg.getFromWxId());
                taskUser.setIsDelete(0);
                taskUser.setCreateTime(new Date());
                taskUser.setRobotWxId(msg.getToWxId());
                // 插入
                taskUserMapper.insertSelective(taskUser);
                sendWxMsgRequest.setMsgContent(
                        "验证通过，欢迎使用暖群功能！您本次任务名称为【" + taskInfo.getTaskName() + "】，请您拉我进入您在任务下的所有群。");
                bilinService.sendWxMsg(sendWxMsgRequest);
                sendWxMsgRequest.setMsgContent("温馨提示：\n"
                        + "① 请确认每个个微群的群成员数小于498人，企微群的群成员数小于198人，并且将群聊验证关闭，不然我就进不了群哦。\n"
                        + "② 如部署成功，2分钟后会收到提示，如您群内人数较少或部署没反应，请在群内@我。\n" + "③ 切勿将我踢出群聊，否则将影响我为您提供服务，任务到期后我会自动退群。");

                bilinService.sendWxMsg(sendWxMsgRequest);
                GroupOnCallbackDTO groupOnCallbackDTO = new GroupOnCallbackDTO();
                groupOnCallbackDTO.setIdentity(identity);
                groupOnCallbackDTO.setItemIds(Lists.newArrayList(msg.getToWxId()));
                // 订阅群监听
                Result<Void> r = Urls.groupOnCallback(groupOnCallbackDTO);
                count = count + 1;
                continue;
            }
        }
        if (count == 0) {
            RobotInfo info = robotInfoMapper.selectRobotByWxId(msg.getFromWxId());
            if (info == null) {
                sendWxMsgRequest.setMsgContent("绑定失败，验证码错误!");
                bilinService.sendWxMsg(sendWxMsgRequest);
            }
        }
        System.err.println("私聊消息回调>>" + JSON.toJSONString(msg));
    }



    /**
     * title: 3006 添加好友结果回调
     *
     * @param msg kafka消息内容
     * @author HadLuo 2020-9-12 14:23:10
     */
    @Type(value = 3006, clazz = AddFriendCallbackVo.class)
    public void pullFriendCallback(AddFriendCallbackVo msg) {
        Logs.e(getClass(), "添加好友成功" + JSON.toJSONString(msg));

    }

    /**
     * title: 3005 添加好友回调
     *
     * @param msg kafka消息内容
     * @author 鲁志学 2020-9-12 14:23:10
     */
    @Type(value = 3005, clazz = AddFriendMsgVo.class)
    public void addFriendMsg(List<AddFriendMsgVo> msg) {
        Logs.e(getClass(), "添加好友回调" + JSON.toJSONString(msg));
        String fromWxId = MsgThreadLocals.get().getWxId();
        String toWxId = msg.get(0).getVcFriendSerialNo();
        RobotInfo fromRobotInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
        RobotInfo toRobotInfo = robotInfoMapper.selectRobotByWxId(toWxId);
        Logs.e(getClass(), "添加好友回调 fromRobotInfo:" + JSON.toJSONString(fromRobotInfo)+"toRobotInfo:"+toRobotInfo);
        if (fromRobotInfo == null && toRobotInfo == null) {
            return;
        }
        // 代表用户加机器人好友
        if (fromRobotInfo != null && toRobotInfo == null) {
            SendWxMsgRequest sendWxMsgRequest = new SendWxMsgRequest();
            sendWxMsgRequest.setToWxId(toWxId);
            RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
            sendWxMsgRequest.setFromWxId(fromWxId);
            sendWxMsgRequest.setMerchantId(robotInfo.getMerchantId());
            sendWxMsgRequest.setMsgContent(
                    "您好，我是您本次任务的小助手，请您输入验证码（可进入暖群任务列表中查看，还未创建任务的请先新建暖群任务。），如：“88881234”，验证通过后，可邀请入群。\n");
            bilinService.sendWxMsg(sendWxMsgRequest);
            System.err.println("私聊消息回调>>" + JSON.toJSONString(msg));
        }
        if (fromRobotInfo != null && toRobotInfo != null) {
            Logs.e(getClass(), "机器人添加好友回调" + JSON.toJSONString(msg));
            robotFriendService.robotAddFriendCallback(fromWxId, toWxId);
        }
    }

    /**
     * title: 4502 群成员入群回调
     *
     * @param msg kafka消息内容
     * @author 鲁志学 2020-9-16 14:23:10
     */
    @Type(value = 4502, clazz = IncomeGroupCallBackResponse.class)
    public void pullFriend(IncomeGroupCallBackResponse msg) {
        Logs.e(getClass(), "打印机器人入群回调! --------4502" + JSON.toJSONString(msg));
    }

    /**
     * title: 4008 获取企业微信二维码
     *
     * @param msg kafka消息内容
     * @author 鲁志学 2020-9-16 14:23:10
     */
    @Type(value = 4008, clazz = IncomeGroupCallBackResponse.class)
    public void getGroupQRCode(IncomeGroupCallBackResponse msg) {
        Logs.e(getClass(), "企业微信二维码回调!" + JSON.toJSONString(msg));
        // 获取到配置数
        FireGroupConfig fireGroupConfig = fireGroupConfigService.selectFireGroupConfig();
        // 执行任务数最少的号
        List<RobotInfo> robotInfos = robotInfoMapper.selectRobotByGroup(fireGroupConfig.getRobotGroupCount());
        if (robotInfos.size() == 0) {
            return;
        }
        for (RobotInfo robotInfo:robotInfos){
            // 判断当前号是否已经加过
            String redisIsScan = RedisHandler.get(redis_is_scan+robotInfo.getWxId());
            Logs.e(getClass(), "判断当前号是否已经加过！微信ID："+robotInfo.getWxId()+"redis值："+redisIsScan);
            if (redisIsScan != null && redisIsScan.equals("true")){
                continue;
            }
            //获取24小时内计数
            String redisScanCodeCount = RedisHandler.get(redis_scan_code_count+robotInfo.getWxId());
            Logs.e(getClass(), "24小时计数值！微信ID："+robotInfo.getWxId()+"redis值："+redisScanCodeCount);
            Integer count = 0;
            if (!StringUtils.isEmpty(redisScanCodeCount)){
                count = Integer.valueOf(redisScanCodeCount);
            }
            //获取配置的次数
            String redisScanCode = RedisHandler.get(redis_scan_code+robotInfo.getWxId());
            if (StringUtils.isEmpty(redisScanCode)){
                redisScanCode = "1";
            }
            //判断是否超出限制
            if (count.intValue() >= Integer.valueOf(redisScanCode).intValue()){
                continue;
            }
            QRCodeInChatRoomInputDTO qrCodeInChatRoomInputDTO = new QRCodeInChatRoomInputDTO();
            qrCodeInChatRoomInputDTO.setGroupQRCode(msg.getVcChatRoomQRCode());
            qrCodeInChatRoomInputDTO.setGroupType(0);
            qrCodeInChatRoomInputDTO.setIdentity(identity);
            qrCodeInChatRoomInputDTO.setMerchantId(robotInfo.getMerchantId());
            qrCodeInChatRoomInputDTO.setWxId(robotInfo.getWxId());
            Result<QRCodeInChatRoomInputDTO> qrCodeInChatRoomInputDTOResult = Urls
                    .scanQRCodeInChatRoom(qrCodeInChatRoomInputDTO);
            Logs.e(getClass(), "打印企微进群回调" + JSON.toJSONString(qrCodeInChatRoomInputDTOResult));
            if (qrCodeInChatRoomInputDTOResult.getCode() != 0) {
                Logs.e(getClass(), "机器人扫码进企微群异常！" + JSON.toJSONString(qrCodeInChatRoomInputDTOResult));
                continue;
            }
            String redisIsScanTime = RedisHandler.get(redis_is_scan_time+robotInfo.getWxId());
            if (redisIsScanTime == null){
                redisIsScanTime = "3";
            }
            Integer times =  Integer.valueOf(redisIsScanTime)*60;
            //配置Redis
            RedisHandler.setExpire(redis_is_scan+robotInfo.getWxId(),"true",times);
            Logs.e(getClass(), "设置是否能继续添加信息！微信ID："+robotInfo.getWxId()+"超时时间："+times);
            if (count > 0){
                //新增次数
                RedisHandler.incr(redis_scan_code_count+robotInfo.getWxId());
                Logs.e(getClass(), "设置24小时内新增次数！微信ID："+robotInfo.getWxId()+"次数："+count+"加一");
            }else {
                RedisHandler.setExpire(redis_scan_code_count+robotInfo.getWxId(),"1",24*60*60);
                Logs.e(getClass(), "设置24小时内第一次新增次数！微信ID："+robotInfo.getWxId());
            }
            return;
        }

    }

    /**
     * title: 4504
     *
     * @param msg kafka消息内容
     * @author 鲁志学 2020-9-16 14:23:10
     */
    @Type(value = 4504, clazz = IncomeGroupCallBackResponse.class)
    public void addFriend(IncomeGroupCallBackResponse msg) {
        Logs.e(getClass(), "打印机器人扫码入群回调!" + JSON.toJSONString(msg));
        // 查询是否初始化数据
        String fromWxId = MsgThreadLocals.get().getWxId();
        RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
        if (robotInfo == null) {
            return;
        }
        FireGroupConfig fireGroupConfig = fireGroupConfigService.selectFireGroupConfig();
        GroupInfo groupInfo = groupInfoMapper.selectInitByWxGroupId(msg.getVcChatRoomSerialNo());
        // 查询已经存在的 群
        GroupInfo oldGroupInfo = groupInfoMapper.selectInfoByWxGroupId(msg.getVcChatRoomSerialNo());
        if (groupInfo == null|| oldGroupInfo != null) {
            Logs.e(getClass(), "企业微信拉群获取初始化数据异常！" + JSON.toJSONString(msg));
            // 调用退群接口
            MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
            baseGpDTO.setIdentity(identity);
            baseGpDTO.setMerchatId(robotInfo.getMerchantId());
            baseGpDTO.setWxId(robotInfo.getWxId());
            baseGpDTO.setVcGroupId(msg.getVcChatRoomSerialNo());
            // 调用主动退群接口
            Result<BooleanResultVo> resultVoResult = Urls.deleteAndLeaveGroup(baseGpDTO);
            if (resultVoResult.getCode() != 0) {
                Logs.e(getClass(), "机器人退企微群异常！" + JSON.toJSONString(resultVoResult));
            }
            return;
        }
        TaskInfo taskInfo = taskInfoMapper.selectByTaskId(groupInfo.getTaskId());
        // 扣款
        DeductMercBalanceInputDTO inputDTO = new DeductMercBalanceInputDTO();
        inputDTO.setAmount(fireGroupConfig.getRobotDayMoney().doubleValue());
        inputDTO.setBusiType("SBT_002");
        inputDTO.setMerchantId(taskInfo.getCreateId());
        inputDTO.setRemarks("暖群宝邀请水军入群（" + msg.getVcChatRoomName() + "）");
        Result<?> result1 = Urls.deductMercBalance(inputDTO);
        if (result1.getCode() != 0) {
            // 扣款失败让水军自己退群
            MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
            baseGpDTO.setIdentity(identity);
            baseGpDTO.setMerchatId(taskInfo.getCreateId());
            baseGpDTO.setWxId(fromWxId);
            baseGpDTO.setVcGroupId(msg.getVcChatRoomSerialNo());
            // 调用主动退群接口
            Result<BooleanResultVo> resultVoResult = Urls.deleteAndLeaveGroup(baseGpDTO);
            if (resultVoResult.getCode() != 0) {
                Logs.e(getClass(), "机器人退群失败" + JSON.toJSONString(resultVoResult));
            }
            return;
        }
        // 调用群订阅的方法
        GroupOnCallbackDTO dto = new GroupOnCallbackDTO();
        dto.setIdentity(identity);
        dto.setItemIds(Lists.newArrayList(msg.vcChatRoomSerialNo));
        // 开启群订阅
        Result<Void> voidResult = Urls.groupOnCallback(dto);
        Logs.e(getClass(), "开启群订阅" + JSON.toJSONString(voidResult));
        groupInfo.setState(GroupStatusEnum.ACTIVE.getKey());
        groupInfo.setRobotNum(groupInfo.getRobotNum() + 1);
        groupInfoMapper.updateByPrimaryKeySelective(groupInfo);
        Date time = new Date();
        // 新增水军与群绑定关系
        RobotGroupRelation robotGroupRelation = new RobotGroupRelation();
        robotGroupRelation.setWxGroupId(msg.getVcChatRoomSerialNo());
        robotGroupRelation.setRobotWxId(robotInfo.getWxId());
        robotGroupRelation.setIncomeGroupTime(time);
        robotGroupRelation.setCreateTime(time);
        robotGroupRelation.setModifyTime(time);
        robotGroupRelation.setIsDelete(0);
        // 在群内
        robotGroupRelation.setState(1);
        robotGroupRelation.setRobotGroupName("");
        // 插入记录
        robotGroupRelationMapper.insertSelective(robotGroupRelation);
        // 水军入群数量加一
        robotInfoMapper.updateGroupNum(1, robotInfo.getRobotId());
        OperationRobotRequest robotRequest = new OperationRobotRequest();
        robotRequest.setAddGroupNum(groupInfo.getLastBuyRobotNum() - 1);
        robotRequest.setGroupIds(Lists.newArrayList(groupInfo.getGroupId()));
        if (groupInfo.getLastBuyRobotNum() > 1) {
            // 添加水军入群
            pullFriendService.addFriend(robotRequest);
        }
    }

    /**
     * title: 4505 邀请好友入群回调
     *
     * @param msg kafka消息内容
     * @author 鲁志学 2020-9-15
     */
    @Type(value = 4505, clazz = IncomeGroupCallBackResponse.class)
    @Transactional
    public void incomeGroupCallBack(IncomeGroupCallBackResponse msg) {
        Logs.e(getClass(), "打印机器人入群回调!------------------------4505" + JSON.toJSONString(msg));
        String fromWxId = MsgThreadLocals.get().getWxId();
        msg.setWxId(fromWxId);
        //获取是否是第一次入群
        GroupInfo groupInfo = groupInfoMapper.selectInfoByWxGroupId(msg.getVcChatRoomSerialNo());
        Logs.e(getClass(), "打印机器人入群回调!------------------------4505-----------group" + JSON.toJSONString(groupInfo));
        //校验判断是否能入群
        if (!verify(fromWxId)){
            //直接退群 发送消息
            SendWxMsgRequest sendWxMsgRequest = new SendWxMsgRequest();
            sendWxMsgRequest.setToWxId(msg.getVcFriendSerialNo());
            RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
            sendWxMsgRequest.setFromWxId(fromWxId);
            sendWxMsgRequest.setMerchantId(robotInfo.getMerchantId());
            sendWxMsgRequest.setMsgContent("当前小助手进群任务过多，请添加其他助手执行后续部署任务，避免入群失败。\n");
            bilinService.sendWxMsg(sendWxMsgRequest);

            MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
            baseGpDTO.setIdentity(identity);
            baseGpDTO.setMerchatId(robotInfo.getMerchantId());
            baseGpDTO.setWxId(fromWxId);
            baseGpDTO.setVcGroupId(msg.getVcChatRoomSerialNo());
            // 调用主动退群接口
            Result<BooleanResultVo> resultVoResult = Urls.deleteAndLeaveGroup(baseGpDTO);
            if (resultVoResult.getCode() != 0) {
                Logs.e(getClass(), "机器人退群失败" + JSON.toJSONString(resultVoResult));
            }
            return;
        }
        //
        if (groupInfo == null){
            incomeGroup(msg);
        }else {
            pullFriendService.pullFriend(msg);
        }
        System.err.println("私聊消息回调>>" + JSON.toJSONString(msg));
    }

    /***
     *
     * title: 群消息回调(资源已下载)
     *
     * @param msg
     * @author HadLuo 2020-9-19 15:42:32
     */
    @Type(value = 5051, clazz = Type5051Response.class)
    public void onGroupMsgReceive(Type5051Response msg) {
        // 说话的人的wxId
        String wxId = msg.getFromWxId();
        if (StringUtils.isEmpty(wxId) || msg.getMsgInfo() == null) {
            return;
        }
        // 判断 消息类型
        if (msg.getMsgInfo().getNMsgType() != 2001) {
            // 非 文字消息
            return;
        }
        String groupId = msg.getVcChatRoomId();
        if (StringUtils.isEmpty(groupId)) {
            return;
        }
        // 是我们水军发的 ， 直接过滤
        String key = "fg.cache.robotinfo." + wxId;
        String value = RedisHandler.get(key);
        if (StringUtils.isEmpty(value)) {
            // redis没有，查库
            RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(wxId);
            if (robotInfo == null) {
                // 没有查到， 缓存空值
                RedisHandler.setExpire(key, "-1", 60);
                // 不是水军 ， 调 消息触发
                AbstractChatPushProssor.getProssor(KeyWordsChatPushProcessor.class).onStartup(msg);
            } else {
                // 查到了， 表示 是 水军
                RedisHandler.setExpire(key, "1", 60);
            }
        } else {
            if (value.equals("1")) {
                // 是水军
                return;
            } else if (value.equals("-1")) {
                // 不是水军 ， 调 消息触发
                AbstractChatPushProssor.getProssor(KeyWordsChatPushProcessor.class).onStartup(msg);
            }
        }
    }
    
    /***
     * 
     * title: 消息发送成功回调 
     *
     * @param response
     * @author HadLuo 2020-10-12 9:17:15
     */
    @Type(value = 5002, clazz = Type5051Response.class)
    public void sendSuccessCall (Type5051Response response) {
        Logs.e(getClass(), "[ 消息发送成功回调 ]>>response="+JSON.toJSONString(response));
        // 群id
        String groupWxId = response.getVcChatRoomSerialNo();
        MqCallbackMessage message = MsgThreadLocals.get();
        // 回调确认吗
        String ackKey = message.getOptSerNo();
        // 查询 og
        PushLog record = new PushLog();
        record.setPushErrorMsg(ackKey);
        PushLog log = App.getBean(PushLogMapper.class).selectOne(record);
        if(log != null) {
            //查询剧本
            PlayInfo playInfo = App.getBean(PlayInfoMapper.class).selectByPrimaryKey(log.getPlayId());
            if(playInfo != null) {
                AbstractChatPushProssor.getProssor(playInfo).onAck(ackKey, groupWxId, null) ;
            }
        }
    }
    

    /***
     *
     * 移除机器人入群
     *
     * @param msg
     * @author 鲁志学 2020-9-21
     */
    @Type(value = 4507, clazz = IncomeGroupCallBackResponse.class)
    public void removeRobot(IncomeGroupCallBackResponse msg) {
        Logs.e(getClass(), "监听机器人被踢出群");
        String fromWxId = MsgThreadLocals.get().getWxId();
        String wxGroupId = msg.getVcChatRoomSerialNo();
        // 用微信ID和群ID查询记录
        RobotGroupRelation robotGroupRelation = robotGroupRelationMapper.selectByWxIdGroupId(fromWxId,
                wxGroupId);
        // 修改状态为待退群
        if (robotGroupRelation != null) {
            //修改状态为已退群 更新退群时间
            robotGroupRelation.setOutGroupTime(new Date());
            robotGroupRelation.setState(2);
            robotGroupRelationMapper.updateByPrimaryKeySelective(robotGroupRelation);
            RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
            if (robotInfo == null){
                return;
            }
            //水军在群数量减少
            robotInfoMapper.updateGroupNum(-1,robotInfo.getRobotId());
            //已购水军数量减少
            groupInfoMapper.updateRobotNum(robotGroupRelation.getWxGroupId(),-1);
        }
    }

    private void incomeGroup(IncomeGroupCallBackResponse msg){
        SendWxMsgRequest sendWxMsgRequest = new SendWxMsgRequest();
        sendWxMsgRequest.setToWxId(msg.getVcFriendSerialNo());
        //机器人微信ID
        String fromWxId = MsgThreadLocals.get().getWxId();
        RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
        sendWxMsgRequest.setFromWxId(fromWxId);
        sendWxMsgRequest.setMerchantId(robotInfo.getMerchantId());
        FireGroupConfig fireGroupConfig = fireGroupConfigService.selectFireGroupConfig();
        TaskUser taskUser = taskUserMapper.selectUserByWxId(msg.getVcFriendSerialNo());
        if (taskUser == null || !taskUser.getRobotWxId().equals(fromWxId)) {
            Logs.e(getClass(), "taskUser:" + JSON.toJSONString(taskUser));
            sendWxMsgRequest.setMsgContent(
                    "先输入验证码进行绑定！");
            bilinService.sendWxMsg(sendWxMsgRequest);
            MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
            baseGpDTO.setIdentity(identity);
            baseGpDTO.setMerchatId(robotInfo.getMerchantId());
            baseGpDTO.setWxId(fromWxId);
            baseGpDTO.setVcGroupId(msg.getVcChatRoomSerialNo());
            // 调用主动退群接口
            Result<BooleanResultVo> resultVoResult = Urls.deleteAndLeaveGroup(baseGpDTO);
            if (resultVoResult.getCode() != 0) {
                Logs.e(getClass(), "机器人退群失败" + JSON.toJSONString(resultVoResult));
            }
            return;
        }
        // 如果任务设置的水军号大于1 则拉其他好友入群
        TaskInfo task = taskInfoMapper.selectByPrimaryKey(taskUser.getTaskId());
        // 判断群没有进行绑定
        GroupInfo info = groupInfoMapper.selectInfoByWxGroupId(msg.getVcChatRoomSerialNo());
        if (info != null && info.getGroupId() != null && info.getTaskId() != taskUser.getTaskId()) {
            TaskInfo taskInfo = taskInfoMapper.selectByPrimaryKey(info.getTaskId());
            sendWxMsgRequest.setMsgContent(
                    "【" + msg.getVcChatRoomName() + "】群已在任务【" + taskInfo.getTaskName() + "】，请在后台直接转移任务。\n");
            bilinService.sendWxMsg(sendWxMsgRequest);
            MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
            baseGpDTO.setIdentity(identity);
            baseGpDTO.setMerchatId(robotInfo.getMerchantId());
            baseGpDTO.setWxId(fromWxId);
            baseGpDTO.setVcGroupId(msg.getVcChatRoomSerialNo());
            // 调用主动退群接口
            Result<BooleanResultVo> resultVoResult = Urls.deleteAndLeaveGroup(baseGpDTO);
            if (resultVoResult.getCode() != 0) {
                Logs.e(getClass(), "机器人退群失败" + JSON.toJSONString(resultVoResult));
            }
            return;
        }
        String openWXId = "";
        //判断是否已经开群
        QueryOpenedGroupDTO openedGroupDTO = new QueryOpenedGroupDTO();
        openedGroupDTO.setIdentity(identity);
        openedGroupDTO.setMerchatId(task.getCreateId());
        openedGroupDTO.setParams(Lists.newArrayList(msg.getVcChatRoomSerialNo()));
        Result<List<QueryOpenedGroupVO>> queryOpenedGroupVOResult = Urls.queryOpenedGroup(openedGroupDTO);
        Logs.e(getClass(),"打印查询开通信息日志：req:"+JSON.toJSONString(openedGroupDTO)+"res:"+JSON.toJSONString(queryOpenedGroupVOResult));
        //如果进行过开群 那么保存开群微信ID
        if (queryOpenedGroupVOResult.getCode() == 0 && queryOpenedGroupVOResult.getData() != null && queryOpenedGroupVOResult.getData().size() != 0){
            openWXId = queryOpenedGroupVOResult.getData().get(0).getWxId();
        }else {
            // 开群
            MerchantBaseGpDTO merchantBaseGpDTO = new MerchantBaseGpDTO();
            merchantBaseGpDTO.setMerchatId(robotInfo.getMerchantId());
            merchantBaseGpDTO.setVcGroupId(msg.getVcChatRoomSerialNo());
            merchantBaseGpDTO.setWxId(fromWxId);
            merchantBaseGpDTO.setIdentity(identity);
            Result<BooleanResultVo> booleanResultVoResult = Urls.robotGroupOpen(merchantBaseGpDTO);
            if (booleanResultVoResult.getCode() != 0) {
                Logs.e(getClass(), "小助手开群失败！req:" + JSON.toJSONString(merchantBaseGpDTO) + " res:"
                        + JSON.toJSONString(booleanResultVoResult));
                sendWxMsgRequest.setMsgContent("小助手开群失败！请拉取其他小助手进行开群");
                bilinService.sendWxMsg(sendWxMsgRequest);
                return;
            }
            openWXId = fromWxId;
        }

        // 扣款
        DeductMercBalanceInputDTO inputDTO = new DeductMercBalanceInputDTO();
        inputDTO.setAmount(fireGroupConfig.getRobotDayMoney().doubleValue());
        inputDTO.setBusiType("SBT_002");
        inputDTO.setMerchantId(task.getCreateId());
        inputDTO.setRemarks("暖群宝邀请水军入群（" + msg.getVcChatRoomName() + "）");
        Result<?> result1 = Urls.deductMercBalance(inputDTO);
        if (result1.getCode() != 0) {
            sendWxMsgRequest.setMsgContent("扣款失败" + result1.getMessage());
            bilinService.sendWxMsg(sendWxMsgRequest);
            // 扣款失败让水军自己退群
            MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
            baseGpDTO.setIdentity(identity);
            baseGpDTO.setMerchatId(task.getCreateId());
            baseGpDTO.setWxId(fromWxId);
            baseGpDTO.setVcGroupId(msg.getVcChatRoomSerialNo());
            // 调用主动退群接口
            Result<BooleanResultVo> resultVoResult = Urls.deleteAndLeaveGroup(baseGpDTO);
            if (resultVoResult.getCode() != 0) {
                Logs.e(getClass(), "机器人退群失败" + JSON.toJSONString(resultVoResult));
            }
            return;
        }
        Date time = new Date();
        GroupInfo groupInfo = new GroupInfo();
        Boolean isAddFriend = false;
            // 新增群信息表
            groupInfo.setTaskId(taskUser.getTaskId());
            QueryBaseGroupDTO queryBaseGroupDTO = new QueryBaseGroupDTO();
            queryBaseGroupDTO.setGroupId(msg.getVcChatRoomSerialNo());
            queryBaseGroupDTO.setIdentity(identity);
            queryBaseGroupDTO.setWxId(fromWxId);
            Result<GroupBaseInfoVO> result = Urls.queryGroupMermber(queryBaseGroupDTO);
            if (result == null || result.getData() == null) {
                groupInfo.setGroups("0");
                groupInfo.setGroupName("未分组");
            } else {
                groupInfo.setGroups(result.getData().getVcGroupId());
                groupInfo.setGroupName(result.getData().getVcGroupName());
            }
            groupInfo.setCreateTime(time);
            groupInfo.setIsDelete(0);
            groupInfo.setModifyTime(time);
            // 枚举
            groupInfo.setState(GroupStatusEnum.ACTIVE.getKey());
            groupInfo.setWxGroupId(msg.getVcChatRoomSerialNo());
            groupInfo.setWxGroupName(msg.getVcChatRoomName());
            groupInfo.setRobotNum(task.getRobotNum());
            groupInfo.setLastBuyRobotNum(task.getRobotNum());
            groupInfo.setLastDelRobotNum(0);
            // 插入群组开通号
            groupInfo.setOpenRobotWxId(openWXId);
            //获取群成员信息 调用比邻的接口获取
           QueryBaseGroupDTO baseGroupDTO = new QueryBaseGroupDTO();
           baseGroupDTO.setIdentity(identity);
           baseGroupDTO.setGroupId(msg.getVcChatRoomSerialNo());
           baseGroupDTO.setWxId(groupInfo.getOpenRobotWxId());
           Result<GroupBaseInfoVO> groupResult = Urls.queryGroupMermber(baseGroupDTO);
           Logs.e(getClass(),"打印比邻获取群成员信息接口req"+JSON.toJSONString(baseGroupDTO)+"返回值："+JSON.toJSONString(groupResult));
           // 个人微信群
           groupInfo.setGroupType(1);
           if(groupResult.getCode() == 0){
               String  type = groupResult.getData().getEnterpriseChatRoom();
               Logs.e(getClass(),"打印群标识："+type);
               if (type != null && type.equals("1")){
                   groupInfo.setGroupType(2);
               }
           }
           // 调用群订阅的方法
            GroupOnCallbackDTO dto = new GroupOnCallbackDTO();
            dto.setIdentity(identity);
            dto.setItemIds(Lists.newArrayList(msg.vcChatRoomSerialNo));
            // 开启群订阅
            Result<Void> voidResult = Urls.groupOnCallback(dto);
            Logs.e(getClass(), "开启群订阅" + JSON.toJSONString(voidResult));
            // 插入
            groupInfoMapper.insertSelective(groupInfo);
            if (task.getRobotNum() > 1) {
                isAddFriend = true;
            }
        // 新增水军与群绑定关系
        RobotGroupRelation robotGroupRelation = new RobotGroupRelation();
        robotGroupRelation.setWxGroupId(msg.getVcChatRoomSerialNo());
        robotGroupRelation.setRobotWxId(fromWxId);
        robotGroupRelation.setIncomeGroupTime(time);
        robotGroupRelation.setCreateTime(time);
        robotGroupRelation.setModifyTime(time);
        robotGroupRelation.setIsDelete(0);
        // 在群内
        robotGroupRelation.setState(1);
        // 插入记录
        robotGroupRelationMapper.insertSelective(robotGroupRelation);
        // 水军入群数量加一
        robotInfoMapper.updateGroupNum(1, robotInfo.getRobotId());
        if (!isAddFriend){
            sendWxMsgRequest.setMsgContent("【" + msg.getVcChatRoomName() + "】群已经完成绑定。\n");
            bilinService.sendWxMsg(sendWxMsgRequest);
        }
        // 判断当前群组水军数量小于购买水军数量
        if (isAddFriend) {
            PullFreInGroupChatDTO pullFreInGroupChatDTO = new PullFreInGroupChatDTO();
            pullFreInGroupChatDTO.setIdentity(identity);
            pullFreInGroupChatDTO.setMerchatId(robotInfo.getMerchantId());
            pullFreInGroupChatDTO.setVcGroupId(msg.getVcChatRoomSerialNo());
            pullFreInGroupChatDTO.setWxId(fromWxId);
            List<String> friends = new ArrayList<>();
            // 获取该机器人的好友数
            List<FriendRelation> friendRelations = friendRelationMapper.selectRelationAllByWxId(fromWxId);
            // 如果没有好友关系
            if (friendRelations == null) {
                sendWxMsgRequest.setMsgContent("当前小助手进群任务过多，请添加其他助手执行后续部署任务，避免入群失败。\n");
                bilinService.sendWxMsg(sendWxMsgRequest);
                return;
            }
            Boolean isSend = true;
            for (int i = 0; i < friendRelations.size(); i++) {
                String friendWxId = "";
                // 查询好友微信号对应的信息
                if (friendRelations.get(i).getFromWxId().equals(fromWxId)) {
                    friendWxId = friendRelations.get(i).getToWxId();
                } else {
                    friendWxId = friendRelations.get(i).getFromWxId();
                }
                if (!verify(friendWxId)){
                    continue;
                }
                friends.add(friendWxId);
                pullFreInGroupChatDTO.setFreWxIds(friends);
                Result<BooleanResultVo> voResult = Urls.pullFreInGroupChat(pullFreInGroupChatDTO);
                //调用个人号订阅
                GroupOnCallbackDTO groupOnCallbackDTO = new GroupOnCallbackDTO();
                dto.setIdentity(identity);
                dto.setItemIds(friends);
                ResultBody resultBody = Rpc.post("http://neighbour-message/partner/callback/personal/on", dto,
                        ResultBody.class);
                if (voResult.getCode() != 0) {
                    Logs.e(getClass(), "机器人拉好友入群失败！请求参数：" + JSON.toJSONString(pullFreInGroupChatDTO) + "返回参数："
                            + JSON.toJSONString(voResult));
                    continue;
                } else {
                    sendWxMsgRequest.setMsgContent("【" + msg.getVcChatRoomName()
                            + "】群已经完成绑定，稍后我将在半小时内，拉其他小伙伴入群，请耐心等待，若半小时后群内助手不够，请拉其他小助手入群试试。\n");
                    bilinService.sendWxMsg(sendWxMsgRequest);
                    //设置redis计数
                    RedisHandler.set(Constant.RedisAddRobotNumKey+msg.getVcChatRoomSerialNo(),1);
                    isSend = false;
                    break;
                }
            }
            // 如果已经发送过 那么就不发送了
            if (isSend) {
                // 将购买好友数量修改成当前水军数量
                GroupInfo infos = new GroupInfo();
                Integer robotCount = robotGroupRelationMapper
                        .selectRobotCountByGroupId(msg.getVcChatRoomSerialNo());
                infos.setRobotNum(robotCount);
                infos.setGroupId(groupInfo.getGroupId());
                groupInfoMapper.updateByPrimaryKeySelective(infos);
                sendWxMsgRequest.setMsgContent("当前小助手进群任务过多，请添加其他助手执行后续部署任务，避免入群失败。\n");
                bilinService.sendWxMsg(sendWxMsgRequest);
                return;
            }
        }
    }

    private Boolean verify(String friendWxId){
        RobotInfo friendInfo = robotInfoMapper.selectRobotByWxId(friendWxId);
        FireGroupConfig fireGroupConfig = fireGroupConfigService.selectFireGroupConfig();
        if (friendInfo == null) {
            return false;
        }
        // 如果已经大于等于最大入群数量 直接换下一个
        if (friendInfo.getGroupNum().intValue() >= fireGroupConfig.getRobotGroupCount().intValue()) {
            return false;
        }
        // 如果今天入群次数已达上限 直接换下一个
        Calendar cal = Calendar.getInstance();
        Date beginDate = cal.getTime();
        SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        Date endDate = c.getTime();
        Integer logSize = robotGroupRelationMapper.selectCountByWxIdDate(friendWxId,
                sp.format(beginDate), sp.format(endDate));
        if (logSize == null) {
            logSize = 0;
        }
        // 剩余入群次数
        Integer number = fireGroupConfig.getRobotDayCount() - logSize;
        if (number.intValue() <= 0) {
            return false;
        }
        return true;
    }

}
