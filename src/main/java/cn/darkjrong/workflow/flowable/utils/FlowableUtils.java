package cn.darkjrong.workflow.flowable.utils;

import cn.darkjrong.workflow.flowable.domain.ExtensionElementInfo;
import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;

import java.util.*;

/**
 * 流程引擎工具类封装
 *
 * @author Rong.Jia
 * @date 2022/12/07
 */
@Slf4j
public class FlowableUtils {

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "stringValue";
    private static final String ATTRIBUTE_EXPRESSION = "expression";

    /**
     * 获取扩展属性值
     *
     * @param sequenceFlow 序列流
     * @return {@link List}<{@link ExtensionElementInfo}>
     */
    public static List<ExtensionElementInfo> getExtensionElement(SequenceFlow sequenceFlow) {
        Map<String, List<ExtensionElement>> extensionElements = sequenceFlow.getExtensionElements();
        return getExtensionElement(extensionElements);
    }

    /**
     * 获取扩展元素
     *
     * @param extensionElements 扩展元素
     * @return {@link List}<{@link ExtensionElementInfo}>
     */
    public static List<ExtensionElementInfo> getExtensionElement(Map<String, List<ExtensionElement>> extensionElements) {
        if (CollectionUtil.isNotEmpty(extensionElements)) {
            List<ExtensionElementInfo> extensionElementInfos = CollectionUtil.newArrayList();
            extensionElements.forEach((key, value) -> {
                ExtensionElementInfo extensionElementInfo = new ExtensionElementInfo();
                value.forEach(a -> {
                    extensionElementInfo.setName(a.getAttributeValue(null, ATTRIBUTE_NAME));
                    extensionElementInfo.setValue(a.getAttributeValue(null, ATTRIBUTE_VALUE));
                    extensionElementInfo.setExpression(a.getAttributeValue(null, ATTRIBUTE_EXPRESSION));
                });
                extensionElementInfos.add(extensionElementInfo);
            });
           return extensionElementInfos;
        }
        return Collections.emptyList();
    }

    /**
     * 根据节点，获取入口连线
     *
     * @param source 源
     * @return {@link List}<{@link SequenceFlow}>
     */
    public static List<SequenceFlow> getElementIncomingFlows(FlowElement source) {
        List<SequenceFlow> sequenceFlows = null;
        if (source instanceof Task) {
            sequenceFlows = ((Task) source).getIncomingFlows();
        } else if (source instanceof Gateway) {
            sequenceFlows = ((Gateway) source).getIncomingFlows();
        } else if (source instanceof SubProcess) {
            sequenceFlows = ((SubProcess) source).getIncomingFlows();
        } else if (source instanceof StartEvent) {
            sequenceFlows = ((StartEvent) source).getIncomingFlows();
        } else if (source instanceof EndEvent) {
            sequenceFlows = ((EndEvent) source).getIncomingFlows();
        }
        return sequenceFlows;
    }

    /**
     * 根据节点，获取出口连线
     *
     * @param source 源
     * @return {@link List}<{@link SequenceFlow}>
     */
    public static List<SequenceFlow> getElementOutgoingFlows(FlowElement source) {
        List<SequenceFlow> sequenceFlows = null;
        if (source instanceof Task) {
            sequenceFlows = ((Task) source).getOutgoingFlows();
        } else if (source instanceof Gateway) {
            sequenceFlows = ((Gateway) source).getOutgoingFlows();
        } else if (source instanceof SubProcess) {
            sequenceFlows = ((SubProcess) source).getOutgoingFlows();
        } else if (source instanceof StartEvent) {
            sequenceFlows = ((StartEvent) source).getOutgoingFlows();
        } else if (source instanceof EndEvent) {
            sequenceFlows = ((EndEvent) source).getOutgoingFlows();
        }
        return sequenceFlows;
    }

