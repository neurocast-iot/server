package com.neurocast.member.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 会员资料更新请求
 */
@Getter
@Setter
public class MemberUpdateProfileDto {

    private String nickname;

    private String avatar;
}
