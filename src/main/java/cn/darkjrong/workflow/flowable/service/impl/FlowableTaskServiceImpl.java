package cn.darkjrong.workflow.flowable.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import cn.darkjrong.workflow.flowable.domain.ActivityInfo;
import cn.darkjrong.workflow.flowable.domain.ExtensionElementInfo;
import cn.darkjrong.workflow.flowable.domain.TaskInfo;
import cn.darkjrong.workflow.flowable.enums.CommentTypeEnum;
import cn.darkjrong.workflow.flowable.enums.VariablesEnum;
import cn.darkjrong.workflow.flowable.service.FlowableHistoricService;
import cn.darkjrong.workflow.flowable.service.FlowableProcessInstanceService;
import cn.darkjrong.workflow.flowable.service.FlowableProcessService;
import cn.darkjrong.workflow.flowable.service.FlowableTaskService;
import cn.darkjrong.workflow.flowable.utils.FlowableUtils;
import cn.darkjrong.workflow.flowable.utils.PageableUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.impl.de.odysseus.el.ExpressionFactoryImpl;
import org.flowable.common.engine.impl.de.odysseus.el.util.SimpleContext;
import org.flowable.common.engine.impl.javax.el.ExpressionFactory;
import org.flowable.common.engine.impl.javax.el.ValueExpression;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.flowable.engine.impl.cmd.AddMultiInstanceExecutionCmd;
import org.flowable.engine.impl.cmd.DeleteMultiInstanceExecutionCmd;
import org.flowable.engine.impl.persistence.entity.CommentEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Flowable任务服务实现类
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
@Slf4j
@Service
public class FlowableTaskServiceImpl extends FlowableFactory implements FlowableTaskService {

    @Autowired
    private FlowableHistoricService flowableHistoricService;

    @Autowired
    private FlowableProcessInstanceService flowableProcessInstanceService;

    @Autowired
    private FlowableProcessService flowableProcessService;

    @Override
    public TaskQuery createTaskQuery() {
        return taskService.createTaskQuery();
    }

    @Override
    public ExecutionQuery createExecutionQuery() {
        return runtimeService.createExecutionQuery();
    }

    @Override
    public List<Execution> queryRunningActivityByProcessInstanceId(String procInstId) {
        return runtimeService.createExecutionQuery().processInstanceId(procInstId).list();
    }