    /**
     * 获取全部节点列表，包含子流程节点
     *
     * @param flowElements 流元素
     * @param allElements  所有元素
     * @return {@link Collection}<{@link FlowElement}>
     */
    public static Collection<FlowElement> getAllElements(Collection<FlowElement> flowElements, Collection<FlowElement> allElements) {
        allElements = allElements == null ? new ArrayList<>() : allElements;

        for (FlowElement flowElement : flowElements) {
            allElements.add(flowElement);
            if (flowElement instanceof SubProcess) {
                // 继续深入子流程，进一步获取子流程
                allElements = FlowableUtils.getAllElements(((SubProcess) flowElement).getFlowElements(), allElements);
            }
        }
        return allElements;
    }

    /**
     * 迭代获取父级任务节点列表，向前找
     *
     * @param source          起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param userTaskList    已找到的用户任务节点
     * @return {@link List}<{@link UserTask}>
     */
    public static List<UserTask> getParentUserTasks(FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;

        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            userTaskList = getParentUserTasks(source.getSubProcess(), hasSequenceFlow, userTaskList);
        }

        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);

        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 类型为用户节点，则新增父级节点
                if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
                    userTaskList.add((UserTask) sequenceFlow.getSourceFlowElement());
                    continue;
                }
                // 类型为子流程，则添加子流程开始节点出口处相连的节点
                if (sequenceFlow.getSourceFlowElement() instanceof SubProcess) {
                    // 获取子流程用户任务节点
                    List<UserTask> childUserTaskList = getChildProcessUserTasks((StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, null);
                    // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                    if (childUserTaskList != null && childUserTaskList.size() > 0) {
                        userTaskList.addAll(childUserTaskList);
                        continue;
                    }
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                userTaskList = getParentUserTasks(sequenceFlow.getSourceFlowElement(), new HashSet<>(hasSequenceFlow), userTaskList);
            }
        }
        return userTaskList;
    }

    /**
     * 根据正在运行的任务节点，迭代获取子级任务节点列表，向后找
     *
     * @param source          起始节点
     * @param runActiveIdList 正在运行的任务 Key，用于校验任务节点是否是正在运行的节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param flowElementList 需要撤回的用户任务列表
     * @return {@link List}<{@link FlowElement}>
     */
    public static List<FlowElement> getChildUserTasks(FlowElement source, List<String> runActiveIdList, Set<String> hasSequenceFlow, List<FlowElement> flowElementList) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        flowElementList = flowElementList == null ? new ArrayList<>() : flowElementList;

        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof EndEvent && source.getSubProcess() != null) {
            flowElementList = getChildUserTasks(source.getSubProcess(), runActiveIdList, hasSequenceFlow, flowElementList);
        }

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);

        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果为用户任务类型，或者为网关
                // 活动节点ID 在运行的任务中存在，添加
                if ((sequenceFlow.getTargetFlowElement() instanceof UserTask || sequenceFlow.getTargetFlowElement() instanceof Gateway) && runActiveIdList.contains((sequenceFlow.getTargetFlowElement()).getId())) {
                    flowElementList.add(sequenceFlow.getTargetFlowElement());
                    continue;
                }
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    List<FlowElement> childUserTaskList = getChildUserTasks((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), runActiveIdList, hasSequenceFlow, null);
                    // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                    if (childUserTaskList != null && childUserTaskList.size() > 0) {
                        flowElementList.addAll(childUserTaskList);
                        continue;
                    }
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                flowElementList = getChildUserTasks(sequenceFlow.getTargetFlowElement(), runActiveIdList, new HashSet<>(hasSequenceFlow), flowElementList);
            }
        }
        return flowElementList;
    }

    /**
     * 迭代获取子流程用户任务节点
     *
     * @param source          起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param userTaskList    需要撤回的用户任务列表
     * @return {@link List}<{@link UserTask}>
     */
    public static List<UserTask> getChildProcessUserTasks(FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);

        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果为用户任务类型，且任务节点的 Key 正在运行的任务中存在，添加
                if (sequenceFlow.getTargetFlowElement() instanceof UserTask) {
                    userTaskList.add((UserTask) sequenceFlow.getTargetFlowElement());
                    continue;
                }
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    List<UserTask> childUserTaskList = getChildProcessUserTasks((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, null);
                    // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                    if (childUserTaskList != null && childUserTaskList.size() > 0) {
                        userTaskList.addAll(childUserTaskList);
                        continue;
                    }
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                userTaskList = getChildProcessUserTasks(sequenceFlow.getTargetFlowElement(), new HashSet<>(hasSequenceFlow), userTaskList);
            }
        }
        return userTaskList;
    }

    /**
     * 从后向前寻路，获取所有脏线路上的点
     *
     * @param source          起始节点
     * @param passRoads       已经经过的点集合
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param targets         目标脏线路终点
     * @param dirtyRoads      确定为脏数据的点，因为不需要重复，因此使用 set 存储
     * @return {@link Set}<{@link String}>
     */
    public static Set<String> getDirtyRoads(FlowElement source, List<String> passRoads, Set<String> hasSequenceFlow, List<String> targets, Set<String> dirtyRoads) {
        passRoads = passRoads == null ? new ArrayList<>() : passRoads;
        dirtyRoads = dirtyRoads == null ? new HashSet<>() : dirtyRoads;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;

        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            dirtyRoads = getDirtyRoads(source.getSubProcess(), passRoads, hasSequenceFlow, targets, dirtyRoads);
        }

        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);

        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 新增经过的路线
                passRoads.add(sequenceFlow.getSourceFlowElement().getId());
                // 如果此点为目标点，确定经过的路线为脏线路，添加点到脏线路中，然后找下个连线
                if (targets.contains(sequenceFlow.getSourceFlowElement().getId())) {
                    dirtyRoads.addAll(passRoads);
                    continue;
                }
                // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
                if (sequenceFlow.getSourceFlowElement() instanceof SubProcess) {
                    dirtyRoads = getChildProcessAllDirtyRoad((StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, dirtyRoads);
                    // 是否存在子流程上，true 是，false 否
                    Boolean isInChildProcess = dirtyTargetInChildProcess((StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, targets, null);
                    if (isInChildProcess) {
                        // 已在子流程上找到，该路线结束
                        continue;
                    }
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                dirtyRoads = getDirtyRoads(sequenceFlow.getSourceFlowElement(), new ArrayList<>(passRoads), new HashSet<>(hasSequenceFlow), targets, dirtyRoads);
            }
        }
        return dirtyRoads;
    }

    /**
     * 迭代获取子流程脏路线
     * 说明，假如回退的点就是子流程，那么也肯定会回退到子流程最初的用户任务节点，因此子流程中的节点全是脏路线
     *
     * @param source          起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param dirtyRoads      确定为脏数据的点，因为不需要重复，因此使用 set 存储
     * @return {@link Set}<{@link String}>
     */
    public static Set<String> getChildProcessAllDirtyRoad(FlowElement source, Set<String> hasSequenceFlow, Set<String> dirtyRoads) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        dirtyRoads = dirtyRoads == null ? new HashSet<>() : dirtyRoads;

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);

        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 添加脏路线
                dirtyRoads.add(sequenceFlow.getTargetFlowElement().getId());
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    dirtyRoads = getChildProcessAllDirtyRoad((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, dirtyRoads);
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                dirtyRoads = getChildProcessAllDirtyRoad(sequenceFlow.getTargetFlowElement(), new HashSet<>(hasSequenceFlow), dirtyRoads);
            }
        }
        return dirtyRoads;
    }

    /**
     * 判断脏路线结束节点是否在子流程上
     *
     * @param source          起始节点
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param targets         判断脏路线节点是否存在子流程上，只要存在一个，说明脏路线只到子流程为止
     * @param inChildProcess  是否存在子流程上，true 是，false 否
     * @return {@link Boolean}
     */
    public static Boolean dirtyTargetInChildProcess(FlowElement source, Set<String> hasSequenceFlow, List<String> targets, Boolean inChildProcess) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        inChildProcess = inChildProcess != null && inChildProcess;

        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);

        if (sequenceFlows != null && !inChildProcess) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果发现目标点在子流程上存在，说明只到子流程为止
                if (targets.contains(sequenceFlow.getTargetFlowElement().getId())) {
                    inChildProcess = true;
                    break;
                }
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    inChildProcess = dirtyTargetInChildProcess((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, targets, inChildProcess);
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                inChildProcess = dirtyTargetInChildProcess(sequenceFlow.getTargetFlowElement(), new HashSet<>(hasSequenceFlow), targets, inChildProcess);
            }
        }
        return inChildProcess;
    }

    /**
     * 迭代从后向前扫描，判断目标节点相对于当前节点是否是串行
     * 不存在直接回退到子流程中的情况，但存在从子流程出去到父流程情况
     *
     * @param source          起始节点
     * @param isSequential    是否串行
     * @param hasSequenceFlow 已经经过的连线的 ID，用于判断线路是否重复
     * @param targetKsy       目标节点
     * @return {@link Boolean}
     */
    public static Boolean checkSequentialReferTarget(FlowElement source, String targetKsy, Set<String> hasSequenceFlow, Boolean isSequential) {
        isSequential = isSequential == null || isSequential;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;

        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            isSequential = checkSequentialReferTarget(source.getSubProcess(), targetKsy, hasSequenceFlow, isSequential);
        }

        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);

        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果目标节点已被判断为并行，后面都不需要执行，直接返回
                if (!isSequential) {
                    break;
                }
                // 这条线路存在目标节点，这条线路完成，进入下个线路
                if (targetKsy.equals(sequenceFlow.getSourceFlowElement().getId())) {
                    continue;
                }
                if (sequenceFlow.getSourceFlowElement() instanceof StartEvent) {
                    isSequential = false;
                    break;
                }
                // 否则就继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                isSequential = checkSequentialReferTarget(sequenceFlow.getSourceFlowElement(), targetKsy, new HashSet<>(hasSequenceFlow), isSequential);
            }
        }
        return isSequential;
    }

    /**
     * 从后向前寻路，获取到达节点的所有路线
     * 不存在直接回退到子流程，但是存在回退到父级流程的情况
     *
     * @param source          起始节点
     * @param passRoads       已经经过的点集合
     * @param roads           路线
     * @param hasSequenceFlow 有序列流
     * @return {@link List}<{@link List}<{@link UserTask}>>
     */
    public static List<List<UserTask>> findRoad(FlowElement source, List<UserTask> passRoads, Set<String> hasSequenceFlow, List<List<UserTask>> roads) {
        passRoads = passRoads == null ? new ArrayList<>() : passRoads;
        roads = roads == null ? new ArrayList<>() : roads;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;

        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            roads = findRoad(source.getSubProcess(), passRoads, hasSequenceFlow, roads);
        }

        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);

        if (sequenceFlows != null && sequenceFlows.size() != 0) {
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 添加经过路线
                if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
                    passRoads.add((UserTask) sequenceFlow.getSourceFlowElement());
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                roads = findRoad(sequenceFlow.getSourceFlowElement(), new ArrayList<>(passRoads), new HashSet<>(hasSequenceFlow), roads);
            }
        } else {
            // 添加路线
            roads.add(passRoads);
        }
        return roads;
    }

    /**
     * 历史节点数据清洗，清洗掉又回滚导致的脏数据
     *
     * @param allElements            全部节点信息
     * @param historicActivityIdList 历史任务实例信息，数据采用开始时间升序
     * @return {@link List}<{@link String}>
     */
    public static List<String> historicTaskInstanceClean(Collection<FlowElement> allElements, List<HistoricActivityInstance> historicActivityIdList) {
        // 会签节点收集
        List<String> multiTask = new ArrayList<>();
        allElements.forEach(flowElement -> {
            if (flowElement instanceof UserTask) {
                // 如果该节点的行为为会签行为，说明该节点为会签节点
                if (((UserTask) flowElement).getBehavior() instanceof ParallelMultiInstanceBehavior || ((UserTask) flowElement).getBehavior() instanceof SequentialMultiInstanceBehavior) {
                    multiTask.add(flowElement.getId());
                }
            }
        });
        // 循环放入栈，栈 LIFO：后进先出
        Stack<HistoricActivityInstance> stack = new Stack<>();
        historicActivityIdList.forEach(stack::push);
        // 清洗后的历史任务实例
        List<String> lastHistoricTaskInstanceList = new ArrayList<>();
        // 网关存在可能只走了部分分支情况，且还存在跳转废弃数据以及其他分支数据的干扰，因此需要对历史节点数据进行清洗
        // 临时用户任务 key
        StringBuilder userTaskKey = null;
        // 临时被删掉的任务 key，存在并行情况
        List<String> deleteKeyList = new ArrayList<>();
        // 临时脏数据线路
        List<Set<String>> dirtyDataLineList = new ArrayList<>();
        // 由某个点跳到会签点,此时出现多个会签实例对应 1 个跳转情况，需要把这些连续脏数据都找到
        // 会签特殊处理下标
        int multiIndex = -1;
        // 会签特殊处理 key
        StringBuilder multiKey = null;
        // 会签特殊处理操作标识
        boolean multiOpera = false;
        while (!stack.empty()) {
            // 从这里开始 userTaskKey 都还是上个栈的 key
            // 是否是脏数据线路上的点
            final boolean[] isDirtyData = {false};
            for (Set<String> oldDirtyDataLine : dirtyDataLineList) {
                if (oldDirtyDataLine.contains(stack.peek().getActivityId())) {
                    isDirtyData[0] = true;
                }
            }
            // 删除原因不为空，说明从这条数据开始回跳或者回退的
            // MI_END：会签完成后，其他未签到节点的删除原因，不在处理范围内
            if (stack.peek().getDeleteReason() != null && !"MI_END".equals(stack.peek().getDeleteReason())) {
                // 可以理解为脏线路起点
                String dirtyPoint = "";
                if (stack.peek().getDeleteReason().contains("Change activity to ")) {
                    dirtyPoint = stack.peek().getDeleteReason().replace("Change activity to ", "");
                }
                // 会签回退删除原因有点不同
                if (stack.peek().getDeleteReason().contains("Change parent activity to ")) {
                    dirtyPoint = stack.peek().getDeleteReason().replace("Change parent activity to ", "");
                }
                FlowElement dirtyTask = null;
                // 获取变更节点的对应的入口处连线
                // 如果是网关并行回退情况，会变成两条脏数据路线，效果一样
                for (FlowElement flowElement : allElements) {
                    if (flowElement.getId().equals(stack.peek().getActivityId())) {
                        dirtyTask = flowElement;
                    }
                }
                // 获取脏数据线路
                Set<String> dirtyDataLine = getDirtyRoads(dirtyTask, null, null, Arrays.asList(dirtyPoint.split(",")), null);
                // 自己本身也是脏线路上的点，加进去
                dirtyDataLine.add(stack.peek().getActivityId());
                log.info(stack.peek().getActivityId() + "点脏路线集合：" + dirtyDataLine);
                // 是全新的需要添加的脏线路
                boolean isNewDirtyData = true;
                for (Set<String> strings : dirtyDataLineList) {
                    // 如果发现他的上个节点在脏线路内，说明这个点可能是并行的节点，或者连续驳回
                    // 这时，都以之前的脏线路节点为标准，只需合并脏线路即可，也就是路线补全
                    if (strings.contains(userTaskKey.toString())) {
                        isNewDirtyData = false;
                        strings.addAll(dirtyDataLine);
                    }
                }
                // 已确定时全新的脏线路
                if (isNewDirtyData) {
                    // deleteKey 单一路线驳回到并行，这种同时生成多个新实例记录情况，这时 deleteKey 其实是由多个值组成
                    // 按照逻辑，回退后立刻生成的实例记录就是回退的记录
                    // 至于驳回所生成的 Key，直接从删除原因中获取，因为存在驳回到并行的情况
                    deleteKeyList.add(dirtyPoint + ",");
                    dirtyDataLineList.add(dirtyDataLine);
                }
                // 添加后，现在这个点变成脏线路上的点了
                isDirtyData[0] = true;
            }
            // 如果不是脏线路上的点，说明是有效数据，添加历史实例 Key
            if (!isDirtyData[0]) {
                lastHistoricTaskInstanceList.add(stack.peek().getActivityId());
            }
            // 校验脏线路是否结束
            for (int i = 0; i < deleteKeyList.size(); i ++) {
                // 如果发现脏数据属于会签，记录下下标与对应 Key，以备后续比对，会签脏数据范畴开始
                if (multiKey == null && multiTask.contains(stack.peek().getActivityId())
                        && deleteKeyList.get(i).contains(stack.peek().getActivityId())) {
                    multiIndex = i;
                    multiKey = new StringBuilder(stack.peek().getActivityId());
                }
                // 会签脏数据处理，节点退回会签清空
                // 如果在会签脏数据范畴中发现 Key改变，说明会签脏数据在上个节点就结束了，可以把会签脏数据删掉
                if (multiKey != null && !multiKey.toString().equals(stack.peek().getActivityId())) {
                    deleteKeyList.set(multiIndex , deleteKeyList.get(multiIndex).replace(stack.peek().getActivityId() + ",", ""));
                    multiKey = null;
                    // 结束进行下校验删除
                    multiOpera = true;
                }
                // 其他脏数据处理
                // 发现该路线最后一条脏数据，说明这条脏数据线路处理完了，删除脏数据信息
                // 脏数据产生的新实例中是否包含这条数据
                if (multiKey == null && deleteKeyList.get(i).contains(stack.peek().getActivityId())) {
                    // 删除匹配到的部分
                    deleteKeyList.set(i , deleteKeyList.get(i).replace(stack.peek().getActivityId() + ",", ""));
                }
                // 如果每组中的元素都以匹配过，说明脏数据结束
                if ("".equals(deleteKeyList.get(i))) {
                    // 同时删除脏数据
                    deleteKeyList.remove(i);
                    dirtyDataLineList.remove(i);
                    break;
                }
            }
            // 会签数据处理需要在循环外处理，否则可能导致溢出
            // 会签的数据肯定是之前放进去的所以理论上不会溢出，但还是校验下
            if (multiOpera && deleteKeyList.size() > multiIndex && "".equals(deleteKeyList.get(multiIndex))) {
                // 同时删除脏数据
                deleteKeyList.remove(multiIndex);
                dirtyDataLineList.remove(multiIndex);
                multiIndex = -1;
                multiOpera = false;
            }
            // pop() 方法与 peek() 方法不同，在返回值的同时，会把值从栈中移除
            // 保存新的 userTaskKey 在下个循环中使用
            userTaskKey = new StringBuilder(stack.pop().getActivityId());
        }
        log.info("清洗后的历史节点数据：" + lastHistoricTaskInstanceList);
        return lastHistoricTaskInstanceList;
    }

