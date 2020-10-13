package com.uc.firegroup.api.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "t_play_message_push")
@Data
public class PlayMessagePush {

    @Id
    private Integer id;

    private Integer playId;

    private String playName;

    private Integer playType;

    /**
     * 商户Id
     */
    private String merchatId;

    private String wxGroupId;

    private String wxGroupName;

    private Integer taskId;

    private String taskName;

    private String triggerKeyword;

    private Integer robotNum;

    private Integer contentNum;

    private Date createTime;

    private Date modifyTime;

    private Integer pushState;

    private String pushFailReason;

    private Date pushTime;

    private Integer isDelete;
}