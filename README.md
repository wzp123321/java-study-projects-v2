# Spring Boot 项目 - 常用中间件示例

基于 Spring Boot 3.2.0 + Java 17 的演示项目，集成了多种常用中间件。

> 📚 **学习路径**:本仓库同时作为个人 Spring Boot 学习沙盒,完整路线见 [docs/learning-roadmap.md](docs/learning-roadmap.md),第 1 周可落地任务见 [docs/week-1-tasks.md](docs/week-1-tasks.md)。

## 技术栈

| 中间件 | 用途 | Spring Boot Starter |
|--------|------|---------------------|
| **MySQL** | 关系型数据库 | `spring-boot-starter-data-jpa` |
| **Redis** | 缓存 / 分布式缓存 | `spring-boot-starter-data-redis` |
| **MongoDB** | 文档数据库 | `spring-boot-starter-data-mongodb` |
| **Elasticsearch** | 搜索引擎 | `spring-boot-starter-data-elasticsearch` |
| **Kafka** | 消息队列 | `spring-kafka` |
| **HikariCP** | 数据库连接池 | 内置 |
| **Swagger/OpenAPI** | API 文档 | `springdoc-openapi` |
| **Actuator** | 监控端点 | `spring-boot-starter-actuator` |

## 项目结构

```
java-project-1/
├── pom.xml                          # Maven 配置
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── DemoApplication.java           # 启动类
│   │   │   ├── config/                        # 配置类
│   │   │   │   ├── RedisConfig.java           # Redis 序列化配置
│   │   │   │   ├── KafkaConfig.java           # Kafka Topic 配置
│   │   │   │   ├── ElasticsearchConfig.java   # ES 客户端配置
│   │   │   │   └── MongoConfig.java           # MongoDB 审计配置
│   │   │   ├── entity/                        # 实体类
│   │   │   │   ├── User.java                  # MySQL JPA 实体
│   │   │   │   ├── Order.java                 # MySQL JPA 实体
│   │   │   │   ├── LogEntry.java              # MongoDB 文档
│   │   │   │   └── Product.java               # ES 文档
│   │   │   ├── repository/                    # 数据访问层
│   │   │   │   ├── UserRepository.java        # JPA
│   │   │   │   ├── OrderRepository.java       # JPA
│   │   │   │   ├── LogRepository.java         # MongoDB
│   │   │   │   └── ProductRepository.java     # ES
│   │   │   ├── service/                       # 业务逻辑层
│   │   │   │   ├── UserService.java           # 用户服务（含缓存）
│   │   │   │   ├── OrderService.java          # 订单服务
│   │   │   │   ├── LogService.java            # 日志服务（MongoDB）
│   │   │   │   ├── ProductService.java        # 商品服务（ES）
│   │   │   │   ├── RedisService.java          # Redis 操作封装
│   │   │   │   └── KafkaProducerService.java  # Kafka 生产者
│   │   │   ├── controller/                    # 控制层（REST API）
│   │   │   │   ├── UserController.java        # 用户 CRUD
│   │   │   │   ├── OrderController.java       # 订单 CRUD
│   │   │   │   ├── LogController.java         # 日志 CRUD
│   │   │   │   ├── ProductController.java     # 商品搜索
│   │   │   │   ├── RedisController.java       # Redis 操作 API
│   │   │   │   └── KafkaController.java       # 消息发送 API
│   │   │   └── listener/                      # 监听器
│   │   │       └── KafkaListeners.java        # Kafka 消费者
│   │   └── resources/
│   │       └── application.yml                # 主配置文件
│   └── test/                                  # 测试代码
└── README.md
```

## 快速开始

### 1. 环境准备

确保本地已安装并运行以下服务：

- **Java 17+**
- **MySQL 8.0+** (端口 3306，数据库 `demo_db`，用户/密码 `root`/`root`)
- **Redis** (端口 6379)
- **MongoDB** (端口 27017)
- **Elasticsearch 8.x** (端口 9200)
- **Kafka** (端口 9092，需同时运行 Zookeeper)

### 2. 构建项目

```bash
# 使用 Maven 构建
mvn clean package -DskipTests

# 或者直接运行
mvn spring-boot:run
```

### 3. 访问服务

