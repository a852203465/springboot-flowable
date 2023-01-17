package cn.darkjrong.workflow.flowable.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 流程实例信息
 *
 * @author Rong.Jia
 * @date 2022/05/17
 */
@Data
public class ProcessInstanceInfo implements Serializable {

    private static final long serialVersionUID = 8782672608811674527L;

    /**
     * 流程实例ID
     */
    private String id;

    /**
     * 流程部署ID
     */
    private String deploymentId;

    /**
     * 流程部署KEY
     */
    private String deploymentKey;

    /**
     * 流程部署名称
     */
    private String deploymentName;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 流程定义ID
     */
    private String processDefinitionKey;

    /**
     * 流程定义名
     */
    private String processDefinitionName;

    /**
     * 业务Key
     */
    private String businessKey;

    /**
     * 流程发起时间
     */
    private Date startTime;

    /**
     * 流程发起人
     */
    private String startUserId;

    /**
     * 流程是否结束
     */
    private Boolean finished;

    /**
     * 当前节点审批人, 如果是会签是多个
     */
    private Set<String> currentAssignee;

    /**
     * 任务名称
     */
    private String currentTaskName;








}
