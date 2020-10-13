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
@ApiModel("剧本定时推送记录 返回实体")
public class PlayTimingPushRecordResponse {

    @ApiModelProperty("推送Id")
    private Integer pushId;

    @ApiModelProperty("微信群唯一标识")
    private String wxGroupId;

    @ApiModelProperty("群名称")
    private String groupName;

    @ApiModelProperty("所属任务")
    private String taskName;

    @ApiModelProperty("实际推送数量")
    private Integer pushNum;

    @ApiModelProperty("推送时间")
    private Date pushTime;

    @ApiModelProperty("推送状态:1:待发送 2:进行中 3:已结束 4:人工暂停 5:推送失败 6:系统暂停")
    private Integer pushState;

    @ApiModelProperty("推送失败原因")
    private String pushFailReason;
}
