package com.uc.firegroup.api.pojo;

import com.uc.framework.obj.Initialization;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "t_play_robot_config")
@ApiModel("剧本发言人设置实体")
public class PlayRobotConfig implements Initialization {
    /**
     * 
     */
    private static final long serialVersionUID = 64223439069867943L;
    @Id
    @ApiModelProperty(hidden = true)
    private Integer id;
    /** 剧本id */
    @ApiModelProperty(hidden = true)
    private Integer playId;
    @ApiModelProperty("发言人昵称，多个发言人昵称必须唯一,且与消息内容对应")
    private String robotNickname;
    @ApiModelProperty("发言人设置类型 1指定个人号 2随机个人号 3 随机水军号")
    private Integer robotConfigType;
    @ApiModelProperty("指定个人号时的 微信id，多个用逗号隔开")
    private String clearWxId;
    @ApiModelProperty("备用水军号微信id， 多个用逗号隔开")
    private String backupWxId;
    @ApiModelProperty(hidden = true)
    private Date createTime;
    @ApiModelProperty(hidden = true)
    private Date modifyTime;
    @ApiModelProperty(hidden = true)
    private Integer isDelete;

    @Override
    public void onInit() {
        id = null;
        Date date = new Date();
        createTime = date;
        modifyTime = date;
        isDelete = 0;
    }
}