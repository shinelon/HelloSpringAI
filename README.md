# HelloSpringAI

Spring Boot + Spring AI 智谱AI集成示例项目。

## 技术栈

- Java 17
- Spring Boot 3.4.3
- Spring AI 1.1.2
- 智谱AI (zhipuai)
- H2 Database

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

### 同步对话

```bash
POST /ai/chat
Content-Type: application/json

{
  "sessionId": "session-001",
  "content": "你好"
}
```

### 流式对话 (SSE)

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
# 获取所有会话
GET /ai/sessions

# 获取会话历史
GET /ai/sessions/{sessionId}/messages

# 删除会话
DELETE /ai/sessions/{sessionId}
```

## License

MIT
