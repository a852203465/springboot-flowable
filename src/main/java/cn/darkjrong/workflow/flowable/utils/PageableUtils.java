package cn.darkjrong.workflow.flowable.utils;

import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 分页工具类
 *
 * @author Rong.Jia
 * @date 2022/05/25
 */
public class PageableUtils {

    /**
     * 基本查询
     *
     * @param pageNum    页号
     * @param pageSize   分页大小
     * @param total    总计
     * @param records  记录
     * @return {@link PageInfo}<{@link T}>
     */
    public static <T> PageInfo<T> basicQuery(Integer pageNum, Integer pageSize, Long total, List<T> records) {

        PageInfo<T> pageInfo = new PageInfo<>(records);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(total);
        pageInfo.setList(records);

        return pageInfo;
    }























}
