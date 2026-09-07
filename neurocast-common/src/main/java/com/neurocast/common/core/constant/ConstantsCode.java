package com.neurocast.common.core.constant;

/**
 * 状态码接口，所有状态码枚举需实现该接口
 */
public interface ConstantsCode {

    /**
     * 获取状态码
     */
    Integer getCode();

    /**
     * 获取提示信息
     */
    String getMsg();
}
