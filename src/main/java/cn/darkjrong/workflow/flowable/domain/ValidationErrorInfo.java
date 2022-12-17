package cn.darkjrong.workflow.flowable.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 验证错误信息
 *
 * @author Rong.Jia
 * @date 2022/12/12
 */
@Data
@ApiModel("错误信息")
public class ValidationErrorInfo implements Serializable {

    private static final long serialVersionUID = -199238151284437754L;

    /**
     * 验证集合名
     */
    @ApiModelProperty("验证集合名")
    protected String validatorSetName;

    /**
     * 问题
     */
    @ApiModelProperty("问题")
    protected String defaultDescription;

    /**
     * 流程定义ID
     */
    @ApiModelProperty("流程定义ID")
    protected String processDefinitionId;

    /**
     * 流程定义名
     */
    @ApiModelProperty("流程定义名")
    protected String processDefinitionName;

    /**
     * XML列号
     */
    @ApiModelProperty("XML列号")
    protected int xmlLineNumber;

    /**
     * XML行号
     */
    @ApiModelProperty("XML行号")
    protected int xmlColumnNumber;

    /**
     * 节点ID
     */
    @ApiModelProperty("节点ID")
    protected String activityId;

    /**
     * 节点名
     */
    @ApiModelProperty("节点名")
    protected String activityName;

    /**
     * 是否警告
     */
    @ApiModelProperty("是否警告")
    protected Boolean isWarning;

}
