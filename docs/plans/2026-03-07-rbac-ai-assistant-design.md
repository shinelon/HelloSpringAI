# 角色权限查询AI小助手 - 设计文档

**日期**: 2026-03-07  
**版本**: 1.0  
**作者**: AI Assistant

---

## 1. 项目概述

### 1.1 目标
开发一个基于Spring AI的角色权限查询AI小助手，通过Function Calling技术，让用户可以通过自然语言交互完成以下操作：
- 查询用户信息及其角色、权限
- 查询角色拥有的权限
- 查询拥有特定权限的角色
- 提交角色或权限变更的审批申请

### 1.2 核心特性
- ✅ AI开场自动询问用户需要的服务
- ✅ 4个独立的Tool，通过Function Calling自动调用
- ✅ 内存Map存储模拟数据
- ✅ 支持同步和流式响应
- ✅ 友好的自然语言交互

---

## 2. 架构设计

### 2.1 整体架构图

```
┌─────────────────┐
│   Controller    │  /rbac/chat, /rbac/chat/stream
└────────┬────────┘
         │
┌────────▼────────┐
│    Service      │  RbacService
└────────┬────────┘
         │
┌────────▼────────┐
│    Manager      │  RbacChatManager (管理AI对话和工具调用)
└────────┬────────┘
         │
    ┌────▼────┬─────────┬─────────┬──────────┐
    │         │         │         │          │
┌───▼───┐ ┌──▼──┐ ┌────▼───┐ ┌───▼───┐ ┌────▼────┐
│ Tool1 │ │Tool2│ │ Tool3  │ │ Tool4 │ │DataService│
│用户查询│ │角色查│ │ 权限查 │ │审批提交│ │(模拟数据) │
└───────┘ │询权限│ │ 询角色 │ └───────┘ └──────────┘
          └─────┘ └────────┘
```

### 2.2 技术栈
- **框架**: Spring Boot 3.4.3, Spring AI 1.1.2
- **AI模型**: ZhipuAI (glm-4-flash)
- **数据存储**: 内存Map
- **工具**: Lombok, Validation
- **响应格式**: 统一Result包装 + SSE流式

---

## 3. 数据模型设计

### 3.1 核心实体

#### UserDO (用户实体)
```java
- userId: Long              // 用户ID
- name: String              // 姓名
- phone: String             // 手机号
- email: String             // 邮箱
- company: String           // 公司
- roleIds: List<Long>       // 角色ID列表
```

#### RoleDO (角色实体)
```java
- roleId: Long              // 角色ID
- roleName: String          // 角色名称
- roleCode: String          // 角色编码
- description: String       // 角色描述
- permissionIds: List<Long> // 权限ID列表
```

#### PermissionDO (权限实体)
```java
- permissionId: Long        // 权限ID
- permissionName: String    // 权限名称
- permissionCode: String    // 权限编码
- resource: String          // 资源路径
- action: String            // 操作类型 (READ/WRITE/DELETE)
```

### 3.2 模拟数据

#### 用户数据 (5条)
1. 张三 - 13800138000 - 系统管理员
2. 李四 - 13900139000 - 普通用户
3. 王五 - 13700137000 - 审计员
4. 赵六 - 13600136000 - 普通用户
5. 孙七 - 13500135000 - 审计员

#### 角色数据 (3个)
1. 系统管理员 (ADMIN) - 拥有所有权限
2. 普通用户 (USER) - 拥有基础权限
3. 审计员 (AUDITOR) - 拥有查看权限

#### 权限数据 (10个)
1. 用户查看 (USER_READ)
2. 用户新增 (USER_WRITE)
3. 用户删除 (USER_DELETE)
4. 角色查看 (ROLE_READ)
5. 角色管理 (ROLE_WRITE)
6. 权限查看 (PERMISSION_READ)
7. 权限管理 (PERMISSION_WRITE)
8. 审批查看 (APPROVAL_READ)
9. 审批处理 (APPROVAL_WRITE)
10. 日志查看 (LOG_READ)

---

## 4. 四个Tool设计

