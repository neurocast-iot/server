package com.neurocast.device.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.common.utils.BeanConvertor;
import com.neurocast.device.domain.Device;
import com.neurocast.device.domain.Product;
import com.neurocast.device.domain.dto.ProductCreateDto;
import com.neurocast.device.domain.dto.ProductSearchQuery;
import com.neurocast.device.domain.vo.ProductVo;
import com.neurocast.device.mapper.DeviceMapper;
import com.neurocast.device.mapper.ProductMapper;
import com.neurocast.device.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 产品管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final DeviceMapper deviceMapper;

    @Override
    public ProductVo create(ProductCreateDto createDto) {
        // 校验 model 唯一性
        Product existed = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getModel, createDto.getModel()));
        if (existed != null) {
            throw new ServiceException(ApiStatus.DUPLICATE_KEY, "产品型号已存在：" + createDto.getModel());
        }

        Product product = new Product();
        product.setName(createDto.getName());
        product.setModel(createDto.getModel());
        product.setTbProfileId(createDto.getTbProfileId());
        product.setDescription(createDto.getDescription());
        productMapper.insert(product);

        log.info("产品已创建：id={}, model={}", product.getId(), product.getModel());
        return BeanConvertor.toBean(product, ProductVo.class);
    }

    @Override
    public void update(String productId, ProductCreateDto updateDto) {
        Product product = getProductOrThrow(productId);

        // 如果修改了 model，校验唯一性
        if (!product.getModel().equals(updateDto.getModel())) {
            Product existed = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                    .eq(Product::getModel, updateDto.getModel()));
            if (existed != null) {
                throw new ServiceException(ApiStatus.DUPLICATE_KEY, "产品型号已存在：" + updateDto.getModel());
            }
            product.setModel(updateDto.getModel());
        }

        product.setName(updateDto.getName());
        product.setTbProfileId(updateDto.getTbProfileId());
        product.setDescription(updateDto.getDescription());
        productMapper.updateById(product);
    }

    @Override
    public void delete(String productId) {
        getProductOrThrow(productId);

        // 检查是否有设备关联该产品
        Long count = deviceMapper.selectCount(new LambdaQueryWrapper<Device>()
                .eq(Device::getProductId, productId));
        if (count > 0) {
            throw new ServiceException(ApiStatus.BUSINESS_PRODUCT_HAS_DEVICES, "该产品下还有 " + count + " 台设备，无法删除");
        }

        productMapper.deleteById(productId);
        log.info("产品已删除：id={}", productId);
    }

    @Override
    public PageResult<ProductVo> page(ProductSearchQuery query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Product::getName, query.getKeyword())
                    .or().like(Product::getModel, query.getKeyword()));
        }
        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> page = productMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        List<ProductVo> voList = new ArrayList<>(page.getRecords().size());
        for (Product product : page.getRecords()) {
            voList.add(BeanConvertor.toBean(product, ProductVo.class));
        }
        return PageResult.of(page.getTotal(), voList);
    }

    @Override
    public ProductVo get(String productId) {
        return BeanConvertor.toBean(getProductOrThrow(productId), ProductVo.class);
    }

    @Override
    public Product getById(String productId) {
        return getProductOrThrow(productId);
    }

    private Product getProductOrThrow(String productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException(ApiStatus.BUSINESS_PRODUCT_NOT_EXISTED);
        }
        return product;
    }
}
