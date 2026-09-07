package com.neurocast.device.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neurocast.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 产品（设备类型，关联 ThingsBoard Device Profile）
 */
@Getter
@Setter
@TableName("product")
public class Product extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 产品名称（如 "AV100 高清摄像头"）
     */
    private String name;

    /**
     * 产品型号（如 "av100"，全局唯一）
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
}