    @Override
    public List<TaskInfo> queryToDoTaskInfo(String processInstanceId) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        if (CollectionUtil.isNotEmpty(taskList)) {
            return taskList.stream().map(a -> queryTaskInfo(a.getId())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public TaskInfo queryTaskInfo(String taskId) {
        Task task = createTaskQuery().taskId(taskId).singleResult();
        Assert.notNull(task, "任务不存在, 请检查");
        return getTaskInfo(task);
    }

    @Override
    public PageInfo<TaskInfo> queryAssigneeTasks(String assignee, Integer pageNum, Integer pageSize) {
        TaskQuery query = createTaskQuery().taskAssignee(assignee);
        return this.queryTask(query, pageNum, pageSize);
    }

    @Override
    public PageInfo<TaskInfo> queryTaskByCandidateUser(String candidateUser, Integer pageNum, Integer pageSize) {
        TaskQuery query = createTaskQuery().taskCandidateUser(candidateUser);
        return this.queryTask(query, pageNum, pageSize);
    }

    @Override
    public PageInfo<TaskInfo> queryTaskByCandidateOrAssigned(String candidateUser, Integer pageNum, Integer pageSize) {
        TaskQuery query = createTaskQuery().taskCandidateOrAssigned(candidateUser);
        return this.queryTask(query, pageNum, pageSize);
    }

    @Override
    public PageInfo<TaskInfo> queryTask(TaskQuery query, Integer pageNum, Integer pageSize) {
        List<Task> taskList = query.listPage((pageNum - 1) * pageSize, pageSize);
        List<TaskInfo> taskInfos = CollUtil.newArrayList();
        for (Task task : taskList) {
            taskInfos.add(queryTaskInfo(task.getId()));
        }

        return PageableUtils.basicQuery(pageNum, pageSize, query.count(), taskInfos);
    }

    @Override
    public String findVariableByTaskId(String taskId, String variableName) {
        return Convert.toStr(taskService.getVariable(taskId, variableName));
    }

    @Override
    public List<Task> processInstanceId4Multi(String processInstanceId) {
        List<Task> resultList = CollectionUtil.newArrayList();
        TaskQuery taskQuery = createTaskQuery().processInstanceId(processInstanceId).active();
        if (taskQuery.count() > 1) {
            resultList.addAll(taskQuery.list());
        } else {
            resultList.add(taskQuery.singleResult());
        }
        return resultList;
    }

    @Override
    public String findBusinessKeyByTaskId(String taskId) {
        Task task = this.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance pi = flowableProcessInstanceService.queryProcessInstanceById(task.getProcessInstanceId());
        if (ObjectUtil.isNotNull(pi)) {
            return pi.getBusinessKey();
        }
        return null;
    }

    @Override
    public Task queryTask(String taskId) {
        return createTaskQuery().taskId(taskId).singleResult();
    }

    @Override
    public Task queryTaskByProcessInstanceId(String processInstanceId) {
        return createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
    }

    @Override
    public Task queryTaskByProcessInstanceId(String processInstanceId, String assignee) {
        return createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee(assignee).singleResult();
    }

    @Override
    public Task queryTaskByAssigneeOrCandidate(String processInstanceId, String assignee, Set<String> candidateGroups) {

        TaskQuery taskQuery = createTaskQuery();
        taskQuery.processInstanceId(processInstanceId);

        if (CollectionUtil.isNotEmpty(candidateGroups) && StrUtil.isNotBlank(assignee)) {
            taskQuery.or().taskInvolvedGroups(candidateGroups).taskInvolvedUser(assignee).endOr();
        }else if (StrUtil.isNotBlank(assignee) && CollectionUtil.isEmpty(candidateGroups)) {
            taskQuery.taskInvolvedUser(assignee);
        }else if (CollectionUtil.isNotEmpty(candidateGroups) && StrUtil.isBlank(assignee)) {
            taskQuery.taskInvolvedGroups(candidateGroups);
        }
        List<Task> tasks = taskQuery.list();
        return CollectionUtil.isNotEmpty(tasks) ? tasks.get(0) : null;
    }

    @Override
    public TaskInfo queryTodoTask(String processInstanceId, String assignee, Set<String> candidateGroups) {
        Task task = this.queryTaskByAssigneeOrCandidate(processInstanceId, assignee, candidateGroups);
        return getTaskInfo(task);
    }

    @Override
    public List<Task> queryTodoTask(String assignee, Set<String> candidateGroups) {
        TaskQuery taskQuery = createTaskQuery();
        if (CollectionUtil.isNotEmpty(candidateGroups) && StrUtil.isNotBlank(assignee)) {
            taskQuery.or().taskInvolvedGroups(candidateGroups).taskInvolvedUser(assignee).endOr();
        }else if (StrUtil.isNotBlank(assignee) && CollectionUtil.isEmpty(candidateGroups)) {
            taskQuery.taskInvolvedUser(assignee);
        }else if (CollectionUtil.isNotEmpty(candidateGroups) && StrUtil.isBlank(assignee)) {
            taskQuery.taskInvolvedGroups(candidateGroups);
        }
        return taskQuery.list();
    }

    @Override
    public Task queryTaskByCandidateUser(String processInstanceId, String candidateUser) {
        return createTaskQuery()
                .taskCandidateUser(candidateUser)
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    @Override
    public Task queryTaskByCandidateGroup(String processInstanceId, Set<String> candidateGroups) {
        return createTaskQuery()
                .taskCandidateGroupIn(candidateGroups)
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    @Override
    public List<Task> queryTasksByProcessInstanceId(String processInstanceId) {
        return createTaskQuery().processInstanceId(processInstanceId).list();
    }

    @Override
    public PageInfo<TaskInfo> queryTaskCandidateUserByCondition(String candidateUser, Map<String, Object> variables, Integer pageNum, Integer pageSize) {
        TaskQuery taskQuery = buildTaskQueryByVariables(variables).taskCandidateUser(candidateUser)
                .orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc();
        return this.queryTask(taskQuery, pageNum, pageSize);
    }

    @Override
    public PageInfo<TaskInfo> queryTaskAssigneeByCondition(String assignee, Map<String, Object> variables, Integer pageNum, Integer pageSize) {
        TaskQuery taskQuery = buildTaskQueryByVariables(variables).taskAssignee(assignee)
                .orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc();
        return this.queryTask(taskQuery, pageNum, pageSize);
    }

    @Override
    public PageInfo<TaskInfo> queryTaskCandidateOrAssignedByCondition(String userId, Map<String, Object> variables, Integer pageNum, Integer pageSize) {
        TaskQuery taskQuery = buildTaskQueryByVariables(variables).taskCandidateOrAssigned(userId)
                .orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc();
        return this.queryTask(taskQuery, pageNum, pageSize);
    }

    @Override
    public long countTaskCandidateUser(String candidateUser) {
        return createTaskQuery().taskCandidateUser(candidateUser)
                .orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc()
                .count();
    }

    @Override
    public long countTaskAssignee(String assignee) {
        return createTaskQuery().taskAssignee(assignee).orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc()
                .count();
    }

    @Override
    public long countTaskCandidateOrAssigned(String userId) {
        return createTaskQuery().taskCandidateOrAssigned(userId).orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc()
                .count();
    }

    @Override
    public long countTaskCandidateUserByCondition(String candidateUser, Map<String, Object> variables) {
        return buildTaskQueryByVariables(variables).taskCandidateUser(candidateUser)
                .orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc()
                .count();
    }

    @Override
    public long countTaskAssigneeByCondition(String assignee, Map<String, Object> variables) {
        return buildTaskQueryByVariables(variables).taskAssignee(assignee).orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc()
                .count();
    }

    @Override
    public long countTaskCandidateOrAssignedByCondition(String userId, Map<String, Object> variables) {
        return createTaskQuery().taskCandidateOrAssigned(userId).orderByTaskPriority().desc()
                .orderByTaskCreateTime().asc()
                .count();
    }

    @Override
    public void setVariableLocal(String taskId, String variableName, Object value) {
        taskService.setVariableLocal(taskId, variableName, value);
    }

    @Override
    public void setVariablesLocal(String taskId, Map<String, Object> variables) {
        taskService.setVariablesLocal(taskId, variables);
    }

    @Override
    public void setVariable(String taskId, String variableName, Object value) {
        taskService.setVariable(taskId, variableName, value);
    }

    @Override
    public void setVariables(String taskId, Map<String, Object> variables) {
        taskService.setVariables(taskId, variables);
    }

    @Override
    public void claim(String taskId, String assignee) {
        taskService.claim(taskId, assignee);
    }

    @Override
    public void unClaim(String taskId) {
        taskService.unclaim(taskId);
    }

    @Override
    public void complete(String taskId) {
        complete(taskId, null);
    }

    @Override
    public void complete(String taskId, String description, Map<String, Object> variables) {

        TaskEntity taskEntity = (TaskEntity) queryTask(taskId);
        Assert.notNull(taskEntity, "任务不存在, 请检查");

        this.addComment(taskId, taskEntity.getProcessInstanceId(), description);

        //委派处理
        if (DelegationState.PENDING.equals(taskEntity.getDelegationState())) {
            //生成历史记录
            TaskEntity task = this.createSubTask(taskEntity, taskEntity.getAssignee());
            taskService.complete(task.getId());
            taskId = task.getId();
            //执行委派
            resolveTask(taskId, variables);
        } else {
            //修改执行人 其实我这里就相当于签收了
            assignTask(taskId, taskEntity.getAssignee());
            //执行任务
            taskService.complete(taskId, variables);
            //处理加签父任务
            String parentTaskId = taskEntity.getParentTaskId();
            if (StrUtil.isNotBlank(parentTaskId)) {
                String tableName = managementService.getTableName(TaskEntity.class);
                String sql = "select count(1) from " + tableName + " where PARENT_TASK_ID_=#{parentTaskId}";
                long subTaskCount = taskService.createNativeTaskQuery().sql(sql).parameter("parentTaskId", parentTaskId).count();
                if (subTaskCount == 0) {
                    Task task = queryTask(parentTaskId);
                    //处理前后加签的任务
                    resolveTask(parentTaskId);
                    if ("after".equals(task.getScopeType())) {
                        taskService.complete(parentTaskId);
                    }
                }
            }
        }
    }

    @Override
    public void complete(String taskId, String description) {
        complete(taskId, description, MapUtil.empty());
    }

    @Override
    public void complete(String processInstanceId, String assignee, String description) {
        complete(processInstanceId, assignee, description, null);
    }

    @Override
    public void complete(String processInstanceId, String assignee, String description, Map<String, Object> variables) {
        HistoricProcessInstance historicProcessInstance = flowableHistoricService.queryHistoricProcessInstance(processInstanceId);
        Assert.notNull(historicProcessInstance, "流程不存在, 请检查");
        Task task = queryTaskByProcessInstanceId(processInstanceId, assignee);
        Assert.notNull(task, String.format("分配人 [%s] 没有待处理任务", assignee));
        complete(task.getId(), description, variables);
    }

    @Override
    public void delegateTask(String taskId, String userId) {
        taskService.delegateTask(taskId, userId);
    }

    @Override
    public void resolveTask(String taskId) {
        resolveTask(taskId, null);
    }

    @Override
    public void resolveTask(String taskId, Map<String, Object> variables) {
        taskService.resolveTask(taskId, variables);
    }

    @Override
    public void assignTask(String taskId, String userId) {
        taskService.setAssignee(taskId, userId);
    }

    @Override
    public void setOwner(String taskId, String userId) {
        taskService.setOwner(taskId, userId);
    }

    @Override
    public void setDueDate(String taskId, Date date) {
        taskService.setDueDate(taskId, date);
    }

    @Override
    public void deleteTask(String taskId) {
        taskService.deleteTask(taskId);
    }

    @Override
    public void deleteTask(String taskId, String reason) {
        taskService.deleteTask(taskId, reason);
    }

    @Override
    public void addCandidateUser(String taskId, String userId) {
        taskService.addCandidateUser(taskId, userId);
    }

    @Override
    public Comment addComment(String taskId, String processInstanceId, String message) {
        return taskService.addComment(taskId, processInstanceId, message);
    }

    @Override
    public List<Comment> queryTaskComments(String taskId) {
        return taskService.getTaskComments(taskId);
    }

    @Override
    public List<Comment> queryTaskComments(String taskId, String type) {
        return taskService.getTaskComments(taskId, type);
    }

    @Override
    public Boolean isSuspendedTask(String taskId) {
        return createTaskQuery().taskId(taskId).singleResult().isSuspended();
    }

    @Override
    public void withdraw(String processInstanceId, String currentActivityId, String newActivityId) {
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstanceId)
                .moveActivityIdTo(currentActivityId, newActivityId)
                .changeState();
    }

    @Override
    public void taskReject(String taskId, String description) {
        Assert.isFalse(isSuspendedTask(taskId), "任务处于挂起状态");

        // 当前任务 task
        Task task = queryTask(taskId);
        // 获取流程定义信息
        ProcessDefinition processDefinition = flowableProcessService.queryProcessDefinition(task.getProcessDefinitionId());
        // 获取所有节点信息
        Process process = repositoryService.getBpmnModel(processDefinition.getId()).getProcesses().get(0);
        // 获取全部节点列表，包含子节点
        List<FlowElement> allElements = getAllElements(CollectionUtil.newArrayList(process.getFlowElements()), null);
        // 获取当前任务节点元素
        FlowElement source = null;
        if (CollectionUtil.isNotEmpty(allElements)) {
            for (FlowElement flowElement : allElements) {
                // 类型为用户节点
                if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                    // 获取节点信息
                    source = flowElement;
                }
            }
        }

        // 目的获取所有跳转到的节点 targetIds
        // 获取当前节点的所有父级用户任务节点
        // 深度优先算法思想：延边迭代深入
        List<UserTask> parentUserTaskList = getParentUserTasks(source, null, null);
        if (parentUserTaskList == null || parentUserTaskList.size() == 0) {
            throw new FlowableException("当前节点为初始任务节点，不能驳回");
        }
        // 获取活动 ID 即节点 Key
        List<String> parentUserTaskKeyList = new ArrayList<>();
        parentUserTaskList.forEach(item -> parentUserTaskKeyList.add(item.getId()));
        // 获取全部历史节点活动实例，即已经走过的节点历史，数据采用开始时间升序
        List<HistoricTaskInstance> historicTaskInstanceList = historyService.createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId()).orderByHistoricTaskInstanceStartTime().asc().list();
        // 数据清洗，将回滚导致的脏数据清洗掉
        List<String> lastHistoricTaskInstanceList = historicTaskInstanceClean(allElements, historicTaskInstanceList);
        // 此时历史任务实例为倒序，获取最后走的节点
        List<String> targetIds = new ArrayList<>();
        // 循环结束标识，遇到当前目标节点的次数
        int number = 0;
        StringBuilder parentHistoricTaskKey = new StringBuilder();
        for (String historicTaskInstanceKey : lastHistoricTaskInstanceList) {
            // 当会签时候会出现特殊的，连续都是同一个节点历史数据的情况，这种时候跳过
            if (parentHistoricTaskKey.toString().equals(historicTaskInstanceKey)) {
                continue;
            }
            parentHistoricTaskKey = new StringBuilder(historicTaskInstanceKey);
            if (historicTaskInstanceKey.equals(task.getTaskDefinitionKey())) {
                number++;
            }
            // 在数据清洗后，历史节点就是唯一一条从起始到当前节点的历史记录，理论上每个点只会出现一次
            // 在流程中如果出现循环，那么每次循环中间的点也只会出现一次，再出现就是下次循环
            // number == 1，第一次遇到当前节点
            // number == 2，第二次遇到，代表最后一次的循环范围
            if (number == 2) {
                break;
            }
            // 如果当前历史节点，属于父级的节点，说明最后一次经过了这个点，需要退回这个点
            if (parentUserTaskKeyList.contains(historicTaskInstanceKey)) {
                targetIds.add(historicTaskInstanceKey);
            }
        }


        // 目的获取所有需要被跳转的节点 currentIds
        // 取其中一个父级任务，因为后续要么存在公共网关，要么就是串行公共线路
        UserTask oneUserTask = parentUserTaskList.get(0);
        // 获取所有正常进行的任务节点 Key，这些任务不能直接使用，需要找出其中需要撤回的任务
        List<Task> runTaskList = createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = new ArrayList<>();
        runTaskList.forEach(item -> runTaskKeyList.add(item.getTaskDefinitionKey()));
        // 需驳回任务列表
        List<String> currentIds = new ArrayList<>();
        // 通过父级网关的出口连线，结合 runTaskList 比对，获取需要撤回的任务
        List<UserTask> currentUserTaskList = getChildUserTasks(oneUserTask, runTaskKeyList, null, null);
        currentUserTaskList.forEach(item -> currentIds.add(item.getId()));

        // 规定：并行网关之前节点必须需存在唯一用户任务节点，如果出现多个任务节点，则并行网关节点默认为结束节点，原因为不考虑多对多情况
        if (targetIds.size() > 1 && currentIds.size() > 1) {
            throw new FlowableException("任务出现多对多情况，无法撤回");
        }

        // 循环获取那些需要被撤回的节点的ID，用来设置驳回原因
        List<String> currentTaskIds = new ArrayList<>();
        currentIds.forEach(currentId -> runTaskList.forEach(runTask -> {
            if (currentId.equals(runTask.getTaskDefinitionKey())) {
                currentTaskIds.add(runTask.getId());
            }
        }));
        // 设置驳回意见
        currentTaskIds.forEach(item -> taskService.addComment(item, task.getProcessInstanceId(), CommentTypeEnum.BH.getName(), description));

        try {
            // 如果父级任务多于 1 个，说明当前节点不是并行节点，原因为不考虑多对多情况
            if (targetIds.size() > 1) {
                // 1 对 多任务跳转，currentIds 当前节点(1)，targetIds 跳转到的节点(多)
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(task.getProcessInstanceId()).
                        moveSingleActivityIdToActivityIds(currentIds.get(0), targetIds).changeState();
            }
            // 如果父级任务只有一个，因此当前任务可能为网关中的任务
            if (targetIds.size() == 1) {
                // 1 对 1 或 多 对 1 情况，currentIds 当前要跳转的节点列表(1或多)，targetIds.get(0) 跳转到的节点(1)
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(task.getProcessInstanceId())
                        .moveActivityIdsToSingleActivityId(currentIds, targetIds.get(0)).changeState();
            }
        } catch (FlowableObjectNotFoundException e) {
            throw new FlowableException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            throw new FlowableException("无法取消或开始活动");
        }

    }

    @Override
    public void taskReject(String processInstanceId, String assignee, String description) {
        HistoricProcessInstance historicProcessInstance = flowableHistoricService.queryHistoricProcessInstance(processInstanceId);
        Assert.notNull(historicProcessInstance, "流程不存在, 请检查");
        Task task = createTaskQuery().processDefinitionId(historicProcessInstance.getProcessDefinitionId()).taskAssignee(assignee).singleResult();
        Assert.notNull(task, String.format("分配人 [%s] 没有待处理任务", assignee));
        this.taskReject(task.getId(), description);
    }

    @Override
    public void addMultiInstanceExecution(String activityDefId, String instanceId, Map<String, Object> variables) {
        managementService.executeCommand(new AddMultiInstanceExecutionCmd(activityDefId, instanceId, variables));
    }

    @Override
    public void deleteMultiInstanceExecution(String currentChildExecutionId, boolean flag) {
        managementService.executeCommand(new DeleteMultiInstanceExecutionCmd(currentChildExecutionId, flag));
    }

    @Override
    public void taskReturn(String taskId, String preTaskId, String description) {
        Assert.isFalse(isSuspendedTask(taskId), "任务处于挂起状态");

        HistoricTaskInstance historicTaskInstance = flowableHistoricService.queryHistoricTaskInstance(preTaskId);
        Assert.notNull(historicTaskInstance, "回退节点不存在");

        // 当前任务 task
        Task task = queryTask(taskId);
        // 获取流程定义信息
        ProcessDefinition processDefinition = flowableProcessService.queryProcessDefinition(task.getProcessDefinitionId());
        // 获取所有节点信息
        Process process = repositoryService.getBpmnModel(processDefinition.getId()).getProcesses().get(0);
        // 获取全部节点列表，包含子节点
        List<FlowElement> allElements = getAllElements((List<FlowElement>) process.getFlowElements(), null);
        // 获取当前任务节点元素
        FlowElement source = null;
        // 获取跳转的节点元素
        FlowElement target = null;
        if (allElements != null) {
            for (FlowElement flowElement : allElements) {
                // 当前任务节点元素
                if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                    source = flowElement;
                }
                // 跳转的节点元素
                if (flowElement.getId().equals(historicTaskInstance.getTaskDefinitionKey())) {
                    target = flowElement;
                }
            }
        }

        // 从当前节点向前扫描
        // 如果存在路线上不存在目标节点，说明目标节点是在网关上或非同一路线上，不可跳转
        // 否则目标节点相对于当前节点，属于串行
        Boolean isSequential = checkSequentialReferTarget(source, historicTaskInstance.getTaskDefinitionKey(), null, null);
        if (!isSequential) {
            throw new FlowableException("当前节点相对于目标节点，不属于串行关系，无法回退");
        }


        // 获取所有正常进行的任务节点 Key，这些任务不能直接使用，需要找出其中需要撤回的任务
        List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runTaskKeyList = new ArrayList<>();
        runTaskList.forEach(item -> runTaskKeyList.add(item.getTaskDefinitionKey()));
        // 需退回任务列表
        List<String> currentIds = new ArrayList<>();
        // 通过父级网关的出口连线，结合 runTaskList 比对，获取需要撤回的任务
        List<UserTask> currentUserTaskList = getChildUserTasks(target, runTaskKeyList, null, null);
        currentUserTaskList.forEach(item -> currentIds.add(item.getId()));

        // 循环获取那些需要被撤回的节点的ID，用来设置驳回原因
        List<String> currentTaskIds = new ArrayList<>();
        currentIds.forEach(currentId -> runTaskList.forEach(runTask -> {
            if (currentId.equals(runTask.getTaskDefinitionKey())) {
                currentTaskIds.add(runTask.getId());
            }
        }));
        // 设置回退意见
        for (String currentTaskId : currentTaskIds) {
            taskService.addComment(currentTaskId, task.getProcessInstanceId(), CommentTypeEnum.TH.getName(), description);
        }

        try {
            // 1 对 1 或 多 对 1 情况，currentIds 当前要跳转的节点列表(1或多)，targetKey 跳转到的节点(1)
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(task.getProcessInstanceId())
                    .moveActivityIdsToSingleActivityId(currentIds, historicTaskInstance.getTaskDefinitionKey()).changeState();
        } catch (FlowableObjectNotFoundException e) {
            throw new FlowableException("未找到流程实例，流程可能已发生变化");
        } catch (FlowableException e) {
            throw new FlowableException("无法取消或开始活动");
        }
    }

