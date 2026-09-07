package com.neurocast.system.domain.vo;

/**
 * 登录结果
 *
 * @param accessToken  访问令牌
 * @param refreshToken 刷新令牌
 * @param tokenType    令牌类型，固定 Bearer
 * @param expiresIn    访问令牌有效期（秒）
 * @param userId       用户 ID
 * @param username     登录用户名
 * @param nickname     昵称
 * @param roleCode     角色编码
 */
public record LoginVo(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Long userId,
        String username,
        String nickname,
        String roleCode
) {
    /** tokenType 固定值 */
    private static final String DEFAULT_TOKEN_TYPE = "Bearer";

    /**
     * 构造登录响应（tokenType 默认 Bearer）
     */
    public static LoginVo of(String accessToken, String refreshToken, Long expiresIn,
                             Long userId, String username, String nickname, String roleCode) {
        return new LoginVo(accessToken, refreshToken, DEFAULT_TOKEN_TYPE,
                expiresIn, userId, username, nickname, roleCode);
    }
}
