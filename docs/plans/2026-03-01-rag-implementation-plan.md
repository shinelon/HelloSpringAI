# Spring AI RAG 学习示例实现计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在 HelloSpringAI 项目中新增 RAG 学习示例模块，包含简单版（QuestionAnswerAdvisor）和进阶版（RetrievalAugmentationAdvisor）两种实现。

**Architecture:** 采用标准分层架构（Controller -> Service -> Manager），与现有 MemoryChat、ToolChat 模块保持一致。使用 SimpleVectorStore 内存向量数据库，启动时预置 5 篇示例文档。

**Tech Stack:** Spring Boot 3.4.3, Spring AI 1.1.2, SimpleVectorStore, QuestionAnswerAdvisor, RetrievalAugmentationAdvisor, RewriteQueryTransformer

---

## Task 1: 添加 Maven 依赖

**Files:**
- Modify: `HelloSpringAI/pom.xml`

**Step 1: 添加 spring-ai-advisors-vector-store 依赖**

在 `pom.xml` 的 `<!-- Test Dependencies -->` 注释之前添加：

```xml
        <!-- Spring AI RAG Advisors -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-advisors-vector-store</artifactId>
            <version>${spring-ai.version}</version>
        </dependency>

        <!-- Spring AI RAG Modules -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-rag</artifactId>
            <version>${spring-ai.version}</version>
        </dependency>
```

**Step 2: 验证依赖添加成功**

Run: `cd HelloSpringAI && mvn dependency:resolve -q`
Expected: BUILD SUCCESS (无错误)

**Step 3: Commit**

```bash
cd HelloSpringAI && git add pom.xml && git commit -m "feat: add spring-ai RAG dependencies"
```

---

## Task 2: 创建请求和响应模型

**Files:**
- Create: `HelloSpringAI/src/main/java/com/shinelon/hello/model/dto/RagChatRequestDTO.java`
- Create: `HelloSpringAI/src/main/java/com/shinelon/hello/model/vo/RagChatVO.java`

**Step 1: 创建 RagChatRequestDTO**

```java
package com.shinelon.hello.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RAG 对话请求 DTO
 *
 * @author shinelon
 */
@Data
public class RagChatRequestDTO {

    /**
     * 用户查询内容
     */
    @NotBlank(message = "查询内容不能为空")
    @Size(max = 2000, message = "查询内容不能超过2000字符")
    private String query;
}
```

**Step 2: 创建 RagChatVO**

```java
package com.shinelon.hello.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG 对话响应 VO
 *
 * @author shinelon
 */
@Data
@Builder
public class RagChatVO {

    /**
     * AI 回复内容
     */
    private String content;

    /**
     * 检索到的文档数量
     */
    private Integer sourceCount;

    /**
     * 文档来源列表
     */
    private List<String> sources;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
```

**Step 3: 验证编译**

Run: `cd HelloSpringAI && mvn compile -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
cd HelloSpringAI && git add src/main/java/com/shinelon/hello/model/dto/RagChatRequestDTO.java src/main/java/com/shinelon/hello/model/vo/RagChatVO.java && git commit -m "feat: add RagChatRequestDTO and RagChatVO"
```

---

## Task 3: 创建 RAG 配置类

**Files:**
- Create: `HelloSpringAI/src/main/java/com/shinelon/hello/config/RagConfig.java`

**Step 1: 创建 RagConfig**

