package com.uc.firegroup.service.tools;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.PersonalWxInfoDTO;
import com.uc.external.bilin.res.PersonalSimpleInfoVO;
import com.uc.firegroup.api.IPlayPushService;
import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.pojo.PlayMessagePush;
import com.uc.firegroup.api.pojo.PlayMessagePushDetail;
import com.uc.firegroup.api.pojo.PushLog;
import com.uc.firegroup.api.request.PlayPushCreateRequest;
import com.uc.firegroup.service.config.Env;
import com.uc.firegroup.service.mapper.PlayInfoMapper;
import com.uc.firegroup.service.mapper.PlayMessagePushDetailMapper;
import com.uc.firegroup.service.mapper.PlayMessagePushMapper;
import com.uc.firegroup.service.mapper.PushTaskMapper;
import com.uc.firegroup.service.tools.event.Event;
import com.uc.framework.logger.Logs;
import com.uc.framework.obj.Result;

/***
 * 
 * title: 程序事件处理 业务
 *
 * @author HadLuo
 * @date 2020-9-21 13:37:24
 */
@Component
public class EventService {

    @Autowired
    IPlayPushService playPushService;
    @Autowired
    PlayInfoMapper playInfoMapper;
    @Autowired
    PushTaskMapper pushTaskMapper;
    @Autowired
    PlayMessagePushMapper playMessagePushMapper;
    @Autowired
    PlayMessagePushDetailMapper playMessagePushDetailMapper;
    @Autowired
    Env env;

    /***
     * title :开始推送消息了
     * 
     * @param groupId 群id
     * @param playId 剧本id
     */
    @Event(EventType.startPush)
    public void onStartPush(String groupId, int playId) {
        debug("onStartPush事件", playId, groupId, "");
        // 更新 进行中 状态
        // 更新t_play_message_push
        PlayMessagePush push = playMessagePushMapper.selectOneByIds(playId, groupId);
        if (push == null) {
            return;
        }
        if (push.getPushState() != 1) {
            return;
        }
        push.setPushState(2);
        push.setModifyTime(new Date());
        push.setPushTime(new Date());
        playMessagePushMapper.updateByPrimaryKey(push);
    }

    /**
     * 
     * title: 关键词触发回调
     *
     * @param
     * @author HadLuo 2020-9-21 13:53:10
     */
    @Event(EventType.keyWordsActive)
    public void onKeyWordsActive(Integer playId, String keyWord, List<String> groupIds) {
        if (playId == null) {
            return;
        }
        HashSet<String> ids = new HashSet<String>(groupIds);
        for (String groupId : ids) {
            PlayMessagePush exsit = playMessagePushMapper.selectOneByIds(playId, groupId);
            if (exsit != null) {
                continue;
            }
            PlayPushCreateRequest req = new PlayPushCreateRequest();
            req.setPlayId(playId);
            req.setTriggerKeyword(keyWord);
            req.setWxGroupId(groupId);
            playPushService.createPush(req);
            debug("keyWordsActive事件", playId, groupId, "");
        }

    }

    private void debug(String flag, Integer playId, String groupId, String msg) {
        Logs.e(getClass(), "[" + flag + "]>>platId=" + playId + ",groupId=" + groupId + "," + msg);
    }

    /***
     * 
     * title: 群维度 发送完成
     *
     * @param play
     * @param groupId
     * @author HadLuo 2020-9-25 9:26:31
     */
    @Event(EventType.groupPushFinish)
    public void onGroupPushFinish(PlayInfo play, String groupId) {
        debug("groupPushFinish事件", play.getPlayId(), groupId, "");
        // 更新t_play_message_push
        PlayMessagePush push = playMessagePushMapper.selectOneByIds(play.getPlayId(), groupId);
        if (push == null) {
            return;
        }
        push.setModifyTime(new Date());
        push.setPushState(3);
        playMessagePushMapper.updateByPrimaryKey(push);
    }

    /***
     * 
     * title: 群维度 发送完成
     *
     * @param play
     * @param groupId
     * @author HadLuo 2020-9-25 9:26:31
     */
    @Event(EventType.groupPushError)
    public void onGroupPushError(PlayInfo play, String groupId, String error) {
        debug("groupPushError事件", play.getPlayId(), groupId, "error=" + error);
        // 更新t_play_message_push
        PlayMessagePush push = playMessagePushMapper.selectOneByIds(play.getPlayId(), groupId);
        if (push == null) {
            return;
        }
        if (push.getPushState() != null && push.getPushState() == 3) {
            return;
        }
        push.setModifyTime(new Date());
        push.setPushState(5);
        push.setPushFailReason(error);
        playMessagePushMapper.updateByPrimaryKey(push);
    }

