package com.neurocast.media.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 设备分片上传初始化响应
 */
@Getter
@Setter
public class DeviceUploadInitVo {

    /**
     * 上传任务 ID（秒传完成时为空）
     */
    private String uploadId;

    /**
     * 是否秒传完成
     */
    private Boolean instantComplete;

    /**
     * 秒传时已存在文件的相对路径
     */
    private String filePath;

    /**
     * 已上传的分片索引（断点续传）
     */
    private List<Integer> uploadedParts;
}
