package cn.darkjrong.workflow.flowable.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.event.FlowableEntityEventImpl;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.impl.context.Context;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

/**
 * 全局自动完成首个任务监听器
 *
 * @author Rong.Jia
 * @date 2022/12/09
 */
@AllArgsConstructor
public class AutoCompleteFirstTaskListener implements FlowableEventListener {

    private final RepositoryService repositoryService;

    @Override
    public void onEvent(FlowableEvent event) {
        if (!(event instanceof FlowableEntityEventImpl)) {
            return;
        }

        FlowableEntityEventImpl entityEvent = (FlowableEntityEventImpl) event;
        Object entity = entityEvent.getEntity();

        //是否是任务实体类
        if (!(entity instanceof TaskEntity)) {
            return;
        }

        TaskEntity taskEntity = (TaskEntity) entity;

        //是否是在任务节点创建时
        if (FlowableEngineEventType.TASK_CREATED.equals(event.getType())) {
            //找到流程第一个userTask节点
            FlowElement firstElement = this.findFirstFlowElement(taskEntity);

            //对比节点是否相同,因为有可能有子流程的节点进来
            if (ObjectUtil.isNotNull(firstElement) && taskEntity.getTaskDefinitionKey().equals(firstElement.getId())) {
                Context.getProcessEngineConfiguration().getTaskService().complete(taskEntity.getId());
            }
        }
    }

    /**
     * 查找流程第一个userTask
     */
    private FlowElement findFirstFlowElement(TaskEntity taskEntity) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(taskEntity.getProcessDefinitionId());
        for (FlowElement flowElement : bpmnModel.getProcesses().get(0).getFlowElements()) {
            if (flowElement instanceof StartEvent) {
                StartEvent startEvent = (StartEvent) flowElement;
                if (CollectionUtil.isNotEmpty(startEvent.getOutgoingFlows()) && startEvent.getOutgoingFlows().size() == 1) {
                    return bpmnModel.getFlowElement(((StartEvent) flowElement).getOutgoingFlows().get(0).getTargetRef());
                }
            }
        }
        return null;
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
