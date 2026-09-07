package com.neurocast.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neurocast.device.domain.Device;
import com.neurocast.device.domain.dto.DeviceSearchQuery;
import com.neurocast.device.domain.vo.DeviceVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 设备 Mapper
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * 分页查询设备（含产品信息）
     */
    Page<DeviceVo> selectPageWithProduct(Page<?> page, @Param("query") DeviceSearchQuery query);
}
