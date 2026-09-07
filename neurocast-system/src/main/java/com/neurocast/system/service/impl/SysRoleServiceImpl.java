package com.neurocast.system.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.system.domain.SysRole;
import com.neurocast.system.domain.SysUser;
import com.neurocast.system.domain.dto.SysRoleDto;
import com.neurocast.system.domain.vo.SysPermissionVo;
import com.neurocast.system.domain.vo.SysRoleVo;
import com.neurocast.system.mapper.SysRoleMapper;
import com.neurocast.system.mapper.SysUserMapper;
import com.neurocast.system.service.SysRoleService;

import lombok.RequiredArgsConstructor;

/**
 * 系统角色服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<SysRoleVo> listEnabled() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getId))
                .stream()
                .map(SysRoleVo::from)
                .toList();
    }

    @Override
    public void create(SysRoleDto dto) {
        checkRoleCodeUnique(dto.roleCode(), null);

        SysRole role = new SysRole();
        role.setRoleCode(dto.roleCode());
        role.setRoleName(dto.roleName());
        role.setStatus(dto.status() == null ? 1 : dto.status());
        role.setRemark(dto.remark());
        sysRoleMapper.insert(role);
    }

    @Override
    public void update(SysRoleDto dto) {
        SysRole role = getRoleOrThrow(dto.id());
        // 角色编码变更时校验唯一性
        if (!role.getRoleCode().equals(dto.roleCode())) {
            checkRoleCodeUnique(dto.roleCode(), role.getId());
            role.setRoleCode(dto.roleCode());
        }
        role.setRoleName(dto.roleName());
        if (dto.status() != null) {
            role.setStatus(dto.status());
        }
        role.setRemark(dto.remark());
        sysRoleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long roleId) {
        SysRole role = getRoleOrThrow(roleId);
        // 检查是否有用户正在使用该角色
        Long userCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRoleId, roleId));
        if (userCount > 0) {
            throw new ServiceException(ApiStatus.BUSINESS_ROLE_IN_USE,
                    "角色「" + role.getRoleName() + "」正在被 " + userCount + " 个用户使用，无法删除");
        }
        // 清理角色-权限关联
        sysRoleMapper.deleteRolePermissions(roleId);
        // 删除角色本身
        sysRoleMapper.deleteById(roleId);
    }

    @Override
    public List<SysPermissionVo> getRolePermissions(Long roleId) {
        getRoleOrThrow(roleId);
        return sysRoleMapper.selectPermissionsByRoleId(roleId)
                .stream()
                .map(SysPermissionVo::from)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(Long roleId, List<Long> permissionIds) {
        getRoleOrThrow(roleId);
        // 全量替换：先清空再批量插入
        sysRoleMapper.deleteRolePermissions(roleId);
        if (permissionIds != null) {
            for (Long permissionId : permissionIds) {
                sysRoleMapper.assignRolePermission(roleId, permissionId);
            }
        }
    }

    /**
     * 校验角色编码唯一性
     *
     * @param roleCode 角色编码
     * @param excludeId 排除的角色 ID（更新时传当前 ID，创建时传 null）
     */
    private void checkRoleCodeUnique(String roleCode, Long excludeId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode);
        if (excludeId != null) {
            wrapper.ne(SysRole::getId, excludeId);
        }
        Long count = sysRoleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new ServiceException(ApiStatus.BUSINESS_ROLE_CODE_EXISTED);
        }
    }

    private SysRole getRoleOrThrow(Long roleId) {
        if (roleId == null) {
            throw new ServiceException(ApiStatus.VALIDATE_FAILED, "角色 ID 不能为空");
        }
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException(ApiStatus.BUSINESS_ROLE_NOT_EXISTED);
        }
        return role;
    }
}
