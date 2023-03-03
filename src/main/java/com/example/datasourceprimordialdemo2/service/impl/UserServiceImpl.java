package com.example.datasourceprimordialdemo2.service.impl;


import com.example.datasourceprimordialdemo2.dao.UserDao;
import com.example.datasourceprimordialdemo2.entity.User;
import com.example.datasourceprimordialdemo2.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * (User)表服务实现类
 *
 * @author makejava
 * @since 2022-11-08 11:00:44
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;


    @Override
    public List<User> query() {
        return userDao.selectList(null);
    }

    /**
     * 新增数据
     *
     * @param user 实例对象
     * @return 实例对象
     */
    @Override
    public User insert(User user) {
        this.userDao.insert(user);
        return user;
    }
}
