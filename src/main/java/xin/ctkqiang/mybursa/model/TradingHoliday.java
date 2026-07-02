package xin.ctkqiang.mybursa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 交易日历假期 (Trading holiday)。
 *
 * <p>原 Python 版本从 {@code holiday_calendar} 表读取，重命名为 {@code trading_holiday}。
 * 用于判断某一天是否为非交易日。
 *
 * @author 钟智强
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trading_holiday")
public class TradingHoliday {

    /** 假期日期，主键 (Holiday date, primary key)。 */
    @Id
    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    /** 假期描述 (Holiday description)。 */
    @Column(name = "description", length = 128)
    private String description;

    /** 交易所代码 (Exchange code)。 */
    @Column(name = "exchange", length = 8)
    private String exchange;
}
