//package com.uc.firegroup.service.inner.push;
//
//import java.util.Arrays;
//import java.util.List;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.StringUtils;
//import com.alibaba.fastjson.JSON;
//import com.google.common.collect.Lists;
//import com.uc.firegroup.api.pojo.PlayInfo;
//import com.uc.firegroup.api.pojo.PlayMessage;
//import com.uc.firegroup.api.pojo.PlayRobotConfig;
//import com.uc.firegroup.api.pojo.PushTask;
//import com.uc.firegroup.api.request.PlayInfoRequest.KeyWordsRule;
//import com.uc.firegroup.api.request.PlayInfoRequest.KeyWordsSearchRule;
//import com.uc.firegroup.api.response.Type5051Response;
//import com.uc.firegroup.service.mapper.PlayInfoMapper;
//import com.uc.firegroup.service.mapper.PlayMessageMapper;
//import com.uc.firegroup.service.mapper.PushTaskMapper;
//import com.uc.firegroup.service.tools.EventType;
//import com.uc.firegroup.service.tools.event.AppEventProcessor;
//import com.uc.framework.App;
//import com.uc.framework.Objects;
//import com.uc.framework.Times;
//import com.uc.framework.logger.Logs;
//import com.uc.framework.redis.lock.RedisLock;
//import com.uc.framework.redis.queue.DelayQueue;
//import com.uc.framework.redis.queue.DelayQueueManager;
//import com.uc.framework.redis.queue.Task;
//
///***
// * 
// * title: 定时器推送 的 任务处理器
// *
// * @author HadLuo
// * @date 2020-9-19 16:16:31
// */
//public class KeyWordsPushTaskProcessor extends AbstractPushTaskProcessor {
//    private static final String KEY = "queue.delay.keywords";
//
//    @Override
//    public void onAccept(Object obj) {
//        Type5051Response msg = (Type5051Response) obj;
//        // 查询要推送的话术触发剧本
//        List<PlayInfo> plays = App.getBean(PlayInfoMapper.class).selectKeyWordsTimeUp();
//        if (CollectionUtils.isEmpty(plays)) {
//            return;
//        }
//        for (PlayInfo play : plays) {
//            String lockKey = "push.play." + play.getPlayId();
//            // 幂等锁
//            try {
//                boolean ret = App.getBean(RedisLock.class).lock(lockKey, lockKey, 60 * 30);
//                if (!ret) {
//                    Logs.e(getClass(), "[幂等锁]>>定时推送,play=" + JSON.toJSONString(play));
//                    continue;
//                }
//                if (play.getIsStart() == 2) {
//                    // 禁用
//                    continue;
//                }
//                if (!enable(play)) {
//                    continue;
//                }
//                // 模糊查询
//                String keyWordsRuleJson = play.getPlayKeywordRule();
//                if (StringUtils.isEmpty(keyWordsRuleJson)) {
//                    continue;
//                }
//                KeyWordsRule rule = JSON.parseObject(keyWordsRuleJson, KeyWordsRule.class);
//                if (Objects.wrapNull(rule.getDelaySecond(), 0) <= 0) {
//                    // 触发后 多少秒 推送
//                    continue;
//                }
//                // 在触发时间内
//                if (Times.betweenHMS(rule.getStartTime(), rule.getEndTime())) {
//                    // 匹配 关键词 规则
//                    String match = matchs(msg.getMsgInfo().getMsgContent(), rule.getSearchs());
//                    System.err.println("关键词匹配>>" + JSON.toJSONString(match));
//                    if (StringUtils.isEmpty(match)) {
//                        // 没有匹配上
//                        continue;
//                    }
//                    controller.debug("KeyWords Match", play.getPlayId(), msg.getVcChatRoomId(),
//                            "match words=" + msg.getMsgInfo().getMsgContent());
//                    // 判断 触发的群是不是在剧本所选择的群里面
//                    PushTask record = new PushTask();
//                    record.setPlayId(play.getPlayId());
//                    List<PushTask> tasks = App.getBean(PushTaskMapper.class).select(record);
//                    if (CollectionUtils.isEmpty(tasks)) {
//                        continue;
//                    }
//                    List<PushTask> useTasks = Lists.newArrayList();
//                    List<String> groupIds = Lists.newArrayList();
//                    // 一天只能触发一次
//                    String key = "keywords.push.oneday." + play.getPlayId() + "." + msg.getVcChatRoomId();
//                    ret = App.getBean(RedisLock.class).lock(key, key, Times.getSecondsTobeforedawn());
//                    if (!ret) {
//                        controller.debug("关键词风控", play.getPlayId(), msg.getVcChatRoomId(), "key=" + key);
//                        continue;
//                    }
//                    for (PushTask task : tasks) {
//                        if (!StringUtils.isEmpty(task.getWxGroupId())) {
//                            if (Arrays.asList(task.getWxGroupId().split(","))
//                                    .contains(msg.getVcChatRoomId())) {
//                                // 匹配到了群 , 这个task 要触发
//                                task.setCurrentGroup(msg.getVcChatRoomId());
//                                useTasks.add(task);
//                                groupIds.add(msg.getVcChatRoomId());
//                            }
//                        }
//                    }
//                    if (!CollectionUtils.isEmpty(useTasks)) {
//                        // 发送事件
//                        AppEventProcessor.sendEvent(EventType.keyWordsActive, play.getPlayId(), match,
//                                groupIds);
//                        System.err.println("加入 关键词延迟  消息的redis队列>>" + JSON.toJSONString(useTasks));
//                        // 丢到 redis延时队列里面等待发送
//                        makeKwywordsQueue()
//                                .put(Task.newTask(JSON.toJSONString(useTasks), rule.getDelaySecond()));
//                    }
//                }
//            } finally {
//                App.getBean(RedisLock.class).unlock(lockKey, lockKey);
//            }
//
//        }
//    }
//
//    public synchronized DelayQueue makeKwywordsQueue() {
//        return App.getBean(DelayQueueManager.class).getQueue("keywordsQueue", (task) -> {
//            // 将消息 真正扔到 发送队列 等待 推送消息
//            List<PushTask> tasks = JSON.parseArray(task.getData().toString(), PushTask.class);
//            System.err.println("收到 关键词延迟  消息的redis队列>>" + JSON.toJSONString(tasks));
//            enQueue(tasks);
//        });
//    }
//
//    public void enQueue(List<PushTask> tasks) {
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
//            System.err.println("遍历发言人>>" + JSON.toJSONString(task));
//            if (StringUtils.isEmpty(task.getCurrentGroup())) {
//                continue;
//            }
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
//            System.err.println("加入 实际发送 消息的redis队列>>" + JSON.toJSONString(task));
//            makeDelayQueue().put(Task.newTask(task, delay));
//        }
//    }
//
//    @Override
//    public String createDelayQueueKey() {
//        return KEY;
//    }
//
//    private String matchs(String keyWords, List<KeyWordsSearchRule> searchs) {
//        if (CollectionUtils.isEmpty(searchs)) {
//            return null;
//        }
//        for (KeyWordsSearchRule search : searchs) {
//            if (search.getType() == 1 && keyWords.equals(search.getWords())) {
//                // 精准匹配
//                return search.getWords();
//            }
//            if (search.getType() == 0 && keyWords.contains(search.getWords())) {
//                // 模糊匹配
//                return search.getWords();
//            }
//        }
//        return null;
//    }
//
//    /***
//     * 
//     * title: 是否启用推送 true-启用推送 false-禁用推送
//     *
//     * @return
//     * @author HadLuo 2020-9-19 16:38:45
//     */
//    public static boolean enable(PlayInfo play) {
//        KeyWordsRule rule = JSON.parseObject(play.getPlayKeywordRule(), KeyWordsRule.class);
//        // 在触发时间内
//        return Times.betweenHMS(rule.getStartTime(), rule.getEndTime());
//    }
//}
