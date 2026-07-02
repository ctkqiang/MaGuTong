package xin.ctkqiang.mybursa.service;

import xin.ctkqiang.mybursa.config.ScraperProperties;
import xin.ctkqiang.mybursa.repository.TradingHolidayRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 交易日历服务 (Trading calendar service)。
 *
 * <p>对应原 Python 版本的 {@code is_trading_time} 与 {@code load_holidays}。判断当前时刻
 * 是否处于交易时段：需同时满足 —— 非周末、非假期、且落在上午或下午交易时段内。
 *
 * @author 钟智强
 */
@Slf4j
@Service
public class TradingCalendarService {

    private final TradingHolidayRepository holidayRepository;
    private final ScraperProperties.TradingHours tradingHours;

    public TradingCalendarService(TradingHolidayRepository holidayRepository,
                                  ScraperProperties properties) {
        this.holidayRepository = holidayRepository;
        this.tradingHours = properties.getTradingHours();
    }

    /**
     * 判断当前是否为交易时段 (Whether the current moment is within trading hours)。
     *
     * @return true 表示可以抓取
     */
    public boolean isTradingTime() {
        return isTradingTime(LocalDateTime.now());
    }

    /**
     * 判断指定时刻是否为交易时段 (可测试重载, testable overload)。
     *
     * @param now 待判断时刻
     */
    public boolean isTradingTime(LocalDateTime now) {
        // 1) 排除周末 (Exclude weekend)
        int dayOfWeek = now.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        if (tradingHours.getWeekendDays().contains(dayOfWeek)) {
            return false;
        }

        // 2) 排除假期 (Exclude holidays)
        if (isHoliday(now.toLocalDate())) {
            return false;
        }

        // 3) 判断交易时段 (Within a session window)
        LocalTime time = now.toLocalTime();
        boolean morning = withinWindow(time, tradingHours.getMorningStart(), tradingHours.getMorningEnd());
        boolean afternoon = withinWindow(time, tradingHours.getAfternoonStart(), tradingHours.getAfternoonEnd());
        return morning || afternoon;
    }

    /**
     * 查询某日期是否为假期 (Check whether a date is a holiday)，数据库异常时安全降级为非假期。
     */
    public boolean isHoliday(LocalDate date) {
        try {
            return holidayRepository.existsByHolidayDate(date);
        } catch (Exception e) {
            log.error("查询假期出错，按非假期处理 (Error checking holiday, treating as non-holiday): {}",
                    e.getMessage());
            return false;
        }
    }

    /** 判断时间是否落在 [start, end) 区间内 (Half-open interval check)。 */
    private boolean withinWindow(LocalTime time, LocalTime start, LocalTime end) {
        return !time.isBefore(start) && time.isBefore(end);
    }
}
