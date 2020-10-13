package com.uc.firegroup.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.*;
import com.uc.external.bilin.res.BooleanResultVo;
import com.uc.external.bilin.res.GroupBaseInfoVO;
import com.uc.external.bilin.res.GroupQRCodeVo;
import com.uc.firegroup.api.IBilinService;
import com.uc.firegroup.api.IFireGroupConfigService;
import com.uc.firegroup.api.IGroupInfoService;
import com.uc.firegroup.api.ITaskInfoService;
import com.uc.firegroup.api.enums.GroupStatusEnum;
import com.uc.firegroup.api.pojo.*;
import com.uc.firegroup.api.request.EnterpriseGroupRequest;
import com.uc.firegroup.api.request.MoveGroupTaskRequest;
import com.uc.firegroup.api.request.TaskInfoRequest;
import com.uc.firegroup.api.request.TaskInfoUpdateRequest;
import com.uc.firegroup.api.response.RobotWxQRCodeResponse;
import com.uc.firegroup.api.response.TaskAllResponse;
import com.uc.firegroup.api.response.TaskInfoResponse;
import com.uc.firegroup.service.mapper.*;
import com.uc.framework.UUIDUtils;
import com.uc.framework.db.PageInfo;
import com.uc.framework.logger.Logs;
import com.uc.framework.login.User;
import com.uc.framework.login.UserThreadLocal;
import com.uc.framework.natives.Beans;
import com.uc.framework.obj.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@RestController
public class ITaskInfoServiceImpl implements ITaskInfoService {
    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private IBilinService  bilinService;

    @Autowired
    private RobotInfoMapper robotInfoMapper;

    @Autowired
    private IFireGroupConfigService fireGroupConfigService;

    @Autowired
    private RobotGroupRelationMapper robotGroupRelationMapper;

    @Autowired
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private IGroupInfoService groupInfoService;

    @Value(value = "${identity}")
    private String identity;

    @Autowired
    private IGroupInfoService iGroupInfoService;

    @Autowired
    private FireGroupConfigMapper fireGroupConfigMapper;

    @Override
    public Result<Void>  insert(TaskInfoRequest request) {
        User user =  UserThreadLocal.get();
        if (request.getTaskName() == null){
            return Result.err(-1,"请设置任务名称！");
        }
        TaskInfo taskInfo = new TaskInfo();
        //任务名称
        taskInfo.setTaskName(request.getTaskName());
        //用户名称
        taskInfo.setCreateName(user.getUsername());
        //商户号
        taskInfo.setCreateId(user.getMerchatId());
        //创建时间
        taskInfo.setCreateTime(new Date());
        //修改时间
        taskInfo.setModifyTime(new Date());
        //默认新建水军数量
        taskInfo.setRobotNum(1);
        //是否删除
        taskInfo.setIsDelete(0);
        String code = "";
        while (true){
            //生成六位数字验证码
            code =  UUIDUtils.generateUuid6();
            //校验验证码是否存在
            int count =  taskInfoMapper.selectCodeByCount(code);
            if (count == 0){
               break;
            }
        }
        //验证码
        taskInfo.setVerificationCode(code);
        taskInfoMapper.insertSelective(taskInfo);
        return Result.ok(null);
    }

