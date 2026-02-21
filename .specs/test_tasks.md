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
