package com.uc.firegroup.api.response;

import com.uc.firegroup.api.pojo.GroupInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("群列表返回实体")
public class GroupInfoResponse extends GroupInfo implements Serializable {
    @ApiModelProperty("群成员数量")
    private Integer groupPersonNum;

    @ApiModelProperty("开通号昵称")
    private String openRobotNickName;

    @ApiModelProperty("开通号微信号")
    private String openRobotWxAcc;

    @ApiModelProperty("开通号微信ID")
    private String openRobotWxId;

    @ApiModelProperty("开通号头像")
    private String openRobotImg;

    @ApiModelProperty("开通号类型 1水军 2个人号")
    private Integer openRobotType;

    @ApiModelProperty("正常水军数量")
    private Integer normalRobotNum;

    @ApiModelProperty("今日剧本")
    private Integer playDaySum;

    @ApiModelProperty("今日未执行剧本")
    private Integer playDayNum;

    @ApiModelProperty("总未执行剧本")
    private Integer playNum;
}
