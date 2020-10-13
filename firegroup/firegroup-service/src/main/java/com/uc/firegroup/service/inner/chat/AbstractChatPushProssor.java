package com.uc.firegroup.service.inner.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.SendGroupChatMsgDTO;
import com.uc.external.bilin.req.SendGroupChatMsgDTO.Data;
import com.uc.external.bilin.res.BooleanResultVo;
import com.uc.firegroup.api.pojo.GroupInfo;
import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.pojo.PlayMessage;
import com.uc.firegroup.api.pojo.PlayRobotConfig;
import com.uc.firegroup.api.pojo.PushLog;
import com.uc.firegroup.api.pojo.PushTask;
import com.uc.firegroup.api.pojo.PlayMessage.ContentJson;
import com.uc.firegroup.service.config.Env;
import com.uc.firegroup.service.mapper.GroupInfoMapper;
import com.uc.firegroup.service.mapper.PlayInfoMapper;
import com.uc.firegroup.service.mapper.PlayMessageMapper;
import com.uc.firegroup.service.mapper.PushLogMapper;
import com.uc.firegroup.service.mapper.PushTaskMapper;
import com.uc.firegroup.service.tools.EventType;
import com.uc.firegroup.service.tools.event.AppEventProcessor;
import com.uc.framework.App;
import com.uc.framework.Ids;
import com.uc.framework.Pair;
import com.uc.framework.chat.Chat;
import com.uc.framework.chat.ChatConfigure;
import com.uc.framework.chat.ChatConfigureBuilder;
import com.uc.framework.chat.ChatGroup;
import com.uc.framework.chat.ChatGroupFactory;
import com.uc.framework.chat.ChatLifeCycleAdapter;
import com.uc.framework.chat.Future;
import com.uc.framework.chat.context.ChatProcessor;
import com.uc.framework.chat.strategy.SendChatStrategy;
import com.uc.framework.logger.Logs;
import com.uc.framework.obj.Result;
import com.uc.framework.redis.RedisHandler;

public abstract class AbstractChatPushProssor implements InitializingBean {
    public static AbstractChatPushProssor getProssor(PlayInfo playInfo) {
        if (playInfo.getPlayType() == 1) {
            // 定时
            return App.getBean(JobChatPushProcessor.class);
        } else {
            return App.getBean(KeyWordsChatPushProcessor.class);
        }
    }

    public static <T> T getProssor(Class<T> clazz) {
        return (T) App.getBean(clazz);
    }

    //
    public abstract ChatProcessor getChatProcessor();
    //
    // /***
    // * title:推送处理器
    // */
    // ChatProcessor chatProcessor;

    /***
     * 
     * title: kafka 消息发送成功回调
     *
     * @param ackKey
     * @param groupId
     * @param errorMessage
     * @author HadLuo 2020-10-12 14:38:24
     */
    public void onAck(String ackKey, String groupId, String errorMessage) {
        if (StringUtils.isEmpty(errorMessage)) {
            getChatProcessor().onAck(() -> Future.newSuccessFuture(ackKey, groupId));
        } else {
            getChatProcessor().onAck(() -> Future.newErrorFuture(groupId, ackKey, errorMessage));
        }
    }

    /**
     * 
     * title: 暂停恢复
     *
     * @param playId
     * @param groupWxId
     * @author HadLuo 2020-10-12 14:38:36
     */
    public void onResume(int playId, String groupWxId) {
        getChatProcessor().cancelPause(playId + "", groupWxId);
    }

    /***
     * 
     * title: 暂停
     *
     * @param playId
     * @param groupWxId
     * @author HadLuo 2020-10-12 14:38:49
     */
    public void pause(int playId, String groupWxId) {
        getChatProcessor().startPause(playId + "", groupWxId);
    }

    /***
     * 
     * title: 将剧本消息入延迟队列
     *
     * @param play
     * @author HadLuo 2020-9-21 15:22:38
     */
    void putQueue(List<PushTask> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        int playId = 0;
        // 分配聊天包
        List<Chat> chats = new ArrayList<Chat>();
        // 查询 具体 message ，并封装
        PlayMessage playMessage = null;
        for (PushTask task : tasks) {
            playId = task.getPlayId();
            // 查询封装 消息
            playMessage = App.getBean(PlayMessageMapper.class).selectByPrimaryKey(task.getPlayMessageId());
            if (playMessage == null) {
                continue;
            }
            Chat chat = new Chat(task.getPlayId() + "", playMessage.getIntervalTime(),
                    playMessage.getMessageSort(), task.getWxGroupId());
            // 额外参数
            chat.setArg(task.getId());
            chats.add(chat);
        }
        ChatGroup chatGroup = ChatGroupFactory.create(playId + "", chats);
        getChatProcessor().onAccept(chatGroup);
    }

