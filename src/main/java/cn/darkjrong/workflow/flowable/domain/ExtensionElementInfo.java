package cn.darkjrong.workflow.flowable.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 扩展元素信息
 *
 * @author Rong.Jia
 * @date 2022/12/08
 */
@Data
@ApiModel("条件表达式信息")
public class ExtensionElementInfo implements Serializable {

    private static final long serialVersionUID = 5667265891820412579L;

    /**
     * 名称
     */
    @ApiModelProperty("名称")
    private String name;

    /**
     * 值
     */
    @ApiModelProperty("值")
    private String value;

    /**
     * 表达式
     */
    @ApiModelProperty("表达式")
    private String expression;


}
