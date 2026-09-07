package com.neurocast.system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.core.domain.Result;
import com.neurocast.system.domain.dto.ResetPasswordDto;
import com.neurocast.system.domain.dto.SysUserDto;
import com.neurocast.system.domain.dto.SysUserSearchQuery;
import com.neurocast.system.domain.vo.SysUserVo;
import com.neurocast.system.service.SysUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 用户管理接口（仅管理员）
 */
@RestController
@RequestMapping("/api/admin/system/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户
     */
    @GetMapping
    public Result<PageResult<SysUserVo>> page(SysUserSearchQuery query) {
        return Result.ok(sysUserService.page(query));
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody SysUserDto userDto) {
        sysUserService.create(userDto);
        return Result.ok();
    }

    /**
     * 更新用户
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SysUserDto userDto) {
        sysUserService.update(userDto);
        return Result.ok();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public Result<Void> delete(@PathVariable Long userId) {
        sysUserService.delete(userId);
        return Result.ok();
    }

    /**
     * 重置密码
     */
    @PutMapping("/{userId}/password")
    public Result<Void> resetPassword(@PathVariable Long userId,
                                      @Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        sysUserService.resetPassword(userId, resetPasswordDto);
        return Result.ok();
    }
}
