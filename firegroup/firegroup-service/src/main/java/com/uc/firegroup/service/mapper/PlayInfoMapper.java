package com.uc.firegroup.service.mapper;

import java.util.List;
import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.request.TimingPlayPageRequest;
import com.uc.firegroup.api.request.TriggerPlayPageRequest;
import tk.mybatis.mapper.common.BaseMapper;

public interface PlayInfoMapper extends BaseMapper<PlayInfo> {
    /**
     * 
     * title: 查询 话术触发有效剧本
     *
     * @return
     * @author HadLuo 2020-9-16 13:48:30
     */
    public List<PlayInfo> selectKeyWordsTimeUp();

    /**
     * 
     * title: 查询 定时推送类型 且 到了推送时间的有效剧本
     *
     * @return
     * @author HadLuo 2020-9-16 13:48:30
     */
    public List<PlayInfo> selectListTimeUp();

    /**
     * 分页查询定时数据总条数
     * 
     * @param pageRequest
     * @return
     */
    int selectTimingPlayPageCount(TimingPlayPageRequest pageRequest);

    /**
     * 分页查询定时数据
     * 
     * @param pageRequest
     * @return
     */
    List<PlayInfo> selectTimingPlayPage(TimingPlayPageRequest pageRequest);

    /**
     * 分页查询触发剧本总条数
     * 
     * @param pageRequest
     * @return
     */
    int selectTriggerPlayPageCount(TriggerPlayPageRequest pageRequest);

    /**
     * 分页查询触发剧本数据
     * 
     * @param pageRequest
     * @return
     */
    List<PlayInfo> selectTriggerPlayPage(TriggerPlayPageRequest pageRequest);
}