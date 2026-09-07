# NeuroCast Community

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)

NeuroCast 是一个开源的 IoT 设备管理平台，提供设备管理、实时流媒体、远程控制等功能。

## ✨ 特性

- **设备管理**：设备 CRUD、批量创建、产品管理（设备类型）
- **设备配置**：通过 ThingsBoard SHARED_SCOPE 下发配置，支持视频参数、抓拍参数、OSD 水印
- **远程控制**：FRP 内网穿透、SSH 隧道、设备重启/复位
- **实时流媒体**：基于 SRS 的实时流推送、HLS 回放
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
- **SRS**（流媒体服务器）
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

## 📖 API 文档

详细的 API 文档请参考 [API接口文档](docs/API接口文档.md)

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

- [功能架构文档](docs/功能架构文档.md)
- [API接口文档](docs/API接口文档.md)
- [OSD 配置协议](docs/osd_elements_config_api.md)
- [触发器配置协议](docs/triggers_config_api.md)

## 💬 交流

如有问题，请提交 Issue 或联系维护者。

---

**注意**：本开源版本不包含 OTA 升级管理功能，如需使用请购买企业版。
