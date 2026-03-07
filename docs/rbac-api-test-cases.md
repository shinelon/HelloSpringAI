# RBAC AI 助手 API 测试用例

**日期**: 2026-03-07  
**版本**: 1.0  
**基础URL**: http://localhost:8080/ai

---

## 测试环境准备

### 1. 启动应用

```bash
# 设置环境变量（Windows）
set ZHIPUAI_API_KEY=your_api_key

# 启动应用
mvn.cmd spring-boot:run
```

### 2. 验证服务状态

```bash
curl -X GET http://localhost:8080/ai
```

---

## 测试用例列表

### 1. AI 开场询问测试

#### 1.1 发送问候语 - "你好"

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"你好\"}"
```

**预期结果**: AI 返回开场白，介绍4个功能

#### 1.2 发送问候语 - "hello"

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"hello\"}"
```

**预期结果**: AI 返回开场白，介绍4个功能

#### 1.3 询问功能 - "你能做什么"

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"你能做什么\"}"
```

**预期结果**: AI 返回功能介绍

#### 1.4 询问帮助 - "help"

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"help\"}"
```

**预期结果**: AI 返回功能介绍

---

### 2. Tool1 - 用户查询测试

#### 2.1 查询手机号 13800138000 的用户信息（系统管理员）

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"查询手机号13800138000的用户信息\"}"
```

**预期结果**: 返回张三的用户信息，包括系统管理员角色和所有权限

#### 2.2 查询手机号 13900139000 的用户信息（普通用户）

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"帮我查一下手机号13900139000的用户\"}"
```

**预期结果**: 返回李四的用户信息，包括普通用户角色和基础权限

#### 2.3 查询手机号 13700137000 的用户信息（审计员）

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"手机号13700137000这个用户的详细信息\"}"
```

**预期结果**: 返回王五的用户信息，包括审计员角色和查看权限

#### 2.4 查询不存在的手机号

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"查询手机号19999999999的用户\"}"
```

**预期结果**: AI 提示用户不存在

#### 2.5 自然语言查询用户

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"我想了解一下手机号13800138000这个人的角色和权限\"}"
```

**预期结果**: AI 理解意图并返回完整的用户信息

---

### 3. Tool2 - 角色权限查询测试

#### 3.1 查询"系统管理员"角色的权限

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"系统管理员这个角色有哪些权限\"}"
```

**预期结果**: 返回系统管理员的所有权限（10个权限）

#### 3.2 查询"ADMIN"角色编码的权限

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"ADMIN角色有什么权限\"}"
```

**预期结果**: 返回系统管理员的所有权限

#### 3.3 查询"普通用户"角色的权限

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"普通用户角色的权限列表\"}"
```

**预期结果**: 返回普通用户的权限（USER_READ, ROLE_READ, PERMISSION_READ等）

#### 3.4 查询"审计员"角色的权限

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"AUDITOR角色能做什么\"}"
```

**预期结果**: 返回审计员的查看权限

#### 3.5 查询不存在的角色

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"超级管理员角色有什么权限\"}"
```

**预期结果**: AI 提示角色不存在

---

### 4. Tool3 - 权限角色查询测试

#### 4.1 查询拥有"用户查看"权限的角色

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"哪些角色有用户查看权限\"}"
```

**预期结果**: 返回系统管理员和普通用户角色

#### 4.2 查询拥有"USER_DELETE"权限的角色

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"USER_DELETE权限哪些角色有\"}"
```

**预期结果**: 返回系统管理员角色

#### 4.3 查询拥有"日志查看"权限的角色

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"谁有日志查看的权限\"}"
```

**预期结果**: 返回系统管理员和审计员角色

#### 4.4 查询拥有"审批处理"权限的角色

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"审批处理权限在哪些角色里\"}"
```

**预期结果**: 返回系统管理员角色

#### 4.5 查询不存在的权限

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"哪些角色有超级删除权限\"}"
```

**预期结果**: AI 提示权限不存在

---

### 5. Tool4 - 审批提交测试

#### 5.1 提交角色变更申请

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"我要申请系统管理员角色，我的手机号是13800138000，姓名张三，邮箱zhangsan@example.com，公司XX科技，原因是工作需要\"}"
```

**预期结果**: 返回审批申请成功，包含审批ID

#### 5.2 简化申请（AI 引导补充信息）

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"我想申请普通用户角色\"}"
```

**预期结果**: AI 询问申请人的详细信息

#### 5.3 完整信息申请

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"帮我提交一个审批申请，申请人是李四，手机13900139000，邮箱lisi@example.com，公司YY公司，申请审计员角色，因为需要查看系统日志\"}"
```

**预期结果**: 返回审批申请成功

---

### 6. 综合场景测试

#### 6.1 连续对话 - 先查询用户，再查询角色权限

**第一次请求**:
```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"查一下13800138000这个用户\"}"
```

**第二次请求**:
```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"那普通用户角色有哪些权限呢\"}"
```

