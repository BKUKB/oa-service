package org.example.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.model.process.ProcessTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
public interface Process_templateService extends IService<ProcessTemplate> {

    //分页查询审批模板，把审批类型对应名称查询
    IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageInfo);

    void publish(Long id);
}
