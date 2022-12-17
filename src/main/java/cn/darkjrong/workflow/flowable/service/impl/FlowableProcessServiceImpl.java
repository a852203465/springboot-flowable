package cn.darkjrong.workflow.flowable.service.impl;

import cn.darkjrong.workflow.flowable.domain.ValidationErrorInfo;
import cn.darkjrong.workflow.flowable.image.CustomProcessDiagramGenerator;
import cn.darkjrong.workflow.flowable.image.svg.DefaultSvgProcessDiagramGenerator;
import cn.darkjrong.workflow.flowable.image.svg.SvgProcessDiagramGenerator;
import cn.darkjrong.workflow.flowable.service.FlowableProcessService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.repository.*;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.spring.boot.FlowableProperties;
import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ValidationError;
import org.flowable.validation.validator.ProcessLevelValidator;
import org.flowable.validation.validator.ValidatorSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Flowable过程服务实现类
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
@Slf4j
@Service
public class FlowableProcessServiceImpl extends FlowableFactory implements FlowableProcessService {

    private static final String CLASS_PATH = "classpath:";

    @Autowired
    private FlowableProperties flowableProperties;

    @Override
    public DeploymentBuilder createDeployment() {
        return repositoryService.createDeployment();
    }

    @Override
    public DeploymentQuery createDeploymentQuery() {
        return repositoryService.createDeploymentQuery();
    }

    @Override
    public ProcessDefinitionQuery createProcessDefinitionQuery() {
        return repositoryService.createProcessDefinitionQuery();
    }

    @Override
    public ProcessDefinition deploy(String bpmnFileUrl) {
        return deploy(bpmnFileUrl, null);
    }

    @Override
    public ProcessDefinition deploy(String url, String pngUrl) {
        return deploy(url, pngUrl, FileUtil.getName(url), null);
    }

    @Override
    public ProcessDefinition deploy(String url, String pngUrl, String name) {
        return deploy(url, pngUrl, name, IdUtil.objectId());
    }

    @Override
    public ProcessDefinition deploy(String url, String pngUrl, String name, String key) {
        List<Deployment> deployments = createDeploymentQuery().deploymentKey(key).list();
        Assert.isFalse(CollectionUtil.isNotEmpty(deployments), "重复部署");

        DeploymentBuilder builder = createDeployment().key(key);
        if (StrUtil.isNotBlank(name)) {
            builder.name(name);
        }

        try {
            File file = StrUtil.startWith(url, CLASS_PATH) ? ResourceUtils.getFile(CLASS_PATH + url) : new File(url);
            builder.addInputStream(file.getName(), FileUtil.getInputStream(file));
            if (StrUtil.isNotBlank(pngUrl)) {
                File image = StrUtil.startWith(pngUrl, CLASS_PATH) ? ResourceUtils.getFile(CLASS_PATH + pngUrl) : new File(pngUrl);
                if (FileUtil.exist(image)) {
                    builder.addInputStream(image.getName(), FileUtil.getInputStream(image));
                }
            }
        } catch (FileNotFoundException e) {
            log.error("流程文件不存在 {}", e.getMessage());
            throw new FlowableException(e.getMessage());
        }
        return createProcessDefinitionQuery().deploymentId(builder.deploy().getId()).singleResult();
    }

    @Override
    public ProcessDefinition deploy(InputStream url, String urlName, String name) {
        return deploy(url, null, urlName, null, name);
    }

    @Override
    public ProcessDefinition deploy(InputStream url, InputStream pngUrl, String urlName, String pngName, String name) {
        return deploy(url, pngUrl, urlName, pngName, name, IdUtil.objectId());
    }

    @Override
    public ProcessDefinition deploy(InputStream url, InputStream pngUrl, String urlName, String pngName, String name, String key) {
        List<Deployment> deployments = createDeploymentQuery().deploymentKey(key).list();
        Assert.isFalse(CollectionUtil.isNotEmpty(deployments), "重复部署");

        Assert.isFalse(ObjectUtil.isNull(url) || StrUtil.isBlank(urlName), "流程文件不存在");

        DeploymentBuilder builder = createDeployment().key(key);
        if (StrUtil.isNotBlank(name)) {
            builder.name(name);
        }

        builder.addInputStream(urlName, url);
        if (ObjectUtil.isAllNotEmpty(pngUrl, pngName)) {
            builder.addInputStream(pngName, pngUrl);
        }

        return createProcessDefinitionQuery().deploymentId(builder.deploy().getId()).singleResult();
    }

