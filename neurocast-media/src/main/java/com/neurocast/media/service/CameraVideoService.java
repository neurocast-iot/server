package com.neurocast.media.service;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.media.domain.CameraVideo;
import com.neurocast.media.domain.dto.CameraVideoSearchQuery;
import com.neurocast.media.domain.vo.CameraVideoVo;

import java.util.List;

/**
 * 录像记录服务
 */
public interface CameraVideoService {

    /**
     * 新增记录
     */
    void create(CameraVideo cameraVideo);

    /**
     * 批量新增记录
     */
    void createBatch(List<CameraVideo> list);

    /**
     * 更新上传状态
     */
    void updateStatus(String id, Integer status);

    /**
     * 更新 HLS 分片状态
     */
    void updateHlsStatus(String id, Integer hlsStatus);

    /**
     * 按设备与文件名查询
     */
    CameraVideoVo findByDeviceUidAndName(String deviceUid, String name);

    /**
     * 查询详情（上传成功时附带下载链接）
     */
    CameraVideoVo findById(String id);

    /**
     * 分页查询
     */
    PageResult<CameraVideoVo> page(CameraVideoSearchQuery query);

    /**
     * 查询数据时间早于 toTime 的记录（用于清理）
     */
    List<CameraVideoVo> findListByEndTime(Long toTime);

    /**
     * 清理数据时间早于 toTime 的记录
     */
    void clearData(Long toTime);

    /**
     * 查询指定时段内覆盖的录像记录（按 startTime 排序）
     *
     * @param deviceUid 设备 Uid
     * @param startTime 时段起始（秒级时间戳）
     * @param endTime   时段结束（秒级时间戳）
     */
    List<CameraVideo> findByTimeRange(String deviceUid, Long startTime, Long endTime);

    /**
     * 根据 ID 查询实体（不存在返回 null）
     */
    CameraVideo findEntityById(String id);
}
