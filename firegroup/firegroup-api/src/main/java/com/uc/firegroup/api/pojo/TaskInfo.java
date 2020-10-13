package com.uc.firegroup.api.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "t_task_info")
@ApiModel("任务信息表")
public class TaskInfo  implements Serializable {
    @Id
    @ApiModelProperty("自增主键")
    private Integer taskId;

    @ApiModelProperty("任务名称")
    private String taskName;

    @ApiModelProperty("验证码")
    private String verificationCode;

    @ApiModelProperty("创建人ID")
    private String createId;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("0正常 1删除")
    private Integer isDelete;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改时间")
    private Date modifyTime;

    @ApiModelProperty("新群购买水军数量")
    private Integer robotNum;

}