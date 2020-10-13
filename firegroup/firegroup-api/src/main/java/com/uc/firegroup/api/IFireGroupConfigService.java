package com.uc.firegroup.api;

import com.uc.firegroup.api.pojo.FireGroupConfig;


public interface IFireGroupConfigService {
    /**
     * 查找当前最新配置
     * @return
     * @author 鲁志学 2020年9月09日
     */
    public FireGroupConfig selectFireGroupConfig();

}