    /**
     * 
     * title: 实际推送一条消息 成功回调
     *
     * @param log
     * @author HadLuo 2020-9-21 13:53:10
     */
    @Event(EventType.PushMsgSuccess)
    public void onPushMsgSuccess(PushLog log, int accSource, int sort) {
        // 更新t_play_message_push
        PlayMessagePush push = playMessagePushMapper.selectOneByIds(log.getPlayId(), log.getGroupId());
        if (push == null) {
            return;
        }
        // 更新t_play_message_push_detail
        List<PlayMessagePushDetail> details = playMessagePushDetailMapper.selectListByPushId(push.getId());
        for (PlayMessagePushDetail detail : details) {
            if (StringUtils.isEmpty(detail.getRobotNickname())) {
                continue;
            }
            if (detail.getRobotNickname().equals(log.getPersonName()) && sort == detail.getMessageSort()) {
                PersonalWxInfoDTO dto = new PersonalWxInfoDTO();
                dto.setWxIds(Lists.newArrayList(log.getRobotWxId()));
                dto.setIdentity(Env.identity());
                Result<List<PersonalSimpleInfoVO>> r = Urls.queryPersonalSimpleInfo(dto);
                if (r.successful() && !CollectionUtils.isEmpty(r.getData())) {
                    PersonalSimpleInfoVO p = r.getData().get(0);
                    detail.setWxNickname(p.getWxNick());
                    detail.setWxImgUrl(p.getWxImgUrl());
                    detail.setWxAcc(p.getWxAcc());
                }
                // 账号来源:1.水军 2.个人号
                detail.setAccSource(accSource);
                detail.setWxId(log.getRobotWxId());
                detail.setSendState(1);
                playMessagePushDetailMapper.updateByPrimaryKeySelective(detail);
                debug("PushMsgSuccess事件", log.getPlayId(), log.getRobotWxId(),
                        ",sort=" + sort + ",accSource=" + accSource + ",");
                break;
            }
        }
    }

    /**
     * 
     * title: 实际推送一条消息 成功回调
     *
     * @param
     * @author HadLuo 2020-9-21 13:53:10
     */
    @Event(EventType.PushMsgError)
    public void onPushMsgError(int accSource, String nick, String robotId, int playId, String groupId,
            String error, int sort) {

        // 更新t_play_message_push
        PlayMessagePush push = playMessagePushMapper.selectOneByIds(playId, groupId);
        if (push == null) {
            return;
        }
        // 更新t_play_message_push_detail
        List<PlayMessagePushDetail> details = playMessagePushDetailMapper.selectListByPushId(push.getId());
        for (PlayMessagePushDetail detail : details) {
            if (detail.getRobotNickname().equals(nick) && sort == detail.getMessageSort()) {
                if (!StringUtils.isEmpty(robotId)) {
                    PersonalWxInfoDTO dto = new PersonalWxInfoDTO();
                    dto.setWxIds(Lists.newArrayList(robotId));
                    dto.setIdentity(Env.identity());
                    Result<List<PersonalSimpleInfoVO>> r = Urls.queryPersonalSimpleInfo(dto);
                    if (r.successful() && !CollectionUtils.isEmpty(r.getData())) {
                        PersonalSimpleInfoVO p = r.getData().get(0);
                        detail.setWxNickname(p.getWxNick());
                        detail.setWxImgUrl(p.getWxImgUrl());
                        detail.setWxAcc(p.getWxAcc());
                    }
                }
                // 账号来源:1.水军 2.个人号
                detail.setAccSource(accSource);
                detail.setWxId(robotId);
                detail.setRobotNickname(nick);
                detail.setSendState(2);
                playMessagePushDetailMapper.updateByPrimaryKeySelective(detail);
                debug("PushMsgError事件", playId, groupId,
                        ",sort=" + sort + ",accSource=" + accSource + ",error=" + error);
                break;
            }
        }
    }

    /***
     * title : 系统暂停
     * 
     * @param groupId 群id
     * @param playId 剧本id
     */
    @Event(EventType.groupSysPause)
    public void onSysPause(int playId, String groupId, String msg) {
        debug("groupSysPause事件", playId, groupId, "");
        // 更新 进行中 状态
        // 更新t_play_message_push
        PlayMessagePush push = playMessagePushMapper.selectOneByIds(playId, groupId);
        if (push == null) {
            return;
        }
        push.setPushFailReason(msg);
        push.setPushState(6);
        push.setModifyTime(new Date());
        playMessagePushMapper.updateByPrimaryKey(push);
    }

    /***
     * title : 系统暂停恢复
     * 
     * @param groupId 群id
     * @param playId 剧本id
     */
    @Event(EventType.groupSysResume)
    public void onSysResume(int playId, String groupId) {
        debug("groupSysResume事件", playId, groupId, "");
        // 更新 进行中 状态
        // 更新t_play_message_push
        PlayMessagePush push = playMessagePushMapper.selectOneByIds(playId, groupId);
        if (push == null) {
            return;
        }
        push.setPushState(2);
        push.setModifyTime(new Date());
        playMessagePushMapper.updateByPrimaryKey(push);
    }

    /***
     * title : 剧本完成
     * 
     * @param groupId 群id
     * @param playId 剧本id
     */
    @Event(EventType.PlayFinish)
    public void onPlayFinish(int playId) {
        debug("PlayFinish事件", playId, "", "");
        PlayInfo playInfo = playInfoMapper.selectByPrimaryKey(playId);
        if (playInfo != null && playInfo.getIsScan() == 0) {
            // 修改 剧本为 不再扫描
            playInfo.setIsScan(1);
            playInfoMapper.updateByPrimaryKey(playInfo);
        }
    }

    /***
     * title : 开始推送剧本
     * 
     * @param groupId 群id
     * @param playId 剧本id
     */
    @Event(EventType.PlayStart)
    public void onPlayStart(int playId) {
        debug("PlayStart事件", playId, "", "");
        PlayInfo playInfo = playInfoMapper.selectByPrimaryKey(playId);
        if (playInfo != null && playInfo.getState() == 1) {
            // 修改 剧本为 已推送
            playInfo.setState(2);
            playInfoMapper.updateByPrimaryKey(playInfo);
        }
    }

}
