package com.zhongzhiqiang.magutong.model;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 年度报告公告 (Annual report announcement)。
 *
 * <p>原 Python 版本写入 {@code gpmining.arpt} 表，重命名为语义清晰的 {@code annual_report}。
 *
 * @author 钟智强
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "annual_report")
public class AnnualReport {

    /** 自增主键 (Auto-increment surrogate key)。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 公告参考编号 (Announcement reference id)。 */
    @Column(name = "ref_id", length = 64)
    private String refId;

    /** 修订前的参考编号 (Amended reference id)。 */
    @Column(name = "ref_id_amend", length = 64)
    private String refIdAmend;

    /** 公告类型 (Announcement / entitlement type)。 */
    @Column(name = "en_type", length = 32)
    private String enType;

    /** 股票代码 (Stock code)。 */
    @Column(name = "stock_code", length = 16)
    private String stockCode;

    /** 财政年度 (Financial year)。 */
    @Column(name = "fin_year")
    private Integer finYear;

    /** 财政年度截止日 (Financial year end date)。 */
    @Column(name = "fin_year_end")
    private LocalDate finYearEnd;

    /** 公告日期 (Announcement date)。 */
    @Column(name = "announce_date")
    private LocalDate announceDate;

    /** 公告分类 (Announcement category)。 */
    @Column(name = "category", length = 128)
    private String category;

    /** 附件列表 (Attachment list, name|url per line)。 */
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
