package com.neurocast.device.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * 产品信息
 */
@Getter
@Setter
public class ProductVo {

    private String id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品型号
     */
    private String model;

    /**
     * 关联的 ThingsBoard Device Profile ID
     */
    private String tbProfileId;

    /**
     * 描述信息
     */
    private String description;

    private OffsetDateTime createTime;
}
