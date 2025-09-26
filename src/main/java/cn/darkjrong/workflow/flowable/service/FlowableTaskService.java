package cn.darkjrong.workflow.flowable.service;

import com.github.pagehelper.PageInfo;
import cn.darkjrong.workflow.flowable.domain.ActivityInfo;
import cn.darkjrong.workflow.flowable.domain.ExtensionElementInfo;
import cn.darkjrong.workflow.flowable.domain.HistoricTaskInfo;
import cn.darkjrong.workflow.flowable.domain.TaskInfo;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.impl.persistence.entity.CommentEntity;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Flowable任务服务
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
public interface FlowableTaskService {

    /**
     * 创建流程任务查询对象
     *
     * @return {@link TaskQuery} 流程查询对象
     */
    TaskQuery createTaskQuery();

    /**
     * 创建执行查询对象
     *
     * @return {@link ExecutionQuery}
     */
    ExecutionQuery createExecutionQuery();

    /**
     * 通过流程实例ID获取流程中正在执行的节点
     *
     * @param procInstId 流程实例ID
     * @return {@link List}<{@link Execution}>
     */
    List<Execution> queryRunningActivityByProcessInstanceId(String procInstId);

    /**
     * 获取待办任务明细
     *
     * @param processInstanceId 流程实例id
     * @return {@link List}<{@link TaskInfo}>
     */
    List<TaskInfo> queryToDoTaskInfo(String processInstanceId);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return {@link TaskInfo}
     */
    TaskInfo queryTaskInfo(String taskId);

    /**
     * 待批任务
     *
     * @param pageNum  页号
     * @param pageSize 分页大小
     * @param assignee 用户标识(一般是用户ID)
     * @return {@link PageInfo}<{@link TaskInfo}>
     */
    PageInfo<TaskInfo> queryAssigneeTasks(String assignee, Integer pageNum, Integer pageSize);

    /**
     * 通过用户标识(任务候选人)，分页查询任务列表
     *
     * @param pageNum       页号
     * @param pageSize      分页大小
     * @param candidateUser 用户标识
     * @return {@link PageInfo}<{@link TaskInfo}>
     */
    PageInfo<TaskInfo> queryTaskByCandidateUser(String candidateUser, Integer pageNum, Integer pageSize);

    /**
     * 通过用户标识(实际参与者、或候选人)，分页查询任务列表
     *
     * @param pageNum       页号
     * @param pageSize      分页大小
     * @param candidateUser 用户标识
     * @return {@link PageInfo}<{@link TaskInfo}>
     */
    PageInfo<TaskInfo> queryTaskByCandidateOrAssigned(String candidateUser, Integer pageNum, Integer pageSize);

    /**
     * 查询任务
     *
     * @param query    查询
     * @param pageNum  页号
     * @param pageSize 分页大小
     * @return {@link PageInfo}<{@link TaskInfo}>
     */
    PageInfo<TaskInfo> queryTask(TaskQuery query, Integer pageNum, Integer pageSize);

    /**
     * 通过用户标识(或候选人)，分页查询任务列表
     *
     * @param userId    用户标识.
     * @param variables 查询条件map.
     * @param pageNum   页号
     * @param pageSize  分页大小
     * @return {@link PageInfo}<{@link TaskInfo}>
     */
    PageInfo<TaskInfo> queryTaskCandidateUserByCondition(String userId, Map<String, Object> variables, Integer pageNum, Integer pageSize);

    /**
     * 通过用户标识(实际参与者)，分页查询任务列表
     *
     * @param assignee  用户标识.
     * @param variables 查询条件map.
     * @param pageNum   页号
     * @param pageSize  分页大小
     * @return {@link PageInfo}<{@link TaskInfo}>
     */
    PageInfo<TaskInfo> queryTaskAssigneeByCondition(String assignee, Map<String, Object> variables, Integer pageNum, Integer pageSize);

