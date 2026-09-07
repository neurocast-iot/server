package com.neurocast.device.service.impl;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.neurocast.device.domain.Device;
import com.neurocast.device.mapper.DeviceMapper;
import com.neurocast.device.service.DeviceEventService;
import com.neurocast.device.service.DeviceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备事件处理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceEventServiceImpl implements DeviceEventService {

    private final DeviceMapper deviceMapper;
    private final DeviceService deviceService;

    @Override
    public void onStatusEvent(String tbDeviceId, boolean active) {
        Device device = deviceService.getByTbDeviceId(tbDeviceId);
        if (device == null) {
            log.warn("收到未知设备的活动事件：tbDeviceId={}", tbDeviceId);
            return;
        }
        int newStatus = active ? 1 : 0;
        if (newStatus == device.getStatus()) {
            return;
        }
        device.setStatus(newStatus);
        OffsetDateTime now = OffsetDateTime.now();
        if (active) {
            device.setLastedOnlineTime(now);
        } else {
            device.setLastedOfflineTime(now);
        }
        deviceMapper.updateById(device);
        log.info("设备状态变更：deviceUid={}, status={}", device.getDeviceUid(), active ? "online" : "offline");
    }
}
