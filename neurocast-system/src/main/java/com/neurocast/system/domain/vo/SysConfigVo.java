package com.neurocast.system.domain.vo;

import com.neurocast.system.domain.SysConfig;

/**
 * 系统配置项（查询返回）
 */
public record SysConfigVo(
        String key,
        String value,
        String description
) {

    /**
     * 从 DB 记录构建
     */
    public static SysConfigVo from(SysConfig config) {
        return new SysConfigVo(config.getConfigKey(), config.getConfigValue(), config.getDescription());
    }
}
