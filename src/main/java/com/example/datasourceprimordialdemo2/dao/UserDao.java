package com.example.datasourceprimordialdemo2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.datasourceprimordialdemo2.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * (User)表数据库访问层
 *
 * @author makejava
 * @since 2022-11-08 11:00:41
 */
public interface UserDao extends BaseMapper<User> {

}

