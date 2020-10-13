package com.uc.firegroup.api;

import com.uc.firegroup.api.pojo.GroupInfo;
import com.uc.firegroup.api.request.*;
import com.uc.firegroup.api.response.GroupInfoResponse;
import com.uc.firegroup.api.response.RobotInfoGroupResponse;
import com.uc.framework.db.PageInfo;
import com.uc.framework.login.Login;
import com.uc.framework.obj.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = Constant.instanceName, decode404 = true)
@Api(tags = "群操作接口")
public interface IGroupInfoService {

    /**
     * 查询群列表
     * @param request
     * @return
     * @author 鲁志学 2020年9月17日
     */
    @ApiOperation(value = "查询群列表", response = GroupInfoResponse.class)
    @PostMapping("/group/selectGroupInfoList")
    @Login
    public Result<PageInfo<GroupInfoResponse>> selectGroupInfoList(@RequestBody GroupInfoRequest request);

    /**
     * 添加水军
     * @param request
     * @return
     * @author 鲁志学 2020年9月17日
     */
    @ApiOperation(value = "添加水军", response = Result.class)
    @PostMapping("/group/addRobotByGroup")
    @Login
    public Result<Void> addRobotByGroup(@RequestBody OperationRobotRequest request);

    /**
     * 移除水军
     * @param request
     * @return
     * @author 鲁志学 2020年9月17日
     */
    @ApiOperation(value = "移除水军", response = Result.class)
    @PostMapping("/group/rmRobotByGroup")
    @Login
    public Result<Void> rmRobotByGroup(@RequestBody OperationRobotRequest request);

    /**
     * 移动群到其他任务
     * @param request
     * @return
     * @author 鲁志学 2020年9月17日
     */
    @ApiOperation(value = "移动群到其他任务", response = Result.class)
    @PostMapping("/group/moveGroupTask")
    @Login
    public Result<Void> moveGroupTask(@RequestBody MoveGroupTaskRequest request);


    /**
     * 群停止服务
     * @param groupIds
     * @return
     * @author 鲁志学 2020年9月17日
     */
    @ApiOperation(value = "群停止服务", response = Result.class)
    @PostMapping("/group/stopGroup")
    @Login
    public Result<Void> stopGroup(@RequestBody List<Integer> groupIds);

    /**
     * 续费
     * @param groupIds
     * @return
     * @author 鲁志学 2020年9月17日
     */
    @ApiOperation(value = "续费", response = Result.class)
    @PostMapping("/group/groupRenew")
    @Login
    public Result<Void> groupRenew(@RequestBody List<Integer> groupIds);


    /**
     * 查询群内水军详细信息
     * @param request
     * @return
     * @author 鲁志学 2020年9月18日
     */
    @ApiOperation(value = "查询群内水军详细信息列表", response = RobotInfoGroupResponse.class)
    @PostMapping("/group/selectRobotByGroupIdList")
    @Login
    public Result<PageInfo<RobotInfoGroupResponse>> selectRobotByGroupIdList(@RequestBody RobotInfoGroupRequest request);


    /**
     * 查询群内成员信息(包括商户所属个人号)
     * @param request
     * @return
     * @author 鲁志学 2020年9月18日
     */
    @ApiOperation(value = "查询群内成员信息(包括商户所属个人号)", response = RobotInfoGroupResponse.class)
    @PostMapping("/group/selectOpenRobotGroupList")
    @Login
    public Result<List<RobotInfoGroupResponse>>  selectOpenRobotGroupList(@RequestBody RobotInfoGroupRequest request);

    /**
     * 设置群开通号
     * @param request
     * @return
     * @author 鲁志学 2020年9月18日
     */
    @ApiOperation(value = "设置群开通号", response = Result.class)
    @PostMapping("/group/setOpenGroupRobot")
    public Result<Void> setOpenGroupRobot(@RequestBody OpenGroupRequest request);


    /**
     * 封号逻辑
     * @param wxRobotId  被封号水军微信ID
     * @author 鲁志学 2020年9月22日
     */
    @ApiOperation(value = "封号逻辑", response = Result.class)
    @PostMapping("/group/sealRobot")
    public Result<Void> sealRobot(List<String> wxRobotId);

    /**
     * 通过微信群唯一标识集合查询微信群信息
     * @return
     * @author 袁哲 2020年9月21日
     */
    @Login
    @PostMapping("group/findListByWxGroupIds")
    @ApiOperation(value = "通过微信群唯一标识集合查询微信群信息", response = Result.class)
    Result<List<GroupInfo>> findListByTaskIds(@RequestBody List<String> wxGroupIds);

    /**
     * 一键添加水军
     * @return
     * @author 鲁志学 2020年9月22日
     */
    @Login
    @PostMapping("group/insertRobots")
    @ApiOperation(value = "一键添加水军", response = Result.class)
    Result<Void> insertRobots(@RequestBody List<InsertRobotRequest> request);

}
