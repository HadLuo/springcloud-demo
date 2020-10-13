//package com.uc.firegroup.service.inner.push;
//
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.util.StringUtils;
//import com.alibaba.fastjson.JSON;
//import com.google.common.collect.Lists;
//import com.uc.external.bilin.Urls;
//import com.uc.external.bilin.req.SendGroupChatMsgDTO;
//import com.uc.external.bilin.req.SendGroupChatMsgDTO.Data;
//import com.uc.external.bilin.res.BooleanResultVo;
//import com.uc.firegroup.api.pojo.GroupInfo;
//import com.uc.firegroup.api.pojo.PlayInfo;
//import com.uc.firegroup.api.pojo.PlayMessage;
//import com.uc.firegroup.api.pojo.PlayRobotConfig;
//import com.uc.firegroup.api.pojo.PushLog;
//import com.uc.firegroup.api.pojo.PushTask;
//import com.uc.firegroup.api.pojo.PlayMessage.ContentJson;
//import com.uc.firegroup.service.config.Env;
//import com.uc.firegroup.service.inner.chat.AllocateWxIdStrategy;
//import com.uc.firegroup.service.mapper.GroupInfoMapper;
//import com.uc.firegroup.service.mapper.PlayInfoMapper;
//import com.uc.firegroup.service.mapper.PlayMessageMapper;
//import com.uc.firegroup.service.mapper.PushLogMapper;
//import com.uc.firegroup.service.mapper.PushTaskMapper;
//import com.uc.firegroup.service.redis.AdaminPushOpRedisModel;
//import com.uc.firegroup.service.redis.GroupPushCounter;
//import com.uc.firegroup.service.redis.SpeakedSortRedisModel;
//import com.uc.firegroup.service.redis.SysPushOpRedisModel;
//import com.uc.firegroup.service.tools.EventType;
//import com.uc.firegroup.service.tools.event.AppEventProcessor;
//import com.uc.framework.App;
//import com.uc.framework.Pair;
//import com.uc.framework.logger.Logs;
//import com.uc.framework.logger.alert.AlertContext;
//import com.uc.framework.obj.Result;
//import com.uc.framework.redis.queue.MessageListener;
//import com.uc.framework.redis.queue.DelayQueue;
//import com.uc.framework.redis.queue.DelayQueueManager;
//import com.uc.framework.redis.queue.Task;
//
///***
// * 
// * title: push 实现 抽象类
// *
// * @author HadLuo
// * @date 2020-9-19 16:03:37
// */
//public abstract class AbstractPushTaskProcessor implements MessageListener, DisposableBean, InitializingBean {
//
//    /** redis 延时队列 */
//    DelayQueue delayQueue;
//    /** 推送控制器 */
//    final PushController controller = new DefaultPushController();
//
//    /***
//     * 
//     * title: 接受 push任务
//     *
//     * @param param
//     * @author HadLuo 2020-9-19 16:03:49
//     */
//    public abstract void onAccept(Object param);
//
//    /**
//     * 
//     * title: 延时队列的 构造key
//     *
//     * @return
//     * @author HadLuo 2020-9-19 16:09:20
//     */
//    public abstract String createDelayQueueKey();
//
//    @Override
//    public void onMessage(Task task) {
//        if (task == null || task.getData() == null) {
//            return;
//        }
//        onKafkaMsgReceive((PushTask) task.getData());
//        // 正式环境 扔到kafka
//        // App.getBean(PollMsgForRedisProducer.class).send(task);
//    }
//
//    /**
//     * 
//     * title: 获取 延时队列
//     *
//     * @return
//     * @author HadLuo 2020-9-19 16:09:34
//     */
//    public synchronized DelayQueue makeDelayQueue() {
//        return App.getBean(DelayQueueManager.class).getQueue(createDelayQueueKey(), this);
//    }
////
////    /***
////     * 
////     * title: 剧本修改时 ，更新任务
////     *
////     * @param playInfo
////     * @param msg
////     * @param oldPlayMessageId
////     * @param newPlayMessageId
////     * @param configs
////     * @param merchatId
////     * @author HadLuo 2020-9-29 11:32:49
////     */
////    public void updatePushTask(PlayInfo playInfo, String merchatId) {
////        PushTask record = new PushTask();
////        record.setPlayId(playInfo.getPlayId());
////        // 查询老的
////        List<PushTask> tasks = App.getBean(PushTaskMapper.class).select(record);
////        if (!CollectionUtils.isEmpty(tasks)) {
////            for (PushTask task : tasks) {
////                App.getBean(PushTaskMapper.class).deleteByPrimaryKey(task.getId());
////            }
////        }
////        PlayMessage p = new PlayMessage();
////        p.setPlayId(playInfo.getPlayId());
////        List<PlayMessage> msgs = App.getBean(PlayMessageMapper.class).select(p);
////        // 查询最新的config
////        List<PlayRobotConfig> configs = App.getBean(PlayRobotConfigMapper.class)
////                .selectListByPlayId(playInfo.getPlayId());
////        for (PlayMessage m : msgs) {
////            // 重新创建
////            createPushTask(playInfo, m, configs, merchatId);
////        }
////    }
////
////    /***
////     * 
////     * title: 创建推送任务 ， 创建剧本的 时候 会调用(定时，话术都会 调用)
////     *
////     * @param playInfo
////     * @param msg
////     * @param configs
////     * @return
////     * @author HadLuo 2020-9-18 16:48:44
////     */
////    public Result<?> createPushTask(PlayInfo playInfo, PlayMessage msg, List<PlayRobotConfig> configs,
////            String merchatId) {
////        if (StringUtils.isEmpty(playInfo.getPushGroupIds())) {
////            throw new BusinessException("微信id为空");
////        }
////        PushTask record = new PushTask();
////        record.setPlayId(playInfo.getPlayId());
////        record.setPlayMessageId(msg.getPlayMessageId());
////        // 查询老的
////        PushTask old = App.getBean(PushTaskMapper.class).selectOne(record);
////        // 查找对应的发言人
////        PlayRobotConfig currentRobot = ListTools.selectOne(configs, msg.getRobotNickname(),
////                (item) -> item.getRobotNickname());
////        if (old == null) {
////            PushTask task = new PushTask();
////            task.setPlayId(playInfo.getPlayId());
////            task.setCreateTime(new Date());
////            task.setGroupSize(playInfo.getGroupNum());
////            task.setRobots(JSON.toJSONString(currentRobot));
////            task.setRobotsSize(configs.size());
////            task.setWxGroupId(org.apache.commons.lang.StringUtils.join(new HashSet<>(playInfo.getPushGroupIds()), ","));
////            task.setIsDelete(0);
////            task.setPlayMessageId(msg.getPlayMessageId());
////            task.setMerchatId(merchatId);
////            int ret = App.getBean(PushTaskMapper.class).insert(task);
////            if (ret <= 0) {
////                AlertContext.robot()
////                        .alert("t_push_task表插入数据失败,ret=" + ret + ",bo=" + JSON.toJSONString(task));
////                return Result.err(Constants.ServerErrorHint);
////            }
////        } else {
////            // 修改 群
////            old.setRobots(JSON.toJSONString(currentRobot));
////            old.setGroupSize(playInfo.getGroupNum());
////            old.setRobotsSize(configs.size());
////            old.setMerchatId(merchatId);
////            old.setWxGroupId(org.apache.commons.lang.StringUtils.join(new HashSet<>(playInfo.getPushGroupIds()), ","));
////            int ret = App.getBean(PushTaskMapper.class).updateByPrimaryKey(old);
////            if (ret <= 0) {
////                AlertContext.robot().alert("t_push_task表更新数据失败,ret=" + ret + ",bo=" + JSON.toJSONString(old));
////                return Result.err(Constants.ServerErrorHint);
////            }
////        }
////
////        Logs.e(getClass(),
////                "[创建推送任务(createPushTask)]>>playInfo=" + JSON.toJSONString(playInfo) + ",msg="
////                        + JSON.toJSONString(msg) + ",configs=" + JSON.toJSONString(configs) + ",merchatId="
////                        + merchatId);
////        return Result.ok();
////    }
//
//    /***
//     * 
//     * title: 有定时 推送消息来到 MQ 消费的
//     *
//     * @param msg
//     * @author HadLuo 2020-9-16 14:21:36
//     */
//    public void onKafkaMsgReceive(PushTask task) {
//        // 当前发言人
//        PlayRobotConfig currentRobot = task.getCurrentRobot();
//        Logs.e(getClass(), "[start push]>>task=" + JSON.toJSONString(task));
//        if (currentRobot == null || StringUtils.isEmpty(currentRobot.getRobotNickname())) {
//            return;
//        }
//        // 查询剧本 看剧本是否正常
//        PlayInfo play = App.getBean(PlayInfoMapper.class).selectByPrimaryKey(task.getPlayId());
//        // Result<?> r = controller.validatePlay(play);
//        // if (!r.successful()) {
//        // return;
//        // }
//        // 查询最新消息内容
//        PlayMessage playMessage = task.getPlayMessage();
//        task.setPlayMessage(playMessage);
//        if (playMessage == null) {
//            Logs.e(getClass(), "[push exception]>>PlayMessage被删除,id=" + task.getPlayMessageId());
//        }
//        // 获取要推送的群
//        List<String> groups = Lists.newArrayList();
//        if (play.getPlayType() == 1) {
//            // 定时剧本 ， 每个群都要推
//            groups.addAll(Arrays.asList(task.getWxGroupId().split(",")));
//        } else {
//            // 关键词触发 ， 只推送 触发的群
//            if (!StringUtils.isEmpty(task.getCurrentGroup())) {
//                groups.add(task.getCurrentGroup());
//            } else {
//                return;
//            }
//        }
//        // 遍历所有要推送的群
//        for (String groupId : groups) {
//            // 上一次发言到哪个位置了
//            int sort = SpeakedSortRedisModel.getSort(groupId, play.getPlayId());
//            if (sort != -1) {
//                // 这一次的发言位置 不等于上一次的 加1
//                if (task.getSort() != sort + 1) {
//                    continue;
//                }
//            }
//            // 群剧本 是否人工暂停
//            if (AdaminPushOpRedisModel.isPause(groupId, play.getPlayId())) {
//                continue;
//            }
//            // 是否是系统暂停
//            if (SysPushOpRedisModel.isPause(groupId, play.getPlayId())) {
//                continue;
//            }
//            // 开始推消息 事件
//            AppEventProcessor.sendEvent(EventType.startPush, groupId, play.getPlayId());
//            // 实际分配 发送 号
//            Map<String, String> mapper = AllocateWxIdStrategy.allocateRobot(groupId, currentRobot, task);
//            String wxId = (String) mapper.get("sendPersonWxId");
//            String errorWxId = (String) mapper.get("errorWxId");
//            String merchatId = mapper.get("_mecahrtId");
//            String errMessage = mapper.get("errMessage");
//            int accSource = Integer.parseInt(mapper.get("accSource").toString());
//            if (StringUtils.isEmpty(wxId) || StringUtils.isEmpty(merchatId)) {
//                // 水军号不足， 分配失败 ,发送失败
//                AppEventProcessor.sendEvent(EventType.PushMsgError, accSource,
//                        currentRobot.getRobotNickname(), StringUtils.isEmpty(wxId) ? errorWxId : wxId,
//                        task.getPlayId(), groupId, errMessage, playMessage.getMessageSort());
//                // AppEventProcessor.sendEvent(EventType.groupPushError,play,groupId
//                // , errMessage);
//                // 检测是否停止剧本
//                if (playMessage.getPlayErrorType() == 2) {
//                    // 系统暂停 这个群的 这个剧本 推送
//                    SysPushOpRedisModel.pause(groupId, play.getPlayId());
//                    AppEventProcessor.sendEvent(EventType.groupSysPause, play.getPlayId(), groupId,
//                            errMessage);
//                } else if (playMessage.getPlayErrorType() == 1) {
//                    // 群维度 计数器加1
//                    GroupPushCounter.incr(play.getPlayId(), groupId);
//                    // 继续推送 下面的内容
//                    // AppEventProcessor.sendEvent(EventType.groupPushError,play,groupId
//                    // , errMessage);
//                }
//                continue;
//            }
//            GroupInfo info = App.getBean(GroupInfoMapper.class).selectInfoByWxGroupId(groupId);
//            if (info == null || info.getState() == 2 || info.getState() == 3) {
//                // 这个群 停止服务
//                AppEventProcessor.sendEvent(EventType.PushMsgError, accSource,
//                        currentRobot.getRobotNickname(), StringUtils.isEmpty(wxId) ? errorWxId : wxId,
//                        task.getPlayId(), groupId, "群停止服务", playMessage.getMessageSort());
//                if (playMessage.getPlayErrorType() == 2) {
//                    // 系统暂停 这个群的 这个剧本 推送
//                    SysPushOpRedisModel.pause(groupId, play.getPlayId());
//                    AppEventProcessor.sendEvent(EventType.groupSysPause, play.getPlayId(), groupId, "群停止服务");
//                } else if (playMessage.getPlayErrorType() == 1) {
//                    // 群维度 计数器加1
//                    GroupPushCounter.incr(play.getPlayId(), groupId);
//                    // 继续推送 下面的内容
//                    // AppEventProcessor.sendEvent(EventType.groupPushError,play,groupId
//                    // , errMessage);
//                }
//                continue;
//            }
//            // if (SysPushOpRedisModel.isPause(groupId, play.getPlayId())) {
//            // // 系统暂停恢复
//            // SysPushOpRedisModel.resume(groupId, play.getPlayId());
//            // AppEventProcessor.sendEvent(EventType.groupSysResume,
//            // play.getPlayId(), groupId);
//            // }
//            // 生产log
//            PushLog log = new PushLog();
//            log.setCreateTime(new Date());
//            log.setGroupId(groupId);
//            log.setPlayId(task.getPlayId());
//            log.setPlayMessageId(task.getPlayMessageId());
//            log.setPersonName(currentRobot.getRobotNickname());
//            log.setRobotWxId(wxId);
//            log.setMerchatId(merchatId);
//            Pair<Boolean, String> retPair = send0(groupId, wxId, playMessage, merchatId);
//            boolean ret = retPair.getLeft();
//            if (ret) {
//                // 群维度计数器加1
//                GroupPushCounter.incr(play.getPlayId(), groupId);
//                // 发送成功，记录日志
//                log.setPushErrorMsg(retPair.getRight());
//                log.setPushState(0);
//                Logs.e(getClass(), "[调用比邻消息发送成功]>>log=" + JSON.toJSONString(log));
//                AppEventProcessor.sendEvent(EventType.PushMsgSuccess, log, accSource,
//                        playMessage.getMessageSort());
//            } else {
//                log.setPushErrorMsg(retPair.getRight());
//                log.setPushState(1);
//                Logs.e(getClass(),
//                        "[调用比邻发送私聊接口失败]>>task=" + JSON.toJSONString(task) + ",ret=" + retPair.getRight());
//                AppEventProcessor.sendEvent(EventType.PushMsgError, accSource,
//                        currentRobot.getRobotNickname(), StringUtils.isEmpty(wxId) ? errorWxId : wxId,
//                        task.getPlayId(), groupId, retPair.getRight(), playMessage.getMessageSort());
//                if (playMessage.getPlayErrorType() == 2) {
//                    // 系统暂停 这个群的 这个剧本 推送
//                    SysPushOpRedisModel.pause(groupId, play.getPlayId());
//                    AppEventProcessor.sendEvent(EventType.groupSysPause, play.getPlayId(), groupId,
//                            retPair.getRight());
//                } else if (playMessage.getPlayErrorType() == 1) {
//                    // 群维度 计数器加1
//                    GroupPushCounter.incr(play.getPlayId(), groupId);
//                    // 继续推送 下面的内容
//                    // AppEventProcessor.sendEvent(EventType.groupPushError,play,groupId
//                    // , errMessage);
//                }
//            }
//            if (GroupPushCounter.get(play.getPlayId(), groupId) >= play.getContentNum()) {
//                // 这个群已经 发送完成
//                System.err.println("群发送完成,playId=" + play.getPlayId() + ",groupId=" + groupId);
//                AppEventProcessor.sendEvent(EventType.groupPushFinish, play, groupId);
//            }
//            // 记录这个发言人 已经 发言了
//            SpeakedSortRedisModel.speak(groupId, play.getPlayId(), task.getSort());
//            int logId = App.getBean(PushLogMapper.class).insert(log);
//            if (logId <= 0) {
//                Logs.e(getClass(), "[t_push_log表插入失败]>>" + JSON.toJSONString(log));
//                AlertContext.robot().alert("[t_push_log表插入失败]>>" + JSON.toJSONString(log));
//            }
//        }
//        if (play.getPlayType() == 1) {
//            // 定时触发 修改 为 完成
//            // 查询日志条数
//            int count = App.getBean(PushLogMapper.class).selectCountByPlayId(play.getPlayId());
//            if (count >= play.getContentNum() * play.getGroupNum()) {
//                // 条数已经满足 , 剧本完成
//                AppEventProcessor.sendEvent(EventType.PlayFinish, play.getPlayId());
//            }
//        }
//    }
//
//    /***
//     * 
//     * title: 调用比邻发送私聊消息
//     *
//     * @param groupId
//     * @param wxId
//     * @param playMessage
//     * @return
//     * @author HadLuo 2020-9-17 18:21:36
//     */
//    private Pair<Boolean, String> send0(String groupId, String wxId, PlayMessage playMessage,
//            String merchatId) {
//        try {
//            SendGroupChatMsgDTO dto = new SendGroupChatMsgDTO();
//            dto.setIdentity(Env.identity());
//            dto.setVcGroupId(groupId);
//            dto.setWxId(wxId);
//            Data data = new Data();
//            if (playMessage.getCallAll() == 1) {
//                // 要@所有人
//                data.setIsHit(1);
//            } else {
//                data.setIsHit(0);
//            }
//            ContentJson contentJson = JSON.parseObject(playMessage.getMessageContent(), ContentJson.class);
//            // 消息内容
//            data.setMsgContent(contentJson.getSMateContent());
//            // 消息类型 文字 ，图片 等
//            // 消息类型 2001 文字 2002 图片 2003 语音(只支持amr格式) 2004 视频 2005 链接 2006 好友名片
//            // 2010
//            // 文件 2013 小程序 2016 音乐
//            data.setMsgType(contentJson.getMomentTypeId());
//
//            if (contentJson.getMomentTypeId() == 2002) {
//                // 图片消息
//                data.setMsgContent(contentJson.getSMateImgUrl());
//                data.setVcHref(contentJson.getSMateImgUrl());
//            }
//            if (contentJson.getMomentTypeId() == 2003) {
//                // 语音消息
//                data.setMsgContent(contentJson.getSMateAwrUrl());
//                data.setVcHref(contentJson.getSMateAwrUrl());
//            }
//            if (contentJson.getMomentTypeId() == 2004) {
//                // 视频消息
//                data.setVcHref(contentJson.getSMateVUrl());
//                data.setMsgContent(contentJson.getSMateImgUrl());
//            }
//            if (contentJson.getMomentTypeId() == 2005) {
//                // 链接
//                data.setVcHref(contentJson.getSMateVUrl());
//                data.setMsgContent(contentJson.getSMateImgUrl());
//                data.setVcDesc(contentJson.getSMateContent());
//            }
//            if (contentJson.getMomentTypeId() == 2013) {
//                data.setMsgContent(contentJson.getSMateContent());
//                data.setVcHref(contentJson.getSMateVUrl());
//            }
//            if (contentJson.getMomentTypeId() == 2010 || contentJson.getMomentTypeId() == 2015) {
//                data.setVcHref(contentJson.getSMateVUrl());
//                data.setMsgContent(contentJson.getSMateContent());
//            }
//            data.setVcTitle(StringUtils.isEmpty(contentJson.getSMateTitle()) ? contentJson.getTitle()
//                    : contentJson.getSMateTitle());
//            data.setVoiceTime(contentJson.getSMateBVLen());
//            dto.setData(Lists.newArrayList(data));
//            dto.setMerchatId(merchatId);
//            // 发送群内聊天消息
//            Result<BooleanResultVo> r = Urls.sendGroupChatMessages(dto);
//            if (r.successful() && r.getData().getResultCode() == 0) {
//                return Pair.of(true, r.getData().getOptSerNo());
//            }
//            return Pair.of(false, r.getMessage());
//        } catch (Throwable e) {
//            Logs.e(getClass(), "发送消息错误", e);
//            return Pair.of(false, "系统错误,msg:" + e.getMessage());
//        }
//    }
//
//    /***
//     * 
//     * title: 将剧本消息入延迟队列
//     *
//     * @param play
//     * @author HadLuo 2020-9-21 15:22:38
//     */
//    public void enQueue(int playId) {
//        PushTask record = new PushTask();
//        record.setPlayId(playId);
//        // 查询要推送的 任务
//        List<PushTask> tasks = App.getBean(PushTaskMapper.class).select(record);
//        // 查询 具体 message ，并封装
//        for (PushTask task : tasks) {
//            // 查询封装 消息
//            task.setPlayMessage(
//                    App.getBean(PlayMessageMapper.class).selectByPrimaryKey(task.getPlayMessageId()));
//        }
//        int delay = 0;
//        // 排序消息 ,包装消息严格顺序
//        controller.initSort(tasks);
//        // 遍历 所有发言人 维度
//        for (PushTask task : tasks) {
//            // 查询 消息
//            PlayMessage msg = task.getPlayMessage();
//            if (msg == null) {
//                continue;
//            }
//            // 查询当前的 发言人设置
//            PlayRobotConfig currentRobot = JSON.parseObject(task.getRobots(), PlayRobotConfig.class);
//            if (currentRobot == null) {
//                continue;
//            }
//            // 设置值
//            task.setCurrentRobot(currentRobot);
//            // 放入 消息推送器 去执行延迟推送
//            delay += msg.getIntervalTime();
//            makeDelayQueue().put(Task.newTask(task, delay));
//        }
//    }
//
//    @Override
//    public void destroy() throws Exception {
//        if (this.delayQueue != null) {
//            this.delayQueue.destory();
//        }
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        // 预加载
//        makeDelayQueue();
//    }
//}
