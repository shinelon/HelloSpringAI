# Spring AI RAG 学习示例设计文档

> 创建日期：2026-03-01
> 作者：shinelon
> 状态：已批准

## 一、概述

### 1.1 目标

在 HelloSpringAI 项目中新增 RAG（Retrieval Augmented Generation）学习示例，帮助开发者理解：

- RAG 的基本概念和工作原理
- Spring AI 提供的两种 RAG 实现方式
- 向量数据库的使用方法

### 1.2 设计原则

| 原则 | 说明 |
|------|------|
| 简单明了 | 减少外部依赖，使用内存向量存储 |
| 学习导向 | 提供简单版和进阶版两种实现对比 |
| 结构一致 | 与现有 MemoryChat、ToolChat 模块结构保持一致 |

### 1.3 技术选型

| 组件 | 选择 | 理由 |
|------|------|------|
| 向量数据库 | SimpleVectorStore | 零外部依赖，开箱即用 |
| 简单版 RAG | QuestionAnswerAdvisor | 开箱即用，代码最少 |
| 进阶版 RAG | RetrievalAugmentationAdvisor | 支持查询改写、模块化 |
| 示例数据 | 硬编码预置 | 无需用户准备数据 |

---

## 二、整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        RAG 学习模块                              │
├─────────────────────────────────────────────────────────────────┤
│  /learn/rag/simple/*     简单版 (QuestionAnswerAdvisor)         │
│  /learn/rag/advanced/*   进阶版 (RetrievalAugmentationAdvisor)  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      RagChatManager                              │
│  ┌─────────────────┐      ┌─────────────────────────────────┐   │
│  │ SimpleRagClient │      │ AdvancedRagClient               │   │
│  │ (QA Advisor)    │      │ (查询改写 + 向量检索 + 上下文增强) │   │
│  └─────────────────┘      └─────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SimpleVectorStore                             │
│                    (内存向量数据库)                               │
│                    + 预置示例文档 (5篇)                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ZhipuAI Embedding Model                       │
│                    (文本向量化)                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、文件结构

### 3.1 新增文件清单

```
src/main/java/com/shinelon/hello/
├── config/
│   └── RagConfig.java                 # VectorStore 配置 + 示例数据初始化
├── manager/
│   └── RagChatManager.java            # RAG 对话管理器（简单版 + 进阶版）
├── controller/
│   └── RagController.java             # REST API 控制器
├── service/
│   ├── RagService.java                # 服务接口
│   └── impl/
│       └── RagServiceImpl.java        # 服务实现
└── model/
    ├── dto/
    │   └── RagChatRequestDTO.java     # 请求 DTO
    └── vo/
        └── RagChatVO.java             # 响应 VO
```

### 3.2 新增测试文件

```
src/test/java/com/shinelon/hello/
├── manager/
│   └── RagChatManagerTest.java        # Manager 单元测试
├── service/
│   └── RagServiceTest.java            # Service 单元测试
└── controller/
    └── RagControllerTest.java         # Controller 集成测试
```

### 3.3 依赖变更

在 `pom.xml` 中新增：

```xml
<!-- RAG Advisor 支持 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-advisors-vector-store</artifactId>
</dependency>

<!-- 高级 RAG 模块 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-rag</artifactId>
</dependency>
```

---

## 四、API 设计

### 4.1 端点列表

| 端点 | 方法 | 描述 | RAG 类型 |
|------|------|------|----------|
| `/learn/rag/simple/chat` | POST | 简单版 RAG 对话 | QuestionAnswerAdvisor |
| `/learn/rag/simple/chat/stream` | POST | 简单版 RAG 流式对话 | QuestionAnswerAdvisor |
| `/learn/rag/advanced/chat` | POST | 进阶版 RAG 对话 | RetrievalAugmentationAdvisor |
| `/learn/rag/advanced/chat/stream` | POST | 进阶版 RAG 流式对话 | RetrievalAugmentationAdvisor |
| `/learn/rag/documents` | GET | 查看已加载的文档列表 | - |

### 4.2 请求/响应模型

**请求 DTO (RagChatRequestDTO)**:
```java
@Data
public class RagChatRequestDTO {
    
    @NotBlank(message = "查询内容不能为空")
    @Size(max = 2000, message = "查询内容不能超过2000字符")
    private String query;
}
```

**响应 VO (RagChatVO)**:
```java
@Data
@Builder
public class RagChatVO {
    
    /** AI 回复内容 */
    private String content;
    
    /** 检索到的文档数量 */
    private Integer sourceCount;
    
    /** 文档来源列表 */
    private List<String> sources;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
