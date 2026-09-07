package com.neurocast.media.domain.vo;

/**
 * m3u8 获取结果：包含 m3u8 内容（就绪时）或状态信息（未就绪时）。
 * 单次 DB 查询同时获得两者，避免重复查询。
 */
public record PlaybackM3u8ResultVo(
        /** m3u8 文本内容，未就绪时为 null */
        String m3u8Content,
        /** 回放状态（preparing / partial / ready） */
        PlaybackStatusVo status
) {
    public boolean isReady() {
        return m3u8Content != null;
    }
}
