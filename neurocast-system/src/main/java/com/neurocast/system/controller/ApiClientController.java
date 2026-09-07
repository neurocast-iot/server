package com.neurocast.system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.neurocast.common.core.domain.PageResult;
import com.neurocast.common.core.domain.Result;
import com.neurocast.system.domain.dto.ApiClientDto;
import com.neurocast.system.domain.dto.ApiClientDto.Create;
import com.neurocast.system.domain.dto.ApiClientDto.Update;
import com.neurocast.system.domain.dto.ApiClientSearchQuery;
import com.neurocast.system.domain.vo.ApiClientVo;
import com.neurocast.system.service.ApiClientService;

import lombok.RequiredArgsConstructor;

/**
 * API 客户端管理接口（仅管理员）
 */
@RestController
@RequestMapping("/api/admin/system/api-clients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ApiClientController {

    private final ApiClientService apiClientService;

    /**
     * 分页查询
     */
    @GetMapping
    public Result<PageResult<ApiClientVo>> page(ApiClientSearchQuery query) {
        return Result.ok(apiClientService.page(query));
    }

    /**
     * 创建客户端（返回含 API Key 的完整信息，仅此一次可见）
     */
    @PostMapping
    public Result<ApiClientVo> create(@Validated(Create.class) @RequestBody ApiClientDto clientDto) {
        return Result.ok(apiClientService.create(clientDto));
    }

    /**
     * 更新客户端
     */
    @PutMapping
    public Result<Void> update(@Validated(Update.class) @RequestBody ApiClientDto clientDto) {
        apiClientService.update(clientDto);
        return Result.ok();
    }

    /**
     * 删除客户端
     */
    @DeleteMapping("/{clientId}")
    public Result<Void> delete(@PathVariable Long clientId) {
        apiClientService.delete(clientId);
        return Result.ok();
    }

    /**
     * 重新生成 API Key
     */
    @PutMapping("/{clientId}/key")
    public Result<ApiClientVo> regenerateKey(@PathVariable Long clientId) {
        return Result.ok(apiClientService.regenerateKey(clientId));
    }
}
