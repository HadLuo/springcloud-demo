package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel("群内水军列表查询实体")
public class RobotInfoGroupResponse implements Serializable {
    @ApiModelProperty("机器人主键ID")
    private Integer robotId;

    @ApiModelProperty("机器人昵称")
    private String robotNick;

    @ApiModelProperty("机器人微信ID")
    private String robotWxId;

    @ApiModelProperty("机器人头像")
    private String headImage;

    @ApiModelProperty("机器人微信号")
    private String robotWxAcc;

    @ApiModelProperty("机器人登陆状态 1 在线 2离线 3封号")
    private Integer loginState;

    @ApiModelProperty("群主键ID")
    private Integer groupId;

    @ApiModelProperty("群微信ID")
    private String groupWxId;

    @ApiModelProperty("在群内状态 1.在群内 2.已退群")
    private Integer robotGroupState;

    @ApiModelProperty("入群时间")
    private Date incomeGroupTime;

    @ApiModelProperty("退群时间")
    private Date outGroupTime;

    @ApiModelProperty("是否开通号 0 否 1是")
    private Integer isOpenGroup;

    @ApiModelProperty("账号类型 1水军 2个人号")
    private Integer openRobotType;}
