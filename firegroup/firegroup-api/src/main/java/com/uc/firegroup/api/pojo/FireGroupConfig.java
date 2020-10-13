package com.uc.firegroup.api.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "t_fire_group_config")
@ApiModel("全局配置表")
public class FireGroupConfig  implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -4475105367858591390L;

    /**
     *
     */
    @Id
    @ApiModelProperty("自增主键")
    private Integer configId;

    /**
     *
     */
    @ApiModelProperty("每天水军主动加好友次数上限")
    private Integer fromAddCount;

    /**
     *
     */
    @ApiModelProperty("水军连续加N天后需要停1天")
    private Integer addDays;

    /**
     *
     */
    @ApiModelProperty("每天水军被动加好友次数上限")
    private Integer toAddCount;

    /**
     *
     */
    @ApiModelProperty("每个水军好友数量上限")
    private Integer robotFriendNum;

    /**
     *
     */
    @ApiModelProperty("每天所有水军最多加好友次数上限")
    private Integer robotFriendCount;

    /**
     *
     */
    @ApiModelProperty("加好友定时任务开关 0 关 1开")
    private Integer friendSwitch;

    /**
     *
     */
    @ApiModelProperty("水军最多服务群数")
    private Integer robotGroupCount;

    @ApiModelProperty("水军每天每个所需比邻币")
    private BigDecimal robotDayMoney;

    @ApiModelProperty("水军每天最多加群数")
    private Integer robotDayCount;

    /**
     *
     */
    @ApiModelProperty("修改时间")
    private Date modifyTime;

    /**
     *
     */
    @ApiModelProperty("创建时间")
    private Date createTime;

    public FireGroupConfig(){

    }




}