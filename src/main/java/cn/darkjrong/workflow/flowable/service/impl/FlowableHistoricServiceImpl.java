package cn.darkjrong.workflow.flowable.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import cn.darkjrong.workflow.flowable.domain.HistoricActivityInfo;
import cn.darkjrong.workflow.flowable.domain.HistoricTaskInfo;
import cn.darkjrong.workflow.flowable.domain.ReturnTask;
import cn.darkjrong.workflow.flowable.service.FlowableHistoricService;
import cn.darkjrong.workflow.flowable.service.FlowableTaskService;
import cn.darkjrong.workflow.flowable.utils.PageableUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.impl.persistence.entity.CommentEntity;
import org.flowable.engine.impl.util.ExecutionGraphUtil;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Flowable历史任务服务实现类
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
@Slf4j
@Service
public class FlowableHistoricServiceImpl extends FlowableFactory implements FlowableHistoricService {

    @Autowired
    private FlowableTaskService flowableTaskService;

    @Override
    public HistoricTaskInstanceQuery createHistoricTaskInstanceQuery() {
        return historyService.createHistoricTaskInstanceQuery();
    }

    @Override
    public HistoricActivityInstanceQuery createHistoricActivityInstanceQuery() {
        return historyService.createHistoricActivityInstanceQuery();
    }

