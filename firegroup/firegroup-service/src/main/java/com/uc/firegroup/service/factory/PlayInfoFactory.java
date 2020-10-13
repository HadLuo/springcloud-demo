package com.uc.firegroup.service.factory;

import com.alibaba.fastjson.JSON;
import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.request.PlayInfoRequest;
import com.uc.firegroup.api.request.PlayInfoUpdateRequest;
import com.uc.framework.login.UserThreadLocal;
import com.uc.framework.natives.Classes;

/**
 * 
 * title: 剧本 bean工厂
 *
 * @author HadLuo
 * @date 2020-9-18 10:24:59
 */
public class PlayInfoFactory {
    /**
     * 
     * title: 根据前端 页面传的 PlayInfoRequest 来构造
     *
     * @param request
     * @return
     * @author HadLuo 2020-9-18 10:26:10
     */
    public static PlayInfo create(PlayInfoRequest request, int groupNum) {
        PlayInfo playInfo = new PlayInfo();
        // 拷贝一些基础字段
        Classes.mergeBean(request, playInfo);
        playInfo.setPlayKeywordRule(JSON.toJSONString(request.getPlayKeywordRule()));
        playInfo.setRobotNum(request.getPlayRobotConfigs().size());
        // 初始化字段
        playInfo.onInit();
        if(request.getPlayType() == 2){//触发剧本
            playInfo.setPushTimeType(0);
        }
        if (request.getPushTimeType() == 1) {
            // 草稿状态
            playInfo.setState(0);
        } else {
            playInfo.setState(1);
        }
        // 选择的群的数量
        playInfo.setGroupNum(groupNum);
        return playInfo;
    }


    /**
     *
     * title: 根据前端 页面传的 PlayInfoRequest 来构造
     *
     * @param request
     * @return
     * @author HadLuo 2020-9-18 10:26:10
     */
    public static PlayInfo create(PlayInfoUpdateRequest request, int groupNum) {
        PlayInfo playInfo = new PlayInfo();
        // 初始化字段
        if (UserThreadLocal.get() != null) {
            playInfo.setModifyName(UserThreadLocal.get().getUsername());
        }
        // 拷贝一些基础字段
        Classes.mergeBean(request, playInfo);
        playInfo.setPlayKeywordRule(JSON.toJSONString(request.getPlayKeywordRule()));
        playInfo.setRobotNum(request.getPlayRobotConfigs().size());
        // 初始化字段
        if(request.getPlayType() == 2){//触发剧本
            playInfo.setPushTimeType(0);
        }
        if (request.getPushTimeType() == 1) {
            // 草稿状态
            playInfo.setState(0);
        } else {
            playInfo.setState(1);
        }
        // 选择的群的数量
        playInfo.setGroupNum(groupNum);
        return playInfo;
    }

}