//    /**
//     * 获取当前任务节点的下一个任务节点
//     *
//     * @param task 当前任务节点
//     * @return {@link FlowElement} 下个任务节点
//     */
//    public static FlowElement getNextUserFlowElement(Task task) {
//        // 取得已提交的任务
//        HistoricTaskInstance historicTaskInstance = activitiUtil.historyService.createHistoricTaskInstanceQuery()
//                .taskId(task.getId()).singleResult();
//
//        // 获得流程定义
//        ProcessDefinition processDefinition = activitiUtil.repositoryService.getProcessDefinition(historicTaskInstance.getProcessDefinitionId());
//
//        //获得当前流程的活动ID
//        ExecutionQuery executionQuery = activitiUtil.runtimeService.createExecutionQuery();
//        Execution execution = executionQuery.executionId(historicTaskInstance.getExecutionId()).singleResult();
//        String activityId = execution.getActivityId();
//        UserTask userTask = null;
//        while (true) {
//            //根据活动节点获取当前的组件信息
//            FlowNode flowNode = getFlowNode(processDefinition.getId(), activityId);
//            //获取该节点之后的流向
//            List<SequenceFlow> sequenceFlowListOutGoing = flowNode.getOutgoingFlows();
//
//            // 获取的下个节点不一定是userTask的任务节点，所以要判断是否是任务节点
//            if (sequenceFlowListOutGoing.size() > 1) {
//                // 如果有1条以上的出线，表示有分支，需要判断分支的条件才能知道走哪个分支
//                // 遍历节点的出线得到下个activityId
//                activityId = getNextActivityId(execution.getId(),
//                        task.getProcessInstanceId(), sequenceFlowListOutGoing);
//            } else if (sequenceFlowListOutGoing.size() == 1) {
//                // 只有1条出线,直接取得下个节点
//                SequenceFlow sequenceFlow = sequenceFlowListOutGoing.get(0);
//                // 下个节点
//                FlowElement flowElement = sequenceFlow.getTargetFlowElement();
//                if (flowElement instanceof UserTask) {
//                    // 下个节点为UserTask时
//                    userTask = (UserTask) flowElement;
//                    System.out.println("下个任务为:" + userTask.getName());
//                    return userTask;
//                } else if (flowElement instanceof ExclusiveGateway) {
//                    // 下个节点为排它网关时
//                    ExclusiveGateway exclusiveGateway = (ExclusiveGateway) flowElement;
//                    List<SequenceFlow> outgoingFlows = exclusiveGateway.getOutgoingFlows();
//                    // 遍历网关的出线得到下个activityId
//                    activityId = getNextActivityId(execution.getId(), task.getProcessInstanceId(), outgoingFlows);
//                }
//
//            } else {
//                // 没有出线，则表明是结束节点
//                return null;
//            }
//        }
//
//    }
//
//    /**
//     * 根据el表达式取得满足条件的下一个activityId
//     *
//     * @param executionId       执行ID
//     * @param processInstanceId 过程实例ID
//     * @param outgoingFlows     即将离任流
//     * @return {@link String}
//     */
//    public static String getNextActivityId(String executionId, String processInstanceId, List<SequenceFlow> outgoingFlows) {
//        String activityId = null;
//        // 遍历出线
//        for (SequenceFlow outgoingFlow : outgoingFlows) {
//            // 取得线上的条件
//            String conditionExpression = outgoingFlow.getConditionExpression();
//            // 取得所有变量
//            Map<String, Object> variables = activitiUtil.runtimeService.getVariables(executionId);
//            String variableName = "";
//            // 判断网关条件里是否包含变量名
//            for (String s : variables.keySet()) {
//                if (conditionExpression.contains(s)) {
//                    // 找到网关条件里的变量名
//                    variableName = s;
//                }
//            }
//            String conditionVal = getVariableValue(variableName, processInstanceId);
//            // 判断el表达式是否成立
//            if (isCondition(variableName, conditionExpression, conditionVal)) {
//                // 取得目标节点
//                FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
//                activityId = targetFlowElement.getId();
//                continue;
//            }
//        }
//        return activityId;
//    }
//
//    /**
//     * 根据活动节点和流程定义ID获取该活动节点的组件信息
//     *
//     * @param flowElementId 流程节点ID
//     * @param bpmnModel     bpmn模型
//     * @return {@link FlowNode}
//     */
//    public static FlowNode getFlowNode(BpmnModel bpmnModel, String flowElementId) {
//        return (FlowNode) bpmnModel.getMainProcess().getFlowElement(flowElementId);
//    }
//
//    /**
//     * 根据key和value判断el表达式是否通过
//     *
//     * @param key   el表达式key
//     * @param el    el表达式
//     * @param value el表达式传入值
//     * @return {@link Boolean}
//     */
//    public static Boolean isCondition(String key, String el, String value) {
//        ExpressionFactory factory = new ExpressionFactoryImpl();
//        SimpleContext context = new SimpleContext();
//        context.setVariable(key, factory.createValueExpression(value, String.class));
//        ValueExpression e = factory.createValueExpression(context, el, Boolean.class);
//        return (Boolean) e.getValue(context);
//    }
//
//    /**
//     * 取得流程变量的值
//     *
//     * @param variableName      变量名
//     * @param processInstanceId 流程实例Id
//     * @return
//     */
//    public static String getVariableValue(String variableName, String processInstanceId) {
//        Execution execution = activitiUtil.runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list().get(0);
//        Object object = activitiUtil.runtimeService.getVariable(execution.getId(), variableName);
//        return object == null ? "" : object.toString();
//    }
//
//
//    /**
//     * 通过表达式获取其中的变量名
//     *
//     * @param expression 表达式
//     * @return {@link String}
//     */
//    public static String getVariableNameByExpression(String expression) {
//        return expression.replace("${", "")
//                .replace("}", "");
//    }

}
