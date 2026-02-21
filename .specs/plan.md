# HelloSpringAI 开发计划

**版本**: 1.0.0
**创建日期**: 2026-02-21
**基于规格**: `.specs/spec.md v1.0.0`

---

## 开发原则

1. **TDD 先行**：每个功能先写测试，再写实现
2. **分层依赖**：按 DAO → Manager → Service → Controller 顺序开发
3. **最小增量**：每个 Phase 独立可验证，完成后可启动测试
4. **规范合规**：严格遵循阿里巴巴 Java 开发手册

---

## Phase 0: 项目初始化

**目标**：创建可启动的 Spring Boot 项目骨架

### 0.1 创建 pom.xml

| 任务 | 说明 |
|------|------|
| 定义 GAV | `com.shinelon:hello-spring-ai:1.0.0` |
| 引入 parent | `spring-boot-starter-parent:3.x` |
| 引入依赖 | spring-ai-starter-model-zhipuai、spring-boot-starter-web、spring-boot-starter-data-jpa、h2、lombok |
| 配置 Maven | Java 17、UTF-8 编码 |

### 0.2 创建启动类

| 文件 | 说明 |
|------|------|
| `HelloSpringAiApplication.java` | Spring Boot 入口 |

### 0.3 创建配置文件

| 文件 | 说明 |
|------|------|
| `application.yml` | 服务端口、context-path、H2数据源、JPA配置、智谱AI配置 |
| `schema.sql` | 建表脚本（chat_session、chat_message） |

**验收标准**：`mvn spring-boot:run` 启动成功，访问 `/ai/actuator/health` 返回 UP

---

## Phase 1: 公共基础设施

**目标**：建立统一的异常处理和响应格式

### 1.1 统一响应模型

| 文件 | 说明 |
|------|------|
| `common/result/Result.java` | 统一响应包装类 |

### 1.2 异常体系

| 文件 | 说明 |
|------|------|
| `common/enums/ErrorCodeEnum.java` | 错误码枚举（200/400/404/500/503） |
| `common/exception/BusinessException.java` | 业务异常类 |
| `common/exception/GlobalExceptionHandler.java` | 全局异常处理器 |

**验收标准**：手动抛出 BusinessException，API 返回统一错误格式

---

## Phase 2: 数据层

**目标**：完成实体定义和数据访问

### 2.1 实体类

| 文件 | 说明 |
|------|------|
| `model/entity/ChatSessionDO.java` | 会话实体（@Entity） |
| `model/entity/ChatMessageDO.java` | 消息实体（@Entity，外键关联 Session） |

### 2.2 DAO 接口

| 文件 | 说明 |
|------|------|
| `dao/ChatSessionDao.java` | 继承 JpaRepository，自定义查询方法 |
| `dao/ChatMessageDao.java` | 继承 JpaRepository，自定义查询方法 |

### 2.3 DAO 测试（TDD）

| 测试文件 | 测试内容 |
|---------|---------|
| `dao/ChatSessionDaoTest.java` | CRUD + findBySessionId |
| `dao/ChatMessageDaoTest.java` | CRUD + findBySessionIdOrderByCreateTime |

**验收标准**：所有 DAO 测试通过

---

## Phase 3: Manager 层

**目标**：封装智谱 AI 调用能力

### 3.1 Manager 实现

| 文件 | 说明 |
|------|------|
| `manager/ZhipuAiManager.java` | 注入 ChatClient，提供同步/流式调用方法 |

### 3.2 Manager 测试（TDD）

| 测试文件 | 测试内容 |
|---------|---------|
| `manager/ZhipuAiManagerTest.java` | 集成测试，调用真实智谱AI API |

**验收标准**：Manager 能成功调用智谱AI，返回响应

---

## Phase 4: Service 层

**目标**：实现核心业务逻辑

### 4.1 DTO/VO 定义

| 文件 | 说明 |
|------|------|
| `model/dto/ChatRequestDTO.java` | 对话请求参数 |
| `model/vo/MessageVO.java` | 消息视图对象 |
| `model/vo/SessionVO.java` | 会话视图对象（含消息列表） |

### 4.2 Service 接口与实现

| 文件 | 说明 |
|------|------|
| `service/ChatService.java` | 服务接口定义 |
| `service/impl/ChatServiceImpl.java` | 服务实现（会话管理、消息组装） |

### 4.3 Service 测试（TDD）

| 测试文件 | 测试用例 |
|---------|---------|
| `service/ChatServiceTest.java` | 表格驱动：新会话对话、继续对话、无效会话、空消息 |

**验收标准**：所有 Service 测试通过

---

## Phase 5: Controller 层

**目标**：暴露 REST API

### 5.1 Controller 实现

| 文件 | 说明 |
|------|------|
| `controller/ChatController.java` | 对话接口（同步/流式） |
| `controller/SessionController.java` | 会话管理接口 |

### 5.2 Controller 测试（TDD）

| 测试文件 | 测试内容 |
|---------|---------|
| `controller/ChatControllerTest.java` | @WebMvcTest 集成测试 |
| `controller/SessionControllerTest.java` | @WebMvcTest 集成测试 |

**验收标准**：通过 curl/Postman 完整验证所有 API

---

## Phase 6: 集成验证

**目标**：端到端功能验证

| 任务 | 说明 |
|------|------|
| 完整流程测试 | 新建会话 → 多轮对话 → 查询历史 → 删除会话 |
| 流式响应测试 | SSE 连接稳定，内容实时返回 |
| 异常场景测试 | 参数校验、不存在的资源、AI服务异常 |

**验收标准**：所有功能符合 spec.md 定义

---

## 文件清单总览

```
待创建文件（共 20+ 个）：

src/main/java/com/shinelon/hello/
├── HelloSpringAiApplication.java
├── common/
│   ├── result/Result.java
│   ├── enums/ErrorCodeEnum.java
│   └── exception/
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
├── config/
│   └── (可选配置类)
├── controller/
│   ├── ChatController.java
│   └── SessionController.java
├── dao/
│   ├── ChatSessionDao.java
│   └── ChatMessageDao.java
├── manager/
│   └── ZhipuAiManager.java
├── model/
│   ├── entity/
│   │   ├── ChatSessionDO.java
│   │   └── ChatMessageDO.java
│   ├── dto/
│   │   └── ChatRequestDTO.java
│   └── vo/
│       ├── MessageVO.java
│       └── SessionVO.java
└── service/
    ├── ChatService.java
    └── impl/ChatServiceImpl.java

src/main/resources/
├── application.yml
└── schema.sql

src/test/java/com/shinelon/hello/
├── dao/
│   ├── ChatSessionDaoTest.java
│   └── ChatMessageDaoTest.java
├── manager/
│   └── ZhipuAiManagerTest.java
├── service/
│   └── ChatServiceTest.java
└── controller/
    ├── ChatControllerTest.java
    └── SessionControllerTest.java
```

---

## 风险与注意事项

| 风险 | 应对措施 |
|------|---------|
| 智谱AI API 限流 | 测试时控制调用频率 |
| H2 与 MySQL 语法差异 | 使用 JPA 标准 API，避免原生 SQL |
| SSE 连接超时 | 配置合理的超时时间 |
| 并发会话问题 | 会话 ID 使用 UUID，无状态设计 |

---

## 执行建议

1. 每完成一个 Phase，提交一次 Git commit
2. 遇到阻塞问题，及时记录到 plan.md 备注中
3. 优先保证核心流程（对话），会话管理可后续补充
