package com.neurocast.system.service.impl;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.system.constant.RoleCode;
import com.neurocast.system.domain.SysRole;
import com.neurocast.system.domain.SysUser;
import com.neurocast.system.domain.dto.ResetPasswordDto;
import com.neurocast.system.domain.dto.SysUserDto;
import com.neurocast.system.domain.dto.SysUserSearchQuery;
import com.neurocast.system.domain.vo.SysUserVo;
import com.neurocast.system.mapper.SysRoleMapper;
import com.neurocast.system.mapper.SysUserMapper;
import com.neurocast.system.service.SysUserService;

import lombok.RequiredArgsConstructor;

/**
 * 系统用户服务实现
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysUserVo> page(SysUserSearchQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or().like(SysUser::getNickname, query.getKeyword()));
        }
        wrapper.orderByDesc(SysUser::getId);
        Page<SysUser> page = sysUserMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        // 批量加载角色信息，避免 N+1 查询
        Map<Long, SysRole> roleMap = loadRoleMap(page.getRecords());

        return PageResult.of(page.getTotal(),
                page.getRecords().stream()
                        .map(u -> SysUserVo.from(u, roleMap.get(u.getRoleId())))
                        .toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysUserDto userDto) {
        if (!StringUtils.hasText(userDto.password())) {
            throw new ServiceException(ApiStatus.VALIDATE_FAILED, "创建用户时密码不能为空");
        }
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, userDto.username()));
        if (count > 0) {
            throw new ServiceException(ApiStatus.BUSINESS_USER_EXISTED);
        }

        SysUser user = new SysUser();
        user.setUsername(userDto.username());
        user.setPassword(passwordEncoder.encode(userDto.password()));
        user.setNickname(userDto.nickname());
        user.setPhone(userDto.phone());
        user.setEmail(userDto.email());
        user.setRoleId(resolveRoleId(userDto.roleCode()));
        user.setStatus(userDto.status() == null ? 1 : userDto.status());
        user.setRemark(userDto.remark());
        sysUserMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserDto userDto) {
        SysUser user = getUserOrThrow(userDto.id());
        // 用户名变更时校验唯一性
        if (!user.getUsername().equals(userDto.username())) {
            Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, userDto.username()));
            if (count > 0) {
                throw new ServiceException(ApiStatus.BUSINESS_USER_EXISTED);
            }
            user.setUsername(userDto.username());
        }
        if (StringUtils.hasText(userDto.password())) {
            user.setPassword(passwordEncoder.encode(userDto.password()));
        }
        user.setNickname(userDto.nickname());
        user.setPhone(userDto.phone());
        user.setEmail(userDto.email());
        // 角色变更：传了 roleCode 才更新
        if (StringUtils.hasText(userDto.roleCode())) {
            user.setRoleId(resolveRoleId(userDto.roleCode()));
        }
        if (userDto.status() != null) {
            user.setStatus(userDto.status());
        }
        user.setRemark(userDto.remark());
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        getUserOrThrow(userId);
        sysUserMapper.deleteById(userId);
    }

    @Override
    public void resetPassword(Long userId, ResetPasswordDto resetPasswordDto) {
        SysUser user = getUserOrThrow(userId);
        user.setPassword(passwordEncoder.encode(resetPasswordDto.newPassword()));
        sysUserMapper.updateById(user);
    }

    /**
     * 根据角色编码解析角色 ID，编码为空时默认 VIEWER
     */
    private Long resolveRoleId(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            roleCode = RoleCode.VIEWER;
        }
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode));
        if (role == null) {
            throw new ServiceException(ApiStatus.VALIDATE_FAILED, "角色不存在：" + roleCode);
        }
        return role.getId();
    }

    /**
     * 批量加载用户列表中的角色，返回 roleId → SysRole 映射
     */
    private Map<Long, SysRole> loadRoleMap(java.util.List<SysUser> users) {
        var roleIds = users.stream()
                .map(SysUser::getRoleId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysRoleMapper.selectBatchIds(roleIds)
                .stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity()));
    }

    private SysUser getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new ServiceException(ApiStatus.VALIDATE_FAILED, "用户 ID 不能为空");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ApiStatus.BUSINESS_USER_NOT_EXISTED);
        }
        return user;
    }
}
