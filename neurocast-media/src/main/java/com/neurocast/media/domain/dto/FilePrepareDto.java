package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 请求设备上传媒体文件（图片/录像）
 */
@Getter
@Setter
public class FilePrepareDto {

    /**
     * 文件类型：image / video
     */
    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    @NotBlank(message = "设备 Uid 不能为空")
    private String deviceUid;

    @NotBlank(message = "文件名不能为空")
    private String filename;
}
