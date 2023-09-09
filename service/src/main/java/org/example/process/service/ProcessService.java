package org.example.process.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.model.process.Process;
import org.example.vo.process.ApprovalVo;
import org.example.vo.process.ProcessFormVo;
import org.example.vo.process.ProcessQueryVo;
import org.example.vo.process.ProcessVo;

import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
public interface ProcessService extends IService<Process> {

    IPage<ProcessVo> selectPage(Page<ProcessVo> processPage, ProcessQueryVo processQueryVo);

    void deployByZip(String processDefinitionPath);

    void startUp(ProcessFormVo processFormVo);

    IPage<ProcessVo> findPending(Page<Process> pageParam);

    Map<String,Object> show(Long id);

    void approve(ApprovalVo approvalVo);

    void endTask(String taskId);

    IPage<ProcessVo> findProcessed(Page<Process> pageParam);

    IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam);
}
