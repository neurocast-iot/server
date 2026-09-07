-- =====================================================================
-- neurocast-server 数据库初始化脚本（PostgreSQL）
-- 执行前请先创建数据库：CREATE DATABASE neurocast;
-- =====================================================================

-- ----------------------------
-- 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user
(
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64),
    phone       VARCHAR(32),
    email       VARCHAR(128),
    role_id     BIGINT,
    status      INT          NOT NULL DEFAULT 1,
    remark      VARCHAR(256),
    create_time TIMESTAMPTZ,
    create_by   VARCHAR(64),
    update_time TIMESTAMPTZ,
    update_by   VARCHAR(64),
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);
COMMENT ON TABLE sys_user IS '系统用户';

-- ----------------------------
-- 系统角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role
(
    id          BIGSERIAL PRIMARY KEY,
    role_code   VARCHAR(32) NOT NULL,
    role_name   VARCHAR(64) NOT NULL,
    status      INT         NOT NULL DEFAULT 1,
    remark      VARCHAR(256),
    create_time TIMESTAMPTZ,
    create_by   VARCHAR(64),
    update_time TIMESTAMPTZ,
    update_by   VARCHAR(64),
    CONSTRAINT uk_sys_role_code UNIQUE (role_code)
);
COMMENT ON TABLE sys_role IS '系统角色';

-- ----------------------------
-- 权限标识表（后端只管权限标识，前端根据权限列表自行控制菜单显隐）
-- ----------------------------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission
(
    id              BIGSERIAL PRIMARY KEY,
    permission_code VARCHAR(128) NOT NULL,
    description     VARCHAR(256),
    type            SMALLINT     NOT NULL DEFAULT 1,
    status          INT          NOT NULL DEFAULT 1,
    create_time     TIMESTAMPTZ,
    create_by       VARCHAR(64),
    update_time     TIMESTAMPTZ,
    update_by       VARCHAR(64),
    CONSTRAINT uk_sys_permission_code UNIQUE (permission_code)
);
COMMENT ON TABLE sys_permission IS '统一权限标识（type: 1=用户权限, 2=API Scope）';

-- ----------------------------
-- 角色-权限关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission
(
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);
COMMENT ON TABLE sys_role_permission IS '角色-权限关联';

-- ----------------------------
-- API 客户端表（内部系统接入凭证，X-API-KEY 认证）
-- ----------------------------
DROP TABLE IF EXISTS api_client;
CREATE TABLE api_client
(
    id          BIGSERIAL PRIMARY KEY,
    client_code VARCHAR(64)  NOT NULL,
    client_name VARCHAR(128),
    api_key     VARCHAR(128) NOT NULL,
    status      INT          NOT NULL DEFAULT 1,
    expire_time TIMESTAMPTZ,
    remark      VARCHAR(256),
    create_time TIMESTAMPTZ,
    create_by   VARCHAR(64),
    update_time TIMESTAMPTZ,
    update_by   VARCHAR(64),
    CONSTRAINT uk_api_client_code UNIQUE (client_code),
    CONSTRAINT uk_api_client_key UNIQUE (api_key)
);
COMMENT ON TABLE api_client IS 'API 客户端';

-- ----------------------------
-- API 客户端权限范围表（独立于用户角色，控制客户端可调用的接口）
-- ----------------------------
DROP TABLE IF EXISTS api_client_scope;
CREATE TABLE api_client_scope
(
    client_id BIGINT      NOT NULL,
    scope     VARCHAR(64) NOT NULL,
    PRIMARY KEY (client_id, scope)
);
COMMENT ON TABLE api_client_scope IS 'API 客户端权限范围';

