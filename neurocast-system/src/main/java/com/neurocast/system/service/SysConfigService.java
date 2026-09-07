package com.neurocast.system.service;

import java.util.List;
import java.util.Map;

import com.neurocast.system.domain.dto.SysConfigUpdateDto;
import com.neurocast.system.domain.vo.SysConfigVo;

/**
 * 系统配置服务：服务端自身的配置项（KV 存储，支持前端动态新增/删除，不下发设备）
 */
public interface SysConfigService {

    /**
     * 查询全部系统配置（仅返回已配置的配置项）
     */
    List<SysConfigVo> list();

    /**
     * 新增或更新系统配置：差异比对 → 落库（值未变化的项跳过）
     */
    void update(SysConfigUpdateDto dto);

    /**
     * 删除系统配置
     */
    void delete(String key);

    /**
     * 获取全部配置生效值（一次查表，配置项少，避免逐条查询）
     *
     * @return key -> value
     */
    Map<String, String> getConfigMap();
}
