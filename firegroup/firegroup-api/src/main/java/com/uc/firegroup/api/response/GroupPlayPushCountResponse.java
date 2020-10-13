package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 群推送任务数 响应实体
 * @author: Ryan.yuan
 * @time: 2020/9/22/022 10:44
 */
@ApiModel("群推送任务数 响应实体")
@Data
public class GroupPlayPushCountResponse implements Serializable {

    @ApiModelProperty("今天完成推送剧本数")
    private Integer todayFinishPushCount;

    @ApiModelProperty("今天推送剧本总数")
    private Integer todayPushTotalCount;

    @ApiModelProperty("待推送剧本总数")
    private Integer waitPushTotalCount;

    public GroupPlayPushCountResponse(Integer todayFinishPushCount, Integer todayPushTotalCount, Integer waitPushTotalCount) {
        this.todayFinishPushCount = todayFinishPushCount;
        this.todayPushTotalCount = todayPushTotalCount;
        this.waitPushTotalCount = waitPushTotalCount;
    }
}
