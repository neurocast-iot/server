package com.neurocast.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.device.domain.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品 Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
