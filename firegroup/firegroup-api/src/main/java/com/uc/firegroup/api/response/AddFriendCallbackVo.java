package com.uc.firegroup.api.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class AddFriendCallbackVo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5467770408694149443L;

    private String vcAccount;

    private String wxId;

    private String vcAddUserSerialNo;



}