    @Override
    public Result<Void> delete(TaskInfoRequest request) {
        if (request.getDelTaskIds() == null){
            return Result.err(-1,"参数异常！");
        }
        for (Integer taskId:request.getDelTaskIds()){
            //将下面的群进行转移
            List<GroupInfo> groupInfos = groupInfoMapper.selectGroupListByTaskId(taskId);
            if (groupInfos.size() > 0 && request.getMoveTaskId() == null){
                for (GroupInfo groupInfo:groupInfos){
                    if (groupInfo.getState() == 1){
                        return Result.err(-1,"该任务ID"+taskId+"下有进行中的群，不能进行删除！");
                    }
                }
            }
            if (request.getMoveTaskId() != null){
                if (taskId.equals(request.getMoveTaskId())){
                    return Result.err(-1,"删除的任务ID不能和转移的任务ID相同:"+taskId);
                }
                TaskInfo task = taskInfoMapper.selectByTaskId(request.getMoveTaskId());
                if (task == null || task.getIsDelete() == 1){
                    return Result.err(-1,"转移的任务ID"+request.getMoveTaskId()+"不存在！");
                }
            }
            Logs.e(getClass(),"查询群信息"+JSON.toJSONString(groupInfos));
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setTaskId(taskId);
            taskInfo.setIsDelete(1);
            taskInfoMapper.updateByPrimaryKeySelective(taskInfo);
            for (GroupInfo groupInfo:groupInfos){
                //进行中的任务需要转移
               if (groupInfo.getState() == 1 && request.getMoveTaskId() != null){
                 groupInfo.setTaskId(request.getMoveTaskId());
                 groupInfoMapper.updateByPrimaryKey(groupInfo);
               }
               //暂停的任务需要停止
                if (groupInfo.getState() == 2){
                    groupInfo.setState(GroupStatusEnum.END.getKey());
                    groupInfo.setModifyTime(new Date());
                    groupInfoMapper.updateByPrimaryKeySelective(groupInfo);
                    //获取群内所有水军的信息
                    List<RobotGroupRelation> list = robotGroupRelationMapper.selectRobotByGroupId(groupInfo.getWxGroupId());
                    if (list == null) {
                        continue;
                    }
                    //循环水军信息进行退群操作
                    for (RobotGroupRelation robotGroupRelation:list){
                        RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(robotGroupRelation.getRobotWxId());
                        if (robotInfo == null){
                            continue;
                        }
                        //修改机器人在群内信息
                        robotGroupRelation.setState(3);
                        robotGroupRelationMapper.updateByPrimaryKeySelective(robotGroupRelation);
                    }
                    RobotInfo info = robotInfoMapper.selectRobotByWxId(groupInfo.getOpenRobotWxId());
                    if (info != null){
                        //调用取消开通接口
                        MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
                        baseGpDTO.setMerchatId(taskInfo.getCreateId());
                        baseGpDTO.setVcGroupId(groupInfo.getWxGroupId());
                        baseGpDTO.setWxId(groupInfo.getOpenRobotWxId());
                        baseGpDTO.setIdentity(identity);
                        Result<BooleanResultVo> result = Urls.robotGroupCancel(baseGpDTO);
                        Logs.e(getClass(),"群注销！请求参数："+JSON.toJSONString(baseGpDTO)+"返回参数："+JSON.toJSONString(result));
                        if (result.getCode() != 0){
                            Logs.e(getClass(),"群注销失败！请求参数："+JSON.toJSONString(baseGpDTO)+"返回参数："+JSON.toJSONString(result));
                        }
                    }
                }
            }
        }
        return Result.ok();
    }

    @Override
    public Result<PageInfo<TaskInfoResponse>> queryPageList(TaskInfoRequest request) {
        User user =  UserThreadLocal.get();
        request.setCreateId(user.getMerchatId());
        Integer count = taskInfoMapper.selectPageListCount(request);
         PageInfo<TaskInfoResponse> pageInfo = new PageInfo<>();
         if (count > 0){
             pageInfo.setTotal(count);
             List<TaskInfo> taskInfos = taskInfoMapper.selectPageList(request);
             List<TaskInfoResponse> list = Beans.toView(taskInfos,TaskInfoResponse.class);
             FireGroupConfig fireGroupConfig = fireGroupConfigService.selectFireGroupConfig();
             //封装前端显示数据
             for (TaskInfoResponse taskInfo:list){
                List<GroupInfo> groupInfos = groupInfoMapper.selectGroupListByTaskId(taskInfo.getTaskId());
                if (groupInfos == null){
                    taskInfo.setGroupNum(0);
                    taskInfo.setTomorrowCost(0);
                    taskInfo.setStopGroupNum(0);
                }else {
                    Integer sum = 0;
                    int groupSum = 0;
                    int stopGroupSum = 0;
                    for (GroupInfo groupInfo:groupInfos){
                        Integer robotCount  = robotGroupRelationMapper.selectRobotCountByGroupId(groupInfo.getWxGroupId());
                        if (robotCount == null){
                            robotCount =0;
                        }
                        if (groupInfo.getState() == GroupStatusEnum.ACTIVE.getKey()){
                            groupSum++;
                            //获取群内水军个数
                            sum = sum+robotCount.intValue()*fireGroupConfig.getRobotDayMoney().intValue();
                        }
                        if (groupInfo.getState() == GroupStatusEnum.STOP.getKey()){
                            stopGroupSum++;
                        }
                    }
                    taskInfo.setGroupNum(groupSum);
                    taskInfo.setTomorrowCost(sum);
                    taskInfo.setStopGroupNum(stopGroupSum);
                }
             }
             pageInfo.setDatas(list);
         }
        return Result.ok(pageInfo);
    }

