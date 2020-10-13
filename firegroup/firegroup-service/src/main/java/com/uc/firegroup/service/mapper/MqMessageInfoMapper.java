package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.MqMessageInfo;
import com.uc.firegroup.api.request.PullMqLogRequest;
import tk.mybatis.mapper.common.BaseMapper;

public interface MqMessageInfoMapper extends BaseMapper<MqMessageInfo> {

    /**
     * 根据操作编码查询MQ记录
     * @param optId 操作编码
     * @return
     * @author 鲁志学 2020年9月27日
     */
    public MqMessageInfo selectInfoByOptId(String optId);

    /**
     * 查询4505状态回调的日志
     * @param request
     * @return
     * @author 鲁志学 2020年9月27日
     */
    public MqMessageInfo selectInfoByPullWxId(PullMqLogRequest request);

}