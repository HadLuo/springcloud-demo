package com.uc.firegroup.api.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "t_group_info")
@ApiModel("群信息")
public class GroupInfo {

    @ApiModelProperty("自增主键")
    @Id
    private Integer groupId;

    @ApiModelProperty("微信群唯一标识")
    private String wxGroupId;

    @ApiModelProperty("微信群名称")
    private String wxGroupName;

    @ApiModelProperty("所属任务ID")
    private Integer taskId;

    @ApiModelProperty("状态：1正在使用 2 暂停服务待续费 3停止服务")
    private Integer state;

    @ApiModelProperty("0正常 1删除")
    private Integer isDelete;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改时间")
    private Date modifyTime;

    @ApiModelProperty("群分组ID")
    private String groups;

    @ApiModelProperty("群分组名称")
    private String groupName;

    @ApiModelProperty("群类型：1个人微信群 2企业微信群")
    private Integer groupType;

    @ApiModelProperty("已购水军数量")
    private Integer robotNum;

    @ApiModelProperty("最后一次购买水军数量")
    private Integer lastBuyRobotNum;

    @ApiModelProperty("最后一次移除水军数量")
    private Integer lastDelRobotNum;

    @ApiModelProperty("开通号微信ID")
    private String openRobotWxId;

    @ApiModelProperty("暂停服务时间")
    private Date pauseTime;
}