    @Override
    public Result<List<TaskInfo>> queryAll() {
        User user =  UserThreadLocal.get();
        List<TaskInfo> taskInfos = taskInfoMapper.selectAllList(user.getMerchatId());
        return Result.ok(taskInfos);
    }

    @Override
    public Result<RobotWxQRCodeResponse> queryRobotQRCode(Integer count) {
        //获取到配置数
        FireGroupConfig fireGroupConfig =  fireGroupConfigService.selectFireGroupConfig();
        //执行任务数最少的号
        List<RobotInfo> robotInfos = robotInfoMapper.selectRobotByGroup(fireGroupConfig.getRobotGroupCount());
        if (count == null){
            count = 0;
        }
        if (robotInfos.size() == 0){
            return Result.err("当前已经没有空闲的小助手！请联系客服");
        }
        //如果刷新次数超过列表长度 代表已经没有小助手
        if (count.intValue() > robotInfos.size()){
            //将count对size取模获取count数量
            count = count.intValue()%robotInfos.size();
        }
        RobotInfo robotInfo = robotInfos.get(count);
        RobotWxQRCodeResponse robotWxQRCodeResponse = new RobotWxQRCodeResponse();
        String wxId = robotInfo.getWxId();
        Result<String> wxQRCode  = bilinService.getWxQRCode(wxId);
        if (wxQRCode.getCode() == -1){
            return Result.err(-1,"计算异常！");
        }
        //返回二维码
        robotWxQRCodeResponse.setWxQRCode(wxQRCode.getData());
        Calendar cal=Calendar.getInstance();
        Date beginDate=cal.getTime();
        SimpleDateFormat sp=new SimpleDateFormat("yyyy-MM-dd");
        Calendar c=Calendar.getInstance();
        cal.add(Calendar.DATE,1);
        Date endDate=cal.getTime();
        //插入今日剩余可入群数
        Integer logSize = robotGroupRelationMapper.selectCountByWxIdDate(robotInfo.getWxId(),sp.format(beginDate),sp.format(endDate));
        if (logSize == null){
            logSize = 0;
        }
        Integer number = fireGroupConfig.getRobotDayCount() - logSize;
        if (number < 0){
            number = fireGroupConfig.getRobotDayCount() ;
        }
        robotWxQRCodeResponse.setNumber(number);
        robotWxQRCodeResponse.setWxAcc(robotInfo.getWxAcc());
        return Result.ok(robotWxQRCodeResponse);
    }

    @Override
    public Result<Void> addEnterpriseGroup( List<EnterpriseGroupRequest> requests) {
        if (requests == null){
            return Result.err("参数异常！");
        }
        //判断
        for (EnterpriseGroupRequest request:requests){
            TaskInfo taskInfo = taskInfoMapper.selectByTaskId(request.getTaskId());
            //获取企业微信群的二维码
            GroupQRCodeDTO groupQRCodeDTO = new GroupQRCodeDTO();
            groupQRCodeDTO.setIdentity(identity);
            groupQRCodeDTO.setMerchatId(taskInfo.getCreateId());
            groupQRCodeDTO.setVcGroupId(request.getWxGroupId());
            groupQRCodeDTO.setWxId(request.getOpenGroupWxId());
            Result<GroupQRCodeVo> result = Urls.getGroupQRCode(groupQRCodeDTO);
            if (result.getCode() != 0){
                Logs.e(getClass(),"获取微信二维码返回异常！"+ JSON.toJSONString(result));
                return Result.err(-1,"群名称"+request.getGroupName()+"获取群二维码异常！");
            }
            //创建群实体
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setTaskId(request.getTaskId());
            groupInfo.setState(GroupStatusEnum.INIT.getKey());
            groupInfo.setRobotNum(0);
            groupInfo.setGroupName(request.getGroupName());
            groupInfo.setWxGroupName(request.getWxGroupName());
            groupInfo.setWxGroupId(request.getWxGroupId());
            groupInfo.setLastBuyRobotNum(request.getRobotNum());
            groupInfo.setOpenRobotWxId(request.getOpenGroupWxId());
            groupInfo.setLastDelRobotNum(0);
            groupInfo.setGroups(request.getGroups());
            Date time = new Date();
            groupInfo.setCreateTime(time);
            groupInfo.setModifyTime(time);
            groupInfo.setIsDelete(0);
            //获取群成员信息 调用比邻的接口获取
            QueryBaseGroupDTO baseGroupDTO = new QueryBaseGroupDTO();
            baseGroupDTO.setIdentity(identity);
            baseGroupDTO.setGroupId(request.getWxGroupId());
            baseGroupDTO.setWxId(groupInfo.getOpenRobotWxId());
            Result<GroupBaseInfoVO> groupResult = Urls.queryGroupMermber(baseGroupDTO);
            Logs.e(getClass(),"查询群基础信息 req"+JSON.toJSONString(baseGroupDTO)+"返回值："+JSON.toJSONString(groupResult));
            //企业微信群
            groupInfo.setGroupType(2);
            if(groupResult.getCode() == 0 && groupResult.getData() != null){
                String  type = groupResult.getData().getEnterpriseChatRoom();
                Logs.e(getClass(),"打印群标识："+type);
                if (type != null && type.equals("0")){
                    groupInfo.setGroupType(1);
                }
            }
            groupInfoMapper.insertSelective(groupInfo);
        }
        return Result.ok();
    }

