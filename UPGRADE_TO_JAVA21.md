# Austin 项目 Java 21 升级说明

本文档记录了将 Austin 项目从 Java 8 升级到 Java 21 的所有变更。

## 升级概览

- **Java 版本**: 1.8 → 21
- **Spring Boot 版本**: 2.5.6 → 3.2.0
- **MySQL 驱动版本**: 5.1.35 → 8.0.33
- **API 框架**: Springfox Swagger → SpringDoc OpenAPI

## 主要变更

### 1. POM 文件变更

#### 主 pom.xml (`/pom.xml`)
- Java 版本从 1.8 升级到 21
- Spring Boot 从 2.5.6 升级到 3.2.0
- 核心依赖库版本升级:
  - `mysql-connector-java`: 5.1.35 → 8.0.33
  - `hutool-all`: 5.7.15 → 5.8.24
  - `guava`: 31.0.1-jre → 33.0.0-jre
  - `okhttp`: 4.9.2 → 4.12.0
  - `fastjson`: 1.2.83 → 2.0.43 (包名也变更为 fastjson2)
  - `tencentcloud-sdk-java`: 3.1.510 → 3.1.900
  - `nacos-config-spring-boot-starter`: 0.2.12 → 0.3.0
  - `apollo-client`: 2.1.0 → 2.3.0
  - `weixin-java`: 4.5.3.B → 4.6.0
  - `rocketmq-spring-boot-starter`: 2.2.2 → 2.3.0
- Swagger 替换: `springfox-boot-starter` → `springdoc-openapi-starter-webmvc-ui` (2.3.0)

#### 子模块 pom.xml
- `austin-web/pom.xml`: 将 springfox 依赖替换为 springdoc

### 2. Java 代码变更

#### 2.1 包名迁移 (javax.* → jakarta.*)

由于 Spring Boot 3.x 迁移到 Jakarta EE,所有 `javax.*` 包都需要改为 `jakarta.*`:

- `javax.annotation.*` → `jakarta.annotation.*`
  - 影响注解: `@PostConstruct`, `@PreDestroy`
  
- `javax.persistence.*` → `jakarta.persistence.*`
  - 影响注解: `@Entity`, `@Id`, `@GeneratedValue`, `@GenerationType`
  
- `javax.servlet.*` → `jakarta.servlet.*`
  - 影响类: `HttpServletRequest`, `HttpServletResponse`
  
- `javax.validation.*` → `jakarta.validation.*`
  - 影响注解: `@NotNull`, `@Valid` 等

#### 2.2 FastJSON 包名变更

- `com.alibaba.fastjson.*` → `com.alibaba.fastjson2.*`
- 所有使用 FastJSON 的类都需要更新导入语句

#### 2.3 Swagger 注解迁移

Swagger 框架从 Springfox 迁移到 SpringDoc OpenAPI 3.x:

- `io.swagger.annotations.*` → `io.swagger.v3.oas.annotations.*`
- 注解变更:
  - `@Api` → `@Tag`
    - 参数: `tags = "..."` → `name = "..."`
  - `@ApiOperation` → `@Operation`
    - 参数: `value = "..."` → `summary = "..."`

#### 2.4 Swagger 配置变更

`SwaggerConfiguration.java` 从 Springfox 配置改为 SpringDoc 配置:
- 删除 `Docket` 配置
- 使用 `OpenAPI` 对象配置 API 文档
- 访问地址保持不变: `http://localhost:8080/swagger-ui/index.html`

### 3. 配置文件变更

#### application.properties
- MySQL 驱动类名: `com.mysql.jdbc.Driver` → `com.mysql.cj.jdbc.Driver`

### 4. Docker 配置变更

#### Dockerfile
- 基础镜像: `openjdk:8-jre` → `openjdk:21-jdk-slim`

### 5. 文档变更

#### README.md
- JDK 版本标识: 8 → 21
- Spring Boot 版本标识: 2.5.6 → 3.2.0
- MySQL 版本说明: 5.7.x → 8.0.x

## 受影响的文件列表

### 核心配置文件
- `/pom.xml`
- `/Dockerfile`
- `/README.md`
- `/austin-web/pom.xml`
- `/austin-web/src/main/resources/application.properties`

### Java 源代码文件 (部分列表)

