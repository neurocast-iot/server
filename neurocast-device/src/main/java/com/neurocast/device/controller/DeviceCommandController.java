package com.neurocast.device.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.core.domain.Result;
import com.neurocast.device.domain.dto.DeviceCommandLogQuery;
import com.neurocast.device.domain.dto.DeviceStartFrpDto;
import com.neurocast.device.domain.dto.DeviceStartSshTunnelDto;
import com.neurocast.device.domain.dto.DeviceUidDto;
import com.neurocast.device.domain.vo.DeviceCommandLogVo;
import com.neurocast.device.service.DeviceCommandService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 设备远程指令接口（隧道、复位、重启、指令审计日志）
 */
@RestController
@RequestMapping("/api/admin/device/command")
@RequiredArgsConstructor
public class DeviceCommandController {

    private final DeviceCommandService deviceCommandService;

    /**
     * 启动 FRP 内网穿透
     */
    @PostMapping("/frp/start")
    public Result<Map<String, Object>> startFrp(@Valid @RequestBody DeviceStartFrpDto dto) {
        return Result.ok(deviceCommandService.startFrp(dto));
    }

    /**
     * 停止 FRP 内网穿透
     */
    @PostMapping("/frp/stop")
    public Result<Void> stopFrp(@Valid @RequestBody DeviceUidDto dto) {
        deviceCommandService.stopFrp(dto.getDeviceUid());
        return Result.ok();
    }

    /**
     * 启动 SSH 隧道
     */
    @PostMapping("/ssh-tunnel/start")
    public Result<Map<String, Object>> startSshTunnel(@Valid @RequestBody DeviceStartSshTunnelDto dto) {
        return Result.ok(deviceCommandService.startSshTunnel(dto));
    }

    /**
     * 停止 SSH 隧道
     */
    @PostMapping("/ssh-tunnel/stop")
    public Result<Void> stopSshTunnel(@Valid @RequestBody DeviceUidDto dto) {
        deviceCommandService.stopSshTunnel(dto.getDeviceUid());
        return Result.ok();
    }

    /**
     * 复位设备：清除设备配置并自动重启
     */
    @PostMapping("/reset")
    public Result<Void> reset(@Valid @RequestBody DeviceUidDto dto) {
        deviceCommandService.reset(dto.getDeviceUid());
        return Result.ok();
    }

    /**
     * 重启设备
     */
    @PostMapping("/restart")
    public Result<Void> restart(@Valid @RequestBody DeviceUidDto dto) {
        deviceCommandService.restart(dto.getDeviceUid());
        return Result.ok();
    }

    /**
     * 分页查询指令记录（可选 deviceUid 过滤）
     */
    @GetMapping("/command-log")
    public Result<PageResult<DeviceCommandLogVo>> pageCommandLog(DeviceCommandLogQuery query) {
        return Result.ok(deviceCommandService.pageLog(query));
    }
}
