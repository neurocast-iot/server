package com.neurocast.system.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * API 客户端创建/更新请求
 *
 * @param id         客户端 ID（更新时必填）
 * @param clientCode 客户端编码（创建时必填）
 * @param clientName 客户端名称
 * @param scopes     权限范围列表，如 ["ota:read", "device:read", "event:push"]
 * @param status     状态：1 启用 0 禁用
 * @param expireTime 过期时间（为空表示永不过期）
 * @param remark     备注
 */
public record ApiClientDto(
        Long id,
        @NotBlank(message = "clientCode 客户端编码不能为空", groups = Create.class)
        @Size(max = 64, message = "客户端编码长度不能超过64")
        String clientCode,
        @Size(max = 128, message = "客户端名称长度不能超过128")
        String clientName,
        List<String> scopes,
        Integer status,
        OffsetDateTime expireTime,
        @Size(max = 256, message = "备注长度不能超过256")
        String remark
) {
    /** 创建校验分组 */
    public interface Create {}

    /** 更新校验分组 */
    public interface Update {}
}
