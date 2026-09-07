package com.neurocast.media.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSON;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.RedisKeys;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.common.utils.IdUtils;
import com.neurocast.device.domain.Device;
import com.neurocast.device.protocol.DeviceRpcMethod;
import com.neurocast.device.service.DeviceService;
import com.neurocast.framework.config.properties.SrsProperties;
import com.neurocast.framework.integration.srs.SrsApiClient;
import com.neurocast.framework.integration.thingsboard.RpcBody;
import com.neurocast.framework.integration.thingsboard.ThingsBoardDeviceClient;
import com.neurocast.media.domain.dto.OnEventDto;
import com.neurocast.media.domain.dto.PushStreamStopDto;
import com.neurocast.media.domain.vo.StreamUrlVo;
import com.neurocast.media.service.StreamService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 直播流服务实现（回放已改为 HLS 方案）。
 * 流程：签发推流凭证 -> RPC 通知设备推流到 SRS -> SRS 回调 on_publish/on_play 校验凭证 ->
 * 观众全部断开后（on_stop 入队）由定时任务下发停流 RPC。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamServiceImpl implements StreamService {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("accessToken=([^&]+)");

    private final DeviceService deviceService;
    private final ThingsBoardDeviceClient thingsBoardDeviceClient;
    private final SrsProperties srsProperties;
    private final SrsApiClient srsApiClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public StreamUrlVo startLiveStream(String deviceUid) {
        Device device = getOnlineDevice(deviceUid);

        // 一次查询 SRS 客户端列表，同时判断是否有观众和推流（减少 API 调用）
        boolean hasPlayer = false;
        boolean hasPublisher = false;
        try {
            var clients = srsApiClient.findClientsByStream(deviceUid);
            hasPlayer = clients.stream().anyMatch(c -> c.getType() != null && c.getType().contains("play"));
            hasPublisher = clients.stream().anyMatch(c -> c.getType() != null && c.getType().contains("publish"));
        } catch (Exception e) {
            log.warn("查询 SRS 流状态失败，降级为新建流：deviceUid={}", deviceUid, e);
        }

        // 有观众：检查是否可以复用现有凭证
        if (hasPlayer) {
            String existingToken = (String) redisTemplate.opsForValue()
                    .get(RedisKeys.Stream.REALTIME_PUSH_TOKEN + deviceUid);
            if (StringUtils.hasText(existingToken)) {
                // 播放凭证续期
                redisTemplate.opsForValue().set(
                        RedisKeys.Stream.REALTIME_PLAY_TOKEN + deviceUid + ":" + existingToken,
                        existingToken, RedisKeys.Stream.PLAY_TOKEN_TIMEOUT_MINUTES, TimeUnit.MINUTES);
                StreamUrlVo vo = new StreamUrlVo();
                vo.setVideoUrl(playUrl(deviceUid, existingToken));

                if (hasPublisher) {
                    // 有推流有观众：正常复用，不下发 RPC
                    log.info("实时流正常，复用现有流：deviceUid={}", deviceUid);
                } else {
                    // 有观众无推流：设备可能断了，复用 token 但重新下发 RPC
                    Map<String, Object> params = new HashMap<>();
                    params.put("accessToken", existingToken);
                    thingsBoardDeviceClient.rpcOneWay(device.getTbDeviceId(), RpcBody.of(DeviceRpcMethod.PUSH_CAMERA_STREAM.getMethod(), params));
                    log.info("实时流有观众但无推流，复用凭证并重新下发推流 RPC：deviceUid={}", deviceUid);
                }
                return vo;
            }
            // 有观众但 Redis 无凭证（异常情况）：降级为新建流
            log.warn("SRS 有观众但 Redis 无推流凭证（状态异常），降级为新建流：deviceUid={}", deviceUid);
        }

        String accessToken = IdUtils.simpleUuid();

        // 推流凭证（设备 on_publish 时校验，停流时删除）
        redisTemplate.opsForValue().set(RedisKeys.Stream.REALTIME_PUSH_TOKEN + deviceUid, accessToken);
        // 播放凭证（观众 on_play 时校验）
        redisTemplate.opsForValue().set(RedisKeys.Stream.REALTIME_PLAY_TOKEN + deviceUid + ":" + accessToken,
                accessToken, RedisKeys.Stream.PLAY_TOKEN_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        Map<String, Object> params = new HashMap<>();
        params.put("accessToken", accessToken);
        thingsBoardDeviceClient.rpcOneWay(device.getTbDeviceId(), RpcBody.of(DeviceRpcMethod.PUSH_CAMERA_STREAM.getMethod(), params));
        log.info("实时推流指令已下发：deviceUid={}", deviceUid);

        StreamUrlVo vo = new StreamUrlVo();
        vo.setVideoUrl(playUrl(deviceUid, accessToken));
        return vo;
    }

    @Override
    public void stopLiveStream(String deviceUid) {
        // 校验设备存在且在线（与开播对齐，设备不可用时直接抛异常）
        Device device = getOnlineDevice(deviceUid);

        // 一次查询 SRS 客户端列表，同时判断是否有推流和观众（减少 API 调用）
        boolean hasPublisher = false;
        boolean hasPlayer = false;
        try {
            var clients = srsApiClient.findClientsByStream(deviceUid);
            hasPublisher = clients.stream().anyMatch(c -> c.getType() != null && c.getType().contains("publish"));
            hasPlayer = clients.stream().anyMatch(c -> c.getType() != null && c.getType().contains("play"));
        } catch (Exception e) {
            log.warn("查询 SRS 流状态失败，继续执行停流：deviceUid={}", deviceUid, e);
        }

        // 有推流且有观众：跳过停流，避免中断其他观众
        if (hasPublisher && hasPlayer) {
            log.info("SRS 有推流且有观众，跳过停流：deviceUid={}", deviceUid);
            return;
        }

        // 无推流或有推流但无观众：清理凭证并下发停流 RPC
        // 即使 SRS 无推流，也下发 RPC（设备端可能仍在推流，SRS 状态可能不同步）
        log.info("SRS 状态：hasPublisher={}，hasPlayer={}，执行停流：deviceUid={}", hasPublisher, hasPlayer, deviceUid);

        // 清理凭证（推流 + 播放）
        String accessToken = (String) redisTemplate.opsForValue().get(RedisKeys.Stream.REALTIME_PUSH_TOKEN + deviceUid);
        redisTemplate.delete(RedisKeys.Stream.REALTIME_PUSH_TOKEN + deviceUid);
        if (accessToken != null) {
            redisTemplate.delete(RedisKeys.Stream.REALTIME_PLAY_TOKEN + deviceUid + ":" + accessToken);
        }

        // 下发停流 RPC
        Map<String, Object> params = new HashMap<>();
        params.put("accessToken", accessToken != null ? accessToken : "");
        thingsBoardDeviceClient.rpcOneWay(device.getTbDeviceId(), RpcBody.of(DeviceRpcMethod.STOP_CAMERA_STREAM.getMethod(), params));
        // 从停流队列移除（防止定时任务重复下发 RPC）
        redisTemplate.opsForSet().remove(RedisKeys.Stream.STOP_PUSH_QUEUE, "{\"streamName\":\"" + deviceUid + "\"}");
        log.info("停流指令已下发：deviceUid={}", deviceUid);
    }

    @Override
    public boolean handleEvent(OnEventDto event) {
        if (event == null || !StringUtils.hasText(event.getAction())) {
            return false;
        }
        switch (event.getAction()) {
            case "on_publish":
                return checkPublish(event);
            case "on_play":
                return checkPlay(event);
            case "on_stop":
                String streamName = event.getStream();
                if (StringUtils.hasText(streamName)) {
                    // 观众断开后检查是否还有其他人观看，无人观看才入队等定时任务停流
                    // 若推流凭证已不存在，说明已手动停流，跳过入队避免重复 RPC
                    String pushToken = (String) redisTemplate.opsForValue()
                            .get(RedisKeys.Stream.REALTIME_PUSH_TOKEN + streamName);
                    if (pushToken == null) {
                        log.debug("推流凭证已清理，跳过入队：stream={}", streamName);
                        return true;
                    }
                    try {
                        if (!srsApiClient.hasPlayer(streamName)) {
                            enqueueStop(streamName);
                        }
                    } catch (Exception e) {
                        log.warn("查询 SRS 观看者失败，跳过入队：stream={}", streamName, e);
                    }
                }
                return true;
            default:
                return true;
        }
    }

    @Override
    public void enqueueStop(String streamName) {
        PushStreamStopDto dto = new PushStreamStopDto();
        dto.setStreamName(streamName);
        redisTemplate.opsForSet().add(RedisKeys.Stream.STOP_PUSH_QUEUE, JSON.toJSONString(dto));
    }

    /**
     * 推流鉴权：校验推流地址中携带的 accessToken 与签发的推流凭证精确匹配（仅判键存在无法防伪造推流）
     */
    private boolean checkPublish(OnEventDto event) {
        String stream = event.getStream();
        String accessToken = extractAccessToken(event.getParam());
        if (stream == null || accessToken == null) {
            return false;
        }
        return accessToken.equals(redisTemplate.opsForValue()
                .get(RedisKeys.Stream.REALTIME_PUSH_TOKEN + stream));
    }

    /**
     * 播放鉴权：校验播放地址中签发的一次性 accessToken
     */
    private boolean checkPlay(OnEventDto event) {
        String stream = event.getStream();
        String accessToken = extractAccessToken(event.getParam());
        if (stream == null || accessToken == null) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(
                RedisKeys.Stream.REALTIME_PLAY_TOKEN + stream + ":" + accessToken));
    }

    private String extractAccessToken(String param) {
        if (!StringUtils.hasText(param)) {
            return null;
        }
        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(param);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String playUrl(String streamName, String accessToken) {
        // WHEP 格式：{apiUrl}/rtc/v1/whep/?app=live&stream={streamName}&accessToken={accessToken}
        String apiUrl = srsProperties.getApiUrl();
        if (apiUrl != null && apiUrl.endsWith("/")) {
            apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
        }
        return apiUrl + "/rtc/v1/whep/?app=live&stream=" + streamName + "&accessToken=" + accessToken;
    }

    private Device getOnlineDevice(String deviceUid) {
        Device device = deviceService.getDeviceOrThrow(deviceUid);
        if (!Integer.valueOf(1).equals(device.getStatus())) {
            throw new ServiceException(ApiStatus.BUSINESS_DEVICE_NOT_ONLINE);
        }
        return device;
    }
}
