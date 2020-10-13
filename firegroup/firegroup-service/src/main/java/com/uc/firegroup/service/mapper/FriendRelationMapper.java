package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.FriendRelation;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.List;
import java.util.Map;

public interface FriendRelationMapper extends BaseMapper<FriendRelation> {

    /**
     * 根据微信id查询对应的好友关系
     * @param fromWxId 主微信ID
     * @param toWxId 被加微信ID
     * @return
     * @author 鲁志学 2020年9月09日
     */
    public FriendRelation selectRelationForWxId(@Param("fromWxId")String fromWxId,@Param("toWxId")String toWxId);

    /**
     * 根据微信号查询所有好友关系
     * @param wxId 微信ID
     * @return
     * @author 鲁志学 2020年9月15日
     */
    public List<FriendRelation> selectRelationAllByWxId(String wxId);

    /**
     * 查询关系集合通过微信Id
     * @param wxIds
     * @return
     */
    List<FriendRelation> findRelationListByWxIds(List<String> wxIds);

    /**
     * 删除好友关系通过fromWxId集合
     * @param wxIds
     * @return
     */
    int deleteByFromWxIds(List<String> wxIds);

    /**
     * 删除好友关系通过toWxId集合
     * @param wxIds
     * @return
     */
    int deleteByToWxIds(List<String> wxIds);
}