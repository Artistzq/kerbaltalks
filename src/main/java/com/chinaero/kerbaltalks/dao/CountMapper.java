package com.chinaero.kerbaltalks.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * @Author : Artis Yao
 */
@Mapper
public interface CountMapper {

    public void incCount(String tableName);

    public void setCount(String tableName, int count);
}
