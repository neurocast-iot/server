package com.neurocast.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neurocast.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.OffsetDateTime;

/**
 * API 客户端（内部系统/设备接入凭证，通过 X-API-KEY 请求头认证）
 */
@Getter
@Setter
@TableName("api_client")
public class ApiClient extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 客户端编码，如 inner、device
     */
    private String clientCode;

    /**
     * 客户端名称
     */
    private String clientName;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 状态：1 启用 0 禁用
     */
    private Integer status;

    /**
     * 过期时间（为空表示永不过期）
     */
    private OffsetDateTime expireTime;

    /**
     * 备注
     */
    private String remark;
}
