package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.RobotInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface RobotInfoMapper extends BaseMapper<RobotInfo> {

    /**
     * 查询好友数小于num的机器人信息列表根据好友数量正序排列
     * @param num 配置的好友数量
     * @return
     * @author 鲁志学 2020年9月09日
     */
    public List<RobotInfo> queryRobotList(int num);

    /**
     * 根据wxId查询水军信息
     * @param wxId 水军微信号
     * @return
     * @author 鲁志学 2020年9月10日
     */
    public RobotInfo selectRobotByWxId(String wxId);

    /**
     * 查询所有正常的水军号信息
     * @return
     * @author 鲁志学 2020年9月10日
     */
    public List<RobotInfo> queryRobotListAll();

    /**
     * 根据微信号查询水军信息
     * @param wxAcc 水军微信号
     * @return
     * @author 鲁志学 2020年9月11日
     */
    public RobotInfo selectRobotByWxAcc(String wxAcc);

    /**
     * 查询入群数量少于配置值的水军号 正序
     * @param num 配置最大的入群数量
     * @return
     * @author 鲁志学 2020年9月14日
     */
    public List<RobotInfo> selectRobotByGroup(int num);

    /**
     * 操作入群数量字段
     * @param num 正数加 负数减
     * @author 鲁志学 2020年9月14日
     */
    public void updateGroupNum(@Param("groupNum") int num, @Param("robotId") Integer robotId);

    /**
     * 批量插入机器人信息
     * @param robotInfoInsert
     */
    int batchInsert(List<RobotInfo> robotInfoInsert);

    /**
     * 通过微信Id集合删除机器人信息
     * @param wxIdDeleteList
     * @return
     */
    int deleteByWxIds(List<String> wxIdDeleteList);

    /**
     * 扣减好友数
     * @param wxId
     * @param num
     * @return
     */
    int decrFriendNum(@Param("wxId")String wxId,@Param("num") Integer num);
}