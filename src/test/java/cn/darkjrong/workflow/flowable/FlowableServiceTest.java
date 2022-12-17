package cn.darkjrong.workflow.flowable;

import cn.darkjrong.workflow.SpringbootFlowableApplicationTests;
import cn.darkjrong.workflow.flowable.domain.*;
import cn.darkjrong.workflow.flowable.service.FlowableHistoricService;
import cn.darkjrong.workflow.flowable.service.FlowableProcessInstanceService;
import cn.darkjrong.workflow.flowable.service.FlowableProcessService;
import cn.darkjrong.workflow.flowable.service.FlowableTaskService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FlowableServiceTest extends SpringbootFlowableApplicationTests {

    @Autowired
    private FlowableHistoricService flowableHistoricService;

    @Autowired
    private FlowableProcessInstanceService flowableProcessInstanceService;

    @Autowired
    private FlowableProcessService flowableProcessService;

    @Autowired
    private FlowableTaskService flowableTaskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    protected ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Test
    void deploy() {

        File file = new File("C:\\Users\\Rong.Jia\\Desktop\\evection.bpmn20.xml");

        ProcessDefinition processDefinition = flowableProcessService.deploy("出差申请单");

        System.out.println("id : " + processDefinition.getId());
        System.out.println("key : " + processDefinition.getKey());
        System.out.println("name : " + processDefinition.getName());
        System.out.println("depId : " + processDefinition.getDeploymentId());
        System.out.println("version : " + processDefinition.getVersion());
    }

    @Test
    void undeploy() {
        flowableProcessService.undeploy("6283065595497d5502be68c9", Boolean.TRUE);
    }

    @Test
    void getProcessDefs() {
        PageInfo<ProcessDefinition> processDefs = flowableProcessService.queryProcessDefinition(1, 10);
        System.out.println(processDefs.toString());
    }

    @Test
    void startProcessInstance(){

        ProcessInstance processInstance = flowableProcessInstanceService.startProcessInstanceById("evection:1:3startProcessInstanceByProcessDefinitionIdc542a56-d588-11ec-9356-005056c00008");
        System.out.println(processInstance.getProcessInstanceId());
        System.out.println(processInstance.getName());
    }

    @Test
    void getTaskDetails() {

        TaskInfo taskDetails = flowableTaskService.queryTaskInfo("5c66a3d8-d590-11ec-b0db-005056c00008");
        System.out.println(JSON.toJSONString(taskDetails));


    }

    @Test
    void getAssigneeTasks() {
        PageInfo<TaskInfo> assigneeTasks = flowableTaskService.queryAssigneeTasks("张三", 1, 20);

        System.out.println(assigneeTasks.toString());
    }

    @Test
    void disposeTask() {
        flowableTaskService.complete("5c66a3d8-d590-11ec-b0db-005056c00008", null);
    }

    @Test
    void processInstanceDetail() {
        ProcessInstanceInfo processInstanceInfo = flowableProcessInstanceService.queryProcessInstance("5c628524-d590-11ec-b0db-005056c00008");
        System.out.println(JSON.toJSONString(processInstanceInfo));
    }

    @Test
    void getFlowImgByInstanceId() {
        byte[] bytes = flowableProcessInstanceService.generateFlowImgByProcInstId("4f7af5f9-7784-11ed-8d21-005056c00008");
        FileUtil.writeBytes(bytes, "F:\\1.svg");


    }

    @Test
    void getFlowImgByInstanceIdImageType() {
        byte[] bytes = flowableProcessInstanceService.generateFlowImgByProcInstId("4f7af5f9-7784-11ed-8d21-005056c00008");
        FileUtil.writeBytes(bytes, "F:\\1.jpeg");


    }

    @Test
    void getHistoricTask() {
        HistoricTaskInfo historicTask = flowableHistoricService.queryHistoricTask("42084ee9-d649-11ec-819a-005056c00008");
        System.out.println(JSON.toJSONString(historicTask));

    }

    @Test
    void getHistoricActivity() {
        HistoricActivityInfo historicActivity = flowableHistoricService.queryHistoricActivity("sid-3b943bd0-7a69-48a1-907d-7adeed79021f");
        System.out.println(JSON.toJSONString(historicActivity));

    }

    @Test
    void getHistoricTasks() {

        List<HistoricTaskInfo> historicTasks = flowableHistoricService.queryHistoricTasks("a69eafbf-7a90-11ed-81ff-0242c0a85007");

        System.out.println(JSON.toJSONString(historicTasks));

    }

    @Test
    void getHistoricActivitys() {
        List<HistoricActivityInfo> historicActivitys = flowableHistoricService.queryHistoricActivitys("a69eafbf-7a90-11ed-81ff-0242c0a85007");
        System.out.println(JSON.toJSONString(historicActivitys));
    }

    @Test
    void backTask() {
        flowableTaskService.taskReturn("57975760-d64b-11ec-a77a-005056c00008", "退回");
    }

    @Test
    void queryHistoricActivityInstanceAsc() {
        List<HistoricActivityInstance> historicActivityInstances = flowableHistoricService.queryHistoricActivityInstanceAsc("9d7f431a-75ef-11ed-b85f-005056c00008");
        System.out.println(historicActivityInstances.toString());
    }

    @Test
    void queryHistoricFinishedByProcInstId() {
        List<HistoricProcessInstance> historicProcessInstances = flowableHistoricService.queryHistoricFinishedByProcInstId("08547eba-76d5-11ed-88da-005056c00008");
        System.out.println(historicProcessInstances.toString());
    }

    @Test
    void queryReturnTasks() {

        List<ReturnTask> returnTasks = flowableHistoricService.queryReturnTasks("728fd452-7b72-11ed-88cd-0242c0a85007");
        System.out.println(returnTasks);
    }

    @Test
    void queryReturnTasks2() {

        List<ReturnTask> returnTasks = flowableHistoricService.queryReturnTasks("874c85c7-7b81-11ed-a851-0242c0a85007", null, CollectionUtil.newHashSet("3"));
        System.out.println(returnTasks);


    }

    @Test
    void downloadDeploy() {
        byte[] bytes = flowableProcessService.downloadDeploy("el:2:de30aa94-763c-11ed-990c-005056c00008");
        FileUtil.writeBytes(bytes, new File("F:\\1.xml"));
    }

    @Test
    void getSequenceFlowExtensionElement() {
        ProcessInstanceInfo processInstanceInfo = flowableProcessInstanceService.queryProcessInstance("74292ac7-7641-11ed-a5cd-005056c00008");
        Task task = flowableTaskService.queryTaskByProcessInstanceId(processInstanceInfo.getId());
//        FlowElement flowElement = getNextUserFlowElement(task);

        Map<String, List<ExtensionElement>> sequenceFlowExtensionElement = flowableTaskService.getSequenceFlowExtensionElement(task.getId());
        System.out.println(sequenceFlowExtensionElement.toString());
    }

    @Test
    void getExtensionElement() {
        ProcessInstanceInfo processInstanceInfo = flowableProcessInstanceService.queryProcessInstance("a83b28e0-76b9-11ed-ba4f-005056c00008");
        Task task = flowableTaskService.queryTaskByProcessInstanceId(processInstanceInfo.getId());
        List<ExtensionElementInfo> extensionElements = flowableTaskService.getExtensionElement(task.getId());
        System.out.println(extensionElements);
    }

    @Test
    void getNextUserFlows() {
        ProcessInstanceInfo processInstanceInfo = flowableProcessInstanceService.queryProcessInstance("08547eba-76d5-11ed-88da-005056c00008");
        Task task = flowableTaskService.queryTaskByProcessInstanceId(processInstanceInfo.getId());
        Map<FlowElement, SequenceFlow> nextUserFlows = flowableTaskService.getNextUserFlows(task.getId());
        System.out.println(nextUserFlows);
    }

    @Test
    void queryHistoricTaskOrderCreateTimeAsc() {
        List<HistoricTaskInstance> historicTaskInstances = flowableHistoricService.queryHistoricTaskOrderCreateTimeAsc("74292ac7-7641-11ed-a5cd-005056c00008");
        System.out.println(historicTaskInstances.toString());
    }

    @Test
    void queryTodoTask() {
        List<Task> tasks = flowableTaskService.queryTodoTask("Rong.Jia", Collections.emptySet());
        System.out.println(tasks.toString());
    }

    @Test
    void getActivityInfo() {
        List<ActivityInfo> activityInfo = flowableTaskService.getActivityInfo("4f7af5f9-7784-11ed-8d21-005056c00008");
        System.out.println(JSON.toJSONString(activityInfo));
    }

    @Test
    void generateFlowSvgByProcInstId() {

        String processInstanceId = "4f7af5f9-7784-11ed-8d21-005056c00008";
        byte[] svgs = flowableProcessInstanceService.generateFlowSvgByProcInstId(processInstanceId);
        FileUtil.writeBytes(svgs, "F:\\svg.svg");

    }

    @Test
    void generateFlowImgByProcessDefinitionId() {

        byte[] bytes = flowableProcessService.generateFlowImgByProcessDefinitionId("el:1:a4398452-7782-11ed-9af5-005056c00008");
        FileUtil.writeBytes(bytes, "F:\\jpeg.jpeg");
    }

    @Test
    void generateFlowSvgByProcessDefinitionId() {

        byte[] bytes = flowableProcessService.generateFlowSvgByProcessDefinitionId("el:1:a4398452-7782-11ed-9af5-005056c00008");
        FileUtil.writeBytes(bytes, "F:\\svg.svg");
    }

    @Test
    void validateProcess() {
        String s = FileUtil.readString("G:\\workspace\\IDC项目\\xdcos-cloud\\v3.x\\bpmn\\el.bpmn20.xml", StandardCharsets.UTF_8);
        List<ValidationErrorInfo> validationErrors = flowableProcessService.validateProcess(s);
        System.out.println(validationErrors.toString());

    }

    @Test
    void validate() {
        InputStream inputStream = ResourceUtil.getStream("G:\\workspace\\IDC项目\\xdcos-cloud\\v3.x\\bpmn\\el.bpmn20.xml");
        List<ValidationErrorInfo> validationErrors = flowableProcessService.validateProcess(inputStream);
        System.out.println(JSON.toJSONString(validationErrors));
    }

    @Test
    void isNextFlowNodeEndEvent() {
        Boolean nodeEndEvent = flowableTaskService.isNextFlowNodeEndEvent("d9c42c99-7aa8-11ed-8866-005056c00008", null, null);
        System.out.println(nodeEndEvent);

    }

    @Test
    void queryTodoTask2() {
        flowableTaskService.queryTodoTask("d9c42c99-7aa8-11ed-8866-005056c00008", null, CollectionUtil.newHashSet("6"));
    }




}