    @Override
    public Result<TaskAllResponse> selectTaskAllByMerchant() {
        TaskAllResponse response = new TaskAllResponse();
        User user = UserThreadLocal.get();
        Integer groupSum = 0;
        Integer robotSum = 0;
        Double tomorrowCostSum = 0.00;
        Double merchantMoney = 0.00;
        Integer waitGroupSum = 0;
        int waitRobotSum = 0;
        //查询商家比邻币
        MerchatAccDTO dto = new MerchatAccDTO();
        dto.setMerchatId(user.getMerchatId());
        Result<MerchatAccDTO> result = Urls.getMerchatAcc(dto);
        if (result.getCode() == 0){
            merchantMoney =   result.getData().getAccBalance();
        }
        //根据商户号查询所有任务
        List<TaskInfo> list = taskInfoMapper.selectAllList(user.getMerchatId());
        if (list == null || list.size() == 0){
            response.setGroupSum(groupSum);
            response.setMerchantMoney(merchantMoney);
            response.setTomorrowCostSum(tomorrowCostSum);
            response.setWaitGroupSum(waitGroupSum);
            response.setRobotSum(robotSum);
            return Result.ok(response);
        }
        FireGroupConfig fireGroupConfig = fireGroupConfigService.selectFireGroupConfig();
        List<Integer> groupIds = new ArrayList<>();
        for (TaskInfo info:list){
            //查询任务下属的群信息
            List<GroupInfo> groupInfos = groupInfoMapper.selectGroupListByTaskId(info.getTaskId());
            if (groupInfos == null|| groupInfos.size() == 0){
                continue ;
            }
            //服务数量
            for (GroupInfo groupInfo:groupInfos){
                if (groupInfo.getState() == GroupStatusEnum.END.getKey() || groupInfo.getState() == GroupStatusEnum.INIT.getKey()){
                    continue;
                }
                //查询当前群组已有多少水军数量
                Integer robotCount  = robotGroupRelationMapper.selectRobotCountByGroupId(groupInfo.getWxGroupId());
                if (robotCount == null){
                    robotCount =0;
                }
                if (groupInfo.getState() == GroupStatusEnum.STOP.getKey()){
                    waitGroupSum++;
                    //待续费群ID
                    groupIds.add(groupInfo.getGroupId());
                    //待续费水军ID
                    waitRobotSum = waitRobotSum+robotCount.intValue();
                }else {
                    groupSum++;
                    //服务内的水军数量
                    robotSum = robotSum+robotCount;
                }
            }
        }
        //明日预计费用
        tomorrowCostSum =  fireGroupConfig.getRobotDayMoney().doubleValue() * robotSum;
        response.setRobotSum(robotSum);
        response.setGroupSum(groupSum);
        response.setWaitGroupSum(waitGroupSum);
        response.setMerchantMoney(merchantMoney);
        response.setTomorrowCostSum(tomorrowCostSum);
        response.setWaitGroupIds(groupIds);
        response.setWaitRobotSum(waitRobotSum);
        return Result.ok(response);
    }

