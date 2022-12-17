package cn.darkjrong.workflow.flowable.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 活动信息
 *
 * @author Rong.Jia
 * @date 2022/12/07
 */
@Data
public class ActivityInfo implements Serializable {

    private static final long serialVersionUID = -1446483212350728041L;

    /**
     * 节点ID
     */
    @ApiModelProperty("节点ID")
    private String id;

    /**
     * x
     */
    @ApiModelProperty("x")
    private Double x;

    /**
     * y
     */
    @ApiModelProperty("y")
    private Double y;

    /**
     * 宽
     */
    @ApiModelProperty("宽")
    private Double width;

    /**
     * 高
     */
    @ApiModelProperty("高")
    private Double height;

    /**
     * 节点名称
     */
    @ApiModelProperty("节点名称")
    private String name;

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

    /**
     * 开始时间
     */
    @ApiModelProperty("开始时间")
    private Long startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty("结束时间")
    private Long endTime;

    /**
     * 处理人
     */
    @ApiModelProperty("处理人")
    private String assignee;

    /**
     * 持续时间(毫秒)
     */
    @ApiModelProperty("持续时间(毫秒)")
    private Long duration;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String description;

    /**
     * 要求时间
     */
    @ApiModelProperty("要求时间")
    private Long claimTime;

    /**
     * 截止时间
     */
    @ApiModelProperty("截止时间")
    private Long dueDate;

    /**
     * 分类
     */
    @ApiModelProperty("分类")
    private String category;

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
