package com.uc.firegroup.api.pojo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;

@Data
@Table(name = "t_push_task")
public class PushTask implements Serializable {
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
    /** 微信群ID,多个以逗号隔开 */
    private String wxGroupId;
    /** 发言人信息json 对象 @see PlayRobotConfig */
    private String robots;
    /** 发言人数量 */
    private Integer robotsSize;
    /** 要推的群的数量 */
    private Integer groupSize;
    private Integer isDelete;
    private Date createTime;
    private String merchatId;
    @Transient
    private PlayMessage playMessage;
    /** 当前的发言人 */
    @Transient
    private PlayRobotConfig currentRobot;
    @Transient
    /** 发言人顺序 */
    private int sort;
    @Transient
    // 当前触发的群id
    private String currentGroup;
}
