package com.uc.firegroup.service.strategy;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.InputDTO;
import com.uc.external.bilin.req.InputDTO.Data;
import com.uc.external.bilin.req.MerchatWxDTO;
import com.uc.external.bilin.res.IsGroupMembersVo;
import com.uc.firegroup.api.IBilinService;
import com.uc.firegroup.api.pojo.GroupInfo;
import com.uc.firegroup.api.pojo.PlayRobotConfig;
import com.uc.firegroup.api.pojo.PushLog;
import com.uc.firegroup.api.pojo.PushTask;
import com.uc.firegroup.api.pojo.RobotGroupRelation;
import com.uc.firegroup.api.pojo.TaskInfo;
import com.uc.firegroup.service.mapper.GroupInfoMapper;
import com.uc.firegroup.service.mapper.PushLogMapper;
import com.uc.firegroup.service.mapper.RobotGroupRelationMapper;
import com.uc.firegroup.service.mapper.TaskInfoMapper;
import com.uc.framework.App;
import com.uc.framework.Pair;
import com.uc.framework.Randoms;
import com.uc.framework.Times;
import com.uc.framework.logger.Logs;
import com.uc.framework.logger.alert.AlertContext;
import com.uc.framework.obj.Result;

@Component
public class PushTaskHandleStrategy {
    @Autowired
    private RobotGroupRelationMapper robotGroupRelationMapper;

    @Autowired
    PushLogMapper pushLogMapper;
    @Autowired
    GroupInfoMapper groupInfoMapper;
    @Autowired
    TaskInfoMapper taskInfoMapper;
    @Value(value = "${identity}")
    private String identity;
    @Autowired
    IBilinService bilinService;
    @Value(value = "${merchatId}")
    String merchatId;

    public Result<?> validate(String groupId, PushTask task) {
        // 查询最近一条log日志
        PushLog log = App.getBean(PushLogMapper.class).selectLastLog(groupId, task.getPlayId());
        if (log == null) {
            return Result.ok();
        }
        // 1.判断与上条消息 间隔 是否 大于 指定间隔时间
        if (!Times.isMoreThanSeconds(log.getCreateTime(), new Date(),
                task.getPlayMessage().getIntervalTime())) {
            // 间隔时间不够
            return Result.err();
        }
        return Result.ok();
    }

    /***
     * 
     * title: 分配水军
     *
     * @param groupId 群id
     * @param config 发言人设置
     * @param task 任务
     * @return <个人号wxId,商户id>
     * @author HadLuo 2020-9-17 15:06:29
     */
    public Map<String, String> allocateRobot(String groupId, PlayRobotConfig currentRobot, PushTask task) {
        Map<String, String> mapper = Maps.newHashMap();
        String errMessage = "";
        String sendPersonWxId = null;
        String _mecahrtId = task.getMerchatId();
        // 水军号 离线 时 的微信id
        String errorWxId = "";
        // 账号来源:1.水军 2.个人号
        String accSource = "1";
        // 查询群里面的水军
        // 发言人设置类型 1指定个人号 2随机个人号 3 随机水军号
        if (currentRobot.getRobotConfigType() == 1) {
            // 指定个人号
            sendPersonWxId = currentRobot.getClearWxId();
            accSource = "1";
        } else if (currentRobot.getRobotConfigType() == 2) {
            accSource = "2";
        } else if (currentRobot.getRobotConfigType() == 3) {
            // 水军号 要取我们自己的商户id
            _mecahrtId = merchatId;
            accSource = "1";
        }
        List<String> excutions = null ;
        if (StringUtils.isEmpty(sendPersonWxId)) {
            // 查询 这个人 已经发言过的 wxId号
            PushLog old = pushLogMapper.selectLogByNickName(groupId, task.getPlayId(),
                    currentRobot.getRobotNickname());
            if (old != null && (old.getPushState() == 0||old.getPushState() == 2) && !StringUtils.isEmpty(old.getRobotWxId())) {
                sendPersonWxId = old.getRobotWxId();
            } else {
                PushLog record = new PushLog();
                record.setGroupId(groupId);
                record.setPlayId(task.getPlayId());
                List<PushLog> logs = pushLogMapper.select(record);
                 excutions = Lists.newArrayList();
                if (!CollectionUtils.isEmpty(logs)) {
                    for (PushLog log : logs) {
                        if (!log.getPersonName().equals(currentRobot.getRobotNickname())) {
                            excutions.add(log.getRobotWxId());
                        }
                    }
                }
                // 随机 个人号或者 水军号，但是要排除 已经发言了的
                if (currentRobot.getRobotConfigType() == 2) {
                    // 随机个人号
                    sendPersonWxId = Randoms.random(selectAllPersonWxids(groupId), excutions);
                } else if (currentRobot.getRobotConfigType() == 3) {
                    // 随机水军号
                    sendPersonWxId = Randoms.random(selectAllRobotsWxids(groupId), excutions);
                }
            }
        }
        if (!StringUtils.isEmpty(sendPersonWxId)) {
            // 判断 此号 是否可用
            Pair<Boolean, Object> pair = bilinService.robotHasEnable(sendPersonWxId);
            if (!pair.getLeft() || pair.getRight() == null) {
                errorWxId = sendPersonWxId;
                // 号不可用
                sendPersonWxId = null;
                errMessage = "水军或个人号离线或者被冻结";
            }
        } else {
            errMessage = "水军或个人号不足";
        }
        if (StringUtils.isEmpty(sendPersonWxId) && !StringUtils.isEmpty(currentRobot.getBackupWxId())) {
            // 设置了备用号 ， 读取 备用号
            sendPersonWxId = selectBackUpWxId(currentRobot,excutions);
            if (StringUtils.isEmpty(sendPersonWxId)) {
                errMessage = "备用号离线或者被冻结";
                errorWxId = sendPersonWxId;
            }
        }
        if (StringUtils.isEmpty(sendPersonWxId)) {
            // 没有号可用了
            // 插入log 日志
            PushLog log = new PushLog();
            log.setCreateTime(new Date());
            log.setGroupId(groupId);
            log.setPlayId(task.getPlayId());
            log.setPlayMessageId(task.getPlayMessageId());
            log.setPushErrorMsg(errMessage);
            log.setPushState(1);
            log.setRobotWxId(errorWxId);
            log.setPersonName(currentRobot.getRobotNickname());
            log.setMerchatId(_mecahrtId);
            int ret = pushLogMapper.insert(log);
            if (ret <= 0) {
                Logs.e(getClass(), "[t_push_log表插入失败]>>" + JSON.toJSONString(log));
                AlertContext.robot().alert("[t_push_log表插入失败]>>" + JSON.toJSONString(log));
            }
            Logs.e(getClass(), "[消息发送失败(" + errMessage + ")]>> wxGroupId=" + groupId + ",task="
                    + JSON.toJSONString(task) + ",logId=" + ret);
        }
        mapper.put("sendPersonWxId", sendPersonWxId);
        mapper.put("_mecahrtId", _mecahrtId);
        mapper.put("errMessage", errMessage);
        mapper.put("accSource", accSource);
        mapper.put("errorWxId", errorWxId);
        return mapper;
    }

