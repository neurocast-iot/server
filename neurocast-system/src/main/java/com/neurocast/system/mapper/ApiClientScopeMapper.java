package com.neurocast.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.system.domain.ApiClientScope;
import org.apache.ibatis.annotations.Mapper;

/**
 * API 客户端权限范围 Mapper
 */
@Mapper
public interface ApiClientScopeMapper extends BaseMapper<ApiClientScope> {
}
