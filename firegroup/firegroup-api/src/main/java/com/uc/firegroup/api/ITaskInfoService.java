package com.uc.firegroup.api;

import com.uc.firegroup.api.pojo.FireGroupConfig;
import com.uc.firegroup.api.pojo.TaskInfo;
import com.uc.firegroup.api.request.EnterpriseGroupRequest;
import com.uc.firegroup.api.request.TaskInfoRequest;
import com.uc.firegroup.api.request.TaskInfoUpdateRequest;
import com.uc.firegroup.api.response.RobotWxQRCodeResponse;
import com.uc.firegroup.api.response.TaskAllResponse;
import com.uc.firegroup.api.response.TaskInfoResponse;
import com.uc.framework.db.PageInfo;
import com.uc.framework.login.Login;
import com.uc.framework.obj.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = Constant.instanceName, decode404 = true)
@Api(tags = "任务操作接口")
public interface ITaskInfoService {
    /**
     * 新增任务
     * @param request
     * @return
     * @author 鲁志学 2020年9月14日
     */
    @Login
    @PostMapping("taskInfo/insert")
    @ApiOperation(value = "新增任务", response = Result.class)
    public Result<Void> insert(@RequestBody  TaskInfoRequest request);

    /**
     * 删除
     * @param request
     * @return
     * @author 鲁志学 2020年9月14日
     */
    @Login
    @PostMapping("taskInfo/delete")
    @ApiOperation(value = "删除任务", response = Result.class)
    public Result<Void>  delete(@RequestBody TaskInfoRequest request);

    /**
     * 分页查询任务列表
     * @param request
     * @return
     * @author 鲁志学 2020年9月14日
     */
    @Login
    @PostMapping("taskInfo/queryPageList")
    @ApiOperation(value = "分页查询任务列表", response = TaskInfoResponse.class)
    public Result<PageInfo<TaskInfoResponse>>  queryPageList(@RequestBody TaskInfoRequest request);


    /**
     * 查询所有的任务信息
     * @return
     * @author 鲁志学 2020年9月14日
     */
    @Login
    @GetMapping("taskInfo/queryAll")
    @ApiOperation(value = "查询所有的任务信息", response = TaskInfo.class)
    public Result<List<TaskInfo>>  queryAll();

    /**
     * 查询当前有效的小助手微信二维码和可入群次数
     * @return
     * @param count 刷新次数
     * @author 鲁志学 2020年9月14日
     */
    @GetMapping("taskInfo/queryRobotQRCode")
    @ApiOperation(value = "查询当前有效的小助手微信二维码和可入群次数", response = RobotWxQRCodeResponse.class)
    public Result<RobotWxQRCodeResponse> queryRobotQRCode(@RequestParam("count") Integer count);


    /**
     * 绑定企业微信
     * @param request
     * @return
     * @author 鲁志学 2020年9月19日
     */
    @Login
    @PostMapping("taskInfo/addEnterpriseGroup")
    @ApiOperation(value = "绑定企业微信", response = Result.class)
    public Result<Void> addEnterpriseGroup(@RequestBody  List<EnterpriseGroupRequest> request);


    /**
     * 查询商家汇总信息
     * @return
     * @author 鲁志学 2020年9月19日
     */
    @Login
    @PostMapping("taskInfo/selectTaskAllByMerchant")
    @ApiOperation(value = "查询商家汇总信息", response = TaskAllResponse.class)
    public Result<TaskAllResponse> selectTaskAllByMerchant();

    /**
     * 修改任务信息
     * @return
     * @author 鲁志学 2020年9月19日
     */
    @Login
    @PostMapping("taskInfo/update")
    @ApiOperation(value = "修改任务信息", response = Result.class)
    public Result<Void> update(@RequestBody  TaskInfoUpdateRequest request);

    /**
     * 通过任务Id集合查询任务信息
     * @return
     * @author 袁哲 2020年9月21日
     */
    @Login
    @PostMapping("taskInfo/findListByIds")
    @ApiOperation(value = "通过任务Id集合查询任务信息", response = Result.class)
    Result<List<TaskInfo>> findListByTaskIds(@RequestBody List<String> taskIds);

    @Login
    @GetMapping("taskInfo/stopTask")
    @ApiOperation(value = "停止任务", response = Result.class)
    public Result<Void> stopTaskId(@RequestParam("taskId")Integer taskId);

    /**
     *
     * @return
     */
    @Login
    @GetMapping("taskInfo/selectEnterpriseWxIds")
    @ApiOperation(value = "获取所有已经绑定的企业外部群ID", response = Result.class)
    public Result<List<String>> selectEnterpriseWxIds();

    /**
     * 查找当前最新配置
     * @return
     * @author 鲁志学 2020年9月09日
     */
    @Login
    @GetMapping("taskInfo/getFireGroupConfig")
    @ApiOperation(value = "获取基础配置", response = FireGroupConfig.class)
    public Result<FireGroupConfig> selectFireGroupConfig();
}
