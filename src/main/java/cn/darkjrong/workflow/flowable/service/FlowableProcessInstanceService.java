package cn.darkjrong.workflow.flowable.service;

import cn.darkjrong.workflow.flowable.domain.ProcessInstanceInfo;
import com.github.pagehelper.PageInfo;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.engine.task.Comment;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Flowable过程实例服务
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
public interface FlowableProcessInstanceService {

    /**
     * 创建流程实例查询
     *
     * @return {@link ProcessInstanceQuery}
     */
    ProcessInstanceQuery createProcessInstanceQuery();

    /**
     * 过程实例根据id
     *
     * @param processInstanceId 流程实例标识
     * @return {@link ProcessInstance}
     */
    ProcessInstance queryProcessInstanceById(String processInstanceId);

    /**
     * 查询流程实例
     *
     * @param processInstanceBusinessKey 流程实例业务键名
     * @return {@link ProcessInstance}
     */
    ProcessInstance queryProcessInstanceByBusinessKey(String processInstanceBusinessKey);

    /**
     * 查询流程实例
     *
     * @param taskId 流程任务标识
     * @return {@link ProcessInstance}
     */
    ProcessInstance queryProcessInstanceByTaskId(String taskId);

    /**
     * 判断流程实例是否已结束
     *
     * @param processInstanceId 流程实例标识
     * @return 流程实例是否已结束
     */
    boolean hasProcessInstanceFinished(String processInstanceId);

    /**
     * 启动流程实例---通过流程定义key（模板ID)
     *
     * @param processDefinitionKey 流程定义Key，不能为空.
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceByKey(String processDefinitionKey);

    /**
     * 启动流程实例
     *
     * @param processDefinitionId 流程定义ID，不能为空.
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceById(String processDefinitionId);

    /**
     * 启动流程实例---流程定义ID
     *
     * @param processDefinitionId 流程定义ID，不能为空.
     * @param variables           流程实例变量。
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables);

    /**
     * 启动流程实例--通过流程定义key、流程实例变量
     *
     * @param processDefinitionKey 流程定义Key，不能为空.
     * @param variables            要传递给流程实例的变量，可以为null.
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables);

    /**
     * 启动流程实例--通过流程定义key、流程实例变量、业务系统标识
     *
     * @param processDefinitionKey 流程定义Key，不能为空.
     * @param tenantId             业务系统标识.
     * @param variables            要传递给流程实例的变量，可以为null.
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceByKeyAndTenantId(String processDefinitionKey, String tenantId, Map<String, Object> variables);

    /**
     * 启动新流程实例----使用给定key在最新版本的流程定义中。
     * <p>
     * 可以提供业务key以将流程实例与具有明确业务含义的特定标识符相关联。
     * 例如，在订单处理中，业务密钥可以是订单ID。
     * 然后，可以使用此业务键轻松查找该流程实例.
     * 提供这样的业务密钥肯定是一个最佳实践.
     * <p>
     * 【说明】processdefinitionKey-businessKey的组合必须是唯一的。
     *
     * @param processDefinitionKey 流程定义Key，不能为空.
     * @param variables            要传递给流程实例的变量，可以为null.
     * @param businessKey          业务key
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    /**
     * 启动新流程实例----使用给定key在最新版本的流程定义中。
     * <p>
     * 可以提供业务key以将流程实例与具有明确业务含义的特定标识符相关联。
     * 例如，在订单处理中，业务密钥可以是订单ID。
     * 然后，可以使用此业务键轻松查找该流程实例.
     * 提供这样的业务密钥肯定是一个最佳实践.
     * <p>
     * 【说明】processdefinitionKey-businessKey的组合必须是唯一的。
     *
     * @param processDefinitionKey 流程定义Key，不能为空.
     * @param businessKey          业务key
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey);

    /**
     * 启动新流程实例----使用给定ID在最新版本的流程定义中。
     * <p>
     * 可以提供业务key以将流程实例与具有明确业务含义的特定标识符相关联。
     * 例如，在订单处理中，业务密钥可以是订单ID。
     * 然后，可以使用此业务键轻松查找该流程实例.
     * 提供这样的业务密钥肯定是一个最佳实践.
     * <p>
     * 【说明】processdefinitionId-businessKey的组合必须是唯一的。
     *
     * @param processDefinitionId 流程定义ID，不能为空.
     * @param variables            要传递给流程实例的变量，可以为null.
     * @param businessKey          业务key
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables);

    /**
     * 启动新流程实例----使用给定ID在最新版本的流程定义中。
     * <p>
     * 可以提供业务key以将流程实例与具有明确业务含义的特定标识符相关联。
     * 例如，在订单处理中，业务密钥可以是订单ID。
     * 然后，可以使用此业务键轻松查找该流程实例.
     * 提供这样的业务密钥肯定是一个最佳实践.
     * <p>
     * 【说明】processdefinitionId-businessKey的组合必须是唯一的。
     *
     * @param processDefinitionId 流程定义ID，不能为空.
     * @param businessKey         业务key
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey);

    /**
     * 启动流程实例--通过流程定义key、业务主键key、流程实例变量、业务系统标识
     *
     * @param processDefinitionKey 流程定义Key，不能为空.
     * @param businessKey          业务主键key.
     * @param tenantId             业务系统标识.
     * @param variables            要传递给流程实例的变量，可以为null.
     * @return {@link ProcessInstance}
     */
    ProcessInstance startProcessInstanceByKeyAndTenantId(String processDefinitionKey, String businessKey, String tenantId, Map<String, Object> variables);

