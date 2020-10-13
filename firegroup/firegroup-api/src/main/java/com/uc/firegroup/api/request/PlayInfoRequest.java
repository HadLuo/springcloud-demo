package com.uc.firegroup.api.request;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.uc.firegroup.api.pojo.PlayMessage;
import com.uc.firegroup.api.pojo.PlayRobotConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("剧本创建实体信息")
public class PlayInfoRequest implements Serializable {
    private static final long serialVersionUID = 6077283969412549308L;

    @ApiModelProperty("剧本名称")
    private String playName;
    @ApiModelProperty("剧本推送类型 ：1.定时推送剧本 2.话术触发剧本")
    private Integer playType;
    @ApiModelProperty("剧本消息实体集合")
    private List<PlayMessage> messages;
    @ApiModelProperty("内容总条数")
    private Integer contentNum;
    @ApiModelProperty("消息内容总时长")
    private Integer playTime;
    @ApiModelProperty("发言人设置集合")
    private List<PlayRobotConfig> playRobotConfigs;
    @ApiModelProperty("推送时间类型：1.保存草稿 2定时推送")
    private Integer pushTimeType;
    @ApiModelProperty("定时推送时间")
    private Date pushTime;
    @ApiModelProperty("推送对象类型 1.按任务推送 2指定群聊推送")
    private Integer pushTargetType;
    @ApiModelProperty("按任务推送时的任务id,按群推送就是群的微信id ,多个以逗号分隔")
    private String pushTargetId;
    @ApiModelProperty("话术触发设置实体")
    private KeyWordsRule playKeywordRule;

    @ApiModel("话术触发类型规则实体")
    @Data
    public static class KeyWordsRule {
        @ApiModelProperty("触发关键词后 多少秒开始推送")
        private Integer delaySecond;
        @ApiModelProperty("触发的开始时间")
        private Date startTime;
        @ApiModelProperty("触发的结束时间")
        private Date endTime;
        @ApiModelProperty("发搜索规则集合")
        private List<KeyWordsSearchRule> searchs;
    }

    @ApiModel("话术触发搜索规则实体")
    @Data
    public static class KeyWordsSearchRule {
        @ApiModelProperty("0-模糊匹配  1-精准匹配")
        private Integer type;
        @ApiModelProperty("具体关键字")
        private String words;
    }

}