    @Override
    public HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
        return historyService.createHistoricProcessInstanceQuery();
    }

    @Override
    public HistoricTaskInfo queryHistoricTask(String taskId) {
        HistoricTaskInstance historicTaskInstance = createHistoricTaskInstanceQuery()
                .taskId(taskId).includeProcessVariables().includeTaskLocalVariables().singleResult();
        return convertHistoricTask(historicTaskInstance);
    }

    @Override
    public HistoricActivityInfo queryHistoricActivity(String activityId) {
        HistoricActivityInstance historicActivityInstance = createHistoricActivityInstanceQuery().activityId(activityId).singleResult();
        return convertHistoricActivity(historicActivityInstance);
    }

    @Override
    public List<HistoricTaskInfo> queryHistoricTasks(String processInstanceId) {

        List<HistoricTaskInstance> historicTaskInstanceList = createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeTaskLocalVariables()
                .includeProcessVariables()
                .orderByHistoricTaskInstanceStartTime()
                .asc()
                .list();

        if (CollectionUtil.isNotEmpty(historicTaskInstanceList)) {
            return historicTaskInstanceList.stream()
                    .map(this::convertHistoricTask)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<HistoricActivityInfo> queryHistoricActivitys(String processInstanceId) {
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();

        if (CollectionUtil.isNotEmpty(historicActivityInstances)) {
            return historicActivityInstances.stream()
                    .filter(activityInstance -> BpmnXMLConstants.ELEMENT_TASK_USER.equals(activityInstance.getActivityType())
                            || BpmnXMLConstants.ELEMENT_EVENT_START.equals(activityInstance.getActivityType())
                            || BpmnXMLConstants.ELEMENT_EVENT_END.equals(activityInstance.getActivityType()))
                    .map(this::convertHistoricActivity).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public PageInfo<HistoricTaskInfo> queryHistoricTasks(String assignee, Integer pageNum, Integer pageSize) {
        HistoricTaskInstanceQuery query = createHistoricTaskInstanceQuery().taskAssignee(assignee)
                .orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc();
        List<HistoricTaskInstance> historicTaskInstances = query.listPage((pageNum - 1) * pageSize, pageSize);
        List<HistoricTaskInfo> taskInstances = CollUtil.newArrayList();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            taskInstances.add(queryHistoricTask(historicTaskInstance.getId()));
        }
       return PageableUtils.basicQuery(pageNum, pageSize, query.count(), taskInstances);
    }

    @Override
    public List<HistoricTaskInfo> queryHistoricTasksByAssignee(String assignee) {
        HistoricTaskInstanceQuery query = createHistoricTaskInstanceQuery().taskAssignee(assignee)
                .orderByTaskPriority().desc().orderByTaskCreateTime().asc();
        List<HistoricTaskInstance> historicTaskInstances = query.list();
        List<HistoricTaskInfo> taskInstances = CollUtil.newArrayList();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            taskInstances.add(queryHistoricTask(historicTaskInstance.getId()));
        }
        return taskInstances;
    }

    @Override
    public HistoricProcessInstance queryHistoricProcessInstance(String procInstId) {
        return createHistoricProcessInstanceQuery().processInstanceId(procInstId).singleResult();
    }

    @Override
    public List<HistoricActivityInstance> queryHistoricActivityInstanceAsc(String procInstId) {
        return createHistoricActivityInstanceQuery().processInstanceId(procInstId).orderByHistoricActivityInstanceId().asc().list();
    }

    @Override
    public List<HistoricProcessInstance> queryHistoricFinishedByProcInstId(String procInstId) {
        return  historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstId).finished().list();
    }

    @Override
    public List<HistoricTaskInstance> queryHistoricTaskOrderCreateTimeDesc(String procInstId) {
        return createHistoricTaskInstanceQuery().processInstanceId(procInstId).orderByTaskCreateTime().desc().list();
    }

    @Override
    public List<HistoricTaskInstance> queryHistoricTaskOrderCreateTimeAsc(String procInstId) {
        return createHistoricTaskInstanceQuery()
                .processInstanceId(procInstId).orderByTaskCreateTime()
                .asc()
                .list();
    }

    @Override
    public HistoricTaskInstance queryHistoricTaskInstance(String taskId) {
        return createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
    }

    @Override
    public List<HistoricProcessInstance> queryHistoricTasksByStartUser(String user) {
        return historyService.createHistoricProcessInstanceQuery()
                .startedBy(user)
                .orderByProcessInstanceStartTime()
                .asc()
                .list();
    }

    @Override
    public List<ReturnTask> queryReturnTasks(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (ObjectUtil.isNotNull(task)) {

            List<ReturnTask> returnTasks = CollectionUtil.newArrayList();

            // 任务定义key 等于 当前任务节点id
            String taskDefinitionKey = task.getTaskDefinitionKey();
            Process mainProcess = repositoryService.getBpmnModel(task.getProcessDefinitionId()).getMainProcess();
            // 当前节点
            FlowNode currentFlowElement = (FlowNode) mainProcess.getFlowElement(taskDefinitionKey, true);
            // 查询历史节点实例
            List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .finished()
                    .orderByHistoricActivityInstanceEndTime().asc().list();
            Collection<HistoricActivityInstance> activityList = activityInstanceList.stream()
                    .filter(activityInstance -> !taskDefinitionKey.equals(activityInstance.getActivityId())
                            && BpmnXMLConstants.ELEMENT_TASK_USER.equals(activityInstance.getActivityType()))
                    .collect(Collectors.toMap(HistoricActivityInstance::getActivityId, Function.identity(), (a1, a2) -> a2)).values();

            for (HistoricActivityInstance activity : activityList) {
                // 回退到主流程的节点
                FlowNode toBackFlowElement = (FlowNode) mainProcess.getFlowElement(activity.getActivityId(), true);
                if (ObjectUtil.isNotNull(toBackFlowElement)
                        && ExecutionGraphUtil.isReachable(mainProcess,toBackFlowElement,currentFlowElement, CollectionUtil.newHashSet())) {
                    ReturnTask returnTask = new ReturnTask();
                    returnTask.setTaskId(activity.getTaskId());
                    returnTask.setAssignee(activity.getAssignee());
                    returnTask.setProcessDefinitionId(activity.getProcessDefinitionId());
                    returnTask.setProcessInstanceId(activity.getProcessInstanceId());
                    returnTask.setTaskName(toBackFlowElement.getName());
                    returnTasks.add(returnTask);
                }
            }
            return returnTasks;
        }
        return Collections.emptyList();
    }

    @Override
    public List<ReturnTask> queryReturnTasks(String processInstanceId, String assignee, Set<String> candidateGroups) {
        Task task = flowableTaskService.queryTaskByAssigneeOrCandidate(processInstanceId, assignee, candidateGroups);
        if (ObjectUtil.isNotEmpty(task)) {
            return this.queryReturnTasks(task.getId());
        }
        return Collections.emptyList();
    }

    /**
     * 转换历史活动
     *
     * @param historicActivityInstance 历史性活动实例
     * @return {@link HistoricActivityInfo}
     */
    private HistoricActivityInfo convertHistoricActivity(HistoricActivityInstance historicActivityInstance) {
        if (ObjectUtil.isNotNull(historicActivityInstance)) {
            HistoricActivityInfo historicActivityInfo = new HistoricActivityInfo();
            historicActivityInfo.setId(historicActivityInstance.getId());
            historicActivityInfo.setActivityId(historicActivityInstance.getActivityId());
            historicActivityInfo.setActivityName(historicActivityInstance.getActivityName());
            historicActivityInfo.setActivityType(historicActivityInstance.getActivityType());
            historicActivityInfo.setExecutionId(historicActivityInstance.getExecutionId());
            historicActivityInfo.setAssignee(historicActivityInstance.getAssignee());
            historicActivityInfo.setTaskId(historicActivityInstance.getTaskId());
            historicActivityInfo.setCalledProcessInstanceId(historicActivityInstance.getCalledProcessInstanceId());
            historicActivityInfo.setProcessDefinitionId(historicActivityInstance.getProcessDefinitionId());
            historicActivityInfo.setProcessInstanceId(historicActivityInstance.getProcessInstanceId());
            Optional.ofNullable(historicActivityInstance.getStartTime()).ifPresent(a -> historicActivityInfo.setStartTime(a.getTime()));
            Optional.ofNullable(historicActivityInstance.getEndTime()).ifPresent(a -> historicActivityInfo.setEndTime(a.getTime()));
            historicActivityInfo.setDuration(historicActivityInstance.getDurationInMillis());
            historicActivityInfo.setDeleteReason(historicActivityInstance.getDeleteReason());
            List<Comment> comments = flowableTaskService.queryTaskComments(historicActivityInstance.getTaskId(), CommentEntity.TYPE_COMMENT);
            if (CollectionUtil.isNotEmpty(comments)) {
                comments.stream()
                        .filter(a -> StrUtil.isNotBlank(a.getFullMessage())).findAny()
                        .ifPresent(b -> historicActivityInfo.setDescription(b.getFullMessage()));
            }
            return historicActivityInfo;
        }
        return null;
    }

    @Override
    public HistoricTaskInfo convertHistoricTask(HistoricTaskInstance historicTaskInstance) {
        if (ObjectUtil.isNotNull(historicTaskInstance)) {
            HistoricTaskInfo historicTaskInfo = new HistoricTaskInfo();
            historicTaskInfo.setId(historicTaskInstance.getId());
            historicTaskInfo.setProcessDefinitionId(historicTaskInstance.getProcessDefinitionId());
            historicTaskInfo.setTaskDefKey(historicTaskInstance.getTaskDefinitionKey());
            historicTaskInfo.setProcessInstanceId(historicTaskInstance.getProcessInstanceId());
            historicTaskInfo.setProcessDefinitionId(historicTaskInstance.getProcessDefinitionId());
            historicTaskInfo.setExecutionId(historicTaskInstance.getExecutionId());
            historicTaskInfo.setName(historicTaskInstance.getName());
            historicTaskInfo.setParentTaskId(historicTaskInstance.getParentTaskId());
            historicTaskInfo.setOwner(historicTaskInstance.getOwner());
            historicTaskInfo.setAssignee(historicTaskInstance.getAssignee());
            historicTaskInfo.setStartTime(historicTaskInstance.getCreateTime());
            historicTaskInfo.setClaimTime(historicTaskInstance.getClaimTime());
            historicTaskInfo.setEndTime(historicTaskInstance.getEndTime());
            historicTaskInfo.setDuration(historicTaskInstance.getDurationInMillis());
            historicTaskInfo.setDeleteReason(historicTaskInstance.getDeleteReason());
            historicTaskInfo.setDueTime(historicTaskInstance.getDueDate());
            historicTaskInfo.setPriority(historicTaskInstance.getPriority());
            historicTaskInfo.setCategory(historicTaskInstance.getCategory());
            historicTaskInfo.setTaskLocalVariables(historicTaskInstance.getTaskLocalVariables());
            historicTaskInfo.setProcessVariables(historicTaskInstance.getProcessVariables());
            List<Comment> comments = flowableTaskService.queryTaskComments(historicTaskInstance.getId(), CommentEntity.TYPE_COMMENT);
            if (CollectionUtil.isNotEmpty(comments)) {
                comments.stream().filter(a -> StrUtil.isNotBlank(a.getFullMessage())).findAny().ifPresent(b -> historicTaskInfo.setDescription(b.getFullMessage()));
            }

            return historicTaskInfo;
        }
        return null;
    }
















}
