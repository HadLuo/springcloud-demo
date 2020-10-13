//package com.uc.firegroup.service.mq;
//
//import java.util.Optional;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Component;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.uc.firegroup.api.pojo.PushTask;
//import com.uc.firegroup.service.inner.push.AbstractPushTaskProcessor;
//import com.uc.firegroup.service.inner.push.JobPushTaskProcessor;
//import com.uc.firegroup.service.tools.msg.MsgThreadLocals;
//import com.uc.framework.logger.Logs;
//import com.uc.framework.logger.alert.AlertContext;
//import com.uc.framework.redis.lock.RedisLock;
//import com.uc.framework.redis.queue.Task;
//
///**
// * 
// * title: 从redis取出消息发送
// *
// * @author HadLuo
// * @date 2020-9-17 17:09:00
// */
//@Component
//public class PollMsgForRedisConsumer {
//    @Autowired
//    RedisLock redisLock;
//    final AbstractPushTaskProcessor pushTaskProcessor = new JobPushTaskProcessor();
//
//    @KafkaListener(topics = "${topic_firegroup_poll_redis_msg}", groupId = "${topic_group_firegroup_poll_redis_msg}")
//    public void topic_test(ConsumerRecord<?, ?> record, Acknowledgment ack,
//            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
//        Optional<?> message = Optional.ofNullable(record.value());
//        if (message.isPresent()) {
//            Object msg = message.get();
//            Task task;
//            try {
//                task = JSON.parseObject(msg.toString(), Task.class);
//            } catch (Exception e) {
//                AlertContext.robot().alert("kafka parse message exception>>topic=" + topic + ",msg=" + msg);
//                Logs.e(getClass(), "kafka parse message exception>>topic=" + topic + ",msg=" + msg);
//                ack.acknowledge();
//                return;
//            }
//            if (task == null) {
//                AlertContext.robot().alert("kafka parse message exception>>topic=" + topic + ",msg=" + msg);
//                Logs.e(getClass(), "kafka parse message exception>>topic=" + topic + ",msg=" + msg);
//                ack.acknowledge();
//                return;
//            }
//            String key = task.getTaskId();
//            boolean ret = redisLock.lock(key, key, 30 * 24 * 3600);
//            if (!ret) {
//                ack.acknowledge();
//                return;
//            }
//            Logs.console().i(getClass(), "[kafka consumer]>>Topic:" + topic + ",Message:" + msg);
//            try {
//                // 执行自己的业务
//                try {
//                    //pushTaskProcessor.onKafkaMsgReceive(task);
//                    // 回复消费成功
//                    ack.acknowledge();
//                } catch (Throwable e) {
//                    AlertContext.robot().alert(
//                            "kafka message execute unkonw exception>>topic=" + topic + ",msg=" + msg, e);
//                    Logs.e(getClass(),
//                            "kafka message execute unkonw exception>>topic=" + topic + ",msg=" + msg, e);
//                    // 解锁 以便下次 重试
//                    redisLock.unlock(key, key);
//                }
//            } finally {
//                MsgThreadLocals.clear();
//            }
//        } else {
//            AlertContext.robot().alert("kafka message unPresent>>topic=" + topic + ",record=" + record);
//            Logs.e(getClass(), "kafka message unPresent>>topic=" + topic + ",record=" + record);
//            ack.acknowledge();
//        }
//    }
//}
