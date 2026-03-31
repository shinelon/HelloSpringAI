# OpenAI 兼容接口实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 通过添加 OpenAI starter 依赖和配置，实现 AI Provider 的互斥切换

**Architecture:** 利用 Spring AI 的 `chat.enabled` 属性控制自动配置开关，无需新建 Java 类

**Tech Stack:** Spring AI 1.1.2, spring-ai-starter-model-openai

---

## 文件变更

| 文件 | 变更类型 |
|------|----------|
| `pom.xml` | 添加依赖 |
| `application.yml` | 添加配置 |
| `application-test.yml` | 修改测试配置 |

---

## Task 1: 替换依赖（移除智谱，添加 OpenAI）

**Files:**
- Modify: `pom.xml:52-57`

- [ ] **Step 1: 替换依赖**

将 `spring-ai-starter-model-zhipuai` 替换为 `spring-ai-starter-model-openai`：

```xml
<!-- Spring AI OpenAI (替换原有的 zhipuai) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
    <version>${spring-ai.version}</version>
</dependency>
```

删除 `spring-ai-starter-model-zhipuai` 依赖。

- [ ] **Step 2: 验证依赖**

```bash
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd dependency:tree -q | findstr openai
```

预期输出包含 `spring-ai-starter-model-openai`

- [ ] **Step 3: 提交**

```bash
git add pom.xml
git commit -m "feat: add spring-ai-starter-model-openai dependency"
```

---

## Task 2: 更新 application.yml 为 OpenAI 配置

**Files:**
- Modify: `application.yml`

- [ ] **Step 1: 替换为 OpenAI 配置**

将 `spring.ai` 节点下的 zhipuai 配置替换为 OpenAI 配置：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
```

- [ ] **Step 2: 验证配置**

检查 YAML 格式正确性。

- [ ] **Step 3: 提交**

```bash
git add src/main/resources/application.yml
git commit -m "feat: add OpenAI configuration with mutual exclusion"
```

---

## Task 3: 更新测试配置

**Files:**
- Modify: `application-test.yml`

- [ ] **Step 1: 更新测试配置**

读取 `application-test.yml`，将 zhipuai 配置替换为 OpenAI 配置：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:test-key}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
```

- [ ] **Step 2: 提交**

```bash
git add src/test/resources/application-test.yml
git commit -m "test: configure test profile to use OpenAI"
```

---

## Task 4: 验证构建

**Files:**
- None (验证)

- [ ] **Step 1: 编译验证**

```bash
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd compile -q
```

预期：BUILD SUCCESS

- [ ] **Step 2: 运行测试**

```bash
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd test -q
```

预期：所有测试通过（或跳过需要真实 AI API 的测试）

- [ ] **Step 3: 提交**

无新文件变更，跳过。

---

## 使用说明

### 配置 OpenAI

修改 `application.yml`：

```yaml
spring:
  ai:
    openai:
      api-key: your-openai-api-key
      base-url: your-custom-base-url  # 可选，默认 https://api.openai.com/v1
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
```

---

## 验证清单

- [ ] `spring-ai-starter-model-openai` 依赖已添加
- [ ] `spring-ai-starter-model-zhipuai` 依赖已移除
- [ ] `application.yml` 包含 OpenAI 配置
- [ ] 项目可正常编译
- [ ] 测试可通过