    /**
     * 通过用户标识(实际参与者、或候选人)，分页查询任务列表
     *
     * @param userId    用户标识.
     * @param variables 查询条件map.
     * @param pageNum   页号
     * @param pageSize  分页大小
     * @return {@link PageInfo}<{@link TaskInfo}>
     */
    PageInfo<TaskInfo> queryTaskCandidateOrAssignedByCondition(String userId, Map<String, Object> variables, Integer pageNum, Integer pageSize);

    /**
     * 通过用户标识(候选人)，统计活动任务数目
     *
     * @param userId 用户标识.
     * @return 活动任务数量.
     */
    long countTaskCandidateUser(String userId);

    /**
     * 通过用户标识(实际参与者)，统计活动任务数目
     *
     * @param userId 用户标识.
     * @return 活动任务数量.
     */
    long countTaskAssignee(String userId);

    /**
     * 通过用户标识(实际参与者、或候选人)，统计活动任务数目（用户参与的）
     *
     * @param userId 用户标识.
     * @return 活动任务数量.
     */
    long countTaskCandidateOrAssigned(String userId);

    /**
     * 通过用户标识(候选人)及查询条件map，统计活动任务数目（用户参与的）
     *
     * @param userId    用户标识.
     * @param variables 查询条件map.
     * @return 活动任务数量.
     */
    long countTaskCandidateUserByCondition(String userId, Map<String, Object> variables);

    /**
     * 通过用户标识(实际参与者)及查询条件map，统计活动任务数目（用户参与的）
     *
     * @param userId    用户标识.
     * @param variables 查询条件map.
     * @return 活动任务数量.
     */
    long countTaskAssigneeByCondition(String userId, Map<String, Object> variables);

    /**
     * 通过用户标识(实际参与者、候选人)及查询条件map，统计活动任务数目（用户参与的）
     *
     * @param userId    用户标识.
     * @param variables 查询条件map.
     * @return 活动任务数量.
     */
    long countTaskCandidateOrAssignedByCondition(String userId, Map<String, Object> variables);

    /**
     * 查询变量根据任务id
     *
     * @param taskId       流程任务ID.
     * @param variableName 流程变量name.
     * @return {@link String} 流程变量Value
     */
    String findVariableByTaskId(String taskId, String variableName);

    /**
     * 根据流程实例ID,查询活动任务列表（多实例）
     *
     * @param processInstanceId 流程实例ID.
     * @return {@link List}<{@link Task}>
     */
    List<Task> processInstanceId4Multi(String processInstanceId);

    /**
     * 查询任务业务主键
     *
     * @param taskId 流程任务ID.
     * @return {@link String}   业务主键.
     */
    String findBusinessKeyByTaskId(String taskId);

    /**
     * 查询任务
     *
     * @param taskId 流程任务ID.
     * @return {@link Task}
     */
    Task queryTask(String taskId);

    /**
     * 查询活动的流程任务
     *
     * @param processInstanceId 流程实例ID.
     * @return {@link Task} 流程任务
     */
    Task queryTaskByProcessInstanceId(String processInstanceId);

    /**
     * 查询活动的流程任务
     *
     * @param assignee 处理人
     * @param processInstanceId 流程实例ID.
     * @return {@link Task} 流程任务
     */
    Task queryTaskByProcessInstanceId(String processInstanceId, String assignee);

    /**
     * 根据 办理人或者候选人或者候选人组查询活动的流程任务
     *
     * @param assignee          处理人
     * @param processInstanceId 流程实例ID.
     * @param candidateGroups   候选人团体
     * @return {@link Task}
     */
    Task queryTaskByAssigneeOrCandidate(String processInstanceId, String assignee, Set<String> candidateGroups);

    /**
     * 查询已办
     *
     * @param assignee 处理人
     * @param candidateGroups 候选人团体
     * @return {@link List }<{@link HistoricTaskInfo }>
     */
    List<HistoricTaskInfo> findHaveDoneTask(String assignee, Set<String> candidateGroups);

