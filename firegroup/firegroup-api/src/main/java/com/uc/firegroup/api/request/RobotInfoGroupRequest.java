package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("查询群内水军请求实体")
public class RobotInfoGroupRequest extends BaseRequest {

    @ApiModelProperty("群微信ID")
    private String wxGroupId;

    @ApiModelProperty("用户登陆状态 登陆状态 1 在线 2离线 3封号")
    private Integer loginState;

    @ApiModelProperty("群内状态 1.在群内 2.已退群")
    private Integer robotGroupState;

    @ApiModelProperty("水军昵称")
    private String  robotNick;

    @ApiModelProperty("群创建时间")
    private Date createTime;

}
