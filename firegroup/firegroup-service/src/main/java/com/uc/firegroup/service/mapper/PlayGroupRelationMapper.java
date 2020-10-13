package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.PlayGroupRelation;

public interface PlayGroupRelationMapper {
    int deleteByPrimaryKey(Integer playGroupRelationId);

    int insert(PlayGroupRelation record);

    int insertSelective(PlayGroupRelation record);

    PlayGroupRelation selectByPrimaryKey(Integer playGroupRelationId);

    int updateByPrimaryKeySelective(PlayGroupRelation record);

    int updateByPrimaryKey(PlayGroupRelation record);
}