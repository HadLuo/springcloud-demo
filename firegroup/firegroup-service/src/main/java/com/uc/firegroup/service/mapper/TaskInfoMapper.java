package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.TaskInfo;
import com.uc.firegroup.api.request.TaskInfoRequest;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;

public interface TaskInfoMapper extends BaseMapper<TaskInfo> {

    /**
     * 校验验证码是否存在
     * @param verificationCode
     * @return
     * @author 鲁志学 2020年9月14日
     */
    public int selectCodeByCount(String verificationCode);

    /**
     * 分页查询总数
     * @param request
     * @return
     * @author 鲁志学 2020年9月14日
     */
    public int selectPageListCount(TaskInfoRequest request);

    /**
     * 分页查询任务列表
     * @param request
     * @return
     * @author 鲁志学 2020年9月14日
     */
    public List<TaskInfo> selectPageList(TaskInfoRequest request);

    /**
     * 根据用户ID查询所有有效的任务信息
     * @param createId
     * @return
     * @author 鲁志学 2020年9月14日
     */
    public List<TaskInfo> selectAllList(String createId);

    /**
     * 查询所有有效的任务信息
     * @param
     * @return
     * @author 鲁志学 2020年9月14日
     */
    public List<TaskInfo> queryAllList();

    /**
     * 查询任务通过任务Id
     * @param taskId
     * @return
     */
    public TaskInfo selectByTaskId(Integer taskId);

    /**
     * 通过任务Id集合查询任务信息
     * @param taskIds
     * @return
     */
    List<TaskInfo> selectByTaskIds(List<String> taskIds);
}