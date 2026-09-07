package com.neurocast.framework.integration.thingsboard;

import com.neurocast.framework.config.properties.ThingsBoardProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.OtaPackageId;
import org.thingsboard.server.common.data.OtaPackageInfo;
import org.thingsboard.server.common.data.ota.OtaPackageType;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;

/**
 * ThingsBoard OTA 包客户端（防腐层）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThingsBoardOtaClient {

    private final ThingsBoardClientManager clientManager;
    private final ThingsBoardProperties thingsBoardProperties;

    /**
     * 分页查询 OTA 包
     */
    public PageData<OtaPackageInfo> getOtaPackages(PageLink pageLink) {
        return clientManager.execute(client -> client.getOtaPackages(pageLink));
    }

    /**
     * 按设备配置 + 类型过滤查询 OTA 包（服务端过滤）。
     * 注：TB RestClient 的 getOtaPackages(DeviceProfileId, OtaPackageType, boolean, PageLink)
     * 拼接了 3 个路径参数，但服务端只有 2 个（/{deviceProfileId}/{type}），属于客户端 bug，
     * 这里直接构造正确的 URL 调用。
     */
    public PageData<OtaPackageInfo> getOtaPackages(DeviceProfileId deviceProfileId,
                                                    OtaPackageType otaPackageType,
                                                    PageLink pageLink) {
        return clientManager.execute(client -> {
            String url = thingsBoardProperties.getUrl()
                    + "/api/otaPackages/" + deviceProfileId.getId()
                    + "/" + otaPackageType.name();
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url)
                    .queryParam("pageSize", pageLink.getPageSize())
                    .queryParam("page", pageLink.getPage());
            if (pageLink.getTextSearch() != null && !pageLink.getTextSearch().isEmpty()) {
                uriBuilder.queryParam("textSearch", pageLink.getTextSearch());
            }
            if (pageLink.getSortOrder() != null && pageLink.getSortOrder().getProperty() != null) {
                uriBuilder.queryParam("sortProperty", pageLink.getSortOrder().getProperty());
                uriBuilder.queryParam("sortOrder", pageLink.getSortOrder().getDirection().name());
            }
            ResponseEntity<PageData<OtaPackageInfo>> response = client.getRestTemplate().exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<PageData<OtaPackageInfo>>() {});
            return response.getBody();
        });
    }

    /**
     * 根据 ID 查询 OTA 包（TB 未提供按 ID 直接查询的接口，分页遍历）
     */
    public OtaPackageInfo getOtaPackageById(String otaPackageId) {
        PageLink pageLink = new PageLink(100);
        PageData<OtaPackageInfo> pageData;
        do {
            pageData = getOtaPackages(pageLink);
            for (OtaPackageInfo info : pageData.getData()) {
                if (info.getId().toString().equals(otaPackageId)) {
                    return info;
                }
            }
            pageLink = pageLink.nextPageLink();
        } while (pageData.hasNext());
        return null;
    }

    /**
     * 保存 OTA 包信息
     */
    public OtaPackageInfo saveOtaPackageInfo(OtaPackageInfo otaPackageInfo) {
        return clientManager.execute(client -> client.saveOtaPackageInfo(otaPackageInfo, true));
    }

    /**
     * 删除 OTA 包
     */
    public void deleteOtaPackage(String otaPackageId) {
        clientManager.executeVoid(client -> client.deleteOtaPackage(OtaPackageId.fromString(otaPackageId)));
    }
}
