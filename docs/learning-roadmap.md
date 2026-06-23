# Java Spring Boot 学习路线

> 个人学习路径,配合本仓库 [java-project-1](../) 作为练手沙盒。
> 当前栈基线:**Spring Boot 3.2.12 + Java 17**,2026 年完成阶段 5 时可考虑升到 Boot 3.3/3.4 + Java 21。
> 配套文档:[week-1-tasks.md](./week-1-tasks.md)(第 1 周可落地的具体改造任务)。

---

## 总览

```
阶段 0  前置基础           ────  2-3 周
阶段 1  Spring Boot 核心   ────  3-4 周   ← 当前仓库正好在这里
阶段 2  Web 与数据层        ────  3-4 周
阶段 3  安全 / 缓存 / 消息  ────  3 周
阶段 4  微服务与云原生      ────  4-6 周
阶段 5  进阶与求职冲刺      ────  持续
```

每个阶段的实战目标都在本仓库 (`java-project-1`) 中迭代,git 分支按 `stage-N-xxx` 命名。

---

## 阶段 0｜前置基础(2-3 周)

如果 Java 已熟练可跳过。

| 主题 | 必须掌握 |
|---|---|
| Java SE 核心 | OOP 三大特性、集合(ArrayList/HashMap/ConcurrentHashMap)、泛型、异常体系、IO/NIO |
| Java 8+ 现代特性 | Lambda、Stream、Optional、`CompletableFuture`、`var`、Record、Sealed Class |
| 并发编程 | `Thread`/`Runnable`、`synchronized`、`volatile`、`ReentrantLock`、线程池、AQS 思想 |
| JVM 基础 | 内存区域、GC 基本算法、`jstack`/`jmap`/`Arthas` 入门 |
| 构建与协作 | Maven 生命周期与依赖、Git 基本流、IntelliJ IDEA 快捷键 |

**资源**:《Java 核心技术 卷一/卷二》、《Effective Java 第 3 版》、尚硅谷 宋红康 JVM。

**自检**:能独立写出一个支持文件上传/下载、用线程池处理任务的 CLI 工具。

---

## 阶段 1｜Spring 与 Spring Boot 核心(3-4 周)

> 🟢 **本仓库当前所处阶段**。已具备:三层架构、构造器注入、`@Transactional`、`@Cacheable`、Actuator、Swagger。需要补:`@Valid` 校验、DTO 分层、全局异常处理、统一响应体、Profile 切分、`@ConfigurationProperties`。详见 [week-1-tasks.md](./week-1-tasks.md)。

### 1.1 Spring Framework 思想
- IoC / DI:`@Component`/`@Service`/`@Repository`、`@Autowired` vs **构造器注入**(推荐,本仓库已用 `@RequiredArgsConstructor`)
- Bean 生命周期与作用域(singleton/prototype)
- AOP:`@Aspect`、`JoinPoint`,典型应用(日志、权限、限流)
- 事件机制:`ApplicationEvent` + `@EventListener`

### 1.2 Spring Boot 必会
- **自动装配原理**:`@SpringBootApplication` 拆解、`AutoConfiguration.imports`(Boot 3.x)
- 配置体系:`application.yml`、`@ConfigurationProperties`、Profile 多环境
- Starter 机制与**自定义 Starter**
- Actuator:`/health`、`/metrics`、`/prometheus`,本仓库已配置

### 1.3 阶段产出
将本仓库的 `UserController` 重构为带 DTO + Bean Validation + GlobalExceptionHandler + 统一 `Result<T>` 的标准三层接口。

---

## 阶段 2｜Web 与数据层(3-4 周)

### 2.1 Web 层
- Spring MVC:`@RestController`、参数绑定、`HttpMessageConverter`
- REST API 设计规范、版本化(`/api/v1/...`)
- 全局异常处理:`@RestControllerAdvice` + `@ExceptionHandler`
- 拦截器 vs 过滤器 vs AOP 的取舍
- 跨域、文件上传下载、`WebClient`(替代 RestTemplate)

### 2.2 数据持久化(两条路线都要会)

| 路线 | 适用场景 | 本仓库状态 |
|---|---|---|
| MyBatis-Plus | 国内业务系统主流、SQL 可控 | ❌ 待引入 |
| Spring Data JPA / Hibernate | DDD、原型快速开发 | ✅ 已用 |

配套:
- 连接池:HikariCP(默认,已用)、Druid(国内常用,带监控)
- 事务传播行为、`@Transactional` **失效场景**(自调用、非 public、异常类型)
- MySQL 8:索引、`EXPLAIN`、慢查询定位
- 多数据源 / 分库分表入门:ShardingSphere

### 2.3 阶段产出
- 引入 MyBatis-Plus,与 JPA 并存,做一个 `OrderQueryService` 对比两者的写法差异
- 给当前所有 entity 加 `Flyway` 迁移脚本,关掉 `ddl-auto: update`

---

