package com.neurocast.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neurocast.member.domain.MemberUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员用户 Mapper
 */
@Mapper
public interface MemberUserMapper extends BaseMapper<MemberUser> {
}
