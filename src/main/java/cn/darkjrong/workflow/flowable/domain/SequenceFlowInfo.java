package cn.darkjrong.workflow.flowable.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 节点流转信息
 *
 * @author Rong.Jia
 * @date 2022/12/08
 */
@Data
@ApiModel("节点流转信息")
public class SequenceFlowInfo implements Serializable {

    private static final long serialVersionUID = -4357686429612455431L;

    /**
     * 线ID
     */
    @ApiModelProperty("线ID")
    private String id;

    /**
     * 线名称
     */
    @ApiModelProperty("线名称")
    private String name;

    /**
     * 条件表达式
     */
    @ApiModelProperty("条件表达式")
    private String conditionExpression;

    /**
     * 来源线ID
     */
    @ApiModelProperty("来源线ID")
    private String sourceRef;

    /**
     * 目标线ID
     */
    @ApiModelProperty("目标线ID")
    private String targetRef;

    /**
     * 跳过表达式
     */
    @ApiModelProperty("跳过表达式")
    private String skipExpression;

    /**
     * 条件信息
     */
    @ApiModelProperty("条件信息")
    private List<ExtensionElementInfo> extensionElements;

}
