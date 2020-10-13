package com.uc.firegroup.api.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.uc.framework.obj.Initialization;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Table(name = "t_play_message")
@ApiModel("剧本消息实体")
public class PlayMessage implements Initialization {
    private static final long serialVersionUID = 8026239963848514827L;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @ApiModelProperty(hidden = true)
    private Integer playMessageId;
    /** 剧本ID */
    @ApiModelProperty(hidden = true)
    private Integer playId;
    @ApiModelProperty("发言人昵称,多个发言人必须唯一，前端要校验")
    private String robotNickname;
    @ApiModelProperty("与上条消息间隔时间 单位秒")
    private Integer intervalTime;
    @ApiModelProperty("单条消息推送失败后状态：1.继续推送  2.终止推送")
    private Integer playErrorType;
    /** 消息主体json , 数据库字段，不暴露前端 */
    @ApiModelProperty(hidden = true)
    private String messageContent;
    @ApiModelProperty("消息主体json对象")
    private ContentJson messageContentObj;
    @ApiModelProperty("是否需要@所有人  0-不需要  1-需要")
    private Integer callAll;
    @ApiModelProperty("发言顺序,值越大越在后面发")
    private Integer messageSort;
    @ApiModelProperty(hidden = true)
    private Integer isDelete;
    @ApiModelProperty(hidden = true)
    private Date createTime;
    @ApiModelProperty(hidden = true)
    private Date modifyTime;

    @Override
    public void onInit() {
        playMessageId = null;
        createTime = new Date();
        modifyTime = new Date();
        isDelete = 0;
        if (StringUtils.isEmpty(getMessageContent())) {
            setMessageContent(JSON.toJSONString(getMessageContentObj()));
        }
        if(getCallAll() == null) {
            setCallAll(0);
        }
    }

    /***
     * 
     * title: 前端传的 messageContent字段 对应 json ，
     *
     * @author HadLuo
     * @date 2020-9-18 9:19:30
     */
    @Data
    public static class ContentJson implements Serializable {
        private static final long serialVersionUID = -6434841971083561503L;
        @ApiModelProperty("消息内容")
        private String sMateContent;
        @ApiModelProperty("消息类型: 2001 文字 2002 图片 2003 语音(只支持amr格式) 2004 视频 2005 链接 2006 好友名片 2010 文件 2013 小程序 2016 音乐 ")
        private Integer momentTypeId;
        @ApiModelProperty("图片消息url ")
        private String sMateImgUrl;
        @ApiModelProperty("语音时长")
        private Integer sMateBVLen;
        @ApiModelProperty("语音链接")
        private String sMateAwrUrl;
        private String sMateVUrl ;
        private String sMateId ;
        private String sMateTitle ;
        private String title ;
    }
}