# NeuroCast API 接口文档

> Base URL: `http://<host>:8189`
>
> 认证方式：
> - 管理后台：Redis Token（`Authorization: Bearer <accessToken>`）
> - C 端会员：JWT（`Authorization: Bearer <accessToken>`）
> - 第三方服务：X-API-KEY（`X-API-KEY: <apiKey>`）
>
> 路径规范：
> - `/api/admin/**` — 管理后台接口（需 Redis Token 认证）
> - `/api/app/**` — C 端会员接口（需 JWT 认证）
> - `/api/auth/**` — 管理后台认证接口（公开）
> - `/api/device/event` — TB 回调（X-API-KEY）

---

## 1. 认证模块 `/api/auth`

### 1.1 登录

`POST /api/auth/login` — 无需认证

**请求体**：
```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应**：
```json
{
  "code": 0,
  "data": {
    "accessToken": "a1b2c3d4e5f6...",
    "refreshToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "roleCode": "ADMIN"
  }
}
```

> 管理后台 accessToken 为不透明 Redis Token（非 JWT），服务端可随时作废。

### 1.2 刷新令牌

`POST /api/auth/refresh` — 无需认证

**请求体**：
```json
{ "refreshToken": "eyJhbGciOi..." }
```

**响应**：同登录响应

### 1.3 登出

`POST /api/auth/logout` — 需要 Bearer Token

**响应**：`{ "code": 0 }`

### 1.4 当前用户信息

`GET /api/auth/userinfo` — 需要 Bearer Token

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "status": 1,
    "remark": "超级管理员",
    "createTime": "2026-07-01T08:00:00+08:00",
    "createBy": "system",
    "updateTime": "2026-08-01T10:00:00+08:00",
    "updateBy": "system"
  },
  "message": "success"
}
```

> `password` 字段已脱敏，不会返回

### 1.5 当前用户权限列表

`GET /api/auth/permissions` — 需要 Bearer Token

前端登录后调用此接口获取权限标识列表，根据返回结果控制菜单显隐。

**响应示例**：
```json
{
  "code": 0,
  "data": [
    "system:user:list",
    "system:user:create",
    "device:list",
    "device:create",
    "media:library:list",
    "ota:package:list"
  ],
  "message": "success"
}
```

> 权限标识与前端路由/菜单配置中的 `access` 字段对应，后端不管菜单，只管权限标识。

---

## 2. 用户管理 `/api/admin/system/users` — 需要 ADMIN 角色

### 2.1 分页查询用户

