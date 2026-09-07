package com.neurocast.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.media.domain.CameraVideo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 录像记录 Mapper
 */
@Mapper
public interface CameraVideoMapper extends BaseMapper<CameraVideo> {
}