```java
package com.shinelon.hello.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;

/**
 * RAG 配置类
 * 配置向量存储和示例文档
 *
 * @author shinelon
 */
@Slf4j
@Configuration
public class RagConfig {

    /**
     * 创建内存向量存储
     *
     * @param embeddingModel 嵌入模型
     * @return VectorStore
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        log.info("[RAG] 初始化 SimpleVectorStore");
        return new SimpleVectorStore(embeddingModel);
    }

    /**
     * 初始化示例文档数据
     *
     * @param vectorStore 向量存储
     * @return CommandLineRunner
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
     *
     * @return 文档列表
     */
    private List<Document> createSampleDocuments() {
        return List.of(
                new Document(
                        "【公司介绍】\n智云科技成立于2020年，是一家专注于企业级AI解决方案的高科技公司。" +
                        "公司总部位于北京，在上海、深圳设有研发中心。" +
                        "我们的使命是让AI技术惠及每一家企业，愿景是成为最受信赖的AI技术服务商。",
                        Map.of("source", "公司介绍", "category", "about")),
                new Document(
                        "【产品说明】\n主要产品包括：\n" +
                        "1. 智能客服系统：7x24小时自动应答，支持多轮对话，可对接企业知识库\n" +
                        "2. 文档智能处理：自动提取、分类、摘要，支持PDF、Word等格式\n" +
                        "3. 知识库管理：企业知识统一管理，智能检索，版本控制",
                        Map.of("source", "产品说明", "category", "product")),
                new Document(
                        "【技术架构】\n系统采用微服务架构，主要技术栈包括：\n" +
                        "- 后端框架：Spring Boot 3.x + Spring AI\n" +
                        "- 向量数据库：PGVector / Milvus\n" +
                        "- 消息队列：RabbitMQ / Kafka\n" +
                        "- 容器化部署：Kubernetes + Docker\n" +
                        "- 监控告警：Prometheus + Grafana",
                        Map.of("source", "技术架构", "category", "tech")),
                new Document(
                        "【常见问题】\n" +
                        "Q: 如何获取API密钥？\n" +
                        "A: 登录控制台，在「设置-密钥管理」中创建新密钥\n\n" +
                        "Q: 支持哪些大模型？\n" +
                        "A: 目前支持智谱AI、OpenAI、Azure OpenAI、通义千问等\n\n" +
                        "Q: 有调用频率限制吗？\n" +
                        "A: 不同套餐有不同的调用限制，详情请查看控制台",
                        Map.of("source", "常见问题", "category", "faq")),
                new Document(
                        "【联系方式】\n" +
                        "客服电话：400-888-8888（工作日 9:00-21:00）\n" +
                        "技术支持邮箱：support@example.com\n" +
                        "商务合作邮箱：business@example.com\n" +
                        "公司地址：北京市海淀区中关村大街1号科技大厦18层\n" +
                        "官方网站：https://www.example.com",
                        Map.of("source", "联系方式", "category", "contact"))
        );
    }
}
```

**Step 2: 验证编译**

Run: `cd HelloSpringAI && mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
cd HelloSpringAI && git add src/main/java/com/shinelon/hello/config/RagConfig.java && git commit -m "feat: add RagConfig with sample documents"
```

---

## Task 4: 创建 RAG 对话管理器

**Files:**
- Create: `HelloSpringAI/src/main/java/com/shinelon/hello/manager/RagChatManager.java`

**Step 1: 创建 RagChatManager**

```java
package com.shinelon.hello.manager;

import com.shinelon.hello.common.enums.ErrorCodeEnum;
import com.shinelon.hello.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.rag.transformation.query.rewrite.RewriteQueryTransformer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 对话管理器
 * 封装简单版和进阶版 RAG 对话能力
 *
 * @author shinelon
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagChatManager {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;

    private ChatClient simpleRagClient;
    private ChatClient advancedRagClient;

    private static final List<String> DOCUMENT_SOURCES = List.of(
            "公司介绍", "产品说明", "技术架构", "常见问题", "联系方式"
    );

    @PostConstruct
    public void init() {
        initSimpleRagClient();
        initAdvancedRagClient();
        log.info("[RAG] RagChatManager 初始化完成");
    }

    /**
     * 初始化简单版 RAG 客户端
     * 使用 QuestionAnswerAdvisor
     */
    private void initSimpleRagClient() {
        this.simpleRagClient = ChatClient.builder(chatClientBuilder.build().mutate())
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.5)
                                .topK(3)
                                .build())
                        .build())
                .build();
        log.info("[RAG] 简单版 RAG 客户端初始化完成 (QuestionAnswerAdvisor)");
    }

    /**
     * 初始化进阶版 RAG 客户端
     * 使用 RetrievalAugmentationAdvisor + 查询改写
     */
    private void initAdvancedRagClient() {
        var advancedAdvisor = RetrievalAugmentationAdvisor.builder()
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
        log.info("[RAG] 进阶版 RAG 客户端初始化完成 (RetrievalAugmentationAdvisor + RewriteQueryTransformer)");
    }

    /**
     * 简单版 RAG 同步对话
     *
     * @param query 用户查询
     * @return AI 回复
     */
    public String simpleChat(String query) {
        validateInput(query);
        log.debug("[RAG-Simple] 查询: {}", truncate(query, 100));

        try {
            return simpleRagClient.prompt()
                    .user(query)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[RAG-Simple] 调用失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "RAG服务调用失败", e);
        }
    }

    /**
     * 简单版 RAG 流式对话
     *
     * @param query 用户查询
     * @return AI 回复流
     */
    public Flux<String> simpleChatStream(String query) {
        validateInput(query);
        log.debug("[RAG-Simple-Stream] 查询: {}", truncate(query, 100));

        try {
            return simpleRagClient.prompt()
                    .user(query)
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("[RAG-Simple-Stream] 调用失败: {}", e.getMessage(), e);
            return Flux.error(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "RAG服务调用失败", e));
        }
    }

    /**
     * 进阶版 RAG 同步对话（含查询改写）
     *
     * @param query 用户查询
     * @return AI 回复
     */
    public String advancedChat(String query) {
        validateInput(query);
        log.debug("[RAG-Advanced] 查询: {}", truncate(query, 100));

        try {
            return advancedRagClient.prompt()
                    .user(query)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("[RAG-Advanced] 调用失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "RAG服务调用失败", e);
        }
    }

    /**
     * 进阶版 RAG 流式对话
     *
     * @param query 用户查询
     * @return AI 回复流
     */
    public Flux<String> advancedChatStream(String query) {
        validateInput(query);
        log.debug("[RAG-Advanced-Stream] 查询: {}", truncate(query, 100));

        try {
            return advancedRagClient.prompt()
                    .user(query)
                    .stream()
                    .content();
        } catch (Exception e) {
            log.error("[RAG-Advanced-Stream] 调用失败: {}", e.getMessage(), e);
            return Flux.error(new BusinessException(ErrorCodeEnum.AI_SERVICE_UNAVAILABLE, "RAG服务调用失败", e));
        }
    }

    /**
     * 获取已加载的文档来源列表
     *
     * @return 文档来源列表
     */
    public List<String> getDocumentSources() {
        return DOCUMENT_SOURCES;
    }

    /**
     * 验证输入
     */
    private void validateInput(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("查询内容不能为空");
        }
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
```