`GET /api/admin/system/users?pageNum=1&pageSize=10`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| pageNum | query | int | 否 | 页码，默认 1 |
| pageSize | query | int | 否 | 每页条数，默认 10 |
| keyword | query | string | 否 | 用户名/昵称模糊搜索 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "total": 50,
    "items": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "管理员",
        "phone": "13800138000",
        "email": "admin@example.com",
        "roleCode": "ADMIN",
        "roleName": "管理员",
        "active": true,
        "createTime": "2026-07-01T08:00:00+08:00"
      }
    ]
  },
  "message": "success"
}
```

**响应字段说明**：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | long | 用户 ID |
| username | string | 登录用户名 |
| nickname | string | 昵称 |
| phone | string | 手机号 |
| email | string | 邮箱 |
| roleCode | string | 角色编码，如 `ADMIN` / `OPERATOR` / `VIEWER` |
| roleName | string | 角色名称 |
| active | boolean | 是否启用 |
| createTime | string | 创建时间 |

### 2.2 创建用户

`POST /api/admin/system/users`

**请求体**：
```json
{
  "username": "zhangsan",
  "password": "Abc12345",
  "nickname": "张三",
  "phone": "13800138001",
  "email": "zhangsan@example.com",
  "status": 1,
  "roleCode": "OPERATOR",
  "remark": "运营人员"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | 是 | 用户名（3-32 位） |
| password | string | 是 | 密码（创建时必填，6-64 位） |
| nickname | string | 否 | 昵称 |
| phone | string | 否 | 手机号 |
| email | string | 否 | 邮箱 |
| status | int | 否 | 状态：1=启用 0=禁用 |
| roleCode | string | 否 | 角色编码：ADMIN/OPERATOR/VIEWER |
| remark | string | 否 | 备注 |

### 2.3 更新用户

`PUT /api/admin/system/users`

**请求体**：同创建，额外包含 `"id": 1`。`password` 为空表示不修改密码。

### 2.4 删除用户

`DELETE /api/admin/system/users/{userId}`

### 2.5 重置密码

`PUT /api/admin/system/users/{userId}/password`

**请求体**：
```json
{ "newPassword": "NewPass123" }
```

---

## 3. 角色管理 `/api/admin/system/roles` — 需要 ADMIN 角色

### 3.1 查询角色列表

`GET /api/admin/system/roles`

返回系统固定角色列表，供创建用户时下拉选择。

**响应示例**：
```json
{
  "code": 0,
  "data": [
    { "id": 1, "roleCode": "ADMIN", "roleName": "超级管理员", "active": true, "remark": null },
    { "id": 2, "roleCode": "OPERATOR", "roleName": "运营人员", "active": true, "remark": null },
    { "id": 3, "roleCode": "VIEWER", "roleName": "只读用户", "active": true, "remark": null }
  ],
  "message": "success"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| id | long | 角色 ID |
| roleCode | string | 角色编码 |
| roleName | string | 角色名称 |
| active | boolean | 是否启用 |
| remark | string | 备注 |

### 3.2 创建角色

`POST /api/admin/system/roles`

**请求体**：
```json
{
  "roleCode": "EDITOR",
  "roleName": "编辑人员",
  "status": 1,
  "remark": "负责内容编辑"
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|---|---|---|---|---|
| roleCode | string | 是 | 最大 32 字符 | 角色编码，全局唯一 |
| roleName | string | 是 | 最大 64 字符 | 角色名称 |
| status | int | 否 | | 状态：1=启用 0=禁用，默认 1 |
| remark | string | 否 | 最大 256 字符 | 备注 |

**响应示例**：
```json
{ "code": 0, "data": null, "message": "success" }
```

**错误码**：

| code | message | 说明 |
|---|---|---|
| 1000 | 参数校验失败 | 缺少必填字段或超出长度限制 |
| 7003 | 角色编码已存在 | roleCode 重复 |

### 3.3 更新角色

`PUT /api/admin/system/roles`

**请求体**：同创建，额外包含 `"id": 4`。`roleCode` 变更时校验唯一性。

**错误码**：

| code | message | 说明 |
|---|---|---|
| 1000 | 参数校验失败 | 缺少必填字段 |
| 7002 | 角色不存在 | ID 无效 |
| 7003 | 角色编码已存在 | roleCode 重复 |

### 3.4 删除角色

`DELETE /api/admin/system/roles/{roleId}`

删除角色。如果有用户正在使用该角色，将拒绝删除。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| roleId | path | long | 是 | 角色 ID |

**响应示例**：
```json
{ "code": 0, "data": null, "message": "success" }
```

**错误码**：

| code | message | 说明 |
|---|---|---|
| 7002 | 角色不存在 | roleId 无效 |
| 7004 | 角色正在被用户使用，无法删除 | 有用户引用该角色 |

### 3.5 查询角色的权限标识列表

`GET /api/admin/system/roles/{roleId}/permissions`

返回指定角色已分配的权限列表（含完整字段）。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| roleId | path | long | 是 | 角色 ID |

**响应示例**：
```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "permissionCode": "system:user:list",
      "description": "查看用户列表",
      "active": true,
      "createTime": "2025-01-01T00:00:00+08:00"
    },
    {
      "id": 2,
      "permissionCode": "system:user:create",
      "description": "创建用户",
      "active": true,
      "createTime": "2025-01-01T00:00:00+08:00"
    }
  ],
  "message": "success"
}
```

**响应字段说明**：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | long | 权限 ID |
| permissionCode | string | 权限标识，如 `system:user:list` |
| description | string | 权限描述 |
| active | boolean | 是否启用 |
| createTime | string | 创建时间 |

**错误码**：

| code | message | 说明 |
|---|---|---|
| 7002 | 角色不存在 | roleId 无效 |

### 3.6 为角色分配权限

`PUT /api/admin/system/roles/{roleId}/permissions`

全量替换角色的权限列表。传入新的权限 ID 列表，系统会先清空该角色的所有权限关联，再批量插入新的关联。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| roleId | path | long | 是 | 角色 ID |

**请求体**：
```json
[1, 2, 3, 14]
```

| 字段 | 类型 | 说明 |
|---|---|---|
| (数组元素) | long[] | 权限 ID 列表，空数组表示清空该角色的所有权限 |

**响应示例**：
```json
{
  "code": 0,
  "data": null,
  "message": "success"
}
```

**错误码**：

| code | message | 说明 |
|---|---|---|
| 7002 | 角色不存在 | roleId 无效 |

---

## 4. 权限管理 `/api/admin/system/permissions` — 需要 ADMIN 角色

权限标识由管理员维护，前端根据用户权限列表控制菜单显隐。支持权限的 CRUD（增删改查）和启用/禁用。

### 4.1 查询全部权限标识

`GET /api/admin/system/permissions`

返回系统全部权限标识（含启用和禁用），供权限管理页面使用。

**响应示例**：
```json
{
  "code": 0,
  "data": [
    { "id": 1, "permissionCode": "system:user:list", "description": "查看用户列表", "active": true, "createTime": "2026-01-15T10:30:00+08:00" },
    { "id": 2, "permissionCode": "system:user:create", "description": "创建用户", "active": true, "createTime": "2026-01-15T10:30:00+08:00" },
    { "id": 14, "permissionCode": "device:list", "description": "查看设备列表", "active": false, "createTime": "2026-02-20T14:00:00+08:00" }
  ],
  "message": "success"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| id | long | 权限 ID |
| permissionCode | string | 权限标识，如 `system:user:list` |
| description | string | 描述 |
| active | boolean | 是否启用 |
| createTime | string | 创建时间（ISO 8601） |

### 4.2 新增权限标识

`POST /api/admin/system/permissions`

**请求体**：
```json
{
  "permissionCode": "system:role:delete",
  "description": "删除角色"
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|---|---|---|---|---|
| permissionCode | string | 是 | 最大 128 字符 | 权限标识，全局唯一 |
| description | string | 否 | 最大 256 字符 | 描述 |

**响应示例**：
```json
{
  "code": 0,
  "data": null,
  "message": "success"
}
```

**错误码**：

| code | message | 说明 |
|---|---|---|
| 1000 | 参数校验失败 | 缺少必填字段或超出长度限制 |
| 9001 | 权限标识已存在 | permissionCode 重复 |

### 4.3 删除权限

`DELETE /api/admin/system/permissions/{id}`

删除权限标识。如果有角色正在使用该权限，将拒绝删除。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| id | path | long | 是 | 权限 ID |

**响应示例**：
```json
{
  "code": 0,
  "data": null,
  "message": "success"
}
```

**错误码**：

| code | message | 说明 |
|---|---|---|
| 9000 | 权限不存在 | ID 无效 |
| 9002 | 权限正在被角色使用，无法删除 | 有角色引用该权限 |

### 4.4 启用/禁用权限

`PUT /api/admin/system/permissions/{id}/active?active=true`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| id | path | long | 是 | 权限 ID |
| active | query | boolean | 是 | true=启用，false=禁用 |

**响应示例**：
```json
{
  "code": 0,
  "data": null,
  "message": "success"
}
```

**错误码**：

| code | message | 说明 |
|---|---|---|
| 9000 | 权限不存在 | ID 无效 |

---

## 5. API 客户端管理 `/api/admin/system/api-clients` — 需要 ADMIN 角色

### 5.1 分页查询

`GET /api/admin/system/api-clients?pageNum=1&pageSize=10`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| pageNum | query | int | 否 | 页码，默认 1 |
| pageSize | query | int | 否 | 每页条数，默认 10 |
| keyword | query | string | 否 | 客户端编码/名称模糊搜索 |

### 5.2 创建客户端

`POST /api/admin/system/api-clients`

**请求体**：
```json
{
  "clientCode": "device-gateway",
  "clientName": "设备网关",
  "scopes": ["event:push", "device:read"],
  "status": 1,
  "expireTime": "2027-12-31T23:59:59+08:00",
  "remark": "用于设备接入"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| clientCode | string | 是 | 客户端编码（全局唯一） |
| clientName | string | 否 | 客户端名称 |
| scopes | string[] | 否 | 权限范围列表，如 `["ota:read", "device:read", "event:push"]` |
| status | int | 否 | 状态：1=启用 0=禁用 |
| expireTime | string | 否 | 过期时间（ISO 8601，为空表示永不过期） |
| remark | string | 否 | 备注 |

> `scopes` 控制该客户端可以调用哪些接口，接口通过 `@PreAuthorize("@ss.hasScope('xxx')")` 声明所需 scope。

**响应**：返回含 `apiKey` 的完整对象（**仅此一次可见**）

### 5.3 更新客户端

`PUT /api/admin/system/api-clients`

**请求体**：
```json
{
  "id": 1,
  "clientName": "设备网关 V2",
  "scopes": ["event:push", "device:read", "ota:read"],
  "status": 1,
  "expireTime": null,
  "remark": "更新后的备注"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | long | 是 | 客户端 ID |
| clientName | string | 否 | 客户端名称 |
| scopes | string[] | 否 | 权限范围列表 |
| status | int | 否 | 状态 |
| expireTime | string | 否 | 过期时间 |
| remark | string | 否 | 备注 |

> 更新时不需要 `clientCode`

### 5.4 删除客户端

`DELETE /api/admin/system/api-clients/{clientId}`

### 5.5 重新生成 API Key

`PUT /api/admin/system/api-clients/{clientId}/key`

**响应**：返回含新 `apiKey` 的完整对象

---

## 6. 产品管理 `/api/admin/product`

产品即“设备类型”，每个产品关联一个 ThingsBoard Device Profile。

### 6.1 创建产品

`POST /api/admin/product`

**请求体**：
```json
{
  "name": "AV100 高清摄像头",
  "model": "av100",
  "tbProfileId": "d1199610-...",
  "description": "1080P 高清网络摄像头"
}
```

**响应**：
```json
{
  "code": 0,
  "data": {
    "id": "uuid...",
    "name": "AV100 高清摄像头",
    "model": "av100",
    "tbProfileId": "d1199610-...",
    "description": "1080P 高清网络摄像头",
    "createTime": "2026-08-06T10:00:00Z"
  }
}
```

### 6.2 更新产品

`PUT /api/admin/product/{productId}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| productId | path | string | 是 | 产品 ID |

**请求体**：同创建

**响应**：`{ "code": 0, "message": "success" }`

### 6.3 删除产品

`DELETE /api/admin/product/{productId}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| productId | path | string | 是 | 产品 ID |

> 产品下有设备关联时禁止删除

### 6.4 分页查询产品

`GET /api/admin/product/list?pageNum=1&pageSize=10`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| pageNum | query | int | 否 | 页码，默认 1 |
| pageSize | query | int | 否 | 每页条数，默认 10 |
| keyword | query | string | 否 | 名称/型号模糊搜索 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "total": 5,
    "items": [
      {
        "id": "uuid...",
        "name": "AV100 高清摄像头",
        "model": "av100",
        "tbProfileId": "d1199610-...",
        "description": "1080P 高清网络摄像头",
        "createTime": "2026-08-06T10:00:00+08:00"
      }
    ]
  },
  "message": "success"
}
```

### 6.5 查询产品详情

`GET /api/admin/product/{productId}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| productId | path | string | 是 | 产品 ID |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "id": "uuid...",
    "name": "AV100 高清摄像头",
    "model": "av100",
    "tbProfileId": "d1199610-...",
    "description": "1080P 高清网络摄像头",
    "createTime": "2026-08-06T10:00:00+08:00"
  },
  "message": "success"
}
```

---

## 7. 设备管理 `/api/admin/device`

### 7.1 创建设备

`POST /api/admin/device`

**请求体**：
```json
{
  "deviceUid": "A4C1380092CFA43E-C",
  "name": "1号相机",
  "productId": "uuid..."
}
```

> productId 为必填，创建设备时根据产品关联的 TB Profile 自动指定设备类型

**响应**：
```json
{
  "code": 0,
  "data": {
    "deviceUid": "A4C1380092CFA43E-C",
    "accessToken": "abc123def456ghijk"
  }
}
```

> accessToken 仅创建时返回，用于设备接入 ThingsBoard

### 7.2 批量创建设备

`POST /api/admin/device/batch`

**请求体**：`DeviceCreateDto` 数组

```json
[
  { "deviceUid": "A4C1380092CFA43E-C", "name": "1号相机", "productId": "uuid..." },
  { "deviceUid": "B5D24911A3DGB54F-D", "name": "2号相机", "productId": "uuid..." }
]
```

**响应示例**：
```json
{
  "code": 0,
  "data": [
    { "deviceUid": "A4C1380092CFA43E-C", "accessToken": "abc123def456ghijk" },
    { "deviceUid": "B5D24911A3DGB54F-D", "accessToken": "xyz789uvw012opqrs" }
  ],
  "message": "success"
}
```

> 已存在的设备自动跳过，不会报错

### 7.3 分页查询设备

`GET /api/admin/device/list?pageNum=1&pageSize=10`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| pageNum | query | int | 否 | 页码，默认 1 |
| pageSize | query | int | 否 | 每页条数，默认 10 |
| productId | query | string | 否 | 产品 ID 精确过滤 |
| deviceUid | query | string | 否 | 设备 Uid 模糊搜索 |
| status | query | int | 否 | 在线状态：1=在线 0=离线 |
| deviceUids | query | string[] | 否 | 精确匹配列表（传入时忽略模糊搜索） |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "total": 20,
    "items": [
      {
        "id": "1",
        "deviceUid": "A4C1380092CFA43E-C",
        "tbDeviceId": "d1199610-...",
        "name": "1号相机",
        "status": 1,
        "lastedOnlineTime": "2026-08-06T10:00:00+08:00",
        "lastedOfflineTime": "2026-08-05T22:00:00+08:00",
        "currentFwVersion": "1.0.3",
        "currentSwVersion": "2.1.0",
        "createTime": "2026-07-01T08:00:00+08:00",
        "product": {
          "id": "03746350-914b-11f1-b4f6-932edadd00d3",
          "name": "AV100 高清摄像头",
          "model": "av100"
        }
      }
    ]
  },
  "message": "success"
}
```
```

### 7.4 查询设备详情

`GET /api/admin/device/{deviceUid}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | path | string | 是 | 设备唯一标识 |

**响应示例**：同 7.3 中的单个 `items` 元素（含 `product` 子结构）

### 7.5 更新设备名称

`PUT /api/admin/device`

**请求体**：
```json
{
  "deviceUid": "A4C1380092CFA43E-C",
  "name": "新名称"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceUid | string | 是 | 设备唯一标识 |
| name | string | 否 | 设备名称 |

**响应**：`{ "code": 0, "message": "success" }`

### 7.6 删除设备

`DELETE /api/admin/device/{deviceUid}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | path | string | 是 | 设备唯一标识 |

**响应**：`{ "code": 0, "message": "success" }`

---

## 8. 设备配置 `/api/admin/device/config`

### 8.1 查询设备业务配置

`GET /api/admin/device/config/settings/{deviceUid}`

**响应**：
```json
{
  "code": 0,
  "data": {
    "lastUpdatedTime": 1786004613412,
    "mainCodec": "h264",
    "mainFrameRate": 20,
    "mainResolutionWidth": 1280,
    "mainResolutionHeight": 720,
    "mainBitrate": 2048,
    "mainBrMode": "cbr",
    "subCodec": "h264",
    "subFrameRate": 5,
    "subResolutionWidth": 1280,
    "subResolutionHeight": 720,
    "subBitrate": 2048,
    "subBrMode": "cbr",
    "snapshotQuality": 100,
    "recordSegmentSec": 600
  }
}
```

### 8.2 下发设备业务配置

