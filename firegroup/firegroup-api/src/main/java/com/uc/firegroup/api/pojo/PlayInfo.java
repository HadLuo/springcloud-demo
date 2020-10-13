package com.uc.firegroup.api.pojo;

import java.util.Date;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.util.StringUtils;

import com.uc.framework.login.UserThreadLocal;
import com.uc.framework.obj.Initialization;
import lombok.Data;

@Data
@Table(name = "t_play_info")
public class PlayInfo implements Initialization {
    /**
     *
     */
    private static final long serialVersionUID = -7670761223148207364L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer playId;
    /** 剧本推送类型 ：1.定时推送剧本 2.话术触发剧本 */
    private Integer playType;
    /** 剧本触发关键词规则json @see PlayInfoRequest.KeyWordsRule */
    private String playKeywordRule;
    /** 剧本名称 */
    private String playName;
    /** 内容时长 */
    private Integer playTime;
    /** 发言人数量 */
    private Integer robotNum;
    /** 内容数量 */
    private Integer contentNum;
    /** 选择群数量 */
    private Integer groupNum;
    /** 推送时间类型：1.保存草稿 2定时推送 */
    private Integer pushTimeType;
    /** 定时推送时间 */
    private Date pushTime;
    /** 推送对象类型 1.按任务推送 2指定群聊推送 */
    private Integer pushTargetType;
    /** 推送对象Id 逗号隔开 , 任务推送就是任务id ，否则就是微信id */
    private String pushTargetId;
    /** 剧本状态 0 草稿 1待推送 2已推送 3已取消 */
    private Integer state;
    private String createId;
    private String createName;
    private Date createTime;
    private Date modifyTime;
    private String modifyId;
    private String modifyName;
    private Integer isDelete;
    /**剧本是不是正在跑(备用号不足也要扫描) 0-正在跑 1-停止扫描*/
    private Integer isScan;
    /** 关键词类型启动状态 1 启动 2 关闭 */
    private Integer isStart;

    /** 临时字段， 实际要推送的多个群id，指定任务时，也会转换成群id */
    @Transient
    private List<String> pushGroupIds;

    @Override
    public void onInit() {
        playId = null;
        if (StringUtils.isEmpty(createId) && UserThreadLocal.get() != null) {
            createId = UserThreadLocal.get().getMerchatId();
        }
        if (StringUtils.isEmpty(createName) && UserThreadLocal.get() != null) {
            createName = UserThreadLocal.get().getUsername();
        }
        if (StringUtils.isEmpty(modifyId) && UserThreadLocal.get() != null) {
            modifyId = UserThreadLocal.get().getUsername();
        }
        if (StringUtils.isEmpty(modifyName) && UserThreadLocal.get() != null) {
            modifyName = UserThreadLocal.get().getUsername();
        }
        createTime = new Date();
        modifyTime = new Date();
        isDelete = 0;
        isStart = 1;
        isScan = 0;
    }
}