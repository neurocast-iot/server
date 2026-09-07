package com.neurocast.device.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.constant.TbTelemetryKey;
import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.common.utils.BeanConvertor;
import com.neurocast.common.utils.PasswordGenerator;
import com.neurocast.device.domain.Device;
import com.neurocast.device.domain.Product;
import com.neurocast.device.domain.dto.DeviceCreateDto;
import com.neurocast.device.domain.dto.DeviceSearchQuery;
import com.neurocast.device.domain.dto.DeviceUpdateDto;
import com.neurocast.device.domain.vo.DeviceCreateVo;
import com.neurocast.device.domain.vo.DeviceVo;
import com.neurocast.device.mapper.DeviceMapper;
import com.neurocast.device.service.DeviceConfigService;
import com.neurocast.device.service.DeviceService;
import com.neurocast.device.service.ProductService;
import com.neurocast.framework.config.properties.ThingsBoardProperties;
import com.neurocast.framework.integration.thingsboard.TbDeviceInfo;
import com.neurocast.framework.integration.thingsboard.ThingsBoardDeviceClient;
import com.neurocast.framework.integration.thingsboard.ThingsBoardTelemetryClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    /**
     * 设备 accessToken 长度
     */
    private static final int ACCESS_TOKEN_LENGTH = 21;

    private final DeviceMapper deviceMapper;
    private final ThingsBoardDeviceClient thingsBoardDeviceClient;
    private final ThingsBoardTelemetryClient thingsBoardTelemetryClient;
    private final ThingsBoardProperties thingsBoardProperties;
    private final DeviceConfigService deviceConfigService;
    private final ProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceCreateVo create(DeviceCreateDto createDto) {
        String deviceUid = createDto.getDeviceUid();
        Device existed = findByDeviceUid(deviceUid);
        if (existed != null) {
            throw new ServiceException(ApiStatus.BUSINESS_DEVICE_EXISTED);
        }

        String accessToken = PasswordGenerator.generateRandomPassword(ACCESS_TOKEN_LENGTH);
        String name = StringUtils.hasText(createDto.getName()) ? createDto.getName() : deviceUid;

        // 查询产品，获取 TB Profile ID
        Product product = productService.getById(createDto.getProductId());

        // 1. ThingsBoard 创建设备（ACCESS_TOKEN 凭证）
        TbDeviceInfo tbDevice = thingsBoardDeviceClient.saveDevice(deviceUid, deviceUid, name,
                product.getTbProfileId(), accessToken, null, null);

        // 2. 本地落库
        Device device = new Device();
        device.setProductId(product.getId());
        device.setTbDeviceId(tbDevice.getDeviceId());
        device.setDeviceUid(deviceUid);
        device.setName(name);
        device.setStatus(0);
        deviceMapper.insert(device);

        // 3. 下发初始配置，失败则回滚 TB 设备（本地由事务回滚）
        try {
            deviceConfigService.initDeviceConfig(deviceUid);
        } catch (Exception e) {
            log.error("设备初始配置下发失败，回滚 TB 设备：{}", deviceUid, e);
            thingsBoardDeviceClient.deleteDevice(tbDevice.getDeviceId());
            throw new ServiceException(ApiStatus.BUSINESS_THINGSBOARD_ERROR,
                    "设备初始配置下发失败：" + e.getMessage());
        }

        DeviceCreateVo vo = new DeviceCreateVo();
        vo.setDeviceUid(deviceUid);
        vo.setAccessToken(accessToken);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DeviceCreateVo> createBatch(List<DeviceCreateDto> createDtoList) {
        List<DeviceCreateVo> result = new ArrayList<>();
        for (DeviceCreateDto createDto : createDtoList) {
            if (findByDeviceUid(createDto.getDeviceUid()) != null) {
                continue;
            }
            result.add(create(createDto));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String deviceUid) {
        Device device = getDeviceOrThrow(deviceUid);
        // 先删 ThingsBoard 设备，失败则终止（避免 TB 残留孤儿设备）
        if (StringUtils.hasText(device.getTbDeviceId())) {
            thingsBoardDeviceClient.deleteDevice(device.getTbDeviceId());
        }
        deviceMapper.deleteById(device.getId());
        log.info("设备已删除：{}", deviceUid);
    }

    @Override
    public PageResult<DeviceVo> page(DeviceSearchQuery query) {
        Page<DeviceVo> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<DeviceVo> result = deviceMapper.selectPageWithProduct(page, query);

        List<DeviceVo> voList = result.getRecords();
        fillVersionsBatch(voList);
        return PageResult.of((int) result.getTotal(), voList);
    }

    @Override
    public DeviceVo get(String deviceUid) {
        DeviceVo vo = toDeviceVo(getDeviceOrThrow(deviceUid));
        fillVersions(vo);
        return vo;
    }

    @Override
    public void update(DeviceUpdateDto updateDto) {
        Device device = getDeviceOrThrow(updateDto.getDeviceUid());
        if (StringUtils.hasText(updateDto.getName())) {
            device.setName(updateDto.getName());
        }
        deviceMapper.updateById(device);
    }

    @Override
    public Device getDeviceOrThrow(String deviceUid) {
        Device device = findByDeviceUid(deviceUid);
        if (device == null) {
            throw new ServiceException(ApiStatus.BUSINESS_DEVICE_NOT_EXISTED);
        }
        return device;
    }

    @Override
    public Device getByTbDeviceId(String tbDeviceId) {
        return deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getTbDeviceId, tbDeviceId));
    }

    @Override
    public Device findByDeviceUid(String deviceUid) {
        return deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceUid, deviceUid));
    }

    private DeviceVo toDeviceVo(Device device) {
        DeviceVo vo = BeanConvertor.toBean(device, DeviceVo.class);
        // 填充产品信息
        if (StringUtils.hasText(device.getProductId())) {
            Product product = productService.getById(device.getProductId());
            if (product != null) {
                DeviceVo.ProductInfo productInfo = BeanConvertor.toBean(product, DeviceVo.ProductInfo.class);
                vo.setProduct(productInfo);
            }
        }
        return vo;
    }

    /**
     * 分页列表批量填充版本遥测（一次 HTTP 请求）
     */
    private void fillVersionsBatch(List<DeviceVo> voList) {
        if (voList.isEmpty()) {
            return;
        }
        List<String> tbDeviceIds = voList.stream()
                .map(DeviceVo::getTbDeviceId)
                .filter(Objects::nonNull)
                .toList();
        if (tbDeviceIds.isEmpty()) {
            return;
        }
        try {
            Map<String, Map<String, Object>> batch = thingsBoardTelemetryClient.getLatestTimeseriesBatch(
                    tbDeviceIds,
                    List.of(TbTelemetryKey.CURRENT_FW_VERSION.getKey(), TbTelemetryKey.CURRENT_SW_VERSION.getKey()));
            for (DeviceVo vo : voList) {
                Map<String, Object> telemetry = batch.get(vo.getTbDeviceId());
                if (telemetry == null) {
                    continue;
                }
                Object fw = telemetry.get(TbTelemetryKey.CURRENT_FW_VERSION.getKey());
                if (fw != null) {
                    vo.setCurrentFwVersion(fw.toString());
                }
                Object sw = telemetry.get(TbTelemetryKey.CURRENT_SW_VERSION.getKey());
                if (sw != null) {
                    vo.setCurrentSwVersion(sw.toString());
                }
            }
        } catch (Exception e) {
            log.warn("批量获取设备版本遥测失败：error={}", e.getMessage());
        }
    }

    /**
     * 从 TB 遥测补充当前固件/软件版本，无数据时忽略
     */
    private void fillVersions(DeviceVo vo) {
        try {
            Map<String, Object> telemetry = thingsBoardTelemetryClient.getLatestTimeseries(
                    vo.getTbDeviceId(),
                    List.of(TbTelemetryKey.CURRENT_FW_VERSION.getKey(), TbTelemetryKey.CURRENT_SW_VERSION.getKey()));
            Object fw = telemetry.get(TbTelemetryKey.CURRENT_FW_VERSION.getKey());
            if (fw != null) {
                vo.setCurrentFwVersion(fw.toString());
            }
            Object sw = telemetry.get(TbTelemetryKey.CURRENT_SW_VERSION.getKey());
            if (sw != null) {
                vo.setCurrentSwVersion(sw.toString());
            }
        } catch (Exception e) {
            log.warn("获取设备版本遥测失败：tbDeviceId={}, error={}", vo.getTbDeviceId(), e.getMessage());
        }
    }
}