`PUT /api/admin/device/config/settings/{deviceUid}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | path | string | 是 | 设备唯一标识 |

**请求体**：
```json
{
  "mainCodec": "h264",
  "mainFrameRate": 25,
  "mainResolutionWidth": 1280,
  "mainResolutionHeight": 720,
  "mainBitrate": 2048,
  "mainBrMode": "cbr",
  "subCodec": "h264",
  "subFrameRate": 5,
  "subResolutionWidth": 1280,
  "subResolutionHeight": 720,
  "subBitrate": 2048,
  "subBrMode": "cbr",
  "snapshotQuality": 100,
  "recordSegmentSec": 600
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|---|---|---|---|---|
| mainCodec | string | 否 | `h264` \| `h265` | 主通道编码格式 |
| mainFrameRate | int | 否 | 5-30 | 主通道帧率（fps） |
| mainResolutionWidth | int | 否 | 见下方分辨率表 | 主通道分辨率宽度 |
| mainResolutionHeight | int | 否 | 见下方分辨率表 | 主通道分辨率高度 |
| mainBitrate | int | 否 | — | 主通道码率（kbps） |
| mainBrMode | string | 否 | `cbr` \| `vbr` \| `avbr` | 主通道码率模式 |
| subCodec | string | 否 | `h264` \| `h265` | 子通道编码格式 |
| subFrameRate | int | 否 | 5-30 | 子通道帧率（fps） |
| subResolutionWidth | int | 否 | 见下方分辨率表 | 子通道分辨率宽度 |
| subResolutionHeight | int | 否 | 见下方分辨率表 | 子通道分辨率高度 |
| subBitrate | int | 否 | — | 子通道码率（kbps） |
| subBrMode | string | 否 | `cbr` \| `vbr` \| `avbr` | 子通道码率模式 |
| snapshotQuality | int | 否 | 20-100 | 拍照图片质量 |
| recordSegmentSec | int | 否 | 1-3600 | 录像分段时长（秒） |

**主通道支持的分辨率（宽×高）**：

| 分辨率 |
|---|
| 320 × 176 |
| 640 × 360 |
| 768 × 432 |
| 1024 × 576 |
| 1280 × 720 |
| 1920 × 1080 |
| 2560 × 1440 |

**子通道支持的分辨率（宽×高）**：

| 分辨率 |
|---|
| 320 × 176 |
| 640 × 360 |
| 768 × 432 |
| 1024 × 576 |
| 1280 × 720 |

> OSD 基准分辨率由前端在下发 OSD 配置接口中传入，用于像素↔千分比坐标换算

### 8.3 查询 OSD 配置

`GET /api/admin/device/config/osd/{deviceUid}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | path | string | 是 | 设备唯一标识 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "osdBaseResolutionWidth": 1920,
    "osdBaseResolutionHeight": 1080,
    "enabled": true,
    "osdElements": [
      {
        "id": "time_main",
        "type": "time",
        "enabled": true,
        "x": 23,
        "y": 24,
        "size": "medium",
        "format": "YYYY-MM-DD",
        "showWeek": false
      },
      {
        "id": "label_main",
        "type": "label",
        "enabled": true,
        "x": 96,
        "y": 97,
        "text": "1号大棚",
        "size": "medium"
      },
      {
        "id": "cover_rect",
        "type": "rect",
        "enabled": true,
        "x": 576,
        "y": 378,
        "w": 480,
        "h": 216,
        "color": "red",
        "opacity": 20
      }
    ]
  },
  "message": "success"
}
```

**响应字段说明**：

| 字段 | 类型 | 说明 |
|---|---|---|
| osdBaseResolutionWidth | int | 基准分辨率宽度（像素） |
| osdBaseResolutionHeight | int | 基准分辨率高度（像素） |
| enabled | boolean | OSD 总开关 |
| osdElements | OsdElement[] | OSD 元素列表（千分比坐标，详见下方字段表） |

**OsdElement 公共字段**：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | string | 元素唯一标识（前端生成） |
| type | string | 元素类型：`time` / `label` / `rect` / `circle` / `ellipse` / `polygon` / `bitmap` |
| enabled | boolean | 单元素开关 |
| x | int | 千分比横坐标 0~1000（后端存储值，前端可按基准分辨率换算为像素显示） |
| y | int | 千分比纵坐标 0~1000 |
| opacity | int | 透明度 0~100，0=完全不透明（当前固件暂不支持） |

**各类型专用字段**：

| 类型 | 额外字段 | 说明 |
|---|---|---|
| time | size, format, showWeek | size: small/medium/large；format: YYYY-MM-DD/MM-DD-YYYY/Chinese；showWeek: boolean |
| label | text, size | text: 显示内容；size: 字号档位 |
| rect | w, h, color | w/h: 千分比宽高；color: 填充色 |
| circle | r, color | r: 千分比半径（占画面高度）；color: 填充色 |
| ellipse | w, h, color | w/h: 千分比直径；color: 填充色 |
| polygon | points, color | points: 千分比顶点 `[[x,y],...]` 3~16 个；color: 填充色 |
| bitmap | w, h, imageUrl | w/h: 千分比目标尺寸；imageUrl: 图片下载地址 |

**color 取值**：`black` / `white` / `red` / `green` / `blue` / `yellow`，其它值回落为 `black`

> 每路画面最多同时显示 4 个有效元素，`enabled: false` 的元素不占槽位

### 8.4 下发 OSD 配置

`PUT /api/admin/device/config/osd/{deviceUid}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | path | string | 是 | 设备唯一标识 |

**请求体**：
```json
{
  "osdBaseResolutionWidth": 1920,
  "osdBaseResolutionHeight": 1080,
  "enabled": true,
  "osdElements": [
    {
      "id": "time_main",
      "type": "time",
      "enabled": true,
      "x": 12,
      "y": 22,
      "size": "medium",
      "format": "YYYY-MM-DD",
      "showWeek": false
    },
    {
      "id": "label_main",
      "type": "label",
      "enabled": true,
      "x": 96,
      "y": 97,
      "text": "1号大棚",
      "size": "medium"
    },
    {
      "id": "cover_rect",
      "type": "rect",
      "enabled": true,
      "x": 576,
      "y": 378,
      "w": 480,
      "h": 216,
      "color": "red",
      "opacity": 20
    }
  ]
}
```

| 字段 | 类型 | 必填 | 校验规则 | 说明 |
|---|---|---|---|---|
| osdBaseResolutionWidth | int | 是（有元素时） | 320-2560 | 基准分辨率宽度（像素），用于像素↔千分比换算 |
| osdBaseResolutionHeight | int | 是（有元素时） | 176-1440 | 基准分辨率高度（像素） |
| enabled | boolean | 否 | — | OSD 总开关 |
| osdElements | OsdElement[] | 否 | 最多 4 个 | OSD 元素列表（像素坐标，为 null 时清空全部水印） |

**OsdElement 像素版字段**（前端传入，后端按 osdBaseResolution 换算为千分比存储）：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | string | 元素唯一标识（前端生成，建议 UUID） |
| type | string | 类型：time / label / rect / circle / ellipse / polygon / bitmap |
| enabled | boolean | 单元素开关 |
| x | int | 横坐标（像素） |
| y | int | 纵坐标（像素） |
| opacity | int | 透明度 0~100 |
| size | string | 字号：small / medium / large |
| format | string | 日期格式：YYYY-MM-DD / MM-DD-YYYY / Chinese |
| showWeek | boolean | 是否显示星期 |
| text | string | 文本内容（label 类型） |
| w | int | 宽（像素） |
| h | int | 高（像素） |
| r | int | 半径（像素，circle 类型） |
| color | string | 填充色：black / white / red / green / blue / yellow |
| points | int[][] | 多边形顶点像素坐标 `[[x,y],...]` |
| imageUrl | string | 图片下载地址（bitmap 类型，BMP 24位无压缩） |

> **坐标换算说明**：前端传入像素值，后端按 `千分比 = 像素 × 1000 / 基准分辨率` 转换后存储；
> x/w 方向使用 osdBaseResolutionWidth，y/h 方向使用 osdBaseResolutionHeight

### 8.5 查询事件触发器配置

`GET /api/admin/device/config/triggers/{deviceUid}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | path | string | 是 | 设备唯一标识 |

**响应示例**：
```json
{
  "code": 0,
  "data": [
    {
      "id": "allday_snapshot",
      "type": "timer",
      "enabled": true,
      "allDay": true,
      "intervalSec": 600,
      "burstCount": 1
    },
    {
      "id": "work_hours_snapshot",
      "type": "timer",
      "enabled": true,
      "allDay": false,
      "priority": 10,
      "intervalSec": 120,
      "burstCount": 3,
      "burstIntervalMs": 500,
      "schedule": {
        "startTime": "09:00",
        "endTime": "18:00",
        "days": [1, 2, 3, 4, 5]
      }
    },
    {
      "id": "allday_record",
      "type": "record",
      "enabled": true,
      "allDay": true,
      "priority": 50
    },
    {
      "id": "bt_snapshot",
      "type": "bluetooth",
      "enabled": true,
      "allDay": true,
      "priority": 30,
      "burstCount": 5,
      "burstIntervalMs": 2000
    }
  ],
  "message": "success"
}
```

**Trigger 字段说明**：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | string | 触发器唯一标识（由前端生成，如 `allday_snapshot`、`bt_snapshot`） |
| type | string | 触发类型：`timer`（定时拍照）、`record`（时间段录像）、`bluetooth`（蓝牙标签拍照） |
| enabled | boolean | 是否启用 |
| priority | int | 优先级，数字越大越高，默认 0。建议：SOS=100, Motion=50, Bluetooth=30, Timer=10 |
| allDay | boolean | 是否全天生效，默认 false。`true` 时忽略 schedule |
| intervalSec | int | 拍照间隔（秒），仅 `type=timer` 时必填 |
| burstCount | int | 连拍次数，默认 1（`timer` / `bluetooth` 可用） |
| burstIntervalMs | int | 连拍间隔（毫秒），两张照片之间的等待时间 |
| schedule | Schedule | 执行计划（可选），`allDay=false` 时生效。不填 = 全天生效 |

**Schedule 字段说明**：

| 字段 | 类型 | 说明 |
|---|---|---|
| startTime | string | 生效开始时间（HH:mm 格式，24 小时制） |
| endTime | string | 生效结束时间（HH:mm 格式，24 小时制） |
| days | int[] | 生效星期几：0=周日, 1=周一, ..., 6=周六 |

### 8.6 下发事件触发器配置

`PUT /api/admin/device/config/triggers/{deviceUid}`

整体替换设备端当前的触发器配置。服务端将驼峰字段转为 snake_case 后通过 ThingsBoard SHARED_SCOPE 下发到设备。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | path | string | 是 | 设备唯一标识 |

**请求体**：
```json
{
  "triggers": [
    {
      "id": "allday_snapshot",
      "type": "timer",
      "enabled": true,
      "allDay": true,
      "intervalSec": 600,
      "burstCount": 1
    },
    {
      "id": "work_hours_snapshot",
      "type": "timer",
      "enabled": true,
      "allDay": false,
      "priority": 10,
      "intervalSec": 120,
      "burstCount": 3,
      "burstIntervalMs": 500,
      "schedule": {
        "startTime": "09:00",
        "endTime": "18:00",
        "days": [1, 2, 3, 4, 5]
      }
    },
    {
      "id": "allday_record",
      "type": "record",
      "enabled": true,
      "allDay": true,
      "priority": 50
    },
    {
      "id": "bt_snapshot",
      "type": "bluetooth",
      "enabled": true,
      "allDay": true,
      "priority": 30,
      "burstCount": 5,
      "burstIntervalMs": 2000
    }
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| triggers | Trigger[] | 是 | 触发器列表（整体替换） |

**响应**：`{ "code": 0, "message": "success" }`

### 8.7 查询设备全局配置

`GET /api/admin/device/config/global`

查询所有设备通用的配置项（如 MQTT 地址、上传 API Key 等）。

**响应示例**：
```json
{
  "code": 0,
  "data": [
    { "key": "mqtt_host", "value": "47.121.24.61", "description": "MQTT 服务器地址" },
    { "key": "mqtt_port", "value": "1883", "description": "MQTT 服务器端口" },
    { "key": "push_camera_stream_url", "value": "rtmp://47.121.24.61/live/{deviceUid}?accessToken={accessToken}", "description": "实时直播推流地址模板" },
    { "key": "upload_file_base_url", "value": "https://dev-neurocast-api.joycrystal.com/", "description": "文件上传服务地址" },
    { "key": "upload_file_api_key", "value": "nk_8f3a2b1c4d5e6f7a", "description": "文件上传服务 API Key" }
  ],
  "message": "success"
}
```

> 配置项支持前端动态新增/删除，仅返回已配置的配置项；表为空时返回空列表

### 8.8 新增或更新设备全局配置

`PUT /api/admin/device/config/global`

新增或更新全局配置，**变更项自动下发到所有设备**（经 ThingsBoard SHARED_SCOPE 共享属性：在线设备实时推送，离线设备上线后拉取最新值）。支持批量提交，仅传需要新增或变更的配置项。

**请求体**：
```json
{
  "items": [
    { "key": "ntp_server", "value": "ntp.aliyun.com", "description": "NTP 校时服务器" },
    { "key": "mqtt_port", "value": "8883", "description": "MQTT 端口" }
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| items | array | 是 | 配置项列表，不能为空 |
| items[].key | string | 是 | 配置键（snake_case 格式：小写字母开头，仅允许小写字母/数字/下划线，最长 64 位） |
| items[].value | string | 是 | 配置值（最长 512 位） |
| items[].description | string | 否 | 配置描述（最长 256 位，直接以传入值保存，不传则为空） |

**响应**：`{ "code": 0, "message": "success" }`

**错误码**：

| code | message | 说明 |
|---|---|---|
| 1000 | 参数校验失败 | items 为空，或 key 格式不符合 snake_case、超长、为空 |

> 与当前生效值相同的项不会重复下发；所有变更项在一次推送中携带，新增的键名需与设备固件协议对齐，否则设备无法识别

### 8.9 删除设备全局配置

`DELETE /api/admin/device/config/global`

删除指定全局配置项，**同时从所有设备移除对应属性**（在线设备实时通知，离线设备上线后同步）。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| key | query | string | 是 | 配置键，如 ntp_server |

**响应**：`{ "code": 0, "message": "success" }`

**错误码**：

| code | message | 说明 |
|---|---|---|
| 4008 | 设备全局配置项不存在 | key 对应的配置项未创建 |

---

## 9. 设备指令 `/api/admin/device/command`

### 9.1 启动 FRP 内网穿透

`POST /api/admin/device/command/frp/start`

**请求体**：
```json
{
  "deviceUid": "A4C1380092CFA43E-C",
  "sshLocalIp": "192.168.1.100",
  "sshLocalPort": 22
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceUid | string | 是 | 设备唯一标识 |
| sshLocalIp | string | 是 | 设备侧 SSH 本地 IP |
| sshLocalPort | int | 是 | 设备侧 SSH 本地端口 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "frpServerHost": "1.2.3.4",
    "frpServerPort": 7000,
    "remotePort": 12345
  },
  "message": "success"
}
```

### 9.2 停止 FRP

`POST /api/admin/device/command/frp/stop`

**请求体**：
```json
{ "deviceUid": "A4C1380092CFA43E-C" }
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceUid | string | 是 | 设备唯一标识 |

**响应**：`{ "code": 0, "message": "success" }`

### 9.3 启动 SSH 隧道

`POST /api/admin/device/command/ssh-tunnel/start`

通过 SSH 反向隧道暴露设备本地服务到公网。

**请求体**：
```json
{
  "deviceUid": "A4C1380092CFA43E-C",
  "localIp": "127.0.0.1",
  "localPort": 22
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceUid | string | 是 | 设备唯一标识 |
| localIp | string | 是 | 设备侧本地 IP（要暴露的服务地址） |
| localPort | int | 是 | 设备侧本地端口（要暴露的服务端口） |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "deviceUid": "A4C1380092CFA43E-C",
    "serverAddr": "1.2.3.4",
    "remotePort": 12345
  },
  "message": "success"
}
```

### 9.4 停止 SSH 隧道

`POST /api/admin/device/command/ssh-tunnel/stop`

**请求体**：
```json
{ "deviceUid": "A4C1380092CFA43E-C" }
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceUid | string | 是 | 设备唯一标识 |

**响应**：`{ "code": 0, "message": "success" }`

### 9.5 复位设备

`POST /api/admin/device/command/reset`

清除设备配置并自动重启设备。

**请求体**：
```json
{ "deviceUid": "A4C1380092CFA43E-C" }
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceUid | string | 是 | 设备唯一标识 |

**响应**：`{ "code": 0, "message": "success" }`

### 9.6 重启设备

`POST /api/admin/device/command/restart`

**请求体**：
```json
{ "deviceUid": "A4C1380092CFA43E-C" }
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceUid | string | 是 | 设备唯一标识 |

**响应**：`{ "code": 0, "message": "success" }`

### 9.7 分页查询指令记录

`GET /api/admin/device/command/command-log?pageNum=1&pageSize=10`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| pageNum | query | int | 否 | 页码，默认 1 |
| pageSize | query | int | 否 | 每页条数，默认 10 |
| deviceUid | query | string | 否 | 按设备过滤（为空查全部） |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "total": 100,
    "items": [
      {
        "id": "1",
        "deviceUid": "A4C1380092CFA43E-C",
        "method": "setDeviceConfig",
        "description": "下发设备配置",
        "params": "{\"videoFrameRate\":25}",
        "status": "SUCCESS",
        "response": "{\"result\":\"ok\"}",
        "costMs": 320,
        "createTime": "2026-08-06T10:00:00+08:00",
        "createBy": "admin"
      }
    ]
  },
  "message": "success"
}
```

> `status` 取值：`SENT`=已下发、`SUCCESS`=成功、`FAILED`=失败

---

## 10. 直播流 `/api/admin/media/stream`

录像回放已改为 HLS 方案，见第 12 章。本章仅提供实时直播接口，播放协议为 WHEP（WebRTC），延迟低于 500ms。

### 10.1 开启实时直播

`POST /api/admin/media/stream/realtime/start?deviceUid=A4C1380092CFA43E-C`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | query | string | 是 | 设备唯一标识 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "videoUrl": "http://47.121.24.61:1985/rtc/v1/whep/?app=live&stream=A4C1380092CFA43E-C&accessToken=5f8b2a9c4d1e4f6a8b3c7d2e9f0a1b4c"
  },
  "message": "success"
}
```

