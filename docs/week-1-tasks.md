# 第 1 周任务清单

> 配套 [learning-roadmap.md](./learning-roadmap.md)。
> 目标:把现有 `UserController` / `UserService` 重构成**生产级三层接口**,补齐 Spring Boot 阶段 1 必备的工程化基本功。
> 每个任务都给出**为什么做、改哪里、改完怎么验证**。建议每天 1 个,周末做集成回顾。

---

## 一周路线图

| 天 | 主题 | 产出 |
|---|---|---|
| Day 1 | DTO 分层 + Bean Validation | `UserCreateRequest` / `UserResponse` / `UserUpdateRequest` |
| Day 2 | 统一响应体 `Result<T>` | `common/Result.java` + Controller 全部包一层 |
| Day 3 | 全局异常处理 + 业务异常 | `GlobalExceptionHandler` + `BusinessException` |
| Day 4 | Profile 多环境拆分 + `@ConfigurationProperties` | `application-{dev,test,prod}.yml` |
| Day 5 | 请求/响应日志 AOP + Actuator 自定义 info | `WebLogAspect` |
| Day 6 | 测试补全(JUnit 5 + Mockito + MockMvc) | `UserService` 单测 + Controller 集成测试 |
| Day 7 | 整合回顾 + 提交 PR | git 分支 `stage-1-essentials` |

> 💡 **强烈建议先建分支**:`git checkout -b stage-1-essentials`,每天一个 commit,周末发起 PR 自己 review。

---

## Day 1｜DTO 分层 + Bean Validation

### 为什么做
当前 [UserController.java](../src/main/java/com/example/demo/controller/UserController.java) 直接接收/返回 `User` 实体,有 3 个问题:
1. **密码字段会原样返回给前端**(`User.password` 序列化时不会被过滤)
2. 接口字段与 DB 表结构强耦合,后续表加字段就破坏 API
3. 没有 `@Valid`,前端传 `username = null` 也会一路下沉到 SQL 才报错

### 改哪里
1. 新建 `dto/` 包:
   - `UserCreateRequest` — 含 `@NotBlank`、`@Email`、`@Size`、`@Pattern`
   - `UserUpdateRequest` — 区别于 Create,允许部分字段为 `null`(走 PATCH 语义)
   - `UserResponse` — **不含 password**
2. `UserService` 入口/出口都改成 DTO,实体只在 service 内部用
3. Controller 上 `@RequestBody @Valid UserCreateRequest req`

### 参考片段
```java
// dto/UserCreateRequest.java
@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需 3-50")
    private String username;

    @NotBlank @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;

    private String fullName;
}
```

### 验证
- 启动后 POST `/api/users`,body 传 `{"username":""}`,**应返回 400 而非 500**
- 响应 JSON **不应包含** `password` 字段
- Swagger 文档(`/swagger-ui.html`)能自动显示校验规则

### 延伸思考
- DTO ↔ Entity 转换:手写、MapStruct、还是 BeanUtils?调研后选一个
- `UserResponse` 该用 record 还是 class?Boot 3 + Java 17 已支持 record

---

## Day 2｜统一响应体 `Result<T>`

### 为什么做
当前 controller 直接 `ResponseEntity.ok(user)`,前端拿到的就是裸数据,没有 `code` / `message` / `success`。一旦后续要做埋点、错误码体系、链路追踪,改起来痛苦。国内业务系统几乎都有 `Result<T>` 这一层。

### 改哪里
1. 新建 `common/Result.java`:
```java
@Data
@AllArgsConstructor
public class Result<T> {
    private int code;          // 0=成功,非0=各种业务/系统错
    private String message;
    private T data;
    private long timestamp;
    private String traceId;    // 预留,Day 5 接 MDC

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "OK", data, System.currentTimeMillis(), MDC.get("traceId"));
    }
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null, System.currentTimeMillis(), MDC.get("traceId"));
    }
}
```
2. Controller 全部改成 `return Result.success(...)`
3. 思考:要不要写一个 `ResponseBodyAdvice` 自动包装?(进阶,可选)

### 验证
- 所有接口返回都长这样:
```json
{"code":0,"message":"OK","data":{...},"timestamp":1750000000000,"traceId":"..."}
```

### 陷阱
- `Result` 不要继承 `ResponseEntity`,两者职责不同
- HTTP 状态码 vs 业务 code:建议**业务异常一律 200**,只在系统级错误(网关、超时、5xx)上用 HTTP 错误码

---

## Day 3｜全局异常处理 + 业务异常

