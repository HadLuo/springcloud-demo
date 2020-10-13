package com.uc.firegroup.api.response;

import lombok.Data;

import java.util.List;

@Data
public class IncomeGroupCallBackResponse {
    /**
     * 好友昵称
     */
    public String vcNickName;

    /**
     * 好友微信号
     */
    public String vcFriendSerialNo;

    /**
     * 微信群ID
     */
    public String vcChatRoomSerialNo;

    /**
     * 微信群名称
     */
    public String vcChatRoomName;

    /**
     * 自己的微信ID
     */
    public String wxId;

    /**
     * 群二维码
     */
    public String vcChatRoomQRCode;

    public List<Members> Members;

    public static  class Members{

        private String vcMemberUserSerialNo;

        private String vcMemberUserWxId;

        public String getVcMemberUserSerialNo() {
            return vcMemberUserSerialNo;
        }

        public void setVcMemberUserSerialNo(String vcMemberUserSerialNo) {
            this.vcMemberUserSerialNo = vcMemberUserSerialNo;
        }

        public String getVcMemberUserWxId() {
            return vcMemberUserWxId;
        }

        public void setVcMemberUserWxId(String vcMemberUserWxId) {
            this.vcMemberUserWxId = vcMemberUserWxId;
        }
    }
}
