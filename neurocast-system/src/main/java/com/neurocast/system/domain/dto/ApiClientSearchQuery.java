package com.neurocast.system.domain.dto;

import com.neurocast.common.core.domain.PageQuery;
import lombok.Getter;
import lombok.Setter;

/**
 * API 客户端分页查询条件
 */
@Getter
@Setter
public class ApiClientSearchQuery extends PageQuery {

    /**
     * 客户端编码 / 名称模糊搜索
     */
    private String keyword;
}
