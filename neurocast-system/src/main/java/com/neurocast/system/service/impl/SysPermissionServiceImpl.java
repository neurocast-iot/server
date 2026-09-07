package com.neurocast.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.system.domain.SysPermission;
import com.neurocast.system.domain.dto.SysPermissionDto;
import com.neurocast.system.domain.vo.SysPermissionVo;
import com.neurocast.system.mapper.SysPermissionMapper;
import com.neurocast.system.mapper.SysRoleMapper;
import com.neurocast.system.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 权限标识服务实现
 *
 * <p>权限管理涉及 sys_permission 和 sys_role_permission 两张表，
 * 删除权限和分配权限操作需要事务保证一致性。
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<SysPermissionVo> list(Integer type) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(SysPermission::getType, type);
        }
        wrapper.orderByAsc(SysPermission::getId);
        return sysPermissionMapper.selectList(wrapper)
                .stream()
                .map(SysPermissionVo::from)
                .toList();
    }

    @Override
    public void create(SysPermissionDto dto) {
        // 检查 permission_code 唯一性
        Long count = sysPermissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getPermissionCode, dto.permissionCode()));
        if (count > 0) {
            throw new ServiceException(ApiStatus.BUSINESS_PERMISSION_CODE_EXISTED);
        }

        SysPermission entity = new SysPermission();
        entity.setPermissionCode(dto.permissionCode());
        entity.setDescription(dto.description());
        entity.setType(dto.type() != null ? dto.type() : 1); // 默认用户权限
        entity.setStatus(1); // 新建权限默认启用
        sysPermissionMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysPermission permission = getPermissionOrThrow(id);
        // 检查是否有角色正在使用该权限
        long roleCount = sysRoleMapper.countRolesByPermissionId(id);
        if (roleCount > 0) {
            throw new ServiceException(ApiStatus.BUSINESS_PERMISSION_IN_USE,
                    "权限「" + permission.getPermissionCode() + "」正在被 " + roleCount + " 个角色使用，无法删除");
        }
        sysPermissionMapper.deleteById(id);
    }

    @Override
    public void updateActive(Long id, Boolean active) {
        SysPermission permission = getPermissionOrThrow(id);
        // 契约层 Boolean 转换为存储层 Integer
        permission.setStatus(active ? 1 : 0);
        sysPermissionMapper.updateById(permission);
    }

    /**
     * 获取权限实体，不存在则抛异常
     */
    private SysPermission getPermissionOrThrow(Long id) {
        SysPermission permission = sysPermissionMapper.selectById(id);
        if (permission == null) {
            throw new ServiceException(ApiStatus.BUSINESS_PERMISSION_NOT_EXISTED);
        }
        return permission;
    }
}