```

### 4.3 API 示例

**简单版对话请求**:
```bash
POST /learn/rag/simple/chat
Content-Type: application/json

{
  "query": "公司的核心业务是什么？"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": "根据公司介绍，核心业务是提供企业级 AI 解决方案...",
    "sourceCount": 2,
    "sources": ["公司介绍", "产品说明"],
    "createTime": "2026-03-01T15:30:00"
  }
}
```

**进阶版对话请求**:
```bash
POST /learn/rag/advanced/chat
Content-Type: application/json

{
  "query": "我想了解一下你们公司主要是做什么的"
}
```

**流式对话 (SSE)**:
```bash
POST /learn/rag/simple/chat/stream
Content-Type: application/json

{
  "query": "技术架构是怎样的？"
}
```

---

## 五、核心组件设计

### 5.1 RagConfig.java

```java
@Slf4j
@Configuration
public class RagConfig {

    /**
     * 创建内存向量存储
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }

    /**
     * 初始化示例文档数据
     */
    @Bean
    @Order(1)
    public CommandLineRunner initRagDocuments(VectorStore vectorStore) {
        return args -> {
            List<Document> documents = createSampleDocuments();
            vectorStore.add(documents);
            log.info("[RAG] 示例文档加载完成，共 {} 篇", documents.size());
        };
    }

    /**
     * 创建示例文档
     */
    private List<Document> createSampleDocuments() {
        return List.of(
            new Document(
                "【公司介绍】\n智云科技成立于2020年，是一家专注于企业级AI解决方案的高科技公司。" +
                "公司总部位于北京，在上海、深圳设有研发中心。" +
                "我们的使命是让AI技术惠及每一家企业。",
                Map.of("source", "公司介绍", "category", "about")
            ),
            new Document(
                "【产品说明】\n主要产品包括：\n" +
                "1. 智能客服系统：7x24小时自动应答，支持多轮对话\n" +
                "2. 文档智能处理：自动提取、分类、摘要\n" +
                "3. 知识库管理：企业知识统一管理，智能检索",
                Map.of("source", "产品说明", "category", "product")
            ),
            new Document(
                "【技术架构】\n系统采用微服务架构，主要技术栈包括：\n" +
                "- 后端：Spring Boot 3.x + Spring AI\n" +
                "- 数据库：PostgreSQL + pgvector\n" +
                "- 消息队列：RabbitMQ\n" +
                "- 部署：Kubernetes + Docker",
                Map.of("source", "技术架构", "category", "tech")
            ),
            new Document(
                "【常见问题】\nQ: 如何获取API密钥？\n" +
                "A: 登录控制台，在「设置-密钥管理」中创建\n\n" +
                "Q: 支持哪些模型？\n" +
                "A: 目前支持智谱AI、OpenAI、Azure OpenAI等",
                Map.of("source", "常见问题", "category", "faq")
            ),
            new Document(
                "【联系方式】\n客服电话：400-888-8888\n" +
                "技术支持邮箱：support@example.com\n" +
                "公司地址：北京市海淀区中关村大街1号\n" +
                "工作时间：周一至周五 9:00-18:00",
                Map.of("source", "联系方式", "category", "contact")
            )
        );
    }
}
```

### 5.2 RagChatManager.java

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class RagChatManager {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    private ChatClient simpleRagClient;
    private ChatClient advancedRagClient;

    @PostConstruct
    public void init() {
        // 初始化简单版 RAG 客户端
        this.simpleRagClient = ChatClient.builder(chatClientBuilder.build().mutate())
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.5)
                                .topK(3)
                                .build())
                        .build())
                .build();

        // 初始化进阶版 RAG 客户端
        Advisor advancedAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder)
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.5)
                        .topK(3)
                        .build())
                .build();

        this.advancedRagClient = ChatClient.builder(chatClientBuilder.build().mutate())
                .defaultAdvisors(advancedAdvisor)
                .build();
    }

    /**
     * 简单版 RAG 同步对话
     */
    public String simpleChat(String query) {
        log.debug("[RAG-Simple] 查询: {}", truncate(query, 100));
        return simpleRagClient.prompt()
                .user(query)
                .call()
                .content();
    }

    /**
     * 简单版 RAG 流式对话
     */
    public Flux<String> simpleChatStream(String query) {
        log.debug("[RAG-Simple-Stream] 查询: {}", truncate(query, 100));
        return simpleRagClient.prompt()
                .user(query)
                .stream()
                .content();
    }

    /**
     * 进阶版 RAG 同步对话（含查询改写）
     */
    public String advancedChat(String query) {
        log.debug("[RAG-Advanced] 查询: {}", truncate(query, 100));
        return advancedRagClient.prompt()
                .user(query)
                .call()
                .content();
    }

    /**
     * 进阶版 RAG 流式对话
     */
    public Flux<String> advancedChatStream(String query) {
        log.debug("[RAG-Advanced-Stream] 查询: {}", truncate(query, 100));
        return advancedRagClient.prompt()
                .user(query)
                .stream()
                .content();
    }

    /**
     * 获取已加载的文档列表
     */
    public List<String> getDocumentSources() {
        // 返回预置文档的来源列表
        return List.of("公司介绍", "产品说明", "技术架构", "常见问题", "联系方式");
    }

    private String truncate(String str, int max) {
        if (str == null) return null;
        return str.length() > max ? str.substring(0, max) + "..." : str;
    }
}
```

