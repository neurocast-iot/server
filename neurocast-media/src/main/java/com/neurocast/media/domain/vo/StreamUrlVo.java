package com.neurocast.media.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 播放地址视图
 */
@Getter
@Setter
public class StreamUrlVo {

    /**
     * HTTP-FLV 播放地址（带一次性 accessToken）
     */
    private String videoUrl;
}
