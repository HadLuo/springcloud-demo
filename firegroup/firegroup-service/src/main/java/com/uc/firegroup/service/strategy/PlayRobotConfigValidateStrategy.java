package com.uc.firegroup.service.strategy;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;
import com.google.common.collect.Lists;
import com.uc.firegroup.api.pojo.PlayRobotConfig;
import com.uc.framework.Numbers;
import com.uc.framework.obj.Result;

/**
 * 
 * title: 剧本 发言人 设置
 *
 * @author HadLuo
 * @date 2020-9-15 14:05:22
 */
public class PlayRobotConfigValidateStrategy {

    private static boolean exsists(List<PlayRobotConfig> configs) {
        ArrayList<String> ex = Lists.newArrayList();
        for (PlayRobotConfig c : configs) {
            if (ex.contains(c.getRobotNickname())) {
                return true;
            }
            ex.add(c.getRobotNickname());
        }
        return false;
    }

    public static Result<?> validate(List<PlayRobotConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return Result.err("剧本发言人设置参数错误！");
        }
        // 同一剧本的 发言人 不能 重名
        if (exsists(configs)) {
            return Result.err("剧本发言人设置昵称有重复，请修改！");
        }

        for (PlayRobotConfig config : configs) {
            if (Numbers.assertInEnums(config.getRobotConfigType(), 1, 2, 3)) {
                return Result.err("剧本发言人参数错误！");
            }
        }
        return Result.ok();
    }
}
