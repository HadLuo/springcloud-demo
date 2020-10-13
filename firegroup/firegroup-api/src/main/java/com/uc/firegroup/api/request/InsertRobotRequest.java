package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel("一键添加水军实体")
@Data
public class InsertRobotRequest implements Serializable {
    @ApiModelProperty("微信群ID")
    private String wxGroupId;

    @ApiModelProperty("添加入群数量")
    private Integer addRobotNum;
}