### 4.1 Tool1: UserQueryTool (用户查询工具)

**功能**: 根据手机号查询用户信息、角色信息和权限信息

**方法签名**:
```java
@Tool(description = "根据手机号查询用户信息、角色信息和权限信息")
public UserInfoVO queryUserByPhone(
    @ToolParam(description = "手机号，11位数字") String phone)
```

**返回示例**:
```json
{
  "userId": 1,
  "name": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "company": "XX科技",
  "roles": [
    {
      "roleId": 1,
      "roleName": "系统管理员",
      "roleCode": "ADMIN",
      "permissions": [
        {"permissionId": 1, "permissionName": "用户查看", "code": "USER_READ"},
        {"permissionId": 2, "permissionName": "用户新增", "code": "USER_WRITE"}
      ]
    }
  ]
}
```

### 4.2 Tool2: RolePermissionQueryTool (角色权限查询工具)

**功能**: 根据角色信息查询该角色下的所有权限

**方法签名**:
```java
@Tool(description = "根据角色名称或角色编码查询该角色下的所有权限信息")
public List<PermissionVO> queryPermissionsByRole(
    @ToolParam(description = "角色名称或角色编码，如：系统管理员、ADMIN") String roleIdentifier)
```

**返回示例**:
```json
[
  {
    "permissionId": 1,
    "permissionName": "用户查看",
    "permissionCode": "USER_READ",
    "resource": "/api/users",
    "action": "READ"
  }
]
```

### 4.3 Tool3: PermissionRoleQueryTool (权限角色查询工具)

**功能**: 根据权限信息查询拥有该权限的所有角色

**方法签名**:
```java
@Tool(description = "根据权限名称或权限编码查询拥有该权限的所有角色")
public List<RoleVO> queryRolesByPermission(
    @ToolParam(description = "权限名称或权限编码，如：用户查看、USER_READ") String permissionIdentifier)
```

**返回示例**:
```json
[
  {
    "roleId": 1,
    "roleName": "系统管理员",
    "roleCode": "ADMIN",
    "description": "系统管理员，拥有所有权限"
  }
]
```

### 4.4 Tool4: ApprovalSubmitTool (审批提交工具)

**功能**: 提交角色或权限变更的审批请求

**方法签名**:
```java
@Tool(description = "提交权限或角色变更的审批请求")
public ApprovalVO submitApproval(
    @ToolParam(description = "申请人手机号") String phone,
    @ToolParam(description = "申请人姓名") String name,
    @ToolParam(description = "申请人邮箱") String email,
    @ToolParam(description = "所属公司") String company,
    @ToolParam(description = "申请的角色，如：系统管理员、普通用户") String roleName,
    @ToolParam(description = "申请原因") String reason)
```

**返回示例**:
```json
{
  "approvalId": "AP202603070001",
  "status": "PENDING",
  "message": "审批申请已提交成功",
  "applicant": "张三",
  "appliedRole": "系统管理员",
  "submitTime": "2026-03-07 10:30:00"
}
```

---

## 5. AI开场询问设计

### 5.1 System Prompt

```
你是角色权限查询AI小助手，专门帮助用户查询用户信息、角色信息、权限信息，以及提交审批申请。

## 你的能力：
1. 根据手机号查询用户及其角色、权限信息
2. 根据角色查询该角色拥有的所有权限
3. 根据权限查询哪些角色拥有该权限
4. 帮助用户提交角色或权限的审批申请

## 交互规则：
- 当用户发送空消息、问候语（如"你好"、"hi"、"hello"）或询问你能做什么时，主动介绍你的4个功能并询问用户需要什么服务
- 根据用户的描述，自动调用相应的工具获取信息
- 用自然、友好的语言回复用户，避免生硬的机器回复

## 功能介绍模板：
您好！我是角色权限查询AI小助手，我可以为您提供以下服务：

1. 📱 查询用户信息 - 根据手机号查询用户的详细信息和权限
2. 🔍 查询角色权限 - 查询某个角色拥有的所有权限
3. 🔎 查询权限角色 - 查询拥有某个权限的所有角色  
4. 📝 提交审批申请 - 提交角色或权限的变更申请

请问您需要什么服务？
```

