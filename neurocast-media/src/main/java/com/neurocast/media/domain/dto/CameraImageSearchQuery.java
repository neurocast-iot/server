package com.neurocast.media.domain.dto;

import com.neurocast.common.core.domain.PageQuery;

import lombok.Getter;
import lombok.Setter;

/**
 * 抓拍图片分页查询条件
 */
@Getter
@Setter
public class CameraImageSearchQuery extends PageQuery {

    /** 设备 Uid（必填） */
    private String deviceUid;

    /** 起始时间（秒级时间戳，含） */
    private Long fromTime;

    /** 结束时间（秒级时间戳，含） */
    private Long toTime;
}
