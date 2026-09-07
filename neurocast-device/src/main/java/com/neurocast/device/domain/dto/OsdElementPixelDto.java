package com.neurocast.device.domain.dto;

/**
 * OSD 元素（像素格式，用于前端 API 交互）。
 * 前端以像素为单位编辑 OSD 元素，后端 Service 层负责像素 ↔ 千分比转换。
 * 结构与 OsdElementItem 一致，仅坐标/尺寸字段语义为像素值。
 */
public record OsdElementPixelDto(
        /** 元素唯一标识（前端生成） */
        String id,
        /** 元素类型：time / label / rect / circle / ellipse / polygon / bitmap */
        String type,
        /** 单元素开关 */
        Boolean enabled,
        /** 横坐标（像素）。文本/矩形/位图=左上角；圆/椭圆=圆心 */
        Integer x,
        /** 纵坐标（像素），语义同 x */
        Integer y,
        /** 透明度 0~100，0=完全不透明，值越大越透明 */
        Integer opacity,

        // ── time / label 专用 ──

        /** 字号档位：small / medium / large */
        String size,
        /** 日期格式：YYYY-MM-DD / MM-DD-YYYY / Chinese */
        String format,
        /** 日期后是否追加星期 */
        Boolean showWeek,
        /** label 文本内容 */
        String text,

        // ── rect / ellipse / bitmap 专用 ──

        /** 宽（像素） */
        Integer w,
        /** 高（像素） */
        Integer h,

        // ── circle 专用 ──

        /** 半径（像素） */
        Integer r,

        // ── rect / circle / ellipse / polygon / bitmap 共用 ──

        /** 填充色：black / white / red / green / blue / yellow */
        String color,

        // ── polygon 专用 ──

        /** 多边形顶点像素坐标 [[x,y], ...]，至少 3 个点，最多 16 个 */
        int[][] points,

        // ── bitmap 专用 ──

        /** 图片下载地址（建议 HTTPS） */
        String imageUrl
) {
}
