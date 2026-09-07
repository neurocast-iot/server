package com.neurocast.device.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备全局配置（所有设备通用，KV 存储）。
 * 变更后经 ThingsBoard SHARED_SCOPE 共享属性批量下发到所有设备。
 */
@Getter
@Setter
@TableName("device_global_config")
public class DeviceGlobalConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置键（与设备协议 snake_case 属性名一致，如 mqtt_host）
     */
    @TableId(type = IdType.INPUT)
    private String configKey;

    /**
     * 配置值（统一存字符串，数值型键由服务端校验）
     */
    private String configValue;

    /**
     * 配置描述（展示用）
     */
    private String description;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updateTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}
