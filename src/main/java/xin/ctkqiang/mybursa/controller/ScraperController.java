package xin.ctkqiang.mybursa.controller;

import xin.ctkqiang.mybursa.scheduler.IndexScrapeScheduler;
import xin.ctkqiang.mybursa.service.TradingCalendarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 抓取控制器 (Scraper REST controller)。
 *
 * <p>MVC 中的 "C" 层，暴露手动触发抓取、查询交易状态等接口。返回 JSON。
 *
 * @author 钟智强
 */
@Slf4j
@RestController
@RequestMapping("/api/scraper")
public class ScraperController {

    private final IndexScrapeScheduler scheduler;
    private final TradingCalendarService tradingCalendarService;

    public ScraperController(IndexScrapeScheduler scheduler,
                             TradingCalendarService tradingCalendarService) {
        this.scheduler = scheduler;
        this.tradingCalendarService = tradingCalendarService;
    }

    /**
     * 手动触发一次指数行情抓取 (Manually trigger an index scrape)。
     *
     * @return 抓取结果，含保存记录数
     */
    @PostMapping("/index/run")
    public ResponseEntity<Map<String, Object>> runIndexScrape() {
        int count = scheduler.runScrape();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", count > 0);
        body.put("savedCount", count);
        return ResponseEntity.ok(body);
    }

    /**
     * 查询当前是否处于交易时段 (Check whether trading is currently open)。
     */
    @GetMapping("/trading-status")
    public ResponseEntity<Map<String, Object>> tradingStatus() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tradingOpen", tradingCalendarService.isTradingTime());
        return ResponseEntity.ok(body);
    }
}
