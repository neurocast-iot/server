package com.neurocast.device.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 设备事件触发配置下发请求。
 * <p>
 * 将分散的定时抓拍、蓝牙标签触发等配置统一为结构化的 triggers 数组，
 * 通过 ThingsBoard SHARED_SCOPE 以 JSON 形式整体下发到设备端。
 * <p>
 * 前端 API 使用驼峰命名，Service 层写入 TB 时自动转为 snake_case 以匹配设备协议。
 */
@Getter
@Setter
public class DeviceTriggerConfigDto {

    /**
     * 触发器列表（整体替换设备端当前配置）
     */
    @NotEmpty(message = "触发器列表不能为空")
    @Valid
    private List<Trigger> triggers;

    /**
     * 单个触发器配置
     */
    @Getter
    @Setter
    public static class Trigger {

        /**
         * 触发器唯一标识（由前端生成，如 "timer_snapshot_1"、"bluetooth_1"）
         */
        @NotBlank(message = "触发器 id 不能为空")
        private String id;

        /**
         * 触发类型：timer（定时拍照）、record（时间段录像）、bluetooth（蓝牙标签拍照）
         */
        @NotBlank(message = "触发器 type 不能为空")
        private String type;

        /**
         * 是否启用
         */
        private Boolean enabled;

        /**
         * 优先级，数字越大越高，默认 0。建议：SOS=100, Motion=50, Bluetooth=30, Timer=10
         */
        private Integer priority;

        /**
         * 是否全天生效，默认 false。true 时忽略 schedule
         */
        private Boolean allDay;

        /**
         * 拍照间隔（秒），仅 type=timer 时必填
         */
        private Integer intervalSec;

        /**
         * 连拍次数，默认 1（单次）。大于 1 时一次触发拍多张
         */
        private Integer burstCount;

        /**
         * 连拍间隔（毫秒），两张照片之间的等待时间
         */
        private Integer burstIntervalMs;

        /**
         * 执行计划（可选），allDay=false 时生效。不填 = 全天生效
         */
        @Valid
        private Schedule schedule;
    }

    /**
     * 执行计划：限定触发器在指定的时间段和星期几生效
     */
    @Getter
    @Setter
    public static class Schedule {

        /**
         * 生效开始时间（HH:mm 格式，24 小时制）
         */
        private String startTime;

        /**
         * 生效结束时间（HH:mm 格式，24 小时制）
         */
        private String endTime;

        /**
         * 生效星期几：0=周日, 1=周一, ..., 6=周六
         */
        private List<Integer> days;
    }
}
