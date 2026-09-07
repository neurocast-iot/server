package com.neurocast.system.service;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.system.domain.dto.ApiClientDto;
import com.neurocast.system.domain.dto.ApiClientSearchQuery;
import com.neurocast.system.domain.vo.ApiClientVo;

/**
 * API 客户端服务
 */
public interface ApiClientService {

    /**
     * 分页查询
     */
    PageResult<ApiClientVo> page(ApiClientSearchQuery query);

    /**
     * 创建客户端（自动生成 API Key）
     */
    ApiClientVo create(ApiClientDto clientDto);

    /**
     * 更新客户端
     */
    void update(ApiClientDto clientDto);

    /**
     * 删除客户端
     */
    void delete(Long clientId);

    /**
     * 重新生成 API Key，返回更新后的客户端
     */
    ApiClientVo regenerateKey(Long clientId);
}
