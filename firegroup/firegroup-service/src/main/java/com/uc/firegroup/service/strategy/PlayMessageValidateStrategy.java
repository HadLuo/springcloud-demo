package com.uc.firegroup.service.strategy;

import java.util.List;
import org.springframework.util.CollectionUtils;
import com.uc.firegroup.api.pojo.PlayMessage;
import com.uc.framework.obj.Result;

/**
 * 
 * title: 剧本消息校验
 *
 * @author HadLuo
 * @date 2020-9-15 14:05:11
 */
public class PlayMessageValidateStrategy {

    public static Result<?> validate(List<PlayMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Result.err("剧本消息参数错误！");
        }
        for (PlayMessage message : messages) {
            // 消息间隔时间 需要 >= 5s
            if (message.getIntervalTime() == null || message.getIntervalTime() < 5) {
                return Result.err("消息发送间隔时间必须大于等于5s！");
            }
            // 发送内容校验
            if (message.getMessageContentObj() == null) {
                return Result.err("消息内容不能为空！");
            }
//            if (StringUtils.isEmpty(message.getMessageContentObj().getSMateContent())) {
//                return Result.err("消息内容不能为空！");
//            }
//            if (Numbers.assertInEnums(message.getPlayErrorType(), 1, 2)) {
//                return Result.err("消息规则参数错误！");
//            }
//            if (Numbers.assertInEnums(message.getCallAll(), 0, 1)) {
//                return Result.err("消息规则参数错误！");
//            }
        }
        return Result.ok();
    }
}
