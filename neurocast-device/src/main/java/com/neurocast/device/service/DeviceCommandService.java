package com.neurocast.device.service;

import java.util.Map;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.device.domain.dto.DeviceCommandLogQuery;
import com.neurocast.device.domain.dto.DeviceStartFrpDto;
import com.neurocast.device.domain.dto.DeviceStartSshTunnelDto;
import com.neurocast.device.domain.vo.DeviceCommandLogVo;

/**
 * 设备远程指令服务（经 ThingsBoard RPC 下发到设备）
 */
public interface DeviceCommandService {

    /**
     * 启动 FRP 内网穿透（SSH 反向代理），返回穿透代理信息
     */
    Map<String, Object> startFrp(DeviceStartFrpDto dto);

    /**
     * 停止 FRP 内网穿透
     */
    void stopFrp(String deviceUid);

    /**
     * 启动 SSH 隧道，返回隧道代理信息
     */
    Map<String, Object> startSshTunnel(DeviceStartSshTunnelDto dto);

    /**
     * 停止 SSH 隧道
     */
    void stopSshTunnel(String deviceUid);

    /**
     * 复位设备：清除设备配置并自动重启
     */
    void reset(String deviceUid);

    /**
     * 重启设备
     */
    void restart(String deviceUid);

    /**
     * 分页查询指令记录（按下发时间倒序）
     */
    PageResult<DeviceCommandLogVo> pageLog(DeviceCommandLogQuery query);
}
