package com.zhongzhiqiang.magutong.repository;

import com.zhongzhiqiang.magutong.model.TradingHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 交易假期持久层 (TradingHoliday repository)。
 *
 * @author 钟智强
 */
@Repository
public interface TradingHolidayRepository extends JpaRepository<TradingHoliday, LocalDate> {

    /** 判断某日期是否为假期 (Check whether a given date is a holiday)。 */
    boolean existsByHolidayDate(LocalDate holidayDate);
}
