# Python → Java 迁移对照 (Migration Notes)

> 马股通 —— 作者：钟智强

本文档记录原 Python 脚本到本 Java Spring Boot 项目的**逐模块映射**，方便对照理解。

## 一、文件级映射

| 原 Python 文件 | 职责 | 对应 Java 组件 |
|----------------|------|----------------|
| `bursa_indices.py` | 指数抓取入口 + 调度 + 交易时段 | `scheduler/IndexScrapeScheduler`、`service/IndexQuoteService`、`service/TradingCalendarService` |
| `dm_indices.py` | 解析指数表 + 写库 | `service/scraper/parser/IndexParser`、`service/IndexQuoteService` |
| `dmdata.py` | DB 连接 + 工具函数 + `info_data` + 队列函数 | `util/ParseUtils`、`dto/AnnouncementInfo`、`service/AnnouncementQueueService`、（连接改由 Spring 管理） |
| `dm_annual_report.py` | 年报解析 + 写库 | `service/scraper/parser/AnnualReportParser`、`service/AnnouncementService` |
| `dm_financial_result.py` | 季报解析 + 写库 | `service/scraper/parser/FinancialResultParser`、`service/AnnouncementService` |
| `dm_listing_profile.py` | 上市概况解析 + 写库 | `service/scraper/parser/ListingProfileParser`、`service/AnnouncementService` |
| `dm_change_year_end.py` | 财年变更处理 | `service/AnnouncementService#handleChangeYearEnd` |
| `dm_entitlement.py` | （权益，占位） | `service/AnnouncementType` 枚举预留 |

## 二、函数级映射（`dmdata.py` 工具函数）

| 原函数 | 对应 Java 方法 (`util/ParseUtils`) |
|--------|-----------------------------------|
| `from_DMY(dmy)` | `fromDayMonthYear(String)` |
| `remove_white_space(s)` | `removeWhiteSpace(String)` |
| `get_domain(url)` | `getDomain(String)` |
| `get_full_url(domain, url)` | `getFullUrl(String, String)` |
| `get_number(s)` | `getNumber(String)` |
| `update_currency_value(s)` | `updateCurrencyValue(String)` |
| `get_ratio(s)` | `getRatio(String)` |
| `extract_attachment(soup, url)` | `extractAttachment(Element, String)` |

## 三、核心行为差异

1. **数据库连接**：原脚本用全局 `conn` 手动 `open_connection()/close_connection()`，
   本项目交由 Spring 数据源与 `@Transactional` 管理，业务代码不再关心连接生命周期。

2. **调度**：原脚本 `schedule.every(5).minutes` + `while True: sleep(1)`，
   本项目改为 `@Scheduled(fixedDelayString = ...)`，间隔可配置。

3. **写库语义**：原脚本手写 `INSERT ... ON DUPLICATE KEY UPDATE` 或先查后插，
   本项目通过仓储的「按业务键查找 → 存在则更新，否则插入」实现相同的幂等 upsert 语义。

4. **`info_data` 类**：原为解析原始键值行的类，迁移为不可变的 `dto/AnnouncementInfo`，
   构造时完成字段解析。

5. **表名**：所有表名按需求重命名，详见根目录 `README.md` 的对照表。

## 四、未迁移 / 简化项

- 原脚本生成 `BursaAnnoucement.ini`、写出中间 `json`/`csv` 文件的逻辑属于调试辅助，
  在服务化架构中不再需要，故未迁移。
- `dm_entitlement.py` 在原项目中即为占位，未包含完整落库逻辑，
  本项目在 `AnnouncementType` 中保留类型、待后续扩展。
