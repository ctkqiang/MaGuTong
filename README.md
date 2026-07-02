# 马股通

> **马来西亚股票数据抓取与解析服务**
> Java 17 · Spring Boot 3.3.4 · Playwright · MySQL 8

**作者 (Author)：** 钟智强
**许可证 (License)：** [MIT](./LICENSE)

---

## 目录

1. [项目简介](#一项目简介)
2. [技术栈](#二技术栈)
3. [项目结构](#三项目结构)
4. [数据库设计](#四数据库设计)
5. [快速开始](#五快速开始)
6. [Docker 部署](#六docker-部署)
7. [REST API](#七rest-api)
8. [配置参考](#八配置参考)
9. [调度机制](#九调度机制)
10. [架构设计](#十架构设计)
11. [许可证](#十一许可证)

---

## 一、项目简介

马股通是一个基于 **Java 17 + Spring Boot 3** 的后端服务，用于自动抓取马来西亚证券交易所
(Bursa Malaysia) 的公开市场数据，包括：

- **指数与股票行情** (Index / Equity Quotes)
- **年度报告** (Annual Report)
- **季度财报** (Quarterly / Financial Result)
- **上市概况** (Listing Profile)
- **财政年度变更** (Change of Financial Year End)

数据通过 Playwright 无头浏览器从 Bursa Malaysia 官网实时抓取，经 Jsoup 解析后持久化至 MySQL，
并通过 REST API 和 Thymeleaf 仪表盘对外提供服务。

> **架构选型：** 本项目采用经典 **MVC** 分层架构 —— Model (JPA 实体 + 仓储)、
> View (Thymeleaf 仪表盘)、Controller (REST + 页面控制器)。
> 视图层为只读的服务端渲染仪表盘，无需 MVVM 的双向绑定机制。

---

## 二、技术栈

| 领域         | 技术方案                                       | 版本   |
| ------------ | ---------------------------------------------- | ------ |
| 运行时       | Eclipse Temurin JDK                            | 17     |
| 框架         | Spring Boot                                    | 3.3.4  |
| 浏览器自动化 | Playwright for Java                            | 1.47.0 |
| HTML 解析    | Jsoup                                          | 1.18.1 |
| 数据持久化   | Spring Data JPA + Hibernate + MySQL            | 8.0    |
| 定时任务     | Spring `@Scheduled`                            | -      |
| 视图引擎     | Thymeleaf                                      | -      |
| 参数校验     | Spring Validation                              | -      |
| 日志         | SLF4J + Lombok `@Slf4j`                        | -      |
| 配置管理     | `application.yml` + `@ConfigurationProperties` | -      |
| 构建工具     | Apache Maven                                   | 3.9+   |
| 容器化       | Docker + Docker Compose                        | -      |

> **关于 ZenRows：** 本项目**不使用**任何第三方付费抓取代理，
> 完全依赖本地 Playwright 无头浏览器直连目标站点。

---

## 三、项目结构

```
MaGuTong/
├── pom.xml                         # Maven 构建配置
├── Dockerfile                      # 多阶段 Docker 构建
├── docker-compose.yml              # Docker Compose 编排 (app + MySQL)
├── .dockerignore                   # Docker 构建排除规则
├── .githooks/                      # Git 钩子
│   └── pre-commit                  #   提交前语言规范检查
├── LICENSE                         # MIT 许可证
├── README.md                       # 本文件
├── docs/                           # 补充文档
│   ├── ARCHITECTURE.md             #   架构设计说明
│   └── MIGRATION.md                #   Python → Java 迁移对照
└── src/
    ├── main/
    │   ├── java/xin/ctkqiang/mybursa/
    │   │   ├── MaGuTongApplication.java    # Spring Boot 启动类
    │   │   ├── config/                     # 配置层
    │   │   │   ├── PlaywrightConfig.java   #   Playwright 生命周期管理 (单例 Bean)
    │   │   │   └── ScraperProperties.java  #   抓取器可配置属性
    │   │   ├── controller/                 # 控制器层 [C]
    │   │   │   ├── DashboardController.java    # 仪表盘页面
    │   │   │   ├── IndexQuoteController.java   # 行情 REST API
    │   │   │   └── ScraperController.java      # 抓取触发 + 交易状态
    │   │   ├── dto/                        # 数据传输对象
    │   │   │   └── AnnouncementInfo.java
    │   │   ├── model/                      # 实体层 [M]
    │   │   │   ├── AnnouncementQueue.java
    │   │   │   ├── AnnualReport.java
    │   │   │   ├── IndexQuote.java
    │   │   │   ├── LastEodMarker.java
    │   │   │   ├── ListingProfile.java
    │   │   │   ├── QuarterlyResult.java
    │   │   │   └── TradingHoliday.java
    │   │   ├── repository/                 # 仓储层 [M]
    │   │   │   ├── AnnouncementQueueRepository.java
    │   │   │   ├── AnnualReportRepository.java
    │   │   │   ├── IndexQuoteRepository.java
    │   │   │   ├── LastEodMarkerRepository.java
    │   │   │   ├── ListingProfileRepository.java
    │   │   │   ├── QuarterlyResultRepository.java
    │   │   │   └── TradingHolidayRepository.java
    │   │   ├── scheduler/                  # 定时调度
    │   │   │   └── IndexScrapeScheduler.java
    │   │   ├── service/                    # 业务服务层
    │   │   │   ├── AnnouncementQueueService.java
    │   │   │   ├── AnnouncementService.java
    │   │   │   ├── AnnouncementType.java
    │   │   │   ├── IndexQuoteService.java
    │   │   │   ├── TradingCalendarService.java
    │   │   │   └── scraper/               # 抓取子模块
    │   │   │       ├── PageFetcher.java   #   页面获取 (Playwright)
    │   │   │       └── parser/            #   HTML 解析器
    │   │   │           ├── AnnualReportParser.java
    │   │   │           ├── FinancialResultParser.java
    │   │   │           ├── IndexParser.java
    │   │   │           └── ListingProfileParser.java
    │   │   └── util/                       # 通用工具
    │   │       └── ParseUtils.java
    │   └── resources/
    │       ├── application.yml             # 应用配置
    │       ├── schema.sql                  # 数据库建表脚本
    │       ├── templates/                  # Thymeleaf 模板 [V]
    │       │   └── dashboard.html
    │       └── static/                     # 静态资源
    │           └── css/
    │               └── dashboard.css
    └── test/                               # 单元测试
        └── java/xin/ctkqiang/mybursa/
            └── util/
                └── ParseUtilsTest.java
```

---

## 四、数据库设计

数据库名：`bursa_data`，字符集 `utf8mb4`。

| 表名                 | 主键                     | 说明                                                   |
| -------------------- | ------------------------ | ------------------------------------------------------ |
| `market_index_quote` | `ticker_id` (VARCHAR 32) | 实时指数/股票行情快照                                  |
| `annual_report`      | `id` (BIGINT AUTO)       | 年度报告，唯一键 `(stock_code, ref_id)`                |
| `quarterly_result`   | `id` (BIGINT AUTO)       | 季度财报 (26 字段)，唯一键 `(stock_code, fin_qtr_end)` |
| `listing_profile`    | `id` (BIGINT AUTO)       | 上市概况 (18 字段)，唯一键 `(stock_code, ref_id)`      |
| `announcement_queue` | `queue_id` (BIGINT AUTO) | 待处理公告队列，状态：0=待处理 / 1=成功 / -1=失败      |
| `last_eod_marker`    | `en_type` (VARCHAR 32)   | 各公告类型最后处理时间标记                             |
| `trading_holiday`    | `holiday_date` (DATE)    | 交易假期日历                                           |

完整建表语句见 [`src/main/resources/schema.sql`](./src/main/resources/schema.sql)。

---

## 五、快速开始

### 1. 环境要求

- **JDK 17+** (推荐 Eclipse Temurin)
- **Maven 3.8+**
- **MySQL 8.0+**

### 2. 准备数据库

#### 方式 A：Docker（推荐）

```bash
docker run -d \
  --name magutong-mysql \
  -p 3306:3306 \
  -e MYSQL_ALLOW_EMPTY_PASSWORD=yes \
  -e MYSQL_DATABASE=bursa_data \
  -e TZ=Asia/Kuala_Lumpur \
  -v magutong-mysql-data:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci \
  --default-time-zone='+08:00'
```

容器管理：

```bash
docker stop magutong-mysql              # 停止
docker start magutong-mysql             # 重新启动
docker rm -f magutong-mysql             # 删除容器 (数据保留在 volume)
docker volume rm magutong-mysql-data    # 彻底删除数据
```

#### 方式 B：本地 MySQL

```sql
CREATE DATABASE bursa_data
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 3. 环境变量

应用通过环境变量读取数据库连接信息（均有默认值）：

| 环境变量       | 默认值       | 说明               |
| -------------- | ------------ | ------------------ |
| `MYSQL_HOST`   | `localhost`  | 数据库主机         |
| `MYSQL_PORT`   | `3306`       | 端口               |
| `MYSQL_DB`     | `bursa_data` | 数据库名           |
| `SERVER_PORT`  | `8080`       | 应用端口           |
| `JPA_DDL_AUTO` | `update`     | Hibernate DDL 策略 |

### 4. 安装 Playwright 浏览器

首次运行前需下载 Chromium 内核：

```bash
mvn exec:java -e \
  -Dexec.mainClass=com.microsoft.playwright.CLI \
  -Dexec.args="install chromium"
```

### 5. 构建与运行

```bash
# 构建
mvn clean package -DskipTests

# 运行
java -jar target/magu-tong.jar
```

启动后访问仪表盘：**http://localhost:8080/**

---

## 六、Docker 部署

### Docker Compose（一键启动）

无需本地安装 JDK 或 MySQL：

```bash
# 构建镜像并启动 (首次较慢，后续利用缓存)
docker compose up -d

# 查看应用日志
docker compose logs -f app

# 停止所有服务
docker compose down

# 停止并清除数据
docker compose down -v

# 代码改动后重新构建
docker compose build && docker compose up -d
```

启动后访问仪表盘：**http://localhost:8080/**

### Dockerfile 构建策略

采用**多阶段构建**优化镜像体积：

| 阶段    | 基础镜像                       | 用途                             |
| ------- | ------------------------------ | -------------------------------- |
| builder | `maven:3.9-eclipse-temurin-17` | Maven 编译，生成 fat-jar         |
| runtime | `eclipse-temurin:17-jre`       | JRE 运行时 + Playwright Chromium |

- 利用 Maven 依赖层缓存加速增量构建
- 仅安装 Chromium（跳过 Firefox/WebKit 以减小镜像）
- JVM 参数：`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`

### 服务编排

| 服务    | 容器名           | 端口        | 说明                         |
| ------- | ---------------- | ----------- | ---------------------------- |
| `mysql` | `magutong-mysql` | `3306:3306` | MySQL 8.0 + 健康检查         |
| `app`   | `magutong-app`   | `8080:8080` | 应用服务，等待 DB 就绪后启动 |

---

## 七、REST API

| 方法   | 路径                          | 说明                     | 响应示例                               |
| ------ | ----------------------------- | ------------------------ | -------------------------------------- |
| `GET`  | `/` 或 `/dashboard`           | 行情仪表盘页面 (HTML)    | Thymeleaf 渲染页面                     |
| `GET`  | `/api/quotes`                 | 获取全部行情             | `[{tickerId, symbol, lastPrice, ...}]` |
| `GET`  | `/api/quotes/{tickerId}`      | 按代码查询单条行情       | `{tickerId, symbol, lastPrice, ...}`   |
| `POST` | `/api/scraper/index/run`      | 手动触发一次指数抓取     | `{"success": true, "savedCount": 35}`  |
| `GET`  | `/api/scraper/trading-status` | 查询当前是否处于交易时段 | `{"tradingOpen": false}`               |

---

## 八、配置参考

所有抓取器参数通过 `application.yml` 的 `magu-tong.*` 前缀配置，
绑定至 `ScraperProperties` 类：

| 配置项                                    | 类型      | 默认值    | 说明                       |
| ----------------------------------------- | --------- | --------- | -------------------------- |
| `magu-tong.indices-url`                   | String    | Bursa URL | 指数行情页面地址           |
| `magu-tong.max-retries`                   | int       | `3`       | 单次抓取最大重试次数       |
| `magu-tong.navigation-timeout-ms`         | int       | `20000`   | 页面导航超时 (ms)          |
| `magu-tong.table-settle-ms`               | int       | `2000`    | 表格刷新等待时间 (ms)      |
| `magu-tong.headless`                      | boolean   | `true`    | 是否无头模式运行浏览器     |
| `magu-tong.schedule-interval-ms`          | int       | `300000`  | 抓取间隔 (ms)，默认 5 分钟 |
| `magu-tong.trading-hours.morning-start`   | LocalTime | `09:00`   | 上午盘开始                 |
| `magu-tong.trading-hours.morning-end`     | LocalTime | `13:00`   | 上午盘结束                 |
| `magu-tong.trading-hours.afternoon-start` | LocalTime | `14:30`   | 下午盘开始                 |
| `magu-tong.trading-hours.afternoon-end`   | LocalTime | `17:30`   | 下午盘结束                 |
| `magu-tong.trading-hours.weekend-days`    | List      | `[6, 7]`  | 周末 (6=周六, 7=周日)      |

---

## 九、调度机制

指数抓取默认每 **5 分钟**执行一次，仅在**交易时段**内真正执行。

### 交易时段判定（三重过滤）

```
1. 排除周末 → 检查 dayOfWeek 是否在 weekendDays 列表中
2. 排除假期 → 查询 trading_holiday 表 (数据库异常时安全降级为"非假期")
3. 判断时段 → 时间是否落在 [morningStart, morningEnd) 或 [afternoonStart, afternoonEnd)
```

| 时段   | 时间 (MYT)                                 |
| ------ | ------------------------------------------ |
| 上午盘 | 09:00 – 13:00                              |
| 下午盘 | 14:30 – 17:30                              |
| 周末   | 自动跳过                                   |
| 假期   | 自动跳过 (需在 `trading_holiday` 表中登记) |

非交易时段内，调度器静默跳过，不产生任何网络请求。
可通过 `POST /api/scraper/index/run` 随时手动触发，不受时段限制。

---

## 十、架构设计

```
                    ┌─────────────────────────────────┐
                    │         Thymeleaf [V]            │
                    │       dashboard.html             │
                    └──────────┬──────────────────────┘
                               │
┌──────────────┐    ┌──────────▼──────────────────────┐
│  Scheduler   │    │       Controller [C]             │
│  (5 min)     ├───►│  Dashboard / IndexQuote / Scraper│
└──────────────┘    └──────────┬──────────────────────┘
                               │
                    ┌──────────▼──────────────────────┐
                    │         Service                   │
                    │  IndexQuoteService                │
                    │  AnnouncementService              │
                    │  TradingCalendarService           │
                    └──────┬────────────┬──────────────┘
                           │            │
              ┌────────────▼──┐   ┌─────▼──────────────┐
              │  scraper/     │   │  Repository [M]     │
              │  PageFetcher  │   │  Spring Data JPA    │
              │  parser/*     │   └─────┬──────────────┘
              └───────────────┘         │
                                  ┌─────▼──────────────┐
                                  │     MySQL 8.0       │
                                  │   bursa_data        │
                                  └────────────────────┘
```

### 关键设计决策

1. **抓取与解析分离** — `PageFetcher` 负责页面获取，`parser/*` 负责 HTML 解析
2. **公告队列驱动** — `announcement_queue` 表实现异步处理 (状态机：0→1/-1)
3. **交易时段集中判定** — `TradingCalendarService` 统一管理，支持假期表
4. **配置外部化** — `@ConfigurationProperties` 绑定，支持环境变量覆盖
5. **Playwright 生命周期** — 单例 Bean + `@PreDestroy` 确保浏览器资源释放

详细架构说明见 [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md)，
Python → Java 迁移对照见 [`docs/MIGRATION.md`](./docs/MIGRATION.md)。

---

## 十一、许可证

本项目基于 **MIT** 许可证发布。详见 [LICENSE](./LICENSE)。
