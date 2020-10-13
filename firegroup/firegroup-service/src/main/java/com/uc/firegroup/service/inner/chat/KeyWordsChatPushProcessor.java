package com.uc.firegroup.service.inner.chat;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.pojo.PushTask;
import com.uc.firegroup.api.request.PlayInfoRequest.KeyWordsRule;
import com.uc.firegroup.api.request.PlayInfoRequest.KeyWordsSearchRule;
import com.uc.firegroup.api.response.Type5051Response;
import com.uc.firegroup.service.mapper.PlayInfoMapper;
import com.uc.firegroup.service.mapper.PushTaskMapper;
import com.uc.firegroup.service.tools.EventType;
import com.uc.firegroup.service.tools.event.AppEventProcessor;
import com.uc.framework.App;
import com.uc.framework.Objects;
import com.uc.framework.Times;
import com.uc.framework.chat.DefaultChatRequest;
import com.uc.framework.chat.context.ChatProcessor;
import com.uc.framework.chat.context.ChatProcessorContext;
import com.uc.framework.logger.Logs;
import com.uc.framework.redis.lock.RedisLock;
import com.uc.framework.redis.queue.DelayQueue;
import com.uc.framework.redis.queue.DelayQueueManager;
import com.uc.framework.redis.queue.Task;

@Component
public class KeyWordsChatPushProcessor extends AbstractChatPushProssor {
    ChatProcessor chatProcessor;

    @Override
    public void afterPropertiesSet() throws Exception {
        chatProcessor = ChatProcessorContext.createProcessor(new DefaultChatRequest("firegroup-keywords"));
        chatProcessor.setConfigure(getConfigure());
    }

    public void onStartup(Type5051Response msg) {
        // 触发的群
        String groupWxId = msg.getVcChatRoomId();
        // 关键词内容
        String words = msg.getMsgInfo().getMsgContent();

        Logs.e(getClass(), "keywords start >>groupId=" + groupWxId + ",words=" + words);
        // 查询要推送的话术触发剧本
        List<PlayInfo> plays = App.getBean(PlayInfoMapper.class).selectKeyWordsTimeUp();
        if (CollectionUtils.isEmpty(plays)) {
            return;
        }
        for (PlayInfo play : plays) {
            String lockKey = "push.play." + play.getPlayId();
            // 幂等锁
            try {
                boolean ret = App.getBean(RedisLock.class).lock(lockKey, lockKey, 60 * 30);
                if (!ret) {
                    Logs.e(getClass(), "[幂等锁]>>关键词推送,play=" + JSON.toJSONString(play));
                    continue;
                }
                if (play.getIsStart() == 2 || play.getIsDelete() == 1 || play.getState() == 3) {
                    // 禁用
                    continue;
                }
                if (!enable(play)) {
                    continue;
                }
                // 模糊查询
                String keyWordsRuleJson = play.getPlayKeywordRule();
                if (StringUtils.isEmpty(keyWordsRuleJson)) {
                    continue;
                }
                KeyWordsRule rule = JSON.parseObject(keyWordsRuleJson, KeyWordsRule.class);
                if (Objects.wrapNull(rule.getDelaySecond(), 0) <= 0) {
                    // 触发后 多少秒 推送
                    continue;
                }
                // 在触发时间内
                if (Times.betweenHMS(rule.getStartTime(), rule.getEndTime())) {
                    // 匹配 关键词 规则
                    String match = matchs(words, rule.getSearchs());
                    if (StringUtils.isEmpty(match)) {
                        // 没有匹配上
                        continue;
                    }
                    Logs.e(getClass(), "match ok >>playId=" + play.getPlayId() + ",groupId=" + groupWxId
                            + ",words=" + words);
                    // 判断 触发的群是不是在剧本所选择的群里面
                    PushTask record = new PushTask();
                    record.setPlayId(play.getPlayId());
                    List<PushTask> tasks = App.getBean(PushTaskMapper.class).select(record);
                    if (CollectionUtils.isEmpty(tasks)) {
                        continue;
                    }
                    List<PushTask> useTasks = Lists.newArrayList();
                    List<String> groupIds = Lists.newArrayList();
                    // 一天只能触发一次
                    String key = "keywords.push.oneday." + play.getPlayId() + "." + msg.getVcChatRoomId();
                    int s = Times.getSecondsTobeforedawn();
                    ret = App.getBean(RedisLock.class).lock(key, key, s);
                    if (!ret) {
                        Logs.e(getClass(), "keywords forbidden >>key=" + key + ",time=" + s + "秒");
                        continue;
                    }
                    for (PushTask task : tasks) {
                        if (!StringUtils.isEmpty(task.getWxGroupId())) {
                            if (Arrays.asList(task.getWxGroupId().split(","))
                                    .contains(msg.getVcChatRoomId())) {
                                // 匹配到了群 , 这个task 要触发
                                task.setCurrentGroup(msg.getVcChatRoomId());
                                useTasks.add(task);
                                groupIds.add(msg.getVcChatRoomId());
                            }
                        }
                    }
                    if (!CollectionUtils.isEmpty(useTasks)) {
                        // 发送事件
                        AppEventProcessor.sendEvent(EventType.keyWordsActive, play.getPlayId(), match,
                                groupIds);
                        Logs.e(getClass(), "keywords waitting >>" + JSON.toJSONString(useTasks));
                        // 丢到 redis延时队列里面等待发送
                        makeKwywordsQueue()
                                .put(Task.newTask(JSON.toJSONString(useTasks), rule.getDelaySecond()));
                    }
                } else {
                    Logs.e(getClass(), "关键词不在触发时间内 >>groupId=" + msg.getVcChatRoomId() + ",words=" + words);
                }
            } finally {
                App.getBean(RedisLock.class).unlock(lockKey, lockKey);
            }

        }
    }

    synchronized DelayQueue makeKwywordsQueue() {
        return App.getBean(DelayQueueManager.class).getQueue("dely.keywordsQueue", (task) -> {
            // 将消息 真正扔到 发送队列 等待 推送消息
            List<PushTask> tasks = JSON.parseArray(task.getData().toString(), PushTask.class);
            Logs.e(getClass(), "keywords receiver >>" + JSON.toJSONString(tasks));
            putQueue(tasks);
        });
    }

    /***
     * 
     * title: 匹配
     *
     * @param keyWords
     * @param searchs
     * @return
     * @author HadLuo 2020-10-12 14:52:32
     */
    private String matchs(String keyWords, List<KeyWordsSearchRule> searchs) {
        if (CollectionUtils.isEmpty(searchs)) {
            return null;
        }
        for (KeyWordsSearchRule search : searchs) {
            if (search.getType() == 1 && keyWords.equals(search.getWords())) {
                // 精准匹配
                return search.getWords();
            }
            if (search.getType() == 0 && keyWords.contains(search.getWords())) {
                // 模糊匹配
                return search.getWords();
            }
        }
        return null;
    }

    /***
     * 
     * title: 是否启用推送 true-启用推送 false-禁用推送
     *
     * @return
     * @author HadLuo 2020-9-19 16:38:45
     */
    public static boolean enable(PlayInfo play) {
        KeyWordsRule rule = JSON.parseObject(play.getPlayKeywordRule(), KeyWordsRule.class);
        // 在触发时间内
        return Times.betweenHMS(rule.getStartTime(), rule.getEndTime());
    }

    @Override
    public ChatProcessor getChatProcessor() {
        return chatProcessor;
    }
}
