package com.neurocast.framework.integration.srs;

import lombok.Getter;
import lombok.Setter;

/**
 * SRS 客户端连接信息（/api/v1/clients/ 返回）
 */
@Getter
@Setter
public class SrsClientInfo {

    /**
     * 客户端 ID
     */
    private String id;

    /**
     * vhost，如 __defaultVhost__
     */
    private String vhost;

    /**
     * 流名称
     */
    private String stream;

    /**
     * 类型：fmtp-publish（推流）/ fmtp-play（播放）
     */
    private String type;

    /**
     * 播放/推流 URL
     */
    private String url;

    /**
     * 已连接时长（秒）
     */
    private Long alive;
}
