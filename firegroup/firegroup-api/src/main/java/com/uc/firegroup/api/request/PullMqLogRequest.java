package com.uc.firegroup.api.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class PullMqLogRequest implements Serializable {
    private String sendWxId;

    private String toWxId;

    private String reqGroupId;
}
