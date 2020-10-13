package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 触发剧本分页查询 请求类
 * @author: Ryan.yuan
 * @time: 2020/9/18/018 9:42
 */
@Data
@ApiModel("触发剧本分页查询 请求类")
public class TriggerPlayPageRequest implements Serializable {

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("行数")
    private Integer rows;

    @ApiModelProperty("触发状态:1 启动 2 关闭")
    private Integer triggerState;

    @ApiModelProperty("剧本名称")
    private String playName;

    @ApiModelProperty(value = "商户ID",hidden = true)
    private String createId;
}
