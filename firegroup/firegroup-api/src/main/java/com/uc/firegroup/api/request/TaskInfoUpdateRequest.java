package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("修改任务请求实体")
@Data
public class TaskInfoUpdateRequest implements Serializable {
    @ApiModelProperty("任务ID")
    private Integer taskId;

    @ApiModelProperty("新群购买水军数量")
    private Integer robotNum;

    @ApiModelProperty("任务名称")
    private String taskName;
}
