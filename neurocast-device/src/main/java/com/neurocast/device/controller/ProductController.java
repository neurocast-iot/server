package com.neurocast.device.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.core.domain.Result;
import com.neurocast.device.domain.dto.ProductCreateDto;
import com.neurocast.device.domain.dto.ProductSearchQuery;
import com.neurocast.device.domain.vo.ProductVo;
import com.neurocast.device.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 产品管理接口
 */
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 创建产品
     */
    @PostMapping
    public Result<ProductVo> create(@Valid @RequestBody ProductCreateDto createDto) {
        return Result.ok(productService.create(createDto));
    }

    /**
     * 更新产品
     */
    @PutMapping("/{productId}")
    public Result<Void> update(@PathVariable String productId,
                               @Valid @RequestBody ProductCreateDto updateDto) {
        productService.update(productId, updateDto);
        return Result.ok();
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/{productId}")
    public Result<Void> delete(@PathVariable String productId) {
        productService.delete(productId);
        return Result.ok();
    }

    /**
     * 分页查询产品
     */
    @GetMapping("/list")
    public Result<PageResult<ProductVo>> list(ProductSearchQuery query) {
        return Result.ok(productService.page(query));
    }

    /**
     * 查询产品详情
     */
    @GetMapping("/{productId}")
    public Result<ProductVo> get(@PathVariable String productId) {
        return Result.ok(productService.get(productId));
    }
}
