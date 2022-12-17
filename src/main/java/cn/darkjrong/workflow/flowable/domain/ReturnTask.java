package cn.darkjrong.workflow.flowable.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 可退回任务
 *
 * @author Rong.Jia
 * @date 2022/12/07
 */
@Data
@ApiModel("可退回任务信息")
public class ReturnTask implements Serializable {

    private static final long serialVersionUID = -1224648487683447398L;

    /**
     * 任务ID
     */
    @ApiModelProperty("任务ID")
    private String taskId;

    /**
     * 任务名
     */
    @ApiModelProperty("任务名")
    private String taskName;

    /**
     * 审批人
     */
    @ApiModelProperty("审批人")
    private String assignee;

    /**
     * 流程实例ID
     */
    @ApiModelProperty("流程实例ID")
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    @ApiModelProperty("流程定义ID")
    private String processDefinitionId;













}
