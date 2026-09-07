package com.neurocast.common.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 统一分页结果
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total = 0L;

    /**
     * 当前页数据
     */
    private List<T> items = new ArrayList<>();

    public static <T> PageResult<T> of(long total, List<T> items) {
        return new PageResult<>(total, items);
    }
}
