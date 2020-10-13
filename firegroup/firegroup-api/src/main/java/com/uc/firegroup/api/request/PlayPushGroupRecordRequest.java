package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 剧本群推送任务信息 请求实体
 * @author: Ryan.yuan
 * @time: 2020/9/22/022 16:29
 */
@Data
@ApiModel("剧本群推送任务信息 请求实体")
public class PlayPushGroupRecordRequest implements Serializable {

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("行数")
    private Integer rows;

    @ApiModelProperty("剧本推送时间开始")
    private Date pushStartTime;

    @ApiModelProperty("剧本推送时间结束")
    private Date pushEndTime;

    @ApiModelProperty("推送状态:2:进行中 3:已结束 4:人工暂停 5:推送失败 6:系统暂停")
    private Integer pushState;

    @ApiModelProperty("剧本名称")
    private String playName;

    @ApiModelProperty("微信群唯一标识")
    private String wxGroupId;

}
