package com.neurocast.device.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.core.domain.Result;
import com.neurocast.device.domain.dto.DeviceCreateDto;
import com.neurocast.device.domain.dto.DeviceSearchQuery;
import com.neurocast.device.domain.dto.DeviceUpdateDto;
import com.neurocast.device.domain.vo.DeviceCreateVo;
import com.neurocast.device.domain.vo.DeviceVo;
import com.neurocast.device.service.DeviceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 设备管理接口（CRUD）
 */
@RestController
@RequestMapping("/api/admin/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * 创建设备
     */
    @PostMapping
    public Result<DeviceCreateVo> create(@Valid @RequestBody DeviceCreateDto createDto) {
        return Result.ok(deviceService.create(createDto));
    }

    /**
     * 批量创建设备（已存在的跳过）
     */
    @PostMapping("/batch")
    public Result<List<DeviceCreateVo>> createBatch(@Valid @RequestBody List<DeviceCreateDto> createDtoList) {
        return Result.ok(deviceService.createBatch(createDtoList));
    }

    /**
     * 分页查询设备
     */
    @GetMapping("/list")
    public Result<PageResult<DeviceVo>> list(DeviceSearchQuery query) {
        return Result.ok(deviceService.page(query));
    }

    /**
     * 查询设备详情
     */
    @GetMapping("/{deviceUid}")
    public Result<DeviceVo> get(@PathVariable String deviceUid) {
        return Result.ok(deviceService.get(deviceUid));
    }

    /**
     * 更新设备名称
     */
    @PutMapping
    public Result<Void> update(@Valid @RequestBody DeviceUpdateDto updateDto) {
        deviceService.update(updateDto);
        return Result.ok();
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/{deviceUid}")
    public Result<Void> delete(@PathVariable String deviceUid) {
        deviceService.delete(deviceUid);
        return Result.ok();
    }
}
