package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("企业微信添加入参实体")
public class EnterpriseGroupRequest implements Serializable {
    @ApiModelProperty("任务ID")
    private Integer taskId;
    @ApiModelProperty("入群水军数量")
    private Integer robotNum;
    @ApiModelProperty("企业微信ID")
    private String wxGroupId;
    @ApiModelProperty("微信群名称")
    private String wxGroupName;
    @ApiModelProperty("企业微信分组ID")
    private String groups;
    @ApiModelProperty("企业微信分组名称")
    private String groupName;
    @ApiModelProperty("开通号微信ID")
    private String openGroupWxId;
}