### 5.3 RagService.java & RagServiceImpl.java

**接口**:
```java
public interface RagService {
    
    /** 简单版 RAG 对话 */
    RagChatVO simpleChat(RagChatRequestDTO request);
    
    /** 简单版 RAG 流式对话 */
    Flux<RagChatVO> simpleChatStream(RagChatRequestDTO request);
    
    /** 进阶版 RAG 对话 */
    RagChatVO advancedChat(RagChatRequestDTO request);
    
    /** 进阶版 RAG 流式对话 */
    Flux<RagChatVO> advancedChatStream(RagChatRequestDTO request);
    
    /** 获取文档列表 */
    List<String> getDocuments();
}
```

**实现**:
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final RagChatManager ragChatManager;

    @Override
    public RagChatVO simpleChat(RagChatRequestDTO request) {
        validateRequest(request);
        log.info("[simpleChat] 简单版RAG对话开始, query={}", 
                DesensitizationUtils.truncateAndMask(request.getQuery(), 50));

        String content = ragChatManager.simpleChat(request.getQuery());
        
        return RagChatVO.builder()
                .content(content)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Flux<RagChatVO> simpleChatStream(RagChatRequestDTO request) {
        validateRequest(request);
        log.info("[simpleChatStream] 简单版RAG流式对话开始");

        return ragChatManager.simpleChatStream(request.getQuery())
                .map(chunk -> RagChatVO.builder()
                        .content(chunk)
                        .createTime(LocalDateTime.now())
                        .build());
    }

    @Override
    public RagChatVO advancedChat(RagChatRequestDTO request) {
        validateRequest(request);
        log.info("[advancedChat] 进阶版RAG对话开始, query={}", 
                DesensitizationUtils.truncateAndMask(request.getQuery(), 50));

        String content = ragChatManager.advancedChat(request.getQuery());
        
        return RagChatVO.builder()
                .content(content)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Flux<RagChatVO> advancedChatStream(RagChatRequestDTO request) {
        validateRequest(request);
        log.info("[advancedChatStream] 进阶版RAG流式对话开始");

        return ragChatManager.advancedChatStream(request.getQuery())
                .map(chunk -> RagChatVO.builder()
                        .content(chunk)
                        .createTime(LocalDateTime.now())
                        .build());
    }

    @Override
    public List<String> getDocuments() {
        return ragChatManager.getDocumentSources();
    }

    private void validateRequest(RagChatRequestDTO request) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
            throw new IllegalArgumentException("查询内容不能为空");
        }
    }
}
```

### 5.4 RagController.java

```java
@Slf4j
@RestController
@RequestMapping("/learn/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    // ==================== 简单版 RAG ====================

    /**
     * 简单版 RAG 同步对话
     */
    @PostMapping("/simple/chat")
    public Result<RagChatVO> simpleChat(@Valid @RequestBody RagChatRequestDTO request) {
        return Result.success(ragService.simpleChat(request));
    }

    /**
     * 简单版 RAG 流式对话
     */
    @PostMapping("/simple/chat/stream")
    public Flux<RagChatVO> simpleChatStream(@Valid @RequestBody RagChatRequestDTO request) {
        return ragService.simpleChatStream(request);
    }

    // ==================== 进阶版 RAG ====================

    /**
     * 进阶版 RAG 同步对话
     */
    @PostMapping("/advanced/chat")
    public Result<RagChatVO> advancedChat(@Valid @RequestBody RagChatRequestDTO request) {
        return Result.success(ragService.advancedChat(request));
    }

    /**
     * 进阶版 RAG 流式对话
     */
    @PostMapping("/advanced/chat/stream")
    public Flux<RagChatVO> advancedChatStream(@Valid @RequestBody RagChatRequestDTO request) {
        return ragService.advancedChatStream(request);
    }

    // ==================== 文档管理 ====================

    /**
     * 获取已加载的文档列表
     */
    @GetMapping("/documents")
    public Result<List<String>> getDocuments() {
        return Result.success(ragService.getDocuments());
    }
}
```

---

## 六、简单版 vs 进阶版对比

| 特性 | 简单版 (QuestionAnswerAdvisor) | 进阶版 (RetrievalAugmentationAdvisor) |
|------|-------------------------------|--------------------------------------|
| 代码复杂度 | 低 | 中 |
| 查询改写 | ❌ 不支持 | ✅ 支持 RewriteQueryTransformer |
| 多查询扩展 | ❌ 不支持 | ✅ 支持 MultiQueryExpander |
| 文档后处理 | ❌ 不支持 | ✅ 支持 DocumentPostProcessor |
| 模块化 | 单一 Advisor | 可组合多个模块 |
| 适用场景 | 快速原型、简单 RAG | 生产级 RAG、复杂场景 |

**进阶版核心优势**：
1. **查询改写**：将用户模糊查询转换为更精确的检索语句
2. **模块化架构**：可根据需求组合不同模块
3. **可扩展性**：易于添加自定义的 QueryTransformer、DocumentRetriever

---

## 七、测试计划

### 7.1 单元测试

| 测试类 | 测试内容 |
|--------|----------|
| RagChatManagerTest | 简单版/进阶版对话、流式对话 |
| RagServiceTest | 参数校验、异常处理 |
| RagControllerTest | API 端点集成测试 |

### 7.2 测试用例示例

```java
@SpringBootTest
@ActiveProfiles("test")
class RagServiceTest {

