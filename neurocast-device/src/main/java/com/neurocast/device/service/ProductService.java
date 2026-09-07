package com.neurocast.device.service;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.device.domain.Product;
import com.neurocast.device.domain.dto.ProductCreateDto;
import com.neurocast.device.domain.dto.ProductSearchQuery;
import com.neurocast.device.domain.vo.ProductVo;

import java.util.List;

/**
 * 产品管理服务
 */
public interface ProductService {

    /**
     * 创建产品
     */
    ProductVo create(ProductCreateDto createDto);

    /**
     * 更新产品
     */
    void update(String productId, ProductCreateDto updateDto);

    /**
     * 删除产品（有设备关联时禁止删除）
     */
    void delete(String productId);

    /**
     * 分页查询产品
     */
    PageResult<ProductVo> page(ProductSearchQuery query);

    /**
     * 查询产品详情
     */
    ProductVo get(String productId);

    /**
     * 根据 ID 获取产品实体（内部使用）
     */
    Product getById(String productId);
}
