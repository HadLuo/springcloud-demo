package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.PlayMessage;
import tk.mybatis.mapper.common.BaseMapper;
import java.util.List;

public interface PlayMessageMapper extends BaseMapper<PlayMessage> {
    /**
     *
     * title: 根据剧本id 来查 所有 的 message
     *
     * @param playId
     * @return
     * @author HadLuo 2020-9-14 15:42:27
     */
    List<PlayMessage> selectListByPlayId(Integer playId);

    /**
     * 通过剧本id删除
     * @param playId
     * @return
     */
    Integer deleteByPlayId(Integer playId);
}