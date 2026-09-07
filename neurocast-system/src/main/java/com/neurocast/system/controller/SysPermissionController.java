package com.neurocast.system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.Result;
import com.neurocast.system.domain.dto.SysPermissionDto;
import com.neurocast.system.domain.vo.SysPermissionVo;
import com.neurocast.system.service.SysPermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 统一权限标识管理接口
 *
 * <p>提供权限的 CRUD 和角色-权限分配功能。
 * 用户权限（type=1）和 API Scope（type=2）共用同一张表统一管理。
 */
@RestController
@RequestMapping("/api/admin/system/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    /**
     * 查询权限标识列表，支持按类型过滤
     *
     * @param type 权限类型（1=用户权限, 2=API Scope），不传则返回全部
     */
    @GetMapping
    public Result<List<SysPermissionVo>> list(@RequestParam(required = false) Integer type) {
        return Result.ok(sysPermissionService.list(type));
    }

    /**
     * 新增权限标识
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody SysPermissionDto dto) {
        sysPermissionService.create(dto);
        return Result.ok();
    }

    /**
     * 删除权限（同时删除角色关联）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysPermissionService.delete(id);
        return Result.ok();
    }

    /**
     * 启用/禁用权限
     *
     * @param id     权限 ID
     * @param active 是否启用
     */
    @PutMapping("/{id}/active")
    public Result<Void> updateActive(@PathVariable Long id, @RequestParam Boolean active) {
        sysPermissionService.updateActive(id, active);
        return Result.ok();
    }
}
