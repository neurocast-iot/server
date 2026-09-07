package com.neurocast.device.domain.dto;

import com.neurocast.common.core.domain.PageQuery;

import lombok.Getter;
import lombok.Setter;

/**
 * 产品分页查询条件
 */
@Getter
@Setter
public class ProductSearchQuery extends PageQuery {

    /**
     * 产品名称/型号模糊查询
     */
    private String keyword;
}
