package com.uc.firegroup.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.uc.external.bilin.Urls;
import com.uc.external.bilin.req.AddFriendDTO;
import com.uc.external.bilin.req.GroupOnCallbackDTO;
import com.uc.external.bilin.res.BooleanResultVo;
import com.uc.firegroup.api.IFireGroupConfigService;
import com.uc.firegroup.api.IRobotFriendService;
import com.uc.firegroup.api.enums.FriendLogStateEnum;
import com.uc.firegroup.api.pojo.FireGroupConfig;
import com.uc.firegroup.api.pojo.FriendLog;
import com.uc.firegroup.api.pojo.FriendRelation;
import com.uc.firegroup.api.pojo.RobotInfo;
import com.uc.firegroup.api.request.FriendLogRequest;
import com.uc.firegroup.service.mapper.FriendLogMapper;
import com.uc.firegroup.service.mapper.FriendRelationMapper;
import com.uc.firegroup.service.mapper.RobotInfoMapper;
import com.uc.framework.logger.Logs;
import com.uc.framework.obj.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
public class IRobotFriendServiceImpl implements IRobotFriendService {
    @Autowired
    private RobotInfoMapper robotInfoMapper;
    @Autowired
    private FriendLogMapper friendLogMapper;
    @Autowired
    private FriendRelationMapper friendRelationMapper;


    @Transactional
    @Override
    public Result<Void> robotAddFriendCallback(String fromWxId, String toWxId) {
        //判断微信号不能为空且不能相同
        if (fromWxId == null || toWxId == null||fromWxId.equals(toWxId)){
            Logs.e(getClass(),"入参微信号异常:fromWxId"+fromWxId+"toWxId:"+toWxId);
            return  Result.err(-1,"入参微信号异常");
        }
        //判断两个微信号是否已经建立了好友关系
        FriendRelation fromRelation = friendRelationMapper.selectRelationForWxId(fromWxId,toWxId);
        if (fromRelation != null){
            Logs.e(getClass(),"入参微信号已存在好友关系！:fromWxId"+fromWxId+"toWxId:"+toWxId);
            return Result.err(-1,"入参微信号异常");
        }
        FriendRelation toRelation = friendRelationMapper.selectRelationForWxId(toWxId,fromWxId);
        if (toRelation != null){
            Logs.e(getClass(),"入参微信号已存在好友关系！:fromWxId"+fromWxId+"toWxId:"+toWxId);
            return Result.err(-1,"入参微信号异常");
        }
        //查询主加人基础信息
        RobotInfo formInfo = robotInfoMapper.selectRobotByWxId(fromWxId);
        if (formInfo == null){
            Logs.e(getClass(),"主加人微信ID不存在！:fromWxId"+fromWxId+"toWxId:"+toWxId);
            return Result.err(-1,"主加人微信ID不存在！");
        }

        //查询被加人基础信息
        RobotInfo toInfo = robotInfoMapper.selectRobotByWxId(toWxId);
        if (toInfo == null){
            Logs.e(getClass(),"被加人微信ID不存在！:fromWxId"+fromWxId+"toWxId:"+toWxId);
            return Result.err(-1,"被加人微信ID不存在！");
        }

        //根据微信号查询加好友日志里的记录
        FriendLog log = friendLogMapper.selectOneLogByWxId(fromWxId,toWxId);
        if (log == null){
            Logs.e(getClass(),"入参微信号无法查到操作日志记录！:fromWxId"+fromWxId+"toWxId:"+toWxId);
            return Result.err(-1,"入参微信号无法查到操作日志记录");
        }
        //如果状态为1 修改成2 并更新接受时间
        if (log.getState() == FriendLogStateEnum.SEND_MSG.getKey()){
            //修改状态为已接受
            log.setState(FriendLogStateEnum.ACCEPTED.getKey());
            //修改接受时间
            log.setModifyTime(new Date());
            //调用修改接口
            friendLogMapper.updateByPrimaryKeySelective(log);
        }
        //新增好友关系记录
        FriendRelation friendRelation = new FriendRelation();
        friendRelation.setFromWxId(fromWxId);
        friendRelation.setToWxId(toWxId);
        friendRelation.setCreateTime(new Date());
        friendRelation.setModifyTime(new Date());
        friendRelation.setIsDelete(0);
        friendRelationMapper.insertSelective(friendRelation);
        //最后修改两个微信号的好友数量
        formInfo.setFriendNum(formInfo.getFriendNum()+1);
        toInfo.setFriendNum(toInfo.getFriendNum()+1);
        //调用接口修改主加人水军信息
        robotInfoMapper.updateByPrimaryKeySelective(formInfo);
        //调用接口修改被加人水军信息
        robotInfoMapper.updateByPrimaryKeySelective(toInfo);
        return Result.ok(null);
    }

    @Override
    public void refreshRobotInfoJob() {
       //获取所有水军基础信息列表
       List<RobotInfo> robotInfos = robotInfoMapper.queryRobotListAll();


    }
}
