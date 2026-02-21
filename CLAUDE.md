# ==================================
# HelloSpringAI 项目上下文总入口
# ==================================

# --- 核心原则导入 (最高优先级) ---
# 明确导入项目宪法，确保AI在思考任何问题前，都已加载核心原则。
@./constitution.md

# --- 核心使命与角色设定 ---
你是一个资深的JAVA语言工程师，正在协助我开发一个名为 "HelloSpringAI" 的项目。
你的所有行动都必须严格遵守上面导入的项目宪法。

---
## 1. 技术栈与环境
- **语言**: JAVA (版本 = 17)
  - 使用jdk8 路径 C:\Program Files\Java\jdk-17.0.13
  - 项目的编码格式为utf-8
  - 包名要求：com.shinelon.hello
  - content-path要求：/ai
  - 有统一异常处理
  - 使用 springAI 1.1.2版本
  - 使用 spring-ai-starter-model-zhipuai
- **构建与测试**:
  - 使用 maven3 路径D:\dev_soft\apache-maven-3.6.3

---
## 2. Git与版本控制
- **Commit Message规范**: 严格遵循 Conventional Commits 规范。
  - 格式: `<type>(<scope>): <subject>`
  - 当被要求生成commit message时，必须遵循此格式。

## 3.关键配置
- windows的环境变量ZHIPUAI_API_KEY 中包含 zhipuai的APIkey
- 编码环境为window环境，要注意文件路径

## 4.编码规范
- 编码规范要满足\.claude\skills\java-development-manual的相关规范要求


---
## 3. AI协作指令
- **当被要求添加新功能时**: 你的第一步应该是先用`@`指令阅读当前项目下的相关包，并对照项目宪法，然后再提出你的计划。
- **当被要求编写测试时**: 你应该优先编写**表格驱动测试（Table-Driven Tests）**。
- **当被要求构建项目时**: 你应该优先提议使用`D:\dev_soft\apache-maven-3.6.3`
- **当被要求实际操作时**：你应该优先判断需要申请哪些权限，先申请权限后进行操作

---
## 5. 应用启动与关闭

### 启动应用
```bash
ZHIPUAI_API_KEY="$ZHIPUAI_API_KEY" D:/dev_soft/apache-maven-3.6.3/bin/mvn.cmd spring-boot:run -q
```
> 说明：显式传递环境变量确保 maven 子进程正确继承

应用启动后访问: http://localhost:8080/ai

### 关闭应用
查找并终止Java进程:
```bash
# 查找占用8080端口的进程
netstat -ano | findstr :8080

# 终止进程 (替换<PID>为实际进程ID)
taskkill //F //PID <PID>
```

### 关键配置
- **ZHIPUAI_API_KEY**: 已在Windows环境变量中配置
- **端口**: 8080
- **Context Path**: /ai

### 使用规范
- **重要**: 每次启动 Java 进程使用后，都需要关闭进程，释放端口和系统资源