    /**
     * 根据 办理人或者候选人或者候选人组查询活动的流程任务
     *
     * @param assignee          处理人
     * @param processInstanceId 流程实例ID.
     * @param candidateGroups   候选人团体
     * @return {@link TaskInfo}
     */
    TaskInfo queryTodoTask(String processInstanceId, String assignee, Set<String> candidateGroups);

    /**
     *  查询待办
     *  根据 办理人或者候选人或者候选人组查询活动的流程任务
     *
     * @param assignee          处理人或者候选者
     * @param candidateGroups   候选人团体
     * @return {@link Task}
     */
    List<Task> queryTodoTask(String assignee, Set<String> candidateGroups);

    /**
     * 查询活动的流程任务
     *
     * @param candidateUser 代办人
     * @param processInstanceId 流程实例ID.
     * @return {@link Task} 流程任务
     */
    Task queryTaskByCandidateUser(String processInstanceId, String candidateUser);

    /**
     * 查询活动的流程任务
     *
     * @param candidateGroups 代办组
     * @param processInstanceId 流程实例ID.
     * @return {@link Task} 流程任务
     */
    Task queryTaskByCandidateGroup(String processInstanceId, Set<String> candidateGroups);

    /**
     * 查询活动的流程任务
     *
     * @param processInstanceId 流程实例ID.
     * @return {@link Task} 流程任务
     */
    List<Task> queryTasksByProcessInstanceId(String processInstanceId);

    /**
     * 为流程任务设置变量。
     * 如果变量尚未存在，则将在任务中创建该变量。
     *
     * @param taskId        任务的id，不能为null.
     * @param variableName  变量键名.
     * @param variableValue 变量键值.
     */
    void setVariableLocal(String taskId, String variableName, Object variableValue);

    /**
     * 为流程任务设置多对变量。
     * 如果变量尚未存在，则将在任务中创建该变量。
     *
     * @param taskId    任务的id，不能为null.
     * @param variables 多对变量键值对.
     */
    void setVariablesLocal(String taskId, Map<String, Object> variables);

    /**
     * 为流程任务设置变量。
     * 如果变量尚未存在，将在最外层的作用域中创建
     *
     * @param taskId        任务的id，不能为null.
     * @param variableName  变量键名.
     * @param variableValue 变量键值.
     */
    void setVariable(String taskId, String variableName, Object variableValue);

    /**
     * 为流程任务设置多对变量。
     * 如果变量尚未存在，将在最外层的作用域中创建。
     *
     * @param taskId    任务的id，不能为null.
     * @param variables 多对变量键值对.
     */
    void setVariables(String taskId, Map<String, Object> variables);

    /**
     * 任务签收。
     *
     * @param taskId   任务的id
     * @param assignee 签收人标识.
     */
    void claim(String taskId, String assignee);

    /**
     * 任务反签收
     *
     * @param taskId 任务的id
     */
    void unClaim(String taskId);

    /**
     * 执行任务
     *
     * @param taskId    任务的id
     */
    void complete(String taskId);

    /**
     * 任务处理(同意)
     *
     * @param taskId    任务ID
     * @param description   处理批注
     */
    void complete(String taskId, String description);

    /**
     * 任务处理(同意)
     *
     * @param taskId    任务ID
     * @param description   处理批注
     * @param variables 预定义参数值
     */
    void complete(String taskId, String description, Map<String, Object> variables);

    /**
     * 任务处理(同意)
     *
     * @param processInstanceId 流程实例id
     * @param assignee          受让人
     * @param description       描述
     */
    void complete(String processInstanceId, String assignee, String description);

    /**
     * 任务处理(同意)
     *
     * @param processInstanceId 流程实例id
     * @param assignee          受让人
     * @param description       描述
     * @param variables         变量
     */
    void complete(String processInstanceId, String assignee, String description, Map<String, Object> variables);

