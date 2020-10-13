package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 触发剧本分页查询返回
 * @author: Ryan.yuan
 * @time: 2020/9/18/018 9:42
 */
@Data
@ApiModel("触发剧本分页查询返回")
public class TriggerPlayPageResponse implements Serializable {

    @ApiModelProperty("剧本Id")
    private Integer playId;

    @ApiModelProperty("剧本名称")
    private String playName;

    @ApiModelProperty("剧本时长")
    private Integer playTime;

    @ApiModelProperty("发言人数量")
    private Integer robotNum;

    @ApiModelProperty("内容数量")
    private Integer contentNum;

    @ApiModelProperty("群数量")
    private Integer groupNum;

    @ApiModelProperty("触发开始时间")
    private Date triggerStartTime;

    @ApiModelProperty("触发结束时间")
    private Date triggerEndTime;

    @ApiModelProperty("触发状态:1 启动 2 关闭")
    private Integer triggerState;

    @ApiModelProperty("剧本状态 0 草稿 1待推送 2已推送 3已取消 4已暂停")
    private Integer state;

    @ApiModelProperty("创建人名称")
    private String createName;

    @ApiModelProperty("创建人时间")
    private Date createTime;

}
