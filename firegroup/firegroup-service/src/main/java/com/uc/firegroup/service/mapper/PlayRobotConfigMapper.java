package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.PlayInfo;
import com.uc.firegroup.api.pojo.PlayRobotConfig;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface PlayRobotConfigMapper extends BaseMapper<PlayRobotConfig> {

    int deleteByPlayId(Integer playId);

    /**
     * 通过剧本Id查询
     * @param playId
     * @return
     */
    List<PlayRobotConfig> selectListByPlayId(Integer playId);
}