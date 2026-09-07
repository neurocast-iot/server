package com.neurocast.framework.integration.thingsboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.neurocast.framework.config.properties.ThingsBoardProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ThingsBoard 遥测与属性客户端（防腐层）。
 * 提供最新遥测查询、属性查询、共享属性下发（写入 SHARED_SCOPE 后 TB 自动推送给设备）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThingsBoardTelemetryClient {

    private final ThingsBoardClientManager clientManager;
    private final ThingsBoardProperties thingsBoardProperties;

    /**
     * 查询最新遥测数据
     *
     * @param deviceId TB 设备 ID
     * @param keys     遥测键，如 current_fw_version、current_sw_version
     * @return key -> value
     */
    public Map<String, Object> getLatestTimeseries(String deviceId, List<String> keys) {
        Map<String, Object> result = new HashMap<>();
        List<TsKvEntry> entries = clientManager.execute(client ->
                client.getLatestTimeseries(DeviceId.fromString(deviceId), keys));
        if (entries != null) {
            entries.forEach(entry -> {
                Object value = entry.getValue();
                if (value != null) {
                    result.put(entry.getKey(), value);
                }
            });
        }
        return result;
    }

    /**
     * 批量查询多台设备的最新遥测（一次 HTTP 请求，避免逐台 N+1）
     *
     * @param deviceIds TB 设备 ID 列表
     * @param keys      遥测键
     * @return tbDeviceId -> (key -> value)
     */
    public Map<String, Map<String, Object>> getLatestTimeseriesBatch(List<String> deviceIds, List<String> keys) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        if (deviceIds == null || deviceIds.isEmpty() || keys == null || keys.isEmpty()) {
            return result;
        }
        String baseUrl = thingsBoardProperties.getUrl();
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String url = baseUrl + "/api/plugins/telemetry/DEVICE/" + String.join(",", deviceIds)
                + "/latest/timeseries?keys=" + String.join(",", keys);
        ResponseEntity<JsonNode> response = clientManager.execute(client ->
                client.getRestTemplate().getForEntity(url, JsonNode.class));
        JsonNode root = response.getBody();
        if (root == null || !root.isObject()) {
            return result;
        }
        // 响应结构：{ "deviceId": [{"key":"k","ts":0,"value":"v"}, ...], ... }
        root.fields().forEachRemaining(deviceEntry -> {
            Map<String, Object> telemetry = new HashMap<>();
            if (deviceEntry.getValue().isArray()) {
                for (JsonNode item : deviceEntry.getValue()) {
                    JsonNode value = item.get("value");
                    if (item.hasNonNull("key") && value != null) {
                        telemetry.put(item.get("key").asText(), value.asText());
                    }
                }
            }
            result.put(deviceEntry.getKey(), telemetry);
        });
        return result;
    }

    /**
     * 查询指定 scope 下的属性
     *
     * @param deviceId TB 设备 ID
     * @param scope    SERVER_SCOPE / SHARED_SCOPE
     * @param keys     属性键（空列表表示查询全部）
     * @return key -> value
     */
    public Map<String, Object> getAttributesByScope(String deviceId, String scope, List<String> keys) {
        Map<String, Object> result = new HashMap<>();
        List<AttributeKvEntry> entries = clientManager.execute(client ->
                client.getAttributesByScope(DeviceId.fromString(deviceId), scope, keys));
        if (entries != null) {
            entries.forEach(entry -> {
                Object value = entry.getValue();
                if (value != null) {
                    result.put(entry.getKey(), value);
                }
            });
        }
        return result;
    }

    /**
     * 保存实体属性。scope 为 SHARED_SCOPE 时 TB 会自动推送给在线设备（配置下发入口）。
     */
    public void saveAttributes(String deviceId, String scope, JsonNode attributes) {
        log.info("saveAttributes: deviceId={}, scope={}", deviceId, scope);
        clientManager.executeVoid(client ->
                client.saveEntityAttributesV2(DeviceId.fromString(deviceId), scope, attributes));
    }

    /**
     * 删除实体指定 scope 下的属性键（TB 会通知在线设备属性已删除）
     */
    public void deleteAttributes(String deviceId, String scope, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        String baseUrl = thingsBoardProperties.getUrl();
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String url = baseUrl + "/api/plugins/telemetry/DEVICE/" + deviceId
                + "/" + scope + "?keys=" + String.join(",", keys);
        log.info("deleteAttributes: deviceId={}, scope={}, keys={}", deviceId, scope, keys);
        clientManager.executeVoid(client ->
                client.getRestTemplate().delete(url));
    }
}
