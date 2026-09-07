package com.neurocast.device.integration;

import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.common.core.domain.Result;
import com.neurocast.common.exception.ServiceException;
import com.neurocast.framework.config.properties.FrpProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 端口分配服务客户端（为 FRP 穿透申请空闲远程端口）
 */
@Slf4j
@Component
public class PortServiceClient {

    private final FrpProperties frpProperties;
    private final RestClient restClient = RestClient.create();

    public PortServiceClient(FrpProperties frpProperties) {
        this.frpProperties = frpProperties;
    }

    /**
     * 查询可用端口列表
     */
    public List<Integer> findUnusedPorts() {
        try {
            Result<List<Integer>> result = restClient.get()
                    .uri(frpProperties.getPortServiceUrl() + "/unused-ports")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (result == null || !result.isSuccess() || result.getData() == null || result.getData().isEmpty()) {
                throw new ServiceException(ApiStatus.BUSINESS_DEVICE_NO_AVAILABLE_PORT);
            }
            return result.getData();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询可用端口失败", e);
            throw new ServiceException(ApiStatus.BUSINESS_DEVICE_NO_AVAILABLE_PORT);
        }
    }
}
