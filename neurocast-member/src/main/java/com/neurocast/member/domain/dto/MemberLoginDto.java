package com.neurocast.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 会员登录请求（手机号 + 密码）
 */
@Getter
@Setter
public class MemberLoginDto {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
