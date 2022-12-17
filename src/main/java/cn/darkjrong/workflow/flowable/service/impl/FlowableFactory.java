package cn.darkjrong.workflow.flowable.service.impl;

import org.flowable.engine.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Flowable工厂
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
public class FlowableFactory {

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected IdentityService identityService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected FormService formService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected ManagementService managementService;


}
