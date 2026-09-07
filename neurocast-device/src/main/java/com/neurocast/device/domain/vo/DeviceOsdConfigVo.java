package com.neurocast.device.domain.vo;

import com.neurocast.device.domain.dto.OsdElementItem;

import java.util.List;

/**
 * OSD 隐私水印配置查询响应。
 * 元素列表为千分比格式（直接从 TB 读取），前端根据 osdBaseResolutionWidth/Height 自行换算像素。
 *
 * @param osdBaseResolutionWidth  基准分辨率宽度（像素），前端传入的原始值回传
 * @param osdBaseResolutionHeight 基准分辨率高度（像素），前端传入的原始值回传
 * @param enabled                 OSD 总开关
 * @param osdElements             OSD 元素列表（千分比坐标）
 */
public record DeviceOsdConfigVo(
        Integer osdBaseResolutionWidth,
        Integer osdBaseResolutionHeight,
        Boolean enabled,
        List<OsdElementItem> osdElements
) {
}
