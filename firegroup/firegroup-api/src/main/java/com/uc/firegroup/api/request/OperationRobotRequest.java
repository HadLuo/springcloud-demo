package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel("操作水军请求实体")
@Data
public class OperationRobotRequest implements Serializable {
    @ApiModelProperty("群主键ID")
    private List<Integer> groupIds;

    @ApiModelProperty("添加的水军数量")
    private Integer addGroupNum;

    @ApiModelProperty("移除的水军数量")
    private Integer rmGroupNum;
}
