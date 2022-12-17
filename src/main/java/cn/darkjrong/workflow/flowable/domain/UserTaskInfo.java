package cn.darkjrong.workflow.flowable.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户节点信息
 *
 * @author Rong.Jia
 * @date 2022/12/08
 */
@Data
@ApiModel("用户节点信息")
public class UserTaskInfo implements Serializable {

    private static final long serialVersionUID = -4098427054025322454L;

    /**
     * 节点ID
     */
    @ApiModelProperty("节点ID")
    private String id;

    /**
     * 节点名
     */
    @ApiModelProperty("节点名")
    private String name;

    /**
     * 审批人
     */
    @ApiModelProperty("审批人")
    private String assignee;

    /**
     * 拥有人
     */
    @ApiModelProperty("拥有人")
    private String owner;

    /**
     * 优先级
     */
    @ApiModelProperty("优先级")
    private String priority;

    /**
     * 表单KEY
     */
    @ApiModelProperty("表单KEY")
    private String formKey;

    /**
     * 任务截止时间
     */
    @ApiModelProperty("任务截止时间")
    private String dueDate;

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

    /**
     * 跳过表达式
     */
    @ApiModelProperty("跳过表达式")
    private String skipExpression;

    /**
     * 任务ID变量名
     */
    @ApiModelProperty("任务ID变量名")
    private String taskIdVariableName;




}
