package com.neurocast.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.system.domain.SysConfig;
import com.neurocast.system.domain.dto.SysConfigUpdateDto;
import com.neurocast.system.domain.vo.SysConfigVo;
import com.neurocast.system.mapper.SysConfigMapper;
import com.neurocast.system.service.SysConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 系统配置服务实现（完全动态模式：前端可自由新增/删除配置项，键格式由 DTO 校验）。
 * 与设备全局配置不同：系统配置仅供服务端自身读取，不涉及任何设备下发。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private final SysConfigMapper sysConfigMapper;

    @Override
    public List<SysConfigVo> list() {
        List<SysConfig> configs = sysConfigMapper.selectList(null);
        List<SysConfigVo> result = new ArrayList<>();
        for (SysConfig config : configs) {
            result.add(SysConfigVo.from(config));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysConfigUpdateDto dto) {
        // 1. 一次查全表，差异比对（与当前生效值比较，值未变化的项跳过）
        Map<String, String> currentValues = getConfigMap();
        Map<String, String> changes = new HashMap<>();
        Map<String, String> descriptions = new HashMap<>();
        for (SysConfigUpdateDto.Item item : dto.items()) {
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
            SysConfig config = new SysConfig();
            config.setConfigKey(entry.getKey());
            config.setConfigValue(entry.getValue());
            config.setDescription(descriptions.get(entry.getKey()));
            sysConfigMapper.insertOrUpdate(config);
        }
        log.info("系统配置已更新：changes={}", changes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String key) {
        SysConfig config = sysConfigMapper.selectById(key);
        if (config == null) {
            throw new ServiceException(ApiStatus.BUSINESS_SYS_CONFIG_NOT_EXISTED);
        }
        sysConfigMapper.deleteById(key);
        log.info("系统配置已删除：key={}", key);
    }

    @Override
    public Map<String, String> getConfigMap() {
        Map<String, String> values = new HashMap<>();
        for (SysConfig config : sysConfigMapper.selectList(null)) {
            values.put(config.getConfigKey(), config.getConfigValue());
        }
        return values;
    }
}
