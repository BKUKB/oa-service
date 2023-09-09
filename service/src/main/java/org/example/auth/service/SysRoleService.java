package org.example.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.model.system.SysRole;
import org.example.vo.system.AssginRoleVo;

import java.util.Map;

public interface SysRoleService extends IService<SysRole> {
    Map<String, Object> findRoleDataByUserId(Long userId);

    // 2、为用户分配角色
    void doAssign(AssginRoleVo assginRoleVo);

}