### 5.2 触发逻辑

在 `RbacServiceImpl` 中判断用户输入是否为问候语：
- 空消息
- "你好"、"hi"、"hello"、"嗨"、"您好"、"在吗"等问候语
- "help"、"帮助"、"你能做什么"等询问语

匹配时返回固定的开场白，否则走正常的AI对话流程。

---

## 6. 包结构设计

```
com.shinelon.hello/
├── common/
│   ├── constants/
│   │   └── RbacConstants.java          # RBAC相关常量
│   └── enums/
│       └── ApprovalStatusEnum.java      # 审批状态枚举
├── controller/
│   └── RbacController.java              # RBAC AI助手控制器
├── service/
│   ├── RbacService.java                 # 服务接口
│   └── impl/
│       └── RbacServiceImpl.java         # 服务实现
├── manager/
│   └── RbacChatManager.java             # RBAC对话管理器
├── tool/
│   ├── UserQueryTool.java               # Tool1: 用户查询
│   ├── RolePermissionQueryTool.java     # Tool2: 角色权限查询
│   ├── PermissionRoleQueryTool.java     # Tool3: 权限角色查询
│   └── ApprovalSubmitTool.java          # Tool4: 审批提交
├── data/
│   └── RbacMockDataService.java         # 模拟数据服务
└── model/
    ├── entity/
    │   ├── UserDO.java                  # 用户实体
    │   ├── RoleDO.java                  # 角色实体
    │   └── PermissionDO.java            # 权限实体
    ├── vo/
    │   ├── RbacChatVO.java              # AI对话响应
    │   ├── UserInfoVO.java              # 用户信息VO
    │   ├── RoleVO.java                  # 角色VO
    │   ├── PermissionVO.java            # 权限VO
    │   └── ApprovalVO.java              # 审批VO
    └── dto/
        └── RbacChatRequestDTO.java      # 对话请求DTO
```

---

## 7. API接口设计

### 7.1 同步对话接口

**URL**: `POST /rbac/chat`

**请求**:
```json
{
  "content": "查询手机号13800138000的用户信息"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": "根据查询结果，手机号为13800138000的用户是张三...",
    "createTime": "2026-03-07T10:30:00"
  }
}
```

### 7.2 流式对话接口

**URL**: `POST /rbac/chat/stream`

**请求**: 同上

**响应**: SSE流式事件

---

## 8. 实现要点

### 8.1 代码规范
- 遵循项目现有的命名规范（PascalCase类名，camelCase方法名）
- 使用Lombok简化代码（@Slf4j, @RequiredArgsConstructor, @Builder等）
- 构造器注入，禁止字段注入
- 完善的日志记录（INFO记录关键点，DEBUG记录详细信息）
- 统一的异常处理（使用BusinessException）

### 8.2 测试策略
- 单元测试：每个Tool的独立测试
- 集成测试：Service和Controller的测试
- 使用@SpringBootTest和@Transactional
- 采用表驱动测试方式

### 8.3 性能考虑
- 模拟数据在应用启动时初始化一次
- 使用Map存储，O(1)查询复杂度
- 避免重复创建对象

---

## 9. 风险和限制

### 9.1 已知限制
- 模拟数据存储在内存中，应用重启后数据丢失
- 审批流程为模拟，不涉及真实的审批系统
- Tool调用依赖AI的意图理解能力

### 9.2 潜在风险
- AI可能误解用户意图，调用错误的Tool
- 手机号等敏感信息需要脱敏处理
- 流式响应可能出现网络中断

---

## 10. 后续优化方向

1. **数据持久化**: 将模拟数据迁移到H2数据库
2. **审批流程**: 实现完整的审批状态流转
3. **权限控制**: 添加API访问权限控制
4. **缓存优化**: 对频繁查询的数据添加缓存
5. **监控告警**: 添加Tool调用成功率和耗时监控

---

**设计完成时间**: 2026-03-07  
**预计开发时间**: 2-3小时  
**预计测试时间**: 1小时
