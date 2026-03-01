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
        return SimpleVectorStore.builder(embeddingModel).build();
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
