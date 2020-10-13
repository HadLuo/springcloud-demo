package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@ApiModel("任务请求类")
@Data
public class TaskInfoRequest extends BaseRequest implements Serializable {
    @ApiModelProperty("任务名称")
    public String taskName;

    @ApiModelProperty("创建人ID")
    private String createId;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("删除的任务ID")
    private List<Integer> delTaskIds;

    @ApiModelProperty("转移后的任务ID")
    private Integer moveTaskId;
}
