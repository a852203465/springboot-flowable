package cn.darkjrong.workflow.flowable.service;

import cn.darkjrong.workflow.flowable.domain.HistoricActivityInfo;
import cn.darkjrong.workflow.flowable.domain.HistoricTaskInfo;
import cn.darkjrong.workflow.flowable.domain.ReturnTask;
import com.github.pagehelper.PageInfo;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;

import java.util.List;
import java.util.Set;

/**
 * Flowable历史任务服务
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
public interface FlowableHistoricService {

    /**
     * 创建历史任务实例查询
     *
     * @return {@link HistoricTaskInstanceQuery}
     */
    HistoricTaskInstanceQuery createHistoricTaskInstanceQuery();

    /**
     * 创建历史活动实例查询对象
     *
     * @return {@link HistoricActivityInstanceQuery}
     */
    HistoricActivityInstanceQuery createHistoricActivityInstanceQuery();

    /**
     * 创建历史流程实例查询对象
     *
     * @return {@link HistoricProcessInstanceQuery}
     */
    HistoricProcessInstanceQuery createHistoricProcessInstanceQuery();

    /**
     * 根据任务ID获取历史任务
     *
     * @param taskId 任务id
     * @return {@link HistoricTaskInfo}
     */
    HistoricTaskInfo queryHistoricTask(String taskId);

    /**
     * 查询历史环节
     *
     * @param activityId 环节id
     * @return {@link HistoricActivityInfo}
     */
    HistoricActivityInfo queryHistoricActivity(String activityId);

    /**
     * 获取历史任务列表
     *
     * @param processInstanceId 流程实例ID
     * @return {@link List}<{@link HistoricTaskInfo}>
     */
    List<HistoricTaskInfo> queryHistoricTasks(String processInstanceId);

    /**
     * 查询历史环节列表
     *
     * @param processInstanceId 流程实例ID
     * @return {@link List}<{@link HistoricActivityInfo}>
     */
    List<HistoricActivityInfo> queryHistoricActivitys(String processInstanceId);

    /**
     *通过用户标识(实际参与者)，分页查询历史任务列表
     *
     * @param assignee    用户标识.
     * @param pageNum    页号
     * @param pageSize   分页大小
     * @return {@link PageInfo}<{@link HistoricTaskInfo}>
     */
    PageInfo<HistoricTaskInfo> queryHistoricTasks(String assignee, Integer pageNum, Integer pageSize);

    /**
     *通过用户标识(实际参与者)，历史任务列表
     *
     * @param assignee    用户标识.
     * @return {@link List}<{@link HistoricTaskInfo}>
     */
    List<HistoricTaskInfo> queryHistoricTasksByAssignee(String assignee);

    /**
     * 通过流程实例ID获取历史流程实例
     *
     * @param procInstId proc本月id
     * @return {@link HistoricProcessInstance}
     */
    HistoricProcessInstance queryHistoricProcessInstance(String procInstId);

    /**
     * 通过流程实例ID获取流程中已经执行的节点，按照执行先后顺序排序
     *
     * @param procInstId 流程实例ID
     * @return {@link List}<{@link HistoricActivityInstance}>
     */
    List<HistoricActivityInstance> queryHistoricActivityInstanceAsc(String procInstId);

    /**
     * 通过流程实例ID获取已经完成的历史流程实例
     *
     * @param procInstId 流程实例ID
     * @return {@link List}<{@link HistoricProcessInstance}>
     */
    List<HistoricProcessInstance> queryHistoricFinishedByProcInstId(String procInstId);

    /**
     * 通过流程实例ID获取历史任务实例,并根据添加时间倒序
     *
     * @param procInstId 流程实例ID
     * @return {@link List}<{@link HistoricTaskInstance}>
     */
    List<HistoricTaskInstance> queryHistoricTaskOrderCreateTimeDesc(String procInstId);

    /**
     * 通过流程实例ID获取历史任务实例,并根据添加时间升序
     *
     * @param procInstId 流程实例ID
     * @return {@link List}<{@link HistoricActivityInstance}>
     */
    List<HistoricTaskInstance> queryHistoricTaskOrderCreateTimeAsc(String procInstId);

    /**
     * 查询历史任务实例
     *
     * @param taskId 任务id
     * @return {@link HistoricTaskInstance}
     */
    HistoricTaskInstance queryHistoricTaskInstance(String taskId);

    /**
     * 获取某个人发起的流程
     *
     * @param user 用户
     * @return {@link List}<{@link HistoricProcessInstance}>
     */
    List<HistoricProcessInstance> queryHistoricTasksByStartUser(String user);

    /**
     * 查询可回退任务
     *
     * @param taskId 任务ID
     * @return {@link List}<{@link ReturnTask}>
     */
    List<ReturnTask> queryReturnTasks(String taskId);

    /**
     * 查询可回退任务
     *
     * @param processInstanceId 流程实例ID
     * @param assignee          受让人
     * @param candidateGroups   候选人团体
     * @return {@link List}<{@link ReturnTask}>
     */
    List<ReturnTask> queryReturnTasks(String processInstanceId, String assignee, Set<String> candidateGroups);






}
