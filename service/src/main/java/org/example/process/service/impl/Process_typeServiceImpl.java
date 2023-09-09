package org.example.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.model.process.ProcessTemplate;
import org.example.model.process.ProcessType;
import org.example.process.mapper.Process_typeMapper;
import org.example.process.service.Process_templateService;
import org.example.process.service.Process_typeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
@Service
public class Process_typeServiceImpl extends ServiceImpl<Process_typeMapper, ProcessType> implements Process_typeService {

    @Autowired
    private Process_templateService processTemplateService;
    @Override
    public List<ProcessType> findProcessType() {
        //先找到所有类型id
        //1 查询所有审批分类，返回list集合
        List<ProcessType> processTypes = baseMapper.selectList(null);
        //再根据类型id查找对应的模板
        //2 遍历返回所有审批分类list集合
        for(ProcessType processType:processTypes){
            //3 得到每个审批分类，根据审批分类id查询对应审批模板
            //审批分类id
            Long id = processType.getId();
            //根据审批分类id查询对应审批模板
            LambdaQueryWrapper<ProcessTemplate> processTemplateLambdaQueryWrapper = new LambdaQueryWrapper<>();
            processTemplateLambdaQueryWrapper.eq(ProcessTemplate::getProcessTypeId,id);
            List<ProcessTemplate> list = processTemplateService.list(processTemplateLambdaQueryWrapper);

            //4 根据审批分类id查询对应审批模板数据（List）封装到每个审批分类对象里面
            processType.setProcessTemplateList(list);
        }
        //封装
        return processTypes;
    }
}