#### 注解相关 (javax.annotation → jakarta.annotation)
- `austin-handler/src/main/java/com/java3y/austin/handler/config/SensitiveWordsConfig.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/deduplication/build/AbstractDeduplicationBuilder.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/deduplication/limit/SlideWindowLimitService.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/deduplication/service/AbstractDeduplicationService.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/flowcontrol/FlowControlFactory.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/handler/BaseHandler.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/loadbalance/ServiceLoadBalancerFactory.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/pending/TaskPendingHolder.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/receipt/MessageReceipt.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/receiver/kafka/ReceiverStart.java`
- `austin-handler/src/main/java/com/java3y/austin/handler/receiver/redis/RedisReceiver.java`
- `austin-support/src/main/java/com/java3y/austin/support/pending/AbstractLazyPending.java`
- `austin-web/src/main/java/com/java3y/austin/web/config/WeChatLoginConfig.java`

#### JPA 实体相关 (javax.persistence → jakarta.persistence)
- `austin-support/src/main/java/com/java3y/austin/support/domain/ChannelAccount.java`
- `austin-support/src/main/java/com/java3y/austin/support/domain/MessageTemplate.java`
- `austin-support/src/main/java/com/java3y/austin/support/domain/SmsRecord.java`

#### Servlet 相关 (javax.servlet → jakarta.servlet)
- `austin-web/src/main/java/com/java3y/austin/web/aspect/AustinAspect.java`
- `austin-web/src/main/java/com/java3y/austin/web/controller/OfficialAccountController.java`

#### Validation 相关 (javax.validation → jakarta.validation)
- `austin-web/src/main/java/com/java3y/austin/web/vo/MessageTemplateParam.java`

#### Swagger/OpenAPI 相关
- `austin-web/src/main/java/com/java3y/austin/web/config/SwaggerConfiguration.java`
- 所有 Controller 类 (使用 @Tag 和 @Operation 注解)

#### FastJSON 相关
- 所有使用 FastJSON 的类 (约 50+ 个文件)

## 兼容性注意事项

### 1. MySQL 版本要求
- 推荐使用 MySQL 8.0.x
- 如果使用 MySQL 5.7,需要在 pom.xml 中调整 mysql-connector-java 版本

### 2. FastJSON API 变更
- FastJSON 2.x 的 API 基本向后兼容
- 部分高级特性可能需要调整
- 建议测试所有 JSON 序列化/反序列化功能

### 3. Swagger UI 访问
- URL 地址保持不变: `http://localhost:8080/swagger-ui/index.html`
- 如果访问出现问题,可以尝试: `http://localhost:8080/swagger-ui.html`

### 4. Spring Boot 3.x 重大变更
- 所有 javax.* 包迁移到 jakarta.*
- 部分配置属性名称可能有变化
- 某些自动配置类可能有调整

### 5. Java 21 新特性
- 虚拟线程 (Virtual Threads)
- 记录模式 (Record Patterns)
- Switch 模式匹配
- 这些新特性可在后续版本中逐步采用

## 构建和部署

### 构建要求
- JDK 21
- Maven 3.6.x 或更高版本

### 构建命令
```bash
mvn clean install -DskipTests
```

### Docker 部署
```bash
# 构建镜像
docker build -t austin:latest .

# 运行容器
docker-compose up -d
```

## 验证步骤

1. **编译验证**
   ```bash
   mvn clean compile
   ```

2. **单元测试验证**
   ```bash
   mvn test
   ```

3. **启动验证**
   - 启动应用
   - 访问 Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   - 验证核心功能正常

4. **功能测试**
   - 测试消息发送功能
   - 测试数据持久化
   - 测试缓存功能
   - 测试定时任务

## 回滚方案

如果升级后出现问题,可以通过以下步骤回滚:

1. 恢复代码到升级前的 commit
2. 或者手动恢复关键配置:
   - pom.xml 中的版本配置
   - application.properties 中的驱动配置
   - Dockerfile 中的基础镜像

## 后续优化建议

1. **性能优化**
   - 利用 Java 21 的虚拟线程优化高并发场景
   - 使用新的 GC 策略

2. **代码现代化**
   - 使用 Java 新特性简化代码
   - 采用 Record 类型替代部分 DTO

3. **依赖管理**
   - 定期更新依赖到最新稳定版本
   - 关注安全漏洞更新

## 问题排查

如遇到问题,请按以下顺序排查:

1. **编译错误**
   - 检查 javax.* 是否都已改为 jakarta.*
   - 检查 FastJSON 包名是否正确

2. **运行时错误**
   - 检查 MySQL 驱动配置
   - 检查所有第三方依赖版本兼容性

3. **功能异常**
   - 检查 Swagger 配置
   - 检查消息队列连接
   - 检查缓存连接

## 技术支持

如有问题,请联系项目维护者或在项目 Issue 中提问。

---

升级完成日期: 2025-11-26
