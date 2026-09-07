package com.neurocast.member.domain;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.neurocast.common.core.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 会员用户（C 端移动端用户）
 */
@Getter
@Setter
@TableName("member_user")
public class MemberUser extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 手机号（主要登录凭证）
     */
    private String phone;

    /**
     * 密码（BCrypt 加密，不序列化到响应）
     */
    @JSONField(serialize = false)
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 状态：1 启用 0 禁用
     */
    private Integer status;
}
