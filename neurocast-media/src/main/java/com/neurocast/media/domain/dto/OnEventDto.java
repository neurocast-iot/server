package com.neurocast.media.domain.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * SRS HTTP 回调事件（on_publish / on_play / on_stop 等）
 */
@Getter
@Setter
public class OnEventDto {

    @JSONField(name = "server_id")
    private String serverId;

    /**
     * 事件类型：on_publish / on_play / on_stop
     */
    private String action;

    @JSONField(name = "client_id")
    private String clientId;

    private String ip;

    private String vhost;

    private String app;

    @JSONField(name = "tcUrl")
    private String tcUrl;

    /**
     * 流名称：实时流为 deviceUid，回放流为 playback_{deviceUid}
     */
    private String stream;

    /**
     * 流 URL 携带的参数串，如 accessToken=xxx
     */
    private String param;

    @JSONField(name = "stream_url")
    private String streamUrl;

    @JSONField(name = "stream_id")
    private String streamId;
}
