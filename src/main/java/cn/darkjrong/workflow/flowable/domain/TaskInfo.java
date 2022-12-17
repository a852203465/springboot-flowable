package cn.darkjrong.workflow.flowable.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 任务信息
 *
 * @author Rong.Jia
 * @date 2022/05/17
 */
@ApiModel("任务信息")
@NoArgsConstructor
@Data
public class TaskInfo implements Serializable {

    private static final long serialVersionUID = 6716016535093643434L;

    /**
     * 流程部署ID
     */
    @ApiModelProperty("流程部署ID")
    private String deploymentId;

    /**
     * 流程部署KEY
     */
    @ApiModelProperty("流程部署KEY")
    private String deploymentKey;

    /**
     * 流程部署名称
     */
    @ApiModelProperty("流程部署名称")
    private String deploymentName;

    /**
     * 任务ID
     */
    @ApiModelProperty("任务ID")
    private String id;

    /**
     * 任务名
     */
    @ApiModelProperty("任务名")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String description;

    /**
     * 任务的紧迫性【int】
     */
    @ApiModelProperty("任务的紧迫性")
    private Integer priority;

    /**
     * 负责此任务的人员
     */
    @ApiModelProperty("负责此任务的人员")
    private String owner;

    /**
     * 将此任务委派给的对象
     */
    @ApiModelProperty("将此任务委派给的对象")
    private String assignee;

    /**
     * 任务创建的时间
     */
    @ApiModelProperty("任务创建的时间")
    private Long createTime;

    /**
     * 任务截止时间
     */
    @ApiModelProperty("任务截止时间")
    private Long dueDate;

    /**
     * 任务类别
     */
    @ApiModelProperty("任务类别")
    private String category;

    /**
     * 任务的流程变量
     */
    @ApiModelProperty("任务的流程变量")
    private Map<String, Object> processVariables;

    /**
     * 任务领取时间
     */
    @ApiModelProperty("任务领取时间")
    private Long claimTime;

    /**
     * 流程实例名称
     */
    @ApiModelProperty("流程实例名称")
    private String processInstanceName;

    /**
     * 流程定义Key
     */
    @ApiModelProperty("流程定义Key")
    private String processDefinitionKey;

    /**
     * 流程实例关联的业务表ID
     */
    @ApiModelProperty("流程实例关联的业务表ID")
    private String processInstanceBusinessKey;

    /**
     * 流程实例是否被挂起
     */
    @ApiModelProperty("流程实例是否被挂起")
    private Boolean processInstanceIsSuspended;

    /**
     * 流程实例变量
     */
    @ApiModelProperty("流程实例变量")
    private Map<String, Object> processInstanceProcessVariables;

    /**
     * 流程实例描述
     */
    @ApiModelProperty("流程实例描述")
    private String processInstanceDescription;

    /**
     * 流程实例开始的时间
     */
    @ApiModelProperty("流程实例开始的时间")
    private Long processInstanceStartTime;

    /**
     * 流程实例发起人的ID
     */
    @ApiModelProperty("流程实例发起人的ID")
    private String processInstanceStartUserId;

    /**
     * 任务定义KEY
     */
    @ApiModelProperty("任务定义KEY")
    private String taskDefinitionKey;

    /**
     * 候选执行人
     */
    @ApiModelProperty("候选执行人")
    private List<String> candidateUsers;

    /**
     * 候选人组
     */
    @ApiModelProperty("候选人组")
    private List<String> candidateGroups;











}
