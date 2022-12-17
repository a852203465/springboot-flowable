/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.darkjrong.workflow.flowable.image.svg;

import org.flowable.bpmn.model.BpmnModel;

import java.io.InputStream;
import java.util.List;

/**
 * svg流程图生成
 *
 * @author Rong.Jia
 * @date 2022/12/10
 */
@SuppressWarnings("ALL")
public interface SvgProcessDiagramGenerator {

    /**
     * 生成图
     *
     * @param bpmnModel             bpmn模型
     * @param highLightedActivities 活动的节点
     * @param highLightedFlows      活动的线
     * @param activityFontName      活动字体名称
     * @param labelFontName         标签字体名称
     * @param annotationFontName    注释字体名称
     * @return {@link InputStream}
     */
    InputStream generateDiagram(BpmnModel bpmnModel, List<String> highLightedActivities, List<String> highLightedFlows, String activityFontName, String labelFontName, String annotationFontName);

    /**
     * 生成图
     *
     * @param bpmnModel              bpmn模型
     * @param highLightedActivities  活动的节点
     * @param highLightedFlows       活动的线
     * @param activityFontName       活动字体名称
     * @param labelFontName          标签字体名称
     * @param annotationFontName     注释字体名称
     * @param generateDefaultDiagram 生成默认图
     * @return {@link InputStream}
     */
    InputStream generateDiagram(BpmnModel bpmnModel, List<String> highLightedActivities, List<String> highLightedFlows, String activityFontName, String labelFontName, String annotationFontName, boolean generateDefaultDiagram);

    /**
     * 生成图
     *
     * @param bpmnModel                   bpmn模型
     * @param highLightedActivities       活动的节点
     * @param highLightedFlows            活动的线
     * @param activityFontName            活动字体名称
     * @param labelFontName               标签字体名称
     * @param annotationFontName          注释字体名称
     * @param generateDefaultDiagram      生成默认图
     * @param defaultDiagramImageFileName 默认图图像文件名字
     * @return {@link InputStream}
     */
    InputStream generateDiagram(BpmnModel bpmnModel, List<String> highLightedActivities, List<String> highLightedFlows, String activityFontName, String labelFontName, String annotationFontName, boolean generateDefaultDiagram, String defaultDiagramImageFileName);

    /**
     * 生成图
     *
     * @param bpmnModel                   bpmn模型
     * @param highLightedActivities       活动的节点
     * @param highLightedFlows            活动的线
     * @return {@link InputStream}
     */
    InputStream generateDiagram(BpmnModel bpmnModel, List<String> highLightedActivities, List<String> highLightedFlows);

    /**
     * 生成图
     *
     * @param bpmnModel                   bpmn模型
     * @param highLightedActivities       活动的节点
     * @return {@link InputStream}
     */
    InputStream generateDiagram(BpmnModel bpmnModel, List<String> highLightedActivities);

    /**
     * 生成图
     *
     * @param bpmnModel          bpmn模型
     * @param activityFontName   活动字体名称
     * @param labelFontName      标签字体名称
     * @param annotationFontName 注释字体名称
     * @return {@link InputStream}
     */
    InputStream generateDiagram(BpmnModel bpmnModel, String activityFontName, String labelFontName, String annotationFontName);

    /**
     * 获取默认活动字体名字
     *
     * @return {@link String}
     */
    String getDefaultActivityFontName();

    /**
     * 获取默认标签字体名字
     *
     * @return {@link String}
     */
    String getDefaultLabelFontName();

    /**
     * 获取默认注释字体名字
     *
     * @return {@link String}
     */
    String getDefaultAnnotationFontName();

    /**
     * 获取默认图图像文件名字
     *
     * @return {@link String}
     */
    String getDefaultDiagramImageFileName();



}