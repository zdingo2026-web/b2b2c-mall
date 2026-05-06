package com.mall.common.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Paginated response wrapper.
 *
 * @param <T> item type
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> list;
    private long total;
    private long page;
    private long limit;

    public PageResult() {}

    public PageResult(List<T> list, long total, long page, long limit) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.limit = limit;
    }

    public static <T> PageResult<T> empty(long page, long limit) {
        return new PageResult<>(Collections.<T>emptyList(), 0, page, limit);
    }

    public static <T> PageResult<T> of(List<T> list, long total, long page, long limit) {
        return new PageResult<>(list, total, page, limit);
    }
}
