package com.neurocast.media.service;

import com.neurocast.media.domain.dto.OnEventDto;
import com.neurocast.media.domain.vo.StreamUrlVo;

/**
 * 直播流服务：推流指令下发、播放凭证签发、SRS 回调鉴权
 */
public interface StreamService {

    /**
     * 开启实时直播：已有观看者时复用现有流，否则下发推流 RPC 并签发播放地址（与设备 RPC 方法 startLiveStream 对齐）
     */
    StreamUrlVo startLiveStream(String deviceUid);

    /**
     * 停止推流（与设备 RPC 方法 stopLiveStream 对齐）；仍有观看者或 SRS 查询失败时跳过，不下发停流 RPC
     *
     * @param deviceUid 设备编号（实时流的流名即 deviceUid）
     */
    void stopLiveStream(String deviceUid);

    /**
     * 处理 SRS 回调：on_publish / on_play 鉴权，on_stop 入队待停
     *
     * @return true 放行，false 拒绝
     */
    boolean handleEvent(OnEventDto event);

    /**
     * 将待停推流任务加入队列
     */
    void enqueueStop(String streamName);
}
