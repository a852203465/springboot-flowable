package cn.darkjrong.workflow.flowable.image.svg.icon;

import cn.darkjrong.workflow.flowable.image.svg.ProcessDiagramSVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Element;

/**
 * 任务图标类型
 *
 * @author Rong.Jia
 * @date 2022/12/10
 */
public abstract class TaskIconType extends IconType {

    @Override
    public String getAnchorValue() {
        return "top left";
    }

    @Override
    public String getStrokeValue() {
        return null;
    }

    @Override
    public String getFillValue() {
        return null;
    }

    @Override
    public Integer getWidth() {
        return null;
    }

    @Override
    public Integer getHeight() {
        return null;
    }

    @Override
    public String getStrokeWidth() {
        return null;
    }

    @Override
    public void drawIcon(final int imageX,
                         final int imageY,
                         final int iconPadding,
                         final ProcessDiagramSVGGraphics2D svgGenerator) {
        Element gTag = svgGenerator.getDOMFactory().createElementNS(null,
                                                                    SVGGraphics2D.SVG_G_TAG);
        gTag.setAttributeNS(null,
                            "transform",
                            "translate(" + (imageX + iconPadding) + "," + (imageY + iconPadding) + ")");

        Element pathTag = svgGenerator.getDOMFactory().createElementNS(null,
                                                                       SVGGraphics2D.SVG_PATH_TAG);
        pathTag.setAttributeNS(null,
                               "d",
                               this.getDValue());
        pathTag.setAttributeNS(null,
                               "anchors",
                               this.getAnchorValue());
        pathTag.setAttributeNS(null,
                               "style",
                               this.getStyleValue());

        gTag.appendChild(pathTag);
        svgGenerator.getExtendDOMGroupManager().addElement(gTag);
    }
}
