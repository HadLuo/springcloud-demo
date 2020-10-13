package com.uc.firegroup.service.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uc.external.bilin.res.MqCallbackMessage;
import com.uc.firegroup.api.pojo.MqMessageInfo;
import com.uc.firegroup.api.request.PullMqLogRequest;
import com.uc.firegroup.api.response.IncomeGroupCallBackResponse;
import com.uc.firegroup.api.response.Type5051Response;
import com.uc.firegroup.service.inner.chat.AbstractChatPushProssor;
import com.uc.firegroup.service.inner.chat.JobChatPushProcessor;
import com.uc.firegroup.service.inner.chat.KeyWordsChatPushProcessor;
import com.uc.firegroup.service.mapper.MqMessageInfoMapper;
import com.uc.firegroup.service.tools.msg.MessageProcessor;
import com.uc.firegroup.service.tools.msg.MsgThreadLocals;
import com.uc.framework.App;
import com.uc.framework.logger.Logs;
import com.uc.framework.logger.alert.AlertContext;
import com.uc.framework.redis.lock.RedisLock;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Optional;

@Component
public class KafkaConsumers {
    @Autowired
    MessageProcessor processor;
    @Autowired
    RedisLock redisLock;
    @Autowired
    private MqMessageInfoMapper mqMessageInfoMapper;

    @KafkaListener(topics = "${topic_callback_msg}", groupId = "${topic_group_callback_msg}")
    public void topic_test(ConsumerRecord<?, ?> record, Acknowledgment ack,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional<?> message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            Object msg = message.get();
            MqCallbackMessage callback;
            try {
                callback = JSON.parseObject(msg.toString(), MqCallbackMessage.class);
            } catch (Exception e) {
                AlertContext.robot().alert("kafka parse message exception>>topic=" + topic + ",msg=" + msg);
                Logs.e(getClass(), "kafka parse message exception>>topic=" + topic + ",msg=" + msg);
                ack.acknowledge();
                return;
            }
            if (callback == null) {
                AlertContext.robot().alert("kafka parse message exception>>topic=" + topic + ",msg=" + msg);
                Logs.e(getClass(), "kafka parse message exception>>topic=" + topic + ",msg=" + msg);
                ack.acknowledge();
                return;
            }
            String key = null;
            if (!StringUtils.isEmpty(callback.getOptSerNo())) {
                key = callback.getOptSerNo() + "." + callback.getType();
                // 幂等 锁
                boolean ret = redisLock.lock(key, key, 30 * 24 * 3600);
                if (!ret) {
                    ack.acknowledge();
                    return;
                }
            }
            Logs.console().i(getClass(), "[kafka consumer]>>Topic:" + topic + ",Message:" + msg);
            try {
                MsgThreadLocals.set(callback);
                // 判断 业务code
                if (callback.getResultCode() != 1) {
                    // 业务 有异常
                    Logs.e(getClass(), "kafka msg consumer code not sucess>>topic=" + topic + ",msg=" + msg);
                    ack.acknowledge();
                    // 解锁 以便下次 重试
                    // redisLock.unlock(key, key);
                    if(callback.getType() == 5002) {
                        Type5051Response r = (Type5051Response) callback.getData();
                        if(r != null) {
                            AbstractChatPushProssor.getProssor(KeyWordsChatPushProcessor.class).onAck(callback.getOptSerNo(),r.getVcChatRoomSerialNo() , callback.getResultMsg());
                        }else {
                            Logs.e(getClass(), "5002数据异常>>"+JSON.toJSONString(callback));
                        }
                    }
                    return;
                }
                // 执行自己的业务
                try {
                    try {
                        if (processor.containsType(callback.getType())){
                            MqMessageInfo messageInfo = mqMessageInfoMapper.selectInfoByOptId(callback.getOptSerNo());
                            Logs.e(getClass(),"查询回调信息！"+JSON.toJSONString(messageInfo));
                            if (messageInfo != null && messageInfo.getMessageType() != 4505){
                                messageInfo.setMessageState(2);
                                messageInfo.setCallbackTime(new Date());
                                messageInfo.setCallbackInfo(msg.toString());
                                mqMessageInfoMapper.updateByPrimaryKeySelective(messageInfo);
                            }
                            if (callback.getType() == 4505){
                                PullMqLogRequest request = new PullMqLogRequest();
                                request.setToWxId(callback.getWxId());
                                JSONObject jsonObject = (JSONObject) callback.getData();
                                IncomeGroupCallBackResponse response = jsonObject.toJavaObject(IncomeGroupCallBackResponse.class);
                                if (response!= null && response.getVcFriendSerialNo() != null){
                                    request.setSendWxId(response.getVcFriendSerialNo());
                                    request.setReqGroupId(response.getVcChatRoomSerialNo());
                                    MqMessageInfo info = mqMessageInfoMapper.selectInfoByPullWxId(request);
                                    if (info != null){
                                        info.setMessageState(2);
                                        info.setCallbackTime(new Date());
                                        info.setCallbackInfo(msg.toString());
                                        mqMessageInfoMapper.updateByPrimaryKeySelective(info);
                                    }
                                }
                            }
                        }
                    }catch (Exception e){
                        Logs.e(getClass(),"插入回调消息异常！");
                        e.printStackTrace();
                    }
                    // 调用 message 组件
                    processor.invoke(callback, topic);
                    // 回复消费成功
                    ack.acknowledge();
                } catch (Throwable e) {
                    AlertContext.robot().alert(
                            "kafka message execute unkonw exception>>topic=" + topic + ",msg=" + msg, e);
                    Logs.e(getClass(),
                            "kafka message execute unkonw exception>>topic=" + topic + ",msg=" + msg, e);
                    // 解锁 以便下次 重试
                    redisLock.unlock(key, key);
                }
            } finally {
                MsgThreadLocals.clear();
            }
        } else {
            AlertContext.robot().alert("kafka message unPresent>>topic=" + topic + ",record=" + record);
            Logs.e(getClass(), "kafka message unPresent>>topic=" + topic + ",record=" + record);
            ack.acknowledge();
        }
    }
}