**Step 2: 验证编译**

Run: `cd HelloSpringAI && mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
cd HelloSpringAI && git add src/main/java/com/shinelon/hello/manager/RagChatManager.java && git commit -m "feat: add RagChatManager with simple and advanced RAG support"
```

---

## Task 5: 创建服务接口和实现

**Files:**
- Create: `HelloSpringAI/src/main/java/com/shinelon/hello/service/RagService.java`
- Create: `HelloSpringAI/src/main/java/com/shinelon/hello/service/impl/RagServiceImpl.java`

**Step 1: 创建 RagService 接口**

```java
package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.RagChatRequestDTO;
import com.shinelon.hello.model.vo.RagChatVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 对话服务接口
 *
 * @author shinelon
 */
public interface RagService {

    /**
     * 简单版 RAG 同步对话
     *
     * @param request 请求
     * @return 响应
     */
    RagChatVO simpleChat(RagChatRequestDTO request);

    /**
     * 简单版 RAG 流式对话
     *
     * @param request 请求
     * @return 响应流
     */
    Flux<RagChatVO> simpleChatStream(RagChatRequestDTO request);

    /**
     * 进阶版 RAG 同步对话
     *
     * @param request 请求
     * @return 响应
     */
    RagChatVO advancedChat(RagChatRequestDTO request);

    /**
     * 进阶版 RAG 流式对话
     *
     * @param request 请求
     * @return 响应流
     */
    Flux<RagChatVO> advancedChatStream(RagChatRequestDTO request);

    /**
     * 获取已加载的文档列表
     *
     * @return 文档来源列表
     */
    List<String> getDocuments();
}
```

**Step 2: 创建 RagServiceImpl**

