package com.uc.firegroup.api.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 剧本推送创建 请求实体
 * @author: Ryan.yuan
 * @time: 2020/9/16/016 16:09
 */
@Data
public class PlayPushCreateRequest implements Serializable{
    /**
     * 剧本Id
     */
    private Integer playId;
    /**
     * 触发关键字
     */
    private String triggerKeyword;

    /**
     * 触发微信群Id
     */
    private String wxGroupId;
}
