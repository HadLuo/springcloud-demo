package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.FireGroupConfig;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface FireGroupConfigMapper extends BaseMapper<FireGroupConfig> {

    /**
     * 查找当前最新配置
     * @return
     * @author 鲁志学 2020年9月09日
     */
    public List<FireGroupConfig> selectFireGroupConfig();
}