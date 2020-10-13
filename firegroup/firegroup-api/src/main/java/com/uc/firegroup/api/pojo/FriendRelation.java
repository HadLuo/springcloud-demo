package com.uc.firegroup.api.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "t_friend_relation")
public class FriendRelation  implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 3511829454808179503L;

    /**
     * 自增主键
     */
    @Id
    private Integer friendRelationId;

    /**
     * 主加水军微信ID
     */
    private String fromWxId;

    /**
     * 被加水军微信ID
     */
    private String toWxId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 0 正常 1 已删除
     */
    private Integer isDelete;

    /**
     *修改时间
     */
    private Date modifyTime;

    public FriendRelation(Integer friendRelationId, String fromWxId, String toWxId, Date createTime, Integer isDelete, Date modifyTime) {
        this.friendRelationId = friendRelationId;
        this.fromWxId = fromWxId;
        this.toWxId = toWxId;
        this.createTime = createTime;
        this.isDelete = isDelete;
        this.modifyTime = modifyTime;
    }

    public FriendRelation() {
        super();
    }

    public Integer getFriendRelationId() {
        return friendRelationId;
    }

    public void setFriendRelationId(Integer friendRelationId) {
        this.friendRelationId = friendRelationId;
    }

    public String getFromWxId() {
        return fromWxId;
    }

    public void setFromWxId(String fromWxId) {
        this.fromWxId = fromWxId == null ? null : fromWxId.trim();
    }

    public String getToWxId() {
        return toWxId;
    }

    public void setToWxId(String toWxId) {
        this.toWxId = toWxId == null ? null : toWxId.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}