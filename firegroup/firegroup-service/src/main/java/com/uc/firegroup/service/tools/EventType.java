package com.uc.firegroup.service.tools;

public final class EventType {
    /** 单条发言人消息 推送失败 */
    public static final int PushMsgError = 0;
    /** 单条发言人消息 推送成功 */
    public static final int PushMsgSuccess = 1;
    /** 关键词触发 */
    public static final int keyWordsActive = 2;
    /** 单条发言人消息 开始推消息了 */
    public static final int startPush = 3;
    /** 剧本完成 */
    public static final int PlayFinish = 6;
    /** 剧本开始 */
    public static final int PlayStart = 7;
    /** 群推送完成 */
    public static final int groupPushFinish = 9;
    /** 群维度推送 失败 */
    public static final int groupPushError = 8;
    /** 群维度系统暂停 */
    public static final int groupSysPause = 4;
    /** 群维度系统暂停恢复 */
    public static final int groupSysResume = 5;

}
