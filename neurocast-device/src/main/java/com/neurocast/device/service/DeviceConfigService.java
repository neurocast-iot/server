package com.neurocast.device.service;

import com.neurocast.device.domain.dto.DeviceConfigDto;
import com.neurocast.device.domain.dto.DeviceOsdConfigDto;
import com.neurocast.device.domain.dto.DeviceTriggerConfigDto;
import com.neurocast.device.domain.vo.DeviceConfigVo;
import com.neurocast.device.domain.vo.DeviceOsdConfigVo;

import java.util.List;

/**
 * 设备配置服务：SHARED_SCOPE 属性读写（写入后 TB 自动推送给设备）
 */
public interface DeviceConfigService {

    /**
     * 下发初始配置（设备创建后调用）
     */
    void initDeviceConfig(String deviceUid);

    /**
     * 查询设备业务配置
     */
    DeviceConfigVo getSettings(String deviceUid);

    /**
     * 下发设备业务配置（与当前配置差异比对，仅下发变更项）
     */
    void setSettings(String deviceUid, DeviceConfigDto configDto);

    /**
     * 查询 OSD 隐私水印配置
     */
    DeviceOsdConfigVo getOsdConfig(String deviceUid);

    /**
     * 下发 OSD 隐私水印配置
     */
    void setOsdElements(String deviceUid, DeviceOsdConfigDto osdConfigDto);

    /**
     * 查询事件触发器配置
     */
    List<DeviceTriggerConfigDto.Trigger> getTriggers(String deviceUid);

    /**
     * 下发事件触发器配置（整体替换）
     */
    void setTriggers(String deviceUid, DeviceTriggerConfigDto dto);
}
