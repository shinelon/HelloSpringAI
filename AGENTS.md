# AGENTS.md - AI Coding Agent Guide

HelloSpringAI: Spring Boot + ZhipuAI integration with chat, session management, and tool calling.

## Tech Stack

Java 17, Spring Boot 3.4.3, Spring AI 1.1.2, Maven 3, H2, Lombok

---

## Build & Test Commands

```bash
mvn clean compile                        # Build
mvn clean package -DskipTests            # Package
mvn spring-boot:run                      # Run (needs ZHIPUAI_API_KEY)
mvn test                                 # All tests
mvn test -Dtest=CalculatorToolTest       # Single class
mvn test -Dtest=ClassName#methodName     # Single method
```
Access: http://localhost:8080/ai

---

## Package Structure

```
com.shinelon.hello/
├── common/{constants,enums,exception,result,utils}/
├── config/controller/dao/manager/
├── model/{dto,vo,entity}/
├── service/impl/
└── tool/
```
- Constants/Utils: final class, private constructor
- Entity: `*DO`, DTO: `*DTO`, VO: `*VO`, DAO: `*Dao`

---

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| Class | PascalCase | `ChatController` |
| Impl | +Impl | `ChatServiceImpl` |
| Constant | UPPER_SNAKE | `CONTENT_MAX_LENGTH` |
| Method | camelCase | `createSession()` |
| Test | `method_scenario_expected` | `chat_newSession_shouldRespond` |

---

## Class Template

```java
/** Description @author shinelon */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleServiceImpl implements ExampleService {
    private final DependencyDao dao;
    @Override
    public Result doSomething(RequestDTO req) { ... }
    private void helperMethod() { ... }
}
```
**Annotations**: `@Slf4j` → `@Service` → `@RequiredArgsConstructor` → `@Transactional`

---

## Import Order

1. `java.*`, `jakarta.*`  2. Third-party  3. `com.shinelon.hello.*`  4. Static

---

## Error Handling

```java
throw new BusinessException(ErrorCodeEnum.NOT_FOUND, "会话不存在");
// ErrorCodeEnum: SUCCESS(200), PARAM_ERROR(400), NOT_FOUND(404), 
//                INTERNAL_ERROR(500), AI_SERVICE_UNAVAILABLE(503)
```

**DTO Validation**:
```java
@NotBlank(message = "消息内容不能为空")
@Size(max = 4000, message = "消息内容不能超过4000字符")
private String content;
```

---

## Logging

```java
log.info("[chat] 请求开始, sessionId={}", sessionId);
log.info("Request: id={}, content={}",
    DesensitizationUtils.maskId(id),
    DesensitizationUtils.truncateAndMask(content, 50));
```
**Levels**: INFO (key points), DEBUG (details), WARN (recoverable), ERROR (fatal)

---

## Testing (Table-Driven Required)

```java
@SpringBootTest @ActiveProfiles("test") @Transactional
class FeatureTest {
    @Autowired FeatureService service;

    record TC(String name, String in, String out) {}

    static Stream<TC> cases() {
        return Stream.of(new TC("c1", "i1", "o1"), new TC("c2", "i2", "o2"));
    }

    @ParameterizedTest(name = "[{index}] {0}") @MethodSource("cases")
    void test(TC tc) { assertEquals(tc.out(), service.do(tc.in())); }
}
```

---

## API Response

```json
{ "code": 200, "message": "success", "data": {...} }
{ "code": 400, "message": "参数错误", "data": null }
```

---

## Configuration

- Server: port `8080`, context-path `/ai`
- ZhipuAI: model `glm-4-flash`, temperature `0.7`
- Required env: `ZHIPUAI_API_KEY`

---

## Key Rules

1. **TDD**: Write failing tests first
2. **No mocks preferred**: Real dependencies in tests
3. **Constructor injection only**: No field injection
4. **Explicit error handling**: Never silent catches
5. **Windows**: Use `mvn.cmd`, free port 8080 after use
