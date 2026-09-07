package com.neurocast.device.domain.dto;

import com.neurocast.common.core.domain.PageQuery;
import lombok.Getter;
import lombok.Setter;

/**
 * 设备指令记录分页查询条件
 */
@Getter
@Setter
public class DeviceCommandLogQuery extends PageQuery {

    /**
     * 设备 Uid（为空查全部）
     */
    private String deviceUid;
}
