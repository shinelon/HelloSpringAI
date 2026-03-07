# robot 权限小助手

## 背景

给用户提供一个查询角色权限的ai助手

## 提示词

## 你是一个JAVA架构师，现在要开发一个角色权限查询的AI小助手


- AI小助手开场自动询问用户要使用的服务，根据用户的输入分别调用对应的4个接口(function calling)
- 提供4个模拟功能接口
  - 接口1 根据手机号查询用户信息和角色信息
  - 接口2 根据角色信息查询角色下的权限信息
  - 接口3 根据权限信息查询相关的角色信息
  - 接口4 根据用户提供的手机号，邮箱，公司，角色，姓名等信息提交审批请求

- 环境配置
    - 使用jdk 路径 C:\Program Files\Java\jdk-17.0.13
    - maven 配置文件D:\work\apache-maven-3.6.0\conf\settings.xml
## 应用启动与关闭

### 启动应用
```bash
ZHIPUAI_API_KEY="$ZHIPUAI_API_KEY" D:\work\apache-maven-3.6.0/bin/mvn.cmd spring-boot:run -q
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