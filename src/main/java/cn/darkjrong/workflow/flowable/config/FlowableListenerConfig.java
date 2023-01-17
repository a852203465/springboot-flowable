package cn.darkjrong.workflow.flowable.config;

import cn.darkjrong.workflow.flowable.listener.AutoCompleteFirstTaskListener;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 全局监听配置类
 *
 * @author Rong.Jia
 * @date 2022/12/09
 */
@Configuration
public class FlowableListenerConfig {

    @Bean
    public AutoCompleteFirstTaskListener autoCompleteFirstTaskListener() {
        return new AutoCompleteFirstTaskListener();
    }

    /**
     * 监听类配置
     * {@link FlowableEngineEventType} flowable监听器级别
     *
     * @return {@link Map}<{@link String}, {@link List}<{@link FlowableEventListener}>>
     */
    private Map<String, List<FlowableEventListener>> customFlowableListeners() {
        Map<String, List<FlowableEventListener>> listenerMap = MapUtil.newHashMap();
        listenerMap.put(FlowableEngineEventType.TASK_CREATED.name(), CollectionUtil.newArrayList(autoCompleteFirstTaskListener()));
        return listenerMap;
    }

    /**
     * 将自定义监听器纳入flowable监听
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> globalListenerConfigurer() {
        return engineConfiguration -> engineConfiguration.setTypedEventListeners(this.customFlowableListeners());
    }


}
