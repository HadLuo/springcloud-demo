package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("群信息实体")
public class GroupAllByMerchantRequest implements Serializable {
    private static final long serialVersionUID = -5237680394151420440L;

    @ApiModelProperty("微信群唯一标识")
    private String wxGroupId;

    @ApiModelProperty("自增主键")
    private Integer groupId;

    @ApiModelProperty("微信群名称")
    private String wxGroupName;

    @ApiModelProperty("所属任务ID")
    private Integer taskId;

    @ApiModelProperty("群类型：1个人微信群 2企业微信群")
    private Integer groupType;

    @ApiModelProperty("已购水军数量")
    private Integer robotNum;

    @ApiModelProperty("任务名称")
    private String taskName;
    
    @ApiModelProperty("群状态：1正在使用 2 暂停服务待续费 3停止服务")
    private Integer state;

    @ApiModelProperty("正常水军数")
    private int normalRobots;

    @ApiModelProperty("异常水军数")
    private int exceptionRobots;

    @ApiModelProperty("异常原因")
    private String cause;
    @ApiModelProperty("缺少水军数量")
    private int gapRobotNum ;
}
