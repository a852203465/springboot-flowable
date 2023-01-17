package cn.darkjrong.workflow.flowable.image.svg.icon;

import cn.darkjrong.workflow.flowable.image.svg.ProcessDiagramSVGGraphics2D;

/**
 * 图标类型
 *
 * @author Rong.Jia
 * @date 2022/12/10
 */
@SuppressWarnings("all")
public abstract class IconType {

    abstract public Integer getWidth();

    abstract public Integer getHeight();

    abstract public String getAnchorValue();

    abstract public String getFillValue();

    abstract public String getStyleValue();

    abstract public String getDValue();

    abstract public void drawIcon(final int imageX,
                                  final int imageY,
                                  final int iconPadding,
                                  final ProcessDiagramSVGGraphics2D svgGenerator);

    abstract public String getStrokeValue();

    abstract public String getStrokeWidth();
}
