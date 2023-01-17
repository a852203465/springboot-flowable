package cn.darkjrong.workflow.flowable.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import cn.darkjrong.workflow.flowable.domain.ProcessInstanceInfo;
import cn.darkjrong.workflow.flowable.image.CustomProcessDiagramGenerator;
import cn.darkjrong.workflow.flowable.image.svg.DefaultSvgProcessDiagramGenerator;
import cn.darkjrong.workflow.flowable.image.svg.SvgProcessDiagramGenerator;
import cn.darkjrong.workflow.flowable.service.FlowableHistoricService;
import cn.darkjrong.workflow.flowable.service.FlowableProcessInstanceService;
import cn.darkjrong.workflow.flowable.service.FlowableTaskService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.spring.boot.FlowableProperties;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Flowable过程实例服务实现类
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
@Slf4j
@Service
public class FlowableProcessInstanceServiceImpl extends FlowableFactory implements FlowableProcessInstanceService {

    @Autowired
    private FlowableTaskService flowableTaskService;

    @Autowired
    private FlowableHistoricService flowableHistoricService;

    @Autowired
    private FlowableProperties flowableProperties;

    @Override
    public ProcessInstanceQuery createProcessInstanceQuery() {
        return runtimeService.createProcessInstanceQuery();
    }

    @Override
    public ProcessInstance queryProcessInstanceById(String processInstanceId) {
        return createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    @Override
    public ProcessInstance queryProcessInstanceByBusinessKey(String processInstanceBusinessKey) {
        return createProcessInstanceQuery().processInstanceBusinessKey(processInstanceBusinessKey).singleResult();
    }

    @Override
    public boolean hasProcessInstanceFinished(String processInstanceId) {
        long count = historyService.createHistoricProcessInstanceQuery().unfinished().processInstanceId(processInstanceId).count();
        return count == 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public ProcessInstance queryProcessInstanceByTaskId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstId = task.getProcessInstanceId();
        return this.queryProcessInstanceById(processInstId);
    }

    @Override
    public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey);
    }

    @Override
    public ProcessInstance startProcessInstanceById(String processDefinitionId) {
        return runtimeService.startProcessInstanceById(processDefinitionId);
    }

    @Override
    public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceById(processDefinitionId, variables);
    }

    @Override
    public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
    }

    @Override
    public ProcessInstance startProcessInstanceByKeyAndTenantId(String processDefinitionKey, String tenantId, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKeyAndTenantId(processDefinitionKey, variables, tenantId);
    }

    @Override
    public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
    }

    @Override
    public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String businessKey) {
        return startProcessInstanceByKey(processDefinitionKey, businessKey, null);
    }

    @Override
    public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceById(processDefinitionId, businessKey, variables);
    }

    @Override
    public ProcessInstance startProcessInstanceById(String processDefinitionId, String businessKey) {
        return startProcessInstanceById(processDefinitionId, businessKey, null);
    }

    @Override
    public ProcessInstance startProcessInstanceByKeyAndTenantId(String processDefinitionKey, String businessKey, String tenantId, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKeyAndTenantId(processDefinitionKey, businessKey, variables, tenantId);
    }

    @Override
    public void suspendProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    public void deleteProcessInstance(String processInstanceId, String deleteReason) {
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    @Override
    public void setAuthenticatedUserId(String authenticatedUserId) {
        identityService.setAuthenticatedUserId(authenticatedUserId);
    }

    @Override
    public ProcessInstance startInstanceAndExecuteFirstTaskByProcessDefinitionKey(String processDefinitionKey, Map<String, Object> variables, Map<String, Object> actorIds) {
        ProcessInstance pi = startProcessInstanceByKey(processDefinitionKey, variables);
        Task task = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).active().singleResult();
        taskService.complete(task.getId(), actorIds);
        return pi;
    }

    @Override
    public void activateProcessInstanceById(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    @Override
    public void startInstanceAndExecuteFirstTaskByProcessDefinitionKey(String processDefinitionKey, String tenantId, String userId, Map<String, Object> variables) {
        ProcessInstance pi = StrUtil.isNotBlank(tenantId) ? runtimeService.startProcessInstanceByKeyAndTenantId(processDefinitionKey, variables, tenantId)
                : runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
        Task task = flowableTaskService.queryTaskByProcessInstanceId(pi.getProcessInstanceId());

        String id = task.getId();
        flowableTaskService.assignTask(id, userId);
        flowableTaskService.setOwner(id, userId);
        task.setAssignee(userId);
        task.setOwner(userId);
        taskService.complete(id, variables);
    }

    @Override
    public PageInfo<ProcessInstanceInfo> queryMindProcessInstance(String assignee, Integer pageNum, Integer pageSize, Boolean isFinished, Date before, Date after) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().startedBy(assignee);
        if (ObjectUtil.isNotNull(isFinished)) {
            if (isFinished) {
                query.finished();
            } else {
                query.unfinished();
            }
        }
        Optional.ofNullable(before).ifPresent(query::startedBefore);
        Optional.ofNullable(after).ifPresent(query::startedAfter);

        long count = query.count();
        List<HistoricProcessInstance> historicProcessInstances = query.listPage((pageNum - 1) * pageSize, pageSize);
        List<ProcessInstanceInfo> list = new ArrayList<>();
        for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
            list.add(queryProcessInstance(historicProcessInstance.getId()));
        }
        PageInfo<ProcessInstanceInfo> pageInfo = new PageInfo<>(list);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(count);
        pageInfo.setList(list);

        return pageInfo;
    }

    @Override
    public ProcessInstanceInfo queryProcessInstance(String processInstanceId) {

        //历史流程实例
        HistoricProcessInstance historicProcessInstance = flowableHistoricService.queryHistoricProcessInstance(processInstanceId);

        Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(historicProcessInstance.getDeploymentId())
                .singleResult();

        //运行中流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        ProcessInstanceInfo processInstanceInfo = new ProcessInstanceInfo();
        processInstanceInfo.setDeploymentId(deployment.getId());
        processInstanceInfo.setDeploymentKey(deployment.getKey());
        processInstanceInfo.setDeploymentName(deployment.getName());
        processInstanceInfo.setId(historicProcessInstance.getId());
        processInstanceInfo.setProcessDefinitionName(historicProcessInstance.getProcessDefinitionName());
        processInstanceInfo.setProcessDefinitionId(historicProcessInstance.getProcessDefinitionId());
        processInstanceInfo.setProcessDefinitionKey(historicProcessInstance.getProcessDefinitionKey());
        processInstanceInfo.setBusinessKey(historicProcessInstance.getBusinessKey());
        processInstanceInfo.setStartTime(historicProcessInstance.getStartTime());
        processInstanceInfo.setStartUserId(historicProcessInstance.getStartUserId());

        if (ObjectUtil.isNull(processInstance)) {
            processInstanceInfo.setFinished(Boolean.TRUE);
        } else {
            processInstanceInfo.setFinished(Boolean.FALSE);

            //查出当前审批节点
            List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .unfinished()
                    .list();

            processInstanceInfo.setCurrentAssignee(historicTaskInstances.stream().map(HistoricTaskInstance::getAssignee).collect(Collectors.toSet()));
            processInstanceInfo.setCurrentTaskName(historicTaskInstances.get(0).getName());
        }
        return processInstanceInfo;
    }

    @Override
    public List<Comment> queryProcessComments(String processInstanceId) {
        List<Comment> historyCommnets = new ArrayList<>();

        List<HistoricActivityInstance> hais = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).activityType("userTask").list();
        for (HistoricActivityInstance hai : hais) {
            String historytaskId = hai.getTaskId();
            List<Comment> comments = taskService.getTaskComments(historytaskId);
            Optional.ofNullable(comments).ifPresent(historyCommnets::addAll);
        }
        return historyCommnets;
    }

    @Override
    public byte[] generateFlowImgByProcInstId(String processInstanceId) {

        String processDefinitionId;
        // 获取当前的流程实例
        ProcessInstance processInstance = queryProcessInstanceById(processInstanceId);
        // 如果流程已经结束，则得到结束节点
        if (ObjectUtil.isNull(processInstance)) {
            HistoricProcessInstance pi = flowableHistoricService.queryHistoricProcessInstance(processInstanceId);
            processDefinitionId = pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }

        // 获得活动的节点
        List<HistoricActivityInstance> highLightedFlowList = flowableHistoricService.queryHistoricActivityInstanceAsc(processInstanceId);

        List<String> highLightedFlows = new ArrayList<>();
        List<String> highLightedNodes = new ArrayList<>();
        //高亮
        for (HistoricActivityInstance tempActivity : highLightedFlowList) {
            if ("sequenceFlow".equals(tempActivity.getActivityType())) {
                //高亮线
                highLightedFlows.add(tempActivity.getActivityId());
            } else {
                //高亮节点
                if (tempActivity.getEndTime() == null) {
                    highLightedNodes.add(Color.RED.toString() + tempActivity.getActivityId());
                } else {
                    highLightedNodes.add(tempActivity.getActivityId());
                }
            }
        }
        List<String> highLightedNodeList = new ArrayList<>();
        //运行中的节点
        List<String> redNodeCollect = highLightedNodes.stream().filter(e -> e.contains(Color.RED.toString())).collect(Collectors.toList());
        //排除与运行中相同的节点
        for (String nodeId : highLightedNodes) {
            if (!nodeId.contains(Color.RED.toString()) && !redNodeCollect.contains(Color.RED + nodeId)) {
                highLightedNodeList.add(nodeId);
            }
        }
        highLightedNodeList.addAll(redNodeCollect);
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

        ProcessDiagramGenerator diagramGenerator = new CustomProcessDiagramGenerator();
        InputStream inputStream = diagramGenerator.generateDiagram(bpmnModel, ImgUtil.IMAGE_TYPE_JPEG, highLightedNodeList,
                highLightedFlows, flowableProperties.getActivityFontName(), flowableProperties.getLabelFontName(),
                flowableProperties.getAnnotationFontName(), null, 1.0, Boolean.TRUE);
        try {
            return IoUtil.readBytes(inputStream);
        } catch (Exception e) {
            log.error("通过流程实例ID[{}]获取流程图时出现异常！", e.getMessage());
            throw new FlowableException("通过流程实例ID" + processInstanceId + "获取流程图时出现异常！", e);
        } finally {
            IoUtil.close(inputStream);
        }
    }

    @Override
    public byte[] generateFlowSvgByProcInstId(String processInstanceId) {

        // 获取历史流程实例
        HistoricProcessInstance historicProcessInstance = flowableHistoricService.queryHistoricProcessInstance(processInstanceId);

        // 获取流程中已经执行的节点，按照执行先后顺序排序
        List<HistoricActivityInstance> historicActivityInstances = flowableHistoricService.queryHistoricActivityInstanceAsc(processInstanceId);

        // 高亮已经执行流程节点ID集合
        List<String> highLightedActivitiIds = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            //如果历史节点中有结束节点，则高亮结束节点
            if ("endEvent".equalsIgnoreCase(historicActivityInstance.getActivityType())) {
                highLightedActivitiIds.add(historicActivityInstance.getActivityId());
            }
            //如果没有结束时间，则是正在执行节点
            Date endTime = historicActivityInstance.getEndTime();
            if (endTime == null) {
                highLightedActivitiIds.add(historicActivityInstance.getActivityId() + "#");
            } else {
                // 已完成节点
                highLightedActivitiIds.add(historicActivityInstance.getActivityId());
            }
        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
        // 高亮流程已发生流转的线id集合
        List<String> highLightedFlowIds = getHighLightedFlows(bpmnModel, historicActivityInstances);

        SvgProcessDiagramGenerator svg = new DefaultSvgProcessDiagramGenerator();
        InputStream inputStream = svg.generateDiagram(bpmnModel, highLightedActivitiIds, highLightedFlowIds,
                flowableProperties.getActivityFontName(), flowableProperties.getLabelFontName(),
                flowableProperties.getAnnotationFontName());
        try {
            return IoUtil.readBytes(inputStream);
        } catch (Exception e) {
            log.error("通过流程实例ID[{}]获取流程图时出现异常！", e.getMessage());
            throw new FlowableException("通过流程实例ID" + processInstanceId + "获取流程图时出现异常！", e);
        } finally {
            IoUtil.close(inputStream);
        }
    }

    @Override
    public Execution getExecution(String executionId) {
        return runtimeService.createExecutionQuery().executionId(executionId).singleResult();
    }

    /**
     * 获取已经流转的线
     *
     * @param bpmnModel bpmnModel
     * @param historicActivityInstances 历史流转实现集合
     * @return 已经流转的线
     */
    private List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {
        // 高亮流程已发生流转的线id集合
        List<String> highLightedFlowIds = new ArrayList<>();
        // 全部活动节点
        List<FlowNode> historicActivityNodes = new ArrayList<>();
        // 已完成的历史活动节点
        List<HistoricActivityInstance> finishedActivityInstances = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId(), true);
            if (flowElement instanceof FlowNode) {
                FlowNode flowNode = (FlowNode) flowElement;
                historicActivityNodes.add(flowNode);
                if (historicActivityInstance.getEndTime() != null) {
                    finishedActivityInstances.add(historicActivityInstance);
                }
            }
        }

        FlowNode currentFlowNode;
        FlowNode targetFlowNode;
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (HistoricActivityInstance currentActivityInstance : finishedActivityInstances) {
            // 获得当前活动对应的节点信息及outgoingFlows信息
            currentFlowNode = (FlowNode) bpmnModel.getMainProcess()
                    .getFlowElement(currentActivityInstance.getActivityId(), true);
            List<SequenceFlow> sequenceFlows = currentFlowNode.getOutgoingFlows();

            /**
             * 遍历outgoingFlows并找到已已流转的 满足如下条件认为已已流转：
             * 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
             * 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
             */
            if ("parallelGateway".equals(currentActivityInstance.getActivityType())
                    || "inclusiveGateway".equals(currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef(), true);
                    if (historicActivityNodes.contains(targetFlowNode)) {
                        highLightedFlowIds.add(targetFlowNode.getId());
                    }
                }
            } else {
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
                        if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                            Map<String, Object> map = new HashMap<>(16);
                            map.put("highLightedFlowId", sequenceFlow.getId());
                            map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                            tempMapList.add(map);
                        }
                    }
                }

                if (!CollectionUtil.isEmpty(tempMapList)) {
                    // 遍历匹配的集合，取得开始时间最早的一个
                    long earliestStamp = 0L;
                    String highLightedFlowId = null;
                    for (Map<String, Object> map : tempMapList) {
                        long highLightedFlowStartTime = Long.parseLong(map.get("highLightedFlowStartTime").toString());
                        if (earliestStamp == 0 || earliestStamp >= highLightedFlowStartTime) {
                            highLightedFlowId = map.get("highLightedFlowId").toString();
                            earliestStamp = highLightedFlowStartTime;
                        }
                    }
                    highLightedFlowIds.add(highLightedFlowId);
                }

            }
        }
        return highLightedFlowIds;
    }





}
