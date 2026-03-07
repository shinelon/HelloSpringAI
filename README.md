# HelloSpringAI

Spring Boot + Spring AI 智谱AI集成示例项目。

## 技术栈

- Java 17
- Spring Boot 3.4.3
- Spring AI 1.1.2
- 智谱AI (zhipuai)
- H2 Database

## 功能特性

- 同步/流式对话
- 会话管理
- Chat Memory (对话记忆)
- Tool Calling (工具调用)
  - 日期时间工具
  - 计算器工具
- RAG 检索增强生成
  - 简单版
  - 进阶版 (RetrievalAugmentationAdvisor + 查询改写)
- RBAC AI 助手 (基于 Function Calling)
  - 用户信息查询
  - 角色权限查询
  - 权限角色查询
  - 审批申请提交

## 快速开始

### 1. 配置环境变量

```bash
# Windows
set ZHIPUAI_API_KEY=your_api_key

# Linux/Mac
export ZHIPUAI_API_KEY=your_api_key
```

### 2. 启动应用

```bash
./mvnw spring-boot:run
```

应用启动后访问: http://localhost:8080/ai

## API 接口

### 基础对话

#### 同步对话

```bash
POST /ai/chat
Content-Type: application/json

{
  "sessionId": "session-001",
  "content": "你好"
}
```

#### 流式对话 (SSE)

```bash
POST /ai/chat/stream
Content-Type: application/json

{
  "sessionId": "session-001",
  "content": "讲个故事"
}
```

### 会话管理

```bash
# 创建会话
POST /ai/sessions

# 获取会话列表
GET /ai/sessions?page=1&size=10

# 获取会话详情
GET /ai/sessions/{sessionId}

# 删除会话
DELETE /ai/sessions/{sessionId}
```

### Chat Memory (带记忆对话)

```bash
# 同步对话
POST /ai/learn/memory/chat
Content-Type: application/json

{
  "conversationId": "conv-001",
  "content": "我叫张三"
}

# 流式对话
POST /ai/learn/memory/chat/stream
Content-Type: application/json

{
  "conversationId": "conv-001",
  "content": "我叫什么名字？"
}

# 清除记忆
DELETE /ai/learn/memory/{conversationId}
```

### Tool Calling (工具调用)

```bash
# 获取可用工具列表
GET /ai/learn/tool/list

# 同步对话
POST /ai/learn/tool/chat
Content-Type: application/json

{
  "content": "今天日期是多少？计算 123 + 456",
  "enabledTools": ["dateTimeTool", "calculatorTool"]
}

# 流式对话
POST /ai/learn/tool/chat/stream
Content-Type: application/json

{
  "content": "现在是几点？",
  "enabledTools": ["dateTimeTool"]
}
```

#### 可用工具

| 工具名称 | 功能描述 |
|---------|---------|
| `dateTimeTool` | 获取当前日期和时间 |
| `calculatorTool` | 执行数学计算 |

### RAG (检索增强生成)

```bash
# 获取已加载的文档列表
GET /ai/learn/rag/documents

# 简单版 RAG 对话
POST /ai/learn/rag/simple/chat
Content-Type: application/json

{
  "query": "公司的核心业务是什么？"
}

# 简单版 RAG 流式对话
POST /ai/learn/rag/simple/chat/stream
Content-Type: application/json

{
  "query": "介绍一下你们的产品"
}

# 进阶版 RAG 对话（含查询改写）
POST /ai/learn/rag/advanced/chat
Content-Type: application/json

{
  "query": "我想了解一下你们公司主要是做什么的"
}

# 进阶版 RAG 流式对话
POST /ai/learn/rag/advanced/chat/stream
Content-Type: application/json

{
  "query": "技术架构是怎样的？"
}
```

#### RAG 版本对比

| 版本 | 技术方案 | 特点 |
|------|----------|------|
| 简单版 | QuestionAnswerAdvisor | 开箱即用，代码简洁 |
| 进阶版 | RetrievalAugmentationAdvisor | 支持查询改写，模块化架构 |

#### 预置文档

| 文档 | 内容概要 |
|------|----------|
| 公司介绍 | 企业背景、核心业务、发展历程 |
| 产品说明 | 主要产品、功能特性 |
| 技术架构 | 系统架构、技术栈 |
| 常见问题 | FAQ、故障排查 |
| 联系方式 | 客服电话、邮箱、地址 |

### RBAC AI 助手

基于 Function Calling 的角色权限查询AI助手，支持自然语言交互。

```bash
# 对话接口
POST /ai/rbac/chat
Content-Type: application/json

{
  "content": "你好"
}
```

#### 核心功能

**1. 查询用户信息**

```bash
# 根据手机号查询用户及其角色、权限
{
  "content": "查询手机号13800138000的用户信息"
}
```

**2. 查询角色权限**

```bash
# 查询角色拥有的权限
{
  "content": "系统管理员有哪些权限"
}
```

**3. 查询权限角色**

```bash
# 查询拥有特定权限的角色
{
  "content": "哪些角色有用户查看权限"
}
```

**4. 提交审批申请**

```bash
# 提交角色变更申请
{
  "content": "我要申请系统管理员角色，手机号13800138000，姓名张三，邮箱zhangsan@example.com，公司XX科技，原因是工作需要"
}
```

#### 模拟数据

**用户数据** (5条)

| 姓名 | 手机号 | 角色 |
|------|--------|------|
| 张三 | 13800138000 | 系统管理员 |
| 李四 | 13900139000 | 普通用户 |
| 王五 | 13700137000 | 审计员 |
| 赵六 | 13600136000 | 普通用户 |
| 孙七 | 13500135000 | 审计员 |

**角色数据** (3个)

| 角色名称 | 角色编码 | 权限数量 |
|----------|----------|----------|
| 系统管理员 | ADMIN | 10 |
| 普通用户 | USER | 3 |
| 审计员 | AUDITOR | 4 |

**权限数据** (10个)

| 权限名称 | 权限编码 | 操作类型 |
|----------|----------|----------|
| 用户查看 | USER_READ | READ |
| 用户新增 | USER_WRITE | WRITE |
| 用户删除 | USER_DELETE | DELETE |
| 角色查看 | ROLE_READ | READ |
| 角色管理 | ROLE_WRITE | WRITE |
| 权限查看 | PERMISSION_READ | READ |
| 权限管理 | PERMISSION_WRITE | WRITE |
| 审批查看 | APPROVAL_READ | READ |
| 审批处理 | APPROVAL_WRITE | WRITE |
| 日志查看 | LOG_READ | READ |

详细测试用例请参考: [docs/rbac-api-test-cases.md](docs/rbac-api-test-cases.md)

## 项目结构

```
src/main/java/com/shinelon/hello/
├── common/
│   ├── constants/          # 常量定义
│   ├── exception/          # 异常处理
│   ├── result/             # 统一响应
│   └── utils/              # 工具类
├── config/                 # 配置类
├── controller/             # 控制器
├── manager/                # 业务管理器
├── model/
│   ├── dto/                # 数据传输对象
│   └── vo/                 # 视图对象
├── service/                # 服务接口
│   └── impl/               # 服务实现
└── tool/                   # AI 工具定义
```

## 开发规范

本项目遵循阿里巴巴 Java 开发手册（嵩山版）规范，包括：

- 编程规约
- 异常日志
- 单元测试
- 安全规约
- 工程结构

## License

MIT
