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
import com.neurocast.media.domain.CameraImage;
import com.neurocast.media.domain.dto.CameraImageSearchQuery;
import com.neurocast.media.domain.vo.CameraImageVo;
import com.neurocast.media.mapper.CameraImageMapper;
import com.neurocast.media.service.CameraImageService;
import com.neurocast.media.service.MediaUrlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抓拍图片记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CameraImageServiceImpl implements CameraImageService {

    private static final String FILE_DIR = "images";

    private final CameraImageMapper cameraImageMapper;
    private final MediaUrlService mediaUrlService;

    @Override
    public void create(CameraImage cameraImage) {
        if (cameraImage.getStatus() == null) {
            cameraImage.setStatus(FileStatus.INIT);
        }
        cameraImageMapper.insert(cameraImage);
    }

    @Override
    public void createBatch(List<CameraImage> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (CameraImage cameraImage : list) {
            if (cameraImage.getStatus() == null) {
                cameraImage.setStatus(FileStatus.INIT);
            }
            cameraImageMapper.insert(cameraImage);
        }
    }

    @Override
    public void updateStatus(String id, Integer status) {
        cameraImageMapper.update(null, new LambdaUpdateWrapper<CameraImage>()
                .eq(CameraImage::getId, id)
                .set(CameraImage::getStatus, status));
    }

    @Override
    public CameraImageVo findByDeviceUidAndName(String deviceUid, String name) {
        CameraImage cameraImage = cameraImageMapper.selectOne(new LambdaQueryWrapper<CameraImage>()
                .eq(CameraImage::getDeviceUid, deviceUid)
                .eq(CameraImage::getName, name));
        return cameraImage == null ? null : BeanConvertor.toBean(cameraImage, CameraImageVo.class);
    }

    @Override
    public CameraImageVo findById(String id) {
        CameraImage cameraImage = cameraImageMapper.selectById(id);
        if (cameraImage == null) {
            return null;
        }
        CameraImageVo vo = BeanConvertor.toBean(cameraImage, CameraImageVo.class);
        fillUrls(vo, cameraImage);
        return vo;
    }

    @Override
    public PageResult<CameraImageVo> page(CameraImageSearchQuery query) {
        Page<CameraImage> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CameraImage> wrapper = new LambdaQueryWrapper<CameraImage>()
                .eq(CameraImage::getDeviceUid, query.getDeviceUid())
                .ge(query.getFromTime() != null, CameraImage::getEventTime, query.getFromTime())
                .le(query.getToTime() != null, CameraImage::getEventTime, query.getToTime())
                .orderByDesc(CameraImage::getEventTime);
        Page<CameraImage> result = cameraImageMapper.selectPage(page, wrapper);
        List<CameraImageVo> voList = BeanConvertor.toList(result.getRecords(), CameraImageVo.class);
        for (int i = 0; i < result.getRecords().size(); i++) {
            fillUrls(voList.get(i), result.getRecords().get(i));
        }
        return PageResult.of(result.getTotal(), voList);
    }

    @Override
    public List<CameraImageVo> findListByEndTime(Long toTime) {
        List<CameraImage> list = cameraImageMapper.selectList(new LambdaQueryWrapper<CameraImage>()
                .lt(CameraImage::getEventTime, toTime));
        return BeanConvertor.toList(list, CameraImageVo.class);
    }

    @Override
    public void clearData(Long toTime) {
        cameraImageMapper.delete(new LambdaQueryWrapper<CameraImage>()
                .lt(CameraImage::getEventTime, toTime));
    }

    private void fillUrls(CameraImageVo vo, CameraImage entity) {
        vo.setFileUrl(mediaUrlService.genDownloadUrl(FILE_DIR, entity.getDeviceUid(), entity.getName()));
        if (StringUtils.hasText(entity.getThumbName())) {
            vo.setThumbUrl(mediaUrlService.genDownloadUrl(FILE_DIR, entity.getDeviceUid(), entity.getThumbName()));
        }
    }
}
