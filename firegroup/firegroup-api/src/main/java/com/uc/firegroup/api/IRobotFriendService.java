package com.uc.firegroup.api;

import com.uc.framework.obj.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = Constant.instanceName, decode404 = true)
public interface IRobotFriendService {

    /**
     * 好友关系确认服务
     * @param fromWxId 主加人微信号
     * @param toWxId 被加人微信号
     * @return
     * @author 鲁志学 2020年9月09日
     */
    @GetMapping("/robotAddFriendCallback")
    public Result<Void>  robotAddFriendCallback(@RequestParam("fromWxId") String fromWxId, @RequestParam("toWxId") String toWxId);

    /**
     * 更新水军账号信息服务
     * @author 鲁志学 2020年9月10日
     */
    @GetMapping("/refreshRobotInfo")
    public void refreshRobotInfoJob();

}
