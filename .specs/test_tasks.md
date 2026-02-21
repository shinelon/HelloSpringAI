# HelloSpringAI 接口测试任务清单

## 测试环境
- **Base URL**: `http://localhost:8080/ai`
- **测试时间**: 2026-02-21

---

## 测试任务列表

### Session 模块

| 序号 | 任务ID | 测试项 | 接口 | 方法 | 状态 |
|------|--------|--------|------|------|------|
| 1 | S01 | 创建会话 - 正常创建 | /sessions | POST | [ ] |
| 2 | S02 | 获取会话列表 - 默认分页 | /sessions | GET | [ ] |
| 3 | S03 | 获取会话列表 - 自定义分页 | /sessions?page=0&size=5 | GET | [ ] |
| 4 | S04 | 获取会话详情 - 正常查询 | /sessions/{sessionId} | GET | [ ] |
| 5 | S05 | 获取会话详情 - 不存在的ID | /sessions/non-existent-id | GET | [ ] |
| 6 | S06 | 删除会话 - 正常删除 | /sessions/{sessionId} | DELETE | [ ] |
| 7 | S07 | 删除会话 - 不存在的ID | /sessions/non-existent-id | DELETE | [ ] |

### Chat 模块

| 序号 | 任务ID | 测试项 | 接口 | 方法 | 状态 |
|------|--------|--------|------|------|------|
| 8 | C01 | 同步对话 - 无sessionId(新会话) | /chat | POST | [ ] |
| 9 | C02 | 同步对话 - 有sessionId(继续对话) | /chat | POST | [ ] |
| 10 | C03 | 同步对话 - 内容为空 | /chat | POST | [ ] |
| 11 | C04 | 流式对话 - 正常流式返回 | /chat/stream | POST | [ ] |
| 12 | C05 | 流式对话 - SSE格式验证 | /chat/stream | POST | [ ] |

### Chat Memory 模块

| 序号 | 任务ID | 测试项 | 接口 | 方法 | 状态 |
|------|--------|--------|------|------|------|
| 13 | M01 | 带记忆对话 - 正常调用 | /learn/memory/chat | POST | [ ] |
| 14 | M02 | 带记忆对话 - 空会话ID | /learn/memory/chat | POST | [ ] |
| 15 | M03 | 带记忆对话 - 空消息内容 | /learn/memory/chat | POST | [ ] |
| 16 | M04 | 带记忆对话 - 记忆功能验证 | /learn/memory/chat | POST | [ ] |
| 17 | M05 | 带记忆流式对话 - 正常调用 | /learn/memory/chat/stream | POST | [ ] |
| 18 | M06 | 清除记忆 - 正常清除 | /learn/memory/{conversationId} | DELETE | [ ] |

### Tool Calling 模块

| 序号 | 任务ID | 测试项 | 接口 | 方法 | 状态 |
|------|--------|--------|------|------|------|
| 19 | T01 | 带工具对话 - 日期时间工具 | /learn/tool/chat | POST | [ ] |
| 20 | T02 | 带工具对话 - 计算器工具 | /learn/tool/chat | POST | [ ] |
| 21 | T03 | 带工具对话 - 空消息内容 | /learn/tool/chat | POST | [ ] |
| 22 | T04 | 带工具对话 - 选择性启用工具 | /learn/tool/chat | POST | [ ] |
| 23 | T05 | 带工具流式对话 - 正常调用 | /learn/tool/chat/stream | POST | [ ] |
| 24 | T06 | 获取工具列表 | /learn/tool/list | GET | [ ] |

---

## 测试命令

### S01 - 创建会话
```bash
curl -X POST http://localhost:8080/ai/sessions \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### S02 - 获取会话列表(默认分页)
```bash
curl -X GET http://localhost:8080/ai/sessions \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### S03 - 获取会话列表(自定义分页)
```bash
curl -X GET "http://localhost:8080/ai/sessions?page=0&size=5" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### S04 - 获取会话详情
```bash
# 先创建会话获取sessionId, 然后查询
curl -X GET http://localhost:8080/ai/sessions/{sessionId} \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### S05 - 获取会话详情(不存在的ID)
```bash
curl -X GET http://localhost:8080/ai/sessions/non-existent-id-12345 \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### S06 - 删除会话
```bash
curl -X DELETE http://localhost:8080/ai/sessions/{sessionId} \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### S07 - 删除会话(不存在的ID)
```bash
curl -X DELETE http://localhost:8080/ai/sessions/non-existent-id-12345 \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"
```

