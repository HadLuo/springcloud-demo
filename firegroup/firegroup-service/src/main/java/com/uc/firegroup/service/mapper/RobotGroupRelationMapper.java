package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.RobotGroupRelation;
import com.uc.firegroup.api.request.RobotInfoGroupRequest;
import com.uc.firegroup.api.response.RobotInfoGroupResponse;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

import javax.xml.crypto.Data;
import java.util.List;

public interface RobotGroupRelationMapper extends BaseMapper<RobotGroupRelation> {


    /**
     * 根据微信ID和时间查询今天水军入群数量
     * @param wxId 微信ID
     * @param beginDate 开始日期
     * @param endDate  结束日期
     * @return
     * @author 鲁志学 2020年9月14日
     */
    public int selectCountByWxIdDate(@Param("wxId") String wxId, @Param("beginDate") String beginDate,@Param("endDate") String endDate);

    /**
     * 根据微信ID和微信群ID查询是否已经入群
     * @param wxId 微信ID
     * @param wxGroupId 微信群ID
     * @return
     * @author 鲁志学 2020年9月16日
     */
    public int selectCountByWxIdGroupId(@Param("wxId") String wxId,@Param("wxGroupId") String wxGroupId);

    /**
     * 根据微信ID和微信群ID查询记录
     * @param wxId 微信ID
     * @param wxGroupId 微信群ID
     * @return
     * @author 鲁志学 2020年9月16日
     */
    public RobotGroupRelation selectByWxIdGroupId(@Param("wxId") String wxId,@Param("wxGroupId") String wxGroupId);

    /**
     * 根据微信群ID查询群内水军数量
     * @param wxGroupId 微信ID
     * @return
     * @author 鲁志学 2020年9月17日
     */
    public int selectRobotCountByGroupId(String wxGroupId);

    /**
     * 根据微信群ID查询群内水军信息
     * @param wxGroupId
     * @return
     * @author 鲁志学 2020年9月17日
     */
    public List<RobotGroupRelation> selectRobotByGroupId(String wxGroupId);

    /**
     * 按照机器人ID查询所在群数量
     * @param robotWxId 机器人ID
     * @return
     * @author 鲁志学 2020年9月17日
     */
    public Integer selectGroupCountByRobotId(String robotWxId);

    /**
     * 按照机器人ID查询所在群信息
     * @param robotWxId 机器人ID
     * @return
     * @author 鲁志学 2020年9月22日
     */
    public List<RobotGroupRelation> selectGroupByRobotId(String robotWxId);

    /**
     * 根据条件分页查询群内水军信息(包括已经退群的水军)
     * @param request
     * @return
     * @author 鲁志学 2020年9月18日
     */
    public List<RobotInfoGroupResponse> selectRobotAllByGroupId(RobotInfoGroupRequest request);


    /**
     * 根据条件分页查询群内水军信息(包括已经退群的水军)总数
     * @param request
     * @return
     * @author 鲁志学 2020年9月18日
     */
    public int selectRobotAllByGroupIdCount(RobotInfoGroupRequest request);


    /**
     * 根据条件查询所有查询群内水军信息(不包括已经退群的水军)
     * @param request
     * @return
     * @author 鲁志学 2020年9月18日
     */
    public List<RobotInfoGroupResponse> selectRobotByName(RobotInfoGroupRequest request);


    /**
     * 查询所有等待退群的水军信息去重分页查询前20条记录
     * @return
     * @author 鲁志学 2020年9月18日
     */
    public List<RobotGroupRelation> selectWaitGroup();

    /**
     * 根据微信ID将状态改成已退群
     * @param wxRobotId
     * @return
     * @author 鲁志学 2020年9月22日
     */
    public int updateStateByRobotId(String wxRobotId);


}