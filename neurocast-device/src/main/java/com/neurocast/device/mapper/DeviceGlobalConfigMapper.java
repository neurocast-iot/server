package com.neurocast.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.device.domain.DeviceGlobalConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备全局配置 Mapper
 */
@Mapper
public interface DeviceGlobalConfigMapper extends BaseMapper<DeviceGlobalConfig> {
}