    ChatConfigure getConfigure() {
        return ChatConfigureBuilder.build().setLifeCycle(new ChatLifeCycleAdapter() {
            @Override
            public void onStartup(String groupUuid, LinkedList<Chat> chats) {
                // 开始推送剧本消息
                AppEventProcessor.sendEvent(EventType.PlayStart, Integer.parseInt(groupUuid));
            }

            @Override
            public void onGroupFinish(String groupUuid, String groupId) {
                PlayInfo play = App.getBean(PlayInfoMapper.class)
                        .selectByPrimaryKey(Integer.parseInt(groupUuid));
                // 这个群 发送完成
                AppEventProcessor.sendEvent(EventType.groupPushFinish, play, groupId);
                if (play.getPlayType() == 1) {
                    // 剧本完成
                    int count = App.getBean(PushLogMapper.class).selectCountByPlayId(play.getPlayId());
                    if (count >= play.getContentNum() * play.getGroupNum()) {
                        // 条数已经满足 , 剧本完成
                        AppEventProcessor.sendEvent(EventType.PlayFinish, play.getPlayId());
                        // 剧本完成时 调用
                        getChatProcessor().clearStore(groupUuid);
                    }
                }
            }

            @Override
            public void onSingleAckSendSuccess(String groupUuid, String ackKey, Chat chat) {
                PushLogMapper logMapper = App.getBean(PushLogMapper.class);
                int accSource = Integer.parseInt(RedisHandler.get("accSource." + ackKey));
                PushLog record = new PushLog();
                record.setPushErrorMsg(ackKey);
                PushLog log = logMapper.selectOne(record);
                log.setPushState(2);
                logMapper.updateByPrimaryKey(log);
                AppEventProcessor.sendEvent(EventType.PushMsgSuccess, log, accSource, chat.getSort());
                RedisHandler.del("accSource." + ackKey);
            }

            @Override
            public void onSingleAckSendError(String groupUuid, String ackKey, Chat chat,
                    String errorMessage) {
                PushLogMapper logMapper = App.getBean(PushLogMapper.class);
                int accSource = Integer.parseInt(RedisHandler.get("accSource." + ackKey));
                PushLog record = new PushLog();
                record.setPushErrorMsg(ackKey);
                PushLog log = logMapper.selectOne(record);
                log.setPushState(1);
                logMapper.updateByPrimaryKey(log);
                AppEventProcessor.sendEvent(EventType.PushMsgError, accSource, log.getPersonName(),
                        log.getRobotWxId(), log.getPlayId(), log.getGroupId(), errorMessage, chat.getSort());
                RedisHandler.del("accSource." + ackKey);
            }

            @Override
            public void onSingleSendStart(String groupUuid, Chat chat) {
                // 开始推消息 事件
                AppEventProcessor.sendEvent(EventType.startPush, chat.getCurrentSendGroupWxId(),
                        Integer.parseInt(groupUuid));
            }
        }).setSendChatStrategy(new SendChatStrategy() {
            @Override
            public Future asyncSend(Chat chat) {
                Logs.e(getClass(), "chat send >>" + JSON.toJSONString(chat));
                // 比例发送消息
                return buildFuture(chat);
            }
        });
    }

    private void recordErrorLogger(int playId, PlayMessage playMessage, PlayRobotConfig currentRobot,
            String errorMsg, String groupId, String wxId, String merchatId) {
        // 记录日志
        PushLog log = new PushLog();
        log.setCreateTime(new Date());
        log.setGroupId(groupId);
        log.setPlayId(playId);
        log.setPlayMessageId(playMessage.getPlayMessageId());
        log.setPersonName(currentRobot.getRobotNickname());
        log.setRobotWxId(wxId);
        log.setMerchatId(merchatId);
        // // 发送成功，记录日志
        log.setPushErrorMsg(errorMsg);
        log.setPushState(1);
        App.getBean(PushLogMapper.class).insert(log);
    }