```java
package com.shinelon.hello.service.impl;

import com.shinelon.hello.common.utils.DesensitizationUtils;
import com.shinelon.hello.manager.RagChatManager;
import com.shinelon.hello.model.dto.RagChatRequestDTO;
import com.shinelon.hello.model.vo.RagChatVO;
import com.shinelon.hello.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RAG 对话服务实现
 *
 * @author shinelon
 */
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

        log.info("[simpleChat] 简单版RAG对话完成, 响应长度={}", content.length());

        return RagChatVO.builder()
                .content(content)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Flux<RagChatVO> simpleChatStream(RagChatRequestDTO request) {
        validateRequest(request);

        log.info("[simpleChatStream] 简单版RAG流式对话开始, query={}",
                DesensitizationUtils.truncateAndMask(request.getQuery(), 50));

        return ragChatManager.simpleChatStream(request.getQuery())
                .map(chunk -> RagChatVO.builder()
                        .content(chunk)
                        .createTime(LocalDateTime.now())
                        .build())
                .doOnError(e -> log.error("[simpleChatStream] 流式对话错误: {}", e.getMessage(), e));
    }

    @Override
    public RagChatVO advancedChat(RagChatRequestDTO request) {
        validateRequest(request);

        log.info("[advancedChat] 进阶版RAG对话开始, query={}",
                DesensitizationUtils.truncateAndMask(request.getQuery(), 50));

        String content = ragChatManager.advancedChat(request.getQuery());

        log.info("[advancedChat] 进阶版RAG对话完成, 响应长度={}", content.length());

        return RagChatVO.builder()
                .content(content)
                .createTime(LocalDateTime.now())
                .build();
    }

    @Override
    public Flux<RagChatVO> advancedChatStream(RagChatRequestDTO request) {
        validateRequest(request);

        log.info("[advancedChatStream] 进阶版RAG流式对话开始, query={}",
                DesensitizationUtils.truncateAndMask(request.getQuery(), 50));

        return ragChatManager.advancedChatStream(request.getQuery())
                .map(chunk -> RagChatVO.builder()
                        .content(chunk)
                        .createTime(LocalDateTime.now())
                        .build())
                .doOnError(e -> log.error("[advancedChatStream] 流式对话错误: {}", e.getMessage(), e));
    }

    @Override
    public List<String> getDocuments() {
        return ragChatManager.getDocumentSources();
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(RagChatRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求不能为空");
        }
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("查询内容不能为空");
        }
    }
}
```

**Step 3: 验证编译**

Run: `cd HelloSpringAI && mvn compile -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
cd HelloSpringAI && git add src/main/java/com/shinelon/hello/service/RagService.java src/main/java/com/shinelon/hello/service/impl/RagServiceImpl.java && git commit -m "feat: add RagService and RagServiceImpl"
```

---

## Task 6: 创建 REST 控制器

**Files:**
- Create: `HelloSpringAI/src/main/java/com/shinelon/hello/controller/RagController.java`

**Step 1: 创建 RagController**

```java
package com.shinelon.hello.controller;

import com.shinelon.hello.common.result.Result;
import com.shinelon.hello.model.dto.RagChatRequestDTO;
import com.shinelon.hello.model.vo.RagChatVO;
import com.shinelon.hello.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 对话控制器
 * 提供简单版和进阶版 RAG 对话 API
 *
 * @author shinelon
 */
@Slf4j
@RestController
@RequestMapping("/learn/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    // ==================== 简单版 RAG ====================

    /**
     * 简单版 RAG 同步对话
     * 使用 QuestionAnswerAdvisor
     */
    @PostMapping("/simple/chat")
    public Result<RagChatVO> simpleChat(@Valid @RequestBody RagChatRequestDTO request) {
        log.info("[simpleChat] API调用, query={}", request.getQuery());
        return Result.success(ragService.simpleChat(request));
    }

    /**
     * 简单版 RAG 流式对话 (SSE)
     * 使用 QuestionAnswerAdvisor
     */
    @PostMapping("/simple/chat/stream")
    public Flux<RagChatVO> simpleChatStream(@Valid @RequestBody RagChatRequestDTO request) {
        log.info("[simpleChatStream] API调用, query={}", request.getQuery());
        return ragService.simpleChatStream(request);
    }

    // ==================== 进阶版 RAG ====================

    /**
     * 进阶版 RAG 同步对话
     * 使用 RetrievalAugmentationAdvisor + 查询改写
     */
    @PostMapping("/advanced/chat")
    public Result<RagChatVO> advancedChat(@Valid @RequestBody RagChatRequestDTO request) {
        log.info("[advancedChat] API调用, query={}", request.getQuery());
        return Result.success(ragService.advancedChat(request));
    }

    /**
     * 进阶版 RAG 流式对话 (SSE)
     * 使用 RetrievalAugmentationAdvisor + 查询改写
     */
    @PostMapping("/advanced/chat/stream")
    public Flux<RagChatVO> advancedChatStream(@Valid @RequestBody RagChatRequestDTO request) {
        log.info("[advancedChatStream] API调用, query={}", request.getQuery());
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

**Step 2: 验证编译**

Run: `cd HelloSpringAI && mvn compile -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
cd HelloSpringAI && git add src/main/java/com/shinelon/hello/controller/RagController.java && git commit -m "feat: add RagController with simple and advanced RAG endpoints"
```

---

## Task 7: 创建单元测试

**Files:**
- Create: `HelloSpringAI/src/test/java/com/shinelon/hello/manager/RagChatManagerTest.java`
- Create: `HelloSpringAI/src/test/java/com/shinelon/hello/service/RagServiceTest.java`
- Create: `HelloSpringAI/src/test/java/com/shinelon/hello/controller/RagControllerTest.java`

**Step 1: 创建 RagChatManagerTest**

```java
package com.shinelon.hello.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RagChatManager 单元测试
 *
 * @author shinelon
 */
