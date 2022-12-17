package cn.darkjrong.workflow.flowable.image.svg;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.DOMTreeManager;

/**
 * 流程图dom group 管理
 *
 * @author Rong.Jia
 * @date 2022/12/10
 */
@SuppressWarnings("ALL")
public class ProcessDiagramDOMGroupManager extends DOMGroupManager {

    public ProcessDiagramDOMGroupManager(GraphicContext gc,
                                         DOMTreeManager domTreeManager) {
        super(gc,
              domTreeManager);
    }

    public void setCurrentGroupId(String id) {
        this.currentGroup.setAttribute("id",
                                       id);
    }
}