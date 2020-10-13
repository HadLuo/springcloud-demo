package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel("商家任务信息汇总实体")
@Data
public class TaskAllResponse implements Serializable {

    @ApiModelProperty("群总数")
    private Integer groupSum;

    @ApiModelProperty("机器人总数")
    private Integer robotSum;

    @ApiModelProperty("明日预计费用")
    private Double tomorrowCostSum;

    @ApiModelProperty("商家剩余比邻币")
    private Double merchantMoney;

    @ApiModelProperty("当前待续费群组数量")
    private Integer waitGroupSum;

    @ApiModelProperty("待续费水军数量")
    private Integer waitRobotSum;

    @ApiModelProperty("待续费群组ID")
    private List<Integer> waitGroupIds;

}
