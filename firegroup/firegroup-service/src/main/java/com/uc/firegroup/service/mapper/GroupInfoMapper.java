package com.uc.firegroup.service.mapper;


import com.uc.firegroup.api.pojo.GroupInfo;
import com.uc.firegroup.api.request.GroupInfoRequest;
import com.uc.firegroup.api.response.GroupAllByMerchantRequest;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;
import java.util.Date;
import java.util.List;

public interface GroupInfoMapper extends BaseMapper<GroupInfo> {

    /**
     * 根据微信群ID查询群信息
     * @param wxGroupId  微信群ID
     * @return
     * @author 鲁志学 2020年9月15日
     */
    public GroupInfo selectInfoByWxGroupId(String  wxGroupId);

    /**
     * 根据微信群ID查询群信息
     * @param wxGroupId
     * @return
     */
    public GroupInfo selectByWxGroupId(String  wxGroupId);

    /**
     * 通过微信群Id集合查询群信息
     * @param wxGroupIdList
     * @return
     */
    public List<GroupInfo> selectListByGroupIds(List<String> wxGroupIdList);

    /**
     * 通过任务Id集合查询群信息
     * @param taskIdList
     * @return
     */
    public List<GroupInfo> selectListByTaskIds(List<Integer> taskIdList);

    /**
     * 通过多个状态查询群信息
     * @param stateList
     * @return
     */
    List<GroupInfo> selectListByStates(List<Integer> stateList);


    /**
     * 分页查询群列表
     * @param request
     * @return
     * @author 鲁志学 2020年9月17日
     */
    public List<GroupInfo> selectPageList(GroupInfoRequest request);

    /**
     * 分页查询群列表总数
     * @param request
     * @return
     * @author 鲁志学 2020年9月17日
     */
    public Integer selectPageListCount(GroupInfoRequest request);

    /**
     * 根据商户号拿所有群的信息
     * @param merchantId
     * @return
     * @author 鲁志学 2020年9月18日
     */
    public List<GroupAllByMerchantRequest> selectGroupAllByMerchant(@Param("merchantId") String merchantId, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize);


    public List<GroupAllByMerchantRequest> selectLikeGroupAllByMerchant(@Param("merchantId") String merchantId, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize
            ,@Param("groupName") String groupName,@Param("taskId")Integer taskId);

    public int selectCountLikeGroupAllByMerchant(@Param("merchantId") String merchantId, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize
            ,@Param("groupName") String groupName,@Param("taskId")Integer taskId);

    /**
     * 根据任务ID查询群数量
     * @param taskId renewID
     * @return
     * @author 鲁志学 2020年9月18日
     */
    public Integer selectGroupCountByTaskId(Integer taskId);

    /**
     * 根据任务ID查询所有的群数
     * @param taskId
     * @return
     * @author 鲁志学 2020年9月19日
     */
    public List<GroupInfo> selectGroupListByTaskId(Integer taskId);

    /**
     * 修改已购群水军数量
     * @param wxGroupId 群ID
     * @param num 操作数量 正数加负数减
     * @return
     * @author 鲁志学 2020年9月21日
     */
    public int updateRobotNum(@Param("wxGroupId")String wxGroupId,@Param("num")Integer num);


    /**
     * 通过微信唯一标识集合查询微信群信息
     * @param wxGroupIds
     * @return
     */
    List<GroupInfo> selectListByWxGroupIds(List<String> wxGroupIds);

    /**
     * 获取所有正常的企业外部微信群ID
     * @return
     * @author 鲁志学 2020年9月21日
     */
    List<String> selectEnterpriseWxIds();

    /**
     * 查询初始化的企业微信外部群
     * @return
     */
    GroupInfo selectInitByWxGroupId(String wxGroupId);

    /**
     * 修改群为暂停状态
     * @param groupIds 群集合
     * @param pauseTime 暂停时间
     * @param state 暂停状态
     * @return
     */
    Integer updatePauseGroups(@Param("groupIds") List<Integer> groupIds,@Param("pauseTime") Date pauseTime,@Param("groupState") Integer state);
}