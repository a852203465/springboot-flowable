package cn.darkjrong.workflow.flowable.domain;

import lombok.Data;

/**
 * 部署信息
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
@Data
public class DeploymentInfo {

    /**
     * ID
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * KEY
     */
    private String key;

}
