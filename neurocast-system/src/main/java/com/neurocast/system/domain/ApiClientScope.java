package com.neurocast.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * API 客户端权限范围（与 api_client 多对多）
 */
@Getter
@Setter
@TableName("api_client_scope")
public class ApiClientScope implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 客户端 ID
     */
    private Long clientId;

    /**
     * 权限范围标识，如 ota:read、device:read、event:push
     */
    private String scope;
}