### 为什么做
[UserService.java:33](../src/main/java/com/example/demo/service/UserService.java#L33) 用 `throw new RuntimeException("用户不存在: " + id)`,这是反面教材:
- 调用方无法区分这是业务错还是系统错
- 错误码、错误消息、HTTP 状态、日志级别全部纠缠在一起
- README 自己都承认"实际项目可扩展 GlobalExceptionHandler"

### 改哪里
1. 新建 `exception/BusinessException.java`(继承 `RuntimeException`),持有 `code` + `message`
2. 新建 `exception/ErrorCode.java` 枚举:
```java
public enum ErrorCode {
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已被使用"),
    PARAM_INVALID(2001, "参数错误"),
    ...
}
```
3. 新建 `exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`),处理:
   - `BusinessException` → `Result.fail(code, msg)`,日志 `WARN`
   - `MethodArgumentNotValidException` → 拼接所有字段错误,日志 `WARN`
   - `ConstraintViolationException` → 同上(路径参数校验)
   - `DataIntegrityViolationException` → "数据冲突",日志 `ERROR`
   - `Exception` 兜底 → "系统繁忙,请稍后重试",日志 `ERROR` + 堆栈
4. `UserService` 改成 `throw new BusinessException(ErrorCode.USER_NOT_FOUND, id)`

### 验证
- GET `/api/users/99999` 应返回:
```json
{"code":1001,"message":"用户不存在","data":null,...}
```
- POST 传非法 body,应返回 `code=2001` 并列出字段错
- 日志里业务异常**不应打堆栈**(避免污染),系统异常**必须打堆栈**

### 延伸阅读
- 阿里巴巴《Java 开发手册》"异常处理"章节
- 思考:为什么要避免在 service 里返回 `null` 或 `Optional.empty()` 让 controller 判断?

---

## Day 4｜Profile 多环境 + `@ConfigurationProperties`

### 为什么做
当前 [application.yml](../src/main/resources/application.yml) 写死了 `username: root / password: root` 和 `localhost`,生产部署会出问题;还声明了 `profiles.active: dev` 但并没有 `application-dev.yml`。

### 改哪里
1. 把 `application.yml` 拆成三层:
   - `application.yml` — **通用配置**(application name、actuator、logging pattern)
   - `application-dev.yml` — 本地开发(localhost、明文密码 OK)
   - `application-test.yml` — H2 内存库,Redis embedded
   - `application-prod.yml` — 占位符 `${MYSQL_PASSWORD}`,用环境变量注入
2. 启动方式:
   - 本地:`-Dspring.profiles.active=dev`(IDEA Run Configuration)
   - 测试:`@ActiveProfiles("test")`
   - 生产:`SPRING_PROFILES_ACTIVE=prod` env
3. 写一个 `@ConfigurationProperties` 把业务配置类型化:
```java
@ConfigurationProperties(prefix = "app.user")
@Data
public class UserProperties {
    private int defaultPageSize = 20;
    private Duration cacheTtl = Duration.ofMinutes(10);
    private String defaultRole = "USER";
}
```
启动类加 `@EnableConfigurationProperties(UserProperties.class)`,在 `UserService` 注入使用。

### 验证
- 用 `-Dspring.profiles.active=test` 启动,**不连 MySQL/Redis 也能跑起来**
- `/actuator/env` 能看到生效的 profile
- 改 `application-dev.yml` 里 `app.user.default-page-size = 5`,接口分页默认值会变

### 延伸思考
- `application.yml` vs `application.properties` 谁优先?
- Profile + `@Profile("dev")` 注解 + `@Conditional` 三者关系

---

## Day 5｜请求/响应日志 AOP + traceId

### 为什么做
练 AOP 最经典的场景。同时给 Day 2 的 `Result.traceId` 填上真实值,后续接 SkyWalking 就有钩子。

### 改哪里
1. 新建 `aop/WebLogAspect.java`:
```java
@Aspect
@Component
@Slf4j
public class WebLogAspect {
    @Around("execution(* com.example.demo.controller..*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put("traceId", traceId);
        long start = System.currentTimeMillis();
        try {
            log.info("→ {} args={}", pjp.getSignature().toShortString(), Arrays.toString(pjp.getArgs()));
            Object ret = pjp.proceed();
            log.info("← {} cost={}ms", pjp.getSignature().toShortString(), System.currentTimeMillis() - start);
            return ret;
        } finally {
            MDC.remove("traceId");
        }
    }
}
```
2. `logback-spring.xml` 的 pattern 里加 `%X{traceId}`,日志每行带 traceId
3. 把 `application.yml` 里的 `logging.pattern.console` 一起改

