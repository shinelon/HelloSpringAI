# Chat 模块测试报告 #02

## 测试环境

| 项目 | 值 |
|------|-----|
| 测试日期 | 2026-02-21 12:37 |
| 操作系统 | Windows 11 |
| 应用端口 | 8080 |
| Context Path | /ai |
| 环境变量传递 | 显式传递 `ZHIPUAI_API_KEY` |

## 测试结果汇总

| 任务ID | 测试项 | 状态 | 接口 |
|--------|--------|------|------|
| C01 | 同步对话 - 无sessionId | **PASS** | POST /chat |
| C02 | 同步对话 - 有sessionId | **PASS** | POST /chat |
| C04 | 流式对话 | **PASS** | POST /chat/stream |

---

## 详细测试记录

### C01: 同步对话 - 无sessionId

**请求:**
```bash
curl -s -X POST http://localhost:8080/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"content": "Hello, please introduce yourself in one sentence"}'
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": "3df0cf2d-b132-4c0e-bfa8-d7049e6fa5af",
    "role": "assistant",
    "content": "Hello, I am an AI language model designed to assist with a wide range of queries and tasks.",
    "createTime": "2026-02-21T12:37:19.111463"
  }
}
```

**验证:** 返回了有效的 `sessionId` 和 AI 响应内容。

---

### C02: 同步对话 - 有sessionId

**请求:**
```bash
curl -s -X POST http://localhost:8080/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "3df0cf2d-b132-4c0e-bfa8-d7049e6fa5af", "content": "What did I ask you just now?"}'
```

**响应:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sessionId": "3df0cf2d-b132-4c0e-bfa8-d7049e6fa5af",
    "role": "assistant",
    "content": "You asked me to introduce myself in one sentence.",
    "createTime": "2026-02-21T12:37:29.172968"
  }
}
```

**验证:** AI 正确回忆了历史上下文，回答了 "You asked me to introduce myself in one sentence."

---

### C04: 流式对话

**请求:**
```bash
curl -s -X POST http://localhost:8080/ai/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"content": "Write a short poem about spring"}'
```

**响应格式 (SSE):**
```
data:{"sessionId":"e275221b-105e-482e-9ecf-368a23943db6","role":null,"content":"Wh","createTime":null}

data:{"sessionId":"e275221b-105e-482e-9ecf-368a23943db6","role":null,"content":"ispers","createTime":null}

data:{"sessionId":"e275221b-105e-482e-9ecf-368a23943db6","role":null,"content":" of","createTime":null}

... (更多流式数据块)

event:done
data:{"sessionId":null,"role":null,"content":"","createTime":null}
```

**完整响应内容:**
```
Whispers of the earth begin to stir,
As spring's soft breath renews the fire.
The sun, a radiant, golden beam,
Warms the cold, snow-kissed fields.

Buds in the trees, like stars so bright,
Awaken from their winter flight.
Petals burst forth, in vibrant hues,
Painting the sky, a canvas new.

The brook awakens, singing sweet,
Through the verdant meadows meet.
Birds, in their songs, declare their claim,
As springtime's warmth begins to reclaim.

A gentle breeze, with tender grace,
Carries the scent of blooming space.
In the air, a sweet, heady thrill,
Springs from the soil, where life will thrive.

Springtime, a season of rebirth,
With each passing day, the world is reborn.
```

**验证:** SSE 格式正确返回，以 `data:` 开头，最后以 `event:done` 结束。

---

## 问题分析与解决

### 之前失败原因

在 `test_report.md` 中，C01、C02、C04 失败的原因是 `ZHIPUAI_API_KEY` 环境变量未正确传递给应用。

### 解决方案

使用显式传递环境变量的方式启动应用：

```bash
ZHIPUAI_API_KEY="$ZHIPUAI_API_KEY" D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd spring-boot:run -q
```

这确保了 maven 子进程能够正确继承环境变量。

### 注意事项

1. **UTF-8 编码问题**: 在 Windows bash 环境下使用 curl 发送中文内容时，可能出现编码问题。建议测试时使用英文内容，或在生产环境中确保正确的字符编码处理。

---

## 结论

Chat 模块所有测试用例均通过，功能正常。
