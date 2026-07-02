package com.zhongzhiqiang.magutong.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * 抓取相关配置属性 (Scraper configuration properties)。
 *
 * <p>
 * 所有值从 {@code application.yml} 中 {@code magu-tong.*} 前缀绑定，
 * 集中管理抓取行为，避免在代码中硬编码魔法值。
 *
 * @author 钟智强
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "magu-tong")
public class ScraperProperties {

    /** 指数行情页面地址 (Indices prices page URL)。 */
    private String indicesUrl = "https://www.bursamalaysia.com/market_information/indices_prices#";

    /** 单次抓取的最大重试次数 (Max retries per fetch)。 */
    private int maxRetries = 3;

    /** 页面导航超时毫秒数 (Navigation timeout in milliseconds)。 */
    private int navigationTimeoutMs = 20000;

    /** 点击后等待表格刷新的毫秒数 (Wait after clicking to let the table refresh)。 */
    private int tableSettleMs = 2000;

    /** 是否以无头模式运行浏览器 (Run browser in headless mode)。 */
    private boolean headless = true;

    /** 浏览器 User-Agent。 */
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /** 交易日历配置 (Trading calendar configuration)。 */
    private TradingHours tradingHours = new TradingHours();

    /**
     * 交易时段设置 (Trading session windows)。
     * 马来西亚交易所分上午与下午两个交易时段。
     */
    @Getter
    @Setter
    public static class TradingHours {
        /** 上午时段开始 (Morning session start)。 */
        private LocalTime morningStart = LocalTime.of(9, 0);
        /** 上午时段结束 (Morning session end)。 */
        private LocalTime morningEnd = LocalTime.of(13, 0);
        /** 下午时段开始 (Afternoon session start)。 */
        private LocalTime afternoonStart = LocalTime.of(14, 30);
        /** 下午时段结束 (Afternoon session end)。 */
        private LocalTime afternoonEnd = LocalTime.of(17, 30);
        /** 视为周末的星期几 (Days treated as weekend: 6=Sat, 7=Sun)。 */
        private List<Integer> weekendDays = List.of(6, 7);
    }
}
