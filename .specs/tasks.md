# HelloSpringAI 开发任务列表

**版本**: 1.0.0
**创建日期**: 2026-02-21
**基于**: spec.md v1.0.0, plan.md v1.0.0

---

## 任务说明

- **[P]** 表示可并行执行的任务（无依赖关系）
- **依赖** 列表示该任务开始前必须完成的任务
- **TDD** 标记表示测试先行任务

---

## Phase 0: 项目初始化

| ID | 任务 | 文件路径 | 依赖 | 说明 |
|----|------|----------|------|------|
| T-001 | [P] 创建 pom.xml | `pom.xml` | - | GAV定义、依赖管理、Java17配置 |
| T-002 | [P] 创建启动类 | `src/main/java/.../HelloSpringAiApplication.java` | T-001 | Spring Boot 入口类 |
| T-003 | [P] 创建配置文件 | `src/main/resources/application.yml` | - | 服务端口、context-path、数据源、智谱AI配置 |
| T-004 | [P] 创建建表脚本 | `src/main/resources/schema.sql` | - | chat_session、chat_message 表结构 |

**验收**: `mvn spring-boot:run` 启动成功

---

## Phase 1: 公共基础设施

| ID | 任务 | 文件路径 | 依赖 | 说明 |
|----|------|----------|------|------|
| T-005 | [P] 创建错误码枚举 | `src/main/java/.../common/enums/ErrorCodeEnum.java` | - | 200/400/404/500/503 错误码 |
| T-006 | [P] 创建统一响应类 | `src/main/java/.../common/result/Result.java` | - | 统一响应包装类 |
| T-007 | [P] 创建业务异常类 | `src/main/java/.../common/exception/BusinessException.java` | T-005 | 携带 ErrorCode 的业务异常 |
| T-008 | 创建全局异常处理器 | `src/main/java/.../common/exception/GlobalExceptionHandler.java` | T-005, T-006, T-007 | @RestControllerAdvice 统一处理 |

**验收**: 手动抛出 BusinessException 返回统一错误格式

---

## Phase 2: 数据层

| ID | 任务 | 文件路径 | 依赖 | 说明 |
|----|------|----------|------|------|
| T-009 | [P] 创建会话实体 | `src/main/java/.../model/entity/ChatSessionDO.java` | Phase 0 | @Entity, sessionId唯一索引 |
| T-010 | [P] 创建消息实体 | `src/main/java/.../model/entity/ChatMessageDO.java` | Phase 0 | @Entity, 外键关联Session |
| T-011 | 创建会话DAO接口 | `src/main/java/.../dao/ChatSessionDao.java` | T-009 | JpaRepository, findBySessionId |
| T-012 | 创建消息DAO接口 | `src/main/java/.../dao/ChatMessageDao.java` | T-010 | JpaRepository, findBySessionIdOrderByCreateTime |
| T-013 | **[TDD]** 编写会话DAO测试 | `src/test/java/.../dao/ChatSessionDaoTest.java` | T-011 | CRUD + findBySessionId 测试 |
| T-014 | **[TDD]** 编写消息DAO测试 | `src/test/java/.../dao/ChatMessageDaoTest.java` | T-012 | CRUD + 按会话查询测试 |

**验收**: 所有 DAO 测试通过

---

## Phase 3: Manager 层

| ID | 任务 | 文件路径 | 依赖 | 说明 |
|----|------|----------|------|------|
| T-015 | **[TDD]** 编写智谱AI Manager测试 | `src/test/java/.../manager/ZhipuAiManagerTest.java` | Phase 0 | 集成测试，调用真实API |
| T-016 | 实现智谱AI Manager | `src/main/java/.../manager/ZhipuAiManager.java` | T-015 | 注入ChatClient，同步/流式调用 |

**验收**: Manager 能成功调用智谱AI

---

## Phase 4: Service 层

| ID | 任务 | 文件路径 | 依赖 | 说明 |
|----|------|----------|------|------|
| T-017 | [P] 创建对话请求DTO | `src/main/java/.../model/dto/ChatRequestDTO.java` | - | sessionId, content 字段 |
| T-018 | [P] 创建消息VO | `src/main/java/.../model/vo/MessageVO.java` | - | role, content, createTime |
| T-019 | [P] 创建会话VO | `src/main/java/.../model/vo/SessionVO.java` | T-018 | sessionId, title, messages列表 |
| T-020 | **[TDD]** 编写ChatService测试 | `src/test/java/.../service/ChatServiceTest.java` | T-017, T-018, T-019 | 表格驱动：新会话/继续对话/无效会话/空消息 |
| T-021 | 创建ChatService接口 | `src/main/java/.../service/ChatService.java` | T-020 | 方法定义 |
| T-022 | 实现ChatServiceImpl | `src/main/java/.../service/impl/ChatServiceImpl.java` | T-021, Phase 2, Phase 3 | 会话管理、消息组装、AI调用 |

**验收**: 所有 Service 测试通过

---

## Phase 5: Controller 层

| ID | 任务 | 文件路径 | 依赖 | 说明 |
|----|------|----------|------|------|
| T-023 | **[TDD]** 编写ChatController测试 | `src/test/java/.../controller/ChatControllerTest.java` | T-017, Phase 1, Phase 4 | @WebMvcTest 同步/流式接口 |
| T-024 | **[TDD]** 编写SessionController测试 | `src/test/java/.../controller/SessionControllerTest.java` | T-019, Phase 1, Phase 4 | @WebMvcTest 会话管理接口 |
| T-025 | 实现ChatController | `src/main/java/.../controller/ChatController.java` | T-023 | POST /chat, POST /chat/stream |
| T-026 | 实现SessionController | `src/main/java/.../controller/SessionController.java` | T-024 | CRUD /sessions |

