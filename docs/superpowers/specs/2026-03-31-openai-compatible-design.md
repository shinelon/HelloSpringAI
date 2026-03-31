# OpenAI 兼容接口设计文档

## 1. 概述

为 HelloSpringAI 项目增加 OpenAI 兼容接口支持，实现 AI Provider 的互斥配置切换。

## 2. 背景

当前项目仅支持智谱AI（ZhipuAI），需扩展支持 OpenAI 兼容接口（通过自定义 base-url），供用户根据配置灵活切换。

## 3. 目标

- 支持通过配置文件切换 AI Provider（ZhipuAI / OpenAI）
- 保留现有所有功能：Chat Memory、Tool Calling、RAG、RBAC
- 对现有业务代码无侵入

## 4. 技术方案

### 4.1 配置设计

**互斥开关**：`spring.ai.provider` (值为 `zhipuai` 或 `openai`，默认 `zhipuai`)

**ZhipuAI 配置示例**：
```yaml
spring:
  ai:
    provider: zhipuai
    zhipuai:
      api-key: ${ZHIPUAI_API_KEY}
      chat:
        enabled: true
        options:
          model: glm-4-flash
          temperature: 0.7
```

**OpenAI 配置示例**：
```yaml
spring:
  ai:
    provider: openai
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
```

### 4.2 条件加载机制

现有 managers（如 `MemoryChatManager`、`ToolChatManager`、`RagChatManager`、`RbacChatManager`）已直接依赖 `ChatClient.Builder`：

```java
// 现有代码示例
private final ChatClient.Builder chatClientBuilder;
```

Spring AI 根据配置自动创建对应 provider 的 `ChatClient.Builder`。通过 `@ConditionalOnProperty` 控制哪个 provider 的配置生效即可，现有 managers 无需任何修改。

**配置类**：

| Provider | 配置类 | 条件 |
|----------|--------|------|
| zhipuai | ZhipuAiAutoConfiguration | `spring.ai.provider=zhipuai` 或未配置 |
| openai | OpenAiAutoConfiguration | `spring.ai.provider=openai` |

### 4.3 依赖更新

**pom.xml 新增**：
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
    <version>${spring-ai.version}</version>
</dependency>
```

### 4.4 现有功能兼容性

| 功能 | 兼容性 | 说明 |
|------|--------|------|
| Chat Memory | 兼容 | `MemoryChatManager` 依赖 `ChatClient.Builder` |
| Tool Calling | 兼容 | Spring AI 统一处理 |
| RAG | 兼容 | `RagChatManager` 依赖 `ChatClient.Builder` |
| RBAC | 兼容 | `RbacChatManager` 依赖 `ChatClient.Builder` |

## 5. 配置参数

### 5.1 ZhipuAI 参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `spring.ai.zhipuai.api-key` | 智谱AI API Key | - |
| `spring.ai.zhipuai.chat.options.model` | 模型名称 | glm-4-flash |
| `spring.ai.zhipuai.chat.options.temperature` | 温度参数 | 0.7 |

### 5.2 OpenAI 参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `spring.ai.openai.api-key` | OpenAI API Key | - |
| `spring.ai.openai.base-url` | API 基础地址 | https://api.openai.com/v1 |
| `spring.ai.openai.chat.options.model` | 模型名称 | gpt-4o-mini |
| `spring.ai.openai.chat.options.temperature` | 温度参数 | 0.7 |

## 6. 实现步骤

### 步骤1：添加依赖

pom.xml 添加 OpenAI starter：
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
    <version>${spring-ai.version}</version>
</dependency>
```

### 步骤2：自动配置冲突处理

Spring AI 的 `ZhipuAiAutoConfiguration` 和 `OpenAiAutoConfiguration` 会同时存在于 classpath，需要通过配置排除不使用的 provider。

**方案：使用 `spring.ai.{provider}.chat.enabled=false` 禁用不需要的配置**

```yaml
# ZhipuAI 模式 - 禁用 OpenAI
spring:
  ai:
    provider: zhipuai
    zhipuai:
      chat:
        enabled: true
    openai:
      chat:
        enabled: false

# OpenAI 模式 - 禁用 ZhipuAI
spring:
  ai:
    provider: openai
    zhipuai:
      chat:
        enabled: false
    openai:
      chat:
        enabled: true
      base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}
```

Spring AI 的自动配置类会检查对应的 `enabled` 属性，因此只需在配置文件中设置即可实现互斥。

### 步骤3：配置切换验证

验证步骤：
1. 设置 `spring.ai.provider=zhipuai` + `zhipuai.chat.enabled=true` + `openai.chat.enabled=false` → 使用智谱AI
2. 设置 `spring.ai.provider=openai` + `zhipuai.chat.enabled=false` + `openai.chat.enabled=true` → 使用 OpenAI

## 7. 测试验证

- 切换 provider 配置，验证 AI 调用正常
- 验证现有功能（Chat Memory、Tool Calling、RAG、RBAC）不受影响
