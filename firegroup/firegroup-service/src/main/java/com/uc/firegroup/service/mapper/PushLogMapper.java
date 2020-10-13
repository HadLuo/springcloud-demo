package com.uc.firegroup.service.mapper;

import org.apache.ibatis.annotations.Param;

import com.uc.firegroup.api.pojo.PushLog;
import tk.mybatis.mapper.common.BaseMapper;

public interface PushLogMapper extends BaseMapper<PushLog> {

    /***
     * 
     * title: 查询最近一条log日志
     *
     * @param groupId
     * @param playId
     * @return
     * @author HadLuo 2020-9-17 13:19:44
     */
    public PushLog selectLastLog(@Param("groupId") String groupId, @Param("playId") Integer playId);

    /**
     * 
     * title: 查询已发送条数
     *
     * @param playId
     * @return
     * @author HadLuo 2020-9-23 10:46:25
     */
    public int selectCountByPlayId(@Param("playId") Integer playId);
    
    
    
    /***
     * 
     * title:  根据 昵称来查 log， 
     *
     * @param groupId
     * @param playId
     * @param nickName
     * @return
     * @author HadLuo 2020-9-27 9:30:37
     */
    public PushLog selectLogByNickName(@Param("groupId") String groupId, @Param("playId") Integer playId,@Param("nickName") String nickName);

}
