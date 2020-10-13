package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 剧本推送记录 返回实体
 * @author: Ryan.yuan
 * @time: 2020/9/17/017 16:51
 */
@Data
@ApiModel("剧本触发记录 返回实体")
public class PlayTriggerPushRecordResponse {

    @ApiModelProperty("推送时间")
    private Date pushTime;

    @ApiModelProperty("微信群唯一标识")
    private String wxGroupId;

    @ApiModelProperty("群名称")
    private String groupName;

    @ApiModelProperty("所属任务")
    private String taskName;

    @ApiModelProperty("推送Id")
    private Integer pushId;

    @ApiModelProperty("触发话术")
    private String triggerKeyword;

    @ApiModelProperty("剧本Id")
    private Integer playId;

    @ApiModelProperty("触发剧本")
    private String playName;

    @ApiModelProperty("发言人数量")
    private Integer robotNum;

    @ApiModelProperty("内容数量")
    private Integer contentNum;

    @ApiModelProperty("实际推送数量")
    private Integer pushNum;

    @ApiModelProperty("推送状态:1:待发送 2:进行中 3:已结束 4:人工暂停 5:推送失败 6:系统暂停")
    private Integer pushState;

    @ApiModelProperty("推送失败原因")
    private String pushFailReason;
}
