package com.ruoyi.process.Linstener;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.framework.web.domain.server.Sys;
import com.ruoyi.process.definition.domain.TaskAssign;
import com.ruoyi.process.definition.service.impl.ProcessDefinitionService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * ren 用于处理流程提交节点的触发事件
 */
@Component("pruchaseListeners")
public class ActivitiTaskCompleteListener implements TaskListener {



    private ProcessDefinitionService processDefinitionService=SpringUtils.getBean(ProcessDefinitionService.class);;


    private RepositoryService repositoryService=SpringUtils.getBean(RepositoryService.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("activiti 监听执行开始");
        //获取流程定义ID,流程实例ID及任务ID
        String taskId = delegateTask.getTaskDefinitionKey();
        String proDefId = delegateTask.getProcessDefinitionId();
        String procInsId = delegateTask.getProcessInstanceId();
        //获取流程定义key
        String proDefKey = repositoryService.createProcessDefinitionQuery().processDefinitionId(proDefId).latestVersion().singleResult().getKey();
        //如为启动任务则跳过处理
        if (!taskId.equalsIgnoreCase("startTask")) {
            TaskAssign taskAssign = new TaskAssign(taskId, proDefKey);
            // 根据流程定义key,taskId获取被设置的用户信息
            List<TaskAssign> roleList = processDefinitionService.selectListTaskRoleAssign(taskAssign);
            List<TaskAssign> userList = processDefinitionService.selectListTaskUserAssign(taskAssign);
            for (TaskAssign t : userList) {
                delegateTask.addCandidateGroup(t.getAssignId());
                delegateTask.addCandidateUser(t.getAssignId());
            }

            for (TaskAssign t : roleList) {
                delegateTask.addCandidateUser(t.getAssignId());
            }

        }

        System.out.println("activiti 监听器执行结束");
    }
}
