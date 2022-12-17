package cn.darkjrong.workflow.flowable.domain;

import lombok.Data;

/**
 * 流程定义信息
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
@Data
public class ProcessDefinitionInfo {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 类别
     */
    private String category;

    /**
     * 名称
     */
    private String name;

    /**
     *流程定义KEY
     */
    private String key;

    /**
     * 描述
     */
    private String description;

    /**
     * 版本
     */
    int version;

    /**
     * 资源名
     */
    private String resourceName;

    /**
     * 流程部署信息
     */
    private DeploymentInfo deployment;

    /**
     * 图片名称
     */
    private String diagramResourceName;

    /**
     * 是否从key启动，0否1是
     */
    private Boolean startFormKey;

    /**
     * 是否挂起，1激活 2挂起
     */
    private Boolean suspended;

    /**
     * 租户ID
     */
    private String getTenantId;

    private Boolean graphicalNotation;

















}
