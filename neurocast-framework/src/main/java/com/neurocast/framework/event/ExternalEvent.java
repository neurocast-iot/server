package com.neurocast.framework.event;

import org.springframework.context.ApplicationEvent;

import com.alibaba.fastjson2.JSONObject;

/**
 * 外部系统推送的统一事件（ThingsBoard 规则引擎等）。
 * <p>
 * Controller 接收 JSON 后发布此事件，各模块通过 @EventListener + @Async 异步处理，
 * 实现接收与处理的解耦：调用方立即收到响应，实际处理在独立线程中完成。
 */
public class ExternalEvent extends ApplicationEvent {

    /** 事件类型（如 "device.connected"、"device.disconnected"） */
    private final String type;

    /** 原始 JSON 载荷，由各模块 Listener 自行反序列化为具体 DTO */
    private final JSONObject payload;

    public ExternalEvent(Object source, String type, JSONObject payload) {
        super(source);
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public JSONObject getPayload() {
        return payload;
    }
}
