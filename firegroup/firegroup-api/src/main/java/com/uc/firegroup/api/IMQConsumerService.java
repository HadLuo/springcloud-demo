package com.uc.firegroup.api;

import com.uc.firegroup.api.request.OperationRobotRequest;

public interface IMQConsumerService {

    /**
     * 移除水军所在群组MQ调用服务
     * @param request
     */
    public void deleteGroup(OperationRobotRequest request);
}
