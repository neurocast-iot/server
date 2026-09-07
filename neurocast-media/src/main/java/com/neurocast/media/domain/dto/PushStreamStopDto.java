package com.neurocast.media.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 待停推流任务（写入 Redis 队列，由定时任务消费）
 */
@Getter
@Setter
public class PushStreamStopDto {

    /**
     * 流名称：实时流的流名为 deviceUid
     */
    private String streamName;
}
