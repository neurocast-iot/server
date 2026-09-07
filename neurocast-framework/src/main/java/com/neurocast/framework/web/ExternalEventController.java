package com.neurocast.framework.web;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.domain.Result;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.framework.event.ExternalEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 外部事件统一接收端点（X-API-KEY 认证，需 event:push scope）。
 * <p>
 * 所有来自 ThingsBoard 规则引擎的事件（设备上下线、媒体上报、OTA 进度等）
 * 统一通过此端点接收，按 body 中的 type 字段分发到各模块的 @EventListener 异步处理。
 * <p>
 * 调用方立即收到 200 响应，实际处理在独立线程池中完成。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/event")
public class ExternalEventController {

    private final ApplicationEventPublisher eventPublisher;

    @PostMapping
    @PreAuthorize("@ss.hasPermission('event:push')")
    public Result<Void> onEvent(@RequestBody JSONObject body) {
        String type = body.getString("type");
        if (type == null || type.isBlank()) {
            throw new ServiceException(ApiStatus.VALIDATE_FAILED, "type 不能为空");
        }
        log.info("收到外部事件：type={}", type);
        eventPublisher.publishEvent(new ExternalEvent(this, type, body));
        return Result.ok();
    }
}