-- ----------------------------
-- 产品表（设备类型，关联 ThingsBoard Device Profile）
-- ----------------------------
DROP TABLE IF EXISTS product;
CREATE TABLE product
(
    id            VARCHAR(64) PRIMARY KEY,
    name          VARCHAR(128) NOT NULL,
    model         VARCHAR(64)  NOT NULL,
    tb_profile_id VARCHAR(64)  NOT NULL,
    description   VARCHAR(256),
    create_time   TIMESTAMPTZ,
    create_by     VARCHAR(64),
    update_time   TIMESTAMPTZ,
    update_by     VARCHAR(64),
    CONSTRAINT uk_product_model UNIQUE (model)
);
COMMENT ON TABLE product IS '产品（设备类型）';

-- ----------------------------
-- 设备表（与 ThingsBoard 设备一一对应）
-- ----------------------------
DROP TABLE IF EXISTS device;
CREATE TABLE device
(
    id                  VARCHAR(64) PRIMARY KEY,
    product_id          VARCHAR(64),
    tb_device_id        VARCHAR(64) NOT NULL,
    device_uid          VARCHAR(64) NOT NULL,
    name                VARCHAR(128),
    status              INT         NOT NULL DEFAULT 0,
    lasted_online_time  TIMESTAMPTZ,
    lasted_offline_time TIMESTAMPTZ,
    create_time         TIMESTAMPTZ,
    create_by           VARCHAR(64),
    update_time         TIMESTAMPTZ,
    update_by           VARCHAR(64),
    CONSTRAINT uk_device_uid UNIQUE (device_uid),
    CONSTRAINT uk_device_tb_id UNIQUE (tb_device_id)
);
COMMENT ON TABLE device IS '设备';
CREATE INDEX idx_device_status ON device (status);
CREATE INDEX idx_device_product ON device (product_id);

-- ----------------------------
-- 设备全局配置表（所有设备通用，KV 存储，变更后批量下发全部设备）
-- ----------------------------
DROP TABLE IF EXISTS device_global_config;
CREATE TABLE device_global_config
(
    config_key   VARCHAR(64) PRIMARY KEY,
    config_value VARCHAR(512) NOT NULL,
    description  VARCHAR(256),
    update_time  TIMESTAMPTZ,
    update_by    VARCHAR(64)
);
COMMENT ON TABLE device_global_config IS '设备全局配置（所有设备通用，变更后经 ThingsBoard 共享属性下发）';

-- ----------------------------
-- 系统配置表（服务端自身的 KV 配置，不下发设备）
-- ----------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config
(
    config_key   VARCHAR(64) PRIMARY KEY,
    config_value VARCHAR(512) NOT NULL,
    description  VARCHAR(256),
    update_time  TIMESTAMPTZ,
    update_by    VARCHAR(64)
);
COMMENT ON TABLE sys_config IS '系统配置（服务端自身配置，仅供后端读取）';

-- ----------------------------
-- 设备 RPC 指令记录表
-- ----------------------------
DROP TABLE IF EXISTS device_rpc_log;
CREATE TABLE device_rpc_log
(
    id          VARCHAR(64) PRIMARY KEY,
    device_uid  VARCHAR(64) NOT NULL,
    method      VARCHAR(64) NOT NULL,
    description VARCHAR(128),
    params      TEXT,
    status      VARCHAR(16) NOT NULL,
    response    TEXT,
    cost_ms     BIGINT,
    create_time TIMESTAMPTZ,
    create_by   VARCHAR(64),
    update_time TIMESTAMPTZ,
    update_by   VARCHAR(64)
);
COMMENT ON TABLE device_rpc_log IS '设备 RPC 指令记录';
CREATE INDEX idx_device_rpc_log_uid ON device_rpc_log (device_uid, create_time DESC);

-- ----------------------------
-- 抓拍图片记录表
-- ----------------------------
DROP TABLE IF EXISTS camera_image;
CREATE TABLE camera_image
(
    id          VARCHAR(64) PRIMARY KEY,
    device_uid  VARCHAR(64),
    tb_device_id VARCHAR(64),
    name        VARCHAR(256),
    event_time  BIGINT,
    file_size   BIGINT,
    status      INT NOT NULL DEFAULT 0,
    trigger_type VARCHAR(32),
    file_path   VARCHAR(512),
    create_time TIMESTAMPTZ,
    create_by   VARCHAR(64),
    update_time TIMESTAMPTZ,
    update_by   VARCHAR(64)
);
COMMENT ON TABLE camera_image IS '抓拍图片记录';
CREATE INDEX idx_camera_image_device_time ON camera_image (device_uid, event_time);
CREATE INDEX idx_camera_image_name ON camera_image (name);

