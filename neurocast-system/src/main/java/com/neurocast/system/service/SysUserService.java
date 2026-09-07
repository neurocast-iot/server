package com.neurocast.system.service;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.system.domain.dto.ResetPasswordDto;
import com.neurocast.system.domain.dto.SysUserDto;
import com.neurocast.system.domain.dto.SysUserSearchQuery;
import com.neurocast.system.domain.vo.SysUserVo;

/**
 * 系统用户服务
 */
public interface SysUserService {

    /**
     * 分页查询用户
     */
    PageResult<SysUserVo> page(SysUserSearchQuery query);

    /**
     * 创建用户
     */
    void create(SysUserDto userDto);

    /**
     * 更新用户（密码为空表示不修改）
     */
    void update(SysUserDto userDto);

    /**
     * 删除用户
     */
    void delete(Long userId);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, ResetPasswordDto resetPasswordDto);
}
