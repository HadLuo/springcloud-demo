package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 剧本推送机器人分配信息 返回实体
 * @author: Ryan.yuan
 * @time: 2020/9/17/017 21:18
 */
@Data
@ApiModel("剧本推送机器人分配信息 返回实体")
public class PlayPushRobotAllocatedResponse implements Serializable {

    @ApiModelProperty("发言人名称")
    private String robotNickname;

    @ApiModelProperty("匹配微信名称")
    private String wxNickname;

    @ApiModelProperty("匹配微信号头像")
    private String wxImgUrl;

    @ApiModelProperty("匹配微信号")
    private String wxAcc;

    @ApiModelProperty("账号来源:1.水军 2.个人号")
    private Integer accSource;

    @ApiModelProperty("微信号状态:0.离线 1.在线")
    private Integer wxState;

}
