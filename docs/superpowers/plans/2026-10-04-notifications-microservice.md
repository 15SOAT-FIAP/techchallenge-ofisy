# Notifications Microservice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract the notification feature from the core application into a new independent microservice backed by DynamoDB.

**Architecture:** Create a new Spring Boot application `techchallenge-ofisy-notifications` using DynamoDB for storage. The new service will expose HTTP REST endpoints to create, read, and manage notifications. The core application (`techchallenge-ofisy`) will be refactored to remove all notification domain logic, entities, and database tables, using an HTTP Client (e.g. RestClient or Feign) to synchronously push notifications to the new microservice.

**Tech Stack:** Java 17+, Spring Boot 3, AWS SDK v2 (DynamoDB), Flyway, OpenFeign.

**Spec:** https://github.com/15SOAT-FIAP/techchallenge-ofisy/issues/232

## Global Constraints

- Implementar microsserviço de notificações e remover esta funcionalidade do core.
- Utilizar banco de dados NOSQL para persistir as informações de notificações hoje persistidas no core.
- Sugestão de banco NOSQL - Dynamo.

---

### Phase 1: Notification Microservice (New Repository)
*Note: Create a new folder `techchallenge-ofisy-notifications` in the workspace root for these tasks.*

### Task 1: Initialize Notification Microservice and Domain

**Files:**
- Create: `techchallenge-ofisy-notifications/pom.xml`
- Create: `techchallenge-ofisy-notifications/src/main/java/br/com/ofisy/notifications/NotificationsApplication.java`
- Create: `techchallenge-ofisy-notifications/src/main/java/br/com/ofisy/notifications/domain/Notification.java`

**Interfaces:**
- Produces: `Notification` entity with standard DynamoDB annotations.

- [ ] **Step 1: Write the failing test**

