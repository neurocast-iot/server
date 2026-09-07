package com.neurocast.media.service;

import java.util.List;

import com.neurocast.media.domain.dto.PlaybackPrepareDto;
import com.neurocast.media.domain.vo.DailyRecordVo;
import com.neurocast.media.domain.vo.PlaybackFilesResultVo;
import com.neurocast.media.domain.vo.PlaybackFileVo;
import com.neurocast.media.domain.vo.PlaybackM3u8ResultVo;
import com.neurocast.media.domain.vo.PlaybackStatusVo;

/**
 * HLS 回放服务。
 * 查询时段内的录像记录，按前端编排触发单个文件上传，生成虚拟 m3u8。
 */
public interface PlaybackService {

    /**
     * 查询时间段内的录像文件列表（含 HLS 就绪状态和 m3u8Url）
     */
    PlaybackFilesResultVo listFiles(String deviceUid, Long startTime, Long endTime);

    /**
     * 触发单个文件上传（前端编排入口）
     *
     * @return 触发后的文件状态
     */
    PlaybackFileVo prepareFile(PlaybackPrepareDto dto);

    /**
     * 获取回放虚拟 m3u8 内容（动态生成，引用各文件分片）
     *
     * @return m3u8 文本，无就绪分片时返回 null
     */
    String getPlaybackM3u8(String deviceUid, Long startTime, Long endTime);

    /**
     * 获取回放 m3u8 及状态（单次 DB 查询，避免重复查询）
     */
    PlaybackM3u8ResultVo getPlaybackM3u8WithStatus(String deviceUid, Long startTime, Long endTime);

    /**
     * 查询回放状态（不触发上传，仅检查就绪情况）
     */
    PlaybackStatusVo getPlaybackStatus(String deviceUid, Long startTime, Long endTime);

    /**
     * 查询某天可播放的录像记录列表（仅返回 HLS 就绪的文件）
     *
     * @param deviceUid 设备 Uid
     * @param date      日期字符串，格式 yyyy-MM-dd
     */
    List<DailyRecordVo> listDailyRecords(String deviceUid, String date);
}