@SpringBootTest
@ActiveProfiles("test")
class RagChatManagerTest {

    @Autowired
    private RagChatManager ragChatManager;

    @Test
    void getDocumentSources_shouldReturnFiveDocuments() {
        List<String> sources = ragChatManager.getDocumentSources();
        
        assertNotNull(sources);
        assertEquals(5, sources.size());
        assertTrue(sources.contains("公司介绍"));
        assertTrue(sources.contains("产品说明"));
        assertTrue(sources.contains("技术架构"));
        assertTrue(sources.contains("常见问题"));
        assertTrue(sources.contains("联系方式"));
    }

    @Test
    void simpleChat_withValidQuery_shouldReturnResponse() {
        String response = ragChatManager.simpleChat("公司主要做什么业务？");
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void simpleChat_withEmptyQuery_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ragChatManager.simpleChat("");
        });
    }

    @Test
    void simpleChat_withNullQuery_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ragChatManager.simpleChat(null);
        });
    }

    @Test
    void advancedChat_withValidQuery_shouldReturnResponse() {
        String response = ragChatManager.advancedChat("我想了解你们的技术架构");
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void advancedChat_withEmptyQuery_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ragChatManager.advancedChat("");
        });
    }
}
```

**Step 2: 创建 RagServiceTest**

```java
package com.shinelon.hello.service;

import com.shinelon.hello.model.dto.RagChatRequestDTO;
import com.shinelon.hello.model.vo.RagChatVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RagService 单元测试
 *
 * @author shinelon
 */
@SpringBootTest
@ActiveProfiles("test")
class RagServiceTest {

    @Autowired
    private RagService ragService;

    record TestCase(String name, String query, boolean shouldSucceed) {}

