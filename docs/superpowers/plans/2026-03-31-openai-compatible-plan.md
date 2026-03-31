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

## Task 1: 添加 OpenAI starter 依赖

**Files:**
- Modify: `pom.xml:52-57`

- [ ] **Step 1: 添加依赖**

在 `pom.xml` 的 `dependencies` 节点下，添加 OpenAI starter：

```xml
<!-- Spring AI OpenAI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
    <version>${spring-ai.version}</version>
</dependency>
```

位置在 `spring-ai-starter-model-zhipuai` 之后。

- [ ] **Step 2: 验证依赖**

```bash
D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd dependency:tree -q | grep openai
```

预期输出包含 `spring-ai-starter-model-openai`

- [ ] **Step 3: 提交**

```bash
git add pom.xml
git commit -m "feat: add spring-ai-starter-model-openai dependency"
```

---

## Task 2: 更新 application.yml 添加 OpenAI 配置

**Files:**
- Modify: `application.yml:34-42`

- [ ] **Step 1: 添加 OpenAI 配置**

在 `spring.ai` 节点下，添加 `openai` 配置段：

```yaml
spring:
  ai:
    zhipuai:
      api-key: ${ZHIPUAI_API_KEY:123}
      chat:
        enabled: true
        options:
          model: glm-4-flash
          temperature: 0.7
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
      chat:
        enabled: false
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

读取 `application-test.yml`，将 provider 设置为 openai 模式：

```yaml
spring:
  ai:
    provider: openai
    zhipuai:
      chat:
        enabled: false
    openai:
      api-key: ${OPENAI_API_KEY:test-key}
      chat:
        enabled: true
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

### 切换为 OpenAI

修改 `application.yml`：

```yaml
spring:
  ai:
    provider: openai
    zhipuai:
      chat:
        enabled: false
    openai:
      api-key: your-openai-api-key
      base-url: your-custom-base-url  # 可选
      chat:
        enabled: true
```

### 切换为 ZhipuAI

```yaml
spring:
  ai:
    provider: zhipuai
    zhipuai:
      chat:
        enabled: true
    openai:
      chat:
        enabled: false
```

---

## 验证清单

- [ ] `spring-ai-starter-model-openai` 依赖已添加
- [ ] `application.yml` 包含 zhipuai 和 openai 两套配置
- [ ] 互斥机制：`enabled` 属性控制
- [ ] 项目可正常编译
- [ ] 测试可通过
