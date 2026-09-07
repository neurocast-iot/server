package com.neurocast.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.media.domain.CameraImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抓拍图片记录 Mapper
 */
@Mapper
public interface CameraImageMapper extends BaseMapper<CameraImage> {
}
