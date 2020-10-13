package com.uc.firegroup.api.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "t_task_user")
@ApiModel("任务用户绑定表")
public class TaskUser {
    @ApiModelProperty("自增主键")
    @Id
    private Integer taskUserId;

    @ApiModelProperty("任务ID")
    private Integer taskId;

    @ApiModelProperty("绑定的用户微信ID")
    private String wxId;

    @ApiModelProperty("商户ID")
    private String merchantId;

    @ApiModelProperty("绑定时间")
    private Date createTime;

    @ApiModelProperty("0正常 1删除")
    private Integer isDelete;

    @ApiModelProperty("绑定的水军微信ID")
    private String robotWxId;

}