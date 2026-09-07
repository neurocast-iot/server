package com.neurocast.common.core.constant;

/**
 * API 状态码枚举（按模块分段，前端依赖 code 做精确分支处理）
 * <p>
 * 规则：
 * - 0          成功
 * - 1          通用失败（仅限框架内部兜底，业务代码禁止使用）
 * - 1000-1999  参数校验（统一走 VALIDATE_FAILED，不细分）
 * - 2000-2999  系统/认证
 * - 3000-3999  ThingsBoard 集成
 * - 4000-4999  设备与产品管理
 * - 5000-5999  媒体文件与流
 * - 6000-6999  OTA 升级
 * - 7000-7999  用户管理
 * - 8000-8999  API 客户端管理
 * - 9000-9999  权限管理
 */
public enum ApiStatus implements ConstantsCode {

    /************ 特殊状态码 ************/
    SUCCESS(0, "success"),
    FAILED(1, "failed"),  // 仅限框架内部兜底，业务代码禁止使用

    /************ 参数校验错误码（1000-1999） ************/
    VALIDATE_FAILED(1000, "参数校验失败"),
    PARAMETER_FORMAT_ERROR(1002, "参数格式错误"),

    /************ 系统与认证错误码（2000-2999） ************/
    ERROR(2000, "系统异常"),
    TIMEOUT(2001, "请求超时"),
    HTTP_REQUEST_METHOD_NOT_SUPPORTED(2003, "请求方式不支持"),
    FORBIDDEN(2004, "无访问权限，请联系管理员授权"),
    UNAUTHORIZED(2005, "未认证或认证已过期，请重新登录"),
    NO_PERMISSION_ACCESS(2006, "无权访问"),
    ACCESS_TOKEN_INVALID(2007, "访问令牌无效"),
    REFRESH_TOKEN_INVALID(2008, "刷新令牌失效"),
    REFRESH_TOKEN_INCORRECT(2009, "刷新令牌错误"),
    API_INTERFACE_LIMIT(2010, "接口限流，请稍后再试"),
    DUPLICATE_KEY(2011, "数据已存在"),
    LOGIN_USERNAME_PASSWORD_ERROR(2012, "用户名或密码错误"),
    LOGIN_USER_DISABLED(2013, "用户已被禁用"),
    API_KEY_INVALID(2014, "API Key 无效"),

    /************ ThingsBoard 集成错误码（3000-3999） ************/
    BUSINESS_THINGSBOARD_ERROR(3000, "ThingsBoard 调用失败"),
    BUSINESS_THINGSBOARD_DEVICE_EXISTED(3001, "ThingsBoard 设备已存在"),
    BUSINESS_THINGSBOARD_DEVICE_NOT_EXISTED(3002, "ThingsBoard 设备不存在"),

    /************ 设备与产品管理（4000-4999） ************/
    BUSINESS_DEVICE_NOT_EXISTED(4000, "设备不存在"),
    BUSINESS_DEVICE_EXISTED(4001, "设备已存在"),
    BUSINESS_DEVICE_NOT_ONLINE(4002, "设备不在线"),
    BUSINESS_DEVICE_NO_AVAILABLE_PORT(4003, "没有可用的端口"),
    BUSINESS_PRODUCT_NOT_EXISTED(4004, "产品不存在"),
    BUSINESS_PRODUCT_HAS_DEVICES(4005, "产品下还有设备，无法删除"),
    BUSINESS_PRODUCT_NO_TB_PROFILE(4006, "产品无 ThingsBoard Profile"),
    BUSINESS_DEVICE_GLOBAL_CONFIG_NOT_EXISTED(4008, "设备全局配置项不存在"),
    BUSINESS_SYS_CONFIG_NOT_EXISTED(4009, "系统配置项不存在"),

    /************ 媒体文件错误码（5000-5999） ************/
    BUSINESS_FILE_NOT_EXISTED(5000, "文件不存在"),
    BUSINESS_FILE_UPLOAD_NOT_EMPTY(5001, "文件内容不能为空"),
    BUSINESS_FILE_UPLOAD_TOO_LARGE(5002, "文件大小超出限制"),
    BUSINESS_FILE_UPLOAD_TASK_EXPIRED(5003, "上传任务不存在或已过期"),
    BUSINESS_FILE_CHUNK_INCOMPLETE(5004, "分片未上传完整"),
    BUSINESS_FILE_HASH_MISMATCH(5005, "文件校验失败，MD5 不匹配"),
    BUSINESS_STREAM_NOT_STARTED(5006, "流未开启"),
    BUSINESS_SRS_ERROR(5007, "SRS 流媒体服务调用失败"),

    /************ OTA 升级（6000-6999） ************/
    BUSINESS_OTA_PACKAGE_NOT_EXISTED(6000, "OTA 包不存在"),
    BUSINESS_OTA_VERSION_NOT_GREATER(6001, "目标版本必须大于当前版本"),
    BUSINESS_OTA_PACKAGE_EXISTED(6002, "OTA 包已存在"),
    BUSINESS_OTA_TB_SYNC_FAILED(6003, "ThingsBoard OTA 包同步失败"),

    /************ 用户管理（7000-7999） ************/
    BUSINESS_USER_NOT_EXISTED(7000, "用户不存在"),
    BUSINESS_USER_EXISTED(7001, "用户名已存在"),
    BUSINESS_ROLE_NOT_EXISTED(7002, "角色不存在"),
    BUSINESS_ROLE_CODE_EXISTED(7003, "角色编码已存在"),
    BUSINESS_ROLE_IN_USE(7004, "角色正在被用户使用，无法删除"),

    /************ API 客户端管理（8000-8999） ************/
    BUSINESS_API_CLIENT_NOT_EXISTED(8000, "API 客户端不存在"),

    /************ 权限管理（9000-9999） ************/
    BUSINESS_PERMISSION_NOT_EXISTED(9000, "权限不存在"),
    BUSINESS_PERMISSION_CODE_EXISTED(9001, "权限标识已存在"),
    BUSINESS_PERMISSION_IN_USE(9002, "权限正在被角色使用，无法删除"),
    ;

    private final Integer code;
    private final String msg;

    ApiStatus(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
