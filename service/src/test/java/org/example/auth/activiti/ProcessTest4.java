package org.example.auth.activiti;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessTest4 {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    // 1、部署流程定义
    @Test
    public void deployProcess() {
        Deployment deploy = repositoryService.createDeployment().addClasspathResource("process/jiaban04.bpmn20.xml").name("加班申请流程04").deploy();
        System.out.println("deploy.getId() = " + deploy.getId()); // f204be8a-bb48-11ed-950e-005056c00001
        System.out.println("deploy.getName() = " + deploy.getName()); // 加班申请流程04
    }

    // 1.5、启动流程实例
    @Test
    public void startProcessInstance() {
//        Map<String, Object> map = new HashMap<>();
        // 设置任务人
//        map.put("assignee1","tom");
//        map.put("assignee2","jerry");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("jiaban04");

        System.out.println("processInstance.getProcessDefinitionId() = " + processInstance.getProcessDefinitionId()); // jiaban04:1:f210f38c-bb48-11ed-950e-005056c00001

        System.out.println("processInstance.getId() = " + processInstance.getId()); // 428d0c0f-bb49-11ed-83a5-005056c00001
    }

    // 2、查询组任务
    @Test
    public void findGroupTaskList(){
        List<Task> list = taskService.createTaskQuery()
                .taskCandidateUser("tom")
                .list();
        for (Task task : list) {
            System.out.println("----------------------------");
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    // 3、分配组任务
    @Test
    public void claimTask(){
        Task task = taskService.createTaskQuery()
                .taskCandidateUser("tom")
                .singleResult();
        if (task!=null){
            taskService.claim(task.getId(),"tom");
            System.out.println("分配任务完成");
        }
    }

    // 4、查询个人的代办任务--tom
    @Test
    public void findTaskList(){
        String assign = "tom";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assign).list();
        for (Task task : list) {
            System.out.println("task.getProcessInstanceId() = " + task.getProcessInstanceId()); //7f720dd9-bb1d-11ed-b6e9-005056c00001
            System.out.println("任务id：" + task.getId()); // 7f759051-bb1d-11ed-b6e9-005056c00001
            System.out.println("任务负责人：" + task.getAssignee()); // tom
            System.out.println("任务名称：" + task.getName()); // 经理审批
        }
    }


    // 5、办理个人任务
    @Test
    public void completeGroupTask() {
        Task task = taskService.createTaskQuery()
                .taskAssignee("tom")  //要查询的负责人
                .singleResult();//返回一条

        taskService.complete(task.getId());
    }


    //


}
