package xin.ctkqiang.mybursa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 季度财务业绩 (Quarterly financial result)。
 *
 * <p>原 Python 版本写入 {@code gpmining.qrpt} 表，重命名为 {@code quarterly_result}。
 * 字段涵盖当季与累计 (cumulative) 数据，以及同比 (year-over-year) 对照数据。
 *
 * <p>命名约定：{@code yoy*} 表示同比数据，{@code cum*} 表示本财年累计数据，
 * {@code yoyCum*} 表示去年同期累计数据。
 *
 * @author 钟智强
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quarterly_result")
public class QuarterlyResult {

    /** 自增主键 (Auto-increment surrogate key)。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 股票代码 (Stock code)。 */
    @Column(name = "stock_code", length = 16)
    private String stockCode;

    /** 财政季度截止日 (Financial quarter end date) —— 与 stock_code 组成业务唯一键。 */
    @Column(name = "fin_qtr_end")
    private LocalDate finQtrEnd;

    /** 财政年度截止日 (Financial year end date)。 */
    @Column(name = "fin_year_end")
    private LocalDate finYearEnd;

    /** 财政年度 (Financial year)。 */
    @Column(name = "fin_year")
    private Integer finYear;

    /** 财政季度序号 (Financial quarter number, 1-4)。 */
    @Column(name = "fin_qtr")
    private Integer finQtr;

    /** 公告参考编号 (Announcement reference id)。 */
    @Column(name = "ref_id", length = 64)
    private String refId;

    /** 修订前的参考编号 (Amended reference id)。 */
    @Column(name = "ref_id_amend", length = 64)
    private String refIdAmend;

    /** 公告日期 (Announcement date)。 */
    @Column(name = "announce_date")
    private LocalDate announceDate;

    // ---- 当季数据 (Current quarter figures) ----
    /** 营业额 (Revenue)。 */
    @Column(name = "revenue")
    private BigDecimal revenue;
    /** 税前利润 (Profit before tax)。 */
    @Column(name = "profit_before_tax")
    private BigDecimal profitBeforeTax;
    /** 净利润 (Net profit)。 */
    @Column(name = "net_profit")
    private BigDecimal netProfit;
    /** 每股盈利 (Earnings per share)。 */
    @Column(name = "eps")
    private BigDecimal eps;
    /** 每股股息 (Dividend per share)。 */
    @Column(name = "dps")
    private BigDecimal dps;

    // ---- 同比数据 (Year-over-year figures) ----
    @Column(name = "yoy_revenue")
    private BigDecimal yoyRevenue;
    @Column(name = "yoy_profit_before_tax")
    private BigDecimal yoyProfitBeforeTax;
    @Column(name = "yoy_net_profit")
    private BigDecimal yoyNetProfit;
    @Column(name = "yoy_eps")
    private BigDecimal yoyEps;
    @Column(name = "yoy_dps")
    private BigDecimal yoyDps;

    // ---- 本财年累计数据 (Cumulative figures) ----
    @Column(name = "cum_revenue")
    private BigDecimal cumRevenue;
    @Column(name = "cum_profit_before_tax")
    private BigDecimal cumProfitBeforeTax;
    @Column(name = "cum_net_profit")
    private BigDecimal cumNetProfit;
    @Column(name = "cum_eps")
    private BigDecimal cumEps;
    @Column(name = "cum_dps")
    private BigDecimal cumDps;

    // ---- 去年同期累计数据 (Prior-year cumulative figures) ----
    @Column(name = "yoy_cum_revenue")
    private BigDecimal yoyCumRevenue;
    @Column(name = "yoy_cum_profit_before_tax")
    private BigDecimal yoyCumProfitBeforeTax;
    @Column(name = "yoy_cum_net_profit")
    private BigDecimal yoyCumNetProfit;
    @Column(name = "yoy_cum_eps")
    private BigDecimal yoyCumEps;
    @Column(name = "yoy_cum_dps")
    private BigDecimal yoyCumDps;

    /** 季末每股净资产 (Net assets per share at quarter end)。 */
    @Column(name = "nta_ps_qtr_end")
    private BigDecimal ntaPsQtrEnd;
    /** 上一年度末每股净资产 (Net assets per share at previous year end)。 */
    @Column(name = "nta_ps_prev_year_end")
    private BigDecimal ntaPsPrevYearEnd;

    /** 公告分类 (Announcement category)。 */
    @Column(name = "category", length = 128)
    private String category;

    /** 附件列表 (Attachment list)。 */
    @Lob
    @Column(name = "attachment")
    private String attachment;

    /** 公告来源地址 (Source announcement URL)。 */
    @Column(name = "url", length = 512)
    private String url;

    /** 记录写入/更新时间 (Row last update timestamp)。 */
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
}
