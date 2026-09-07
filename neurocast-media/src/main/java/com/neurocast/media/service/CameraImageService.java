package com.neurocast.media.service;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.media.domain.CameraImage;
import com.neurocast.media.domain.dto.CameraImageSearchQuery;
import com.neurocast.media.domain.vo.CameraImageVo;

import java.util.List;

/**
 * 抓拍图片记录服务
 */
public interface CameraImageService {

    /**
     * 新增记录
     */
    void create(CameraImage cameraImage);

    /**
     * 批量新增记录
     */
    void createBatch(List<CameraImage> list);

    /**
     * 更新上传状态
     */
    void updateStatus(String id, Integer status);

    /**
     * 按设备与文件名查询
     */
    CameraImageVo findByDeviceUidAndName(String deviceUid, String name);

    /**
     * 查询详情（上传成功时附带下载链接）
     */
    CameraImageVo findById(String id);

    /**
     * 分页查询
     */
    PageResult<CameraImageVo> page(CameraImageSearchQuery query);

    /**
     * 查询数据时间早于 toTime 的记录（用于清理）
     */
    List<CameraImageVo> findListByEndTime(Long toTime);

    /**
     * 清理数据时间早于 toTime 的记录
     */
    void clearData(Long toTime);
}
