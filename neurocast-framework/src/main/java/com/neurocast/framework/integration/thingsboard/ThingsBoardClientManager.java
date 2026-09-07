package com.neurocast.framework.integration.thingsboard;

import com.neurocast.common.exception.ServiceException;
import com.neurocast.common.core.constant.ApiStatus;
import com.neurocast.framework.config.properties.ThingsBoardProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.thingsboard.rest.client.RestClient;

import java.util.function.Function;

/**
 * ThingsBoard RestClient 管理器。
 * 负责登录与令牌过期后的自动重登（原项目只登录一次，token 过期后所有调用失败）。
 */
@Slf4j
public class ThingsBoardClientManager {

    private final ThingsBoardProperties properties;
    private RestClient client;

    public ThingsBoardClientManager(ThingsBoardProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取已登录的客户端（懒加载）
     */
    public synchronized RestClient getClient() {
        if (client == null) {
            login();
        }
        return client;
    }

    /**
     * 执行 ThingsBoard 调用，遇到 401 自动重新登录并重试一次
     */
    public <T> T execute(Function<RestClient, T> action) {
        try {
            return action.apply(getClient());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("ThingsBoard token 已过期，重新登录");
                synchronized (this) {
                    login();
                }
                try {
                    return action.apply(client);
                } catch (HttpClientErrorException retryError) {
                    throw toServiceException(retryError);
                }
            }
            throw toServiceException(e);
        }
    }

    /**
     * 执行无返回值的 ThingsBoard 调用
     */
    public void executeVoid(java.util.function.Consumer<RestClient> action) {
        execute(client -> {
            action.accept(client);
            return null;
        });
    }

    private void login() {
        try {
            this.client = new RestClient(properties.getUrl());
            this.client.login(properties.getUsername(), properties.getPassword());
            log.info("ThingsBoard 登录成功：{}", properties.getUrl());
        } catch (Exception e) {
            log.error("ThingsBoard 登录失败：{}", properties.getUrl(), e);
            throw new ServiceException(ApiStatus.BUSINESS_THINGSBOARD_ERROR,
                    "ThingsBoard 登录失败：" + e.getMessage());
        }
    }

    private ServiceException toServiceException(HttpClientErrorException e) {
        log.error("ThingsBoard 调用失败：status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
        return new ServiceException(ApiStatus.BUSINESS_THINGSBOARD_ERROR,
                "ThingsBoard 调用失败：" + e.getResponseBodyAsString(), e);
    }
}
