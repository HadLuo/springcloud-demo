package com.uc.firegroup.api.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;


@Data
@Table(name = "t_robot_info")
@ApiModel("水军信息详情")
public class RobotInfo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4949895647482834403L;

    /**
     *
     */
    @Id
    @ApiModelProperty("自增主键")
    private Integer robotId;

    @ApiModelProperty("水军微信ID")
    private String wxId;

    @ApiModelProperty("好友数量")
    private Integer friendNum;

    /**
     * 0 正常 1 已删除 2已冻结
     */
    @ApiModelProperty("0 正常 1 已删除 2已冻结")
    private Integer state;

    /**
     * 水军微信号头像
     */
    @ApiModelProperty("水军微信号头像")
    private String headImage;

    /**
     * 商家编码
     */
    @ApiModelProperty("商家编码")
    private String merchantId;

    /**
     * 微信号
     */
    @ApiModelProperty("微信号")
    private String wxAcc;

    /**
     *
     */
    @ApiModelProperty("昵称")
    private String wxNick;

    /**
     * 入群数量
     */
    @ApiModelProperty("入群数量")
    private Integer groupNum;

    @ApiModelProperty("登陆状态  1 在线 2离线 3封号")
    private Integer loginState;

    /**
     * 创建时间
     */
    @ApiModelProperty("更新批次id(存储时间戳)")
    private Long updateBatchId;

    public  RobotInfo(){

    }


}