package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 触发记录查询请求实体
 * @author: Ryan.yuan
 * @time: 2020/9/17/017 17:01
 */
@ApiModel("触发记录查询请求实体")
@Data
public class TriggerPushRecordPageRequest implements Serializable {

    @ApiModelProperty("推送状态:2:进行中 3:已结束 4:人工暂停 5:推送失败 6:系统暂停")
    private Integer pushState;

    @ApiModelProperty("群名称")
    private String groupName;

    @ApiModelProperty("任务名称")
    private String taskName;

    @ApiModelProperty("剧本名称")
    private String PlayName;

    @ApiModelProperty("开始时间")
    private Date startDate;

    @ApiModelProperty("结束时间")
    private Date endDate;

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("行数")
    private Integer rows;

    @ApiModelProperty(value = "商户ID",hidden = true)
    private String createId;

}
