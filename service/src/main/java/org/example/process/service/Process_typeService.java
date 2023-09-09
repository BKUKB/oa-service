package org.example.process.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.example.model.process.ProcessType;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
public interface Process_typeService extends IService<ProcessType> {

    List<ProcessType> findProcessType();
}
