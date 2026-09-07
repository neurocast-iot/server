package com.neurocast.common.core.constant;

/**
 * 媒体文件状态
 */
public interface FileStatus {

    /**
     * 初始状态（设备已生成，未上传）
     */
    int INIT = 0;

    /**
     * 准备上传中
     */
    int PREPARING = 1;

    /**
     * 上传成功
     */
    int PREPARE_SUCCESS = 2;

    /**
     * 上传失败
     */
    int PREPARE_FAILED = 3;
}
