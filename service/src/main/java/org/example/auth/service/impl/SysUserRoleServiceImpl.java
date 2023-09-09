package org.example.auth.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.auth.mapper.SysUserRoleMapper;
import org.example.auth.service.SysUserRoleService;
import org.example.model.system.SysUserRole;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户角色 服务实现类
 * </p>
 *
 * @author jerry
 * @since 2023-03-02
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

}