    @Autowired
    private RagService ragService;

    @Test
    void simpleChat_withValidQuery_shouldReturnResponse() {
        // Given
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("公司的核心业务是什么？");

        // When
        RagChatVO response = ragService.simpleChat(request);

        // Then
        assertNotNull(response.getContent());
        assertNotNull(response.getCreateTime());
    }

    @Test
    void simpleChat_withEmptyQuery_shouldThrowException() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("");

        assertThrows(IllegalArgumentException.class, 
            () -> ragService.simpleChat(request));
    }
}
```

---

## 八、使用示例

### 8.1 启动应用

```bash
# 设置环境变量
export ZHIPUAI_API_KEY=your_api_key

# 启动
cd HelloSpringAI
./mvnw spring-boot:run
```

### 8.2 测试 API

```bash
# 查看已加载文档
curl http://localhost:8080/ai/learn/rag/documents

# 简单版 RAG 对话
curl -X POST http://localhost:8080/ai/learn/rag/simple/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "公司主要做什么业务？"}'

# 进阶版 RAG 对话（查询会被改写优化）
curl -X POST http://localhost:8080/ai/learn/rag/advanced/chat \
  -H "Content-Type: application/json" \
  -d '{"query": "我想了解下你们的技术"}'
```

---

## 九、后续扩展方向

1. **动态文档上传**：支持 API 上传自定义文档
2. **更多 QueryTransformer**：添加 TranslationQueryTransformer、CompressionQueryTransformer
3. **文档后处理**：实现 RankingDocPostProcessor 进行重排序
4. **持久化向量存储**：升级到 PGVector 或 Milvus

---

## 十、风险与注意事项

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| SimpleVectorStore 不持久化 | 重启后数据丢失 | 文档说明了这是学习示例，生产环境需替换 |
| ZhipuAI Embedding API 限制 | 可能影响向量化速度 | 文档说明需要有效的 API Key |
| 内存占用 | 大量文档可能占用内存 | 限制预置文档数量为 5 篇 |

---

## 附录：模块对比总览

| 模块 | 路径前缀 | 核心技术 | 学习目标 |
|------|----------|----------|----------|
| Chat | `/chat` | 基础对话 | Spring AI 基础 |
| MemoryChat | `/learn/memory` | ChatMemory | 对话记忆 |
| ToolChat | `/learn/tool` | ToolCallback | 工具调用 |
| **RagChat** | `/learn/rag` | VectorStore + Advisor | **RAG 检索增强** |
