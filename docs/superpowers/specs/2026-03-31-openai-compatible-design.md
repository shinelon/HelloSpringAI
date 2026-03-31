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

**互斥开关**：`spring.ai.provider` (值为 `zhipuai` 或 `openai`)

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

使用 Spring `@ConditionalOnProperty` 控制 ChatClient.Builder 的创建：

| Provider | 启用的配置类 | 条件 |
|----------|-------------|------|
| zhipuai | ZhipuChatClientBuilderConfig | `spring.ai.provider=zhipuai` |
| openai | OpenAiChatClientBuilderConfig | `spring.ai.provider=openai` |

### 4.3 架构调整

**新增接口**：
- `AiManager`：定义 AI 调用的统一接口

**重构现有类**：
- `ZhipuAiManager` → 实现 `AiManager` 接口

**新增类**：
- `OpenAiManager`：实现 `AiManager` 接口，提供 OpenAI 调用能力
- `AiChatClientBuilderConfig`：条件化配置类，根据 provider 决定创建哪个 ChatClient.Builder

**目录结构**：
```
src/main/java/com/shinelon/hello/
├── config/
│   └── AiChatClientBuilderConfig.java   # 条件化配置
├── manager/
│   ├── AiManager.java                   # 统一接口
│   ├── ZhipuAiManager.java             # 实现AiManager
│   └── OpenAiManager.java              # 实现AiManager
```

### 4.4 现有功能兼容性

| 功能 | 兼容性 | 说明 |
|------|--------|------|
| Chat Memory | 兼容 | 依赖 AiManager 接口 |
| Tool Calling | 兼容 | Spring AI 统一处理 |
| RAG | 兼容 | 依赖 AiManager 接口 |
| RBAC | 兼容 | 依赖 AiManager 接口 |

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

1. 新增 `AiManager` 接口
2. 重构 `ZhipuAiManager` 实现 `AiManager`
3. 新增 `OpenAiManager` 实现 `AiManager`
4. 新增 `AiChatClientBuilderConfig` 条件化配置
5. 更新 `application.yml` 添加 OpenAI 配置示例
6. 单元测试

## 7. 测试验证

- 切换 provider 配置，验证 AI 调用正常
- 验证现有功能（Chat Memory、Tool Calling、RAG、RBAC）不受影响