启动后可通过以下地址访问：

| 服务 | 地址 |
|------|------|
| Swagger UI (API 文档) | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| Actuator 健康检查 | http://localhost:8080/actuator/health |
| Actuator 指标 | http://localhost:8080/actuator/metrics |

## API 接口说明

### 用户管理 (MySQL + JPA + Redis 缓存)

```
POST   /api/users              # 创建用户
GET    /api/users/{id}         # 根据 ID 查询用户（走缓存）
GET    /api/users/username/{username}  # 根据用户名查询（走缓存）
GET    /api/users              # 查询所有用户
GET    /api/users/page         # 分页查询
PUT    /api/users/{id}         # 更新用户（更新缓存）
DELETE /api/users/{id}         # 删除用户（清除缓存）
```

### 订单管理 (MySQL + JPA)

```
POST   /api/orders                    # 创建订单（自动生成订单号）
GET    /api/orders/{id}               # 查询订单
GET    /api/orders/orderNo/{orderNo}  # 根据订单号查询
GET    /api/orders/user/{userId}      # 查询用户订单列表
GET    /api/orders/status/{status}    # 按状态分页查询
PUT    /api/orders/{id}/status        # 更新订单状态
DELETE /api/orders/{id}               # 删除订单
```

### 日志管理 (MongoDB)

```
POST   /api/logs              # 创建日志
GET    /api/logs              # 查询所有日志
GET    /api/logs/level/{level}       # 按级别查询
GET    /api/logs/service/{service}   # 按服务查询
GET    /api/logs/search?keyword=xxx  # 模糊搜索
DELETE /api/logs/{id}         # 删除日志
```

### 商品搜索 (Elasticsearch)

```
POST   /api/products                # 创建商品索引
GET    /api/products/{id}           # 根据 ID 查询
GET    /api/products                # 查询所有
GET    /api/products/search?keyword=xxx    # 分词搜索
GET    /api/products/category/{category}   # 按分类查询
GET    /api/products/price-range?min=0&max=100  # 价格区间查询
DELETE /api/products/{id}           # 删除索引
```

### Redis 操作

```
POST   /api/redis/set?key=xxx&value=xxx              # 设置键值
POST   /api/redis/set-with-ttl?key=xxx&value=xxx&seconds=60  # 设置并带过期时间
GET    /api/redis/get?key=xxx                        # 获取值
DELETE /api/redis/delete?key=xxx                     # 删除键
GET    /api/redis/has-key?key=xxx                    # 检查键是否存在
POST   /api/redis/hash?key=xxx&hashKey=xxx&value=xxx  # Hash 操作
GET    /api/redis/hash?key=xxx&hashKey=xxx           # 获取 Hash 值
POST   /api/redis/list?key=xxx&value=xxx             # List 添加
GET    /api/redis/list?key=xxx&index=0               # 获取 List 项
POST   /api/redis/increment?key=xxx&delta=1          # 计数器自增
```

### Kafka 消息

```
POST   /api/kafka/user-event?message=xxx    # 发送用户事件
POST   /api/kafka/order-event?message=xxx   # 发送订单事件
POST   /api/kafka/send?topic=xxx&key=xxx&message=xxx  # 自定义 Topic 发送
```

## 配置说明

所有中间件的连接配置在 `application.yml` 中统一管理，各环境可覆盖：

- **数据库连接池**：使用 HikariCP，配置最小/最大连接数、超时等参数
- **Redis**：使用 Lettuce 客户端，配置连接池
- **Kafka**：配置生产者确认模式、重试、批量发送；消费者组、自动提交等
- **Elasticsearch**：配置连接超时和 Socket 超时
- **JPA**：开启 SQL 打印，自动 DDL 更新

## 关键特性

- **缓存**：`UserService` 使用 `@Cacheable` / `@CachePut` / `@CacheEvict` 注解，基于 Redis 实现缓存
- **分页**：Controller 支持 Spring Data `Pageable` 分页查询
- **审计**：`User` / `Order` 实体自动记录创建/更新时间
- **日志**：使用 SLF4J + Lombok `@Slf4j` 记录日志
- **异常**：简洁的异常处理，实际项目可扩展 `GlobalExceptionHandler`
