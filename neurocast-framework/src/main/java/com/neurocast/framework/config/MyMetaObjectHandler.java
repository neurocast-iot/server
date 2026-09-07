package com.neurocast.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.neurocast.framework.security.LoginUser;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * MyBatis-Plus 审计字段自动填充：
 * 插入时填充 createTime/createBy/updateTime/updateBy，更新时填充 updateTime/updateBy。
 * 操作人取自 SecurityContext 中的主体名称（用户名或 API 客户端编码）。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final String SYSTEM = "system";

    @Override
    public void insertFill(MetaObject metaObject) {
        OffsetDateTime now = OffsetDateTime.now();
        String operator = currentOperator();
        this.strictInsertFill(metaObject, "createTime", OffsetDateTime.class, now);
        this.strictInsertFill(metaObject, "createBy", String.class, operator);
        this.strictInsertFill(metaObject, "updateTime", OffsetDateTime.class, now);
        this.strictInsertFill(metaObject, "updateBy", String.class, operator);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", OffsetDateTime.class, OffsetDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, currentOperator());
    }

    /**
     * 获取当前操作人，未认证（如定时任务）时返回 system
     */
    private String currentOperator() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUsername();
        }
        return SYSTEM;
    }
}
