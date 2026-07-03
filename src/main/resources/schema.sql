-- ============================================================
-- 马股通 数据库结构 (Database Schema)
-- 作者 (Author): 钟智强
-- 许可证 (License): GPL v3
-- ------------------------------------------------------------
-- 说明：本文件包含全部业务表的建表语句，表名已相较原 Python 项目
--       进行语义化重命名。生产环境可将 spring.jpa.hibernate.ddl-auto
--       设为 validate，并以本脚本作为唯一权威 DDL 来源。
--
-- 数据表 (Tables):
--   market_index_quote  - 指数/股票行情
--   annual_report       - 年度报告
--   quarterly_result    - 季度财报
--   listing_profile     - 上市概况
--   announcement_queue  - 待处理公告队列
--   last_eod_marker     - 最后收盘处理标记
--   trading_holiday     - 交易假期日历
-- ============================================================
CREATE DATABASE IF NOT EXISTS bursa_data DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bursa_data;
-- ------------------------------------------------------------
-- 实时行情快照 (Real-time index / security quote)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS market_index_quote (
    ticker_id VARCHAR(32) NOT NULL COMMENT '行情代码，如 5099.KL',
    stock_code VARCHAR(16) COMMENT '股票代码',
    symbol VARCHAR(128) COMMENT '证券名称',
    exchange VARCHAR(8) COMMENT '交易所代码',
    sector_id INT COMMENT '板块编号',
    prev_price DECIMAL(18, 6) COMMENT '前收盘价',
    open_price DECIMAL(18, 6) COMMENT '开盘价',
    high_price DECIMAL(18, 6) COMMENT '最高价',
    low_price DECIMAL(18, 6) COMMENT '最低价',
    last_price DECIMAL(18, 6) COMMENT '最新成交价',
    last_update DATETIME COMMENT '来源页面时间戳',
    PRIMARY KEY (ticker_id)
) ENGINE = InnoDB COMMENT = '实时行情快照';
-- ------------------------------------------------------------
-- 年度报告 (Annual report)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS annual_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ref_id VARCHAR(64) COMMENT '公告参考编号',
    ref_id_amend VARCHAR(64) COMMENT '修订前参考编号',
    en_type VARCHAR(32) COMMENT '公告类型',
    stock_code VARCHAR(16) COMMENT '股票代码',
    fin_year INT COMMENT '财政年度',
    fin_year_end DATE COMMENT '财政年度截止日',
    announce_date DATE COMMENT '公告日期',
    category VARCHAR(128) COMMENT '公告分类',
    attachment TEXT COMMENT '附件列表',
    url VARCHAR(512) COMMENT '公告来源地址',
    last_update DATETIME COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_annual_stock_ref (stock_code, ref_id)
) ENGINE = InnoDB COMMENT = '年度报告公告';
-- ------------------------------------------------------------
-- 季度财务业绩 (Quarterly financial result)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS quarterly_result (
    id BIGINT NOT NULL AUTO_INCREMENT,
    stock_code VARCHAR(16) COMMENT '股票代码',
    fin_qtr_end DATE COMMENT '财政季度截止日',
    fin_year_end DATE COMMENT '财政年度截止日',
    fin_year INT COMMENT '财政年度',
    fin_qtr INT COMMENT '财政季度序号',
    ref_id VARCHAR(64) COMMENT '公告参考编号',
    ref_id_amend VARCHAR(64) COMMENT '修订前参考编号',
    announce_date DATE COMMENT '公告日期',
    revenue DECIMAL(20, 4) COMMENT '当季营业额',
    profit_before_tax DECIMAL(20, 4) COMMENT '当季税前利润',
    net_profit DECIMAL(20, 4) COMMENT '当季净利润',
    eps DECIMAL(20, 6) COMMENT '当季每股盈利',
    dps DECIMAL(20, 6) COMMENT '当季每股股息',
    yoy_revenue DECIMAL(20, 4) COMMENT '同比营业额',
    yoy_profit_before_tax DECIMAL(20, 4) COMMENT '同比税前利润',
    yoy_net_profit DECIMAL(20, 4) COMMENT '同比净利润',
    yoy_eps DECIMAL(20, 6) COMMENT '同比每股盈利',
    yoy_dps DECIMAL(20, 6) COMMENT '同比每股股息',
    cum_revenue DECIMAL(20, 4) COMMENT '累计营业额',
    cum_profit_before_tax DECIMAL(20, 4) COMMENT '累计税前利润',
    cum_net_profit DECIMAL(20, 4) COMMENT '累计净利润',
    cum_eps DECIMAL(20, 6) COMMENT '累计每股盈利',
    cum_dps DECIMAL(20, 6) COMMENT '累计每股股息',
    yoy_cum_revenue DECIMAL(20, 4) COMMENT '去年同期累计营业额',
    yoy_cum_profit_before_tax DECIMAL(20, 4) COMMENT '去年同期累计税前利润',
    yoy_cum_net_profit DECIMAL(20, 4) COMMENT '去年同期累计净利润',
    yoy_cum_eps DECIMAL(20, 6) COMMENT '去年同期累计每股盈利',
    yoy_cum_dps DECIMAL(20, 6) COMMENT '去年同期累计每股股息',
    nta_ps_qtr_end DECIMAL(20, 6) COMMENT '季末每股净资产',
    nta_ps_prev_year_end DECIMAL(20, 6) COMMENT '上年度末每股净资产',
    category VARCHAR(128) COMMENT '公告分类',
    attachment TEXT COMMENT '附件列表',
    url VARCHAR(512) COMMENT '公告来源地址',
    last_update DATETIME COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_qtr_stock_end (stock_code, fin_qtr_end)
) ENGINE = InnoDB COMMENT = '季度财务业绩';
-- ------------------------------------------------------------
-- 上市资料 (Listing profile)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS listing_profile (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ref_id VARCHAR(64) COMMENT '公告参考编号',
    ref_id_amend VARCHAR(64) COMMENT '修订前参考编号',
    stock_code VARCHAR(16) COMMENT '股票代码',
    en_type VARCHAR(32) COMMENT '公告类型',
    announce_date DATE COMMENT '公告日期',
    listing_date DATE COMMENT '上市日期',
    issue_price DECIMAL(20, 6) COMMENT '发行/认购价',
    issue_size DECIMAL(24, 4) COMMENT '发行规模',
    indicator VARCHAR(32) COMMENT '发行规模单位指示符',
    expiry_date DATE COMMENT '到期/届满日期',
    conv_ratio VARCHAR(64) COMMENT '行使/换股比率',
    strike_price DECIMAL(20, 6) COMMENT '行使/换股价',
    category VARCHAR(128) COMMENT '公告分类',
    description VARCHAR(1024) COMMENT '描述',
    redemption VARCHAR(1024) COMMENT '赎回条款',
    attachment TEXT COMMENT '附件列表',
    url VARCHAR(512) COMMENT '公告来源地址',
    last_update DATETIME COMMENT '记录更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_listing_stock_ref (stock_code, ref_id)
) ENGINE = InnoDB COMMENT = '证券上市资料';
-- ------------------------------------------------------------
-- 待处理公告队列 (Announcement queue)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS announcement_queue (
    queue_id BIGINT NOT NULL AUTO_INCREMENT,
    en_type VARCHAR(32) COMMENT '公告类型',
    announce_date DATE COMMENT '公告日期',
    stock_code VARCHAR(16) COMMENT '股票代码',
    subject VARCHAR(512) COMMENT '公告主题',
    url VARCHAR(512) COMMENT '公告来源地址',
    process_state INT DEFAULT 0 COMMENT '处理状态 0待处理/1成功/-1失败',
    has_error TINYINT(1) DEFAULT 0 COMMENT '是否出错',
    PRIMARY KEY (queue_id),
    UNIQUE KEY uk_queue_url (url)
) ENGINE = InnoDB COMMENT = '待处理公告队列';
-- ------------------------------------------------------------
-- 最后处理时间标记 (Last EOD marker)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS last_eod_marker (
    en_type VARCHAR(32) NOT NULL COMMENT '公告类型',
    last_update DATETIME COMMENT '最后更新时间',
    PRIMARY KEY (en_type)
) ENGINE = InnoDB COMMENT = '各公告类型最后处理时间';
-- ------------------------------------------------------------
-- 交易假期 (Trading holiday)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS trading_holiday (
    holiday_date DATE NOT NULL COMMENT '假期日期',
    description VARCHAR(128) COMMENT '假期描述',
    exchange VARCHAR(8) COMMENT '交易所代码',
    PRIMARY KEY (holiday_date)
) ENGINE = InnoDB COMMENT = '交易日历假期';