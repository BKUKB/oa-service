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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessTest3 {

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void deployProcessByZip() {
        // 定义zip输入流
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream(
                        "process/jiaban.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        // 流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();
        System.out.println("流程部署id：" + deployment.getId());
        System.out.println("流程部署名称：" + deployment.getName());
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Autowired
    private RuntimeService runtimeService;

    //UEL-value
    @Test
    public void startUpProcess01() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("assignee1","zhangsan");
        variables.put("assignee2","lisi");
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //UEL-method 方式
    @Test
    public void startUpProcess02() {
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //配置监听器
    /**
     * 启动流程实例
     */
    @Test
    public void startUpProcess03() {
        //创建流程实例,我们需要知道流程定义的key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
        //输出实例的相关信息
        System.out.println("流程定义id：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例id：" + processInstance.getId());
    }

    @Autowired
    private TaskService taskService;

    /**
     * 查询当前个人待执行的任务
     */
    @Test
    public void findPendingTaskList() {
        //任务负责人
        String assignee = "jieke";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee)//只查询该任务负责人的任务
                .list();
        for (Task task : list) {
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id：" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //设置局部流程变量
    @Test
    public void completLocalTask() {
        List<Task> task = taskService.createTaskQuery()
                .taskAssignee("zhangsan").list();  //要查询的负责人

                //返回一条
        Task task1 = task.get(0);
        // 设置local变量，作用域为该任务
        taskService.setVariableLocal(task1.getId(),"assignee2","li");
        // 查看local变量
        System.out.println(taskService.getVariableLocal(task1.getId(), "assignee2"));
        //完成任务,参数：任务id
        taskService.complete(task1.getId());
    }
}
