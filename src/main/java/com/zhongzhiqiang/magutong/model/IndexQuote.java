package com.zhongzhiqiang.magutong.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 指数/证券实时行情快照 (Real-time index / security quote snapshot)。
 *
 * <p>原 Python 版本写入 {@code rtdata.image} 表，此处按更清晰的命名重命名为
 * {@code market_index_quote}。字段命名遵循数据库 snake_case、Java 字段 camelCase 的约定。
 *
 * @author 钟智强
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "market_index_quote")
public class IndexQuote {

    /** 主键：行情代码 (Primary key: ticker id, e.g. "5099.KL")。 */
    @Id
    @Column(name = "ticker_id", length = 32, nullable = false)
    private String tickerId;

    /** 股票代码 (Stock code)。 */
    @Column(name = "stock_code", length = 16)
    private String stockCode;

    /** 证券名称 (Security symbol / name)。 */
    @Column(name = "symbol", length = 128)
    private String symbol;

    /** 交易所代码 (Exchange code, fixed "KL")。 */
    @Column(name = "exchange", length = 8)
    private String exchange;

    /** 板块编号 (Sector id)。 */
    @Column(name = "sector_id")
    private Integer sectorId;

    /** 前收盘价 (Previous close price)。 */
    @Column(name = "prev_price")
    private BigDecimal prevPrice;

    /** 开盘价 (Open price)。 */
    @Column(name = "open_price")
    private BigDecimal openPrice;

    /** 最高价 (High price)。 */
    @Column(name = "high_price")
    private BigDecimal highPrice;

    /** 最低价 (Low price)。 */
    @Column(name = "low_price")
    private BigDecimal lowPrice;

    /** 最新成交价 (Last done price)。 */
    @Column(name = "last_price")
    private BigDecimal lastPrice;

    /** 最后更新时间 (Last update timestamp from the source page)。 */
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
}
