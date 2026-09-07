package com.neurocast.system.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.RedisKeys;
import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.common.utils.IdUtils;
import com.neurocast.system.domain.ApiClient;
import com.neurocast.system.domain.ApiClientScope;
import com.neurocast.system.domain.dto.ApiClientDto;
import com.neurocast.system.domain.dto.ApiClientSearchQuery;
import com.neurocast.system.domain.vo.ApiClientVo;
import com.neurocast.system.mapper.ApiClientMapper;
import com.neurocast.system.mapper.ApiClientScopeMapper;
import com.neurocast.system.mapper.SysPermissionMapper;
import com.neurocast.system.service.ApiClientService;

import lombok.RequiredArgsConstructor;

/**
 * API 客户端服务实现
 */
@Service
@RequiredArgsConstructor
public class ApiClientServiceImpl implements ApiClientService {

    private final ApiClientMapper apiClientMapper;
    private final ApiClientScopeMapper apiClientScopeMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult<ApiClientVo> page(ApiClientSearchQuery query) {
        LambdaQueryWrapper<ApiClient> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(ApiClient::getClientCode, query.getKeyword())
                    .or().like(ApiClient::getClientName, query.getKeyword()));
        }
        wrapper.orderByDesc(ApiClient::getId);
        Page<ApiClient> page = apiClientMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(),
                page.getRecords().stream().map(ApiClientVo::from).toList());
    }

    @Override
    public ApiClientVo create(ApiClientDto clientDto) {
        Long count = apiClientMapper.selectCount(new LambdaQueryWrapper<ApiClient>()
                .eq(ApiClient::getClientCode, clientDto.clientCode()));
        if (count > 0) {
            throw new ServiceException(ApiStatus.DUPLICATE_KEY, "客户端编码已存在");
        }

        ApiClient apiClient = new ApiClient();
        apiClient.setClientCode(clientDto.clientCode());
        apiClient.setClientName(clientDto.clientName());
        apiClient.setApiKey(IdUtils.simpleUuid());
        apiClient.setStatus(clientDto.status() == null ? 1 : clientDto.status());
        apiClient.setExpireTime(clientDto.expireTime());
        apiClient.setRemark(clientDto.remark());
        apiClientMapper.insert(apiClient);

        // 保存权限范围
        saveScopes(apiClient.getId(), clientDto.scopes());
        return ApiClientVo.from(apiClient);
    }

    @Override
    public void update(ApiClientDto clientDto) {
        if (clientDto.id() == null) {
            throw new ServiceException(ApiStatus.VALIDATE_FAILED, "客户端 ID 不能为空");
        }
        LambdaUpdateWrapper<ApiClient> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ApiClient::getId, clientDto.id());
        wrapper.set(ApiClient::getClientName, clientDto.clientName());
        if (clientDto.status() != null) {
            wrapper.set(ApiClient::getStatus, clientDto.status());
        }
        if (clientDto.expireTime() != null) {
            wrapper.set(ApiClient::getExpireTime, clientDto.expireTime());
        }
        if (clientDto.remark() != null) {
            wrapper.set(ApiClient::getRemark, clientDto.remark());
        }
        apiClientMapper.update(null, wrapper);

        // 更新权限范围
        if (clientDto.scopes() != null) {
            saveScopes(clientDto.id(), clientDto.scopes());
        }
    }

    @Override
    public void delete(Long clientId) {
        ApiClient apiClient = getClientOrThrow(clientId);
        apiClientMapper.deleteById(clientId);
        deleteScopes(clientId);
        evictApiKeyCache(apiClient.getApiKey());
    }

    @Override
    public ApiClientVo regenerateKey(Long clientId) {
        ApiClient apiClient = getClientOrThrow(clientId);
        evictApiKeyCache(apiClient.getApiKey());
        apiClient.setApiKey(IdUtils.simpleUuid());
        apiClientMapper.updateById(apiClient);
        return ApiClientVo.from(apiClient);
    }

    /**
     * 先删后插：替换客户端的全部权限范围，并校验 scope 合法性
     */
    private void saveScopes(Long clientId, java.util.List<String> scopes) {
        deleteScopes(clientId);
        if (scopes != null && !scopes.isEmpty()) {
            // 校验所有 scope 是否在 sys_permission(type=2) 中已注册
            java.util.Set<String> validScopes = java.util.Set.copyOf(
                    sysPermissionMapper.selectAllApiScopeCodes());
            for (String scope : scopes) {
                if (!validScopes.contains(scope)) {
                    throw new ServiceException(ApiStatus.VALIDATE_FAILED,
                            "无效的 API Scope：" + scope + "，请先在权限管理中注册");
                }
            }
            for (String scope : scopes) {
                ApiClientScope entity = new ApiClientScope();
                entity.setClientId(clientId);
                entity.setScope(scope);
                apiClientScopeMapper.insert(entity);
            }
        }
    }

    private void deleteScopes(Long clientId) {
        apiClientScopeMapper.delete(new LambdaQueryWrapper<ApiClientScope>()
                .eq(ApiClientScope::getClientId, clientId));
    }

    /**
     * 删除 API Key 缓存，避免禁用/删除后旧 Key 在缓存期内仍可用
     */
    private void evictApiKeyCache(String apiKey) {
        redisTemplate.delete(RedisKeys.ApiClient.API_KEY + apiKey);
    }

    private ApiClient getClientOrThrow(Long clientId) {
        if (clientId == null) {
            throw new ServiceException(ApiStatus.VALIDATE_FAILED, "客户端 ID 不能为空");
        }
        ApiClient apiClient = apiClientMapper.selectById(clientId);
        if (apiClient == null) {
            throw new ServiceException(ApiStatus.BUSINESS_API_CLIENT_NOT_EXISTED);
        }
        return apiClient;
    }
}
