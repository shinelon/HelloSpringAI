# HelloSpringAI 项目规格文档

**版本**: 1.0.0
**最后更新**: 2026-02-21
**状态**: 草稿

---

## 1. 项目概述

### 1.1 项目背景

HelloSpringAI 是一个学习型项目，旨在通过 Spring AI 框架集成智谱AI大语言模型，实现一个具备多轮对话能力的 AI 对话服务。

### 1.2 项目目标

- 学习 Spring AI 框架的使用方法
- 掌握智谱AI API 的集成方式
- 实现流式响应 (SSE) 的对话体验
- 练习 TDD 开发流程和规范编码

### 1.3 核心功能

1. **单轮/多轮对话**：支持上下文连续对话
2. **流式响应**：通过 SSE 实时返回生成内容
3. **会话管理**：创建、查询、删除会话
4. **历史记录**：持久化存储对话历史

---

## 2. 技术架构

### 2.1 技术选型

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.x |
| AI框架 | Spring AI | 1.1.2 |
| 模型SDK | spring-ai-starter-model-zhipuai | 1.1.2 |
| 数据库 | H2 (内存模式) | 2.x |
| ORM | Spring Data JPA | 3.x |
| 构建工具 | Maven | 3.6.3 |

### 2.2 项目结构

```
HelloSpringAI/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/shinelon/hello/
│   │   │   ├── HelloSpringAiApplication.java    # 启动类
│   │   │   ├── controller/                      # Web层
│   │   │   │   └── ChatController.java
│   │   │   ├── service/                         # Service层
│   │   │   │   ├── ChatService.java
│   │   │   │   └── impl/
│   │   │   │       └── ChatServiceImpl.java
│   │   │   ├── manager/                         # Manager层
│   │   │   │   └── ZhipuAiManager.java
│   │   │   ├── dao/                             # DAO层
│   │   │   │   ├── ChatSessionDao.java
│   │   │   │   └── ChatMessageDao.java
│   │   │   ├── model/                           # 领域模型
│   │   │   │   ├── entity/
│   │   │   │   │   ├── ChatSessionDO.java
│   │   │   │   │   └── ChatMessageDO.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ChatRequestDTO.java
│   │   │   │   │   └── ChatResponseDTO.java
│   │   │   │   └── vo/
│   │   │   │       ├── SessionVO.java
│   │   │   │       └── MessageVO.java
│   │   │   ├── config/                          # 配置类
│   │   │   │   └── ZhipuAiConfig.java
│   │   │   └── common/                          # 公共组件
│   │   │       ├── exception/
│   │   │       │   ├── BusinessException.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       └── enums/
│   │   │           └── ErrorCodeEnum.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── schema.sql                       # H2建表脚本
│   └── test/
│       └── java/com/shinelon/hello/
│           └── service/
│               └── ChatServiceTest.java
└── .specs/
    └── spec.md
```

### 2.3 分层职责

| 层级 | 组件 | 职责 |
|------|------|------|
| Web层 | ChatController | HTTP请求处理、参数校验、响应封装 |
| Service层 | ChatService | 业务逻辑、会话管理、消息组装 |
| Manager层 | ZhipuAiManager | 智谱AI调用封装、流式响应处理 |
| DAO层 | ChatSessionDao/ChatMessageDao | 数据持久化操作 |

---

## 3. 功能需求

### 3.1 对话接口

**描述**：发送用户消息，获取AI回复（同步模式）

**功能要求**：
- 接收用户输入的消息
- 支持指定会话ID（可选，不指定则创建新会话）
- 自动维护对话上下文
- 返回AI的完整回复

### 3.2 流式响应

**描述**：发送用户消息，通过 SSE 实时返回生成内容

**功能要求**：
- 使用 Server-Sent Events (SSE) 技术
- 逐token/chunk返回生成内容
- 支持中断生成
- 返回完成后保存完整消息到数据库

### 3.3 会话管理

**描述**：管理用户的对话会话

**功能要求**：
- 创建新会话
- 查询会话列表
- 查询会话详情（含历史消息）
- 删除会话

### 3.4 历史记录

**描述**：持久化存储对话历史

**功能要求**：
- 每条消息记录角色（user/assistant）
- 记录消息内容和时间戳
- 支持按会话查询历史消息
- 会话删除时级联删除消息

