package com.uc.firegroup.api.response;

import com.uc.firegroup.api.pojo.TaskInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("任务列表返回实体")
@Data
public class TaskInfoResponse extends TaskInfo implements Serializable {
    @ApiModelProperty("新群购买水军数量")
    public Integer robotNum;
    @ApiModelProperty("当前服务群数")
    public Integer groupNum;
    @ApiModelProperty("明日预计费用")
    public Integer tomorrowCost;
    @ApiModelProperty("暂停待续费的服务数量")
    public Integer stopGroupNum;
}
