//package com.uc.firegroup.service.inner.push;
//
//import java.util.List;
//import org.springframework.util.CollectionUtils;
//import com.alibaba.fastjson.JSON;
//import com.uc.firegroup.api.pojo.PlayInfo;
//import com.uc.firegroup.service.mapper.PlayInfoMapper;
//import com.uc.firegroup.service.tools.EventType;
//import com.uc.firegroup.service.tools.event.AppEventProcessor;
//import com.uc.framework.App;
//import com.uc.framework.logger.Logs;
//import com.uc.framework.redis.lock.RedisLock;
//
///***
// * 
// * title: 定时器推送 的 任务处理器
// *
// * @author HadLuo
// * @date 2020-9-19 16:16:31
// */
//public class JobPushTaskProcessor extends AbstractPushTaskProcessor {
//    private static final String KEY = "queue.delay.time";
//
//    @Override
//    public void onAccept(Object param) {
//        // 扫描 到了 时间的 定时推送的 剧本
//        List<PlayInfo> plays = App.getBean(PlayInfoMapper.class).selectListTimeUp();
//        Logs.e(getClass(), "[定时扫描到推送消息]>>" + JSON.toJSONString(plays));
//        if (CollectionUtils.isEmpty(plays)) {
//            return;
//        }
//        for (PlayInfo play : plays) {
//            String lockKey = "push.play." + play.getPlayId();
//            try {
//                // 幂等锁
//                boolean ret = App.getBean(RedisLock.class).lock(lockKey, lockKey, 60 * 30);
//                if (!ret) {
//                    Logs.e(getClass(), "[幂等锁]>>定时推送,play=" + JSON.toJSONString(play));
//                    continue;
//                }
//                // 开始推送剧本消息
//                AppEventProcessor.sendEvent(EventType.PlayStart, play.getPlayId());
//                // 将剧本入队
//                enQueue(play.getPlayId());
//            } finally {
//                App.getBean(RedisLock.class).unlock(lockKey, lockKey);
//            }
//        }
//
//    }
//
//    @Override
//    public String createDelayQueueKey() {
//        return KEY;
//    }
//
//}
