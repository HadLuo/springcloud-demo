package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("获取当前满足条件的小助手二维码实体")
@Data
public class RobotWxQRCodeResponse implements Serializable {
    @ApiModelProperty("二维码路径")
    public String wxQRCode;

    @ApiModelProperty("可入群次数")
    public Integer number;

    @ApiModelProperty("微信号")
    public String wxAcc;
}
