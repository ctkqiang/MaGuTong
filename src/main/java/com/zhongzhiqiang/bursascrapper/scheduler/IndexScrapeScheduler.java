package com.zhongzhiqiang.bursascrapper.scheduler;

import com.zhongzhiqiang.bursascrapper.service.IndexQuoteService;
import com.zhongzhiqiang.bursascrapper.service.TradingCalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 指数行情定时抓取任务 (Scheduled index scraper)。
 *
 * <p>对应原 Python 版本中基于 {@code schedule} 库、每 5 分钟运行一次的循环。此处改用
 * Spring 的 {@link Scheduled} 注解实现，仅在交易时段内触发实际抓取。
 *
 * @author 钟智强
 */
@Slf4j
@Component
public class IndexScrapeScheduler {

    private final IndexQuoteService indexQuoteService;
    private final TradingCalendarService tradingCalendarService;

    public IndexScrapeScheduler(IndexQuoteService indexQuoteService,
                                TradingCalendarService tradingCalendarService) {
        this.indexQuoteService = indexQuoteService;
        this.tradingCalendarService = tradingCalendarService;
    }

    /**
     * 每 5 分钟触发一次 (Runs every 5 minutes)。
     *
     * <p>{@code fixedDelayString} 从配置读取，默认 300000 毫秒 (5 分钟)。仅在交易时段执行抓取，
     * 非交易时段静默跳过。
     */
    @Scheduled(fixedDelayString = "${magu-tong.schedule-interval-ms:300000}")
    public void scheduledScrape() {
        if (!tradingCalendarService.isTradingTime()) {
            log.debug("非交易时段，跳过抓取 (Outside trading hours, skipping)");
            return;
        }
        runScrape();
    }

    /**
     * 执行一次抓取 (Perform one scrape run)，可被手动触发接口复用。
     *
     * @return 保存的记录数
     */
    public int runScrape() {
        log.info("开始抓取指数行情 (Starting index scrape)...");
        int count = indexQuoteService.scrapeAndSave();
        log.info("指数行情抓取完成，保存 {} 条 (Index scrape finished, saved {} rows)", count, count);
        return count;
    }
}
