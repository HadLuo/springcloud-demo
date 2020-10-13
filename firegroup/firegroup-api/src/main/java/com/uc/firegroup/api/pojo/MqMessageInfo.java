package com.uc.firegroup.api.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "t_mq_message_info")
public class MqMessageInfo {
    @Id
    private Integer id;

    private Integer messageType;

    private String messageReq;

    private String messageRes;

    private Integer messageState;

    private Date callbackTime;

    private Date createTime;

    private String sendWxId;

    private String reqGroupId;

    private String messageOptId;

    private String callbackInfo;

    private String toWxId;

}