## 阶段 3｜安全、缓存、消息(3 周)

### 3.1 Spring Security 6.x
- 认证流程(过滤器链)
- Lambda DSL 配置(不再用 `WebSecurityConfigurerAdapter`)
- **JWT 无状态认证 + 刷新令牌**
- OAuth2 Client / Resource Server
- 方法级安全:`@PreAuthorize`

> ⚠️ 本仓库 `User` 实体的 `password` 字段目前是**明文**且直接出现在响应里,这是阶段 3 必须修的安全漏洞。

### 3.2 缓存
- Spring Cache 抽象(本仓库已用)
- Redis:Lettuce(已用)、`RedisTemplate`、Pub/Sub
- 缓存**三大问题**(穿透、击穿、雪崩)解决方案
- Caffeine 本地缓存 + Redis 二级缓存

### 3.3 消息队列
- RabbitMQ(易上手、业务系统首选)
- Kafka(高吞吐、本仓库已集成)
- 至少深入一种,理解**消息可靠性、幂等、顺序、死信、延迟队列**

### 3.4 阶段产出
- 加 Spring Security + JWT,登录/刷新流程跑通
- 给 `getUserById` 做缓存穿透防护(布隆过滤器或缓存空值)
- 给 Kafka 消费端加幂等 + 死信队列

---

## 阶段 4｜微服务与云原生(4-6 周)

国内主流是 Spring Cloud Alibaba。

| 组件 | 主流选择 |
|---|---|
| 注册中心 + 配置中心 | **Nacos** |
| API 网关 | **Spring Cloud Gateway** |
| 服务调用 | **OpenFeign** + LoadBalancer |
| 熔断限流 | **Sentinel** |
| 分布式事务 | **Seata**(AT/TCC 模式) |
| 链路追踪 | SkyWalking 或 Micrometer Tracing + Zipkin |
| 日志聚合 | ELK / Loki + Grafana |

**配套:容器与编排**
- Docker:镜像、Compose、多阶段构建
- Kubernetes 基础:Pod/Deployment/Service/Ingress
- CI/CD:GitHub Actions 或 GitLab CI 跑一遍流水线

### 阶段产出
把当前单体拆为 `user-service`、`order-service`、`product-service`、`gateway`,Docker Compose 一键拉起,再部署到本地 minikube。

---

## 阶段 5｜进阶与求职冲刺(持续)

- **测试**:JUnit 5 + Mockito + `@SpringBootTest` + **Testcontainers**(真实跑 MySQL/Redis)
- **性能**:Arthas、JProfiler、压测(JMeter / wrk),JVM 调优实战
- **架构思想**:DDD 入门、CQRS、事件驱动、整洁架构
- **源码阅读顺序**:
  1. Spring IoC 启动流程
  2. Spring Boot 自动装配
  3. Spring MVC `DispatcherServlet`
  4. MyBatis 执行流程
- **求职**:能讲清楚 1-2 个真实项目,比刷八股文更重要

---

## 学习方法(踩坑总结)

1. **每阶段都做真项目**,本仓库就是沙盒,别只看视频。
2. **优先看官方文档**:[docs.spring.io](https://docs.spring.io) 质量极高,中文教程常滞后版本。
3. **善用 AI 当陪练**,但先自己想 30 分钟再问,否则永远在抄作业。
4. **写笔记 + 画时序图**:Spring 启动流程、请求链路、事务传播,自己画一遍胜过看十遍。
5. **关注版本**:2026 年事实标准是 **Java 21 LTS + Spring Boot 3.3/3.4 + Spring Cloud 2024.x**;升级也是必练技能。

---

## 本仓库现状速查

| 能力 | 已具备 | 待补 |
|---|---|---|
| 三层架构 | ✅ | — |
| 构造器注入 | ✅(`@RequiredArgsConstructor`) | — |
| JPA + 审计字段 | ✅ | Flyway 迁移脚本 |
| Redis + `@Cacheable` | ✅ | 缓存穿透/击穿/雪崩防护 |
| MongoDB / ES / Kafka | ✅ | 消息幂等、死信队列 |
| Swagger / Actuator | ✅ | Prometheus + Grafana 接入 |
| Bean Validation | ❌ 依赖已引入但未用 | **第 1 周补** |
| DTO 分层 | ❌(直接暴露实体,密码泄漏) | **第 1 周补** |
| GlobalExceptionHandler | ❌ | **第 1 周补** |
| 统一响应体 `Result<T>` | ❌ | **第 1 周补** |
| Profile 多环境配置 | ⚠️ 只声明了 `dev`,无 `application-dev.yml` | **第 1 周补** |
| Spring Security / JWT | ❌(密码明文) | 阶段 3 |
| 单元/集成测试 | ⚠️ 只有 1 个用例 | 持续 |
| 微服务拆分 | ❌ | 阶段 4 |

第 1 周从这里开始:[week-1-tasks.md](./week-1-tasks.md)
