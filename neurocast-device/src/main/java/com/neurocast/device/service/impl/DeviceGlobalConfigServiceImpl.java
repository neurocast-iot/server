package com.neurocast.device.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.TbScope;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.device.domain.Device;
import com.neurocast.device.domain.DeviceGlobalConfig;
import com.neurocast.device.domain.dto.DeviceGlobalConfigUpdateDto;
import com.neurocast.device.domain.vo.DeviceGlobalConfigVo;
import com.neurocast.device.mapper.DeviceGlobalConfigMapper;
import com.neurocast.device.mapper.DeviceMapper;
import com.neurocast.device.service.DeviceGlobalConfigService;
import com.neurocast.framework.integration.thingsboard.ThingsBoardTelemetryClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备全局配置服务实现（完全动态模式：前端可自由新增/删除配置项，键格式由 DTO 校验）。
 * 更新流程：与当前生效值差异比对 → 落库 → 变更项批量写入所有设备的
 * SHARED_SCOPE 共享属性（TB 自动推送在线设备，离线设备上线后拉取最新值）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceGlobalConfigServiceImpl implements DeviceGlobalConfigService {

    private final DeviceGlobalConfigMapper globalConfigMapper;
    private final DeviceMapper deviceMapper;
    private final ThingsBoardTelemetryClient telemetryClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<DeviceGlobalConfigVo> list() {
        List<DeviceGlobalConfig> configs = globalConfigMapper.selectList(null);
        List<DeviceGlobalConfigVo> result = new ArrayList<>();
        for (DeviceGlobalConfig config : configs) {
            result.add(DeviceGlobalConfigVo.from(config));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DeviceGlobalConfigUpdateDto dto) {
        // 1. 一次查全表，差异比对（与当前生效值比较，避免重复下发）
        Map<String, String> currentValues = getConfigMap();
        Map<String, String> changes = new HashMap<>();
        Map<String, String> descriptions = new HashMap<>();
        for (DeviceGlobalConfigUpdateDto.Item item : dto.items()) {
            if (!item.value().equals(currentValues.get(item.key()))) {
                changes.put(item.key(), item.value());
            }
            descriptions.put(item.key(), item.description());
        }
        if (changes.isEmpty()) {
            return;
        }

        // 2. 落库（新增或更新；描述直接以前端传入值为准）
        for (Map.Entry<String, String> entry : changes.entrySet()) {
            DeviceGlobalConfig config = new DeviceGlobalConfig();
            config.setConfigKey(entry.getKey());
            config.setConfigValue(entry.getValue());
            config.setDescription(descriptions.get(entry.getKey()));
            globalConfigMapper.insertOrUpdate(config);
        }

        // 3. 批量下发所有设备（一次推送携带全部变更键）
        pushToAllDevices(changes);
        log.info("设备全局配置已更新并下发：changes={}", changes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String key) {
        DeviceGlobalConfig config = globalConfigMapper.selectById(key);
        if (config == null) {
            throw new ServiceException(ApiStatus.BUSINESS_DEVICE_GLOBAL_CONFIG_NOT_EXISTED);
        }
        globalConfigMapper.deleteById(key);
        // 从所有设备移除该属性（TB 通知在线设备属性已删除）
        List<Device> devices = deviceMapper.selectList(null);
        for (Device device : devices) {
            try {
                telemetryClient.deleteAttributes(device.getTbDeviceId(), TbScope.SHARED_SCOPE, List.of(key));
            } catch (Exception e) {
                log.error("全局配置属性删除失败：deviceUid={}, key={}", device.getDeviceUid(), key, e);
            }
        }
        log.info("设备全局配置已删除：key={}, 设备数={}", key, devices.size());
    }

    @Override
    public Map<String, String> getConfigMap() {
        Map<String, String> values = new HashMap<>();
        for (DeviceGlobalConfig config : globalConfigMapper.selectList(null)) {
            values.put(config.getConfigKey(), config.getConfigValue());
        }
        return values;
    }

    /**
     * 将变更项写入所有设备的 SHARED_SCOPE 共享属性（附带 last_updated_time 供设备感知更新）
     */
    private void pushToAllDevices(Map<String, String> changes) {
        Map<String, Object> params = new HashMap<>(changes);
        params.put("last_updated_time", System.currentTimeMillis() / 1000);
        JsonNode jsonNode = objectMapper.valueToTree(params);

        List<Device> devices = deviceMapper.selectList(null);
        for (Device device : devices) {
            try {
                telemetryClient.saveAttributes(device.getTbDeviceId(), TbScope.SHARED_SCOPE, jsonNode);
            } catch (Exception e) {
                // 单台设备失败不阻断整体下发，TB 已持久化的属性可在设备上线后拉取
                log.error("全局配置下发失败：deviceUid={}", device.getDeviceUid(), e);
            }
        }
    }
}
