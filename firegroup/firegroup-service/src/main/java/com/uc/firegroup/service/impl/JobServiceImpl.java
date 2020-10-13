package com.uc.firegroup.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.*;
import com.uc.external.bilin.res.BooleanResultVo;
import com.uc.external.bilin.res.PersonalSimpleInfoVO;
import com.uc.external.bilin.res.ResultBody;
import com.uc.external.bilin.res.SelectWxInfoPageRo;
import com.uc.firegroup.api.IFireGroupConfigService;
import com.uc.firegroup.api.IJobService;
import com.uc.firegroup.api.enums.FriendLogStateEnum;
import com.uc.firegroup.api.enums.GroupStatusEnum;
import com.uc.firegroup.api.pojo.*;
import com.uc.firegroup.api.request.FriendLogRequest;
import com.uc.firegroup.service.inner.chat.AbstractChatPushProssor;
import com.uc.firegroup.service.inner.chat.JobChatPushProcessor;
import com.uc.firegroup.service.mapper.*;
import com.uc.framework.Times;
import com.uc.framework.logger.Logs;
import com.uc.framework.logger.alert.AlertContext;
import com.uc.framework.login.User;
import com.uc.framework.login.UserThreadLocal;
import com.uc.framework.obj.BusinessException;
import com.uc.framework.obj.Result;
import com.uc.framework.thread.AsyncTask;
import com.uc.framework.web.Rpc;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class JobServiceImpl implements IJobService {

    @Autowired
    private RobotInfoMapper robotInfoMapper;
    @Autowired
    private IFireGroupConfigService fireGroupConfigService;
    @Autowired
    private FriendLogMapper friendLogMapper;
    @Autowired
    private FriendRelationMapper friendRelationMapper;
    @Autowired
    private RobotGroupRelationMapper robotGroupRelationMapper;
    @Value(value = "${identity}")
    private String identity;
    @Value("${merchatId}")
    private String merchatId;
    @Autowired
    private GroupInfoMapper groupInfoMapper;
    @Autowired
    private IGroupInfoServiceImpl groupInfoService;
    @Value("${accountGroupId}")
    private String accountGroupId;
    @Resource
    private TaskInfoMapper taskInfoMapper;
    @Autowired
    private MqMessageInfoMapper mqMessageInfoMapper;

    @Override
    public void scanTimePush() {
        // 接受任务
        AsyncTask.execute(() -> {
            Logs.e(getClass(), "[timer start]>>scanTimePush");
            long s1 = System.currentTimeMillis();
            AbstractChatPushProssor.getProssor(JobChatPushProcessor.class).onStartup();
            s1 = System.currentTimeMillis() - s1;
            Logs.e(getClass(), "[timer end(" + s1 + "ms)]>>scanTimePush");
        });
    }

    @Override
    public void robotAddFriendJob() {
        Logs.e(getClass(), "进入加好友定时任务");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addFriend();
            }
        };
        AsyncTask.execute(runnable);
    }

    private void addFriend() {
        FireGroupConfig fireGroupConfig = fireGroupConfigService.selectFireGroupConfig();
        if (fireGroupConfig == null) {
            Logs.e(getClass(), "配置错误！");
            return;
        }
        // 判断配置开关是否开启
        if (fireGroupConfig.getFriendSwitch().intValue() == 0) {
            Logs.e(getClass(), "开关已关闭！");
            return;
        }
        // 获取当天的日期
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String date = formatter.format(new Date());
        // 判断今天已执行了多少次加好友任务 超过配置数直接返回
        Integer count = friendLogMapper.selectTodayCount(date);
        if (count >= fireGroupConfig.getRobotFriendCount()) {
            Logs.e(getClass(), "今天已超过加好友次数！");
            return;
        }
        // 按照好友排序查询不足配置好友数的机器人号
        List<RobotInfo> robotInfos = robotInfoMapper.queryRobotList(fireGroupConfig.getRobotFriendNum());
        if (robotInfos == null || robotInfos.size() == 0) {
            Logs.e(getClass(), "没有需要添加的好友！");
            return;
        }
        // 构建操作日志实体
        FriendLog newLog = new FriendLog();
        // 循环好友列表
        for (RobotInfo robotInfo : robotInfos) {
            FriendLogRequest request = new FriendLogRequest();
            request.setFriendDate(date);
            request.setWxId(robotInfo.getWxId());
            // 如果是被动加过好友直接换下个账号
            FriendLog log = friendLogMapper.selectFriendLogToByDay(request);
            if (log != null) {
                continue;
            }
            // 判断当前水军是否已经主动加过好友
            FriendLog friendLog = friendLogMapper.selectFriendLogFromByDay(request);
            // 如果已经主动加过好友 判断是否还能继续加好友
            if (friendLog != null) {
                // 如果上次已经主动添加过好友 并且加好友数量大于等于配置加好友上限 则换下个账号
                if (friendLog.getFromAddNum().intValue() != 0
                        && friendLog.getFromAddNum().intValue() >= fireGroupConfig.getFromAddCount()) {
                    continue;
                }
                // 如果上次已经被加过好友 则换下个账号
                if (friendLog.getFromAddedNum().intValue() != 0) {
                    continue;
                }
                // 设置主加人当天已加好友次数
                newLog.setFromAddNum(friendLog.getFromAddNum() + 1);
                newLog.setFromDays(friendLog.getFromDays());
                // 如果今天还没有加过好友则查询昨天的记录
            } else {
                // 根据昨天的记录校验今天是否还能加好友
                if (!verify(robotInfo.getWxId(), fireGroupConfig.getAddDays(), newLog, 1)) {
                    continue;
                }
            }
            // 做完无数校验还幸存的话说明robotInfo可以用来加好友
            // 设置日期
            try {
                newLog.setFriendDate(formatter.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // 设置主加人微信号
            newLog.setFromWxId(robotInfo.getWxId());
            // 设置主加人被加好友次数
            newLog.setFromAddedNum(0);
            // 设置主加人当天已加好友次数
            if (newLog.getFromAddNum() == null || newLog.getFromAddNum() == 0) {
                newLog.setFromAddNum(1);
            }
            // 设置主加人已连续加好友或者被加好友天数
            if (newLog.getFromDays() == null || newLog.getFromDays() == 0) {
                newLog.setFromDays(1);
            }
            // 继续循环列表找被加的小伙伴
            for (RobotInfo info : robotInfos) {
                // 如果wxId相同 那么直接换下一个
                if (robotInfo.getWxId().equals(info.getWxId())) {
                    continue;
                }
                // 如果与主加人已经存在好友关系 则换下一个
                FriendRelation fromRelation = friendRelationMapper.selectRelationForWxId(robotInfo.getWxId(),
                        info.getWxId());
                if (fromRelation != null) {
                    continue;
                }
                FriendRelation toRelation = friendRelationMapper.selectRelationForWxId(info.getWxId(),
                        robotInfo.getWxId());
                if (toRelation != null) {
                    continue;
                }
                // 将日期设置为今天
                request.setFriendDate(date);
                request.setWxId(info.getWxId());
                // 获取当前水军WX号今天主动加好友日志记录
                FriendLog twoFriendLog = friendLogMapper.selectFriendLogFromByDay(request);
                // 如果存在 直接换下一个
                if (twoFriendLog != null) {
                    continue;
                }
                // 获取当前水军WX号今天被动加好友的日志记录
                FriendLog twoToFriendLog = friendLogMapper.selectFriendLogToByDay(request);
                // 如果被加好友记录不为空
                if (twoToFriendLog != null) {
                    // 判断是否超过配置的上限次数
                    if (twoToFriendLog.getToAddedNum() != null && twoToFriendLog.getToAddedNum()
                            .intValue() >= fireGroupConfig.getToAddCount().intValue()) {
                        continue;
                    }
                    // 设置今日已被加次数
                    newLog.setToAddedNum(twoToFriendLog.getToAddedNum() + 1);
                    newLog.setToDays(twoToFriendLog.getToDays());
                    // 如果被加好友记录为空 则需要查询昨天的记录
                } else {
                    // 根据昨天的记录校验今天是否还能加好友
                    if (!verify(info.getWxId(), fireGroupConfig.getAddDays(), newLog, 2)) {
                        continue;
                    }
                }
                // 所有校验都通过以后 还没有淘汰的小伙伴info则满足被加水军的要求 设置被加人操作日志
                // 设置被加人微信号
                newLog.setToWxId(info.getWxId());
                // 设置被加水军加人次数
                newLog.setToAddNum(0);
                // 设置今日已被加次数
                if (newLog.getToAddedNum() == null || newLog.getToAddedNum() == 0) {
                    newLog.setToAddedNum(1);
                }
                // 设置被加水军连续天数
                if (newLog.getToDays() == null || newLog.getToDays() == 0) {
                    newLog.setToDays(1);
                }
                // 跳出循环
                break;
            }
            // 设置操作记录
            // 设置状态
            newLog.setState(FriendLogStateEnum.SEND_MSG.getKey());
            newLog.setCreateTime(new Date());
            newLog.setModifyTime(null);
            try {
                RobotInfo toInfo = robotInfoMapper.selectRobotByWxId(newLog.getToWxId());
                // 调用个人号订阅
                GroupOnCallbackDTO groupOnCallbackDTO = new GroupOnCallbackDTO();
                groupOnCallbackDTO.setIdentity(identity);
                groupOnCallbackDTO.setItemIds(Lists.newArrayList(newLog.getFromWxId(), newLog.getToWxId()));
                ResultBody resultBody = Rpc.post("http://neighbour-message/partner/callback/personal/on",
                        groupOnCallbackDTO, ResultBody.class);
                Logs.e(getClass(), "机器人加好友订阅比邻返回信息：" + JSON.toJSONString(resultBody));
                // 调用比邻加好友接口
                AddFriendDTO dto = new AddFriendDTO();
                dto.setIdentity(identity);
                dto.setMerchatId(robotInfo.getMerchantId());
                // 添加人
                dto.setWxId(newLog.getFromWxId());
                // 被添加人
                dto.setFriendWxAcc(toInfo.getWxAcc());

                Result<BooleanResultVo> result = Urls.addFriend(dto);
                try {
                    // 添加MQ日志记录
                    MqMessageInfo messageInfo = new MqMessageInfo();
                    messageInfo.setCreateTime(new Date());
                    messageInfo.setMessageReq(JSON.toJSONString(dto));
                    messageInfo.setMessageRes(JSON.toJSONString(result));
                    messageInfo.setMessageState(1);
                    messageInfo.setMessageType(3005);
                    messageInfo.setReqGroupId(null);
                    messageInfo.setSendWxId(newLog.getFromWxId());
                    if (result.getData() != null) {
                        messageInfo.setMessageOptId(result.getData().getOptSerNo());
                    }
                    mqMessageInfoMapper.insertSelective(messageInfo);
                } catch (Exception e) {
                    Logs.e(getClass(), "机器人拉好友入群mq日志记录异常！请求参数：" + JSON.toJSONString(dto) + "返回参数："
                            + JSON.toJSONString(result));
                    e.printStackTrace();
                }
                Logs.e(getClass(), "机器人加好友比邻返回信息：" + JSON.toJSONString(result));
            } catch (Exception e) {
                Logs.e(getClass(), "机器人加好友调用接口失败：" + JSON.toJSONString(newLog));
            }
            // 插入日志
            friendLogMapper.insertSelective(newLog);
            Logs.e(getClass(), "任务执行完成！");
            // 本次水军加好友逻辑结束
            break;
        }
    }

    private Boolean verify(String wxId, Integer addDays, FriendLog friendLog, Integer type) {
        FriendLogRequest request = new FriendLogRequest();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date d = cal.getTime();
        SimpleDateFormat sp = new SimpleDateFormat("yyyy-MM-dd");
        // 将日期设置为昨天
        request.setFriendDate(sp.format(d));
        request.setWxId(wxId);
        Integer days = 0;
        // 查询昨天最后一条主动加人的日志
        FriendLog fromLog = friendLogMapper.selectFriendLogFromByDay(request);
        // 如果不等于空 判断已连续加了多少天
        if (fromLog != null && fromLog.getFromDays() >= addDays) {
            return false;
        }
        if (fromLog != null && type == 1) {
            days = fromLog.getFromDays();
        }
        // 查询昨天最后一条被动被加的日志
        FriendLog toLog = friendLogMapper.selectFriendLogToByDay(request);
        // 如果不等于空 判断已连续加了多少天
        if (toLog != null && toLog.getToDays() >= addDays) {
            return false;
        }
        if (toLog != null && type == 2) {
            days = toLog.getToDays();
        }
        if (days == null) {
            days = 0;
        }
        if (type == 1) {
            friendLog.setFromDays(days + 1);
        } else {
            friendLog.setToDays(days + 1);
        }
        return true;
    }

    @Override
    public void robotInfoUpdate() {
        Logs.e(getClass(), "进入机器人账号信息更新定时任务");
        AsyncTask.execute(() -> {
            try {
                List<SelectWxInfoPageRo.Data> accountList = new ArrayList<>();
                SelectWxInfoPageDto pageDto = new SelectWxInfoPageDto();
                pageDto.setIdentity(identity);
                pageDto.setMerchatId(merchatId);
                SelectWxInfoPageDto.Data data = new SelectWxInfoPageDto.Data();
                data.setPage(1);
                data.setLimit(1000);
                data.setAcctClusterIds(Arrays.asList(accountGroupId));
                data.setExcludeCloseWx(false);
                pageDto.setParams(data);

                // 分页查询账号信息
                int page = 1;
                while (true) {
                    Result<SelectWxInfoPageRo> selectWxInfoPageRoResult = Urls.selectWxInfoPage(pageDto);
                    if (selectWxInfoPageRoResult.isSuccess()) {
                        accountList.addAll(selectWxInfoPageRoResult.getData().getRecords());
                        if (selectWxInfoPageRoResult.getData().getPages() <= page++) {
                            break;
                        }
                        data.setPage(page);
                        continue;
                    }
                    throw new BusinessException(
                            "定时任务更新商户下机器人信息失败->查询商户下有效个人号失败,错误消息:" + selectWxInfoPageRoResult.getMessage());
                }

                // 微信账号信息放入Map
                Map<String, SelectWxInfoPageRo.Data> wxAccountInfoMap = new HashMap<>();
                for (SelectWxInfoPageRo.Data wxInfo : accountList) {
                    wxAccountInfoMap.put(wxInfo.getWxId(), wxInfo);
                }
                // 查询所有正常状态的水军信息
                List<RobotInfo> enableRobotList = this.robotInfoMapper.queryRobotListAll();
                Map<String, RobotInfo> robotInfoMap = enableRobotList.stream()
                        .collect(Collectors.toMap(RobotInfo::getWxId, Function.identity()));

                // ---------循环有效个人号,将需要插入、修改、删除的进行分组
                List<String> wxIdInsertList = new ArrayList<>();
                List<RobotInfo> wxIdUpdateList = new ArrayList<>();
                for (Map.Entry<String, SelectWxInfoPageRo.Data> dataEntry : wxAccountInfoMap.entrySet()) {
                    RobotInfo robotInfoDb = robotInfoMap.get(dataEntry.getKey());
                    if (robotInfoDb == null) {
                        wxIdInsertList.add(dataEntry.getKey());
                    } else {
                        wxIdUpdateList.add(robotInfoDb);
                    }
                }

                // 生成updateBatchId
                long updateBatchId = System.currentTimeMillis();

                // ---------循环需要新增的微信集合进行新增
                List<RobotInfo> robotInfoInsert = new ArrayList<>(wxIdInsertList.size());
                // 1.包装插入集合
                for (String wxId : wxIdInsertList) {
                    SelectWxInfoPageRo.Data wxInfo = wxAccountInfoMap.get(wxId);
                    if (wxInfo == null) {
                        AlertContext.robot().alert("定时任务更新商户下机器人->通过微信Id(" + wxId + ")查询不到对应的微信");
                        continue;
                    }
                    RobotInfo robotInfo = new RobotInfo();
                    robotInfo.setWxId(wxInfo.getWxId());
                    robotInfo.setHeadImage(wxInfo.getWxImgUrl());
                    robotInfo.setMerchantId(merchatId);
                    robotInfo.setWxAcc(wxInfo.getWxAcc());
                    robotInfo.setWxNick(wxInfo.getWxNick());
                    robotInfo.setLoginState(StringUtils.equals(wxInfo.getIsClose(), "1") ? 3
                            : StringUtils.equals(wxInfo.getIsOnline(), "1") ? 1 : 2);
                    robotInfo.setUpdateBatchId(updateBatchId);
                    robotInfoInsert.add(robotInfo);
                }
                // 2.进行批量插入
                if (!CollectionUtils.isEmpty(robotInfoInsert)) {
                    this.robotInfoMapper.batchInsert(robotInfoInsert);
                }
                robotInfoInsert = null;
                wxIdInsertList = null;

                // 需要删除朋友关系的微信Id
                List<String> deleteFriendWxIdList = new ArrayList<>();
                // 由正常变为封号的微信Id
                List<String> sealWxIdList = new ArrayList<>();
                // ---------循环修改水军信息
                for (RobotInfo robotInfo : wxIdUpdateList) {
                    SelectWxInfoPageRo.Data wxInfo = wxAccountInfoMap.get(robotInfo.getWxId());
                    if (wxInfo == null) {
                        AlertContext.robot()
                                .alert("定时任务更新商户下机器人->通过微信Id(" + robotInfo.getWxId() + ")查询不到对应的简单信息或详细信息");
                        continue;
                    }

                    // 由正常状态变为封号状态,需要删除朋友关系
                    if (robotInfo.getLoginState() != 3 && StringUtils.equals(wxInfo.getIsClose(), "1")) {
                        sealWxIdList.add(robotInfo.getWxId());// 需要封号的微信Id
                        deleteFriendWxIdList.add(robotInfo.getWxId());// 需要删除好友关系的微信Id
                    }

                    RobotInfo robotInfoUpdate = new RobotInfo();
                    robotInfoUpdate.setRobotId(robotInfo.getRobotId());
                    robotInfoUpdate.setHeadImage(wxInfo.getWxImgUrl());
                    robotInfoUpdate.setMerchantId(merchatId);
                    robotInfoUpdate.setWxAcc(wxInfo.getWxAcc());
                    robotInfoUpdate.setWxNick(wxInfo.getWxNick());
                    robotInfoUpdate.setLoginState(StringUtils.equals(wxInfo.getIsClose(), "1") ? 3
                            : StringUtils.equals(wxInfo.getIsOnline(), "1") ? 1 : 2);
                    robotInfoUpdate.setUpdateBatchId(updateBatchId);
                    this.robotInfoMapper.updateByPrimaryKeySelective(robotInfoUpdate);
                }
                wxIdUpdateList = null;

                // -------------------删除不属于此商家的对应的微信机器人信息
                List<String> wxIdDeleteList = new ArrayList<>(robotInfoMap.keySet());
                wxIdDeleteList.removeAll(wxAccountInfoMap.keySet());
                if (!CollectionUtils.isEmpty(wxIdDeleteList)) {
                    this.robotInfoMapper.deleteByWxIds(wxIdDeleteList);
                    deleteFriendWxIdList.addAll(wxIdDeleteList);// 需要删除好友关系的微信Id
                }
                // -------------------维护好友关系,主要是封号或者被删除的微信号
                // 1.找出每个主加账号需要扣减的好友数量(好友被删或被封)
                if (!CollectionUtils.isEmpty(deleteFriendWxIdList)) {
                    List<FriendRelation> friendRelationList = this.friendRelationMapper
                            .findRelationListByWxIds(deleteFriendWxIdList);
                    Map<String, Integer> wxDecrNumMap = new HashMap<>();// 微信账号需要扣减的好友数量Map
                    friendRelationList.forEach(x -> {
                        String wxId = deleteFriendWxIdList.contains(x.getFromWxId()) ? x.getFromWxId()
                                : x.getToWxId();
                        Integer wxQty = wxDecrNumMap.get(wxId);
                        wxDecrNumMap.put(wxId, wxQty == null ? 1 : wxQty + 1);
                    });
                    // 2.循环扣减主加账号的好友数量
                    for (Map.Entry<String, Integer> entry : wxDecrNumMap.entrySet()) {
                        this.robotInfoMapper.decrFriendNum(entry.getKey(), entry.getValue());
                    }
                    // 3.统一删除好友关系列表
                    this.friendRelationMapper.deleteByFromWxIds(deleteFriendWxIdList);
                    this.friendRelationMapper.deleteByToWxIds(deleteFriendWxIdList);
                }
                groupInfoService.sealRobot(sealWxIdList);// 执行封号逻辑
            } catch (BusinessException ex) {
                Logs.e(getClass(), ex.getMessage());
                AlertContext.robot().alert("定时任务更新商户下机器人信息失败");
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            Logs.e(getClass(), "结束机器人账号信息更新定时任务");
        });
    }

    @Override
    public void initData() {
        User user = UserThreadLocal.get();
        MerchatWxDTO dto = new MerchatWxDTO();
        dto.setMerchatId(user.getMerchatId());
        dto.setIdentity(identity);
        Result<List<String>> result = Urls.getMerchantAvailableWx(dto);
        if (result.getCode() == 0) {
            List<String> numbers = result.getData();
            for (String num : numbers) {
                PersonalWxInfoDTO p = new PersonalWxInfoDTO();
                p.setIdentity(identity);
                p.setWxIds(Lists.newArrayList(num));
                PersonalSimpleInfoVO vo = Urls.queryPersonalSimpleInfo(p).getData().get(0);
                RobotInfo record = new RobotInfo();
                record.setFriendNum(0);
                record.setHeadImage(vo.getWxImgUrl());
                record.setMerchantId(user.getMerchatId());
                record.setState(0);
                record.setWxAcc(vo.getWxAcc());
                record.setWxId(vo.getWxId());
                record.setWxNick(vo.getWxNick());
                robotInfoMapper.insert(record);
                System.err.println(vo);
            }
        }
    }

    @Override
    public void robotQuitGroupJob() {
        Logs.e(getClass(), "进入退群定时任务");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                robotQuitGroup();
            }
        };
        AsyncTask.execute(runnable);

    }

    private void robotQuitGroup() {
        // 查询待退群的水军信息
        List<RobotGroupRelation> robotGroupRelations = this.robotGroupRelationMapper.selectWaitGroup();
        if (robotGroupRelations == null || robotGroupRelations.size() == 0) {
            return;
        }
        for (RobotGroupRelation robotGroupRelation : robotGroupRelations) {
            RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(robotGroupRelation.getRobotWxId());
            MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
            baseGpDTO.setIdentity(identity);
            baseGpDTO.setMerchatId(robotInfo.getMerchantId());
            baseGpDTO.setWxId(robotInfo.getWxId());
            baseGpDTO.setVcGroupId(robotGroupRelation.getWxGroupId());
            Logs.e(getClass(), "机器人退群请求参数" + JSON.toJSONString(baseGpDTO));
            // 调用主动退群接口
            Result<BooleanResultVo> resultVoResult = Urls.deleteAndLeaveGroup(baseGpDTO);
            Logs.e(getClass(), "机器人退群返回值" + JSON.toJSONString(resultVoResult));
            if (resultVoResult.getCode() != 0) {
                Logs.e(getClass(), "机器人退群失败" + JSON.toJSONString(resultVoResult));
                continue;
            }
            // 修改状态为已退群 更新退群时间
            robotGroupRelation.setOutGroupTime(new Date());
            robotGroupRelation.setState(2);
            robotGroupRelationMapper.updateByPrimaryKeySelective(robotGroupRelation);
            // 水军在群数量减少
            robotInfoMapper.updateGroupNum(-1, robotInfo.getRobotId());
            // 已购水军数量减少
            groupInfoMapper.updateRobotNum(robotGroupRelation.getWxGroupId(), -1);
        }
        Logs.e(getClass(), "任务执行完成！");

    }

    @Override
    public void custConsumeHandle() {
        Logs.e(getClass(), "进入用户每日群消费处理定时任务");
        AsyncTask.execute(() -> {
            try {
                // 查询所有有效的任务
                List<TaskInfo> taskInfoList = taskInfoMapper.queryAllList();
                // 将任务和商家信息映射
                Map<Integer, String> taskCustMap = new HashMap<>();
                taskInfoList.forEach(t -> {
                    taskCustMap.put(t.getTaskId(), t.getCreateId());
                });
                // 查询正在使用、待续费的群信息
                List<GroupInfo> groupList = this.groupInfoMapper.selectListByStates(
                        Arrays.asList(GroupStatusEnum.ACTIVE.getKey(), GroupStatusEnum.STOP.getKey()));
                // 正在使用、待续费群分组
                List<GroupInfo> useGroupList = new ArrayList<>();
                List<GroupInfo> pauseGroupList = new ArrayList<>();
                for (GroupInfo groupInfo : groupList) {
                    if (groupInfo.getState() == GroupStatusEnum.ACTIVE.getKey())
                        useGroupList.add(groupInfo);
                    else
                        pauseGroupList.add(groupInfo);
                }
                // 将超过3天未续费的群信息进行停止服务
                if (!CollectionUtils.isEmpty(pauseGroupList)) {
                    List<Integer> stopGroupId = new ArrayList<>();
                    try {
                        for (GroupInfo groupInfo : pauseGroupList) {
                            if (Times.isMoreThanHours(groupInfo.getPauseTime(), new Date(), 72)) {
                                stopGroupId.add(groupInfo.getGroupId());// 暂停服务间隔超过72小时需要停止服务
                            }
                        }
                        Result<Void> result = groupInfoService.stopGroup(stopGroupId);
                        if (!result.isSuccess()) {
                            throw new BusinessException("超过3天未续费停止群服务失败,原因:" + result.getMessage());
                        }
                    } catch (Exception ex) {
                        Logs.e(getClass(), ex.getMessage(), ex);
                        AlertContext.robot()
                                .alert("超过3天未续费停止群服务失败,stopGroupId:" + JSON.toJSONString(stopGroupId));
                    }
                }
                // 查询机器人每天需要的比邻币信息
                FireGroupConfig fireGroupConfig = this.fireGroupConfigService.selectFireGroupConfig();
                if (fireGroupConfig == null) {
                    throw new BusinessException("查不到系统配置信息,无法计算比邻币扣除信息");
                }

                // 将各个商家群信息分组
                Map<String, List<GroupInfo>> custGroupListMap = new HashMap<>();
                for (GroupInfo groupInfo : useGroupList) {
                    String custId = taskCustMap.get(groupInfo.getTaskId());
                    if (custId == null) {
                        String errorMsg = String.format("找不到群(%s)对应的商家信息,无法进行扣费操作", groupInfo.getGroupId());
                        Logs.e(getClass(), errorMsg);
                        AlertContext.robot().alert(errorMsg);
                        continue;
                    }
                    List<GroupInfo> groupInfoList = custGroupListMap.get(custId);
                    if (groupInfoList == null) {
                        groupInfoList = new ArrayList<>();
                        custGroupListMap.put(custId, groupInfoList);
                    }
                    groupInfoList.add(groupInfo);
                }

                // ------------对每个商家进行扣费
                for (Map.Entry<String, List<GroupInfo>> entry : custGroupListMap.entrySet()) {
                    decrCustBalance(entry.getKey(), entry.getValue(), fireGroupConfig.getRobotDayMoney());
                }
            } catch (Exception e) {
                Logs.e(getClass(), e.getMessage(), e);
                AlertContext.robot().alert("用户每日群消费处理定时任务失败");
            }
            Logs.e(getClass(), "结束用户每日群消费处理定时任务");
        });
    }

    // 扣除商家比邻币
    private void decrCustBalance(String custId, List<GroupInfo> groupInfoList, BigDecimal dayBalance) {
        try {
            // 查询商家比邻币数量
            MerchatAccDTO balanceRequest = new MerchatAccDTO();
            balanceRequest.setMerchatId(custId);
            Result<MerchatAccDTO> result = Urls.getMerchatAcc(balanceRequest);
            if (!result.isSuccess()) {
                throw new BusinessException(result.getMessage());
            }
            Double accBalance = result.getData().getAccBalance();// 用户余额
            int robotNum = 0;
            for (GroupInfo groupInfo : groupInfoList) {
                // 查询当前群组已有多少水军数量
                Integer robotCount = robotGroupRelationMapper
                        .selectRobotCountByGroupId(groupInfo.getWxGroupId());
                robotNum = robotNum + robotCount;

            }
            if (robotNum == 0) {
                return;
            }
            BigDecimal decrMoney = dayBalance.multiply(new BigDecimal(robotNum));
            // 比对商家金额是否足够扣减
            if (!(decrMoney.compareTo(new BigDecimal(accBalance)) == 1)) {
                // 足够扣减
                DeductMercBalanceInputDTO inputDTO = new DeductMercBalanceInputDTO();
                inputDTO.setAmount(decrMoney.doubleValue());
                inputDTO.setBusiType("SBT_002");
                inputDTO.setMerchantId(custId);
                inputDTO.setRemarks("暖群包水军定时扣费");
                Result<String> deductResult = Urls.deductMercBalance(inputDTO);
                if (deductResult.isSuccess()) {
                    Logs.e(getClass(), "商家[" + custId + "]扣除费用:" + decrMoney.doubleValue() + ",群集合信息:"
                            + JSONObject.toJSONString(groupInfoList));
                    return;
                } else if (deductResult.getCode() != 10002) {
                    Logs.e(getClass(), "商家[" + custId + "]余额不足,无法进行扣费");
                } else {
                    throw new BusinessException(
                            "请求扣费[" + decrMoney.doubleValue() + "]失败,返回:" + deductResult.getMessage());
                }
            }
            // 扣减失败或余额不足,将拥有的群信息改为待续费状态...待优化
            List<Integer> groupIdList = groupInfoList.stream().map(g -> g.getGroupId())
                    .collect(Collectors.toList());
            groupInfoMapper.updatePauseGroups(groupIdList, Times.getFirstDate(new Date()),
                    GroupStatusEnum.STOP.getKey());
        } catch (Exception ex) {
            String errorMsg = "商家(" + custId + ")水军费用扣除失败,群集合:" + JSONObject.toJSONString(groupInfoList);
            Logs.e(getClass(), errorMsg, ex);
            AlertContext.robot().alert(errorMsg);
        }
    }
}
