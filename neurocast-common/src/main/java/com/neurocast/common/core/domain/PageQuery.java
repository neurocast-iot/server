package com.neurocast.common.core.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询基础参数
 */
@Getter
@Setter
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码，从 1 开始
     */
    private int pageNum;

    /**
     * 每页大小，默认 10，最大 100
     */
    private int pageSize;

    /**
     * 排序字段
     */
    private String orderByName;

    /**
     * 是否倒序
     */
    private Boolean orderByDesc;

    public int getPageNum() {
        return pageNum < 1 ? 1 : pageNum;
    }

    public int getPageSize() {
        return pageSize <= 0 || pageSize > 100 ? 10 : pageSize;
    }
}
