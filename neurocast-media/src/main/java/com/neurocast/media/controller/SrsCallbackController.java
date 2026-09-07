package com.neurocast.media.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.Result;
import com.neurocast.media.domain.dto.OnEventDto;
import com.neurocast.media.service.StreamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SRS HTTP 回调入口（需在 SRS 配置 http_hooks 指向本接口，匿名放行）
 */
@Slf4j
@RestController
@RequestMapping("/api/srs/callback")
@RequiredArgsConstructor
public class SrsCallbackController {

    private final StreamService streamService;

    /**
     * SRS 事件回调：on_publish/on_play 鉴权，on_stop 触发停流流程。
     * SRS 约定：返回 code=0 放行，非 0 拒绝。
     * 需要 API Key 认证且具备 srs:callback scope（SRS 通过 ?apiKey= 查询参数传入）。
     */
    @PostMapping
    @PreAuthorize("@ss.hasPermission('srs:callback')")
    public Result<Void> callback(@RequestBody OnEventDto event) {
        log.info("SRS 回调：action={}, stream={}", event == null ? null : event.getAction(),
                event == null ? null : event.getStream());
        try {
            return streamService.handleEvent(event) ? Result.ok() : Result.failed();
        } catch (Exception e) {
            log.error("SRS 回调处理异常", e);
            return Result.failed();
        }
    }
}
