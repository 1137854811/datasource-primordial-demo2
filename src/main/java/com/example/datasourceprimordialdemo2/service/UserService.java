package com.example.datasourceprimordialdemo2.service;


import com.example.datasourceprimordialdemo2.entity.User;

import java.util.List;

/**
 * (User)表服务接口
 *
 * @author makejava
 * @since 2022-11-08 11:00:43
 */
public interface UserService  {


    List<User> query();


    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 实例对象
     */
    User insert(User user);
}