    @Override
    public Result<Void> update(TaskInfoUpdateRequest request) {
        if (request.getTaskId() == null){
            return Result.err(-1,"参数异常！");
        }
        if (request.getRobotNum() != null && request.getRobotNum() == 0){
            return Result.err(-1,"新建群水军不能为0");
        }
        TaskInfo taskInfo =  taskInfoMapper.selectByTaskId(request.getTaskId());
        if (taskInfo == null){
            return Result.err(-1,"任务ID不存在！");
        }
        if (request.getRobotNum() != null && request.getRobotNum() != 0){
            taskInfo.setRobotNum(request.getRobotNum());
        }
        if (request.getTaskName() != null){
            taskInfo.setTaskName(request.getTaskName());
        }
        taskInfoMapper.updateByPrimaryKeySelective(taskInfo);
        return Result.ok();
    }

    @Override
    public Result<Void> stopTaskId(Integer taskId) {
        if (taskId == null){
            return Result.err(-1,"参数异常！");
        }
        //获取任务下的群信息
        List<GroupInfo> groupInfos = groupInfoMapper.selectGroupListByTaskId(taskId);
        if (groupInfos == null){
            return Result.err(-1,"任务下没有可用群！");
        }
        List<Integer> ids = new ArrayList<>();
        for (GroupInfo groupInfo:groupInfos){
            if (groupInfo == null){
                continue;
            }
            groupInfo.setState(GroupStatusEnum.END.getKey());
            groupInfo.setModifyTime(new Date());
            groupInfoMapper.updateByPrimaryKeySelective(groupInfo);
            //获取群内所有水军的信息
            List<RobotGroupRelation> list = robotGroupRelationMapper.selectRobotByGroupId(groupInfo.getWxGroupId());
            if (list == null) {
                continue;
            }
            //循环水军信息进行退群操作
            for (RobotGroupRelation robotGroupRelation:list){
                RobotInfo robotInfo = robotInfoMapper.selectRobotByWxId(robotGroupRelation.getRobotWxId());
                if (robotInfo == null){
                    continue;
                }
                //修改机器人在群内信息
                robotGroupRelation.setState(3);
                robotGroupRelationMapper.updateByPrimaryKeySelective(robotGroupRelation);

            }
            TaskInfo taskInfo = taskInfoMapper.selectByTaskId(groupInfo.getTaskId());
            RobotInfo info = robotInfoMapper.selectRobotByWxId(groupInfo.getOpenRobotWxId());
            if (info != null){
                //调用取消开通接口
                MerchantBaseGpDTO baseGpDTO = new MerchantBaseGpDTO();
                baseGpDTO.setMerchatId(taskInfo.getCreateId());
                baseGpDTO.setVcGroupId(groupInfo.getWxGroupId());
                baseGpDTO.setWxId(groupInfo.getOpenRobotWxId());
                baseGpDTO.setIdentity(identity);
                Result<BooleanResultVo> result = Urls.robotGroupCancel(baseGpDTO);
                Logs.e(getClass(),"群注销！请求参数："+JSON.toJSONString(baseGpDTO)+"返回参数："+JSON.toJSONString(result));
                if (result.getCode() != 0){
                    Logs.e(getClass(),"群注销失败！请求参数："+JSON.toJSONString(baseGpDTO)+"返回参数："+JSON.toJSONString(result));
                }
            }
        }
        return Result.ok();
    }

    @Override
    public Result<List<TaskInfo>> findListByTaskIds(List<String> taskIds) {
        if(CollectionUtils.isEmpty(taskIds)){
            return Result.err("请求任务Id集合为空");
        }
        return Result.ok(this.taskInfoMapper.selectByTaskIds(taskIds));
    }

    @Override
    public Result<List<String>> selectEnterpriseWxIds() {
        //获取所有未停止服务的企业外部微信群ID
        List<String> wxGroupIds = groupInfoMapper.selectEnterpriseWxIds();
        return Result.ok(wxGroupIds);
    }

    @Override
    public Result<FireGroupConfig> selectFireGroupConfig() {
        List<FireGroupConfig> fireGroupConfigs = fireGroupConfigMapper.selectFireGroupConfig();
        if (CollectionUtils.isEmpty(fireGroupConfigs)){
            return null;
        }
        return Result.ok(fireGroupConfigs.get(0));
    }
}
