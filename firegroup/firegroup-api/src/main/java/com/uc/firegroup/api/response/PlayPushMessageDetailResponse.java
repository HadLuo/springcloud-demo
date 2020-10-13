package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 剧本消息推送明细 返回实体
 * @author: Ryan.yuan
 * @time: 2020/9/17/017 20:26
 */
@Data
@ApiModel("剧本消息推送明细 返回实体")
public class PlayPushMessageDetailResponse implements Serializable {

    @ApiModelProperty("发言人名称")
    private String robotNickname;

    @ApiModelProperty("与上条消息间隔时间")
    private Integer intervalTime;

    @ApiModelProperty("消息失败策略:1.继续推送  2.终止推送")
    private Integer playErrorType;

    @ApiModelProperty("发言顺序")
    private Integer messageSort;

    @ApiModelProperty("消息内容")
    private String messageContent;

    @ApiModelProperty("是否需要@所有人  0-不需要  1-需要")
    private Integer callAll;

    @ApiModelProperty("消息推送状态:0:待发送 1:发送成功 2:发送失败")
    private Integer sendState;

}
