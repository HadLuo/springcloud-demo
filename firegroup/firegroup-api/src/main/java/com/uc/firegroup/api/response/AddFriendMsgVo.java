package com.uc.firegroup.api.response;

import lombok.Data;

@Data
public class AddFriendMsgVo {
    /**
     * 头像
     */
    public String vcHeadImgUrl;

    /**
     * 昵称
     */
    public String vcNickName;

    /**
     * 序列号
     */
    public String vcFriendSerialNo;

    /**
     *
     */
    public String vcWxAlias;

    /**
     * 原始微信号
     */
    public String vcFriendWxId;
}
