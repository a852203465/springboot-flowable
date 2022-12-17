package cn.darkjrong.workflow.flowable.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 历史任务信息
 *
 * @author Rong.Jia
 * @date 2022/05/18
 */
@Data
public class HistoricTaskInfo implements Serializable {

    private static final long serialVersionUID = 2074886613995094139L;

    /**
     * 主键
     */
    private String id;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 任务的ID，画图时的ID
     */
    private String taskDefKey;

    /**
     * 所属流程实例ID
     */
    private String processInstanceId;

    /**
     * 所属执行实例ID
     */
    private String executionId;

    /**
     * 任务名
     */
    private String name;

    /**
     * 父级任务ID
     */
    private String parentTaskId;

    /**
     * 描述
     */
    private String description;

    /**
     * 任务拥有者
     */
    private String owner;

    /**
     * 任务办理人
     */
    private String assignee;

    /**
     * 任务开始时间
     */
    private Date startTime;

    /**
     * 任务被拾取时间
     */
    private Date claimTime;

    /**
     * 任务结束时间
     */
    private Date endTime;

    /**
     * 总耗时(毫秒)
     */
    private Long duration;

    /**
     * 删除原因
     */
    private String deleteReason;

    /**
     * 截止时间
     */
    private Date dueTime;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 任务分类
     */
    private String category;

    /**
     * 本地任务变量
     */
    private Map<String, Object> taskLocalVariables;

    /**
     * 任务的流程变量
     */
    private Map<String, Object> processVariables;











}
