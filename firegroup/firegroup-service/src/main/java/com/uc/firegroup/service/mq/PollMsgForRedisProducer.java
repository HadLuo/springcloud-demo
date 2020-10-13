package com.uc.firegroup.service.mq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.alibaba.fastjson.JSON;
import com.uc.framework.logger.Logs;
import com.uc.framework.logger.alert.AlertContext;
import com.uc.framework.redis.queue.Task;

/**
 * 
 * title: 从redis取出消息发送
 *
 * @author HadLuo
 * @date 2020-9-17 17:09:00
 */
@Component
public class PollMsgForRedisProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value(value = "${topic_firegroup_poll_redis_msg}")
    private String topic_firegroup_poll_redis_msg;

    public void send(Task task) {
        if (task == null) {
            return;
        }
        // 发送消息
        ListenableFuture<SendResult<String, Object>> future = kafkaTemplate
                .send(topic_firegroup_poll_redis_msg, JSON.toJSONString(task));
        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onFailure(Throwable e) {
                // 发送失败的处理
                Logs.e(getClass(), "[kafka producer error]>>topic=" + topic_firegroup_poll_redis_msg + ",obj="
                        + JSON.toJSONString(task) + "", e);
                AlertContext.robot().alert("[kafka producer error]>>topic=" + topic_firegroup_poll_redis_msg
                        + ",obj=" + JSON.toJSONString(task) + "", e);
            }

            @Override
            public void onSuccess(SendResult<String, Object> stringObjectSendResult) {
                // 成功的处理
                Logs.e(getClass(), "[kafka producer]>>topic=" + topic_firegroup_poll_redis_msg + ",obj="
                        + JSON.toJSONString(task) + ",ret=" + stringObjectSendResult);
            }
        });
    }
}
