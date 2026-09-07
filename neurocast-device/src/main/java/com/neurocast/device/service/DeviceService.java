package com.neurocast.device.service;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.device.domain.Device;
import com.neurocast.device.domain.dto.DeviceCreateDto;
import com.neurocast.device.domain.dto.DeviceSearchQuery;
import com.neurocast.device.domain.dto.DeviceUpdateDto;
import com.neurocast.device.domain.vo.DeviceCreateVo;
import com.neurocast.device.domain.vo.DeviceVo;

import java.util.List;

/**
 * 设备管理服务：设备生命周期（本地记录 + ThingsBoard 联动）
 */
public interface DeviceService {

    /**
     * 创建设备：TB 建设备与凭证 -> 本地落库 -> 下发初始配置
     */
    DeviceCreateVo create(DeviceCreateDto createDto);

    /**
     * 批量创建设备（已存在的跳过）
     */
    List<DeviceCreateVo> createBatch(List<DeviceCreateDto> createDtoList);

    /**
     * 删除设备：先删 TB 再删本地
     */
    void delete(String deviceUid);

    /**
     * 分页查询（附带 TB 遥测中的当前固件/软件版本）
     */
    PageResult<DeviceVo> page(DeviceSearchQuery query);

    /**
     * 查询设备详情
     */
    DeviceVo get(String deviceUid);

    /**
     * 更新设备名称
     */
    void update(DeviceUpdateDto updateDto);

    /**
     * 按 deviceUid 查询实体，不存在抛业务异常
     */
    Device getDeviceOrThrow(String deviceUid);

    /**
     * 按 deviceUid 查询实体，不存在返回 null
     */
    Device findByDeviceUid(String deviceUid);

    /**
     * 按 TB 设备 ID 查询实体
     */
    Device getByTbDeviceId(String tbDeviceId);
}