### 验证
- 调任一接口,日志能看到 `→ ... args=...` 和 `← ... cost=XXms`
- 日志每行有 16 位 traceId,同一请求多行 log 同一 traceId
- `Result.traceId` 也是这一串(因为都从 MDC 取)

### 进阶
- 给敏感字段(password、token)脱敏:写个 `MaskingArgsFormatter`
- 把同步 AOP 改成异步发送到 Kafka(本仓库已有 Kafka)→ 日志聚合雏形

---

## Day 6｜测试补全

### 为什么做
当前只有 [UserControllerTest.java](../src/test/java/com/example/demo/controller/UserControllerTest.java) 一个 happy path。没测试的代码不敢重构。

### 改哪里
按金字塔补:

1. **单元测试** `UserServiceTest`(纯 Mockito,不启动 Spring):
   - `createUser_whenUsernameExists_throwsBusinessException`
   - `getUserById_whenNotFound_throwsBusinessException`
   - `updateUser_successfullyEvictsCache`(用 `verify(cacheManager)`)
2. **Web 层切片测试** `UserControllerTest`(`@WebMvcTest`,Mock service):
   - 已有的 200 case
   - `createUser_withInvalidBody_returns400AndErrorCode`
   - `getUserById_whenServiceThrowsBusiness_returnsResult`
3. **集成测试** `UserIntegrationTest`(`@SpringBootTest` + `@ActiveProfiles("test")` + H2):
   - 完整跑通 create → get → update → delete
   - 验证缓存确实生效:第二次 get 不打 SQL(`@AutoConfigureTestDatabase` + Hibernate stat)

### 验证
- `mvn test` 全绿
- 覆盖率(用 JaCoCo)`UserService` 行覆盖 ≥ 80%

### 延伸
- 学一下 [Testcontainers](https://www.testcontainers.org/),阶段 2 用真 MySQL/Redis 跑集成测试
- AssertJ 比 JUnit assert 友好,试一下 `assertThat(result.getCode()).isEqualTo(0)`

---

## Day 7｜整合回顾 + 提交 PR

### 做什么
1. 在分支 `stage-1-essentials` 上 `git log --oneline`,确认 6 个 commit 都清晰
2. 自己 review 一遍 diff,把所有 `TODO` / `// FIXME` 处理掉
3. 更新 README:补一段"统一响应体格式"和"错误码表"
4. 发起 PR(就算只有自己也走流程),用 PR description 强迫自己总结**学到了什么**

### PR 描述模板
```markdown
## 改了什么
- [x] DTO 分层(`UserCreateRequest` / `UserResponse`)
- [x] Bean Validation
- [x] 统一响应体 `Result<T>`
- [x] 全局异常处理 + `BusinessException`
- [x] Profile 拆分(dev/test/prod) + `UserProperties`
- [x] 请求日志 AOP + traceId
- [x] 测试覆盖率 `UserService` ≥ 80%

## 学到了什么
1. ...
2. ...

## 还没解决
- 密码明文 → 阶段 3 上 Spring Security 解决
- 其他 Controller(Order/Product/Log/...) 还没按这套规范改 → 留作下周作业
```

### 周末复盘问题清单(写下答案,不要查资料)
1. `@Valid` 和 `@Validated` 有什么区别?校验失败抛的异常分别是什么?
2. `@Transactional` 在哪些情况下会**失效**?至少说出 3 个。
3. `@RestControllerAdvice` 是怎么工作的?它和 `@ControllerAdvice` 区别在哪?
4. 为什么推荐用构造器注入而不是字段注入?
5. Spring Boot 是怎么找到 `application-{profile}.yml` 的?背后哪个类做的?
6. 如果不写 `Result<T>`,直接返回 `ResponseEntity`,会失去哪些能力?

答不上来的题目就是下周的复习方向。

---

## 下周预告

阶段 1 走完后,下周(Week 2)进入**阶段 2 - Web 与数据层**,大概会做:

- 把 [OrderController.java](../src/main/java/com/example/demo/controller/OrderController.java) / [ProductController.java](../src/main/java/com/example/demo/controller/ProductController.java) 等按本周规范改造
- 引入 MyBatis-Plus,做一个 `OrderQueryService` 对比 JPA 写法
- 加 Flyway 迁移脚本,关掉 `ddl-auto: update`
- 学事务传播行为,用 [OrderService.java](../src/main/java/com/example/demo/service/OrderService.java) 跑一个嵌套事务的实验

—— 一周一个里程碑,半年走完整条 [learning-roadmap.md](./learning-roadmap.md)。
