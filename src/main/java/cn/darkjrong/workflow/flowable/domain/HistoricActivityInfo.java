package cn.darkjrong.workflow.flowable.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 历史环节信息
 *
 * @author Rong.Jia
 * @date 2022/05/18
 */
@Data
@ApiModel("历史环节信息")
public class HistoricActivityInfo implements Serializable {

    private static final long serialVersionUID = 6137371987099535469L;

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private String id;

    /**
     * 环节ID
     */
    @ApiModelProperty("环节ID")
    private String activityId;

    /**
     * 名称
     */
    @ApiModelProperty("名称")
    private String activityName;

    /**
     * 类型
     */
    @ApiModelProperty("类型")
    private String activityType;

    /**
     * 所属执行实例ID
     */
    @ApiModelProperty("所属执行实例ID")
    private String executionId;

    /**
     * 任务办理人
     */
    @ApiModelProperty("任务办理人")
    private String assignee;

    /**
     * 任务ID
     */
    @ApiModelProperty("任务ID")
    private String taskId;

    /**
     * 被流程的流程实例
     */
    @ApiModelProperty("被流程的流程实例")
    private String calledProcessInstanceId;

    /**
     * 流程定义ID
     */
    @ApiModelProperty("流程定义ID")
    private String processDefinitionId;

    /**
     * 流程实例ID
     */
    @ApiModelProperty("流程实例ID")
    private String processInstanceId;

    /**
     * 任务开始时间
     */
    @ApiModelProperty("任务开始时间")
    private Long startTime;

    /**
     * 任务结束时间
     */
    @ApiModelProperty("任务结束时间")
    private Long endTime;

    /**
     * 总耗时(毫秒)
     */
    @ApiModelProperty("总耗时(毫秒)")
    private Long duration;

    /**
     * 删除原因
     */
    @ApiModelProperty("删除原因")
    private String deleteReason;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String description;


}
