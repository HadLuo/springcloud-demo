package com.uc.firegroup.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Ryan.yuan
 * @time: 2020/9/18/018 14:27
 */
@Data
@ApiModel("剧本修改实体信息")
public class PlayInfoUpdateRequest extends PlayInfoRequest implements Serializable {

    @ApiModelProperty("剧本Id")
    private Integer playId;
}
