package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel("移动任务实体")
@Data
public class MoveGroupTaskRequest implements Serializable {
    @ApiModelProperty("移动的群ID")
    private List<Integer> groupIds;
    @ApiModelProperty("移动后的任务ID")
    private Integer taskId;
}
