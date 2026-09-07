package com.neurocast.media.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 抓拍图片记录回传（批量，按 tbDeviceId）
 */
@Getter
@Setter
public class DataCameraImageRecordDto {

    @NotBlank(message = "tbDeviceId 不能为空")
    private String tbDeviceId;

    private List<DataCameraImage> dataCameraImageList;

    @Getter
    @Setter
    public static class DataCameraImage {

        private String fileName;

        private Long eventTime;

        private String triggerType;

        private String filePath;

        private Long fileSize;

        private String thumbName;

        private String thumbPath;

        private Long thumbSize;
    }
}
