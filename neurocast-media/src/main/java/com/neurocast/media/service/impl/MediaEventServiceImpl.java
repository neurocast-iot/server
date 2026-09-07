package com.neurocast.media.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.neurocast.common.core.constant.FileStatus;
import com.neurocast.device.domain.Device;
import com.neurocast.device.service.DeviceService;
import com.neurocast.media.domain.CameraImage;
import com.neurocast.media.domain.CameraVideo;
import com.neurocast.media.domain.dto.DataCameraImageDto;
import com.neurocast.media.domain.dto.DataCameraImageRecordDto;
import com.neurocast.media.domain.dto.DataCameraVideoDto;
import com.neurocast.media.domain.dto.DataFileUploadResultDto;
import com.neurocast.media.service.CameraImageService;
import com.neurocast.media.service.CameraVideoService;
import com.neurocast.media.service.HlsSegmentService;
import com.neurocast.media.service.MediaEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 媒体事件处理服务实现。
 * 所有方法标记 @Async，接收事件后立即返回，异步完成入库，避免阻塞 ThingsBoard 规则链。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaEventServiceImpl implements MediaEventService {

    private static final String FILE_TYPE_IMAGE = "image";
    private static final String FILE_TYPE_VIDEO = "video";

    /**
     * 数据时间与当前时间允许的最大偏差（秒），超出视为脏数据丢弃
     */
    private static final long DATA_TIME_MAX_DIFF_SECONDS = 60;

    private final DeviceService deviceService;
    private final CameraImageService cameraImageService;
    private final CameraVideoService cameraVideoService;
    private final HlsSegmentService hlsSegmentService;

    @Override
    public void createCameraImage(DataCameraImageDto dto) {
        if (isDataTimeExpired(dto.getEventTime())) {
            log.warn("抓拍记录数据时间过期，丢弃：tbDeviceId={}, eventTime={}", dto.getTbDeviceId(), dto.getEventTime());
            return;
        }
        Device device = deviceService.getByTbDeviceId(dto.getTbDeviceId());
        if (device == null) {
            log.warn("抓拍记录入库忽略，设备不存在：tbDeviceId={}", dto.getTbDeviceId());
            return;
        }
        CameraImage cameraImage = new CameraImage();
        cameraImage.setDeviceUid(device.getDeviceUid());
        cameraImage.setTbDeviceId(dto.getTbDeviceId());
        cameraImage.setName(dto.getFileName());
        cameraImage.setEventTime(dto.getEventTime());
        cameraImage.setFileSize(dto.getFileSize());
        cameraImage.setTriggerType(dto.getTriggerType());
        cameraImage.setFilePath(dto.getFilePath());
        cameraImage.setThumbName(dto.getThumbName());
        cameraImage.setThumbPath(dto.getThumbPath());
        cameraImage.setThumbSize(dto.getThumbSize());
        cameraImageService.create(cameraImage);
    }

    @Override
    public void createCameraImageRecord(DataCameraImageRecordDto dto) {
        Device device = deviceService.getByTbDeviceId(dto.getTbDeviceId());
        if (device == null || dto.getDataCameraImageList() == null) {
            log.warn("抓拍记录批量入库忽略，设备不存在或列表为空：tbDeviceId={}", dto.getTbDeviceId());
            return;
        }
        List<CameraImage> list = new ArrayList<>(dto.getDataCameraImageList().size());
        for (DataCameraImageRecordDto.DataCameraImage item : dto.getDataCameraImageList()) {
            CameraImage cameraImage = new CameraImage();
            cameraImage.setDeviceUid(device.getDeviceUid());
            cameraImage.setTbDeviceId(dto.getTbDeviceId());
            cameraImage.setName(item.getFileName());
            cameraImage.setEventTime(item.getEventTime());
            cameraImage.setFileSize(item.getFileSize());
            cameraImage.setTriggerType(item.getTriggerType());
            cameraImage.setFilePath(item.getFilePath());
            cameraImage.setThumbName(item.getThumbName());
            cameraImage.setThumbPath(item.getThumbPath());
            cameraImage.setThumbSize(item.getThumbSize());
            list.add(cameraImage);
        }
        cameraImageService.createBatch(list);
    }

    @Override
    public void createCameraVideoRecord(DataCameraVideoDto dto) {
        if (isDataTimeExpired(dto.getEventTime())) {
            log.warn("录像记录数据时间过期，丢弃：tbDeviceId={}, eventTime={}", dto.getTbDeviceId(), dto.getEventTime());
            return;
        }
        Device device = deviceService.getByTbDeviceId(dto.getTbDeviceId());
        if (device == null) {
            log.warn("录像记录入库忽略，设备不存在：tbDeviceId={}", dto.getTbDeviceId());
            return;
        }
        CameraVideo cameraVideo = new CameraVideo();
        cameraVideo.setDeviceUid(device.getDeviceUid());
        cameraVideo.setTbDeviceId(dto.getTbDeviceId());
        cameraVideo.setName(dto.getFileName());
        cameraVideo.setEventTime(dto.getEventTime());
        cameraVideo.setStartTime(dto.getStartTime());
        cameraVideo.setDuration(dto.getDuration());
        cameraVideo.setFileSize(dto.getFileSize());
        cameraVideo.setTriggerType(dto.getTriggerType());
        cameraVideo.setFilePath(dto.getFilePath());
        cameraVideo.setThumbName(dto.getThumbName());
        cameraVideo.setThumbPath(dto.getThumbPath());
        cameraVideo.setThumbSize(dto.getThumbSize());
        cameraVideoService.create(cameraVideo);
    }

    @Override
    public void handleFileUploadResult(DataFileUploadResultDto dto) {
        int status = Boolean.TRUE.equals(dto.getOk()) ? FileStatus.PREPARE_SUCCESS : FileStatus.PREPARE_FAILED;
        if (FILE_TYPE_IMAGE.equals(dto.getFileType())) {
            cameraImageService.updateStatus(dto.getFileId(), status);
        } else if (FILE_TYPE_VIDEO.equals(dto.getFileType())) {
            cameraVideoService.updateStatus(dto.getFileId(), status);
            // 视频上传成功后，异步触发 HLS 分片
            if (status == FileStatus.PREPARE_SUCCESS) {
                CameraVideo video = cameraVideoService.findEntityById(dto.getFileId());
                if (video != null) {
                    hlsSegmentService.processFileAsync(video);
                }
            }
        } else {
            log.warn("文件上传结果通知忽略，未知文件类型：fileType={}", dto.getFileType());
        }
    }

    /**
     * 判断事件时间是否与当前时间差异超限
     *
     * @param eventTime 秒级时间戳
     */
    private boolean isDataTimeExpired(Long eventTime) {
        if (eventTime == null) {
            return false;
        }
        long diffSeconds = Math.abs(System.currentTimeMillis() / 1000 - eventTime);
        return diffSeconds > DATA_TIME_MAX_DIFF_SECONDS;
    }
}