    @Override
    public void taskReturn(String taskId, String description) {
        Task task = queryTask(taskId);
        Assert.notNull(task, "任务不存在, 请检查");

        // 取得所有历史任务按时间降序排序
        List<HistoricTaskInstance> htiList = flowableHistoricService.queryHistoricTaskOrderCreateTimeDesc(task.getProcessInstanceId());
        if (ObjectUtil.isEmpty(htiList) || htiList.size() < 2) {
            throw new FlowableException("流程未开始，或者处于开始节点不允许退回");
        }

        // list里的第二条代表上一个任务
        HistoricTaskInstance lastTask = htiList.get(1);
        Assert.notNull(lastTask, "上一个任务节点不存在");

        taskReturn(taskId, lastTask.getId(), description);
    }

    @Override
    public void endTask(String taskId, String description) {
        Task task = queryTask(taskId);
        Assert.notNull(task, "当前任务不存在, 请检查");
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        FlowNode endFlowNode = endEventList.get(0);
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        //  临时保存当前活动的原始方向
        List<SequenceFlow> originalSequenceFlowList = new ArrayList<>(currentFlowNode.getOutgoingFlows());
        //  清理活动方向
        currentFlowNode.getOutgoingFlows().clear();

        //  建立新方向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId(IdUtil.objectId());
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        List<SequenceFlow> newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);
        //任务批注
        addComment(taskId, task.getProcessInstanceId(), description);
        //  完成当前任务
        taskService.complete(task.getId());
        //  可以不用恢复原始方向，不影响其它的流程
        currentFlowNode.setOutgoingFlows(originalSequenceFlowList);
    }

    @Override
    public void cancelTask(String processInstanceId) {
        List<Task> task = queryTasksByProcessInstanceId(processInstanceId);
        if (CollectionUtil.isEmpty(task)) {
            throw new FlowableException("流程未启动或已执行完成，取消申请失败");
        }

        ProcessInstance processInstance = flowableProcessInstanceService.queryProcessInstanceById(processInstanceId);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        if (ObjectUtil.isNotNull(bpmnModel)) {
            Process process = bpmnModel.getMainProcess();
            List<EndEvent> endNodes = process.findFlowElementsOfType(EndEvent.class, false);
            if (CollectionUtil.isNotEmpty(endNodes)) {
                String endId = endNodes.get(0).getId();
                List<Execution> executions = runtimeService.createExecutionQuery().parentId(processInstance.getProcessInstanceId()).list();
                List<String> executionIds = new ArrayList<>();
                executions.forEach(execution -> executionIds.add(execution.getId()));
                runtimeService.createChangeActivityStateBuilder().moveExecutionsToSingleActivityId(executionIds, endId).changeState();
            }
        }
    }

    @Override
    public void jumpToStart(String taskId, String description) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Assert.notNull(task, "任务不存在, 请检查");
        String processInstanceId = task.getProcessInstanceId();

        //  获取所有历史任务（按创建时间升序）
        List<HistoricTaskInstance> hisTaskList = flowableHistoricService.queryHistoricTaskOrderCreateTimeAsc(processInstanceId);
        if (CollectionUtil.isEmpty(hisTaskList) || hisTaskList.size() < 2) {
            log.error("当前流程 【{}】 审批节点 【{}】正在初始节点无语退回", processInstanceId, task.getName());
            throw new FlowableException(String.format("当前流程 【%s】 审批节点【%s】正在初始节点无语退回", processInstanceId, task.getName()));
        }

        //  第一个任务
        HistoricTaskInstance startTask = hisTaskList.get(0);
        //  当前任务
        HistoricTaskInstance currentTask = hisTaskList.get(hisTaskList.size() - 1);

        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());

        //  获取第一个活动节点
        FlowNode startFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(startTask.getTaskDefinitionKey());
        //  获取当前活动节点
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentTask.getTaskDefinitionKey());

        //  临时保存当前活动的原始方向
        List<SequenceFlow> originalSequenceFlowList = new ArrayList<>(currentFlowNode.getOutgoingFlows());
        //  清理活动方向
        currentFlowNode.getOutgoingFlows().clear();

        //  建立新方向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(startFlowNode);
        List<SequenceFlow> newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //  完成当前任务
        taskService.addComment(task.getId(), task.getProcessInstanceId(), description);
        taskService.complete(task.getId());

        //  重新查询当前任务
        Task nextTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        if (ObjectUtil.isNotNull(nextTask)) {
            taskService.setAssignee(nextTask.getId(), startTask.getAssignee());
        }
        //  恢复原始方向
        currentFlowNode.setOutgoingFlows(originalSequenceFlowList);
    }

    @Override
    public void addTasksBefore(String processInstanceId, String assignee, Set<String> assignees, String description) {
        addTask(processInstanceId, assignee, assignees, description, Boolean.FALSE);
    }

    @Override
    public void addTasksAfter(String processInstanceId, String assignee, Set<String> assignees, String description) {
        addTask(processInstanceId, assignee, assignees, description, Boolean.TRUE);
    }

    @Override
    public void addTask(String processInstanceId, String assignee, Set<String> assignees, String description, Boolean flag) {
        TaskEntityImpl task = (TaskEntityImpl) createTaskQuery().processInstanceId(processInstanceId).taskAssignee(assignee).singleResult();
        Assert.notNull(task, String.format("分配人 [%s] 没有待处理任务", assignee));

        //如果是加签再加签
        String parentTaskId = task.getParentTaskId();
        if (StrUtil.isBlank(parentTaskId)) {
            task.setOwner(assignee);
            task.setAssignee(null);
            task.setCountEnabled(true);
            if (flag) {
                task.setScopeType("after");
            } else {
                task.setScopeType("before");
            }
            // 设置任务为空执行者
            taskService.saveTask(task);
        }
        //添加加签数据
        this.createSignSubTasks(assignee, assignees, task);
        //添加审批意见
        String type = flag ? CommentTypeEnum.HJQ.getName() : CommentTypeEnum.QJQ.getName();
        taskService.addComment(task.getId(), processInstanceId, type, description);
    }

    @Override
    public Map<String, List<ExtensionElement>> getSequenceFlowExtensionElement(String taskId) {
        Task task = this.queryTask(taskId);
        if (ObjectUtil.isNotNull(task)) {

            Map<String, List<ExtensionElement>> extensionElements = MapUtil.newHashMap();

            HistoricTaskInstance historicTaskInstance = flowableHistoricService.queryHistoricTaskInstance(task.getId());
            ProcessDefinition processDefinition =  flowableProcessService.queryProcessDefinition(historicTaskInstance.getProcessDefinitionId());
            Execution execution = flowableProcessInstanceService.getExecution(historicTaskInstance.getExecutionId());

            String activityId = execution.getActivityId();
            while (true) {
                //根据活动节点获取当前的组件信息
                FlowNode flowNode = getFlowNode(processDefinition.getId(), activityId);

                //获取该节点之后的流向
                List<SequenceFlow> sequenceFlowListOutGoing = flowNode.getOutgoingFlows();

                // 获取的下个节点不一定是userTask的任务节点，所以要判断是否是任务节点
                if (sequenceFlowListOutGoing.size() > 1) {
                    sequenceFlowListOutGoing.forEach(a -> extensionElements.putAll(a.getExtensionElements()));
                } else if (sequenceFlowListOutGoing.size() == 1) {
                    // 只有1条出线,直接取得下个节点
                    SequenceFlow sequenceFlow = sequenceFlowListOutGoing.get(0);
                    // 下个节点
                    FlowElement flowElement = sequenceFlow.getTargetFlowElement();
                    if (flowElement instanceof UserTask) {
                        return extensionElements;
                    } else if (flowElement instanceof ExclusiveGateway) {
                        // 下个节点为排它网关时
                        ExclusiveGateway exclusiveGateway = (ExclusiveGateway) flowElement;
                        List<SequenceFlow> outgoingFlows = exclusiveGateway.getOutgoingFlows();
                        outgoingFlows.forEach(a -> extensionElements.putAll(a.getExtensionElements()));
                        return extensionElements;
                    }

                } else {
                    // 没有出线，则表明是结束节点
                    return Collections.emptyMap();
                }
            }
        }
        return MapUtil.empty();
    }

    @Override
    public List<ExtensionElementInfo> getExtensionElement(String taskId) {
        Map<String, List<ExtensionElement>> extensionElements = getSequenceFlowExtensionElement(taskId);
        return FlowableUtils.getExtensionElement(extensionElements);
    }

    @Override
    public FlowElement getNextUserFlowElement(String taskId) {

        Task task = this.queryTask(taskId);

        HistoricTaskInstance historicTaskInstance = flowableHistoricService.queryHistoricTaskInstance(task.getId());
        ProcessDefinition processDefinition =  flowableProcessService.queryProcessDefinition(historicTaskInstance.getProcessDefinitionId());
        Execution execution = flowableProcessInstanceService.getExecution(historicTaskInstance.getExecutionId());

        String activityId = execution.getActivityId();
        UserTask userTask = null;
        while (true) {
            //根据活动节点获取当前的组件信息
            FlowNode flowNode = getFlowNode(processDefinition.getId(), activityId);
            //获取该节点之后的流向
            List<SequenceFlow> sequenceFlowListOutGoing = flowNode.getOutgoingFlows();

            // 获取的下个节点不一定是userTask的任务节点，所以要判断是否是任务节点
            if (sequenceFlowListOutGoing.size() > 1) {
                // 如果有1条以上的出线，表示有分支，需要判断分支的条件才能知道走哪个分支
                // 遍历节点的出线得到下个activityId
                activityId = getNextActivityId(execution.getId(), task.getProcessInstanceId(), sequenceFlowListOutGoing);
            } else if (sequenceFlowListOutGoing.size() == 1) {
                // 只有1条出线,直接取得下个节点
                SequenceFlow sequenceFlow = sequenceFlowListOutGoing.get(0);
                // 下个节点
                FlowElement flowElement = sequenceFlow.getTargetFlowElement();
                if (flowElement instanceof UserTask) {
                    // 下个节点为UserTask时
                    userTask = (UserTask) flowElement;
                    System.out.println("下个任务为:" + userTask.getName());
                    return userTask;
                } else if (flowElement instanceof ExclusiveGateway) {
                    // 下个节点为排它网关时
                    ExclusiveGateway exclusiveGateway = (ExclusiveGateway) flowElement;
                    List<SequenceFlow> outgoingFlows = exclusiveGateway.getOutgoingFlows();
                    // 遍历网关的出线得到下个activityId
                    activityId = getNextActivityId(execution.getId(), task.getProcessInstanceId(), outgoingFlows);
                }
            } else {
                // 没有出线，则表明是结束节点
                return null;
            }
        }
    }

    @Override
    public Map<FlowElement, SequenceFlow> getNextUserFlows(String taskId) {
        Task task = this.queryTask(taskId);
        return getNextUserFlows(task);
    }

    @Override
    public Map<FlowElement, SequenceFlow> getNextUserFlows(Task task) {
        if (ObjectUtil.isNotNull(task)) {
            Map<FlowElement, SequenceFlow> nextUserFlows = MapUtil.newHashMap();

            HistoricTaskInstance historicTaskInstance = flowableHistoricService.queryHistoricTaskInstance(task.getId());
            ProcessDefinition processDefinition =  flowableProcessService.queryProcessDefinition(historicTaskInstance.getProcessDefinitionId());
            Execution execution = flowableProcessInstanceService.getExecution(historicTaskInstance.getExecutionId());

            String activityId = execution.getActivityId();
            while (true) {
                //根据活动节点获取当前的组件信息
                FlowNode flowNode = getFlowNode(processDefinition.getId(), activityId);
                //获取该节点之后的流向
                List<SequenceFlow> sequenceFlowListOutGoing = flowNode.getOutgoingFlows();

                // 获取的下个节点不一定是userTask的任务节点，所以要判断是否是任务节点
                if (sequenceFlowListOutGoing.size() > 1) {
                    sequenceFlowListOutGoing.forEach(a -> nextUserFlows.put(a.getTargetFlowElement(), a));
                    return nextUserFlows;
                } else if (sequenceFlowListOutGoing.size() == 1) {
                    SequenceFlow sequenceFlow = sequenceFlowListOutGoing.get(0);
                    FlowElement flowElement = sequenceFlow.getTargetFlowElement();
                    if (flowElement instanceof org.flowable.bpmn.model.Task) {
                        nextUserFlows.put(flowElement, sequenceFlow);
                        return nextUserFlows;
                    } else if (flowElement instanceof Gateway) {
                        Gateway gateway = (Gateway) flowElement;
                        List<SequenceFlow> outgoingFlows = gateway.getOutgoingFlows();
                        outgoingFlows.forEach(a -> nextUserFlows.put(a.getTargetFlowElement(), a));
                        return nextUserFlows;
                    }else {
                        return nextUserFlows;
                    }
                } else {
                    return MapUtil.empty();
                }
            }
        }
        return MapUtil.empty();
    }

    @Override
    public List<ActivityInfo> getActivityInfo(String processInstanceId) {
        ProcessInstance processInstance = flowableProcessInstanceService.queryProcessInstanceById(processInstanceId);
        if (ObjectUtil.isNotNull(processInstance)) {
            List<ActivityInfo> activityInfos = CollectionUtil.newArrayList();
            BpmnModel bpmnModel = flowableProcessService.getBpmnModel(processInstance.getProcessDefinitionId());
            Map<String, HistoricTaskInstance> historicTaskInstanceMap = Collections.emptyMap();
            List<HistoricTaskInstance> historicTaskInstances = flowableHistoricService.queryHistoricTaskOrderCreateTimeAsc(processInstance.getId());
            if (CollectionUtil.isNotEmpty(historicTaskInstances)) {
                historicTaskInstanceMap = historicTaskInstances.stream().collect(Collectors.toMap(HistoricTaskInstance::getTaskDefinitionKey, a -> a, (k1, k2) -> k1));
            }

            Map<UserTask, GraphicInfo> userTaskGraphicMap = MapUtil.newHashMap(Boolean.TRUE);

            bpmnModel.getProcesses().forEach(process -> {
                List<UserTask> userTasks = process.findFlowElementsOfType(UserTask.class);
                if (CollectionUtil.isNotEmpty(userTasks)) {
                    userTasks.forEach(a -> userTaskGraphicMap.put(a, bpmnModel.getGraphicInfo(a.getId())));
                }
            });
            
            for (Map.Entry<UserTask, GraphicInfo> entry : userTaskGraphicMap.entrySet()) {
                UserTask key = entry.getKey();
                HistoricTaskInstance historicTaskInstance = historicTaskInstanceMap.get(key.getId());

                ActivityInfo activityInfo = new ActivityInfo();
                BeanUtil.copyProperties(entry.getValue(), activityInfo);

                activityInfo.setId(key.getId());
                activityInfo.setName(key.getName());
                activityInfo.setCandidateGroups(key.getCandidateGroups());
                activityInfo.setCandidateUsers(key.getCandidateUsers());
                activityInfo.setProcessInstanceId(processInstance.getId());
                activityInfo.setProcessDefinitionId(processInstance.getProcessDefinitionId());
                if (ObjectUtil.isNotNull(historicTaskInstance)) {
                    Optional.ofNullable(historicTaskInstance.getCreateTime()).ifPresent(a -> activityInfo.setStartTime(a.getTime()));
                    Optional.ofNullable(historicTaskInstance.getEndTime()).ifPresent(a -> activityInfo.setEndTime(a.getTime()));
                    Optional.ofNullable(historicTaskInstance.getClaimTime()).ifPresent(a -> activityInfo.setClaimTime(a.getTime()));
                    Optional.ofNullable(historicTaskInstance.getDueDate()).ifPresent(a -> activityInfo.setDueDate(a.getTime()));
                    activityInfo.setAssignee(historicTaskInstance.getAssignee());
                    activityInfo.setDuration(historicTaskInstance.getDurationInMillis());
                    List<Comment> comments = queryTaskComments(historicTaskInstance.getId(), CommentEntity.TYPE_COMMENT);
                    if (CollectionUtil.isNotEmpty(comments)) {
                        comments.stream().filter(a -> StrUtil.isNotBlank(a.getFullMessage())).findAny().ifPresent(b -> activityInfo.setDescription(b.getFullMessage()));
                    }
                }
                activityInfos.add(activityInfo);
            }
            return activityInfos;
        }
        return Collections.emptyList();
    }

    @Override
    public Boolean isNextFlowNodeEndEvent(String processInstanceId, String assignee, Set<String> candidateGroups) {
        Task task = this.queryTaskByAssigneeOrCandidate(processInstanceId, assignee, candidateGroups);
        Assert.notNull(task, String.format("流程实例 【%s】 办理人/候选人 【%s】 或者 候选人组【%s】没有待执行任务, 请检查", processInstanceId, assignee, JSON.toJSONString(candidateGroups)));

        ExecutionEntity ee = (ExecutionEntity) createExecutionQuery().executionId(task.getExecutionId()).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(ee.getActivityId());
        List<SequenceFlow> outFlows = flowNode.getOutgoingFlows();

        boolean flag = Boolean.FALSE;
        int index = 0;
        while (true) {
            if (CollectionUtil.isEmpty(outFlows)) break;
            for (SequenceFlow sequenceFlow : outFlows) {
                FlowElement targetFlow = sequenceFlow.getTargetFlowElement();
                if (targetFlow instanceof Gateway) {
                    Gateway gateway = (Gateway) targetFlow;
                    outFlows = gateway.getOutgoingFlows();
                } else {
                    if (targetFlow instanceof org.flowable.bpmn.model.Task) {
                        outFlows = ((org.flowable.bpmn.model.Task) targetFlow).getOutgoingFlows();
                        index++;
                    }
                    if (targetFlow instanceof EndEvent) {
                        outFlows = null;
                        flag = Boolean.TRUE;
                        break;
                    }
                }
            }
        }
        return index <= 0 && flag;
    }

    @Override
    public void claimTask(String processInstanceId, String assignee, Set<String> candidateGroups) {
        Task task = queryTaskByAssigneeOrCandidate(processInstanceId, null, candidateGroups);
        Assert.notNull(task, String.format(" [%s] 没有可领取任务", assignee));
        this.claim(task.getId(), assignee);
    }

    /**
     * 根据活动节点和流程定义ID获取该活动节点的组件信息
     */
    private FlowNode getFlowNode(String processDefinitionId, String flowElementId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        return (FlowNode) bpmnModel.getMainProcess().getFlowElement(flowElementId);
    }

    /**
     * 根据el表达式取得满足条件的下一个activityId
     *
     * @param executionId       执行ID
     * @param processInstanceId 过程实例ID
     * @param outgoingFlows     即将离任流
     * @return {@link String}
     */
    private String getNextActivityId(String executionId, String processInstanceId, List<SequenceFlow> outgoingFlows) {
        String activityId = null;
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            // 取得线上的条件
            String conditionExpression = outgoingFlow.getConditionExpression();

            // 取得所有变量
            Map<String, Object> variables = runtimeService.getVariables(executionId);

            String variableName = "";
            // 判断网关条件里是否包含变量名
            for (String s : variables.keySet()) {
                if (conditionExpression.contains(s)) {
                    // 找到网关条件里的变量名
                    variableName = s;
                }
            }
            String conditionVal = getVariableValue(variableName, processInstanceId);
            // 判断el表达式是否成立
            if (isCondition(variableName, conditionExpression, conditionVal)) {
                // 取得目标节点
                FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
                activityId = targetFlowElement.getId();
            }
        }
        return activityId;
    }

    /**
     * 根据key和value判断el表达式是否通过
     *
     * @param key   el表达式key
     * @param el    el表达式
     * @param value el表达式传入值
     * @return {@link Boolean}
     */
    private Boolean isCondition(String key, String el, String value) {
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        context.setVariable(key, factory.createValueExpression(value, String.class));
        ValueExpression e = factory.createValueExpression(context, el, Boolean.class);
        return (Boolean) e.getValue(context);
    }

    /**
     * 取得流程变量的值
     *
     * @param variableName      变量名
     * @param processInstanceId 流程实例Id
     * @return {@link String}
     */
    private String getVariableValue(String variableName, String processInstanceId) {
        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list().get(0);
        Object object = runtimeService.getVariable(execution.getId(), variableName);
        return object == null ? "" : object.toString();
    }

    /**
     * 创建加签子任务
     * @param assignees 被加签人
     * @param assignee 加签人
     * @param taskEntity 父任务
     */
    private void createSignSubTasks(String assignee, Set<String> assignees, TaskEntity taskEntity) {
        if (CollectionUtil.isNotEmpty(assignees)) {
            String parentTaskId = taskEntity.getParentTaskId();
            if (StrUtil.isBlank(parentTaskId)) {
                parentTaskId = taskEntity.getId();
            }
            String finalParentTaskId = parentTaskId;
            //1.创建被加签人的任务列表
            assignees.forEach(userCode -> {
                if (StrUtil.isNotBlank(userCode)) {
                    this.createSubTask(taskEntity, finalParentTaskId, userCode);
                }
            });
            String taskId = taskEntity.getId();
            if (StrUtil.isBlank(taskEntity.getParentTaskId())) {
                //2.创建加签人的任务并执行完毕
                Task task = this.createSubTask(taskEntity, finalParentTaskId, assignee);
                taskId = task.getId();
            }
            Task taskInfo = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (ObjectUtil.isNotNull(taskInfo)) {
                taskService.complete(taskId);
            }
            //如果是候选人，需要删除运行时候选表种的数据。
            long candidateCount = taskService.createTaskQuery().taskId(parentTaskId).taskCandidateUser(assignee).count();
            if (candidateCount > 0) {
                taskService.deleteCandidateUser(parentTaskId, assignee);
            }
        }
    }

    /**
     * 创建子任务
     *
     * @param ptask    创建子任务
     * @param assignee 子任务的执行人
     * @return 子任务
     */
    private TaskEntity createSubTask(TaskEntity ptask, String ptaskId, String assignee) {
        TaskEntity task = null;
        if (ObjectUtil.isNotEmpty(ptask)) {
            //1.生成子任务
            task = (TaskEntity) taskService.newTask(IdUtil.fastUUID());
            task.setCategory(ptask.getCategory());
            task.setDescription(ptask.getDescription());
            task.setTenantId(ptask.getTenantId());
            task.setAssignee(assignee);
            task.setName(ptask.getName());
            task.setParentTaskId(ptaskId);
            task.setProcessDefinitionId(ptask.getProcessDefinitionId());
            task.setProcessInstanceId(ptask.getProcessInstanceId());
            task.setTaskDefinitionKey(ptask.getTaskDefinitionKey());
            task.setTaskDefinitionId(ptask.getTaskDefinitionId());
            task.setPriority(ptask.getPriority());
            task.setCreateTime(new Date());
            taskService.saveTask(task);
        }
        return task;
    }

    /**
     * 迭代从后向前扫描，判断目标节点相对于当前节点是否是串行
     * 不存在直接回退到子流程中的情况，但存在从子流程出去到父流程情况
     *
     * @param source          起始节点
     * @param isSequential    是否串行
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param targetKsy       目标节点
     * @return {@link Boolean}
     */
    private Boolean checkSequentialReferTarget(FlowElement source, String targetKsy, Set<String> hasSequenceFlow, Boolean isSequential) {
        isSequential = ObjectUtil.isEmpty(isSequential) || isSequential;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;

        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && ObjectUtil.isNotEmpty(source.getSubProcess())) {
            isSequential = checkSequentialReferTarget(source.getSubProcess(), targetKsy, hasSequenceFlow, isSequential);
        }

        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);

        if (CollectionUtil.isNotEmpty(sequenceFlows)) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) continue;

                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());

                // 如果目标节点已被判断为并行，后面都不需要执行，直接返回
                if (!isSequential)  break;

                // 已找到线路存在目标节点 跳出循环
                if (targetKsy.equals(sequenceFlow.getSourceFlowElement().getId())) break;

                if (sequenceFlow.getSourceFlowElement() instanceof StartEvent) {
                    isSequential = false;
                    break;
                }
                // 否则就继续迭代
                isSequential = checkSequentialReferTarget(sequenceFlow.getSourceFlowElement(), targetKsy, hasSequenceFlow, isSequential);
            }
        }
        return isSequential;
    }

    /**
     * 历史节点数据清洗，清洗掉又回滚导致的脏数据
     * @param allElements 全部节点信息
     * @param historicTaskInstanceList 历史任务实例信息，数据采用开始时间升序
     * @return 回滚导致的脏数据
     */
    private List<String> historicTaskInstanceClean(Collection<FlowElement> allElements, List<HistoricTaskInstance> historicTaskInstanceList) {
        // 会签节点收集
        List<String> multiTask = new ArrayList<>();
        allElements.forEach(flowElement -> {
            if (flowElement instanceof UserTask) {
                // 如果该节点的行为为会签行为，说明该节点为会签节点
                if (((UserTask) flowElement).getBehavior() instanceof ParallelMultiInstanceBehavior || ((UserTask) flowElement).getBehavior() instanceof SequentialMultiInstanceBehavior) {
                    multiTask.add(flowElement.getId());
                }
            }
        });
        // 循环放入栈，栈 LIFO：后进先出
        Stack<HistoricTaskInstance> stack = new Stack<>();
        historicTaskInstanceList.forEach(stack::push);
        // 清洗后的历史任务实例
        List<String> lastHistoricTaskInstanceList = new ArrayList<>();
        // 网关存在可能只走了部分分支情况，且还存在跳转废弃数据以及其他分支数据的干扰，因此需要对历史节点数据进行清洗
        // 临时用户任务 key
        StringBuilder userTaskKey = null;
        // 临时被删掉的任务 key，存在并行情况
        List<String> deleteKeyList = new ArrayList<>();
        // 临时脏数据线路
        List<Set<String>> dirtyDataLineList = new ArrayList<>();
        // 由某个点跳到会签点,此时出现多个会签实例对应 1 个跳转情况，需要把这些连续脏数据都找到
        // 会签特殊处理下标
        int multiIndex = -1;
        // 会签特殊处理 key
        StringBuilder multiKey = null;
        // 会签特殊处理操作标识
        boolean multiOpera = false;
        while (!stack.empty()) {
            // 从这里开始 userTaskKey 都还是上个栈的 key
            // 是否是脏数据线路上的点
            final boolean[] isDirtyData = {false};
            for (Set<String> oldDirtyDataLine : dirtyDataLineList) {
                if (oldDirtyDataLine.contains(stack.peek().getTaskDefinitionKey())) {
                    isDirtyData[0] = true;
                }
            }
            // 删除原因不为空，说明从这条数据开始回跳或者回退的
            // MI_END：会签完成后，其他未签到节点的删除原因，不在处理范围内
            if (stack.peek().getDeleteReason() != null && !stack.peek().getDeleteReason().equals("MI_END")) {
                // 可以理解为脏线路起点
                String dirtyPoint = "";
                if (stack.peek().getDeleteReason().contains("Change activity to ")) {
                    dirtyPoint = stack.peek().getDeleteReason().replace("Change activity to ", "");
                }
                // 会签回退删除原因有点不同
                if (stack.peek().getDeleteReason().contains("Change parent activity to ")) {
                    dirtyPoint = stack.peek().getDeleteReason().replace("Change parent activity to ", "");
                }
                FlowElement dirtyTask = null;
                // 获取变更节点的对应的入口处连线
                // 如果是网关并行回退情况，会变成两条脏数据路线，效果一样
                for (FlowElement flowElement : allElements) {
                    if (flowElement.getId().equals(stack.peek().getTaskDefinitionKey())) {
                        dirtyTask = flowElement;
                    }
                }
                // 获取脏数据线路
                Set<String> dirtyDataLine = getDirtyRoads(dirtyTask, null, null, Arrays.asList(dirtyPoint.split(",")), null);
                // 自己本身也是脏线路上的点，加进去
                dirtyDataLine.add(stack.peek().getTaskDefinitionKey());
                log.info(stack.peek().getTaskDefinitionKey() + "点脏路线集合：" + dirtyDataLine);
                // 是全新的需要添加的脏线路
                boolean isNewDirtyData = true;
                for (int i = 0; i < dirtyDataLineList.size(); i++) {
                    // 如果发现他的上个节点在脏线路内，说明这个点可能是并行的节点，或者连续驳回
                    // 这时，都以之前的脏线路节点为标准，只需合并脏线路即可，也就是路线补全
                    if (dirtyDataLineList.get(i).contains(userTaskKey.toString())) {
                        isNewDirtyData = false;
                        dirtyDataLineList.get(i).addAll(dirtyDataLine);
                    }
                }
                // 已确定时全新的脏线路
                if (isNewDirtyData) {
                    // deleteKey 单一路线驳回到并行，这种同时生成多个新实例记录情况，这时 deleteKey 其实是由多个值组成
                    // 按照逻辑，回退后立刻生成的实例记录就是回退的记录
                    // 至于驳回所生成的 Key，直接从删除原因中获取，因为存在驳回到并行的情况
                    deleteKeyList.add(dirtyPoint + ",");
                    dirtyDataLineList.add(dirtyDataLine);
                }
                // 添加后，现在这个点变成脏线路上的点了
                isDirtyData[0] = true;
            }
            // 如果不是脏线路上的点，说明是有效数据，添加历史实例 Key
            if (!isDirtyData[0]) {
                lastHistoricTaskInstanceList.add(stack.peek().getTaskDefinitionKey());
            }
            // 校验脏线路是否结束
            for (int i = 0; i < deleteKeyList.size(); i ++) {
                // 如果发现脏数据属于会签，记录下下标与对应 Key，以备后续比对，会签脏数据范畴开始
                if (multiKey == null && multiTask.contains(stack.peek().getTaskDefinitionKey())
                        && deleteKeyList.get(i).contains(stack.peek().getTaskDefinitionKey())) {
                    multiIndex = i;
                    multiKey = new StringBuilder(stack.peek().getTaskDefinitionKey());
                }
                // 会签脏数据处理，节点退回会签清空
                // 如果在会签脏数据范畴中发现 Key改变，说明会签脏数据在上个节点就结束了，可以把会签脏数据删掉
                if (multiKey != null && !multiKey.toString().equals(stack.peek().getTaskDefinitionKey())) {
                    deleteKeyList.set(multiIndex , deleteKeyList.get(multiIndex).replace(stack.peek().getTaskDefinitionKey() + ",", ""));
                    multiKey = null;
                    // 结束进行下校验删除
                    multiOpera = true;
                }
                // 其他脏数据处理
                // 发现该路线最后一条脏数据，说明这条脏数据线路处理完了，删除脏数据信息
                // 脏数据产生的新实例中是否包含这条数据
                if (multiKey == null && deleteKeyList.get(i).contains(stack.peek().getTaskDefinitionKey())) {
                    // 删除匹配到的部分
                    deleteKeyList.set(i , deleteKeyList.get(i).replace(stack.peek().getTaskDefinitionKey() + ",", ""));
                }
                // 如果每组中的元素都以匹配过，说明脏数据结束
                if ("".equals(deleteKeyList.get(i))) {
                    // 同时删除脏数据
                    deleteKeyList.remove(i);
                    dirtyDataLineList.remove(i);
                    break;
                }
            }
            // 会签数据处理需要在循环外处理，否则可能导致溢出
            // 会签的数据肯定是之前放进去的所以理论上不会溢出，但还是校验下
            if (multiOpera && deleteKeyList.size() > multiIndex && "".equals(deleteKeyList.get(multiIndex))) {
                // 同时删除脏数据
                deleteKeyList.remove(multiIndex);
                dirtyDataLineList.remove(multiIndex);
                multiIndex = -1;
                multiOpera = false;
            }
            // pop() 方法与 peek() 方法不同，在返回值的同时，会把值从栈中移除
            // 保存新的 userTaskKey 在下个循环中使用
            userTaskKey = new StringBuilder(stack.pop().getTaskDefinitionKey());
        }
        log.info("清洗后的历史节点数据：" + lastHistoricTaskInstanceList);
        return lastHistoricTaskInstanceList;
    }

    /**
     * 从后向前寻路，获取所有脏线路上的点
     * @param source 起始节点
     * @param passRoads 已经经过的点集合
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param targets 目标脏线路终点
     * @param dirtyRoads 确定为脏数据的点，因为不需要重复，因此使用 set 存储
     * @return  脏线路上的点
     */
    private Set<String> getDirtyRoads(FlowElement source, List<String> passRoads, Set<String> hasSequenceFlow, List<String> targets, Set<String> dirtyRoads) {
        passRoads = passRoads == null ? new ArrayList<>() : passRoads;
        dirtyRoads = dirtyRoads == null ? new HashSet<>() : dirtyRoads;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;

        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            dirtyRoads = getDirtyRoads(source.getSubProcess(), passRoads, hasSequenceFlow, targets, dirtyRoads);
        }

        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);

        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 新增经过的路线
                passRoads.add(sequenceFlow.getSourceFlowElement().getId());
                // 如果此点为目标点，确定经过的路线为脏线路，添加点到脏线路中，然后找下个连线
                if (targets.contains(sequenceFlow.getSourceFlowElement().getId())) {
                    dirtyRoads.addAll(passRoads);
                    continue;
                }
                // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
                if (sequenceFlow.getSourceFlowElement() instanceof SubProcess) {
                    dirtyRoads = getChildProcessAllDirtyRoad((StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, dirtyRoads);
                    // 是否存在子流程上，true 是，false 否
                    Boolean isInChildProcess = dirtyTargetInChildProcess((StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, targets, null);
                    if (isInChildProcess) {
                        // 已在子流程上找到，该路线结束
                        continue;
                    }
                }
                // 继续迭代
                dirtyRoads = getDirtyRoads(sequenceFlow.getSourceFlowElement(), passRoads, hasSequenceFlow, targets, dirtyRoads);
            }
        }
        return dirtyRoads;
    }

    /**
     * 迭代获取子流程脏路线
     * 说明，假如回退的点就是子流程，那么也肯定会回退到子流程最初的用户任务节点，因此子流程中的节点全是脏路线
     * @param source 起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param dirtyRoads 确定为脏数据的点，因为不需要重复，因此使用 set 存储
     * @return 子流程脏路线
     */
    private Set<String> getChildProcessAllDirtyRoad(FlowElement source, Set<String> hasSequenceFlow, Set<String> dirtyRoads) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        dirtyRoads = dirtyRoads == null ? new HashSet<>() : dirtyRoads;

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);

        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 添加脏路线
                dirtyRoads.add(sequenceFlow.getTargetFlowElement().getId());
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    dirtyRoads = getChildProcessAllDirtyRoad((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, dirtyRoads);
                }
                // 继续迭代
                dirtyRoads = getChildProcessAllDirtyRoad(sequenceFlow.getTargetFlowElement(), hasSequenceFlow, dirtyRoads);
            }
        }
        return dirtyRoads;
    }

    /**
     * 判断脏路线结束节点是否在子流程上
     * @param source 起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param targets 判断脏路线节点是否存在子流程上，只要存在一个，说明脏路线只到子流程为止
     * @param inChildProcess 是否存在子流程上，true 是，false 否
     * @return  结束节点是否在子流程上
     */
    private Boolean dirtyTargetInChildProcess(FlowElement source, Set<String> hasSequenceFlow, List<String> targets, Boolean inChildProcess) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        inChildProcess = inChildProcess != null && inChildProcess;

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);

        if (sequenceFlows != null && !inChildProcess) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果发现目标点在子流程上存在，说明只到子流程为止
                if (targets.contains(sequenceFlow.getTargetFlowElement().getId())) {
                    inChildProcess = true;
                    break;
                }
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    inChildProcess = dirtyTargetInChildProcess((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, targets, inChildProcess);
                }
                // 继续迭代
                inChildProcess = dirtyTargetInChildProcess(sequenceFlow.getTargetFlowElement(), hasSequenceFlow, targets, inChildProcess);
            }
        }
        return inChildProcess;
    }

    /**
     * 根据正在运行的任务节点，迭代获取子级任务节点列表，向后找
     * @param source 起始节点
     * @param runTaskKeyList 正在运行的任务 Key，用于校验任务节点是否是正在运行的节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param userTaskList 需要撤回的用户任务列表
     * @return  任务节点
     */
    private List<UserTask> getChildUserTasks(FlowElement source, List<String> runTaskKeyList, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;

        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof EndEvent && source.getSubProcess() != null) {
            userTaskList = getChildUserTasks(source.getSubProcess(), runTaskKeyList, hasSequenceFlow, userTaskList);
        }

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);

        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果为用户任务类型，且任务节点的 Key 正在运行的任务中存在，添加
                if (sequenceFlow.getTargetFlowElement() instanceof UserTask && runTaskKeyList.contains((sequenceFlow.getTargetFlowElement()).getId())) {
                    userTaskList.add((UserTask) sequenceFlow.getTargetFlowElement());
                    continue;
                }
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    List<UserTask> childUserTaskList = getChildUserTasks((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), runTaskKeyList, hasSequenceFlow, null);
                    // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                    if (childUserTaskList != null && childUserTaskList.size() > 0) {
                        userTaskList.addAll(childUserTaskList);
                        continue;
                    }
                }
                // 继续迭代
                userTaskList = getChildUserTasks(sequenceFlow.getTargetFlowElement(), runTaskKeyList, hasSequenceFlow, userTaskList);
            }
        }
        return userTaskList;
    }

    /**
     * 迭代获取父级任务节点列表，向前找
     * @param source 起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param userTaskList 已找到的用户任务节点
     * @return 父级任务信息
     */
    private List<UserTask> getParentUserTasks(FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;

        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && ObjectUtil.isNotNull(source.getSubProcess())) {
            userTaskList = getParentUserTasks(source.getSubProcess(), hasSequenceFlow, userTaskList);
        }

        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);

        if (CollectionUtil.isNotEmpty(sequenceFlows)) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 类型为用户节点，则新增父级节点
                if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
                    userTaskList.add((UserTask) sequenceFlow.getSourceFlowElement());
                    continue;
                }
                // 类型为子流程，则添加子流程开始节点出口处相连的节点
                if (sequenceFlow.getSourceFlowElement() instanceof SubProcess) {
                    // 获取子流程用户任务节点
                    List<UserTask> childUserTaskList = getChildProcessUserTasks((StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, null);
                    // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                    if (childUserTaskList != null && childUserTaskList.size() > 0) {
                        userTaskList.addAll(childUserTaskList);
                        continue;
                    }
                }
                // 继续迭代
                userTaskList = getParentUserTasks(sequenceFlow.getSourceFlowElement(), hasSequenceFlow, userTaskList);
            }
        }
        return userTaskList;
    }

    /**
     * 迭代获取子流程用户任务节点
     * @param source 起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param userTaskList 需要撤回的用户任务列表
     * @return 用户任务节点
     */
    private List<UserTask> getChildProcessUserTasks(FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);

        if (CollectionUtil.isNotEmpty(sequenceFlows)) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果为用户任务类型，且任务节点的 Key 正在运行的任务中存在，添加
                if (sequenceFlow.getTargetFlowElement() instanceof UserTask) {
                    userTaskList.add((UserTask) sequenceFlow.getTargetFlowElement());
                    continue;
                }
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    List<UserTask> childUserTaskList = getChildProcessUserTasks((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, null);
                    // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                    if (CollUtil.isNotEmpty(childUserTaskList) && childUserTaskList.size() > 0) {
                        userTaskList.addAll(childUserTaskList);
                        continue;
                    }
                }
                // 继续迭代
                userTaskList = getChildProcessUserTasks(sequenceFlow.getTargetFlowElement(), hasSequenceFlow, userTaskList);
            }
        }
        return userTaskList;
    }

    /**
     * 根据节点，获取出口连线
     * @param source 流程节点
     * @return 出口连线
     */
    private List<SequenceFlow> getElementOutgoingFlows(FlowElement source) {
        List<SequenceFlow> sequenceFlows;
        if (source instanceof Gateway) {
            sequenceFlows = ((Gateway) source).getOutgoingFlows();
        } else if (source instanceof SubProcess) {
            sequenceFlows = ((SubProcess) source).getOutgoingFlows();
        } else if (source instanceof StartEvent) {
            sequenceFlows = ((StartEvent) source).getOutgoingFlows();
        } else if (source instanceof EndEvent) {
            sequenceFlows = ((EndEvent) source).getOutgoingFlows();
        }else {
            sequenceFlows = ((FlowNode) source).getOutgoingFlows();
        }
        return sequenceFlows;
    }

    /**
     * 根据节点，获取入口连线
     * @param source 流程节点
     * @return 入口连线
     */
    private List<SequenceFlow> getElementIncomingFlows(FlowElement source) {
        List<SequenceFlow> sequenceFlows;
        if (source instanceof Gateway) {
            sequenceFlows = ((Gateway) source).getIncomingFlows();
        } else if (source instanceof SubProcess) {
            sequenceFlows = ((SubProcess) source).getIncomingFlows();
        } else if (source instanceof StartEvent) {
            sequenceFlows = ((StartEvent) source).getIncomingFlows();
        } else if (source instanceof EndEvent) {
            sequenceFlows = ((EndEvent) source).getIncomingFlows();
        }else {
            sequenceFlows = ((FlowNode) source).getIncomingFlows();
        }
        return sequenceFlows;
    }

    /**
     * 获取全部节点列表，包含子流程节点
     *
     * @param flowElements 流程节点
     * @param allElements  全部节点
     * @return {@link List}<{@link FlowElement}> 全部节点列表
     */
    private List<FlowElement> getAllElements(List<FlowElement> flowElements, List<FlowElement> allElements) {
        allElements = allElements == null ? new ArrayList<>() : allElements;

        for (FlowElement flowElement : flowElements) {
            allElements.add(flowElement);
            if (flowElement instanceof SubProcess) {
                // 继续深入子流程，进一步获取子流程
                allElements = getAllElements((List<FlowElement>) ((SubProcess) flowElement).getFlowElements(), allElements);
            }
        }
        return allElements;
    }

    /**
     * 构建任务查询根据变量
     *
     * @param args 参数
     * @return {@link TaskQuery} 查询对象
     */
    private TaskQuery buildTaskQueryByVariables(Map<String, Object> args) {
        TaskQuery tq = createTaskQuery();
        if (args != null && args.size() > 0) {
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                if (VariablesEnum.activityName.toString().equals(entry.getKey()) ||
                        VariablesEnum.orgName.toString().equals(entry.getKey())) {
                    tq.processVariableValueLike(entry.getKey(), String.valueOf(entry.getValue()));
                } else {
                    tq.processVariableValueEquals(entry.getKey(), entry.getValue());
                }
            }
        }

        return tq;
    }

    private TaskEntity createSubTask(TaskEntity ptask, String assignee) {
        return this.createSubTask(ptask, ptask.getId(), assignee);
    }

    /**
     * 获取任务信息
     *
     * @param task 任务
     * @return {@link TaskInfo}
     */
    private TaskInfo getTaskInfo(Task task) {
        if (ObjectUtil.isNull(task)) return null;
        ProcessInstance processInstance = flowableProcessInstanceService.queryProcessInstanceById(task.getProcessInstanceId());
        Deployment deployment = flowableProcessService.queryDeployment(processInstance.getDeploymentId());

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setDeploymentId(deployment.getId());
        taskInfo.setDeploymentKey(deployment.getKey());
        taskInfo.setDeploymentName(deployment.getName());
        taskInfo.setId(task.getId());
        taskInfo.setName(task.getName());
        taskInfo.setDescription(task.getDescription());
        taskInfo.setPriority(task.getPriority());
        taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
        taskInfo.setOwner(task.getOwner());
        taskInfo.setAssignee(task.getAssignee());
        Optional.ofNullable(task.getCreateTime()).ifPresent(a -> taskInfo.setCreateTime(a.getTime()));
        Optional.ofNullable(task.getDueDate()).ifPresent(a -> taskInfo.setDueDate(a.getTime()));
        taskInfo.setCategory(task.getCategory());
        taskInfo.setProcessVariables(task.getProcessVariables());
        Optional.ofNullable(task.getClaimTime()).ifPresent(a -> taskInfo.setClaimTime(a.getTime()));
        taskInfo.setProcessInstanceName(processInstance.getName());
        taskInfo.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
        taskInfo.setProcessInstanceBusinessKey(processInstance.getBusinessKey());
        taskInfo.setProcessInstanceIsSuspended(processInstance.isSuspended());
        taskInfo.setProcessInstanceProcessVariables(processInstance.getProcessVariables());
        taskInfo.setProcessInstanceDescription(processInstance.getDescription());
        Optional.ofNullable(processInstance.getStartTime()).ifPresent(a -> taskInfo.setProcessInstanceStartTime(a.getTime()));
        taskInfo.setProcessInstanceStartUserId(processInstance.getStartUserId());

        FlowElement flowElement = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId()).getMainProcess().getFlowElement(task.getTaskDefinitionKey());
        if (flowElement instanceof UserTask) {
            UserTask userTask = (UserTask) flowElement;
            taskInfo.setCandidateGroups(userTask.getCandidateGroups());
            taskInfo.setCandidateUsers(userTask.getCandidateUsers());
        }
        return taskInfo;
    }


}