    /**
     * 中断流程实例
     *
     * @param processInstanceId 流程实例id
     */
    void suspendProcessInstance(String processInstanceId);

    /**
     * 删除流程实例
     *
     * @param processInstanceId 流程实例id
     * @param deleteReason      删除原因
     */
    void deleteProcessInstance(String processInstanceId, String deleteReason);

    /**
     * 设置流程开始节点发起人
     *
     * @param authenticatedUserId 用户id
     */
    void setAuthenticatedUserId(String authenticatedUserId);

    /**
     * 启动流程实例并执行第一个流程任务，并且设置下一任务处理人---通过流程定义key（模板ID)
     *
     * @param processDefinitionKey 流程定义Key，不能为空.
     * @param variables            流程实例变量.
     * @param actorIds             下一环节任务处理人.
     * @return {@link ProcessInstance}
     */
    ProcessInstance startInstanceAndExecuteFirstTaskByProcessDefinitionKey(String processDefinitionKey, Map<String, Object> variables, Map<String, Object> actorIds);

    /**
     * 使用给定的id激活流程实例.
     *
     * @param processInstanceId 流程实例ID
     */
    void activateProcessInstanceById(String processInstanceId);

    /**
     * 启动流程实例并执行第一个流程任务，并且设置下一个任务处理人---通过流程定义key（模板ID)
     *
     * @param processDefinitionKey 流程定义Key，不能为空.
     * @param tenantId             业务系统标识.
     * @param userId               流程实例启动人.
     * @param variables            流程实例变量.
     */
    void startInstanceAndExecuteFirstTaskByProcessDefinitionKey(String processDefinitionKey, String tenantId, String userId, Map<String, Object> variables);

    /**
     * 获取我发起的流程实例
     *
     * @param pageNum    页号
     * @param pageSize   分页大小
     * @param assignee   我的用户标识
     * @param isFinished 是否完成
     * @param before     在X时间节点之前
     * @param after      在X时间节点之后
     * @return {@link PageInfo}<{@link ProcessInstanceInfo}>
     */
    PageInfo<ProcessInstanceInfo> queryMindProcessInstance(String assignee, Integer pageNum, Integer pageSize, Boolean isFinished, Date before, Date after);

    /**
     * 流程实例详情
     *
     * @param processInstanceId 流程实例ID
     * @return {@link ProcessInstanceInfo}
     */
    ProcessInstanceInfo queryProcessInstance(String processInstanceId);

    /**
     * 获取过程评论
     *
     * @param processInstanceId 流程实例id
     * @return {@link List}<{@link Comment}>
     */
    List<Comment> queryProcessComments(String processInstanceId);

    /**
     * 根据流程实例Id,获取实时流程图片
     * @param processInstanceId 流程实例ID
     * @return png 图片数组
     */
    byte[] generateFlowImgByProcInstId(String processInstanceId);

    /**
     * 根据流程实例Id,获取实时流程SVG
     *
     * @param processInstanceId 过程实例ID
     * @return {@link byte[]} SVG
     */
    byte[] generateFlowSvgByProcInstId(String processInstanceId);

    /**
     * 获取执行
     *
     * @param executionId 执行ID
     * @return {@link Execution}
     */
    Execution getExecution(String executionId);




























}
