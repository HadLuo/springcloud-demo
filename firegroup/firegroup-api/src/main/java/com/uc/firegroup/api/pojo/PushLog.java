package com.uc.firegroup.api.pojo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Table(name = "t_push_log")
public class PushLog implements Serializable {
    /**
    * 
    */
    private static final long serialVersionUID = -3621190165330311331L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    /** 剧本id */
    private Integer playId;
    /** 剧本消息id */
    private Integer playMessageId;
    /** 水军微信id */
    private String robotWxId;
    /** 推送的 微信群ID */
    private String groupId;
    /** 推送状态 0-成功，1-失败 */
    private Integer pushState;
    /** 创建时间， 具体推送时间 */
    private Date createTime;
    /** 推送失败 原因 */
    private String pushErrorMsg;
    /** 发言人昵称，并不是水军群里的昵称，请注意 */
    private String personName;
    /** 商户id */
    private String merchatId;

}
