package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("请求实体")
@Data
public class OpenGroupRequest implements Serializable {

    /**
     *
     */
    @ApiModelProperty("机器人ID")
    private String robotWxId;
    /**
     *
      */
    @ApiModelProperty("微信群ID")
    private String wxGroupId;

}
