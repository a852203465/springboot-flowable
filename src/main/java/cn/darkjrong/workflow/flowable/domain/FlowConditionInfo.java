package cn.darkjrong.workflow.flowable.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 流转条件信息
 *
 * @author Rong.Jia
 * @date 2022/12/08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("流转条件信息")
public class FlowConditionInfo implements Serializable {

    private static final long serialVersionUID = -8509743968014008810L;

    /**
     * 节点信息
     */
    @ApiModelProperty("节点信息")
    private UserTaskInfo element;

    /**
     * 节点流转信息
     */
    @ApiModelProperty("节点流转信息")
    private SequenceFlowInfo sequence;


}
