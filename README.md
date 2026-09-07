# NeuroCast Community

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![WebRTC](https://img.shields.io/badge/WebRTC-P2P%20%2B%20SFU-red.svg)](https://webrtc.org/)

NeuroCast 是一套完整的音视频解决方案的服务端，以 **WebRTC** 为核心实时视频方案，提供设备管理、WebRTC/FLV 实时直播、HLS 回放、远程控制等功能。

> 🖥️ [前端效果预览](https://github.com/neurocast-iot/docs/blob/main/cn/README.md#%E7%AE%A1%E7%90%86%E5%B9%B3%E5%8F%B0%E7%95%8C%E9%9D%A2%E5%B1%95%E7%A4%BAconsole)

## ✨ 特性

- **设备管理**：设备 CRUD、批量创建、产品管理（设备类型）
- **设备配置**：通过 ThingsBoard SHARED_SCOPE 下发配置，支持视频参数、抓拍参数、OSD 水印
- **远程控制**：FRP 内网穿透、SSH 隧道、设备重启/复位
- **实时流媒体**：基于 SRS 的 WebRTC 实时推流（P2P/SFU）、HTTP-FLV 直播、HLS 回放
- **触发器配置**：定时抓拍、录像、蓝牙标签等事件触发器
- **权限管理**：基于 RBAC 的用户/角色/权限管理
- **API 客户端**：支持 API Key 认证的客户端管理

## 🏗️ 架构

```
server/
├── neurocast-bootstrap    # 启动模块
├── neurocast-common       # 公共模块（常量、工具类、异常）
├── neurocast-device       # 设备管理（设备、产品、配置、指令）
├── neurocast-framework    # 核心框架（安全、集成、配置）
├── neurocast-job          # 定时任务（HLS 清理、媒体清理）
├── neurocast-media        # 媒体服务（实时流、HLS、媒体库）
├── neurocast-member       # 用户模块（APP 端用户认证）
└── neurocast-system       # 系统管理（用户、角色、权限、配置）
```

## 🔧 技术栈

- **Java 21**
- **Spring Boot 3.x**
- **Spring Security** + JWT
- **MyBatis-Plus**
- **PostgreSQL**
- **Redis**
- **ThingsBoard**（设备管理与遥测）
- **SRS**（WebRTC SFU 流媒体服务器）
- **FRP**（内网穿透）

## 📋 前置要求

- JDK 21+
- PostgreSQL 14+
- Redis 6+
- ThingsBoard 3.x
- SRS 5.x
- Maven 3.8+

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/neurocast-iot/server.git
cd server
```

### 2. 初始化数据库

```bash
# 创建数据库
createdb neurocast

# 执行 schema
psql neurocast < neurocast-bootstrap/src/main/resources/sql/schema.sql
```

### 3. 配置应用

编辑 `neurocast-bootstrap/src/main/resources/application-local.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/neurocast
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

neurocast:
  jwt:
    secret: your-jwt-secret-change-me
  thingsboard:
    url: http://localhost:9090/
    username: tenant@thingsboard.org
    password: your-thingsboard-password
  srs:
    api-url: http://localhost:1985
    base-url: http://localhost:8080/
  frp:
    server-addr: your-frp-server-ip
    server-port: 10000
    token: your-frp-token
```

### 4. 编译运行

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar neurocast-bootstrap/target/neurocast-bootstrap-1.0.0.jar --spring.profiles.active=local
```

### 5. 访问

- API 文档：http://localhost:18189/doc.html
- 默认管理员：admin / admin123

## 📖 文档

完整的项目文档（含服务端、固件端、管理平台截图）请参考 [NeuroCast 文档中心](https://github.com/neurocast-iot/docs)。

| 文档 | 说明 |
|------|------|
| [服务端架构](https://github.com/neurocast-iot/docs/blob/main/cn/server/architecture.md) | 系统概述、功能模块、技术栈、认证机制 |
| [API 接口文档](https://github.com/neurocast-iot/docs/blob/main/cn/server/api/overview.md) | 认证方式、响应格式、错误码总表 |
| [OSD 配置协议](https://github.com/neurocast-iot/docs/blob/main/cn/server/protocol/osd_elements_config_api.md) | OSD 水印配置 API |
| [触发器配置协议](https://github.com/neurocast-iot/docs/blob/main/cn/server/protocol/triggers_config_api.md) | 事件触发器配置 API |

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发流程

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 遵循现有的代码风格
- 新增功能需要编写单元测试
- 提交信息使用中文或英文，清晰描述更改内容

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

## 🔗 相关链接

- [文档中心](https://github.com/neurocast-iot/docs) — 完整项目文档（服务端、固件端、管理平台截图）
- [固件端源码](https://github.com/neurocast-iot/firmware) — 设备端嵌入式固件
- [管理平台](https://github.com/neurocast-iot/platform) — Web 管理前端

## 💬 联系

如有问题或合作意向，请发送邮件至 [hnngm163@gmail.com](mailto:hnngm163@gmail.com)。

---

**注意**：本开源版本不包含 OTA 升级管理功能，如需使用请购买企业版。
