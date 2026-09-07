package com.neurocast.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.system.domain.ApiClient;
import org.apache.ibatis.annotations.Mapper;

/**
 * API 客户端 Mapper
 */
@Mapper
public interface ApiClientMapper extends BaseMapper<ApiClient> {
}
