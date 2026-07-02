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

import java.time.LocalDate;

/**
 * 待处理公告队列 (Pending announcement queue)。
 *
 * <p>原 Python 版本写入 {@code gpmining.bursapending} 表，重命名为 {@code announcement_queue}。
 * 抓取器先将待抓取公告入队，解析器处理完成后将 {@code processState} 标记为已完成。
 *
 * <p>{@code processState} 状态约定 (State convention)：
 * <ul>
 *     <li>{@code 0} —— 待处理 (pending)</li>
 *     <li>{@code 1} —— 处理成功 (done)</li>
 *     <li>{@code -1} —— 处理失败 (error)</li>
 * </ul>
 *
 * @author 钟智强
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "announcement_queue")
public class AnnouncementQueue {

    /** 自增主键 (Auto-increment surrogate key)。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Long queueId;

    /** 公告类型 (Entitlement type)。 */
    @Column(name = "en_type", length = 32)
    private String enType;

    /** 公告日期 (Announcement date)。 */
    @Column(name = "announce_date")
    private LocalDate announceDate;

    /** 股票代码 (Stock code)。 */
    @Column(name = "stock_code", length = 16)
    private String stockCode;

    /** 公告主题 (Announcement subject)。 */
    @Column(name = "subject", length = 512)
    private String subject;

    /** 公告来源地址，业务唯一键 (Source URL, business unique key)。 */
    @Column(name = "url", length = 512, unique = true)
    private String url;

    /** 处理状态 (Process state: 0 pending / 1 done / -1 error)。 */
    @Column(name = "process_state")
    @Builder.Default
    private Integer processState = 0;

    /** 是否出错 (Error flag)。 */
    @Column(name = "has_error")
    @Builder.Default
    private Boolean hasError = false;
}
