package com.neurocast.media.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neurocast.common.core.constant.FileStatus;
import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.utils.BeanConvertor;
import com.neurocast.media.domain.CameraVideo;
import com.neurocast.media.domain.dto.CameraVideoSearchQuery;
import com.neurocast.media.domain.vo.CameraVideoVo;
import com.neurocast.media.mapper.CameraVideoMapper;
import com.neurocast.media.service.CameraVideoService;
import com.neurocast.media.service.MediaUrlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 录像记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CameraVideoServiceImpl implements CameraVideoService {

    private static final String FILE_DIR = "videos";
    private static final String THUMB_DIR = "images";

    private final CameraVideoMapper cameraVideoMapper;
    private final MediaUrlService mediaUrlService;

    @Override
    public void create(CameraVideo cameraVideo) {
        if (cameraVideo.getStatus() == null) {
            cameraVideo.setStatus(FileStatus.INIT);
        }
        cameraVideoMapper.insert(cameraVideo);
    }

    @Override
    public void createBatch(List<CameraVideo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (CameraVideo cameraVideo : list) {
            if (cameraVideo.getStatus() == null) {
                cameraVideo.setStatus(FileStatus.INIT);
            }
            cameraVideoMapper.insert(cameraVideo);
        }
    }

    @Override
    public void updateStatus(String id, Integer status) {
        cameraVideoMapper.update(null, new LambdaUpdateWrapper<CameraVideo>()
                .eq(CameraVideo::getId, id)
                .set(CameraVideo::getStatus, status));
    }

    @Override
    public void updateHlsStatus(String id, Integer hlsStatus) {
        cameraVideoMapper.update(null, new LambdaUpdateWrapper<CameraVideo>()
                .eq(CameraVideo::getId, id)
                .set(CameraVideo::getHlsStatus, hlsStatus));
    }

    @Override
    public CameraVideoVo findByDeviceUidAndName(String deviceUid, String name) {
        CameraVideo cameraVideo = cameraVideoMapper.selectOne(new LambdaQueryWrapper<CameraVideo>()
                .eq(CameraVideo::getDeviceUid, deviceUid)
                .eq(CameraVideo::getName, name));
        return cameraVideo == null ? null : BeanConvertor.toBean(cameraVideo, CameraVideoVo.class);
    }

    @Override
    public CameraVideoVo findById(String id) {
        CameraVideo cameraVideo = cameraVideoMapper.selectById(id);
        if (cameraVideo == null) {
            return null;
        }
        CameraVideoVo vo = BeanConvertor.toBean(cameraVideo, CameraVideoVo.class);
        fillUrls(vo, cameraVideo);
        return vo;
    }

    @Override
    public PageResult<CameraVideoVo> page(CameraVideoSearchQuery query) {
        Page<CameraVideo> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CameraVideo> wrapper = new LambdaQueryWrapper<CameraVideo>()
                .eq(CameraVideo::getDeviceUid, query.getDeviceUid())
                .ge(query.getFromTime() != null, CameraVideo::getEventTime, query.getFromTime())
                .le(query.getToTime() != null, CameraVideo::getEventTime, query.getToTime())
                .orderByDesc(CameraVideo::getEventTime);
        Page<CameraVideo> result = cameraVideoMapper.selectPage(page, wrapper);
        List<CameraVideoVo> voList = BeanConvertor.toList(result.getRecords(), CameraVideoVo.class);
        for (int i = 0; i < result.getRecords().size(); i++) {
            fillUrls(voList.get(i), result.getRecords().get(i));
        }
        return PageResult.of(result.getTotal(), voList);
    }

    @Override
    public List<CameraVideoVo> findListByEndTime(Long toTime) {
        List<CameraVideo> list = cameraVideoMapper.selectList(new LambdaQueryWrapper<CameraVideo>()
                .lt(CameraVideo::getEventTime, toTime));
        return BeanConvertor.toList(list, CameraVideoVo.class);
    }

    @Override
    public void clearData(Long toTime) {
        cameraVideoMapper.delete(new LambdaQueryWrapper<CameraVideo>()
                .lt(CameraVideo::getEventTime, toTime));
    }

    @Override
    public List<CameraVideo> findByTimeRange(String deviceUid, Long startTime, Long endTime) {
        return cameraVideoMapper.selectList(new LambdaQueryWrapper<CameraVideo>()
                .eq(CameraVideo::getDeviceUid, deviceUid)
                .isNotNull(CameraVideo::getStartTime)
                .isNotNull(CameraVideo::getDuration)
                // 文件覆盖范围 [startTime, startTime+duration] 与查询范围 [startTime, endTime] 有交集
                .lt(CameraVideo::getStartTime, endTime)
                .apply("start_time + duration > {0}", startTime)
                .orderByAsc(CameraVideo::getStartTime));
    }

    @Override
    public CameraVideo findEntityById(String id) {
        return cameraVideoMapper.selectById(id);
    }

    private void fillUrls(CameraVideoVo vo, CameraVideo entity) {
        vo.setFileUrl(mediaUrlService.genDownloadUrl(FILE_DIR, entity.getDeviceUid(), entity.getName()));
        if (StringUtils.hasText(entity.getThumbName())) {
            vo.setThumbUrl(mediaUrlService.genDownloadUrl(THUMB_DIR, entity.getDeviceUid(), entity.getThumbName()));
        }
    }
}
