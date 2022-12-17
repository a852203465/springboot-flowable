package cn.darkjrong.workflow.flowable.image.svg.icon;

/**
 * 发送任务图标类型
 *
 * @author Rong.Jia
 * @date 2022/12/10
 */
public class SendTaskIconType extends TaskIconType {

    @Override
    public String getStyleValue() {
        return "fill:#16964d;stroke:none;";
    }

    @Override
    public String getDValue() {
        return "M 1 3 L 9 11 L 17 3 L 1 3 z M 1 5 L 1 13 L 5 9 L 1 5 z M 17 5 L 13 9 L 17 13 L 17 5 z M 6 10 L 1 15 L 17 15 L 12 10 L 9 13 L 6 10 z ";
    }
}