    private Future buildFuture(Chat chat) {
        String groupId = chat.getCurrentSendGroupWxId();
        PlayInfo playInfo = App.getBean(PlayInfoMapper.class)
                .selectByPrimaryKey(Integer.parseInt(chat.getGroupUuid()));
        PushTask pushTask = App.getBean(PushTaskMapper.class).selectByPrimaryKey((Integer) chat.getArg());
        if (playInfo == null) {
            return Future.newErrorFuture(groupId, Ids.getId(), "数据库值不存在,playInfo=" + chat.getGroupUuid());
        }
        if (pushTask == null) {
            return Future.newErrorFuture(groupId, Ids.getId(), "数据库值不存在,pushtaskId=" + chat.getArg());
        }
        PlayMessage playMessage = App.getBean(PlayMessageMapper.class)
                .selectByPrimaryKey(pushTask.getPlayMessageId());
        if (playMessage == null) {
            return Future.newErrorFuture(groupId, Ids.getId(),
                    "数据库值不存在,playMessage=" + pushTask.getPlayMessageId());
        }
        if (playInfo.getState() == 3) {
            AppEventProcessor.sendEvent(EventType.PushMsgError, 0, "", "", playInfo.getPlayId(), groupId,
                    "剧本已被取消", playMessage.getMessageSort());
            return Future.newErrorFuture(groupId, Ids.getId(), "剧本已被取消");
        }
        if (playInfo.getIsDelete() == 1) {
            AppEventProcessor.sendEvent(EventType.PushMsgError, 0, "", "", playInfo.getPlayId(), groupId,
                    "剧本已被删除", playMessage.getMessageSort());
            return Future.newErrorFuture(groupId, Ids.getId(), "剧本已被删除");
        }
        if (playInfo.getIsStart() == 2) {
            AppEventProcessor.sendEvent(EventType.PushMsgError, 0, "", "", playInfo.getPlayId(), groupId,
                    "剧本已被禁用", playMessage.getMessageSort());
            return Future.newErrorFuture(groupId, Ids.getId(), "剧本已被禁用");
        }
        // 当前发言人 设置
        PlayRobotConfig currentRobot = JSON.parseObject(pushTask.getRobots(), PlayRobotConfig.class);
        // 实际分配 发送 号
        Map<String, String> mapper = AllocateWxIdStrategy.allocateRobot(groupId, currentRobot, pushTask);
        String wxId = (String) mapper.get("sendPersonWxId");
        String errorWxId = (String) mapper.get("errorWxId");
        String merchatId = mapper.get("_mecahrtId");
        String errMessage = mapper.get("errMessage");
        int accSource = Integer.parseInt(mapper.get("accSource").toString());
        if (StringUtils.isEmpty(wxId) || StringUtils.isEmpty(merchatId)) {
            recordErrorLogger(playInfo.getPlayId(), playMessage, currentRobot, errMessage, groupId,
                    StringUtils.isEmpty(wxId) ? errorWxId : wxId, merchatId);
            // 水军号不足， 分配失败 ,发送失败
            if (playMessage.getPlayErrorType() == 2) {
                // 系统暂停 这个群的 这个剧本 推送
                // 系统暂停事件
                AppEventProcessor.sendEvent(EventType.groupSysPause, pushTask.getPlayId(), groupId,
                        errMessage);
                return Future.newPauseFuture(pushTask.getPlayId() + "", groupId);
            } else if (playMessage.getPlayErrorType() == 1) {
                // 继续发送
                AppEventProcessor.sendEvent(EventType.PushMsgError, accSource,
                        currentRobot.getRobotNickname(), StringUtils.isEmpty(wxId) ? errorWxId : wxId,
                        pushTask.getPlayId(), groupId, errMessage, playMessage.getMessageSort());
                return Future.newErrorFuture(groupId, Ids.getId(), errMessage);
            }
        }
        GroupInfo info = App.getBean(GroupInfoMapper.class).selectInfoByWxGroupId(groupId);
        if (info == null || info.getState() == 2 || info.getState() == 3) {
            // 这个群 停止服务
            AppEventProcessor.sendEvent(EventType.PushMsgError, accSource, currentRobot.getRobotNickname(),
                    StringUtils.isEmpty(wxId) ? errorWxId : wxId, pushTask.getPlayId(), groupId, "群停止服务",
                    playMessage.getMessageSort());
            recordErrorLogger(playInfo.getPlayId(), playMessage, currentRobot, "群停止服务", groupId,
                    StringUtils.isEmpty(wxId) ? errorWxId : wxId, merchatId);
            if (playMessage.getPlayErrorType() == 2) {
                // 系统暂停 这个群的 这个剧本 推送
                AppEventProcessor.sendEvent(EventType.groupSysPause, pushTask.getPlayId(), groupId, "群停止服务");
                return Future.newPauseFuture(pushTask.getPlayId() + "", groupId);
            }
            return Future.newErrorFuture(groupId, Ids.getId(), "群停止服务");
        }
        PushLog log = new PushLog();
        log.setCreateTime(new Date());
        log.setGroupId(groupId);
        log.setPlayId(pushTask.getPlayId());
        log.setPlayMessageId(pushTask.getPlayMessageId());
        log.setPersonName(currentRobot.getRobotNickname());
        log.setRobotWxId(wxId);
        log.setMerchatId(merchatId);
        Pair<Boolean, String> retPair = send0(groupId, wxId, playMessage, merchatId);
        boolean ret = retPair.getLeft();
        if (ret) {
            // // 发送成功，记录日志
            log.setPushErrorMsg(retPair.getRight());
            log.setPushState(0);
            Logs.e(getClass(), "[调用比邻消息发送成功]>>log=" + JSON.toJSONString(log));
            App.getBean(PushLogMapper.class).insert(log);
            // 记录账号来源
            RedisHandler.set("accSource." + retPair.getRight(), accSource + "");
            return Future.newSuccessFuture(retPair.getRight(), groupId);
        } else {
            log.setPushErrorMsg(retPair.getRight());
            log.setPushState(1);
            App.getBean(PushLogMapper.class).insert(log);
            Logs.e(getClass(),
                    "[调用比邻发送私聊接口失败]>>task=" + JSON.toJSONString(pushTask) + ",ret=" + retPair.getRight());
            AppEventProcessor.sendEvent(EventType.PushMsgError, accSource, currentRobot.getRobotNickname(),
                    StringUtils.isEmpty(wxId) ? errorWxId : wxId, pushTask.getPlayId(), groupId,
                    retPair.getRight(), playMessage.getMessageSort());

            if (playMessage.getPlayErrorType() == 2) {
                // 系统暂停 这个群的 这个剧本 推送
                AppEventProcessor.sendEvent(EventType.groupSysPause, pushTask.getPlayId(), groupId,
                        retPair.getRight());
                return Future.newPauseFuture(pushTask.getPlayId() + "", groupId);
            } else if (playMessage.getPlayErrorType() == 1) {
                return Future.newErrorFuture(groupId, Ids.getId(), retPair.getRight());
            }
        }
        return null;
    }

