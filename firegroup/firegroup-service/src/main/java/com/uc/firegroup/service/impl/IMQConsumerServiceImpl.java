package com.uc.firegroup.service.impl;

import com.alibaba.fastjson.JSON;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.MerchantBaseGpDTO;
import com.uc.external.bilin.res.BooleanResultVo;
import com.uc.firegroup.api.IMQConsumerService;
import com.uc.firegroup.api.pojo.GroupInfo;
import com.uc.firegroup.api.pojo.RobotGroupRelation;
import com.uc.firegroup.api.pojo.RobotInfo;
import com.uc.firegroup.api.request.OperationRobotRequest;
import com.uc.firegroup.service.mapper.GroupInfoMapper;
import com.uc.firegroup.service.mapper.RobotGroupRelationMapper;
import com.uc.firegroup.service.mapper.RobotInfoMapper;
import com.uc.framework.logger.Logs;
import com.uc.framework.obj.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class IMQConsumerServiceImpl implements IMQConsumerService {
    @Autowired
    private RobotGroupRelationMapper  robotGroupRelationMapper;
    @Autowired
    private GroupInfoMapper groupInfoMapper;
    @Value(value = "${identity}")
    private String identity;
    @Autowired
    private RobotInfoMapper robotInfoMapper;

    @Override
    public void deleteGroup(OperationRobotRequest request) {

    }
}
