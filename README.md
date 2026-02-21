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
