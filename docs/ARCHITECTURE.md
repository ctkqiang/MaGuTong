# 架构说明 (Architecture)

> BursaScrapper —— 作者：钟智强

本文档说明 BursaScrapper 的分层架构与关键设计决策。

## 一、整体分层

项目遵循经典 **MVC + 服务分层** 架构，自上而下：

```
       ┌─────────────────────────────────────────┐
       │  View 层：Thymeleaf 仪表盘 (dashboard)     │
       └───────────────────┬─────────────────────┘
                           │
       ┌───────────────────┴─────────────────────┐
       │  Controller 层                            │
       │   · DashboardController  (页面)           │
       │   · IndexQuoteController (行情 REST)      │
       │   · ScraperController    (触发/状态 REST) │
       └───────────────────┬─────────────────────┘
                           │
       ┌───────────────────┴─────────────────────┐
       │  Service 层（业务编排）                    │
       │   · IndexQuoteService                     │
       │   · AnnouncementService（分发器）          │
       │   · AnnouncementQueueService              │
       │   · TradingCalendarService（交易时段判定） │
       │   · scraper/  抓取 + parser/ 解析          │
       └───────────────────┬─────────────────────┘
                           │
       ┌───────────────────┴─────────────────────┐
       │  Model 层                                 │
       │   · repository/  Spring Data JPA 仓储      │
       │   · model/       JPA 实体                  │
       └───────────────────┬─────────────────────┘
                           │
                    ┌──────┴──────┐
                    │  MySQL 数据库 │
                    └─────────────┘

   旁路：scheduler/ 定时调用 Service；config/ 提供 Playwright 与参数 Bean
```

## 二、关键设计决策

### 1. 抓取与解析分离
- `service/scraper/PageFetcher`：只负责用 Playwright 打开页面、等待、点击「显示 50 条」并返回 HTML；带重试与独立 `BrowserContext`。
- `service/scraper/parser/*`：只负责用 Jsoup 把 HTML 解析成领域对象，不涉及网络与数据库。

这种分离让解析逻辑可独立测试（喂入静态 HTML 即可），也让抓取策略可单独演进。

### 2. 公告队列驱动
原脚本用 `bursapending` 表作为待处理队列。本项目保留该模式，重命名为 `announcement_queue`，
并用 `AnnouncementQueueService` 封装「入队 / 取待处理 / 标记完成 / 标记错误」四个操作，
状态字段 `process_state`：`0=待处理 / 1=完成 / -1=错误`。

### 3. 交易时段判定集中化
`TradingCalendarService.isTradingTime()` 统一判定：排除周末、排除 `trading_holiday`
表中的假期、并检查上午/下午两个时段窗口。调度器与「立即触发」接口共用同一判定。

### 4. 配置外部化
所有可调参数（抓取间隔、交易时段、目标 URL）通过 `@ConfigurationProperties(prefix = "magu-tong")`
绑定，避免硬编码。数据库连接通过环境变量注入，便于容器化部署。

### 5. Playwright 生命周期管理
`PlaywrightConfig` 将 `Playwright` 与 `Browser` 作为单例 Bean 管理，配合 `@PreDestroy`
在应用关闭时释放资源，避免原脚本中每次抓取都重启浏览器的开销。