**预期结果**: 第一次返回用户信息，第二次返回普通用户的权限列表

#### 6.2 复杂查询 - 权限对比

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"比较一下系统管理员和普通用户的权限有什么区别\"}"
```

**预期结果**: AI 调用多次工具，对比两个角色的权限

#### 6.3 权限推荐

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"我只需要查看用户信息和日志，应该给我分配什么角色\"}"
```

**预期结果**: AI 推荐审计员角色

---

### 7. 边界测试

#### 7.1 空内容测试

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"\"}"
```

**预期结果**: 返回 400 错误，提示"消息内容不能为空"

#### 7.2 超长内容测试（Windows PowerShell）

```powershell
$longContent = "a" * 4001
curl -X POST http://localhost:8080/ai/rbac/chat `
  -H "Content-Type: application/json" `
  -d "{`"content`": `"$longContent`"}"
```

**预期结果**: 返回 400 错误，提示"消息内容不能超过4000字符"

#### 7.3 缺少 content 字段

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{}"
```

**预期结果**: 返回 400 错误，提示缺少必填字段

#### 7.4 错误的 Content-Type

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: text/plain" ^
  -d "你好"
```

**预期结果**: 返回 415 错误，不支持的媒体类型

---

### 8. 响应格式验证

#### 8.1 成功响应格式

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"你好\"}"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": "您好！我是角色权限查询AI小助手，我可以为您提供以下服务：\n\n1. 📱 查询用户信息...\n2. 🔍 查询角色权限...\n3. 🔎 查询权限角色...\n4. 📝 提交审批申请...\n\n请问您需要什么服务？",
    "createTime": "2026-03-07 10:30:00"
  }
}
```

#### 8.2 错误响应格式

```bash
curl -X POST http://localhost:8080/ai/rbac/chat ^
  -H "Content-Type: application/json" ^
  -d "{\"content\": \"\"}"
```

**预期响应**:
```json
{
  "code": 400,
  "message": "消息内容不能为空",
  "data": null
}
```

---

## 测试数据参考

### 用户数据

| 用户ID | 姓名 | 手机号 | 角色 |
|--------|------|--------|------|
| 1 | 张三 | 13800138000 | 系统管理员 |
| 2 | 李四 | 13900139000 | 普通用户 |
| 3 | 王五 | 13700137000 | 审计员 |
| 4 | 赵六 | 13600136000 | 普通用户 |
| 5 | 孙七 | 13500135000 | 审计员 |

### 角色数据

| 角色ID | 角色名称 | 角色编码 | 权限数量 |
|--------|----------|----------|----------|
| 1 | 系统管理员 | ADMIN | 10 |
| 2 | 普通用户 | USER | 3 |
| 3 | 审计员 | AUDITOR | 4 |

### 权限数据

| 权限ID | 权限名称 | 权限编码 | 操作类型 |
|--------|----------|----------|----------|
| 1 | 用户查看 | USER_READ | READ |
| 2 | 用户新增 | USER_WRITE | WRITE |
| 3 | 用户删除 | USER_DELETE | DELETE |
| 4 | 角色查看 | ROLE_READ | READ |
| 5 | 角色管理 | ROLE_WRITE | WRITE |
| 6 | 权限查看 | PERMISSION_READ | READ |
| 7 | 权限管理 | PERMISSION_WRITE | WRITE |
| 8 | 审批查看 | APPROVAL_READ | READ |
| 9 | 审批处理 | APPROVAL_WRITE | WRITE |
| 10 | 日志查看 | LOG_READ | READ |

---

## 测试检查清单

### 功能测试

- [ ] AI 开场询问功能正常
- [ ] Tool1 - 用户查询功能正常
- [ ] Tool2 - 角色权限查询功能正常
- [ ] Tool3 - 权限角色查询功能正常
- [ ] Tool4 - 审批提交功能正常
- [ ] 自然语言理解准确
- [ ] 错误提示友好

### 边界测试

- [ ] 空内容验证
- [ ] 超长内容验证
- [ ] 缺少必填字段验证
- [ ] 错误的 Content-Type 处理

### 响应格式测试

- [ ] 成功响应格式正确
- [ ] 错误响应格式正确
- [ ] 时间格式正确
- [ ] 字符编码正确（UTF-8）

---

## 常见问题排查

### 1. 返回 404 错误

**原因**: 应用未启动或端口错误  
**解决**: 检查应用是否在 8080 端口运行

### 2. 返回 500 错误

**原因**: ZHIPUAI_API_KEY 未设置或无效  
**解决**: 检查环境变量配置

### 3. AI 响应很慢

**原因**: AI 模型调用耗时  
**解决**: 正常现象，glm-4-flash 模型通常 2-5 秒响应

### 4. Tool 调用失败

**原因**: AI 理解错误或参数不匹配  
**解决**: 尝试更明确的表述，或查看日志排查

---

**文档创建时间**: 2026-03-07  
**最后更新时间**: 2026-03-07
