package com.neurocast.device.domain.dto;

import java.util.List;

import com.neurocast.common.core.domain.PageQuery;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备分页查询条件
 */
@Getter
@Setter
public class DeviceSearchQuery extends PageQuery {

    /**
     * 产品 ID 精确过滤
     */
    private String productId;

    /**
     * 设备 Uid 模糊查询
     */
    private String deviceUid;

    /**
     * 在线状态：1 在线 0 离线
     */
    private Integer status;

    /**
     * 设备 Uid 精确列表（传入时忽略模糊搜索，按列表精确匹配）
     */
    private List<String> deviceUids;
}