### C01 - 同步对话(无sessionId)
```bash
curl -X POST http://localhost:8080/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"content": "你好，请介绍一下你自己"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### C02 - 同步对话(有sessionId)
```bash
curl -X POST http://localhost:8080/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "{sessionId}", "content": "请继续"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### C03 - 同步对话(内容为空)
```bash
curl -X POST http://localhost:8080/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"content": ""}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### C04 - 流式对话
```bash
curl -X POST http://localhost:8080/ai/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"content": "请写一首短诗"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### M01 - 带记忆对话(正常)
```bash
curl -X POST http://localhost:8080/ai/learn/memory/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"test-001","content":"你好"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### M02 - 带记忆对话(空会话ID)
```bash
curl -X POST http://localhost:8080/ai/learn/memory/chat \
  -H "Content-Type: application/json" \
  -d '{"content":"你好"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### M03 - 带记忆对话(空消息)
```bash
curl -X POST http://localhost:8080/ai/learn/memory/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"test-001","content":""}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### M04 - 记忆功能验证(第二轮对话)
```bash
curl -X POST http://localhost:8080/ai/learn/memory/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"test-001","content":"我刚才说了什么？"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### M05 - 带记忆流式对话
```bash
curl -X POST http://localhost:8080/ai/learn/memory/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"test-002","content":"请写一首短诗"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### M06 - 清除记忆
```bash
curl -X DELETE http://localhost:8080/ai/learn/memory/test-001 \
  -w "\nHTTP Status: %{http_code}\n"
```

### T01 - 日期时间工具
```bash
curl -X POST http://localhost:8080/ai/learn/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"content":"今天星期几？现在几点了？"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### T02 - 计算器工具
```bash
curl -X POST http://localhost:8080/ai/learn/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"content":"帮我算一下 123 + 456，再算一下 100 / 5"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### T03 - 带工具对话(空消息)
```bash
curl -X POST http://localhost:8080/ai/learn/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"content":""}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### T04 - 选择性启用工具
```bash
curl -X POST http://localhost:8080/ai/learn/tool/chat \
  -H "Content-Type: application/json" \
  -d '{"content":"现在几点了？","enabledTools":["datetime"]}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### T05 - 带工具流式对话
```bash
curl -X POST http://localhost:8080/ai/learn/tool/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"content":"今天几号？"}' \
  -w "\nHTTP Status: %{http_code}\n"
```

### T06 - 获取工具列表
```bash
curl -X GET http://localhost:8080/ai/learn/tool/list \
  -w "\nHTTP Status: %{http_code}\n"
```

---

## 预期结果

| 任务ID | 预期HTTP状态码 | 预期行为 |
|--------|----------------|----------|
| S01 | 200 | 返回包含sessionId的JSON |
| S02 | 200 | 返回会话分页列表 |
| S03 | 200 | 返回最多5条记录的分页列表 |
| S04 | 200 | 返回会话详情信息 |
| S05 | 404 或 500 | 返回会话不存在的错误 |
| S06 | 200 | 返回删除成功标识 |
| S07 | 200 或 404 | 返回删除结果 |
| C01 | 200 | 返回AI对话响应(含sessionId) |
| C02 | 200 | 返回基于历史上下文的AI响应 |
| C03 | 400 | 参数校验失败 |
| C04 | 200 | SSE格式流式返回AI响应 |
| M01 | 200 | 返回包含 conversationId 和 content 的 JSON |
| M02 | 400 | 参数校验失败 |
| M03 | 400 | 参数校验失败 |
| M04 | 200 | AI 能回答之前对话的内容 |
| M05 | 200 | SSE 格式流式返回 |
| M06 | 200 | 返回成功响应 |
| T01 | 200 | 返回包含日期时间信息的 AI 响应 |
| T02 | 200 | 返回包含计算结果的 AI 响应 |
| T03 | 400 | 参数校验失败 |
| T04 | 200 | 仅使用 datetime 工具 |
| T05 | 200 | SSE 格式流式返回 |
| T06 | 200 | 返回 ["datetime", "calculator"] |

---

## 单元测试文件清单

| 文件路径 | 说明 |
|---------|------|
| `src/test/java/.../tool/DateTimeToolTest.java` | 日期时间工具单元测试 |
| `src/test/java/.../tool/CalculatorToolTest.java` | 计算器工具单元测试 |
| `src/test/java/.../service/MemoryChatServiceTest.java` | Chat Memory Service 测试 |
| `src/test/java/.../service/ToolChatServiceTest.java` | Tool Calling Service 测试 |
| `src/test/java/.../manager/MemoryChatManagerTest.java` | Chat Memory Manager 测试 |
| `src/test/java/.../manager/ToolChatManagerTest.java` | Tool Calling Manager 测试 |
| `src/test/java/.../controller/MemoryChatControllerTest.java` | Chat Memory Controller 测试 |
| `src/test/java/.../controller/ToolChatControllerTest.java` | Tool Calling Controller 测试 |

## 运行测试

```bash
# 运行所有测试
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd test

# 运行指定测试类
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd test -Dtest=DateTimeToolTest
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd test -Dtest=CalculatorToolTest
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd test -Dtest=MemoryChatServiceTest
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd test -Dtest=ToolChatServiceTest

# 运行 Controller 测试
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd test -Dtest=MemoryChatControllerTest
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd test -Dtest=ToolChatControllerTest
```