```java
// Create a dummy test to ensure context loads
package br.com.ofisy.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationsApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test` (or wait since pom isn't there yet)
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```xml
<!-- pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    <groupId>br.com.ofisy</groupId>
    <artifactId>notifications</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>software.amazon.awssdk</groupId><artifactId>dynamodb-enhanced</artifactId><version>2.20.162</version></dependency>
        <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    </dependencies>
</project>
```

```java
package br.com.ofisy.notifications;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationsApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationsApplication.class, args);
    }
}
```

```java
package br.com.ofisy.notifications.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import lombok.Data;
import java.time.Instant;

@Data
@DynamoDbBean
public class Notification {
    private String id;
    private String type; // STOCK, SERVICE_ORDER
    private String message;
    private boolean isRead;
    private Instant createdAt;

    @DynamoDbPartitionKey
    public String getId() { return id; }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git init
git add .
git commit -m "feat: init notifications microservice"
```

### Task 2: DynamoDB Repository and Config

**Files:**
- Create: `techchallenge-ofisy-notifications/src/main/java/br/com/ofisy/notifications/config/DynamoDbConfig.java`
- Create: `techchallenge-ofisy-notifications/src/main/java/br/com/ofisy/notifications/repository/NotificationRepository.java`

**Interfaces:**
- Produces: `NotificationRepository` bean for data access.

- [ ] **Step 1: Write the failing test**

```java
// Create test verifying bean loading
```

- [ ] **Step 2: Run test to verify it fails**

- [ ] **Step 3: Write minimal implementation**

```java
package br.com.ofisy.notifications.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {
    @Bean
    public DynamoDbClient dynamoDbClient() { return DynamoDbClient.builder().build(); }
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }
}
```

```java
package br.com.ofisy.notifications.repository;
import br.com.ofisy.notifications.domain.Notification;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class NotificationRepository {
    private final DynamoDbTable<Notification> notificationTable;

    public NotificationRepository(DynamoDbEnhancedClient enhancedClient) {
        this.notificationTable = enhancedClient.table("Notifications", TableSchema.fromBean(Notification.class));
    }

    public void save(Notification notification) {
        notificationTable.putItem(notification);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "feat: add dynamo db repository"
```

### Task 3: Notification Controllers

**Files:**
- Create: `techchallenge-ofisy-notifications/src/main/java/br/com/ofisy/notifications/controller/NotificationController.java`

**Interfaces:**
- Consumes: `NotificationRepository`

- [ ] **Step 1: Write the failing test**
(Skipped for brevity in this step, but executor should create `MockMvc` tests for endpoints)

- [ ] **Step 2: Run test to verify it fails**

- [ ] **Step 3: Write minimal implementation**
Implement endpoints to mimic the old `NotificationApi` from core, plus endpoints for core to push new notifications: `POST /api/v1/notifications/stock` and `POST /api/v1/notifications/service-orders`.

- [ ] **Step 4: Run test to verify it passes**

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "feat: add notification controllers"
```

---

### Phase 2: Core Refactoring
*Note: Execute these tasks inside the `techchallenge-ofisy` folder.*

### Task 4: Create DB Migration to Drop Notifications

**Files:**
- Create: `src/main/resources/db/migration/V28__drop_notifications_tables.sql`

- [ ] **Step 1: Write the failing test**
N/A for SQL

- [ ] **Step 2: Run test to verify it fails**
N/A

- [ ] **Step 3: Write minimal implementation**

```sql
DROP TABLE IF EXISTS notifications CASCADE;
```

- [ ] **Step 4: Run test to verify it passes**
Run: `mvn flyway:migrate` (or application boot test)

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/db/migration/V28__drop_notifications_tables.sql
git commit -m "chore: add migration to drop notifications table"
```

### Task 5: Add HTTP Client for Notifications

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/br/com/ofisy/adapters/gateways/notification/NotificationClient.java`

**Interfaces:**
- Produces: `NotificationClient` with methods `createStockNotification` and `createServiceOrderNotification`

- [ ] **Step 1: Write the failing test**

- [ ] **Step 2: Run test to verify it fails**

- [ ] **Step 3: Write minimal implementation**

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
package br.com.ofisy.adapters.gateways.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificationClient", url = "${notification.service.url}")
public interface NotificationClient {
    @PostMapping("/api/v1/notifications/stock")
    void createStockNotification(@RequestBody Object request);

    @PostMapping("/api/v1/notifications/service-orders")
    void createServiceOrderNotification(@RequestBody Object request);
}
```
*Note: Enable Feign clients in the main app class.*

- [ ] **Step 4: Run test to verify it passes**

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "feat: add feign client for notification service"
```

### Task 6: Refactor Core Services and Delete Old Code

**Files:**
- Modify: `src/main/java/br/com/ofisy/application/stock/consume/ConsumeStockService.java`
- Modify: `src/main/java/br/com/ofisy/application/serviceorder/generatequote/GenerateServiceOrderQuoteService.java`
- Delete: `src/main/java/br/com/ofisy/adapters/controllers/notification/*`
- Delete: `src/main/java/br/com/ofisy/application/notification/*`
- Delete: `src/main/java/br/com/ofisy/domain/notification/*`

**Interfaces:**
- Consumes: `NotificationClient`

- [ ] **Step 1: Write the failing test**
Update existing tests for `ConsumeStockService` and `GenerateServiceOrderQuoteService` to mock `NotificationClient` instead of `CreateLowStockNotificationUseCase` and `CreateQuoteNotificationUseCase`.

- [ ] **Step 2: Run test to verify it fails**

- [ ] **Step 3: Write minimal implementation**
Inject `NotificationClient` into `ConsumeStockService` and `GenerateServiceOrderQuoteService`, replacing the use of old notification use cases. Call the client's methods. Then safely delete the old notification directories.

- [ ] **Step 4: Run test to verify it passes**

- [ ] **Step 5: Commit**

```bash
git rm -r src/main/java/br/com/ofisy/.../notification/
git add src/main/java/br/com/ofisy/application/stock/
git add src/main/java/br/com/ofisy/application/serviceorder/
git commit -m "refactor: remove core notifications and integrate with microservice"
```
