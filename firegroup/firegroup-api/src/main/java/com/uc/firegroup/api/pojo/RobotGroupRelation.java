package com.uc.firegroup.api.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "t_robot_group_relation")
@ApiModel("水军群关系")
public class RobotGroupRelation implements Serializable {
    @ApiModelProperty("自增主键")
    @Id
    private Integer robotGroupRelationId;

    @ApiModelProperty("水军微信ID")
    private String robotWxId;

    @ApiModelProperty("微信群ID")
    private String wxGroupId;

    @ApiModelProperty("水军群内昵称")
    private String robotGroupName;

    @ApiModelProperty("入群时间")
    private Date incomeGroupTime;

    @ApiModelProperty("退群时间")
    private Date outGroupTime;

    @ApiModelProperty("状态：1.在群内 2.已退群")
    private Integer state;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改时间")
    private Date modifyTime;

    @ApiModelProperty("0正常 1删除")
    private Integer isDelete;

    public  RobotGroupRelation(){

    }
}