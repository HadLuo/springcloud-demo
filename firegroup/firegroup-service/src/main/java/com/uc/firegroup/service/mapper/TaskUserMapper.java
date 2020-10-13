package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.TaskUser;
import tk.mybatis.mapper.common.BaseMapper;

public interface TaskUserMapper extends BaseMapper<TaskUser> {

    /**
     * 根据微信ID查询绑定任务情况
     * @param wxId 微信ID
     * @return
     * @author 鲁志学 2020年9月15日
     */
    public TaskUser selectUserByWxId(String wxId);

    /**
     * 根据任务ID查询绑定任务信息
     * @param taskId 任务ID
     * @return
     * @author 鲁志学 2020年9月16日
     */
    public TaskUser selectUserByTaskId(Integer taskId);

}