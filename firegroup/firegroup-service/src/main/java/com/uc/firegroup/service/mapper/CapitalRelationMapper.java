package com.uc.firegroup.service.mapper;

import com.uc.firegroup.api.pojo.CapitalRelation;

public interface CapitalRelationMapper {
    int deleteByPrimaryKey(Integer capitalRelationId);

    int insert(CapitalRelation record);

    int insertSelective(CapitalRelation record);

    CapitalRelation selectByPrimaryKey(Integer capitalRelationId);

    int updateByPrimaryKeySelective(CapitalRelation record);

    int updateByPrimaryKey(CapitalRelation record);
}