package com.neurocast.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neurocast.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 系统角色（ADMIN 管理员 / OPERATOR 操作员 / VIEWER 观察者）
 */
@Getter
@Setter
@TableName("sys_role")
public class SysRole extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码，如 ADMIN
     */
    private String roleCode;

    /**
     * 角色名称，如 管理员
     */
    private String roleName;

    /**
     * 状态：1 启用 0 禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
