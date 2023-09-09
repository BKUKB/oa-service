package org.example;

import org.example.auth.mapper.SysUserMapper;
import org.example.model.system.SysUser;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

@SpringBootApplication(scanBasePackages = "org.example")
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class,args);
    }
}