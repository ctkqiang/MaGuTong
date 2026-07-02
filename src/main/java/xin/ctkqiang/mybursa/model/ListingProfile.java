package xin.ctkqiang.mybursa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * 证券上市资料 (Security listing profile)。
 *
 * <p>
 * 对应数据库表 {@code listing_profile}，存储上市公司基本概况。
 * 主要用于窝轮 (warrant)、结构性产品等衍生工具的上市信息。
 *
 * @author 钟智强
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "listing_profile")
public class ListingProfile {

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

    /** 股票代码 (Stock code)。 */
    @Column(name = "stock_code", length = 16)
    private String stockCode;

    /** 公告类型 (Entitlement type)。 */
    @Column(name = "en_type", length = 32)
    private String enType;

    /** 公告日期 (Announcement date)。 */
    @Column(name = "announce_date")
    private LocalDate announceDate;

    /** 上市日期 (Listing date)。 */
    @Column(name = "listing_date")
    private LocalDate listingDate;

    /** 发行/认购价 (Issue / ask price)。 */
    @Column(name = "issue_price")
    private BigDecimal issuePrice;

    /** 发行规模 (Issue size)。 */
    @Column(name = "issue_size")
    private BigDecimal issueSize;

    /** 发行规模单位指示符 (Issue size indicator)。 */
    @Column(name = "indicator", length = 32)
    private String indicator;

    /** 到期/届满日期 (Maturity / expiry date)。 */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /** 行使/换股比率 (Exercise / conversion ratio)。 */
    @Column(name = "conv_ratio", length = 64)
    private String convRatio;

    /** 行使/换股价 (Strike / conversion price)。 */
    @Column(name = "strike_price")
    private BigDecimal strikePrice;

    /** 公告分类 (Announcement category)。 */
    @Column(name = "category", length = 128)
    private String category;

    /** 描述 (Description)。 */
    @Column(name = "description", length = 1024)
    private String description;

    /** 赎回条款 (Redemption terms)。 */
    @Column(name = "redemption", length = 1024)
    private String redemption;

    /** 附件列表 (Attachment list, name|url per line)。 */
    @jakarta.persistence.Lob
    @Column(name = "attachment")
    private String attachment;

    /** 公告来源地址 (Source announcement URL)。 */
    @Column(name = "url", length = 512)
    private String url;

    /** 记录写入/更新时间 (Row last update timestamp)。 */
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
}