> `videoUrl` 为 WHEP（WebRTC）播放地址，带一次性 accessToken，有效期 60 分钟。前端使用浏览器原生 `RTCPeerConnection` 按 WHEP 协议拉流即可。

**错误码**：

| code | message | 说明 |
|---|---|---|
| 4002 | 设备不在线 | 设备离线，无法下发推流指令 |

### 10.2 停止实时直播

`POST /api/admin/media/stream/realtime/stop?deviceUid=A4C1380092CFA43E-C`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | query | string | 是 | 设备唯一标识 |

**响应示例**：
```json
{
  "code": 0,
  "data": null,
  "message": "success"
}
```

> 服务端向设备下发 `stopLiveStream` RPC 指令（携带 accessToken），并清理推流凭证。观众全部断开时服务端也会通过定时任务自动停流，无需手动调用。

---

## 11. 媒体库 `/api/admin/media`

**文件状态枚举（status）**：

| 值 | 含义 | 说明 |
|---|---|---|
| 0 | 初始状态 | 设备已生成文件，未上传 |
| 1 | 准备上传中 | 服务端已下发上传指令，等待设备完成 |
| 2 | 上传成功 | 设备回传上传成功 |
| 3 | 上传失败 | 设备回传上传失败 |

### 12.1 抓拍图片分页列表

`GET /api/admin/media/image/list`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | query | string | 是 | 设备唯一标识 |
| pageNum | query | int | 否 | 页码，默认 1 |
| pageSize | query | int | 否 | 每页大小，默认 10，最大 100 |
| fromTime | query | long | 否 | 起始时间戳（秒） |
| toTime | query | long | 否 | 结束时间戳（秒） |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "total": 156,
    "items": [
      {
        "id": "1",
        "deviceUid": "A4C1380092CFA43E-C",
        "tbDeviceId": "d1199610-...",
        "name": "snapshot_20260806_100000.jpg",
        "eventTime": 1722924000,
        "status": 1,
        "fileSize": 204800,
        "triggerType": "timer",
        "filePath": "/data/images/snapshot_20260806_100000.jpg",
        "thumbName": "thumb_snapshot_20260806_100000.jpg",
        "thumbPath": "/data/images/thumb_snapshot_20260806_100000.jpg",
        "thumbSize": 12800,
        "fileUrl": "https://dev-neurocast-api.joycrystal.com/api/file/download/images/A4C1380092CFA43E-C/snapshot_20260806_100000.jpg",
        "thumbUrl": "https://dev-neurocast-api.joycrystal.com/api/file/download/images/A4C1380092CFA43E-C/thumb_snapshot_20260806_100000.jpg",
        "createTime": "2026-08-06T10:00:00+08:00",
        "updateTime": "2026-08-06T10:00:05+08:00"
      }
    ]
  },
  "message": "success"
}
```

### 12.2 抓拍图片详情

`GET /api/admin/media/image/{id}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| id | path | string | 是 | 图片记录 ID |

