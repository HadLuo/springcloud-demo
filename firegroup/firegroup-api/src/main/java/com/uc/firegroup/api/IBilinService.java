package com.uc.firegroup.api;

import com.uc.firegroup.api.request.SendWxMsgRequest;
import com.uc.framework.Pair;
import com.uc.framework.obj.Result;

public interface IBilinService {

    /**
     * 根据wxId获取对应二维码图片 并对该微信ID进行订阅
     * 
     * @param wxId 微信ID
     * @return
     * @author 鲁志学 2020年9月14日
     */
    public Result<String> getWxQRCode(String wxId);

    /**
     * 发送私聊消息
     * 
     * @param request
     * @return
     * @author 鲁志学 2020年9月14日
     */
    public Result<Void> sendWxMsg(SendWxMsgRequest request);

    /**
     * 
     * title: 机器人是否正常 true:正常 false：冻结或者离线
     *
     * @param wxId
     * @return <是否正常，机器人数据>
     * @author HadLuo 2020-9-17 10:19:35
     */
    public Pair<Boolean, Object> robotHasEnable(String wxId);
}
