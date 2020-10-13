package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("群列表查询参数实体")
@Data
public class GroupInfoRequest extends BaseRequest {
    @ApiModelProperty("群状态")
    private Integer state;

    @ApiModelProperty("群名称")
    private String wxGroupName;

    @ApiModelProperty("任务ID")
    private Integer taskId;
}
