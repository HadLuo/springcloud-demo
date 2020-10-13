package com.uc.firegroup.service.inner;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.pojo.PlayMessage;
import com.uc.firegroup.api.pojo.PlayRobotConfig;
import com.uc.firegroup.api.pojo.PushTask;
import com.uc.firegroup.service.mapper.PlayMessageMapper;
import com.uc.firegroup.service.mapper.PlayRobotConfigMapper;
import com.uc.firegroup.service.mapper.PushTaskMapper;
import com.uc.framework.App;
import com.uc.framework.Constants;
import com.uc.framework.collection.ListTools;
import com.uc.framework.logger.Logs;
import com.uc.framework.logger.alert.AlertContext;
import com.uc.framework.obj.BusinessException;
import com.uc.framework.obj.Result;

@Service
public class PushTaskService {
    /***
     * 
     * title: 剧本修改时 ，更新任务
     *
     * @param playInfo
     * @param msg
     * @param oldPlayMessageId
     * @param newPlayMessageId
     * @param configs
     * @param merchatId
     * @author HadLuo 2020-9-29 11:32:49
     */
    public void updatePushTask(PlayInfo playInfo, String merchatId) {
        PushTask record = new PushTask();
        record.setPlayId(playInfo.getPlayId());
        // 查询老的
        List<PushTask> tasks = App.getBean(PushTaskMapper.class).select(record);
        if (!CollectionUtils.isEmpty(tasks)) {
            for (PushTask task : tasks) {
                App.getBean(PushTaskMapper.class).deleteByPrimaryKey(task.getId());
            }
        }
        PlayMessage p = new PlayMessage();
        p.setPlayId(playInfo.getPlayId());
        List<PlayMessage> msgs = App.getBean(PlayMessageMapper.class).select(p);
        // 查询最新的config
        List<PlayRobotConfig> configs = App.getBean(PlayRobotConfigMapper.class)
                .selectListByPlayId(playInfo.getPlayId());
        for (PlayMessage m : msgs) {
            // 重新创建
            createPushTask(playInfo, m, configs, merchatId);
        }
    }

    /***
     * 
     * title: 创建推送任务 ， 创建剧本的 时候 会调用(定时，话术都会 调用)
     *
     * @param playInfo
     * @param msg
     * @param configs
     * @return
     * @author HadLuo 2020-9-18 16:48:44
     */
    public Result<?> createPushTask(PlayInfo playInfo, PlayMessage msg, List<PlayRobotConfig> configs,
            String merchatId) {
        if (StringUtils.isEmpty(playInfo.getPushGroupIds())) {
            throw new BusinessException("微信id为空");
        }
        PushTask record = new PushTask();
        record.setPlayId(playInfo.getPlayId());
        record.setPlayMessageId(msg.getPlayMessageId());
        // 查询老的
        PushTask old = App.getBean(PushTaskMapper.class).selectOne(record);
        // 查找对应的发言人
        PlayRobotConfig currentRobot = ListTools.selectOne(configs, msg.getRobotNickname(),
                (item) -> item.getRobotNickname());
        if (old == null) {
            PushTask task = new PushTask();
            task.setPlayId(playInfo.getPlayId());
            task.setCreateTime(new Date());
            task.setGroupSize(playInfo.getGroupNum());
            task.setRobots(JSON.toJSONString(currentRobot));
            task.setRobotsSize(configs.size());
            task.setWxGroupId(
                    org.apache.commons.lang.StringUtils.join(new HashSet<>(playInfo.getPushGroupIds()), ","));
            task.setIsDelete(0);
            task.setPlayMessageId(msg.getPlayMessageId());
            task.setMerchatId(merchatId);
            int ret = App.getBean(PushTaskMapper.class).insert(task);
            if (ret <= 0) {
                AlertContext.robot()
                        .alert("t_push_task表插入数据失败,ret=" + ret + ",bo=" + JSON.toJSONString(task));
                return Result.err(Constants.ServerErrorHint);
            }
        } else {
            // 修改 群
            old.setRobots(JSON.toJSONString(currentRobot));
            old.setGroupSize(playInfo.getGroupNum());
            old.setRobotsSize(configs.size());
            old.setMerchatId(merchatId);
            old.setWxGroupId(
                    org.apache.commons.lang.StringUtils.join(new HashSet<>(playInfo.getPushGroupIds()), ","));
            int ret = App.getBean(PushTaskMapper.class).updateByPrimaryKey(old);
            if (ret <= 0) {
                AlertContext.robot().alert("t_push_task表更新数据失败,ret=" + ret + ",bo=" + JSON.toJSONString(old));
                return Result.err(Constants.ServerErrorHint);
            }
        }

        Logs.e(getClass(),
                "[创建推送任务(createPushTask)]>>playInfo=" + JSON.toJSONString(playInfo) + ",msg="
                        + JSON.toJSONString(msg) + ",configs=" + JSON.toJSONString(configs) + ",merchatId="
                        + merchatId);
        return Result.ok();
    }
}
