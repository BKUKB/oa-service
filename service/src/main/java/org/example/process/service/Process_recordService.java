package org.example.process.service;

import org.example.model.process.ProcessRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批记录 服务类
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
public interface Process_recordService extends IService<ProcessRecord> {

    void record(Long id, Integer status, String 发起申请);
}
