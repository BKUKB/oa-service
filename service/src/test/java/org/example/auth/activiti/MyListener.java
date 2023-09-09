package org.example.auth.activiti;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        if(delegateTask.getName().equals("经理审批")){
            //这里指定任务负责人
            delegateTask.setAssignee("jieke");
        } else if(delegateTask.getName().equals("人事审批")){
            //这里指定任务负责人
            delegateTask.setAssignee("lisi");
        }
    }
}
