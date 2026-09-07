package com.neurocast.device.service;

/**
 * 设备事件处理服务：处理 ThingsBoard webhook 推送的各类事件（上下线、属性变更等）
 */
public interface DeviceEventService {

    /**
     * 处理设备上下线事件（TB ACTIVITY_EVENT webhook 驱动）
     */
    void onStatusEvent(String tbDeviceId, boolean active);
}
