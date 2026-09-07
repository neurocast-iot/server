package com.neurocast.job.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.neurocast.common.core.constant.RedisKeys;
import com.neurocast.device.domain.Device;
import com.neurocast.device.protocol.DeviceRpcMethod;
import com.neurocast.device.service.DeviceService;
import com.neurocast.framework.integration.srs.SrsApiClient;
import com.neurocast.framework.integration.thingsboard.RpcBody;
import com.neurocast.framework.integration.thingsboard.ThingsBoardDeviceClient;
import com.neurocast.media.domain.dto.PushStreamStopDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 停推流巡检任务：消费 on_stop 入队的流，确认无观众后通知设备停止推流并清理凭证
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StopPushStreamTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SrsApiClient srsApiClient;
    private final DeviceService deviceService;
    private final ThingsBoardDeviceClient thingsBoardDeviceClient;

    /**
     * 每分钟巡检一次
     */
    @Scheduled(fixedDelay = 60000)
    public void stopPushStream() {
        Set<Object> streamSet = redisTemplate.opsForSet().members(RedisKeys.Stream.STOP_PUSH_QUEUE);
        if (streamSet == null || streamSet.isEmpty()) {
            return;
        }

        for (Object item : streamSet) {
            PushStreamStopDto dto = JSON.parseObject(item.toString(), PushStreamStopDto.class);
            String streamName = dto.getStreamName();
            try {
                // 仍有观众则保留，等待下次巡检
                if (srsApiClient.hasPlayer(streamName)) {
                    continue;
                }

                // 清理凭证（推流 + 播放）
                String accessToken = (String) redisTemplate.opsForValue()
                        .get(RedisKeys.Stream.REALTIME_PUSH_TOKEN + streamName);
                redisTemplate.delete(RedisKeys.Stream.REALTIME_PUSH_TOKEN + streamName);
                if (accessToken != null) {
                    redisTemplate.delete(RedisKeys.Stream.REALTIME_PLAY_TOKEN + streamName + ":" + accessToken);
                }

                // 设备在线时下发停流 RPC，设备离线或不存在则跳过 RPC（凭证已清理）
                Device device = deviceService.findByDeviceUid(streamName);
                if (device != null && Integer.valueOf(1).equals(device.getStatus())) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("accessToken", accessToken != null ? accessToken : "");
                    thingsBoardDeviceClient.rpcOneWay(device.getTbDeviceId(),
                            RpcBody.of(DeviceRpcMethod.STOP_CAMERA_STREAM.getMethod(), params));
                }

                redisTemplate.opsForSet().remove(RedisKeys.Stream.STOP_PUSH_QUEUE, item);
                log.info("流已停止推送：stream={}", streamName);
            } catch (Exception e) {
                log.error("停推流巡检处理失败：stream={}", streamName, e);
            }
        }
    }
}
