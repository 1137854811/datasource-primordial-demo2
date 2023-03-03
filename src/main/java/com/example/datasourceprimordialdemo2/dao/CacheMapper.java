package com.example.datasourceprimordialdemo2.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author： Aaron
 * @date： 2023-02-28 17:43
 */
@Mapper
public interface CacheMapper {
    List<String> getCompany(@Param("companyName") String companyName);
}
