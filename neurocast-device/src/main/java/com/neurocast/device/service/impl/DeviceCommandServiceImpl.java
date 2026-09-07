package com.neurocast.device.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.common.utils.BeanConvertor;
import com.neurocast.device.domain.Device;
import com.neurocast.device.domain.DeviceCommandLog;
import com.neurocast.device.domain.dto.DeviceCommandLogQuery;
import com.neurocast.device.domain.dto.DeviceStartFrpDto;
import com.neurocast.device.domain.dto.DeviceStartSshTunnelDto;
import com.neurocast.device.domain.vo.DeviceCommandLogVo;
import com.neurocast.device.integration.PortServiceClient;
import com.neurocast.device.mapper.DeviceCommandLogMapper;
import com.neurocast.device.protocol.DeviceRpcMethod;
import com.neurocast.device.service.DeviceCommandService;
import com.neurocast.device.service.DeviceService;
import com.neurocast.framework.config.properties.FrpProperties;
import com.neurocast.framework.config.properties.SshTunnelProperties;
import com.neurocast.framework.integration.thingsboard.RpcBody;
import com.neurocast.framework.integration.thingsboard.ThingsBoardDeviceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备远程指令服务实现：校验设备在线后经 ThingsBoard 下发 RPC，
 * 单向指令落 SENT 记录，双向指令等待设备应答落 SUCCESS/FAILED 记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceCommandServiceImpl implements DeviceCommandService {

    private final DeviceService deviceService;
    private final ThingsBoardDeviceClient thingsBoardDeviceClient;
    private final FrpProperties frpProperties;
    private final SshTunnelProperties sshTunnelProperties;
    private final PortServiceClient portServiceClient;
    private final DeviceCommandLogMapper deviceCommandLogMapper;

    @Override
    public Map<String, Object> startFrp(DeviceStartFrpDto dto) {
        Device device = getOnlineDevice(dto.getDeviceUid());

        // 申请空闲远程端口
        List<Integer> unusedPorts = portServiceClient.findUnusedPorts();
        Integer remotePort = unusedPorts.get(0);

        // 构造 frpc 代理配置：设备 SSH -> frps remotePort
        Map<String, Object> proxy = new HashMap<>();
        proxy.put("name", device.getDeviceUid() + "-ssh");
        proxy.put("type", "tcp");
        proxy.put("localIP", dto.getSshLocalIp());
        proxy.put("localPort", dto.getSshLocalPort());
        proxy.put("remotePort", remotePort);
        List<Map<String, Object>> proxies = new ArrayList<>();
        proxies.add(proxy);

        Map<String, Object> params = new HashMap<>();
        params.put("method", frpProperties.getMethod());
        params.put("token", frpProperties.getToken());
        params.put("serverAddr", frpProperties.getServerAddr());
        params.put("serverPort", frpProperties.getServerPort());
        params.put("proxies", proxies);

        rpcOneWay(device, DeviceRpcMethod.START_FRP, params);
        log.info("FRP 穿透已下发：deviceUid={}, remotePort={}", device.getDeviceUid(), remotePort);

        Map<String, Object> result = new HashMap<>();
        result.put("deviceUid", device.getDeviceUid());
        result.put("serverAddr", frpProperties.getServerAddr());
        result.put("remotePort", remotePort);
        return result;
    }

    @Override
    public void stopFrp(String deviceUid) {
        Device device = getOnlineDevice(deviceUid);
        rpcOneWay(device, DeviceRpcMethod.STOP_FRP, new HashMap<>());
        log.info("FRP 停止指令已下发：deviceUid={}", deviceUid);
    }

    @Override
    public Map<String, Object> startSshTunnel(DeviceStartSshTunnelDto dto) {
        Device device = getOnlineDevice(dto.getDeviceUid());

        // 申请空闲远程端口
        List<Integer> unusedPorts = portServiceClient.findUnusedPorts();
        Integer remotePort = unusedPorts.get(0);

        // 构造 SSH 隧道配置
        Map<String, Object> params = new HashMap<>();
        params.put("serverAddr", sshTunnelProperties.getServerAddr());
        params.put("serverPort", sshTunnelProperties.getServerPort());
        params.put("username", sshTunnelProperties.getUsername());
        if (sshTunnelProperties.getPrivateKey() != null) {
            params.put("privateKey", sshTunnelProperties.getPrivateKey());
        }
        if (sshTunnelProperties.getPassword() != null) {
            params.put("password", sshTunnelProperties.getPassword());
        }
        params.put("localIp", dto.getLocalIp());
        params.put("localPort", dto.getLocalPort());
        params.put("remotePort", remotePort);

        rpcOneWay(device, DeviceRpcMethod.START_SSH_TUNNEL, params);
        log.info("SSH 隧道已启动：deviceUid={}, remotePort={}", device.getDeviceUid(), remotePort);

        Map<String, Object> result = new HashMap<>();
        result.put("deviceUid", device.getDeviceUid());
        result.put("serverAddr", sshTunnelProperties.getServerAddr());
        result.put("remotePort", remotePort);
        return result;
    }

    @Override
    public void stopSshTunnel(String deviceUid) {
        Device device = getOnlineDevice(deviceUid);
        rpcOneWay(device, DeviceRpcMethod.STOP_SSH_TUNNEL, new HashMap<>());
        log.info("SSH 隧道停止指令已下发：deviceUid={}", deviceUid);
    }

    @Override
    public void reset(String deviceUid) {
        Device device = getOnlineDevice(deviceUid);
        rpcTwoWay(device, DeviceRpcMethod.RESET, new HashMap<>());
        log.info("复位指令已执行（清配置 + 自动重启）：deviceUid={}", deviceUid);
    }

    @Override
    public void restart(String deviceUid) {
        Device device = getOnlineDevice(deviceUid);
        rpcTwoWay(device, DeviceRpcMethod.RESTART, new HashMap<>());
        log.info("重启指令已执行：deviceUid={}", deviceUid);
    }

    @Override
    public PageResult<DeviceCommandLogVo> pageLog(DeviceCommandLogQuery query) {
        LambdaQueryWrapper<DeviceCommandLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getDeviceUid())) {
            wrapper.eq(DeviceCommandLog::getDeviceUid, query.getDeviceUid());
        }
        wrapper.orderByDesc(DeviceCommandLog::getCreateTime);
        Page<DeviceCommandLog> page = deviceCommandLogMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(), BeanConvertor.toList(page.getRecords(), DeviceCommandLogVo.class));
    }

    /**
     * 单向下发：下发成功后落 SENT 记录
     */
    private void rpcOneWay(Device device, DeviceRpcMethod method, Map<String, Object> params) {
        try {
            thingsBoardDeviceClient.rpcOneWay(device.getTbDeviceId(), RpcBody.of(method.getMethod(), params));
            saveLog(device.getDeviceUid(), method, params, DeviceCommandLog.STATUS_SENT, null, null);
        } catch (Exception e) {
            saveLog(device.getDeviceUid(), method, params, DeviceCommandLog.STATUS_FAILED, e.getMessage(), null);
            throw e;
        }
    }

    /**
     * 双向下发：等待设备应答，成功落 SUCCESS（含应答内容与耗时），失败落 FAILED 并抛业务异常
     */
    private void rpcTwoWay(Device device, DeviceRpcMethod method, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        try {
            JsonNode response = thingsBoardDeviceClient.rpcTwoWay(device.getTbDeviceId(),
                    RpcBody.of(method.getMethod(), params));
            long costMs = System.currentTimeMillis() - start;
            String responseText = response == null ? null : response.toString();
            saveLog(device.getDeviceUid(), method, params, DeviceCommandLog.STATUS_SUCCESS, responseText, costMs);
        } catch (Exception e) {
            long costMs = System.currentTimeMillis() - start;
            saveLog(device.getDeviceUid(), method, params, DeviceCommandLog.STATUS_FAILED, e.getMessage(), costMs);
            throw new ServiceException(ApiStatus.BUSINESS_THINGSBOARD_ERROR);
        }
    }

    /**
     * 落指令记录（记录失败不影响主流程）
     */
    private void saveLog(String deviceUid, DeviceRpcMethod method, Map<String, Object> params,
                         String status, String response, Long costMs) {
        try {
            DeviceCommandLog commandLog = new DeviceCommandLog();
            commandLog.setDeviceUid(deviceUid);
            commandLog.setMethod(method.getMethod());
            commandLog.setDescription(method.getDescription());
            commandLog.setParams(JSON.toJSONString(params));
            commandLog.setStatus(status);
            commandLog.setResponse(response);
            commandLog.setCostMs(costMs);
            deviceCommandLogMapper.insert(commandLog);
        } catch (Exception e) {
            log.error("指令记录落库失败：deviceUid={}, method={}", deviceUid, method.getMethod(), e);
        }
    }

    /**
     * 校验设备存在且在线
     */
    private Device getOnlineDevice(String deviceUid) {
        Device device = deviceService.getDeviceOrThrow(deviceUid);
        if (!Integer.valueOf(1).equals(device.getStatus())) {
            throw new ServiceException(ApiStatus.BUSINESS_DEVICE_NOT_ONLINE);
        }
        return device;
    }
}
