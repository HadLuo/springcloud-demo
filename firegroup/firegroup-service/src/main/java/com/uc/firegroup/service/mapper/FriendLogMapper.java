package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.FriendLog;
import com.uc.firegroup.api.request.FriendLogRequest;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

public interface FriendLogMapper extends BaseMapper<FriendLog> {


    /**
     * 根据日期查询该水军wxId主动加好友的操作日志信息
     * @param request
     * @return
     * @author 鲁志学 2020年9月09日
     */
    public FriendLog selectFriendLogFromByDay(FriendLogRequest request);

      /**
     * 根据日期查询该水军wxId被加好友的操作日志信息
     * @param request
     * @return
     * @author 鲁志学 2020年9月09日
     */
    public FriendLog selectFriendLogToByDay(FriendLogRequest request);

    /**
     * 根据日期查询该水军wxId最后一条的操作日志信息
     * @param request
     * @return
     * @author 鲁志学 2020年9月09日
     */
    public FriendLog selectFriendLogAllByDay(FriendLogRequest request);

    /**
     * 查询指定时间有多少条水军加好友操作记录
     * @param date
     * @return
     * @author 鲁志学 2020年9月09日
     */
    public Integer selectTodayCount(String date);

    /**
     * 根据传入的主加微信ID跟被加微信ID查询操作记录
     * @param fromWxId 主加WXID
     * @param toWxId 被加WXID
     * @return
     * @author 鲁志学 2020年9月10日
     */
    public FriendLog selectOneLogByWxId(@Param("fromWxId")String fromWxId, @Param("toWxId")String toWxId);



}