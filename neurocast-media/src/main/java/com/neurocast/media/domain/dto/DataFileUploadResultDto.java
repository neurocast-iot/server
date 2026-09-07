package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件上传结果通知（设备上传完成后回传）
 */
@Getter
@Setter
public class DataFileUploadResultDto {

    @NotBlank(message = "tbDeviceId 不能为空")
    private String tbDeviceId;

    /**
     * 文件 ID（prepare 阶段生成的记录 ID）
     */
    private String fileId;

    /**
     * 文件类型：image / video
     */
    private String fileType;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 上传是否成功
     */
    private Boolean ok;
}
