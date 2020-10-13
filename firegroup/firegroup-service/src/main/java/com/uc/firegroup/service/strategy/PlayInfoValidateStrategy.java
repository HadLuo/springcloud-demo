package com.uc.firegroup.service.strategy;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.uc.firegroup.api.request.PlayInfoRequest;
import com.uc.firegroup.api.request.PlayInfoRequest.KeyWordsRule;
import com.uc.firegroup.api.request.PlayInfoRequest.KeyWordsSearchRule;
import com.uc.framework.Numbers;
import com.uc.framework.Objects;
import com.uc.framework.obj.Result;

import java.util.Date;

/**
 * 
 * title: 剧本 校验
 *
 * @author HadLuo
 * @date 2020-9-15 14:02:40
 */
public class PlayInfoValidateStrategy {

    public static Result<?> validate(PlayInfoRequest request) {
        if (StringUtils.isEmpty(request.getPlayName()) || request.getPlayName().length() > 20) {
            return Result.err("剧本名称参数不能为空且在20字以内！");
        }
        if (Numbers.assertInEnums(request.getPlayType(), 1, 2)) {
            return Result.err("推送类型参数错误！");
        }
        if (request.getPlayType() == 2) {
            // 话术触发 校验
            KeyWordsRule rule = request.getPlayKeywordRule();
            if (rule == null) {
                // 话术触发 ， 关键词不能为空
                return Result.err("话术触发参数错误！");
            }
            if (Objects.wrapNull(rule.getDelaySecond(), 0) <= 0) {
                return Result.err("话术触发参数错误！");
            }
            if (rule.getEndTime() == null || null == rule.getStartTime()) {
                return Result.err("话术触发参数错误！");
            }
            if (rule.getEndTime().getTime() <= rule.getStartTime().getTime()) {
                return Result.err("话术触发参数错误！");
            }
            if (CollectionUtils.isEmpty(rule.getSearchs())) {
                return Result.err("话术触发参数错误！");
            }
            for (KeyWordsSearchRule search : rule.getSearchs()) {
                if (search.getType() != 0 && search.getType() != 1) {
                    return Result.err("话术触发参数错误！");
                }
                if (StringUtils.isEmpty(search.getWords())) {
                    return Result.err("话术触发参数错误！");
                }
            }
        }else if(request.getPushTimeType() == 2){ ///定时推送且不为草稿状态
            if(request.getPushTime() == null){
                return Result.err("推送时间参数为空");
            }
            if(new Date().after(request.getPushTime())){
                return Result.err("推送时间需大于当前时间");
            }
        }
        if (Objects.wrapNull(request.getContentNum(), 0) <= 0) {
            return Result.err("内容总条数必须大于0！");
        }
        if (Objects.wrapNull(request.getPlayTime(), 0) <= 0) {
            return Result.err("消息总时长必须大于0！");
        }
        if (Numbers.assertInEnums(request.getPushTargetType(), 1, 2)) {
            return Result.err("推送对象类型参数错误！");
        }
        if(request.getPushTimeType() != 1 && StringUtils.isEmpty(request.getPushTargetId())){
                return Result.err("推送对象参数错误！");
        }

        // 校验剧本 消息
        Result<?> r = PlayMessageValidateStrategy.validate(request.getMessages());
        if (!r.successful()) {
            return r;
        }
        // 校验发言人 设置
        r = PlayRobotConfigValidateStrategy.validate(request.getPlayRobotConfigs());
        if (!r.successful()) {
            return r;
        }
        return Result.ok();
    }

    /***
     * 
     * title: 检测异常 群
     *
     * @return
     * @author HadLuo 2020-9-16 11:14:22
     */
    public static Result<?> validateExcepptionGroup() {
        return null;
    }

}
