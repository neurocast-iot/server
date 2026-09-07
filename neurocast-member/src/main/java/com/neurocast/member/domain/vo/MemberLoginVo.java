package com.neurocast.member.domain.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 会员登录结果
 */
@Getter
@Setter
public class MemberLoginVo {

    /**
     * 访问令牌（JWT）
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 访问令牌有效期（秒）
     */
    private Long expiresIn;

    private String userId;

    private String phone;

    private String nickname;
}
