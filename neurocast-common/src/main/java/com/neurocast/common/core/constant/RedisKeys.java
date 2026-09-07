package com.neurocast.common.core.constant;

/**
 * Redis 缓存 Key 常量
 */
public interface RedisKeys {

    /**
     * 全局 Key 前缀
     */
    String PREFIX = "neurocast:";

    /************ 认证相关 ************/
    interface Auth {
        /**
         * 登出黑名单：token:logout:{jti} -> 过期时间与 JWT 一致
         */
        String LOGOUT_TOKEN = PREFIX + "auth:logout:";

        /**
         * 刷新令牌：token:refresh:{userId} -> refreshToken
         */
        String REFRESH_TOKEN = PREFIX + "auth:refresh:";

        /**
         * 刷新令牌默认有效期（天）
         */
        long REFRESH_TOKEN_TIMEOUT_DAYS = 7;
    }

    /************ 直播流相关 ************/
    interface Stream {
        /**
         * 实时流推流凭证：stream:push:realtime:{deviceUid} -> accessToken
         */
        String REALTIME_PUSH_TOKEN = PREFIX + "stream:push:realtime:";

        /**
         * 实时流播放凭证：stream:play:realtime:{deviceUid}:{accessToken}，有效期 60 分钟
         */
        String REALTIME_PLAY_TOKEN = PREFIX + "stream:play:realtime:";
        long PLAY_TOKEN_TIMEOUT_MINUTES = 60;

        /**
         * 待停推流队列（Set）：元素为 PushStreamStopDto JSON
         */
        String STOP_PUSH_QUEUE = PREFIX + "stream:stop:queue";
    }

    /************ 文件下载相关 ************/
    interface File {
        /**
         * 文件下载临时凭证：file:download:token:{accessToken}，有效期 15 分钟
         */
        String DOWNLOAD_TOKEN = PREFIX + "file:download:token:";
        long DOWNLOAD_TOKEN_TIMEOUT_MINUTES = 15;

        /**
         * 分片上传任务：file:upload:task:{uploadId}
         */
        String UPLOAD_TASK = PREFIX + "file:upload:task:";

        /**
         * 分片上传记录（Set）：file:upload:chunk:{uploadId}
         */
        String UPLOAD_CHUNK = PREFIX + "file:upload:chunk:";

        /**
         * 文件哈希（秒传）：file:hash:{md5} -> 相对路径
         */
        String FILE_HASH = PREFIX + "file:hash:";
    }

    /************ API 客户端缓存 ************/
    interface ApiClient {
        /**
         * API Key 缓存：api:client:{apiKey} -> ApiClient JSON
         */
        String API_KEY = PREFIX + "api:client:";
        long API_KEY_TIMEOUT_MINUTES = 30;

        /**
         * API 客户端 scope 缓存：api:client:scope:{clientId} -> Set<String>
         */
        String API_CLIENT_SCOPE = PREFIX + "api:client:scope:";
        long API_CLIENT_SCOPE_TIMEOUT_MINUTES = 30;
    }

    /************ 管理后台 Token ************/
    interface AdminToken {
        /**
         * 管理后台访问令牌：admin:token:{token} -> LoginUser JSON
         */
        String ACCESS_TOKEN = PREFIX + "admin:token:";
        long ACCESS_TOKEN_TIMEOUT_SECONDS = 7200; // 2 小时

        /**
         * 用户权限缓存：admin:perms:{userId} -> Set<String>
         */
        String USER_PERMISSIONS = PREFIX + "admin:perms:";
        long USER_PERMISSIONS_TIMEOUT_MINUTES = 30;
    }

    /************ HLS 缓存相关 ************/
    interface Hls {
        /**
         * HLS 分片分布式锁：hls:lock:{deviceUid}:{startTime}-{endTime}
         */
        String LOCK = PREFIX + "hls:lock:";
    }
}
