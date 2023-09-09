package org.example.process.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.example.auth.service.SysUserService;
import org.example.model.process.Process;
import org.example.model.process.ProcessRecord;
import org.example.model.process.ProcessTemplate;
import org.example.model.system.SysUser;
import org.example.process.mapper.ProcessMapper;
import org.example.process.service.ProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.process.service.Process_recordService;
import org.example.process.service.Process_templateService;
import org.example.security.custom.LoginUserInfoHelper;
import org.example.vo.process.ApprovalVo;
import org.example.vo.process.ProcessFormVo;
import org.example.vo.process.ProcessQueryVo;
import org.example.vo.process.ProcessVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author lxc
 * @since 2023-09-07
 */
@Service
public class ProcessServiceImpl extends ServiceImpl<ProcessMapper, Process> implements ProcessService {

    @Autowired
    private ProcessMapper processMapper;
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> processPage, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> page = processMapper.selectPage(processPage, processQueryVo);
        return page;
    }

    @Autowired
    private RepositoryService repositoryService;
    @Override
    public void deployByZip(String processDefinitionPath) {
        // 定义zip输入流
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream(processDefinitionPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        // 流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();
    }
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private Process_templateService processTemplateService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private Process_recordService processRecordService;
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //1.根据当前用户id获取用户信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        //2.根据审批模板id把模板信息查询
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());

        Process process = new Process();

        BeanUtils.copyProperties(processFormVo, process);

        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName()+"发起"+processTemplate.getName()+"申请");
        process.setStatus(1);
        //3.保存提交审批信息到业务表
        processMapper.insert(process);

        //绑定业务id
        String businessKey = String.valueOf(process.getId());

        //流程参数
        Map<String, Object> variables = new HashMap<>();

        //将表单数据放入流程实例中
        JSONObject jsonObject = JSON.parseObject(process.getFormValues());
        JSONObject formData = jsonObject.getJSONObject("formData");
        HashMap<String, Object> map = new HashMap<>();
        //循环转换
        for (Map.Entry<String, Object> entry: formData.entrySet()){
            map.put(entry.getKey(),entry.getValue());
        }
        variables.put("data",map);
        //4.启动流程实例
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey(processTemplate.getProcessDefinitionKey(), businessKey, variables);
        //业务表关联当前流程实例id
        String processInstanceId = processInstance.getId();
        process.setProcessInstanceId(processInstanceId);

        //5.计算下一个审批人，可能有多个（并行审批）
        List<Task> taskList = this.getCurrentTaskList(processInstanceId);
        if(!CollectionUtils.isEmpty(taskList)){
            ArrayList<String> assigneeList = new ArrayList<>();
            for(Task task : taskList){
                SysUser user = sysUserService.getUserByUserName(task.getAssignee());
                assigneeList.add(user.getUsername());

                //推送消息给下一个审批人，后续完善
            }
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
        }
        processMapper.updateById(process);
        //记录操作行为
        processRecordService.record(process.getId(), 1, "发起申请");
    }

    @Override
    public IPage<ProcessVo> findPending(Page<Process> pageParam) {
        //1.封装查询条件，根据当前登录的用户名称
        // 根据当前人的ID查询
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .orderByTaskCreateTime()
                .desc();
        //2.调用方法分页条件查询，返回list集合，待办任务集合
        List<Task> list = query
                .listPage(
                        //开始位置，pageParam.getCurrent()得到当前页数
                        (int) ((pageParam.getCurrent() - 1) * pageParam.getSize())
                        //每页显示的记录数
                        , (int) pageParam.getSize()
                );
        Long totalCount = query.count();
        //3.封装返回list集合数据到List<ProcessVo>里面  List<Task> ——> List<ProcessVo>
        List<ProcessVo> processList = new ArrayList<>();
        // 根据流程的业务ID查询实体并关联
        for (Task item :
                list) {
            String processInstanceId = item.getProcessInstanceId();
            //获取流程实例对象
            ProcessInstance processInstance = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if(processInstance == null){
                continue;
            }
            // 获得业务key
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }

            //获得Process对象
            Process process = baseMapper.selectById(Long.parseLong(businessKey));
            //由于之前测试activit时有使用businessKey作为输入，创建与张三相关的流程实例，该实例与oa_process等表未关联，作此处理
            if(process==null){
                continue;
            }
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId(item.getId());
            processList.add(processVo);
        }
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        //4.封装返回IPage对象
        page.setRecords(processList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        Process process = baseMapper.selectById(id);
        List<ProcessRecord> processRecordList = processRecordService
                .list(new LambdaQueryWrapper<ProcessRecord>()
                        .eq(ProcessRecord::getProcessId, id));
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("process",process);
        map.put("processRecordList",processRecordList);
        map.put("processTemplate",processTemplate);
        //计算当前用户是否可以审批，能够查看详情的用户不是都能审批，审批后也不能重复审批
        boolean isApprove = false;
        List<Task> currentTaskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(currentTaskList)){
            for(Task task:currentTaskList){
                if(task.getAssignee().equals(LoginUserInfoHelper.getUsername())){
                    //可能会产生bug？task 列表 不一定都是分配同一人完成，但此处做了唯一判定
                    isApprove = true;
                }
            }
        }
        map.put("isApprove", isApprove);
        return map;
    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        Map<String, Object> variables1 = taskService.getVariables(approvalVo.getTaskId());
        for (Map.Entry<String, Object> entry: variables1.entrySet()){
            System.out.println("key = "+entry.getKey()+" , value = "+entry.getValue());
        }
        String taskId = approvalVo.getTaskId();
        if(approvalVo.getStatus() == 1){
            //已通过
            Map<String, Object> variables = new HashMap<String, Object>();
            taskService.complete(taskId, variables);
        }else {
            //驳回
            this.endTask(taskId);
        }
        String description = approvalVo.getStatus().intValue() == 1 ? "已通过" : "驳回";
        processRecordService.record(approvalVo.getProcessId(), approvalVo.getStatus(), description);
        //计算下一个审批人
        Process process = this.getById(approvalVo.getProcessId());
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if (!CollectionUtils.isEmpty(taskList)) {
            List<String> assigneeList = new ArrayList<>();
            for (Task task :
                    taskList) {
                SysUser sysUser = sysUserService.getUserByUserName(task.getAssignee());
                assigneeList.add(sysUser.getName());

                //推送消息给下一个审批人
            }
            process.setDescription("等待" + StringUtils.join(assigneeList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else {
            if(approvalVo.getStatus().intValue() == 1) {
                process.setDescription("审批完成（同意）");
                process.setStatus(2);
            } else {
                process.setDescription("审批完成（拒绝）");
                process.setStatus(-1);
            }
        }
        //推送消息给申请人
        this.updateById(process);
    }

    @Override
    public void endTask(String taskId) {
        //  1.根据任务id获取当前任务对象
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //  2.获取流程定义模型 BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        //  3.获取结束流向节点，即 end 节点
        List endEventList = bpmnModel.getMainProcess().
                findFlowElementsOfType(
                        //传入结束事件
                        EndEvent.class);
        // 节点可能为null
        if(CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        //  4.获得当前流向节点
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        //  5.清理当前流向，即断开正常流向
        currentFlowNode.getOutgoingFlows().clear();

        //  6.建立新方向，结束流向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  7.当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //  8.完成当前任务
        taskService.complete(task.getId());
    }

    @Autowired
    private HistoryService historyService;

    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> pageParam) {
        // 封装查询条件   根据当前人的ID查询
        HistoricTaskInstanceQuery query = historyService
                .createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished()
                .orderByTaskCreateTime()
                .desc();
        //  调用方法条件分页查询，返回list集合
        List<HistoricTaskInstance> list = query
                .listPage((int) (
                        (pageParam.getCurrent() - 1) * pageParam.getSize()),
                        (int) pageParam.getSize()
                );
        long totalCount = query.count();
        //遍历返回list集合，封装list<ProcessVo>
        List<ProcessVo> processList = new ArrayList<>();
        for (HistoricTaskInstance item : list) {
            String processInstanceId = item.getProcessInstanceId();
            Process process = this.getOne(new LambdaQueryWrapper<Process>().eq(Process::getProcessInstanceId, processInstanceId));
            if(process==null)continue;
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId("0");
            processList.add(processVo);
        }
        //  IPage封装分页查询所有数据，返回
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        page.setRecords(processList);
        return page;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = processMapper.selectPage(pageParam, processQueryVo);
        for (ProcessVo item : page.getRecords()) {
            item.setTaskId("0");
        }
        return page;
    }

    @Autowired
    private TaskService taskService;
    private List<Task> getCurrentTaskList(String processInstanceId) {
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        return list;
    }
}
