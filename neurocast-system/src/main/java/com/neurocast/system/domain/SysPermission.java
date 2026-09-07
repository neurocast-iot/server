package com.neurocast.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neurocast.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 统一权限标识（type: 1=用户权限, 2=API Scope）
 *
 * <p>用户权限通过角色分配给管理后台用户，API Scope 分配给 API 客户端，
 * 两者共用同一张表统一管理，命名风格一致（模块:操作）。
 */
@Getter
@Setter
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限标识，如 system:user:list、device:read
     */
    private String permissionCode;

    /**
     * 描述
     */
    private String description;

    /**
     * 权限类型：1=用户权限（RBAC），2=API Scope（客户端）
     */
    private Integer type;

    /**
     * 状态：1 启用 0 禁用
     */
    private Integer status;
}
