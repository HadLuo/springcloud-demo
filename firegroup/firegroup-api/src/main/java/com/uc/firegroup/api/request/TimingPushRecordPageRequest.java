package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 推送详情查询请求实体
 * @author: Ryan.yuan
 * @time: 2020/9/17/017 17:01
 */
@ApiModel("推送详情查询请求实体")
@Data
public class TimingPushRecordPageRequest implements Serializable {

    @ApiModelProperty("推送状态:1:待发送 2:进行中 3:已结束 4:人工暂停 5:推送失败 6:系统暂停")
    private Integer pushState;

    @ApiModelProperty("群名称")
    private String groupName;

    @ApiModelProperty("剧本Id")
    private Integer playId;

    @ApiModelProperty("页码")
    private Integer page;

    @ApiModelProperty("行数")
    private Integer rows;

}
