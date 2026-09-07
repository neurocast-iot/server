package com.neurocast.system.domain;

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
 * 系统配置（服务端自身的 KV 配置，不下发设备）
 */
@Getter
@Setter
@TableName("sys_config")
public class SysConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置键（snake_case 格式）
     */
    @TableId(type = IdType.INPUT)
    private String configKey;

    /**
     * 配置值（统一存字符串）
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