**验收**: 通过 curl/Postman 完整验证所有 API

---

## Phase 6: 集成验证

| ID | 任务 | 文件路径 | 依赖 | 说明 |
|----|------|----------|------|------|
| T-027 | 完整流程验证 | - | Phase 5 | 新建会话 → 多轮对话 → 查询历史 → 删除会话 |
| T-028 | 流式响应验证 | - | T-027 | SSE 连接稳定，内容实时返回 |
| T-029 | 异常场景验证 | - | T-027 | 参数校验、不存在的资源、AI服务异常 |

**验收**: 所有功能符合 spec.md 定义

---

## 任务依赖图

```
Phase 0 (并行)
├── T-001 pom.xml
├── T-002 HelloSpringAiApplication.java (依赖 T-001)
├── T-003 application.yml
└── T-004 schema.sql

Phase 1 (并行 + 串行)
├── T-005 ErrorCodeEnum.java
├── T-006 Result.java
├── T-007 BusinessException.java (依赖 T-005)
└── T-008 GlobalExceptionHandler.java (依赖 T-005, T-006, T-007)

Phase 2 (实体并行 → DAO → 测试)
├── T-009 ChatSessionDO.java
├── T-010 ChatMessageDO.java
├── T-011 ChatSessionDao.java (依赖 T-009)
├── T-012 ChatMessageDao.java (依赖 T-010)
├── T-013 ChatSessionDaoTest.java (依赖 T-011)
└── T-014 ChatMessageDaoTest.java (依赖 T-012)

Phase 3 (TDD)
├── T-015 ZhipuAiManagerTest.java
└── T-016 ZhipuAiManager.java (依赖 T-015)

Phase 4 (DTO并行 → TDD → 实现)
├── T-017 ChatRequestDTO.java
├── T-018 MessageVO.java
├── T-019 SessionVO.java (依赖 T-018)
├── T-020 ChatServiceTest.java (依赖 T-017, T-018, T-019)
├── T-021 ChatService.java (依赖 T-020)
└── T-022 ChatServiceImpl.java (依赖 T-021)

Phase 5 (TDD)
├── T-023 ChatControllerTest.java
├── T-024 SessionControllerTest.java
├── T-025 ChatController.java (依赖 T-023)
└── T-026 SessionController.java (依赖 T-024)

Phase 6 (串行验证)
├── T-027 完整流程验证 (依赖 Phase 5)
├── T-028 流式响应验证 (依赖 T-027)
└── T-029 异常场景验证 (依赖 T-027)
```

---

## 执行顺序建议

按以下顺序执行可最大化并行效率：

1. **第一批 (并行)**: T-001, T-003, T-004, T-005, T-006, T-017, T-018
2. **第二批**: T-002, T-007, T-009, T-010, T-019
3. **第三批**: T-008, T-011, T-012, T-015
4. **第四批 (TDD)**: T-013, T-014, T-016
5. **第五批 (TDD)**: T-020
6. **第六批 (TDD)**: T-021, T-023, T-024
7. **第七批**: T-022, T-025, T-026
8. **第八批 (验证)**: T-027, T-028, T-029

---

## 文件清单

```
创建文件总数: 26 个

src/main/java/com/shinelon/hello/
├── HelloSpringAiApplication.java          # T-002
├── common/
│   ├── result/
│   │   └── Result.java                    # T-006
│   ├── enums/
│   │   └── ErrorCodeEnum.java             # T-005
│   └── exception/
│       ├── BusinessException.java         # T-007
│       └── GlobalExceptionHandler.java    # T-008
├── controller/
│   ├── ChatController.java                # T-025
│   └── SessionController.java             # T-026
├── dao/
│   ├── ChatSessionDao.java                # T-011
│   └── ChatMessageDao.java                # T-012
├── manager/
│   └── ZhipuAiManager.java                # T-016
├── model/
│   ├── entity/
│   │   ├── ChatSessionDO.java             # T-009
│   │   └── ChatMessageDO.java             # T-010
│   ├── dto/
│   │   └── ChatRequestDTO.java            # T-017
│   └── vo/
│       ├── MessageVO.java                 # T-018
│       └── SessionVO.java                 # T-019
└── service/
    ├── ChatService.java                   # T-021
    └── impl/ChatServiceImpl.java          # T-022

src/main/resources/
├── application.yml                        # T-003
└── schema.sql                             # T-004

src/test/java/com/shinelon/hello/
├── dao/
│   ├── ChatSessionDaoTest.java            # T-013
│   └── ChatMessageDaoTest.java            # T-014
├── manager/
│   └── ZhipuAiManagerTest.java            # T-015
├── service/
│   └── ChatServiceTest.java               # T-020
└── controller/
    ├── ChatControllerTest.java            # T-023
    └── SessionControllerTest.java         # T-024

pom.xml                                    # T-001
```

---

## 备注

- 每完成一个任务，建议提交一次 Git commit
- TDD 标记的任务必须先编写测试，测试失败后再编写实现
- 测试类中的测试用例应采用表格驱动风格
