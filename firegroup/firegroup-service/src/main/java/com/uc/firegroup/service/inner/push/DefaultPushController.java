package com.uc.firegroup.service.inner.push;

import java.util.Collections;
import java.util.List;
import org.springframework.util.CollectionUtils;
import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.pojo.PushTask;
import com.uc.firegroup.service.strategy.TaskSortStrategy;
import com.uc.framework.Objects;
import com.uc.framework.logger.Logs;
import com.uc.framework.obj.Result;

public class DefaultPushController implements PushController {

    @Override
    public Result<?> validatePlay(PlayInfo play) {
        if (null == play) {
            return Result.err();
        }
        if (play.getIsScan() == 1) {
            // 剧本结束
            return Result.err();
        }
        if (play.getIsDelete() == 1) {
            // 剧本已经删除
            Logs.e(getClass(), "[push exception]>>剧本数据被删除,id=" + play.getPlayId());
            return Result.err();
        }
        // 剧本状态不是待推送 , 直接返回
        int state = Objects.wrapNull(play.getState(), 0);
        if (state != 1 && state != 2) {
            // 剧本状态 0 草稿 1待推送 2已推送 3已取消 4已暂停
            Logs.e(getClass(), "[push exception]>>剧本状态异常,id=" + play.getPlayId() + ",state=" + state);
            return Result.err();
        }
        return Result.ok();
    }

    @Override
    public void initSort(List<PushTask> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        int sort = 0;
        // 排序消息
        Collections.sort(tasks, new TaskSortStrategy());
        for (PushTask task : tasks) {
            task.setSort(sort++);
        }
    }

    @Override
    public void debug(String flag, Integer playId, String groupId, String msg) {
        Logs.e(getClass(), "[" + flag + "]>>platId=" + playId + ",groupId=" + groupId + "," + msg);
    }

}
