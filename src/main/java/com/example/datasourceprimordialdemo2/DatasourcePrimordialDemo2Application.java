package com.example.datasourceprimordialdemo2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author dev
 */
@MapperScan(basePackages = "com.example.datasourceprimordialdemo2.dao")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class DatasourcePrimordialDemo2Application {

    public static void main(String[] args) {
        SpringApplication.run(DatasourcePrimordialDemo2Application.class, args);
    }

}
