package com.neurocast.media.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.Result;
import com.neurocast.media.domain.vo.StreamUrlVo;
import com.neurocast.media.service.StreamService;

import lombok.RequiredArgsConstructor;

/**
 * 直播流接口
 */
@RestController
@RequestMapping("/api/admin/media/stream")
@RequiredArgsConstructor
public class LiveStreamController {

    private final StreamService streamService;

    /**
     * 开启实时直播，返回播放地址（已有观看者时复用现有流）
     */
    @PostMapping("/realtime/start")
    public Result<StreamUrlVo> startLiveStream(@RequestParam String deviceUid) {
        return Result.ok(streamService.startLiveStream(deviceUid));
    }

    /**
     * 停止实时直播
     */
    @PostMapping("/realtime/stop")
    public Result<Void> stopLiveStream(@RequestParam String deviceUid) {
        streamService.stopLiveStream(deviceUid);
        return Result.ok();
    }
}
