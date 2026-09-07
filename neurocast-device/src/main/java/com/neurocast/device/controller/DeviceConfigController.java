package com.neurocast.device.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.Result;
import com.neurocast.device.domain.dto.DeviceConfigDto;
import com.neurocast.device.domain.dto.DeviceGlobalConfigUpdateDto;
import com.neurocast.device.domain.dto.DeviceOsdConfigDto;
import com.neurocast.device.domain.dto.DeviceTriggerConfigDto;
import com.neurocast.device.domain.vo.DeviceConfigVo;
import com.neurocast.device.domain.vo.DeviceGlobalConfigVo;
import com.neurocast.device.domain.vo.DeviceOsdConfigVo;
import com.neurocast.device.service.DeviceConfigService;
import com.neurocast.device.service.DeviceGlobalConfigService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 设备配置接口（配置经 ThingsBoard SHARED_SCOPE 属性自动推送到设备）
 */
@RestController
@RequestMapping("/api/admin/device/config")
@RequiredArgsConstructor
public class DeviceConfigController {

    private final DeviceConfigService deviceConfigService;
    private final DeviceGlobalConfigService deviceGlobalConfigService;

    /**
     * 查询设备全局配置（所有设备通用）
     */
    @GetMapping("/global")
    public Result<List<DeviceGlobalConfigVo>> getGlobalConfig() {
        return Result.ok(deviceGlobalConfigService.list());
    }

    /**
     * 新增或更新设备全局配置（变更项自动下发到所有设备，支持动态新增配置项）
     */
    @PutMapping("/global")
    public Result<Void> updateGlobalConfig(@Valid @RequestBody DeviceGlobalConfigUpdateDto dto) {
        deviceGlobalConfigService.update(dto);
        return Result.ok();
    }

    /**
     * 删除设备全局配置（同时从所有设备移除对应属性）
     */
    @DeleteMapping("/global")
    public Result<Void> deleteGlobalConfig(@RequestParam String key) {
        deviceGlobalConfigService.delete(key);
        return Result.ok();
    }

    /**
     * 查询设备业务配置
     */
    @GetMapping("/settings/{deviceUid}")
    public Result<DeviceConfigVo> getSettings(@PathVariable String deviceUid) {
        return Result.ok(deviceConfigService.getSettings(deviceUid));
    }

    /**
     * 下发设备业务配置（差异下发）
     */
    @PutMapping("/settings/{deviceUid}")
    public Result<Void> setSettings(@PathVariable String deviceUid,
                                    @Valid @RequestBody DeviceConfigDto configDto) {
        deviceConfigService.setSettings(deviceUid, configDto);
        return Result.ok();
    }

    /**
     * 查询 OSD 隐私水印配置
     */
    @GetMapping("/osd/{deviceUid}")
    public Result<DeviceOsdConfigVo> getOsd(@PathVariable String deviceUid) {
        return Result.ok(deviceConfigService.getOsdConfig(deviceUid));
    }

    /**
     * 下发 OSD 隐私水印配置
     */
    @PutMapping("/osd/{deviceUid}")
    public Result<Void> setOsd(@PathVariable String deviceUid,
                               @Valid @RequestBody DeviceOsdConfigDto osdConfigDto) {
        deviceConfigService.setOsdElements(deviceUid, osdConfigDto);
        return Result.ok();
    }

    /**
     * 查询事件触发器配置
     */
    @GetMapping("/triggers/{deviceUid}")
    public Result<List<DeviceTriggerConfigDto.Trigger>> getTriggers(@PathVariable String deviceUid) {
        return Result.ok(deviceConfigService.getTriggers(deviceUid));
    }

    /**
     * 下发事件触发器配置（整体替换）
     */
    @PutMapping("/triggers/{deviceUid}")
    public Result<Void> setTriggers(@PathVariable String deviceUid,
                                    @Valid @RequestBody DeviceTriggerConfigDto dto) {
        deviceConfigService.setTriggers(deviceUid, dto);
        return Result.ok();
    }
}
