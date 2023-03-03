package com.example.datasourceprimordialdemo2.controller;


import com.example.datasourceprimordialdemo2.datasource.annotation.DataSource;
import com.example.datasourceprimordialdemo2.datasource.enums.DataSourceType;
import com.example.datasourceprimordialdemo2.entity.User;
import com.example.datasourceprimordialdemo2.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * (User)表控制层
 *
 * @author makejava
 * @since 2022-11-08 11:00:39
 */
@RestController
@RequestMapping("user")
public class UserController {
    /**
     * 服务对象
     */
    @Resource
    private UserService userService;

    /**
     * 通过主键查询单条数据
     *
     * @return 单条数据
     */
    @GetMapping("/0")
    @DataSource(DataSourceType.MASTER)
    public ResponseEntity<List<User>> query() {
        return ResponseEntity.ok(this.userService.query());
    }

    @GetMapping("/1")
    @DataSource(DataSourceType.SLAVE1)
    public ResponseEntity<List<User>> query2() {
        return ResponseEntity.ok(this.userService.query());
    }

    @GetMapping("/2")
    @DataSource(DataSourceType.SLAVE2)
    public ResponseEntity<List<User>> query3() {
        return ResponseEntity.ok(this.userService.query());
    }
}