    /**
     * 任务移交：将任务的所有权转移给其他用户。
     *
     * @param taskId 任务的id，不能为null.
     * @param userId 接受所有权的人.
     */
    void assignTask(String taskId, String userId);

    /**
     * 任务委派
     *
     * @param taskId 任务的id，不能为null.
     * @param userId 被委派人ID.
     */
    void delegateTask(String taskId, String userId);

    /**
     * 委派任务完成，归还委派人
     *
     * @param taskId 任务的id，不能为null.
     */
    void resolveTask(String taskId);

    /**
     * 委派任务完成，归还委派人
     *
     * @param variables 预定义参数值
     * @param taskId 任务的id，不能为null.
     */
    void resolveTask(String taskId, Map<String, Object> variables);

    /**
     * 更改任务拥有者
     *
     * @param taskId 任务的id，不能为null.
     * @param userId 任务拥有者.
     */
    void setOwner(String taskId, String userId);

    /**
     * 设置到期日期
     *
     * @param taskId 任务ID
     * @param date   日期
     */
    void setDueDate(String taskId, Date date);

    /**
     * 删除任务
     *
     * @param taskId 任务的id，不能为null.
     */
    void deleteTask(String taskId);

    /**
     * 删除任务，附带删除理由
     *
     * @param taskId 任务的id，不能为null.
     * @param reason 删除理由.
     */
    void deleteTask(String taskId, String reason);

    /**
     * 为任务添加任务处理人。
     *
     * @param taskId 任务的id，不能为null.
     * @param userId 任务处理人ID.
     */
    void addCandidateUser(String taskId, String userId);

    /**
     * 为流程任务 和/或 流程实例添加注释。
     *
     * @param taskId            流程任务ID.
     * @param processInstanceId 流程实例ID.
     * @param message           注释信息
     * @return {@link Comment} 注释
     */
    Comment addComment(String taskId, String processInstanceId, String message);

    /**
     * 查询与任务相关的注释信息。
     *
     * @param taskId 流程任务ID.
     * @return {@link List}<{@link Comment}> 注释
     */
    List<Comment> queryTaskComments(String taskId);

    /**
     * 查询任务评论
     *
     * @param taskId 任务ID
     * @param type   类型 @see{@link CommentEntity}
     * @return {@link List}<{@link Comment}>
     */
    List<Comment> queryTaskComments(String taskId, String type);

    /**
     * 是暂停任务
     *
     * @param taskId 任务id
     * @return {@link Boolean}
     */
    Boolean isSuspendedTask(String taskId);

    /**
     * 任务撤回
     *
     * @param processInstanceId 流程实例ID.
     * @param currentActivityId 当前活动任务ID.
     * @param newActivityId     撤回到达的任务ID.
     */
    void withdraw(String processInstanceId, String currentActivityId, String newActivityId);

    /**
     * 驳回任务
     *
     * @param taskId 任务ID
     * @param description       描述
     */
    void taskReject(String taskId, String description);

    /**
     * 驳回任务
     *
     * @param processInstanceId 流程实例id
     * @param assignee          受让人
     * @param description 描述
     */
    void taskReject(String processInstanceId, String assignee, String description);

    /**
     * 多实例加签
     *
     * @param activityDefId 流程环节定义Key，不能为空.
     * @param instanceId    流程实例ID.
     * @param variables     流程实例变量.
     */
    void addMultiInstanceExecution(String activityDefId, String instanceId, Map<String, Object> variables);

    /**
     * 多实例减签
     *
     * @param currentChildExecutionId 流程环节定义Key，不能为空.
     * @param flag                    子执行流是否已执行
     */
    void deleteMultiInstanceExecution(String currentChildExecutionId, boolean flag);

