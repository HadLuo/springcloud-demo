package com.uc.firegroup.api.request;

import java.io.Serializable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Api("剧本关联群的批量 暂停，继续 实体参数")
public class BatchPlayPauseResume implements Serializable {
    private static final long serialVersionUID = -7520998757712048030L;

    @ApiModelProperty("剧本id")
    private Integer playId;
    @ApiModelProperty("微信群id")
    private String groupWxId;
    @ApiModelProperty("0-暂停 1-继续")
    private int op;
}
