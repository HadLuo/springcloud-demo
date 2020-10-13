package com.uc.firegroup.service.inner.chat;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.pojo.PushTask;
import com.uc.firegroup.service.mapper.PlayInfoMapper;
import com.uc.firegroup.service.mapper.PushTaskMapper;
import com.uc.framework.App;
import com.uc.framework.chat.DefaultChatRequest;
import com.uc.framework.chat.context.ChatProcessor;
import com.uc.framework.chat.context.ChatProcessorContext;
import com.uc.framework.logger.Logs;
import com.uc.framework.redis.lock.RedisLock;

@Component
public class JobChatPushProcessor extends AbstractChatPushProssor {
    @Autowired
    private PlayInfoMapper playInfoMapper;
    ChatProcessor chatProcessor ;

    @Override
    public void afterPropertiesSet() throws Exception {
        chatProcessor = ChatProcessorContext.createProcessor(new DefaultChatRequest("firegroup-job"));
        chatProcessor.setConfigure(getConfigure());
    }

    public void onStartup() {
        // 扫描 到了 时间的 定时推送的 剧本
        List<PlayInfo> plays = App.getBean(PlayInfoMapper.class).selectListTimeUp();
        Logs.e(getClass(), "[定时扫描到推送消息]>>" + JSON.toJSONString(plays));
        if (CollectionUtils.isEmpty(plays)) {
            return;
        }
        for (PlayInfo play : plays) {
            String lockKey = "push.play." + play.getPlayId();
            PlayInfo playInfo = playInfoMapper.selectByPrimaryKey(play.getPlayId());
            try {
                // 幂等锁
                boolean ret = App.getBean(RedisLock.class).lock(lockKey, lockKey, 60 * 30);
                if (!ret) {
                    Logs.e(getClass(), "[幂等锁]>>定时推送,play=" + JSON.toJSONString(play));
                    continue;
                }
                if (playInfo != null && playInfo.getIsScan() == 0) {
                    if (playInfo.getIsStart() == 2 || playInfo.getIsDelete() == 1
                            || playInfo.getState() == 3) {
                        // 禁用
                        continue;
                    }
                    // 修改 剧本为 不再扫描
                    playInfo.setIsScan(1);
                    playInfoMapper.updateByPrimaryKey(playInfo);
                }
                PushTask record = new PushTask();
                record.setPlayId(play.getPlayId());
                // 查询要推送的 任务
                List<PushTask> tasks = App.getBean(PushTaskMapper.class).select(record);
                putQueue(tasks);
            } finally {
                App.getBean(RedisLock.class).unlock(lockKey, lockKey);
            }
        }
    }

    @Override
    public ChatProcessor getChatProcessor() {
        return chatProcessor;
    }

}