    @Override
    public Boolean exist(String processDefinitionKey) {
        return createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).count() > 0;
    }

    @Override
    public ProcessDefinition queryByProcessDefinitionKey(String processDefinitionKey) {
        return createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).active().singleResult();
    }

    @Override
    public void addCandidateStarterUser(String processDefinitionId, String userId) {
        repositoryService.addCandidateStarterUser(processDefinitionId, userId);
    }

    @Override
    public void undeploy(String deploymentId, Boolean cascade) {
        Deployment deployment = createDeploymentQuery().deploymentId(deploymentId).singleResult();
        Optional.ofNullable(deployment).ifPresent(a -> repositoryService.deleteDeployment(a.getId(), cascade));
    }

    @Override
    public void undeploy(String deploymentId) {
        this.undeploy(deploymentId, Boolean.TRUE);
    }

    @Override
    public PageInfo<ProcessDefinition> queryProcessDefinition(Integer pageNum, Integer pageSize) {
        ProcessDefinitionQuery query = createProcessDefinitionQuery();
        long count = query.count();
        List<ProcessDefinition> processDefinitions = query.listPage((pageNum - 1) * pageSize, pageSize);
        PageInfo<ProcessDefinition> pageInfo = new PageInfo<>(processDefinitions);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(count);
        pageInfo.setList(processDefinitions);
        return pageInfo;
    }

    @Override
    public ProcessDefinition queryProcessDefinition(String processDefinitionId) {
        return createProcessDefinitionQuery().processDefinitionId(processDefinitionId).active().singleResult();
    }

    @Override
    public Deployment queryDeployment(String deploymentId) {
        return createDeploymentQuery().deploymentId(deploymentId).singleResult();
    }

    @Override
    public byte[] downloadDeploy(String processDefinitionId) {
        ProcessDefinition processDefinition = queryProcessDefinition(processDefinitionId);
        Assert.notNull(processDefinition, "流程配置不存在");

        String deploymentId = processDefinition.getDeploymentId();
        return StrUtil.isNotBlank(processDefinition.getResourceName()) ?
                IoUtil.readBytes(repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName())) : null;
    }

    @Override
    public List<ValidationErrorInfo> validateProcess(InputStream inputStream) {
        Assert.notNull(inputStream, "流程不存在, 请检查");

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = null;
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            log.error("流程读取异常", e);
            throw new FlowableException("流程文件读取异常, 请检查文件是否存在");
        }
        BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
        BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xmlStreamReader);
        // 模板验证
        ProcessValidator validator = new ProcessValidatorFactory().createDefaultProcessValidator();
        ValidatorSet validatorSet = new ValidatorSet("custom-flowable-executable-process");
        validator.getValidatorSets().forEach(a -> a.getValidators().forEach(validatorSet::addValidator));
        validatorSet.addValidator(new CustomFlowableValidator());
        validator.getValidatorSets().add(validatorSet);
        return getValidationError(validator.validate(bpmnModel));
    }

    @Override
    public List<ValidationErrorInfo> validateProcess(String bpmXml) {

        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xtr = null;
        try {
            xtr = xif.createXMLStreamReader(new ByteArrayInputStream(StrUtil.bytes(bpmXml, StandardCharsets.UTF_8)), CharsetUtil.UTF_8);
        } catch (XMLStreamException e) {
            log.error("流程文件读取异常", e);
            throw new FlowableException("流程文件读取异常, 请检查文件是否存在");
        }

        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
        return getValidationError(repositoryService.validateProcess(bpmnModel));
    }

    @Override
    public BpmnModel getBpmnModel(String processDefinitionId) {
        return repositoryService.getBpmnModel(processDefinitionId);
    }

    @Override
    public byte[] generateFlowImgByProcessDefinitionId(String processDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if(ObjectUtil.isNotNull(bpmnModel) && bpmnModel.getLocationMap().size() > 0){

            ProcessDiagramGenerator diagramGenerator = new CustomProcessDiagramGenerator();
            InputStream inputStream = diagramGenerator.generateDiagram(bpmnModel, ImgUtil.IMAGE_TYPE_JPEG, new ArrayList<>(),
                    new ArrayList<>(), flowableProperties.getActivityFontName(), flowableProperties.getLabelFontName(),
                    flowableProperties.getAnnotationFontName(), null, 1.0, Boolean.TRUE);
            try {
               return IoUtil.readBytes(inputStream);
            }catch (Exception e) {
                log.error("通过流程定义ID[{}]获取流程图时出现异常！", e.getMessage());
                throw new FlowableException("通过流程定义ID" + processDefinitionId + "获取流程图时出现异常！", e);
            }

        }
        throw new FlowableException("流程定义ID " + processDefinitionId + "的流程定义图不存在，请检查");
    }

    @Override
    public byte[] generateFlowSvgByProcessDefinitionId(String processDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if(ObjectUtil.isNotNull(bpmnModel) && bpmnModel.getLocationMap().size() > 0){
            SvgProcessDiagramGenerator ge = new DefaultSvgProcessDiagramGenerator();
            InputStream inputStream = ge.generateDiagram(bpmnModel, new ArrayList<>(), new ArrayList<>(), flowableProperties.getActivityFontName(), flowableProperties.getLabelFontName(), flowableProperties.getAnnotationFontName(), false);
            try {
              return IoUtil.readBytes(inputStream);
            }catch (Exception e) {
                log.error("通过流程定义ID[{}]获取流程图时出现异常！", e.getMessage());
                throw new FlowableException("通过流程定义ID" + processDefinitionId + "获取流程图时出现异常！", e);
            }
        }
        throw new FlowableException("流程定义ID " + processDefinitionId + "的流程定义图不存在，请检查");
    }

    /**
     * 自定义规则校验器
     *
     * @author Rong.Jia
     * @date 2022/12/05
     */
    private static class CustomFlowableValidator extends ProcessLevelValidator {
        private static final String NULL = null;
        @Override
        protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
            List<FlowNode> flowNodes = process.findFlowElementsOfType(FlowNode.class);
            if (CollectionUtil.isEmpty(flowNodes)) {
                errors.add(validationErrorFactory(NULL, NULL, "can not find Flow Node", Boolean.TRUE));
            }else {
                flowNodes.stream().filter(task -> !(task instanceof EndEvent) && CollectionUtil.isEmpty(task.getOutgoingFlows()))
                        .forEach(s -> errors.add(validationErrorFactory(s.getId(), s.getName(), "Flow Node missing outgoing flows", Boolean.TRUE)));

                flowNodes.stream().filter(task -> !(task instanceof StartEvent) && CollectionUtil.isEmpty(task.getIncomingFlows()))
                        .forEach(s -> errors.add(validationErrorFactory(s.getId(), s.getName(), "Flow Node missing incoming flows", Boolean.TRUE)));
            }

            List<UserTask> userTasks = process.findFlowElementsOfType(UserTask.class);
            if (CollectionUtil.isEmpty(userTasks)) {
                errors.add(validationErrorFactory(NULL, NULL, "can not find UserTask", Boolean.TRUE));
            } else {
                userTasks.stream().filter(task -> StrUtil.isBlank(task.getName()))
                        .forEach(s -> errors.add(validationErrorFactory(s.getId(), s.getName(), "UserTask missing name", Boolean.TRUE)));

                userTasks.stream().filter(task -> StrUtil.isBlank(task.getAssignee())
                        && CollectionUtil.isEmpty(task.getCandidateUsers())
                        && CollectionUtil.isEmpty(task.getCandidateGroups()))
                        .forEach(s -> errors.add(validationErrorFactory(s.getId(), s.getName(), "can not find UserTask recipient", Boolean.FALSE)));
            }

            //路线定义规则，如果该线路上一步是排他分支，则需要指定规则
            List<SequenceFlow> sequenceFlowList = process.findFlowElementsOfType(SequenceFlow.class);

            sequenceFlowList.forEach(s->{
                FlowElement flow = process.getFlowElements().stream().filter(p->p.getId().equals(s.getSourceRef())).collect(Collectors.toList()).get(0);
                if(flow instanceof ExclusiveGateway && ObjectUtil.isNull(((ExclusiveGateway) flow).getDefaultFlow()) && StrUtil.isBlank(s.getConditionExpression())){
                    String sourceName = flow.getName();
                    String targetName = process.getFlowElements().stream().filter(p->p.getId().equals(s.getTargetRef())).collect(Collectors.toList()).get(0).getName();
                    errors.add(validationErrorFactory(s.getId(),s.getName(),sourceName + "至" + targetName + "no rules found",Boolean.FALSE));
                }
            });

            List<StartEvent> startEvents = process.findFlowElementsOfType(StartEvent.class);
            if (CollectionUtil.isEmpty(startEvents)) {
                errors.add(validationErrorFactory(NULL, NULL, "can not find StartEvent", Boolean.TRUE));
            }

            List<EndEvent> endEvents = process.findFlowElementsOfType(EndEvent.class);
            if (CollectionUtil.isEmpty(endEvents)) {
                errors.add(validationErrorFactory(NULL, NULL, "can not find EndEvent", Boolean.TRUE));
            }
        }

        private ValidationError validationErrorFactory(String activityId, String activityName, String msg, boolean warning) {
            ValidationError validationError = new ValidationError();
            validationError.setActivityId(activityId);
            validationError.setActivityName(activityName);
            validationError.setDefaultDescription(msg);
            validationError.setWarning(warning);
            return validationError;
        }
    }

    /**
     * 获取验证错误
     *
     * @param validationErrors 验证错误
     * @return {@link List}<{@link ValidationErrorInfo}>
     */
    private List<ValidationErrorInfo> getValidationError(List<ValidationError> validationErrors) {
        if (CollectionUtil.isEmpty(validationErrors)) return Collections.emptyList();
        return validationErrors.stream()
                .map(a -> BeanUtil.copyProperties(a, ValidationErrorInfo.class))
                .collect(Collectors.toList());
    }

}
