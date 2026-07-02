# 马股通

> 马来西亚股票数据抓取与解析服务
> Java Spring Boot 重构版

**作者 (Author)：** 钟智强
**许可证 (License)：** [MIT](./LICENSE)

---

## 一、项目简介

马股通是一个基于 **Java 17 + Spring Boot 3** 的服务端应用，用于自动抓取
马来西亚股票市场的公开数据，
包括：

- **指数与股票行情** (Index / Equity Quotes)
- **年度报告** (Annual Report)
- **季度财报** (Quarterly / Financial Result)
- **上市概况** (Listing Profile)
- **财政年度变更** (Change of Financial Year End)

本项目是对原有 Python 脚本的**完整重构**。原始脚本采用零散的过程式写法，
数据库连接与业务逻辑高度耦合、缺乏分层、难以维护。本版本以清晰的 **MVC + 分层架构**
重写，遵循面向对象设计与统一命名规范，并使用 Spring 生态替代原有的手写调度与连接管理。

> **为什么选择 MVC 而非 MVVM？**
> MVVM 的核心是前端「数据双向绑定」，主要服务于富客户端 UI。本项目是**后端数据服务**，
> 视图层仅为一个只读的服务端渲染仪表盘，因此采用经典的 **MVC** 更贴切：
> Model（JPA 实体 + 仓储）、View（Thymeleaf 仪表盘）、Controller（REST + 页面控制器）。

---

## 二、技术栈

| 领域         | 原 Python 实现               | 本项目 (Java)                                      |
| ------------ | ---------------------------- | -------------------------------------------------- |
| 浏览器自动化 | Playwright (Python)          | **Playwright for Java**                            |
| HTML 解析    | BeautifulSoup                | **Jsoup**                                          |
| 数据持久化   | mysql.connector（手写 SQL）  | **Spring Data JPA + MySQL**                        |
| 定时任务     | `schedule` 库 + `while True` | **Spring `@Scheduled`**                            |
| 日志         | 自定义 logger                | **SLF4J / Lombok `@Slf4j`**                        |
| 配置管理     | `.ini` + `configparser`      | **`application.yml` + `@ConfigurationProperties`** |
| 视图         | 无                           | **Thymeleaf 仪表盘**                               |

> **关于 ZenRows：** 本项目**不使用**任何第三方付费抓取代理（如 ZenRows），
> 完全依赖本地 Playwright 无头浏览器直连目标站点。

---

## 三、项目结构

```
MaGuTong/
├── pom.xml                     # Maven 构建配置
├── LICENSE                     # MIT 许可证
├── README.md                   # 本文件
├── docs/                       # 补充文档
│   ├── ARCHITECTURE.md         #   架构说明
│   └── MIGRATION.md            #   Python → Java 迁移对照
└── src/
    ├── main/
    │   ├── java/xin/ctkqiang/mybursa/
    │   │   ├── MaGuTongApplication.java        # 启动类
    │   │   ├── config/         # 配置：Playwright、抓取参数
    │   │   ├── controller/     # 【C】REST 接口 + 仪表盘页面控制器
    │   │   ├── model/          # 【M】JPA 实体
    │   │   ├── repository/     # 【M】Spring Data 仓储接口
    │   │   ├── service/        # 业务服务层
    │   │   │   └── scraper/     #   抓取与解析
    │   │   │       └── parser/  #     各类公告的 HTML 解析器
    │   │   ├── scheduler/      # 定时调度
    │   │   ├── dto/            # 数据传输对象
    │   │   └── util/           # 通用工具方法
    │   └── resources/
    │       ├── application.yml # 应用配置
    │       ├── schema.sql      # 数据库建表脚本（含全部重命名后的表）
    │       ├── templates/      # 【V】Thymeleaf 模板
    │       └── static/         # 静态资源 (CSS)
    └── test/                   # 单元测试
```

---

## 四、数据库表重命名对照

按需求，**所有数据库表名均已重命名**。旧名来自原 Python 脚本，新名语义更清晰、
统一使用下划线命名法：

| 表名                 | 含义             |
| -------------------- | ---------------- |
| `market_index_quote` | 指数/股票行情    |
| `annual_report`      | 年度报告         |
| `quarterly_result`   | 季度财报         |
| `listing_profile`    | 上市概况         |
| `announcement_queue` | 待处理公告队列   |
| `last_eod_marker`    | 最后收盘处理标记 |
| `trading_holiday`    | 交易假期日历     |

完整建表语句见 [`src/main/resources/schema.sql`](./src/main/resources/schema.sql)。

---

## 五、快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 2. 准备数据库

#### 方式 A：Docker（推荐）

使用 Docker 一键启动 MySQL，配置与 `application.yml` 完全对应：

```bash
docker run -d \
  --name bursa-mysql \
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

常用管理命令：

```bash
docker stop magutong-mysql      # 停止
docker start magutong-mysql     # 重新启动
docker rm -f magutong-mysql     # 删除容器（数据保留在 volume 中）
docker volume rm magutong-mysql-data  # 彻底删除数据
```

#### 方式 B：本地 MySQL

```sql
CREATE DATABASE bursa_data CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

建表脚本会在 `spring.jpa.hibernate.ddl-auto=update` 下自动创建，
也可手动执行 `src/main/resources/schema.sql`。

### 3. 配置环境变量

应用通过环境变量读取数据库连接信息（含默认值）：

| 环境变量     | 默认值       | 说明       |
| ------------ | ------------ | ---------- |
| `MYSQL_HOST` | `localhost`  | 数据库主机 |
| `MYSQL_PORT` | `3306`       | 端口       |
| `MYSQL_DB`   | `bursa_data` | 数据库名   |
| `MYSQL_USER` | `root`       | 用户名     |
| `MYSQL_PASS` | （空）       | 密码       |

### 4. 安装 Playwright 浏览器

首次运行前需下载 Chromium 内核：

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

### 5. 构建与运行

```bash
mvn clean package
java -jar target/bursa-scrapper-1.0.0.jar
```

启动后访问仪表盘：**http://localhost:8080/**

### 6. Docker Compose（一键启动全部服务）

无需本地安装 JDK 或 MySQL，Docker 会自动构建并运行：

```bash
# 构建镜像并启动（首次会较慢，后续利用缓存）
docker compose up -d

# 查看应用日志
docker compose logs -f app

# 停止所有服务
docker compose down

# 停止并清除数据
docker compose down -v
```

启动后访问仪表盘：**http://localhost:8080/**

> **说明：** `docker-compose.yml` 中已配置 MySQL 健康检查，应用会等待数据库就绪后才启动。

---

## 六、REST API

| 方法   | 路径                          | 说明                     |
| ------ | ----------------------------- | ------------------------ |
| `GET`  | `/` 或 `/dashboard`           | 行情仪表盘页面           |
| `GET`  | `/api/quotes`                 | 获取全部行情             |
| `GET`  | `/api/quotes/{tickerId}`      | 按代码获取单条行情       |
| `POST` | `/api/scraper/index/run`      | 立即触发一次指数抓取     |
| `GET`  | `/api/scraper/trading-status` | 查询当前是否处于交易时段 |

---

## 七、调度说明

指数抓取默认每 **5 分钟**执行一次（可通过 `magu-tong.schedule-interval-ms` 配置），
仅在**交易时段**内真正执行：

- 上午盘：09:00 – 13:00
- 下午盘：14:30 – 17:30
- 自动跳过周末与 `trading_holiday` 表中登记的假期

---

## 八、许可证

本项目基于 **MIT** 许可证发布。详见 [LICENSE](./LICENSE)。
