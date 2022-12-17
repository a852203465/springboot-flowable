package cn.darkjrong.workflow.flowable.image.svg;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;

import java.util.Map;

/**
 * 流程图svg graphics 2d
 *
 * @author Rong.Jia
 * @date 2022/12/10
 */
@SuppressWarnings("ALL")
public class ProcessDiagramSVGGraphics2D extends SVGGraphics2D {

    public ProcessDiagramSVGGraphics2D(Document domFactory) {
        super(domFactory);
        this.setDOMGroupManager(new ProcessDiagramDOMGroupManager(this.getGraphicContext(),
                                                                  this.getDOMTreeManager()));
    }

    @Override
    public void setRenderingHints(@SuppressWarnings("rawtypes") Map hints) {
        super.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(@SuppressWarnings("rawtypes") Map hints) {
        super.addRenderingHints(hints);
    }

    public void setCurrentGroupId(String id) {
        this.getExtendDOMGroupManager().setCurrentGroupId(id);
    }

    public ProcessDiagramDOMGroupManager getExtendDOMGroupManager() {
        return (ProcessDiagramDOMGroupManager) super.getDOMGroupManager();
    }
}