    static Stream<TestCase> queryTestCases() {
        return Stream.of(
                new TestCase("公司业务查询", "公司的核心业务是什么？", true),
                new TestCase("产品查询", "你们有哪些产品？", true),
                new TestCase("技术查询", "技术架构是怎样的？", true),
                new TestCase("联系方式查询", "怎么联系客服？", true),
                new TestCase("常见问题", "如何获取API密钥？", true)
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("queryTestCases")
    void simpleChat_withVariousQueries(TestCase tc) {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery(tc.query());

        RagChatVO response = ragService.simpleChat(request);

        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
        assertNotNull(response.getCreateTime());
    }

    @Test
    void simpleChat_withNullRequest_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ragService.simpleChat(null);
        });
    }

    @Test
    void simpleChat_withEmptyQuery_shouldThrowException() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("");

        assertThrows(IllegalArgumentException.class, () -> {
            ragService.simpleChat(request);
        });
    }

    @Test
    void advancedChat_withValidQuery_shouldReturnResponse() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("我想了解一下你们公司");

        RagChatVO response = ragService.advancedChat(request);

        assertNotNull(response);
        assertNotNull(response.getContent());
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    void getDocuments_shouldReturnFiveDocuments() {
        List<String> documents = ragService.getDocuments();

        assertNotNull(documents);
        assertEquals(5, documents.size());
    }

    @Test
    void simpleChatStream_shouldReturnFlux() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("介绍一下公司");

        List<RagChatVO> results = ragService.simpleChatStream(request)
                .take(3)
                .collectList()
                .block();

        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void advancedChatStream_shouldReturnFlux() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("技术栈是什么");

        List<RagChatVO> results = ragService.advancedChatStream(request)
                .take(3)
                .collectList()
                .block();

        assertNotNull(results);
        assertFalse(results.isEmpty());
    }
}
```

**Step 3: 创建 RagControllerTest**

```java
package com.shinelon.hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shinelon.hello.model.dto.RagChatRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RagController 集成测试
 *
 * @author shinelon
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getDocuments_shouldReturnDocumentList() throws Exception {
        mockMvc.perform(get("/learn/rag/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data", containsInAnyOrder(
                        "公司介绍", "产品说明", "技术架构", "常见问题", "联系方式"
                )));
    }

    @Test
    void simpleChat_withValidRequest_shouldReturnResponse() throws Exception {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("公司主要做什么业务？");

        mockMvc.perform(post("/learn/rag/simple/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content", not(emptyString())))
                .andExpect(jsonPath("$.data.createTime", notNullValue()));
    }

    @Test
    void simpleChat_withEmptyQuery_shouldReturnBadRequest() throws Exception {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("");

        mockMvc.perform(post("/learn/rag/simple/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void advancedChat_withValidRequest_shouldReturnResponse() throws Exception {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("我想了解一下你们的技术");

        mockMvc.perform(post("/learn/rag/advanced/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content", not(emptyString())));
    }

    @Test
    void advancedChat_withEmptyQuery_shouldReturnBadRequest() throws Exception {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuery("");

        mockMvc.perform(post("/learn/rag/advanced/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
```

**Step 4: 运行测试**

Run: `cd HelloSpringAI && mvn test -Dtest="RagChatManagerTest,RagServiceTest,RagControllerTest" -q`
Expected: Tests run: X, Failures: 0, Errors: 0

**Step 5: Commit**

```bash
cd HelloSpringAI && git add src/test/java/com/shinelon/hello/manager/RagChatManagerTest.java src/test/java/com/shinelon/hello/service/RagServiceTest.java src/test/java/com/shinelon/hello/controller/RagControllerTest.java && git commit -m "test: add unit tests for RAG module"
```

---

## Task 8: 更新 README 文档

**Files:**
- Modify: `HelloSpringAI/README.md`

**Step 1: 在 README.md 中添加 RAG 模块说明**

在 `### Tool Calling (工具调用)` 部分之后，添加：

```markdown
### RAG (检索增强生成)

```bash
# 获取已加载的文档列表
GET /ai/learn/rag/documents

# 简单版 RAG 对话
POST /ai/learn/rag/simple/chat
Content-Type: application/json

{
  "query": "公司的核心业务是什么？"
}

# 简单版 RAG 流式对话
POST /ai/learn/rag/simple/chat/stream
Content-Type: application/json

{
  "query": "介绍一下你们的产品"
}

# 进阶版 RAG 对话（含查询改写）
POST /ai/learn/rag/advanced/chat
Content-Type: application/json

{
  "query": "我想了解一下你们公司主要是做什么的"
}

# 进阶版 RAG 流式对话
POST /ai/learn/rag/advanced/chat/stream
Content-Type: application/json

{
  "query": "技术架构是怎样的？"
}
```

#### RAG 版本对比

| 版本 | 技术方案 | 特点 |
|------|----------|------|
| 简单版 | QuestionAnswerAdvisor | 开箱即用，代码简洁 |
| 进阶版 | RetrievalAugmentationAdvisor | 支持查询改写，模块化架构 |

#### 预置文档

| 文档 | 内容概要 |
|------|----------|
| 公司介绍 | 企业背景、核心业务、发展历程 |
| 产品说明 | 主要产品、功能特性 |
| 技术架构 | 系统架构、技术栈 |
| 常见问题 | FAQ、故障排查 |
| 联系方式 | 客服电话、邮箱、地址 |
```

**Step 2: 更新功能特性列表**

在 `## 功能特性` 部分，添加：

```markdown
- RAG 检索增强生成
  - 简单版
  - 进阶版 (RetrievalAugmentationAdvisor + 查询改写)
```

**Step 3: Commit**

```bash
cd HelloSpringAI && git add README.md && git commit -m "docs: add RAG module documentation to README"
```

---

## Task 9: 最终验证

**Step 1: 运行所有测试**

Run: `cd HelloSpringAI && mvn clean test -q`
Expected: BUILD SUCCESS, Tests run: X, Failures: 0, Errors: 0

**Step 2: 打包验证**

Run: `cd HelloSpringAI && mvn clean package -DskipTests -q`
Expected: BUILD SUCCESS

**Step 3: 查看最终 git 状态**

Run: `cd HelloSpringAI && git status && git log --oneline -10`
Expected: 工作区干净，所有提交记录可见

---

## 完成检查清单

- [ ] Maven 依赖已添加
- [ ] RagConfig 配置类已创建
- [ ] RagChatRequestDTO 和 RagChatVO 已创建
- [ ] RagChatManager 已创建（简单版 + 进阶版）
- [ ] RagService 接口和实现已创建
- [ ] RagController 已创建
- [ ] 单元测试全部通过
- [ ] README 已更新
- [ ] 所有代码已提交到 git
