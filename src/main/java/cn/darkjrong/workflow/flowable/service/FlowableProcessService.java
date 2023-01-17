package cn.darkjrong.workflow.flowable.service;

import com.github.pagehelper.PageInfo;
import cn.darkjrong.workflow.flowable.domain.ValidationErrorInfo;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.repository.*;

import java.io.InputStream;
import java.util.List;

/**
 * Flowable 流程服务
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
public interface FlowableProcessService {

    /**
     * 创建流程部署对象
     *
     * @return 查询对象
     */
    DeploymentBuilder createDeployment();

    /**
     * 创建流程部署查询对象
     *
     * @return 查询对象
     */
    DeploymentQuery createDeploymentQuery();

    /**
     * 创建流程定义查询对象
     *
     * @return 查询对象
     */
    ProcessDefinitionQuery createProcessDefinitionQuery();

    /**
     * 部署流程定义
     *
     * @param url 流程定义文件URL
     * @return {@link ProcessDefinition}
     */
    ProcessDefinition deploy(String url);

    /**
     * 部署流程定义
     *
     * @param url    流程定义文件URL
     * @param pngUrl 流程定义文件pngUrl
     * @return {@link ProcessDefinition}
     */
    ProcessDefinition deploy(String url, String pngUrl);

    /**
     * 部署流程定义
     *
     * @param url      流程定义文件URL
     * @param pngUrl   流程定义文件pngUrl
     * @param name     流程定义标识
     * @return {@link ProcessDefinition}
     */
    ProcessDefinition deploy(String url, String pngUrl, String name);

    /**
     * 部署流程定义
     *
     * @param url      流程定义文件URL
     * @param pngUrl   流程定义文件pngUrl
     * @param name     流程定义标识
     * @param key 流程定义KEY
     * @return {@link ProcessDefinition}
     */
    ProcessDefinition deploy(String url, String pngUrl, String name, String key);

    /**
     * 部署流程定义
     *
     * @param url     流程定义文件输入流
     * @param urlName 流程定义文件名
     * @param name    流程定义标识
     * @return {@link ProcessDefinition}
     */
    ProcessDefinition deploy(InputStream url, String urlName, String name);

    /**
     * 部署流程定义
     *
     * @param url     流程定义文件输入流
     * @param pngUrl  流程定义图片文件输入流
     * @param pngName 流程定义图片文件名
     * @param urlName 流程定义文件名
     * @param name    流程定义标识
     * @return {@link ProcessDefinition}
     */
    ProcessDefinition deploy(InputStream url, InputStream pngUrl, String urlName, String pngName, String name);

    /**
     * 部署流程定义
     *
     * @param url    流程定义文件输入流
     * @param pngUrl 流程定义图片文件输入流
     * @param pngName 流程定义图片文件名
     * @param urlName  流程定义文件名
     * @param name   流程定义标识
     * @param key    流程定义KEY
     * @return {@link ProcessDefinition}
     */
    ProcessDefinition deploy(InputStream url, InputStream pngUrl, String urlName, String pngName, String name, String key);

    /**
     * 根据流程定义key，判断流程定义（模板）是否已经部署过
     *
     * @param processDefinitionKey 流程定义key（即：流程模板ID）
     * @return {@link Boolean}
     */
    Boolean exist(String processDefinitionKey);

    /**
     * 根据流程定义key，查询流程定义信息
     *
     * @param processDefinitionKey 流程定义key（即：流程模板ID）
     * @return {@link ProcessDefinition}
     */
    ProcessDefinition queryByProcessDefinitionKey(String processDefinitionKey);

    /**
     * 给流程定义授权用户
     *
     * @param processDefinitionId 流程定义ID
     * @param userId               流程定义key
     */
    void addCandidateStarterUser(String processDefinitionId, String userId);

    /**
     * 取消部署
     *
     * @param deploymentId 流程部署ID
     * @param cascade       是否级联删除所有关联的流程及其历史记录
     */
    void undeploy(String deploymentId, Boolean cascade);

    /**
     * 取消部署
     *
     * @param deploymentId 流程部署ID
     */
    void undeploy(String deploymentId);

    /**
     * 查询流程定义信息
     *
     * @param pageNum  页号
     * @param pageSize 分页大小
     * @return {@link PageInfo}<{@link ProcessDefinition}>
     */
    PageInfo<ProcessDefinition> queryProcessDefinition(Integer pageNum, Integer pageSize);

    /**
     * 查询流程定义信息
     *
     * @param processDefinitionId 流程定义ID
     * @return {@link ProcessDefinition}
     */
    ProcessDefinition queryProcessDefinition(String processDefinitionId);

    /**
     * 查询部署信息
     *
     * @param deploymentId 部署id
     * @return {@link Deployment}
     */
    Deployment queryDeployment(String deploymentId);

    /**
     * 下载部署配置
     *
     * @param processDefinitionId 流程定义id
     * @return {@link byte[]}
     */
    byte[] downloadDeploy(String processDefinitionId);

    /**
     * 验证流程
     *
     * @param inputStream 流程图字节流
     * @return {@link List}<{@link ValidationErrorInfo}>
     */
    List<ValidationErrorInfo> validateProcess(InputStream inputStream);

    /**
     * 验证流程
     *
     * @param bpmXml bpmn XML
     * @return {@link List}<{@link ValidationErrorInfo}>
     */
    List<ValidationErrorInfo> validateProcess(String bpmXml);

    /**
     * 获取bpmn模型
     *
     * @param processDefinitionId 过程定义ID
     * @return {@link BpmnModel}
     */
    BpmnModel getBpmnModel(String processDefinitionId);

    /**
     * 根据流程定义ID,获取流程图片
     * @param processDefinitionId 流程定义ID
     * @return png 图片数组
     */
    byte[] generateFlowImgByProcessDefinitionId(String processDefinitionId);

    /**
     * 根据流程定义ID,获取流程SVG
     *
     * @param processDefinitionId 流程定义ID
     * @return {@link byte[]} SVG
     */
    byte[] generateFlowSvgByProcessDefinitionId(String processDefinitionId);






}