    /**
     * 退回任务
     * @param taskId 任务ID
     * @param description 描述
     * @param preTaskId 指定任务ID
     */
    void taskReturn(String taskId, String preTaskId, String description);

    /**
     * 退回到上一节点
     *
     * @param taskId     任务id
     * @param description 描述
     */
    void taskReturn(String taskId, String description);

    /**
     * 终止任务,指向结束节点
     *
     * @param taskId      任务id
     * @param description 描述
     */
    void endTask(String taskId, String description);

    /**
     * 取消任务
     *
     * @param processInstanceId 流程实例id
     */
    void cancelTask(String processInstanceId);

    /**
     * 跳到最开始的任务节点（直接打回）
     *
     * @param taskId      任务ID
     * @param description 描述
     */
    void jumpToStart(String taskId, String description);

    /**
     * 任务前加签 （如果多次加签只能显示第一次前加签的处理人来处理任务）
     * 多个加签人处理完毕任务之后又流到自己这里
     *
     * @param processInstanceId 流程实例id
     * @param assignee          受让人
     * @param description       描述
     * @param assignees 被加签人
     */
    void addTasksBefore(String processInstanceId, String assignee, Set<String> assignees, String description);

    /**
     * 任务后加签（加签人自己自动审批完毕加签多个人处理任务）
     *
     * @param processInstanceId 流程实例id
     * @param assignee          受让人
     * @param description       描述
     * @param assignees 被加签人
     */
    void addTasksAfter(String processInstanceId, String assignee, Set<String> assignees, String description);

    /**
     * 添加任务
     *
     * @param processInstanceId 流程实例id
     * @param assignee          受让人
     * @param description       描述
     * @param assignees 被加签人
     * @param flag  true向后加签  false向前加签
     */
    void addTask(String processInstanceId, String assignee, Set<String> assignees, String description, Boolean flag);

    /**
     * 获取序列流扩展节点
     *
     * @param taskId 任务ID
     * @return {@link Map}<{@link String}, {@link List}<{@link ExtensionElement}>>
     */
    Map<String, List<ExtensionElement>> getSequenceFlowExtensionElement(String taskId);

    /**
     * 获取扩展属性值
     *
     * @param taskId 任务ID
     * @return {@link List}<{@link ExtensionElementInfo}>
     */
    List<ExtensionElementInfo> getExtensionElement(String taskId);

    /**
     * 获取下一个用户流元素
     *
     * @param taskId 任务ID
     * @return {@link FlowElement}
     */
    FlowElement getNextUserFlowElement(String taskId);

    /**
     * 获取当前任务的下一可流转节点及条件
     *
     * @param taskId 任务ID
     * @return {@link Map}<{@link FlowElement}, {@link SequenceFlow}>
     */
    Map<FlowElement, SequenceFlow> getNextUserFlows(String taskId);

    /**
     * 获取当前任务的下一可流转节点及条件
     *
     * @param task 任务
     * @return {@link Map}<{@link FlowElement}, {@link SequenceFlow}>
     */
    Map<FlowElement, SequenceFlow> getNextUserFlows(Task task);

    /**
     * 获取流程活动信息
     *
     * @param processInstanceId 流程实例ID
     * @return {@link List}<{@link ActivityInfo}>
     */
    List<ActivityInfo> getActivityInfo(String processInstanceId);

    /**
     * 下一个流程节点是否是事件结束
     *
     * @param assignee          处理人
     * @param processInstanceId 流程实例ID
     * @param candidateGroups   候选人团体
     * @return {@link Boolean}
     */
    Boolean isNextFlowNodeEndEvent(String processInstanceId, String assignee, Set<String> candidateGroups);

    /**
     * 领取任务
     *
     * @param assignee          领取人
     * @param processInstanceId 流程实例ID
     * @param candidateGroups   领取所在候选人组
     */
    void claimTask(String processInstanceId, String assignee, Set<String> candidateGroups);
















































































}
