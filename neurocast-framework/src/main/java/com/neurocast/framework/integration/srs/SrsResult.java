package com.neurocast.framework.integration.srs;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * SRS HTTP API /api/v1/clients/ 查询结果
 */
@Getter
@Setter
public class SrsResult {

    /**
     * 0 表示成功
     */
    private Integer code;

    private String server;

    private String service;

    private String pid;

    /**
     * 客户端连接列表
     */
    private List<SrsClientInfo> clients;
}
