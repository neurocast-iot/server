package com.neurocast.media.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 文件上传结果
 */
@Getter
@Setter
public class FileUploadVo {

    private String filename;

    private Long fileSize;

    /**
     * 相对存储路径
     */
    private String filePath;

    /**
     * 文件 MD5
     */
    private String fileHash;
}
