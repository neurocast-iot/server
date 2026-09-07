package com.neurocast.device.domain.dto;

/**
 * OSD 元素（千分比格式，匹配设备协议，用于 TB 存储层）。
 * 扁平结构：所有元素类型共用一组字段，每种类型只取自己用得到的键，多余字段忽略。
 * TB JSON 使用 snake_case 字段名（showWeek → show_week，imageUrl → image_url），
 * Service 层负责 camelCase ↔ snake_case 转换。
 */
public record OsdElementItem(
        /** 元素唯一标识（前端生成） */
        String id,
        /** 元素类型：time / label / rect / circle / ellipse / polygon / bitmap */
        String type,
        /** 单元素开关 */
        Boolean enabled,
        /** 千分比横坐标 0~1000。文本/矩形/位图=左上角；圆/椭圆=圆心 */
        Integer x,
        /** 千分比纵坐标 0~1000，语义同 x */
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

        /** 宽（千分比，占画面宽度） */
        Integer w,
        /** 高（千分比，占画面高度） */
        Integer h,

        // ── circle 专用 ──

        /** 半径（千分比，占画面高度） */
        Integer r,

        // ── rect / circle / ellipse / polygon / bitmap 共用 ──

        /** 填充色：black / white / red / green / blue / yellow */
        String color,

        // ── polygon 专用 ──

        /** 多边形顶点千分比坐标 [[x,y], ...]，至少 3 个点，最多 16 个 */
        int[][] points,

        // ── bitmap 专用 ──

        /** 图片下载地址（建议 HTTPS） */
        String imageUrl
) {
}