    /**
     * 
     * title: 查询 群里面所有的个人号
     *
     * @param groupId
     * @return
     * @author HadLuo 2020-9-17 15:40:15
     */
    public List<String> selectAllPersonWxids(String groupId) {
        // 根据微信群ID查询群信息
        GroupInfo groupInfo = groupInfoMapper.selectInfoByWxGroupId(groupId);
        if (groupInfo == null) {
            Logs.e(getClass(), "[查询群信息失败]>>groupId=" + groupId);
            return Collections.emptyList();
        }
        // 根据群信息查询所属商户
        TaskInfo taskInfo = taskInfoMapper.selectByPrimaryKey(groupInfo.getTaskId());
        if (taskInfo == null) {
            Logs.e(getClass(), "[查询任务信息失败]>>TaskId=" + groupInfo.getTaskId());
            return Collections.emptyList();
        }
        MerchatWxDTO dto = new MerchatWxDTO();
        dto.setIdentity(identity);
        dto.setMerchatId(taskInfo.getCreateId());
        Result<List<String>> r = Urls.getMerchantAvailableWx(dto);
        List<String> use = Lists.newArrayList();
        if (StringUtils.isEmpty(r.getData())) {
            return use;
        }
        for (String wxId : r.getData()) {
            InputDTO in = new InputDTO();
            in.setIdentity(identity);
            InputDTO.Data p = new Data();
            p.setWxIds(Lists.newArrayList(wxId));
            p.setVcGroupIds(Lists.newArrayList(groupId));
            in.setParams(p);
            Result<List<IsGroupMembersVo>> rv = Urls.isGroupMembers(in);
            if (rv.isSuccess() && !CollectionUtils.isEmpty(rv.getData())) {
                use.add(wxId);
                System.err.println("群添加: >>wxId=" + wxId + ",groupId=" + groupId);
            } else {
                System.err.println("群过滤: >>wxId=" + wxId + ",groupId=" + groupId);
            }
        }
        return use;
    }

    /**
     * 
     * title: 查询群里面 的水军号
     *
     * @param groupId
     * @return
     * @author HadLuo 2020-9-17 15:57:39
     */
    public List<String> selectAllRobotsWxids(String groupId) {
        RobotGroupRelation record = new RobotGroupRelation();
        record.setWxGroupId(groupId);
        record.setState(1);
        record.setIsDelete(0);
        List<RobotGroupRelation> robots = robotGroupRelationMapper.select(record);
        List<String> wxIds = Lists.newArrayList();
        for (RobotGroupRelation r : robots) {
            wxIds.add(r.getRobotWxId());
        }
        return wxIds;
    }

    /**
     * 
     * title: 查询备用号
     *
     * @param config
     * @return
     * @author HadLuo 2020-9-17 16:12:11
     */
    private String selectBackUpWxId(PlayRobotConfig config,List<String> excutions) {
        if (StringUtils.isEmpty(config.getBackupWxId())) {
            return null;
        }
        String[] wxIds = config.getBackupWxId().split(",");
        // 查看 备用号 是否 有用
        for (String wxId : wxIds) {
            if(excutions != null && excutions.contains(wxId)) {
                continue ;
            }
            if (bilinService.robotHasEnable(wxId).getLeft()) {
                return wxId;
            }
        }
        return null;
    }

}
