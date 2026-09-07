package com.neurocast.media.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 媒体文件上传准备结果
 */
@Getter
@Setter
public class FilePrepareResultVo {

    private String id;

    private String name;

    private Long fileSize;

    /**
     * 上传状态，见 FileStatus
     */
    private Integer status;

    private Long eventTime;

    /**
     * 下载链接（仅上传成功时填充）
     */
    private String fileUrl;
}