    /***
     * 
     * title: 调用比邻发送私聊消息
     *
     * @param groupId
     * @param wxId
     * @param playMessage
     * @return
     * @author HadLuo 2020-9-17 18:21:36
     */
    private Pair<Boolean, String> send0(String groupId, String wxId, PlayMessage playMessage,
            String merchatId) {
        try {
            SendGroupChatMsgDTO dto = new SendGroupChatMsgDTO();
            dto.setIdentity(Env.identity());
            dto.setVcGroupId(groupId);
            dto.setWxId(wxId);
            Data data = new Data();
            if (playMessage.getCallAll() == 1) {
                // 要@所有人
                data.setIsHit(1);
            } else {
                data.setIsHit(0);
            }
            ContentJson contentJson = JSON.parseObject(playMessage.getMessageContent(), ContentJson.class);
            // 消息内容
            data.setMsgContent(contentJson.getSMateContent());
            // 消息类型 文字 ，图片 等
            // 消息类型 2001 文字 2002 图片 2003 语音(只支持amr格式) 2004 视频 2005 链接 2006 好友名片
            // 2010
            // 文件 2013 小程序 2016 音乐
            data.setMsgType(contentJson.getMomentTypeId());

            if (contentJson.getMomentTypeId() == 2002) {
                // 图片消息
                data.setMsgContent(contentJson.getSMateImgUrl());
                data.setVcHref(contentJson.getSMateImgUrl());
            }
            if (contentJson.getMomentTypeId() == 2003) {
                // 语音消息
                data.setMsgContent(contentJson.getSMateAwrUrl());
                data.setVcHref(contentJson.getSMateAwrUrl());
            }
            if (contentJson.getMomentTypeId() == 2004) {
                // 视频消息
                data.setVcHref(contentJson.getSMateVUrl());
                data.setMsgContent(contentJson.getSMateImgUrl());
            }
            if (contentJson.getMomentTypeId() == 2005) {
                // 链接
                data.setVcHref(contentJson.getSMateVUrl());
                data.setMsgContent(contentJson.getSMateImgUrl());
                data.setVcDesc(contentJson.getSMateContent());
            }
            if (contentJson.getMomentTypeId() == 2013) {
                data.setMsgContent(contentJson.getSMateContent());
                data.setVcHref(contentJson.getSMateVUrl());
            }
            if (contentJson.getMomentTypeId() == 2010 || contentJson.getMomentTypeId() == 2015) {
                data.setVcHref(contentJson.getSMateVUrl());
                data.setMsgContent(contentJson.getSMateContent());
            }
            data.setVcTitle(StringUtils.isEmpty(contentJson.getSMateTitle()) ? contentJson.getTitle()
                    : contentJson.getSMateTitle());
            data.setVoiceTime(contentJson.getSMateBVLen());
            dto.setData(Lists.newArrayList(data));
            dto.setMerchatId(merchatId);
            // 发送群内聊天消息
            Result<BooleanResultVo> r = Urls.sendGroupChatMessages(dto);
            if (r.successful() && r.getData().getResultCode() == 0) {
                return Pair.of(true, r.getData().getOptSerNo());
            }
            return Pair.of(false, r.getMessage());
        } catch (Throwable e) {
            Logs.e(getClass(), "发送消息错误", e);
            return Pair.of(false, "系统错误,msg:" + e.getMessage());
        }
    }
}
