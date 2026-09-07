package com.neurocast.system.domain.vo;

import com.neurocast.system.domain.ApiClient;

import java.time.OffsetDateTime;

/**
 * API 客户端响应
 *
 * @param id         客户端 ID
 * @param clientCode 客户端编码
 * @param clientName 客户端名称
 * @param apiKey     API Key（仅创建和重新生成时返回）
 * @param active     是否启用
 * @param expireTime 过期时间（为空表示永不过期）
 * @param remark     备注
 * @param createTime 创建时间
 */
public record ApiClientVo(
        Long id,
        String clientCode,
        String clientName,
        String apiKey,
        Boolean active,
        OffsetDateTime expireTime,
        String remark,
        OffsetDateTime createTime
) {
    /**
     * 实体转 VO
     */
    public static ApiClientVo from(ApiClient entity) {
        return new ApiClientVo(
                entity.getId(),
                entity.getClientCode(),
                entity.getClientName(),
                entity.getApiKey(),
                entity.getStatus() == 1,
                entity.getExpireTime(),
                entity.getRemark(),
                entity.getCreateTime()
        );
    }
}