**响应示例**：同 12.1 中的单个元素

### 12.3 录像分页列表

`GET /api/admin/media/video/list`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | query | string | 是 | 设备唯一标识 |
| pageNum | query | int | 否 | 页码，默认 1 |
| pageSize | query | int | 否 | 每页大小，默认 10，最大 100 |
| fromTime | query | long | 否 | 起始时间戳（秒） |
| toTime | query | long | 否 | 结束时间戳（秒） |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "total": 42,
    "items": [
      {
        "id": "1",
        "deviceUid": "A4C1380092CFA43E-C",
        "tbDeviceId": "d1199610-...",
        "name": "recording_20260806_100000.mp4",
        "eventTime": 1722924000,
        "status": 1,
        "fileSize": 10485760,
        "triggerType": "timer",
        "filePath": "/data/videos/recording_20260806_100000.mp4",
        "thumbName": "thumb_recording_20260806_100000.jpg",
        "thumbPath": "/data/videos/thumb_recording_20260806_100000.jpg",
        "thumbSize": 15360,
        "fileUrl": "https://dev-neurocast-api.joycrystal.com/api/file/download/videos/A4C1380092CFA43E-C/recording_20260806_100000.mp4",
        "thumbUrl": "https://dev-neurocast-api.joycrystal.com/api/file/download/images/A4C1380092CFA43E-C/thumb_recording_20260806_100000.jpg",
        "createTime": "2026-08-06T10:00:00+08:00",
        "updateTime": "2026-08-06T10:10:00+08:00"
      }
    ]
  },
  "message": "success"
}
```

### 12.4 录像详情

`GET /api/admin/media/video/{id}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| id | path | string | 是 | 录像记录 ID |

**响应示例**：同 10.3 中的单个元素

### 12.5 请求设备上传文件

`POST /api/admin/media/file/prepare`

**请求体**：
```json
{
  "fileType": "video",
  "deviceUid": "A4C1380092CFA43E-C",
  "filename": "recording_20260821_150000.mp4"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| fileType | string | 是 | 文件类型：image/video |
| deviceUid | string | 是 | 设备唯一标识 |
| filename | string | 是 | 文件名 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "id": "1",
    "name": "snapshot_20260806_100000.jpg",
    "fileSize": 204800,
    "status": 1,
    "eventTime": 1722924000000,
    "fileUrl": "http://.../api/file/download/image/A4C1380092CFA43E-C/snapshot_20260806_100000.jpg"
  },
  "message": "success"
}
```

> **处理逻辑**：根据文件当前状态决定行为：
> - `status=0`（初始）或 `status=3`（上传失败）：下发 RPC 指令触发设备上传，状态更新为 `1`（准备上传中）
> - `status=1`（准备上传中）：不重复下发，直接返回当前状态
> - `status=2`（上传成功）：直接返回下载链接

---

## 12. HLS 视频回放 `/api/admin/media/playback`

基于 HLS 协议的录像回放功能。采用**前端编排模式**：前端查询文件列表 → 逐个触发上传 → m3u8 动态累积已就绪分片 → 播放器无缝播放。

### 12.1 查询文件列表

`GET /api/admin/media/playback/files` — 需要 Bearer Token

