package com.neurocast.device.service;

/**
 * OSD 坐标转换工具。
 * 前端以像素为单位编辑 OSD 元素位置/尺寸，TB 存储层使用千分比（0~1000）匹配设备协议。
 * 转换公式：千分比 = 像素 * 1000 / 基准分辨率；反向：像素 = 千分比 * 基准分辨率 / 1000。
 */
public final class OsdCoordConverter {

    private OsdCoordConverter() {
    }

    /**
     * 像素值转千分比，结果钳位到 0~1000
     *
     * @param pixel         像素值（>= 0）
     * @param baseResolution 基准分辨率（宽或高，必须 > 0）
     * @return 千分比值（0~1000）
     */
    public static int pixelToPermillage(int pixel, int baseResolution) {
        if (baseResolution <= 0) {
            throw new IllegalArgumentException("基准分辨率必须大于 0");
        }
        int permillage = Math.round((float) pixel * 1000 / baseResolution);
        return clampPermillage(permillage);
    }

    /**
     * 千分比转像素值
     *
     * @param permillage    千分比值（0~1000）
     * @param baseResolution 基准分辨率（宽或高，必须 > 0）
     * @return 像素值
     */
    public static int permillageToPixel(int permillage, int baseResolution) {
        if (baseResolution <= 0) {
            throw new IllegalArgumentException("基准分辨率必须大于 0");
        }
        return Math.round((float) clampPermillage(permillage) * baseResolution / 1000);
    }

    /**
     * 将千分比值钳位到 0~1000 范围
     */
    public static int clampPermillage(int value) {
        return Math.max(0, Math.min(1000, value));
    }
}
