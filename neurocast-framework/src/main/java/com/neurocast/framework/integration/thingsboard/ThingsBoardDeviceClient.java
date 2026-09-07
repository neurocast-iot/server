package com.neurocast.framework.integration.thingsboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.OtaPackageId;
import org.thingsboard.server.common.data.security.DeviceCredentials;
import org.thingsboard.server.common.data.security.DeviceCredentialsType;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * ThingsBoard 设备生命周期与 RPC 客户端（防腐层）。
 * 业务模块通过本类操作 ThingsBoard，不直接依赖 TB RestClient。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThingsBoardDeviceClient {

    private final ThingsBoardClientManager clientManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建设备（指定 accessToken 凭证）
     */
    public TbDeviceInfo saveDevice(String deviceName, String deviceType, String label,
                                   String deviceProfileId, String accessToken,
                                   String firmwareId, String softwareId) {
        Device device = new Device();
        device.setDeviceProfileId(DeviceProfileId.fromString(deviceProfileId));
        device.setName(deviceName);
        device.setType(deviceType);
        device.setLabel(label);
        if (StringUtils.hasText(firmwareId)) {
            device.setFirmwareId(OtaPackageId.fromString(firmwareId));
        }
        if (StringUtils.hasText(softwareId)) {
            device.setSoftwareId(OtaPackageId.fromString(softwareId));
        }

        Device saved;
        if (StringUtils.hasText(accessToken)) {
            DeviceCredentials credentials = new DeviceCredentials();
            credentials.setCredentialsType(DeviceCredentialsType.ACCESS_TOKEN);
            credentials.setCredentialsId(accessToken);
            saved = clientManager.execute(client ->
                    client.saveDeviceWithCredentials(device, credentials).orElseThrow());
        } else {
            saved = clientManager.execute(client -> client.saveDevice(device));
        }
        return toDeviceInfo(saved);
    }

    /**
     * 保存（更新）设备
     */
    public void saveDevice(Device device) {
        clientManager.execute(client -> client.saveDevice(device));
    }

    /**
     * 根据 ID 查询设备
     */
    public Device getDeviceById(String deviceId) {
        Optional<Device> device = clientManager.execute(client ->
                client.getDeviceById(DeviceId.fromString(deviceId)));
        return device.orElse(null);
    }

    /**
     * 删除设备
     */
    public void deleteDevice(String deviceId) {
        clientManager.executeVoid(client -> client.deleteDevice(DeviceId.fromString(deviceId)));
    }

    /**
     * 查询设备凭证
     */
    public DeviceCredentials getDeviceCredentials(String deviceId) {
        return clientManager.execute(client ->
                client.getDeviceCredentialsByDeviceId(DeviceId.fromString(deviceId)).orElseThrow());
    }

    /**
     * 单向下发 RPC（不等待设备响应）
     */
    public void rpcOneWay(String deviceId, RpcBody<?> body) {
        JsonNode jsonNode = objectMapper.valueToTree(body);
        log.info("RPC one-way: deviceId={}, method={}", deviceId, body.getMethod());
        clientManager.executeVoid(client ->
                client.handleOneWayDeviceRPCRequest(DeviceId.fromString(deviceId), jsonNode));
    }

    /**
     * 双向下发 RPC（等待设备响应）
     */
    public JsonNode rpcTwoWay(String deviceId, RpcBody<?> body) {
        JsonNode jsonNode = objectMapper.valueToTree(body);
        log.info("RPC two-way: deviceId={}, method={}", deviceId, body.getMethod());
        return clientManager.execute(client ->
                client.handleTwoWayDeviceRPCRequest(DeviceId.fromString(deviceId), jsonNode));
    }

    private TbDeviceInfo toDeviceInfo(Device device) {
        TbDeviceInfo info = new TbDeviceInfo();
        info.setDeviceId(device.getId().toString());
        info.setDeviceName(device.getName());
        info.setDeviceType(device.getType());
        info.setDeviceProfileId(device.getDeviceProfileId().toString());
        if (device.getFirmwareId() != null) {
            info.setFirmwareId(device.getFirmwareId().toString());
        }
        if (device.getSoftwareId() != null) {
            info.setSoftwareId(device.getSoftwareId().toString());
        }
        info.setVersion(device.getVersion());
        return info;
    }
}
