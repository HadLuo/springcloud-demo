package com.uc.firegroup.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 剧本详情 返回实体
 * @author: Ryan.yuan
 * @time: 2020/9/18/018 11:37
 */
@ApiModel("剧本详情 返回实体")
@Data
public class PlayInfoDetailResponse implements Serializable {

    @ApiModelProperty("剧本名称")
    private String playName;

    @ApiModelProperty("剧本类型 ：1.定时推送剧本 2.话术触发剧本 ")
    private Integer playType;

    @ApiModelProperty("推送时间类型：1.保存草稿 2定时推送")
    private Integer pushTimeType;

    @ApiModelProperty("剧本时长 单位秒")
    private Integer playTime;

    @ApiModelProperty("内容数量")
    private Integer contentNum;

    @ApiModelProperty("剧本触发相关信息")
    private String playKeywordRule;

    @ApiModelProperty("剧本消息详情")
    private List<PlayMessage> playMessageList;

    @ApiModelProperty("剧本发言人配置详情")
    private List<PlayRobotConfig> playRobotConfigList;

    @ApiModelProperty("推送时间")
    private Date pushTime;

    @ApiModelProperty("推送对象类型 1.按任务推送 2指定群聊推送")
    private Integer pushTargetType;

    @ApiModelProperty("推送对象Id(逗号隔开)")
    private String pushTargetId;

    @Data
    @ApiModel("剧本消息 实体")
    public static class PlayMessage implements Serializable{
        @ApiModelProperty("发言人昵称,多个发言人必须唯一，前端要校验")
        private String robotNickname;
        @ApiModelProperty("与上条消息间隔时间 单位秒")
        private Integer intervalTime;
        @ApiModelProperty("单条消息推送失败后状态：1.继续推送  2.终止推送")
        private Integer playErrorType;
        @ApiModelProperty("消息主体json对象")
        private ContentJson messageContentObj;
        @ApiModelProperty("是否需要@所有人  0-不需要  1-需要")
        private Integer callAll;
        @ApiModelProperty("发言顺序")
        private Integer messageSort;

        @Data
        @ApiModel("剧本消息内容 实体")
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

    @ApiModel("剧本发言人 实体")
    @Data
    public static class PlayRobotConfig implements Serializable {
        @ApiModelProperty("发言人昵称，多个发言人昵称必须唯一,且与消息内容对应")
        private String robotNickname;
        @ApiModelProperty("发言人设置类型 1指定个人号 2随机个人号 3 随机水军号")
        private Integer robotConfigType;
        @ApiModelProperty("指定个人号时的 微信id，多个用逗号隔开")
        private String clearWxId;
        @ApiModelProperty("备用水军号微信id， 多个用逗号隔开")
        private String backupWxId;
    }
}
