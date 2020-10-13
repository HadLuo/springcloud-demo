package com.uc.firegroup.service.mapper;

import java.util.Date;
import java.util.List;

import com.uc.firegroup.api.request.PlayPushGroupRecordRequest;
import org.apache.ibatis.annotations.Param;

import com.uc.firegroup.api.pojo.PlayMessagePush;
import com.uc.firegroup.api.request.TimingPushRecordPageRequest;
import com.uc.firegroup.api.request.TriggerPushRecordPageRequest;
import tk.mybatis.mapper.common.BaseMapper;

public interface PlayMessagePushMapper extends BaseMapper<PlayMessagePush> {

    /**
     * 
     * title: 根据剧本id 和 playMessageId 查询
     *
     * @param playId
     * @param playMessageId
     * @return
     * @author HadLuo 2020-9-16 16:01:01
     */
    public List<PlayMessagePush> selectGroupList(Integer playId, Integer playMessageId);


    int batchInsert(List<PlayMessagePush> playMessagePushList);
    
    
    /***
     * 
     * title:  根据 剧本id 和群id 查询 记录
     *
     * @param playId
     * @param wxGroupId
     * @return
     * @author HadLuo 2020-9-21 15:41:40
     */
    PlayMessagePush selectOneByIds(@Param("playId") Integer playId , @Param("wxGroupId") String wxGroupId);

    /**
     * 分页获取定时推送记录
     * @param pageRequest
     * @return
     */
    List<PlayMessagePush> findTimingPushPageList(TimingPushRecordPageRequest pageRequest);

    /**
     * 获取定时推送记录条数
     * @param pageRequest
     * @return
     */
    Integer findTimingPushPageCount(TimingPushRecordPageRequest pageRequest);


    /**
     * 获取触发推送记录条数
     * @param pageRequest
     * @return
     */
    Integer findTriggerPushPageCount(TriggerPushRecordPageRequest pageRequest);

    /**
     * 分页获取触发推送记录
     * @param pageRequest
     * @return
     */
    List<PlayMessagePush> findTriggerPushPageList(TriggerPushRecordPageRequest pageRequest);

    /**
     * 通过推送时间区间和群唯一标识查询群推送信息
     * @param wxGroupId
     * @param startTime
     * @param endTime
     * @return
     */
    List<PlayMessagePush> findListByWxGroupIdAndPushDate(@Param("wxGroupId") String wxGroupId,@Param("startTime") Date startTime,@Param("endTime") Date endTime);

    /**
     * 通过群标识查询待推送的剧本数
     * @param wxGroupId
     * @return
     */
    int findWaitPushCountByWxGroupId(String wxGroupId);

    /**
     * 获取群推送记录条数
     * @param pageRequest
     * @return
     */
    Integer findGroupPlayPushPageCount(PlayPushGroupRecordRequest pageRequest);

    /**
     * 分页获取群推送记录
     * @param pageRequest
     * @return
     */
    List<PlayMessagePush> findGroupPlayPushPageList(PlayPushGroupRecordRequest pageRequest);
}