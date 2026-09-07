package com.neurocast.media.service;

import com.neurocast.media.domain.dto.DataCameraImageDto;
import com.neurocast.media.domain.dto.DataCameraImageRecordDto;
import com.neurocast.media.domain.dto.DataCameraVideoDto;
import com.neurocast.media.domain.dto.DataFileUploadResultDto;

/**
 * 媒体事件处理服务（ThingsBoard 规则引擎推送的事件回调）
 */
public interface MediaEventService {

    /**
     * 抓拍图片记录入库（单条）
     */
    void createCameraImage(DataCameraImageDto dto);

    /**
     * 抓拍图片记录入库（批量，按 tbDeviceId）
     */
    void createCameraImageRecord(DataCameraImageRecordDto dto);

    /**
     * 录像记录入库
     */
    void createCameraVideoRecord(DataCameraVideoDto dto);

    /**
     * 文件上传结果通知（设备回传 ok/fail）
     */
    void handleFileUploadResult(DataFileUploadResultDto dto);
}
