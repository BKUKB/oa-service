package org.example.process.service.impl;

import org.example.auth.service.SysUserService;
import org.example.model.process.ProcessRecord;
import org.example.model.system.SysUser;
import org.example.process.mapper.Process_recordMapper;
import org.example.process.service.Process_recordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.security.custom.LoginUserInfoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 审批记录 服务实现类
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
@Service
public class Process_recordServiceImpl extends ServiceImpl<Process_recordMapper, ProcessRecord> implements Process_recordService {

    @Autowired
    private SysUserService sysUserService;
    @Override
    public void record(Long id, Integer status, String description) {
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setProcessId(id);
        processRecord.setStatus(status);
        processRecord.setDescription(description);
        processRecord.setOperateUserId(sysUser.getId());
        processRecord.setOperateUser(sysUser.getName());
        baseMapper.insert(processRecord);
    }
}
