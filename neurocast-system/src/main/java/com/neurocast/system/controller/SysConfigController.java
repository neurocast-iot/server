package com.neurocast.system.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.Result;
import com.neurocast.system.domain.dto.SysConfigUpdateDto;
import com.neurocast.system.domain.vo.SysConfigVo;
import com.neurocast.system.service.SysConfigService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 系统配置接口（服务端自身的 KV 配置，仅管理员）
 */
@RestController
@RequestMapping("/api/admin/system/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 查询系统配置（仅返回已配置项）
     */
    @GetMapping
    public Result<List<SysConfigVo>> list() {
        return Result.ok(sysConfigService.list());
    }

    /**
     * 新增或更新系统配置（批量提交，支持动态新增配置项）
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SysConfigUpdateDto dto) {
        sysConfigService.update(dto);
        return Result.ok();
    }

    /**
     * 删除系统配置
     */
    @DeleteMapping
    public Result<Void> delete(@RequestParam String key) {
        sysConfigService.delete(key);
        return Result.ok();
    }
}
