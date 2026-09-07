package com.neurocast.device.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.neurocast.device.domain.dto.DeviceStatusEventDto;
import com.neurocast.device.service.DeviceEventService;
import com.neurocast.framework.event.ExternalEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备事件异步监听器。
 * <p>
 * 监听 ExternalEvent，根据 type 字段分发到对应的处理逻辑。
 * 使用 @Async 在独立线程池中执行，不阻塞 HTTP 响应。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceEventListener {

    private static final String TYPE_CONNECTED = "device.connected";
    private static final String TYPE_DISCONNECTED = "device.disconnected";

    private final DeviceEventService deviceEventService;

    @Async("eventTaskExecutor")
    @EventListener(condition = "#event.type.startsWith('device.')")
    public void onDeviceEvent(ExternalEvent event) {
        JSONObject payload = event.getPayload();
        switch (event.getType()) {
            case TYPE_CONNECTED, TYPE_DISCONNECTED -> {
                DeviceStatusEventDto dto = payload.toJavaObject(DeviceStatusEventDto.class);
                boolean online = TYPE_CONNECTED.equals(event.getType());
                deviceEventService.onStatusEvent(dto.getTbDeviceId(), online);
            }
            default -> log.debug("设备模块忽略事件：type={}", event.getType());
        }
    }
}