---

## 4. 接口设计

### 4.1 对话接口（同步）

**请求**
```
POST /ai/chat
Content-Type: application/json

{
    "sessionId": "string (可选)",
    "content": "string (必填)"
}
```

**响应**
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "sessionId": "uuid",
        "role": "assistant",
        "content": "AI回复的完整内容",
        "createTime": "2026-02-21 10:30:00"
    }
}
```

### 4.2 流式对话接口

**请求**
```
POST /ai/chat/stream
Content-Type: application/json
Accept: text/event-stream

{
    "sessionId": "string (可选)",
    "content": "string (必填)"
}
```

**响应** (SSE)
```
data: {"content": "你", "done": false}

data: {"content": "好", "done": false}

data: {"content": "！", "done": true, "sessionId": "uuid"}
```

### 4.3 会话接口

**创建会话**
```
POST /ai/sessions
Response: { "code": 200, "data": { "sessionId": "uuid" } }
```

**获取会话列表**
```
GET /ai/sessions?page=1&size=10
Response: { "code": 200, "data": { "list": [...], "total": 100 } }
```

**获取会话详情（含历史消息）**
```
GET /ai/sessions/{sessionId}
Response: { "code": 200, "data": { "sessionId": "...", "messages": [...] } }
```

**删除会话**
```
DELETE /ai/sessions/{sessionId}
Response: { "code": 200, "message": "删除成功" }
```

### 4.4 统一响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

**错误码定义**

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 503 | AI服务不可用 |

---

## 5. 数据模型

### 5.1 ChatSession (会话表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| session_id | VARCHAR(36) | 会话UUID，唯一索引 |
| title | VARCHAR(100) | 会话标题（首条消息摘要） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 最后更新时间 |

### 5.2 ChatMessage (消息表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| session_id | VARCHAR(36) | 关联会话ID，外键 |
| role | VARCHAR(20) | 角色：user/assistant |
| content | TEXT | 消息内容 |
| create_time | DATETIME | 创建时间 |

**关系**：一个 Session 包含多条 Message (1:N)

---

## 6. 非功能需求

### 6.1 异常处理

- 所有异常通过 `GlobalExceptionHandler` 统一处理
- 业务异常使用 `BusinessException`
- 错误响应格式统一，包含错误码和友好提示
- DAO层异常转换为Service层业务异常

### 6.2 日志规范

- 使用 SLF4J + Logback
- Service层记录关键业务日志
- 异常日志包含完整堆栈信息
- 禁止使用 `System.out.println()`

### 6.3 安全要求

- API Key 从环境变量读取，禁止硬编码
- 输入参数做基本校验（非空、长度限制）
- 防止 XSS：用户输入不做HTML渲染
- 错误信息不暴露系统内部细节

---

## 7. 测试要求

### 7.1 测试原则

- 遵循 TDD（测试驱动开发）
- 优先编写表格驱动测试
- 优先集成测试，减少 Mock

### 7.2 测试覆盖

| 测试类型 | 覆盖范围 |
|---------|---------|
| 单元测试 | Service层业务逻辑 |
| 集成测试 | Controller层API调用 |
| 数据库测试 | DAO层CRUD操作（使用H2） |

### 7.3 测试用例示例

**ChatService 测试用例**

| 场景 | 输入 | 预期结果 |
|------|------|---------|
| 新会话对话 | content="你好", sessionId=null | 创建新会话，返回AI回复 |
| 继续对话 | content="继续", sessionId=有效ID | 追加到已有会话，返回AI回复 |
| 无效会话 | sessionId=不存在的ID | 抛出 BusinessException |
| 空消息 | content="" 或 null | 参数校验失败 |

---

## 8. 附录

### 8.1 环境配置

```yaml
# application.yml
server:
  servlet:
    context-path: /ai

spring:
  datasource:
    url: jdbc:h2:mem:chatdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: none
  h2:
    console:
      enabled: true

spring-ai:
  zhipuai:
    api-key: ${ZHIPUAI_API_KEY}
    chat:
      options:
        model: glm-4-flash
        temperature: 0.7
```

### 8.2 参考资料

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [智谱AI API 文档](https://open.bigmodel.cn/dev/api)
- [阿里巴巴Java开发手册（嵩山版）]