-- ----------------------------
-- 录像记录表
-- ----------------------------
DROP TABLE IF EXISTS camera_video;
CREATE TABLE camera_video
(
    id          VARCHAR(64) PRIMARY KEY,
    device_uid  VARCHAR(64),
    tb_device_id VARCHAR(64),
    name        VARCHAR(256),
    event_time  BIGINT,
    start_time  BIGINT,
    duration    INT,
    file_size   BIGINT,
    status      INT NOT NULL DEFAULT 0,
    hls_status  INT NOT NULL DEFAULT 0,
    trigger_type VARCHAR(32),
    file_path   VARCHAR(512),
    create_time TIMESTAMPTZ,
    create_by   VARCHAR(64),
    update_time TIMESTAMPTZ,
    update_by   VARCHAR(64)
);
COMMENT ON TABLE camera_video IS '录像记录';
CREATE INDEX idx_camera_video_device_time ON camera_video (device_uid, event_time);
CREATE INDEX idx_camera_video_device_start ON camera_video (device_uid, start_time);
CREATE INDEX idx_camera_video_name ON camera_video (name);

-- ----------------------------
-- OTA 包表（本地维护包元数据，ThingsBoard 仅负责升级下发）
-- ----------------------------
DROP TABLE IF EXISTS ota_package;
CREATE TABLE ota_package
(
    id                  VARCHAR(64) PRIMARY KEY,
    product_id          VARCHAR(64) NOT NULL,
    tb_package_id       VARCHAR(64),
    title               VARCHAR(128) NOT NULL,
    version             VARCHAR(64)  NOT NULL,
    version_num         INT          NOT NULL DEFAULT 0,
    ota_type            VARCHAR(16)  NOT NULL,
    url                 VARCHAR(512) NOT NULL,
    checksum_algorithm  VARCHAR(32),
    checksum            VARCHAR(128),
    description         VARCHAR(256),
    create_time         TIMESTAMPTZ,
    create_by           VARCHAR(64),
    update_time         TIMESTAMPTZ,
    update_by           VARCHAR(64),
    CONSTRAINT uk_ota_package_title_version UNIQUE (title, version)
);
COMMENT ON TABLE ota_package IS 'OTA 包';
CREATE INDEX idx_ota_package_product ON ota_package (product_id);
CREATE INDEX idx_ota_package_type ON ota_package (ota_type);

-- =====================================================================
-- 初始化数据
-- =====================================================================

-- ----------------------------
-- 会员用户表（C 端移动端用户，独立于管理后台 sys_user）
-- ----------------------------
DROP TABLE IF EXISTS member_user;
CREATE TABLE member_user
(
    id          VARCHAR(64) PRIMARY KEY,
    phone       VARCHAR(20),
    password    VARCHAR(128),
    nickname    VARCHAR(64),
    avatar      VARCHAR(256),
    status      INT          NOT NULL DEFAULT 1,
    create_time TIMESTAMPTZ,
    create_by   VARCHAR(64),
    update_time TIMESTAMPTZ,
    update_by   VARCHAR(64),
    CONSTRAINT uk_member_user_phone UNIQUE (phone)
);
COMMENT ON TABLE member_user IS '会员用户（C 端）';

-- =====================================================================
-- 初始化数据
-- =====================================================================

-- 角色
INSERT INTO sys_role (role_code, role_name, status, remark, create_time)
VALUES ('ADMIN', '管理员', 1, '全部权限', now()),
       ('OPERATOR', '操作员', 1, '设备/媒体/OTA 操作权限', now()),
       ('VIEWER', '观察者', 1, '只读权限', now());

