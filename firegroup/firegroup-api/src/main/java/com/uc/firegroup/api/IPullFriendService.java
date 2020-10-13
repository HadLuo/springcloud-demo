package com.uc.firegroup.api;

import com.uc.firegroup.api.request.OperationRobotRequest;
import com.uc.firegroup.api.response.IncomeGroupCallBackResponse;
import com.uc.framework.obj.Result;

public interface IPullFriendService {
    /**
     * 拉好友入群回调
     * @param response
     * @author 鲁志学 2020年9月16日
     */
    public void pullFriend(IncomeGroupCallBackResponse response);

    /**
     * 邀请好友入群
     */
    public Result<Void> addFriend(OperationRobotRequest request);
}
