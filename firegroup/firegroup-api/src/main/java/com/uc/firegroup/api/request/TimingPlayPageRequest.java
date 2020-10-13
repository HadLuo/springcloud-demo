package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时剧本分页查询 请求类
 * @author: Ryan.yuan
 * @time: 2020/9/18/018 9:42
 */
@Data
@ApiModel("定时剧本分页查询 请求类")
public class TimingPlayPageRequest implements Serializable {

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("行数")
    private Integer rows;

    @ApiModelProperty("剧本状态 0 草稿 1待推送 2已推送 3已取消 4已暂停")
    private Integer playState;

    @ApiModelProperty("剧本名称")
    private String playName;

    @ApiModelProperty("剧本推送时间开始")
    private Date pushStartTime;

    @ApiModelProperty("剧本推送时间结束")
    private Date pushEndTime;

    @ApiModelProperty(value = "商户ID",hidden = true)
    private String createId;
}
