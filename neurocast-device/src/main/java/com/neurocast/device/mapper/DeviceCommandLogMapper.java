package com.neurocast.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.device.domain.DeviceCommandLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备指令记录 Mapper
 */
@Mapper
public interface DeviceCommandLogMapper extends BaseMapper<DeviceCommandLog> {
}
