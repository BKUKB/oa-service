package org.example.auth.mapper;

import org.example.model.system.SysUser;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ModelTest {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Test
    public void mapperTest(){
        List<SysUser> users = sysUserMapper.selectList(null);
    }
}
