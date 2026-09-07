package com.neurocast.device.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建/更新产品请求
 */
@Getter
@Setter
public class ProductCreateDto {

    /**
     * 产品名称（如 "AV100 高清摄像头"）
     */
    @NotBlank(message = "产品名称不能为空")
    private String name;

    /**
     * 产品型号（如 "av100"，全局唯一）
     */
    @NotBlank(message = "产品型号不能为空")
    private String model;

    /**
     * 关联的 ThingsBoard Device Profile ID
     */
    @NotBlank(message = "TB Profile ID 不能为空")
    private String tbProfileId;

    /**
     * 描述信息
     */
    private String description;
}
