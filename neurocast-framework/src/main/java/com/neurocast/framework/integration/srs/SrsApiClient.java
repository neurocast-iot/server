package com.neurocast.framework.integration.srs;

import com.neurocast.common.exception.ServiceException;
import com.neurocast.common.core.constant.ApiStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

/**
 * SRS HTTP API 客户端（防腐层）。
 * 通过 SRS 的 /api/v1/clients/ 接口查询流的推流/播放连接。
 */
@Slf4j
@Component
public class SrsApiClient {

    private final RestClient srsRestClient;

    public SrsApiClient(@Qualifier("srsRestClient") RestClient srsRestClient) {
        this.srsRestClient = srsRestClient;
    }

    /**
     * 查询指定流的所有客户端连接
     *
     * @param stream 流名称，如设备 Uid 或 playback_{deviceUid}
     * @return 客户端列表，查询失败返回空列表
     */
    public List<SrsClientInfo> findClientsByStream(String stream) {
        try {
            SrsResult result = srsRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/clients/")
                            .queryParam("stream", stream)
                            .build())
                    .retrieve()
                    .body(SrsResult.class);
            if (result == null || result.getClients() == null) {
                return Collections.emptyList();
            }
            return result.getClients();
        } catch (Exception e) {
            log.error("查询 SRS 客户端失败：stream={}", stream, e);
            throw new ServiceException(ApiStatus.BUSINESS_SRS_ERROR, "查询 SRS 客户端失败：" + e.getMessage());
        }
    }

    /**
     * 判断指定流是否还有观众（播放端连接）
     */
    public boolean hasPlayer(String stream) {
        return findClientsByStream(stream).stream()
                .anyMatch(client -> client.getType() != null && client.getType().contains("play"));
    }

    /**
     * 判断指定流是否还在推流
     */
    public boolean hasPublisher(String stream) {
        return findClientsByStream(stream).stream()
                .anyMatch(client -> client.getType() != null && client.getType().contains("publish"));
    }
}
