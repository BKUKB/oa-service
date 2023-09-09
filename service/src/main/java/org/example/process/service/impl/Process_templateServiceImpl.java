package org.example.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.model.process.ProcessTemplate;
import org.example.model.process.ProcessType;
import org.example.process.mapper.Process_templateMapper;
import org.example.process.service.ProcessService;
import org.example.process.service.Process_templateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.process.service.Process_typeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
@Service
public class Process_templateServiceImpl extends ServiceImpl<Process_templateMapper, ProcessTemplate> implements Process_templateService {

    @Autowired
    private Process_typeService process_typeService;
    @Override
    public IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> pageInfo) {
        LambdaQueryWrapper<ProcessTemplate> processTemplateLambdaQueryWrapper = new LambdaQueryWrapper<>();
        processTemplateLambdaQueryWrapper.orderByDesc(ProcessTemplate::getId);
        //分页查询
        IPage<ProcessTemplate> processTemplatePage = baseMapper.selectPage(pageInfo, processTemplateLambdaQueryWrapper);
        //查询返回数据，从分页数据得到list集合
        List<ProcessTemplate> records = processTemplatePage.getRecords();
        //获得type id列表
        List<Long> collect = records.stream().map(processTemplate -> processTemplate.getProcessTypeId()).collect(Collectors.toList());

        if(!CollectionUtils.isEmpty(collect)){
            //查询type name
            Map<Long, ProcessType> processTypeIdToProcessTypeMap  = process_typeService.list(
                            new LambdaQueryWrapper<ProcessType>().in(ProcessType::getId, collect))
                    .stream()
                    .collect(
                            Collectors.toMap(ProcessType::getId, ProcessType -> ProcessType));
            //设置type name
            for (ProcessTemplate processTemplate:records) {
                ProcessType processType = processTypeIdToProcessTypeMap.get(processTemplate.getProcessTypeId());
                if(null == processType) continue;
                processTemplate.setProcessTypeName(processType.getName());
            }
        }

        return processTemplatePage;
    }

    @Autowired
    private ProcessService processService;
    @Transactional
    @Override
    public void publish(Long id) {
        ProcessTemplate processTemplate = this.getById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);
        //部署流程定义

        //优先发布在线流程设计
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())) {
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
        }
    }

}
