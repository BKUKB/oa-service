package org.example.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.auth.mapper.SysUserMapper;
import org.example.auth.service.SysUserService;
import org.example.common.config.exception.MyException;
import org.example.common.result.Result;
import org.example.common.result.ResultCodeEnum;
import org.example.model.system.SysUser;
import org.example.security.custom.LoginUserInfoHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    @Transactional
    @Override
    public void updateStatus(Long id, Integer status) {
        // 根据用户 userid 查询用户对象
        SysUser sysUser = baseMapper.selectById(id);
        // 设置修改状态
        if (status == 0 || status == 1) {
            sysUser.setStatus(status);
            // 调用方法进行修改
            baseMapper.updateById(sysUser);
        } else {
            log.info("数值不合法");
            throw new MyException(ResultCodeEnum.FAIL);
        }
    }

    // 根据用户名查询
    @Override
    public SysUser getUserByUserName(String username) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername,username);

        SysUser sysUser = baseMapper.selectOne(queryWrapper);
        return sysUser;
    }

    @Override
    public Map<String, Object> getCurrentUser() {
        SysUser sysUser = baseMapper.selectById(LoginUserInfoHelper.getUserId());
        //SysDept sysDept = sysDeptService.getById(sysUser.getDeptId());
        //SysPost sysPost = sysPostService.getById(sysUser.getPostId());
        Map<String, Object> map = new HashMap<>();
        map.put("name", sysUser.getName());
        map.put("phone", sysUser.getPhone());
        //map.put("deptName", sysDept.getName());
        //map.put("postName", sysPost.getName());
        return map;
    }

}