查询时间段内的录像文件列表，包含每个文件的上传状态和 HLS 分片就绪状态。

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceUid | string | 是 | 设备唯一标识 |
| startTime | long | 是 | 起始时间（秒级时间戳） |
| endTime | long | 是 | 结束时间（秒级时间戳） |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "m3u8Url": "/api/admin/media/hls/A4C1380092CFA43E-C/1787561471-1787562081/m3u8",
    "files": [
      {
        "id": "abc123",
        "name": "20260824_180813.mp4",
        "startTime": 1787561501,
        "duration": 200,
        "status": "ready",
        "hlsStatus": "ready",
        "hlsReady": true
      },
      {
        "id": "def456",
        "name": "20260824_181133.mp4",
        "startTime": 1787561701,
        "duration": 180,
        "status": "pending",
        "hlsStatus": "pending",
        "hlsReady": false
      }
    ],
    "startTime": 1787561471,
    "duration": 610.0
  },
  "message": "success"
}
```

**响应字段说明**：

| 字段 | 类型 | 说明 |
|---|---|---|
| m3u8Url | string | HLS 播放列表地址 |
| files | array | 文件列表 |
| startTime | long | 播放开始时间（秒级时间戳），即用户请求的 startTime |
| duration | double | 实际可播放时长（秒），基于数据库记录计算，与文件上传/分片状态无关 |

**文件 status 枚举**（上传状态）：

| 值 | 说明 |
|---|---|
| pending | 未上传，等待触发 |
| uploading | 上传中 |
| ready | 上传成功 |
| failed | 上传失败 |

**hlsStatus 枚举**（HLS 分片状态）：

| 值 | 说明 |
|---|---|
| pending | 待处理，尚未开始分片 |
| processing | 分片中，FFmpeg 正在执行 |
| ready | 分片完成，TS + manifest + m3u8 均已生成 |
| failed | 分片失败，可重试 |

**hlsReady**：`true` 表示该文件的 HLS TS 分片已生成，可播放。

> **说明**：`duration` 表示在用户请求的时间范围内，数据库记录覆盖的实际时长。前端可据此告知用户"该时段最多可播放 X 秒"。m3u8 仅包含用户时间范围内的分片，且按连续就绪逻辑返回（遇到未就绪文件即停止，避免播放断层）。

### 12.2 触发文件上传

`POST /api/admin/media/playback/prepare` — 需要 Bearer Token

触发单个文件从设备上传。前端按文件列表顺序逐个调用，实现渐进式上传。

**请求体**：
```json
{
  "fileId": "abc123"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| fileId | string | 是 | 文件 ID（来自 12.1 的 id 字段） |

**响应示例**（分片已就绪，返回 m3u8Url）：
```json
{
  "code": 0,
  "data": {
    "id": "abc123",
    "name": "20260824_180813.mp4",
    "startTime": 1787561501,
    "duration": 200,
    "status": "ready",
    "hlsStatus": "ready",
    "hlsReady": true,
    "m3u8Url": "/api/admin/media/hls/A4C1380092CFA43E-C/1787561501-1787561701/m3u8"
  },
  "message": "success"
}
```

**响应示例**（上传中，无 m3u8Url）：
```json
{
  "code": 0,
  "data": {
    "id": "abc123",
    "name": "20260824_180813.mp4",
    "startTime": 1787561501,
    "duration": 200,
    "status": "uploading",
    "hlsStatus": "pending",
    "hlsReady": false,
    "m3u8Url": null
  },
  "message": "success"
}
```

**响应示例**（分片失败，可重试）：
```json
{
  "code": 0,
  "data": {
    "id": "abc123",
    "name": "20260824_180813.mp4",
    "startTime": 1787561501,
    "duration": 200,
    "status": "ready",
    "hlsStatus": "failed",
    "hlsReady": false,
    "m3u8Url": null
  },
  "message": "success"
}
```

> **四种场景**：
> 1. 已上传 + 分片就绪 → `hlsStatus="ready"`，`hlsReady=true`，返回 `m3u8Url`
> 2. 未上传 → 下发上传指令，`hlsStatus="pending"`，`hlsReady=false`
> 3. 已上传但未分片/分片中 → 触发分片生成，`hlsStatus="processing"`，`hlsReady=false`
> 4. 分片失败 → 重新触发分片，`hlsStatus` 重置为 `"pending"`，`hlsReady=false`

### 12.3 获取 m3u8 播放列表

`GET /api/admin/media/hls/{deviceUid}/{timeRange}/m3u8` — 无需认证

纯 HLS 标准端点，返回所有已就绪文件的 TS 分片累积列表。符合 HLS 协议标准，可直接交给 hls.js 等播放器。

**路径参数**：

| 参数 | 说明 |
|---|---|
| deviceUid | 设备唯一标识 |
| timeRange | 时间范围，格式：`{startTime}-{endTime}`（秒级时间戳） |

**响应**：

| 状态码 | Content-Type | 说明 |
|---|---|---|
| 200 | `application/vnd.apple.mpegurl` | m3u8 内容，包含所有已就绪分片 |
| 404 | — | 无就绪分片 |

> **动态增长**：随着前端 prepare 更多文件，同一 URL 返回的 m3u8 内容会逐渐增长。hls.js 定时刷新此 URL 自动获取新增分片，实现无缝播放。

### 12.4 获取 TS 分片

`GET /api/admin/media/hls/{deviceUid}/{timeRange}/{segmentName}` — 无需认证

获取 HLS TS 分片文件（二进制流）。此接口由 HLS 播放器自动调用，前端无需主动请求。

**路径参数**：

| 参数 | 说明 |
|---|---|
| deviceUid | 设备唯一标识 |
| timeRange | 时间范围，格式：`{startTime}-{endTime}` |
| segmentName | 分片文件名，如 `seg_0000.ts` |

**响应**：`200 OK`，Content-Type: `video/mp2t`

### 12.5 查询某日可播放记录

`GET /api/admin/media/playback/daily-records` — 需要 Bearer Token

查询指定日期内 HLS 分片已就绪的录像记录列表，用于前端展示当天可回放的时间段。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | query | string | 是 | 设备唯一标识 |
| date | query | string | 是 | 日期，格式 `yyyy-MM-dd` |

**请求示例**：

```
GET /api/admin/media/playback/daily-records?deviceUid=A4C1380092CFA43E-C&date=2026-08-25
```

**响应示例**：

```json
{
  "code": 0,
  "data": [
    {
      "id": "040d6ebbd177ad35abef952cebf5648a",
      "name": "20260825_145111.mp4",
      "startTime": 1787637071,
      "duration": 300,
      "hlsStatus": "ready"
    },
    {
      "id": "e2b5f36743d1f2722a599d56737cedee",
      "name": "20260825_145611.mp4",
      "startTime": 1787637371,
      "duration": 300,
      "hlsStatus": "ready"
    }
  ],
  "message": "success"
}
```

**响应字段说明**：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | string | 文件 ID，用于 prepare 接口触发上传 |
| name | string | 文件名 |
| startTime | long | 录像起始时间（秒级时间戳） |
| duration | int | 录像时长（秒） |
| hlsStatus | string | HLS 分片状态，固定返回 `ready` |

### 12.6 前端调用流程

```
1. GET /playback/files → 获取 { m3u8Url, files: [file1, file2, file3] }

2. 循环调用 POST /playback/prepare { fileId: file1.id }（3s 间隔）
   → 直到返回 hlsReady=true + m3u8Url

3. 将 m3u8Url 交给 hls.js 开始播放

4. 后台继续：循环调用 POST /playback/prepare { fileId: file2.id }（3s 间隔）
   → 直到返回 hlsReady=true

5. hls.js 自动刷新 m3u8Url → 获取到 file1+file2 的分片 → 无缝继续播放

6. 重复步骤 4-5 直到所有文件就绪
```

> **核心原则**：前端控制上传节奏，m3u8 端点纯 HLS 标准，播放器无感知地渐进播放。

---

## 13. 文件上传 `/api/admin/file/upload`

### 13.1 初始化分片上传

`POST /api/admin/file/upload/init`

**请求体**：
```json
{
  "filename": "video.mp4",
  "fileHash": "e3b0c44298fc1c149afbf4c8996fb924...",
  "totalSize": 104857600,
  "chunkSize": 5242880,
  "totalChunks": 20,
  "subDir": "video"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| filename | string | 是 | 文件名 |
| fileHash | string | 否 | 文件 MD5（用于秒传与合并校验） |
| totalSize | long | 是 | 文件总大小（字节） |
| chunkSize | long | 否 | 单个分片大小（字节） |
| totalChunks | int | 是 | 分片总数 |
| subDir | string | 否 | 存储子目录 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "uploadId": "uuid...",
    "instantComplete": false,
    "filePath": null,
    "uploadedChunks": [0, 1, 2]
  },
  "message": "success"
}
```

> `instantComplete=true` 表示秒传命中，`filePath` 为已存在文件的相对路径，无需再上传

### 13.2 上传分片

`POST /api/admin/file/upload/chunk` — `multipart/form-data`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| uploadId | query | string | 是 | 上传任务 ID |
| chunkIndex | query | int | 是 | 分片索引（从 0 开始） |
| file | form-data | file | 是 | 分片文件 |

**响应**：`{ "code": 0, "data": true, "message": "success" }`

### 13.3 合并分片

`POST /api/admin/file/upload/merge`

**请求体**：
```json
{
  "uploadId": "uuid...",
  "fileHash": "e3b0c44298fc1c149afbf4c8996fb924..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| uploadId | string | 是 | 上传任务 ID |
| fileHash | string | 否 | 文件 MD5（用于合并后校验） |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "filename": "video.mp4",
    "fileSize": 104857600,
    "filePath": "video/20260806/video.mp4",
    "fileHash": "e3b0c44298fc1c149afbf4c8996fb924..."
  },
  "message": "success"
}
```

### 13.4 批量上传

`POST /api/admin/file/upload/batch` — `multipart/form-data`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| files | form-data | file[] | 是 | 文件列表 |
| subDir | query | string | 否 | 存储子目录 |

**响应示例**：
```json
{
  "code": 0,
  "data": [
    {
      "filename": "photo1.jpg",
      "fileSize": 204800,
      "filePath": "images/photo1.jpg",
      "fileHash": "abc123..."
    },
    {
      "filename": "photo2.jpg",
      "fileSize": 307200,
      "filePath": "images/photo2.jpg",
      "fileHash": "def456..."
    }
  ],
  "message": "success"
}
```

### 13.5 查询上传进度

`GET /api/admin/file/upload/progress/{uploadId}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| uploadId | path | string | 是 | 上传任务 ID |

**响应示例**：
```json
{
  "code": 0,
  "data": [0, 1, 2, 5, 6],
  "message": "success"
}
```

> 返回已上传分片索引列表，可用于断点续传进度恢复

---

## 14. 文件下载 `/api/file/download`

`GET /api/file/download/{fileType}/{deviceUid}/{filename}?accessToken=eyJ0eXAiOiJKV1QiLCJhbGci...`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| fileType | path | string | 是 | 文件类型：image/video |
| deviceUid | path | string | 是 | 设备唯一标识 |
| filename | path | string | 是 | 文件名 |
| accessToken | query | string | 是 | 一次性下载令牌 |

> 支持 HTTP Range 头断点续传

---

## 15. OTA 升级 `/api/admin/ota`

### 15.1 分页查询 OTA 包

`GET /api/admin/ota/list/page?pageNum=1&pageSize=10`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| pageNum | query | int | 否 | 页码，默认 1 |
| pageSize | query | int | 否 | 每页条数，默认 10 |
| title | query | string | 否 | 标题关键词（模糊匹配） |
| otaType | query | string | 否 | OTA 类型：sw/fw 或 SOFTWARE/FIRMWARE |
| productId | query | string | 否 | 产品 ID |
| version | query | string | 否 | 版本号（精确匹配） |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "total": 2,
    "items": [
      {
        "id": "ota-package-id",
        "createTime": "2026-08-06T10:00:00+08:00",
        "title": "固件 v2.0",
        "version": "2.0.0",
        "otaType": "fw",
        "product": {
          "id": "03746350-914b-11f1-b4f6-932edadd00d3",
          "name": "AV100 高清摄像头",
          "model": "av100"
        },
        "tag": "latest",
        "description": "修复已知问题",
        "checksumAlgorithm": "SHA256",
        "checksum": "abc123...",
        "url": "http://..."
      }
    ]
  }
}
```

### 15.2 查询单个 OTA 包

`GET /api/admin/ota/{otaPackageId}`

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| otaPackageId | path | string | 是 | OTA 包 ID |

**响应示例**：同 13.1 中的单个 `items` 元素

### 15.3 创建 OTA 包

`POST /api/admin/ota`

**请求体**：
```json
{
  "title": "固件 v2.0",
  "productId": "03746350-914b-11f1-b4f6-932edadd00d3",
  "otaType": "fw",
  "version": "2.0.0",
  "url": "http://...",
  "checksumAlgorithm": "SHA256",
  "checksum": "e3b0c44298fc1c149afbf4c8996fb924...",
  "description": "修复已知问题"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| title | string | 是 | OTA 包标题 |
| productId | string | 是 | 产品 ID（根据产品的 TB Profile 关联设备类型） |
| otaType | string | 是 | OTA 类型：sw/fw 或 SOFTWARE/FIRMWARE |
| version | string | 是 | 版本号 |
| url | string | 是 | 固件/软件下载地址 |
| checksumAlgorithm | string | 否 | 校验和算法（SHA256/MD5 等） |
| checksum | string | 否 | 校验和 |
| description | string | 否 | 描述信息 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "id": "7319a5b0-...",
    "createTime": "2026-08-06T10:00:00+08:00",
    "title": "固件 v2.0",
    "version": "2.0.0",
    "otaType": "fw",
    "product": {
      "id": "03746350-914b-11f1-b4f6-932edadd00d3",
      "name": "AV100 高清摄像头",
      "model": "av100"
    },
    "description": "修复已知问题",
    "checksumAlgorithm": "SHA256",
    "checksum": "e3b0c44298fc1c149afbf4c8996fb924...",
    "url": "http://..."
  },
  "message": "success"
}
```

### 15.4 更新 OTA 包

`PUT /api/admin/ota`

**请求体**（仅支持修改描述）：
```json
{
  "id": "7319a5b0-...",
  "description": "更新后的描述信息"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | string | 是 | OTA 包 ID |
| description | string | 否 | 描述信息 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "id": "7319a5b0-...",
    "title": "固件 v2.0",
    "version": "2.0.0",
    "otaType": "fw",
    "product": {
      "id": "03746350-914b-11f1-b4f6-932edadd00d3",
      "name": "AV100 高清摄像头",
      "model": "av100"
    },
    "description": "更新后的描述信息",
    "url": "http://..."
  },
  "message": "success"
}
```

### 15.5 删除 OTA 包

`DELETE /api/admin/ota/{otaPackageId}`

### 15.6 单设备升级

`POST /api/admin/ota/upgrade`

**请求体**：
```json
{
  "tbDeviceId": "0a746350-914b-11f1-b4f6-932edadd00d3",
  "otaPackageId": "7319a5b0-..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| tbDeviceId | string | 是 | ThingsBoard 设备 ID |
| otaPackageId | string | 是 | OTA 包 ID |

> OTA 类型（FIRMWARE/SOFTWARE）从 OTA 包自身读取，无需传入

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "tbDeviceId": "0a746350-...",
    "deviceName": "1号相机",
    "currentVersion": "1.0.3",
    "targetVersion": "2.0.0",
    "otaType": "fw",
    "title": "固件 v2.0",
    "success": true,
    "errorMessage": null
  },
  "message": "success"
}
```

### 15.7 批量设备升级

`POST /api/admin/ota/upgrade/batch`

**请求体**：
```json
{
  "tbDeviceIds": ["0a746350-...", "1b847460-..."],
  "otaPackageId": "7319a5b0-..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| tbDeviceIds | string[] | 是 | ThingsBoard 设备 ID 列表 |
| otaPackageId | string | 是 | OTA 包 ID |

**响应示例**：
```json
{
  "code": 0,
  "data": [
    {
      "tbDeviceId": "0a746350-...",
      "deviceName": "1号相机",
      "currentVersion": "1.0.3",
      "targetVersion": "2.0.0",
      "otaType": "fw",
      "title": "固件 v2.0",
      "success": true,
      "errorMessage": null
    },
    {
      "tbDeviceId": "1b847460-...",
      "deviceName": "2号相机",
      "currentVersion": null,
      "targetVersion": "2.0.0",
      "otaType": "fw",
      "title": "固件 v2.0",
      "success": false,
      "errorMessage": "设备不在线"
    }
  ],
  "message": "success"
}
```

### 15.8 查询可用 OTA 版本

`GET /api/admin/ota/available`

查询指定产品下可用的 OTA 版本列表，可传入当前版本号过滤出更高版本。

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| otaType | string | 是 | OTA 类型：`fw`=固件，`sw`=软件 |
| productId | string | 是 | 产品 ID |
| version | string | 否 | 当前版本号，传入后只返回大于该版本的包 |

**请求示例**：
```
GET /api/admin/ota/available?otaType=fw&productId=03746350-914b-11f1-b4f6-932edadd00d3&version=1.0.0
```

**响应**：
```json
{
  "code": 0,
  "data": [
    {
      "id": "...",
      "version": "2.0.0",
      "title": "固件 v2.0",
      "otaType": "fw",
      "product": {
        "id": "03746350-914b-11f1-b4f6-932edadd00d3",
        "name": "AV100 高清摄像头",
        "model": "av100"
      },
      "url": "http://..."
    }
  ]
}
```

---

## 16. 外部事件统一接收（内部接口） `/api/event`

> 认证方式：X-API-KEY（需 `event:push` scope，供 ThingsBoard 规则引擎推送）

所有来自 ThingsBoard 的外部事件通过统一端点接收，根据 body 中的 `type` 字段分发到各模块异步处理。
接口接收后立即返回 200，实际处理在独立线程池中完成。

### 16.1 接收外部事件

`POST /api/event`

**请求体**（所有事件类型共用结构，通过 `type` 区分）：

#### 设备事件

**设备上线** `device.connected`：
```json
{
  "type": "device.connected",
  "tbDeviceId": "0a746350-914b-11f1-b4f6-932edadd00d3"
}
```

**设备下线** `device.disconnected`：
```json
{
  "type": "device.disconnected",
  "tbDeviceId": "0a746350-914b-11f1-b4f6-932edadd00d3"
}
```

#### 媒体事件

**抓拍图片上报** `media.image`：
```json
{
  "type": "media.image",
  "tbDeviceId": "0a746350-914b-11f1-b4f6-932edadd00d3",
  "fileName": "snapshot_20260806_100000.jpg",
  "eventTime": 1722924000,
  "triggerType": "timer",
  "filePath": "/mnt/data/snapshot_20260806_100000.jpg",
  "fileSize": 204800,
  "thumbName": "snapshot_20260806_100000_thumb.jpg",
  "thumbPath": "/mnt/data/snapshot_20260806_100000_thumb.jpg",
  "thumbSize": 15360
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| type | string | 是 | `media.image` |
| tbDeviceId | string | 是 | ThingsBoard 设备 ID |
| fileName | string | 否 | 文件名 |
| eventTime | long | 否 | 事件时间（秒级时间戳） |
| triggerType | string | 否 | 触发类型：timer / bluetooth / record |
| filePath | string | 否 | 设备端文件路径 |
| fileSize | long | 否 | 文件大小（字节） |
| thumbName | string | 否 | 缩略图文件名 |
| thumbPath | string | 否 | 缩略图设备端路径 |
| thumbSize | long | 否 | 缩略图文件大小（字节） |

**抓拍图片批量上报** `media.image.batch`：
```json
{
  "type": "media.image.batch",
  "tbDeviceId": "0a746350-914b-11f1-b4f6-932edadd00d3",
  "dataCameraImageList": [
    { "fileName": "snapshot_001.jpg", "eventTime": 1722924000, "triggerType": "timer", "filePath": "/mnt/...", "fileSize": 204800, "thumbName": "snapshot_001_thumb.jpg", "thumbPath": "/mnt/...", "thumbSize": 15360 },
    { "fileName": "snapshot_002.jpg", "eventTime": 1722924060, "triggerType": "timer", "filePath": "/mnt/...", "fileSize": 210000, "thumbName": "snapshot_002_thumb.jpg", "thumbPath": "/mnt/...", "thumbSize": 16000 }
  ]
}
```

**录像上报** `media.video`：
```json
{
  "type": "media.video",
  "tbDeviceId": "0a746350-914b-11f1-b4f6-932edadd00d3",
  "fileName": "recording_20260806_100000.mp4",
  "eventTime": 1722924000,
  "triggerType": "record",
  "filePath": "/mnt/data/recording_20260806_100000.mp4",
  "fileSize": 10485760,
  "thumbName": "recording_20260806_100000_thumb.jpg",
  "thumbPath": "/mnt/data/recording_20260806_100000_thumb.jpg",
  "thumbSize": 18432
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| type | string | 是 | `media.video` |
| tbDeviceId | string | 是 | ThingsBoard 设备 ID |
| fileName | string | 否 | 文件名 |
| eventTime | long | 否 | 事件时间（秒级时间戳） |
| triggerType | string | 否 | 触发类型：timer / bluetooth / record |
| filePath | string | 否 | 设备端文件路径 |
| fileSize | long | 否 | 文件大小（字节） |
| thumbName | string | 否 | 缩略图文件名 |
| thumbPath | string | 否 | 缩略图设备端路径 |
| thumbSize | long | 否 | 缩略图文件大小（字节） |

**文件上传结果通知** `media.file_upload_result`：
```json
{
  "type": "media.file_upload_result",
  "tbDeviceId": "0a746350-914b-11f1-b4f6-932edadd00d3",
  "fileId": "1",
  "fileType": "video",
  "fileName": "recording_20260821_150000.mp4",
  "ok": true
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| type | string | 是 | `media.file_upload_result` |
| tbDeviceId | string | 是 | ThingsBoard 设备 ID |
| fileId | string | 是 | 文件记录 ID（prepare 阶段返回） |
| fileType | string | 是 | 文件类型：image/video |
| fileName | string | 是 | 文件名 |
| ok | boolean | 是 | 上传是否成功 |

#### 事件类型汇总

| type | 说明 | 处理模块 |
|---|---|---|
| `device.connected` | 设备上线 | device |
| `device.disconnected` | 设备下线 | device |
| `media.image` | 抓拍图片上报（单条） | media |
| `media.image.batch` | 抓拍图片上报（批量） | media |
| `media.video` | 录像上报 | media |
| `media.file_upload_result` | 文件上传结果通知 | media |

**响应**：`{ "code": 0, "message": "success" }`（立即返回，异步处理）

**错误码**：

| code | message | 说明 |
|---|---|---|
| 1000 | type 不能为空 | 缺少 type 字段 |
| 2014 | API Key 无效 | X-API-KEY 无效或已禁用 |

**扩展新事件类型**：在 body 中增加新的 `type` 值及对应字段，对应模块新增 `@EventListener` 监听分支即可，URL 不变。

---

## 17. SRS 流媒体回调 `/api/srs/callback`

> 无需认证（SRS 服务器内部调用，匿名放行）

### 17.1 SRS 事件回调

`POST /api/srs/callback`

**请求体**（SRS 标准格式）：
```json
{
  "server_id": "...",
  "action": "on_publish",
  "client_id": "...",
  "ip": "192.168.1.100",
  "vhost": "__defaultVhost__",
  "app": "live",
  "tcUrl": "rtmp://...",
  "stream": "A4C1380092CFA43E-C",
  "param": "accessToken=xxx",
  "stream_url": "rtmp://.../live/A4C1380092CFA43E-C",
  "stream_id": "..."
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| server_id | string | SRS 服务器 ID |
| action | string | 事件类型：on_publish/on_play/on_stop |
| client_id | string | SRS 客户端 ID |
| ip | string | 客户端 IP |
| vhost | string | 虚拟主机 |
| app | string | 应用名 |
| tcUrl | string | RTMP tcUrl |
| stream | string | 流名称（实时流为 deviceUid，回放流为 playback_{deviceUid}） |
| param | string | 流 URL 参数串（如 accessToken=xxx） |
| stream_url | string | 完整流 URL |
| stream_id | string | SRS 流 ID |

**响应**：`{ "code": 0 }` 表示放行，非 0 拒绝

---

## 18. C 端会员认证 `/api/app/auth`

> 认证方式：JWT（`Authorization: Bearer <accessToken>`）

### 19.1 手机号登录

`POST /api/app/auth/login` — 无需认证

**请求体**：
```json
{
  "phone": "13800138000",
  "password": "Abc12345"
}
```

**响应**：
```json
{
  "code": 0,
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "eyJhbGciOi...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "userId": "uuid...",
    "phone": "13800138000",
    "nickname": "用户A"
  },
  "message": "success"
}
```

> C 端会员使用 JWT 令牌（无状态），与管理后台 Redis Token 不同。

### 19.2 注册

`POST /api/app/auth/register` — 无需认证

**请求体**：
```json
{
  "phone": "13800138000",
  "password": "Abc12345",
  "nickname": "用户A"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| phone | string | 是 | 手机号（全局唯一） |
| password | string | 是 | 密码（6-64 位） |
| nickname | string | 否 | 昵称 |

**响应**：同登录响应

---

## 19. 会员资料 `/api/app/user`

> 认证方式：JWT Bearer Token

### 20.1 获取当前用户信息

`GET /api/app/user/profile`

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "id": "uuid...",
    "phone": "13800138000",
    "nickname": "用户A",
    "avatar": "https://...",
    "status": 1,
    "createTime": "2026-08-01T10:00:00+08:00"
  },
  "message": "success"
}
```

### 20.2 更新个人资料

`PUT /api/app/user/profile`

**请求体**：
```json
{
  "nickname": "新昵称",
  "avatar": "https://..."
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| nickname | string | 否 | 昵称 |
| avatar | string | 否 | 头像 URL |

**响应**：`{ "code": 0, "message": "success" }`

---

## 20. 设备文件上传 `/api/device/file/upload`

> 认证方式：`X-API-KEY` 请求头（设备通过 ThingsBoard SHARED_SCOPE 获取 API Key）
>
> 支持两种模式：简单直传（小文件）与 S3/OSS 风格分片上传（大文件断点续传）

### 19.1 简单上传（小文件直传）

`POST /api/device/file/upload/simple`

单次请求完成文件上传，适用于抓拍图片等小文件。

**请求参数**（multipart/form-data）：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| deviceUid | query | string | 是 | 设备唯一标识 |
| filename | query | string | 是 | 文件名（如 snapshot_20260806_100000.jpg） |
| fileHash | query | string | 否 | 文件 MD5（用于秒传） |
| file | body | file | 是 | 文件内容 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "filename": "snapshot_20260806_100000.jpg",
    "fileSize": 204800,
    "filePath": "device/A4C1380092CFA43E-C/20260806/10/snapshot_20260806_100000.jpg",
    "fileHash": "d41d8cd98f00b204e9800998ecf8427e"
  },
  "message": "success"
}
```

### 19.2 初始化分片上传

`POST /api/device/file/upload/uploads`

对应 S3 `CreateMultipartUpload` / OSS `InitiateMultipartUpload`。支持秒传检测与断点续传进度恢复。

**请求体**：
```json
{
  "deviceUid": "A4C1380092CFA43E-C",
  "filename": "recording_20260806_100000.mp4",
  "fileSize": 10485760,
  "fileHash": "d41d8cd98f00b204e9800998ecf8427e",
  "totalParts": 3
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| deviceUid | string | 是 | 设备唯一标识 |
| filename | string | 是 | 文件名 |
| fileSize | long | 是 | 文件总大小（字节） |
| fileHash | string | 否 | 文件 MD5（用于秒传与合并校验） |
| totalParts | int | 是 | 分片总数 |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "uploadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "instantComplete": false,
    "uploadedParts": [0]
  },
  "message": "success"
}
```

> `instantComplete=true` 时表示秒传完成，无需后续分片上传；`uploadedParts` 非空表示断点续传，已上传的分片可跳过。

### 19.3 上传单个分片

`PUT /api/device/file/upload/uploads/{uploadId}/parts?partNumber=0`

对应 S3 `UploadPart` / OSS `UploadPart`。请求体为分片二进制数据。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| uploadId | path | string | 是 | 上传任务 ID |
| partNumber | query | int | 是 | 分片序号（从 0 开始） |

**请求体**：`application/octet-stream` 二进制数据

**响应**：`{ "code": 0, "data": null, "message": "success" }`

### 19.4 完成分片上传

`POST /api/device/file/upload/uploads/{uploadId}/complete`

对应 S3 `CompleteMultipartUpload` / OSS `CompleteMultipartUpload`。合并所有分片并校验 MD5。

**请求体**：
```json
{
  "fileHash": "d41d8cd98f00b204e9800998ecf8427e"
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| fileHash | string | 否 | 文件 MD5（合并后校验） |

**响应示例**：
```json
{
  "code": 0,
  "data": {
    "filename": "recording_20260806_100000.mp4",
    "fileSize": 10485760,
    "filePath": "device/A4C1380092CFA43E-C/20260806/10/recording_20260806_100000.mp4",
    "fileHash": "d41d8cd98f00b204e9800998ecf8427e"
  },
  "message": "success"
}
```

### 19.5 中止分片上传

`DELETE /api/device/file/upload/uploads/{uploadId}`

对应 S3 `AbortMultipartUpload` / OSS `AbortMultipartUpload`。清理临时分片文件与 Redis 任务记录。

**响应**：`{ "code": 0, "data": null, "message": "success" }`

---

## 20. 系统配置 `/api/admin/system/config` — 需要 ADMIN 角色

服务端自身的 KV 配置管理（与设备全局配置同构，但不下发设备，仅供后端读取）。支持前端动态新增/删除配置项。

### 20.1 查询系统配置

`GET /api/admin/system/config`

返回全部系统配置。仅返回已配置的配置项；表为空时返回空列表。

**响应示例**：

```json
{
  "code": 0,
  "data": [
    { "key": "media_retention_days", "value": "30", "description": "媒体文件保留天数" },
    { "key": "ntp_server", "value": "ntp.aliyun.com", "description": "服务端校时服务器" }
  ],
  "message": "success"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| key | string | 配置键 |
| value | string | 配置值 |
| description | string | 配置描述（可能为 null） |

### 20.2 新增或更新系统配置

`PUT /api/admin/system/config`

新增或更新系统配置，支持批量提交，仅传需要新增或变更的配置项。

**请求体**：
```json
{
  "items": [
    { "key": "media_retention_days", "value": "30", "description": "媒体文件保留天数" },
    { "key": "ntp_server", "value": "ntp.aliyun.com", "description": "服务端校时服务器" }
  ]
}
```

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| items | array | 是 | 配置项列表，不能为空 |
| items[].key | string | 是 | 配置键（snake_case 格式：小写字母开头，仅允许小写字母/数字/下划线，最长 64 位） |
| items[].value | string | 是 | 配置值（最长 512 位） |
| items[].description | string | 否 | 配置描述（最长 256 位，直接以传入值保存，不传则为空） |

**响应**：`{ "code": 0, "message": "success" }`

**错误码**：

| code | message | 说明 |
|---|---|---|
| 1000 | 参数校验失败 | items 为空，或 key 格式不符合 snake_case、超长、为空 |

> 与当前生效值相同的项不会重复写库

### 20.3 删除系统配置

`DELETE /api/admin/system/config`

删除指定系统配置项。

**请求参数**：

| 参数 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| key | query | string | 是 | 配置键，如 media_retention_days |

**响应**：`{ "code": 0, "message": "success" }`

**错误码**：

| code | message | 说明 |
|---|---|---|
| 4009 | 系统配置项不存在 | key 对应的配置项未创建 |

---

## 附录：错误码总表

所有接口错误统一返回 `{ "code": <错误码>, "data": null, "message": "<描述>" }` 格式。前端可根据 `code` 做精确分支处理。

### 通用码

| code | 常量名 | 说明 |
|---|---|---|
| 0 | `SUCCESS` | 成功 |
| 1 | `FAILED` | 通用失败（仅框架内部兆底，业务接口不会返回） |
| 1000 | `VALIDATE_FAILED` | 参数校验失败 |
| 1002 | `PARAMETER_FORMAT_ERROR` | 参数格式错误 |
| 2000 | `ERROR` | 系统异常 |
| 2001 | `TIMEOUT` | 请求超时 |
| 2003 | `HTTP_REQUEST_METHOD_NOT_SUPPORTED` | 请求方式不支持 |
| 2004 | `FORBIDDEN` | 无访问权限 |
| 2005 | `UNAUTHORIZED` | 未认证或认证已过期 |
| 2006 | `NO_PERMISSION_ACCESS` | 无权访问 |
| 2007 | `ACCESS_TOKEN_INVALID` | 访问令牌无效 |
| 2008 | `REFRESH_TOKEN_INVALID` | 刷新令牌失效 |
| 2009 | `REFRESH_TOKEN_INCORRECT` | 刷新令牌错误 |
| 2010 | `API_INTERFACE_LIMIT` | 接口限流 |
| 2011 | `DUPLICATE_KEY` | 数据已存在（数据库唯一约束冲突） |
| 2012 | `LOGIN_USERNAME_PASSWORD_ERROR` | 用户名或密码错误 |
| 2013 | `LOGIN_USER_DISABLED` | 用户已被禁用 |
| 2014 | `API_KEY_INVALID` | API Key 无效 |

### ThingsBoard 集成（3000-3999）

| code | 常量名 | 说明 |
|---|---|---|
| 3000 | `BUSINESS_THINGSBOARD_ERROR` | ThingsBoard 调用失败 |
| 3001 | `BUSINESS_THINGSBOARD_DEVICE_EXISTED` | ThingsBoard 设备已存在 |
| 3002 | `BUSINESS_THINGSBOARD_DEVICE_NOT_EXISTED` | ThingsBoard 设备不存在 |

### 设备与产品管理（4000-4999）

| code | 常量名 | 说明 |
|---|---|---|
| 4000 | `BUSINESS_DEVICE_NOT_EXISTED` | 设备不存在 |
| 4001 | `BUSINESS_DEVICE_EXISTED` | 设备已存在 |
| 4002 | `BUSINESS_DEVICE_NOT_ONLINE` | 设备不在线 |
| 4003 | `BUSINESS_DEVICE_NO_AVAILABLE_PORT` | 没有可用的端口 |
| 4004 | `BUSINESS_PRODUCT_NOT_EXISTED` | 产品不存在 |
| 4005 | `BUSINESS_PRODUCT_HAS_DEVICES` | 产品下还有设备，无法删除 |
| 4006 | `BUSINESS_PRODUCT_NO_TB_PROFILE` | 产品无 ThingsBoard Profile |
| 4008 | `BUSINESS_DEVICE_GLOBAL_CONFIG_NOT_EXISTED` | 设备全局配置项不存在 |
| 4009 | `BUSINESS_SYS_CONFIG_NOT_EXISTED` | 系统配置项不存在 |

### 媒体文件与流（5000-5999）

| code | 常量名 | 说明 |
|---|---|---|
| 5000 | `BUSINESS_FILE_NOT_EXISTED` | 文件不存在 |
| 5001 | `BUSINESS_FILE_UPLOAD_NOT_EMPTY` | 文件内容不能为空 |
| 5002 | `BUSINESS_FILE_UPLOAD_TOO_LARGE` | 文件大小超出限制 |
| 5003 | `BUSINESS_FILE_UPLOAD_TASK_EXPIRED` | 上传任务不存在或已过期 |
| 5004 | `BUSINESS_FILE_CHUNK_INCOMPLETE` | 分片未上传完整 |
| 5005 | `BUSINESS_FILE_HASH_MISMATCH` | 文件校验失败，MD5 不匹配 |
| 5006 | `BUSINESS_STREAM_NOT_STARTED` | 流未开启 |
| 5007 | `BUSINESS_SRS_ERROR` | SRS 流媒体服务调用失败 |

### OTA 升级（6000-6999）

| code | 常量名 | 说明 |
|---|---|---|
| 6000 | `BUSINESS_OTA_PACKAGE_NOT_EXISTED` | OTA 包不存在 |
| 6001 | `BUSINESS_OTA_VERSION_NOT_GREATER` | 目标版本必须大于当前版本 |
| 6002 | `BUSINESS_OTA_PACKAGE_EXISTED` | OTA 包已存在 |
| 6003 | `BUSINESS_OTA_TB_SYNC_FAILED` | ThingsBoard OTA 包同步失败 |

### 用户管理（7000-7999）

| code | 常量名 | 说明 |
|---|---|---|
| 7000 | `BUSINESS_USER_NOT_EXISTED` | 用户不存在 |
| 7001 | `BUSINESS_USER_EXISTED` | 用户名已存在 |
| 7002 | `BUSINESS_ROLE_NOT_EXISTED` | 角色不存在 |
| 7003 | `BUSINESS_ROLE_CODE_EXISTED` | 角色编码已存在 |
| 7004 | `BUSINESS_ROLE_IN_USE` | 角色正在被用户使用，无法删除 |

### API 客户端管理（8000-8999）

| code | 常量名 | 说明 |
|---|---|---|
| 8000 | `BUSINESS_API_CLIENT_NOT_EXISTED` | API 客户端不存在 |

### 权限管理（9000-9999）

| code | 常量名 | 说明 |
|---|---|---|
| 9000 | `BUSINESS_PERMISSION_NOT_EXISTED` | 权限不存在 |
| 9001 | `BUSINESS_PERMISSION_CODE_EXISTED` | 权限标识已存在 |
| 9002 | `BUSINESS_PERMISSION_IN_USE` | 权限正在被角色使用，无法删除 |
