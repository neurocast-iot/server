package com.neurocast.system.service;

import com.neurocast.system.domain.dto.SysPermissionDto;
import com.neurocast.system.domain.vo.SysPermissionVo;

import java.util.List;

/**
 * 权限标识服务接口
 */
public interface SysPermissionService {

    /**
     * 查询权限标识列表，支持按类型过滤
     *
     * @param type 权限类型（1=用户权限, 2=API Scope），为 null 时返回全部
     */
    List<SysPermissionVo> list(Integer type);

    /**
     * 新增权限标识
     *
     * @throws com.neurocast.common.exception.ServiceException 权限标识已存在时抛出
     */
    void create(SysPermissionDto dto);

    /**
     * 删除权限（同时删除角色关联）
     *
     * @throws com.neurocast.common.exception.ServiceException 权限不存在时抛出
     */
    void delete(Long id);

    /**
     * 启用/禁用权限
     *
     * @param id     权限 ID
     * @param active 是否启用
     * @throws com.neurocast.common.exception.ServiceException 权限不存在时抛出
     */
    void updateActive(Long id, Boolean active);
}
