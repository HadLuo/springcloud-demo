package com.uc.firegroup.service.inner.push;

import java.util.List;

import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.pojo.PushTask;
import com.uc.framework.obj.Result;

/***
 * 
 * title: 推送 控制器
 *
 * @author HadLuo
 * @date 2020-9-23 17:41:46
 */
public interface PushController {

    /**
     * 
     * title: 推送调试 信息
     *
     * @param msg
     * @author HadLuo 2020-9-23 17:43:49
     */
    public void debug(String flag , Integer playId,String groupId,String msg);

    /**
     * 
     * title: 校验剧本 是否可用
     *
     * @param play
     * @return
     * @author HadLuo 2020-9-23 17:54:13
     */
    public Result<?> validatePlay(PlayInfo play);
    
    
    public void initSort(List<PushTask> tasks);

}
