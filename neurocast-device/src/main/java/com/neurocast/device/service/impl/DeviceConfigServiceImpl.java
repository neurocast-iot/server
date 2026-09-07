package com.neurocast.device.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.TbScope;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.device.domain.Device;
import com.neurocast.device.domain.dto.DeviceConfigDto;
import com.neurocast.device.domain.dto.DeviceOsdConfigDto;
import com.neurocast.device.domain.dto.DeviceTriggerConfigDto;
import com.neurocast.device.domain.dto.OsdElementItem;
import com.neurocast.device.domain.dto.OsdElementPixelDto;
import com.neurocast.device.domain.vo.DeviceConfigVo;
import com.neurocast.device.domain.vo.DeviceOsdConfigVo;
import com.neurocast.device.mapper.DeviceMapper;
import com.neurocast.device.service.DeviceConfigService;
import com.neurocast.device.service.DeviceGlobalConfigService;
import com.neurocast.device.service.OsdCoordConverter;
import com.neurocast.framework.integration.thingsboard.ThingsBoardTelemetryClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备配置服务实现。
 * 下发策略：先读取设备当前 SHARED_SCOPE 配置，仅下发有变更的键，减少设备端无谓重启配置。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceConfigServiceImpl implements DeviceConfigService {

    /**
     * 空键列表表示查询全部属性（保持与原项目一致的 TB 查询语义）
     */
    private static final List<String> ALL_KEYS = List.of("");

    private final DeviceMapper deviceMapper;
    private final ThingsBoardTelemetryClient telemetryClient;
    private final DeviceGlobalConfigService deviceGlobalConfigService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initDeviceConfig(String deviceUid) {
        Device device = getDeviceOrThrow(deviceUid);

        Map<String, Object> defaults = new HashMap<>();
        defaults.put("last_updated_time", System.currentTimeMillis()/1000);

        // 全部配置项来自设备全局配置表（含视频编码/抓拍/录像/OSD 等默认值，统一后台维护）
        defaults.putAll(deviceGlobalConfigService.getConfigMap());

        saveSharedAttributes(device.getTbDeviceId(), defaults);
    }

    @Override
    public void setSettings(String deviceUid, DeviceConfigDto configDto) {
        Device device = getDeviceOrThrow(deviceUid);
        DeviceConfigVo currentConfig = getSettings(deviceUid);

        Map<String, Object> params = new HashMap<>();
        params.put("last_updated_time", System.currentTimeMillis() / 1000);

        // ---- 主通道（视频编码） ----
        putDiff(params, "main_codec", configDto.getMainCodec(), currentConfig.getMainCodec());
        putDiff(params, "main_frame_rate", configDto.getMainFrameRate(), currentConfig.getMainFrameRate());
        putDiff(params, "main_resolution_width", configDto.getMainResolutionWidth(), currentConfig.getMainResolutionWidth());
        putDiff(params, "main_resolution_height", configDto.getMainResolutionHeight(), currentConfig.getMainResolutionHeight());
        putDiff(params, "main_bitrate", configDto.getMainBitrate(), currentConfig.getMainBitrate());
        putDiff(params, "main_br_mode", lowerOrNull(configDto.getMainBrMode()), currentConfig.getMainBrMode());

        // ---- 子通道（抓拍） ----
        putDiff(params, "sub_codec", configDto.getSubCodec(), currentConfig.getSubCodec());
        putDiff(params, "sub_frame_rate", configDto.getSubFrameRate(), currentConfig.getSubFrameRate());
        putDiff(params, "sub_resolution_width", configDto.getSubResolutionWidth(), currentConfig.getSubResolutionWidth());
        putDiff(params, "sub_resolution_height", configDto.getSubResolutionHeight(), currentConfig.getSubResolutionHeight());
        putDiff(params, "sub_bitrate", configDto.getSubBitrate(), currentConfig.getSubBitrate());
        putDiff(params, "sub_br_mode", lowerOrNull(configDto.getSubBrMode()), currentConfig.getSubBrMode());

        // ---- 拍照 ----
        putDiff(params, "snapshot_quality", configDto.getSnapshotQuality(), currentConfig.getSnapshotQuality());

        // ---- 录像 ----
        putDiff(params, "record_segment_sec", configDto.getRecordSegmentSec(), currentConfig.getRecordSegmentSec());

        saveSharedAttributes(device.getTbDeviceId(), params);
    }

    @Override
    public DeviceConfigVo getSettings(String deviceUid) {
        Device device = getDeviceOrThrow(deviceUid);
        Map<String, Object> attrs = telemetryClient.getAttributesByScope(
                device.getTbDeviceId(), TbScope.SHARED_SCOPE, ALL_KEYS);

        DeviceConfigVo vo = new DeviceConfigVo();
        // 元数据
        vo.setLastUpdatedTime(getLong(attrs, "last_updated_time"));
        // 主通道参数
        vo.setMainCodec(getString(attrs, "main_codec"));
        vo.setMainFrameRate(getInteger(attrs, "main_frame_rate"));
        vo.setMainResolutionWidth(getInteger(attrs, "main_resolution_width"));
        vo.setMainResolutionHeight(getInteger(attrs, "main_resolution_height"));
        vo.setMainBitrate(getInteger(attrs, "main_bitrate"));
        vo.setMainBrMode(lowerOrNull(getString(attrs, "main_br_mode")));
        // 子通道参数
        vo.setSubCodec(getString(attrs, "sub_codec"));
        vo.setSubFrameRate(getInteger(attrs, "sub_frame_rate"));
        vo.setSubResolutionWidth(getInteger(attrs, "sub_resolution_width"));
        vo.setSubResolutionHeight(getInteger(attrs, "sub_resolution_height"));
        vo.setSubBitrate(getInteger(attrs, "sub_bitrate"));
        vo.setSubBrMode(lowerOrNull(getString(attrs, "sub_br_mode")));

        // 拍照
        vo.setSnapshotQuality(getInteger(attrs, "snapshot_quality"));
        // 录像
        vo.setRecordSegmentSec(getInteger(attrs, "record_segment_sec"));
        return vo;
    }

    @Override
    public DeviceOsdConfigVo getOsdConfig(String deviceUid) {
        Device device = getDeviceOrThrow(deviceUid);
        Map<String, Object> attrs = telemetryClient.getAttributesByScope(device.getTbDeviceId(),
                TbScope.SHARED_SCOPE, List.of("osd_elements", "osd_enable"));

        Boolean enabled = getBoolean(attrs, "osd_enable");

        // 千分比元素列表（不在此处转像素，由调用方按需使用）
        List<OsdElementItem> items = null;
        if (attrs.containsKey("osd_elements")) {
            items = parseOsdElementsFromSnake(attrs.get("osd_elements").toString());
        }

        return new DeviceOsdConfigVo(null, null, enabled, items);
    }

    @Override
    public void setOsdElements(String deviceUid, DeviceOsdConfigDto osdConfigDto) {
        Device device = getDeviceOrThrow(deviceUid);
        DeviceOsdConfigVo current = getOsdConfig(deviceUid);

        Integer baseW = osdConfigDto.osdBaseResolutionWidth();
        Integer baseH = osdConfigDto.osdBaseResolutionHeight();
        if (osdConfigDto.osdElements() != null && (baseW == null || baseH == null)) {
            throw new ServiceException(ApiStatus.PARAMETER_FORMAT_ERROR, "osdBaseResolutionWidth/Height 不能为空");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("last_updated_time", System.currentTimeMillis() / 1000);
        putDiff(params, "osd_enable", osdConfigDto.enabled(), current.enabled());

        if (osdConfigDto.osdElements() != null) {
            // 像素转千分比，构造设备协议格式
            List<OsdElementItem> items = osdConfigDto.osdElements().stream()
                    .map(dto -> convertToItem(dto, baseW, baseH))
                    .toList();
            String newJson = JSON.toJSONString(items, JSONWriter.Feature.MapSortField);
            String currentJson = current.osdElements() != null
                    ? JSON.toJSONString(current.osdElements(), JSONWriter.Feature.MapSortField) : null;
            if (!newJson.equals(currentJson)) {
                params.put("osd_elements", osdElementsToSnakeJson(items).toJSONString());
            }
        } else {
            params.put("osd_elements", "[]");
        }

        saveSharedAttributes(device.getTbDeviceId(), params);
    }

    private void saveSharedAttributes(String tbDeviceId, Map<String, Object> params) {
        JsonNode jsonNode = objectMapper.valueToTree(params);
        log.info("下发 SHARED_SCOPE 配置：tbDeviceId={}, data={}", tbDeviceId, params);
        telemetryClient.saveAttributes(tbDeviceId, TbScope.SHARED_SCOPE, jsonNode);
    }

    /**
     * 仅在新值非空且与当前值不同时写入
     */
    private void putDiff(Map<String, Object> params, String key, Object newValue, Object currentValue) {
        if (newValue != null && !newValue.equals(currentValue)) {
            params.put(key, newValue);
        }
    }

    /** 字符串转小写，null 安全 */
    private String lowerOrNull(String value) {
        return value == null ? null : value.toLowerCase();
    }

    /**
     * 值非空时直接写入
     */
    private void putIfNotNull(Map<String, Object> params, String key, Object value) {
        if (value != null) {
            params.put(key, value);
        }
    }

    private Device getDeviceOrThrow(String deviceUid) {
        Device device = deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceUid, deviceUid));
        if (device == null) {
            throw new ServiceException(ApiStatus.BUSINESS_DEVICE_NOT_EXISTED);
        }
        return device;
    }

    private String getString(Map<String, Object> attrs, String key) {
        Object value = attrs.get(key);
        return value == null ? null : value.toString();
    }

    private Integer getInteger(Map<String, Object> attrs, String key) {
        Object value = attrs.get(key);
        return value == null ? null : Integer.valueOf(value.toString());
    }

    private Long getLong(Map<String, Object> attrs, String key) {
        Object value = attrs.get(key);
        return value == null ? null : Long.valueOf(value.toString());
    }

    private Boolean getBoolean(Map<String, Object> attrs, String key) {
        Object value = attrs.get(key);
        return value == null ? null : Boolean.valueOf(value.toString());
    }

    @Override
    public List<DeviceTriggerConfigDto.Trigger> getTriggers(String deviceUid) {
        Device device = getDeviceOrThrow(deviceUid);
        Map<String, Object> attrs = telemetryClient.getAttributesByScope(
                device.getTbDeviceId(), TbScope.SHARED_SCOPE, List.of("triggers"));

        Object triggersValue = attrs.get("triggers");
        if (triggersValue == null) {
            return List.of();
        }
        // TB 存储的是 snake_case JSON，转为 camelCase DTO 返回前端
        JSONArray snakeArray = JSON.parseArray(triggersValue.toString());
        return snakeArray.stream()
                .map(item -> triggerFromSnake((JSONObject) item))
                .toList();
    }

    @Override
    public void setTriggers(String deviceUid, DeviceTriggerConfigDto dto) {
        Device device = getDeviceOrThrow(deviceUid);

        // 读取当前 triggers 用于差异比对
        List<DeviceTriggerConfigDto.Trigger> currentTriggers = getTriggers(deviceUid);
        String currentJson = JSON.toJSONString(currentTriggers);
        String newJson = JSON.toJSONString(dto.getTriggers());

        if (!newJson.equals(currentJson)) {
            // camelCase DTO 转为 snake_case JSON 写入 TB，匹配设备协议
            JSONArray snakeArray = new JSONArray();
            for (DeviceTriggerConfigDto.Trigger t : dto.getTriggers()) {
                snakeArray.add(triggerToSnake(t));
            }
            Map<String, Object> params = new HashMap<>();
            params.put("last_updated_time", System.currentTimeMillis());
            params.put("triggers", snakeArray.toJSONString());
            saveSharedAttributes(device.getTbDeviceId(), params);
        }
    }

    // ── triggers camelCase ↔ snake_case 转换 ──

    /**
     * 将 camelCase DTO 转为 snake_case JSONObject（写入 TB 用）
     */
    private JSONObject triggerToSnake(DeviceTriggerConfigDto.Trigger t) {
        JSONObject obj = new JSONObject();
        obj.put("id", t.getId());
        obj.put("type", t.getType());
        obj.put("enabled", t.getEnabled());
        obj.put("priority", t.getPriority());
        obj.put("all_day", t.getAllDay());
        obj.put("interval_sec", t.getIntervalSec());
        obj.put("burst_count", t.getBurstCount());
        obj.put("burst_interval_ms", t.getBurstIntervalMs());
        if (t.getSchedule() != null) {
            JSONObject sched = new JSONObject();
            sched.put("start_time", t.getSchedule().getStartTime());
            sched.put("end_time", t.getSchedule().getEndTime());
            sched.put("days", t.getSchedule().getDays());
            obj.put("schedule", sched);
        }
        return obj;
    }

    /**
     * 将 snake_case JSONObject 转为 camelCase DTO（从 TB 读取后返回前端）
     */
    private DeviceTriggerConfigDto.Trigger triggerFromSnake(JSONObject obj) {
        DeviceTriggerConfigDto.Trigger t = new DeviceTriggerConfigDto.Trigger();
        t.setId(obj.getString("id"));
        t.setType(obj.getString("type"));
        t.setEnabled(obj.getBoolean("enabled"));
        t.setPriority(obj.getInteger("priority"));
        t.setAllDay(obj.getBoolean("all_day"));
        t.setIntervalSec(obj.getInteger("interval_sec"));
        t.setBurstCount(obj.getInteger("burst_count"));
        t.setBurstIntervalMs(obj.getInteger("burst_interval_ms"));
        JSONObject sched = obj.getJSONObject("schedule");
        if (sched != null) {
            DeviceTriggerConfigDto.Schedule s = new DeviceTriggerConfigDto.Schedule();
            s.setStartTime(sched.getString("start_time"));
            s.setEndTime(sched.getString("end_time"));
            JSONArray daysArr = sched.getJSONArray("days");
            if (daysArr != null) {
                s.setDays(daysArr.toList(Integer.class));
            }
            t.setSchedule(s);
        }
        return t;
    }

    // ── OSD 像素 ↔ 千分比转换 ──

    /**
     * 像素 DTO 转千分比 Item（保存时调用）
     */
    private OsdElementItem convertToItem(OsdElementPixelDto dto, int baseW, int baseH) {
        int[][] permPoints = null;
        if (dto.points() != null) {
            permPoints = new int[dto.points().length][2];
            for (int i = 0; i < dto.points().length; i++) {
                permPoints[i][0] = OsdCoordConverter.pixelToPermillage(dto.points()[i][0], baseW);
                permPoints[i][1] = OsdCoordConverter.pixelToPermillage(dto.points()[i][1], baseH);
            }
        }
        return new OsdElementItem(
                dto.id(), dto.type(), dto.enabled(),
                dto.x() != null ? OsdCoordConverter.pixelToPermillage(dto.x(), baseW) : null,
                dto.y() != null ? OsdCoordConverter.pixelToPermillage(dto.y(), baseH) : null,
                dto.opacity(),
                dto.size(), dto.format(), dto.showWeek(), dto.text(),
                dto.w() != null ? OsdCoordConverter.pixelToPermillage(dto.w(), baseW) : null,
                dto.h() != null ? OsdCoordConverter.pixelToPermillage(dto.h(), baseH) : null,
                dto.r() != null ? OsdCoordConverter.pixelToPermillage(dto.r(), baseH) : null,
                dto.color(),
                permPoints,
                dto.imageUrl()
        );
    }

    // ── OSD snake_case JSON ↔ OsdElementItem 转换 ──

    /**
     * 解析 TB 中 snake_case JSON 为 OsdElementItem 列表
     */
    private List<OsdElementItem> parseOsdElementsFromSnake(String json) {
        JSONArray array = JSON.parseArray(json);
        return array.stream()
                .map(item -> osdFromSnake((JSONObject) item))
                .toList();
    }

    /**
     * 将 OsdElementItem 列表序列化为 snake_case JSONArray（写入 TB 用）
     */
    private JSONArray osdElementsToSnakeJson(List<OsdElementItem> items) {
        JSONArray array = new JSONArray();
        for (OsdElementItem item : items) {
            array.add(osdToSnake(item));
        }
        return array;
    }

    /**
     * 单个 OsdElementItem 转 snake_case JSONObject
     */
    private JSONObject osdToSnake(OsdElementItem item) {
        JSONObject obj = new JSONObject();
        obj.put("id", item.id());
        obj.put("type", item.type());
        obj.put("enabled", item.enabled());
        obj.put("x", item.x());
        obj.put("y", item.y());
        obj.put("opacity", item.opacity());
        obj.put("size", item.size());
        obj.put("format", item.format());
        obj.put("show_week", item.showWeek());
        obj.put("text", item.text());
        obj.put("w", item.w());
        obj.put("h", item.h());
        obj.put("r", item.r());
        obj.put("color", item.color());
        obj.put("image_url", item.imageUrl());
        if (item.points() != null) {
            JSONArray pts = new JSONArray();
            for (int[] p : item.points()) {
                JSONArray pt = new JSONArray();
                pt.add(p[0]);
                pt.add(p[1]);
                pts.add(pt);
            }
            obj.put("points", pts);
        }
        return obj;
    }

    /**
     * snake_case JSONObject 转 OsdElementItem
     */
    private OsdElementItem osdFromSnake(JSONObject obj) {
        int[][] points = null;
        JSONArray pts = obj.getJSONArray("points");
        if (pts != null) {
            points = new int[pts.size()][2];
            for (int i = 0; i < pts.size(); i++) {
                JSONArray pt = pts.getJSONArray(i);
                points[i][0] = pt.getIntValue(0);
                points[i][1] = pt.getIntValue(1);
            }
        }
        return new OsdElementItem(
                obj.getString("id"),
                obj.getString("type"),
                obj.getBoolean("enabled"),
                obj.getInteger("x"),
                obj.getInteger("y"),
                obj.getInteger("opacity"),
                obj.getString("size"),
                obj.getString("format"),
                obj.getBoolean("show_week"),
                obj.getString("text"),
                obj.getInteger("w"),
                obj.getInteger("h"),
                obj.getInteger("r"),
                obj.getString("color"),
                points,
                obj.getString("image_url")
        );
    }
}
