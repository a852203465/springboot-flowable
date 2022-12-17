package cn.darkjrong.workflow.flowable.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 审批意见的类型
 *
 * @author Rong.Jia
 * @date 2022/05/26
 */
@Getter
@AllArgsConstructor
public enum CommentTypeEnum {

    // 正常
    ZCH("正常"),
    SP("审批"),
    BH("驳回"),
    TH("退回"),
    CH("撤回"),
    ZC("暂存"),
    QS("签收"),
    WP("委派"),
    ZH("知会"),
    ZY("转阅"),
    YY("已阅"),
    ZB("转办"),
    QJQ("前加签"),
    HJQ("后加签"),
    XTZX("系统执行"),
    TJ("提交"),
    CXTJ("重新提交"),
    SPJS("结束"),
    LCZZ("终止"),
    SQ("授权"),
    CFTG("重复跳过"),
    XT("协同"),
    PS("评审"),
    YGD("已归档"),
    YQX("已取消");


    ;

    private final String name;


    /**
     * 获取类型
     *
     * @param name 名字
     * @return {@link CommentTypeEnum}
     */
    public static CommentTypeEnum getType(String name) {
        return Stream.of(CommentTypeEnum.values()).filter(a -> StrUtil.equals(a.getName(), name)).findAny().orElse(ZCH);
    }







}
