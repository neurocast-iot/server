package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * OSD 隐私水印配置下发请求（像素格式，前端以像素为单位编辑）。
 *
 * @param osdBaseResolutionWidth  OSD 基准分辨率宽度（像素），支持: 320, 640, 1280, 1920, 2560
 * @param osdBaseResolutionHeight OSD 基准分辨率高度（像素），支持: 176, 360, 720, 1080, 1440
 * @param enabled                 OSD 总开关
 * @param osdElements             OSD 元素列表（像素坐标，最多 4 个；为 null 时清空）
 */
public record DeviceOsdConfigDto(
        @Min(value = 320, message = "OSD 基准分辨率宽度最小 320")
        @Max(value = 2560, message = "OSD 基准分辨率宽度最大 2560")
        Integer osdBaseResolutionWidth,

        @Min(value = 176, message = "OSD 基准分辨率高度最小 176")
        @Max(value = 1440, message = "OSD 基准分辨率高度最大 1440")
        Integer osdBaseResolutionHeight,

        Boolean enabled,

        @Size(max = 4, message = "osdElements 最多只能配置 4 个")
        List<OsdElementPixelDto> osdElements
) {
}
