package com.uc.framework.chat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/***
 * 
 * title: 单个发言人在单个群要发送的聊天消息包
 *
 * @author HadLuo
 * @date 2020-9-27 11:06:08
 */
public class Chat implements Serializable {
    private static final long serialVersionUID = 5771750665298990336L;
    /** 开始存储的时间 */
    private long createTime;
    /** 关联的参数 */
    private Serializable arg;
    /** 延时时间 */
    private int delay;
    /** 需要发送的 所有微信 群 id */
    private Set<String> needSendGroupWxIds;
    /** 当前发言人排序 */
    private int sort;
    /** 业务唯一标识 ，标识这一个聊天剧本(包含多个聊天包) */
    private String groupUuid;
    /** 当前要发送的微信群id */
    private String currentSendGroupWxId;
    /** 最大包 */
    private int limit;

    public Chat() {
    }

    public Chat(int delay, int sort, Set<String> needSendGroupWxIds) {
        this.createTime = System.currentTimeMillis();
        this.delay = delay;
        this.needSendGroupWxIds = needSendGroupWxIds;
        this.sort = sort;
    }

    public Chat(String groupUuid, int delay, int sort, String needSendGroupWxIds) {
        this.createTime = System.currentTimeMillis();
        this.delay = delay;
        this.needSendGroupWxIds = new HashSet<String>(
                Arrays.asList(StringUtils.split(needSendGroupWxIds, ",")));
        this.sort = sort;
        this.groupUuid = groupUuid;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public Serializable getArg() {
        return arg;
    }

    public void setArg(Serializable arg) {
        this.arg = arg;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public Set<String> getNeedSendGroupWxIds() {
        return needSendGroupWxIds;
    }

    public void setNeedSendGroupWxIds(Set<String> needSendGroupWxIds) {
        this.needSendGroupWxIds = needSendGroupWxIds;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    public String getCurrentSendGroupWxId() {
        return currentSendGroupWxId;
    }

    public void setCurrentSendGroupWxId(String currentSendGroupWxId) {
        this.currentSendGroupWxId = currentSendGroupWxId;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "Chat [createTime=" + createTime + ", arg=" + arg + ", delay=" + delay
                + ", needSendGroupWxIds=" + needSendGroupWxIds + ", sort=" + sort + ", groupUuid=" + groupUuid
                + ", currentSendGroupWxId=" + currentSendGroupWxId + ", limit=" + limit + "]";
    }

}
