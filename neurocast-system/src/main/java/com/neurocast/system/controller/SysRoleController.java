package com.neurocast.system.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.Result;
import com.neurocast.system.domain.dto.SysRoleDto;
import com.neurocast.system.domain.vo.SysPermissionVo;
import com.neurocast.system.domain.vo.SysRoleVo;
import com.neurocast.system.service.SysRoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 角色管理接口（仅管理员）
 */
@RestController
@RequestMapping("/api/admin/system/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    /**
     * 查询全部启用角色（用于下拉选择）
     */
    @GetMapping
    public Result<List<SysRoleVo>> list() {
        return Result.ok(sysRoleService.listEnabled());
    }

    /**
     * 创建角色
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody SysRoleDto dto) {
        sysRoleService.create(dto);
        return Result.ok();
    }

    /**
     * 更新角色
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SysRoleDto dto) {
        sysRoleService.update(dto);
        return Result.ok();
    }

    /**
     * 删除角色（同时清理用户关联和权限关联）
     */
    @DeleteMapping("/{roleId}")
    public Result<Void> delete(@PathVariable Long roleId) {
        sysRoleService.delete(roleId);
        return Result.ok();
    }

    /**
     * 查询角色的权限列表（含完整字段）
     */
    @GetMapping("/{roleId}/permissions")
    public Result<List<SysPermissionVo>> getRolePermissions(@PathVariable Long roleId) {
        return Result.ok(sysRoleService.getRolePermissions(roleId));
    }

    /**
     * 为角色分配权限（全量替换）
     */
    @PutMapping("/{roleId}/permissions")
    public Result<Void> assignRolePermissions(@PathVariable Long roleId,
                                              @RequestBody List<Long> permissionIds) {
        sysRoleService.assignRolePermissions(roleId, permissionIds);
        return Result.ok();
    }
}