-- 管理员账号：admin / admin123（BCrypt），角色为 ADMIN
INSERT INTO sys_user (username, password, nickname, role_id, status, remark, create_time)
SELECT 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
       '超级管理员', r.id, 1, '系统内置账号', now()
FROM sys_role r WHERE r.role_code = 'ADMIN';

-- 内部 API 客户端（ThingsBoard 规则引擎回调 /api/data 使用，X-API-KEY 认证）
INSERT INTO api_client (client_code, client_name, api_key, status, remark, create_time)
VALUES ('inner', 'ThingsBoard 规则引擎', 'neurocast-inner-api-key-2026', 1,
        'ThingsBoard 规则引擎数据回传客户端', now());

INSERT INTO api_client_scope (client_id, scope)
SELECT c.id, s.scope
FROM api_client c
CROSS JOIN (VALUES ('event:push'), ('device:read')) AS s(scope)
WHERE c.client_code = 'inner';

-- ----------------------------
-- 初始权限标识数据（type=1 用户权限 + type=2 API Scope 统一管理）
-- ----------------------------

INSERT INTO sys_permission (id, permission_code, description, type, status, create_time) VALUES
-- 用户权限（type=1）
(1,  'system:user:list',     '查看用户列表',     1, 1, now()),
(2,  'system:user:create',   '创建用户',         1, 1, now()),
(3,  'system:user:update',   '编辑用户',         1, 1, now()),
(4,  'system:user:delete',   '删除用户',         1, 1, now()),
(5,  'system:role:list',     '查看角色列表',     1, 1, now()),
(6,  'system:apiClient:list','查看 API 客户端',  1, 1, now()),
(7,  'system:apiClient:create','创建 API 客户端',1, 1, now()),
(8,  'system:apiClient:update','编辑 API 客户端',1, 1, now()),
(9,  'system:apiClient:delete','删除 API 客户端',1, 1, now()),
(10, 'device:product:list',  '查看产品列表',     1, 1, now()),
(11, 'device:product:create','创建产品',         1, 1, now()),
(12, 'device:product:update','编辑产品',         1, 1, now()),
(13, 'device:product:delete','删除产品',         1, 1, now()),
(14, 'device:list',          '查看设备列表',     1, 1, now()),
(15, 'device:create',        '创建设备',         1, 1, now()),
(16, 'device:update',        '编辑设备',         1, 1, now()),
(17, 'device:delete',        '删除设备',         1, 1, now()),
(18, 'media:library:list',   '查看媒体库',       1, 1, now()),
(19, 'media:camera:list',    '查看相机管理',     1, 1, now()),
(20, 'ota:package:list',     '查看 OTA 包',      1, 1, now()),
(21, 'ota:package:create',   '创建 OTA 包',      1, 1, now()),
(22, 'ota:package:update',   '编辑 OTA 包',      1, 1, now()),
(23, 'ota:package:delete',   '删除 OTA 包',      1, 1, now()),
-- API Scope（type=2），供 API 客户端分配
(24, 'device:read',          '读取设备信息',     2, 1, now()),
(25, 'event:push',           '推送设备事件',     2, 1, now()),
(26, 'srs:callback',         'SRS 流媒体回调',   2, 1, now());

-- 显式 ID 插入后同步 sequence，防止后续自增 ID 冲突
SELECT setval('sys_permission_id_seq', (SELECT MAX(id) FROM sys_permission));

-- ADMIN 拥有全部用户权限（type=1）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'ADMIN' AND p.type = 1;

-- OPERATOR：设备 + 媒体 + OTA（不含系统管理）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'OPERATOR'
  AND (p.permission_code LIKE 'device:%' OR p.permission_code LIKE 'media:%' OR p.permission_code LIKE 'ota:%');

-- VIEWER：仅查看类权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'VIEWER'
  AND p.permission_code LIKE '%:list';
