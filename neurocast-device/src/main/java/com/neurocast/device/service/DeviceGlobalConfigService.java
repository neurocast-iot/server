package com.neurocast.device.service;

import com.neurocast.device.domain.dto.DeviceGlobalConfigUpdateDto;
import com.neurocast.device.domain.vo.DeviceGlobalConfigVo;

import java.util.List;
import java.util.Map;

/**
 * 设备全局配置服务：所有设备通用的配置项（KV 存储，支持前端动态新增/删除）。
 * 配置变更后经 ThingsBoard SHARED_SCOPE 共享属性批量下发到所有设备。
 */
public interface DeviceGlobalConfigService {

    /**
     * 查询全部全局配置（仅返回已配置的配置项）
     */
    List<DeviceGlobalConfigVo> list();

    /**
     * 新增或更新全局配置：校验 → 落库 → 将变更项批量下发到所有设备
     */
    void update(DeviceGlobalConfigUpdateDto dto);

    /**
     * 删除全局配置：删除 DB 记录，并从所有设备移除对应属性
     */
    void delete(String key);

    /**
     * 获取全部配置生效值（一次查表，配置项少，避免逐条查询）
     *
     * @return key -> value
     */
    Map<String, String> getConfigMap();
}
