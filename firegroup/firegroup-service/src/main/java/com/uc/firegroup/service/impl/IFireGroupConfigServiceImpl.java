package com.uc.firegroup.service.impl;

import com.uc.firegroup.api.IFireGroupConfigService;
import com.uc.firegroup.api.pojo.FireGroupConfig;
import com.uc.firegroup.service.mapper.FireGroupConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Service
public class IFireGroupConfigServiceImpl implements IFireGroupConfigService {
    @Autowired
    private FireGroupConfigMapper fireGroupConfigMapper;

    @Override
    public FireGroupConfig selectFireGroupConfig() {
        List<FireGroupConfig> fireGroupConfigs = fireGroupConfigMapper.selectFireGroupConfig();
        if (CollectionUtils.isEmpty(fireGroupConfigs)){
            return null;
        }
        return fireGroupConfigs.get(0);
    }
}
