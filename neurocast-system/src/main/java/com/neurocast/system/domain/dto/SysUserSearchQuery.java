package com.neurocast.system.domain.dto;

import com.neurocast.common.core.domain.PageQuery;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户分页查询条件
 */
@Getter
@Setter
public class SysUserSearchQuery extends PageQuery {

    /**
     * 用户名 / 昵称模糊搜索
     */
    private String keyword;
}
