package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.PlayMessagePushDetail;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface PlayMessagePushDetailMapper extends BaseMapper<PlayMessagePushDetail>{


    int batchInsert(List<PlayMessagePushDetail> pushDetailList);


    public List<PlayMessagePushDetail> selectListByPushId(Integer playMsgPushId);

    /**
     * 通过推送Id查询实际推送完成的消息数量
     * @param id
     * @return
     */
    Integer selectFinishCountByPushId(Integer id);
}