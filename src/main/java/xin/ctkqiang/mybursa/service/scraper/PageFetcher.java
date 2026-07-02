package xin.ctkqiang.mybursa.service.scraper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitUntilState;
import xin.ctkqiang.mybursa.config.ScraperProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 页面抓取器 (Page fetcher)。
 *
 * <p>使用 Playwright 无头浏览器加载动态页面并返回渲染后的 HTML，对应原 Python 版本的
 * {@code extract_page} / {@code initialize_browser} 函数。内置重试机制。
 *
 * <p>本组件仅驱动本地浏览器，<b>不</b>使用任何第三方抓取 API (No ZenRows / external API)。
 *
 * @author 钟智强
 */
@Slf4j
@Component
public class PageFetcher {

    private final Browser browser;
    private final ScraperProperties properties;

    public PageFetcher(Browser browser, ScraperProperties properties) {
        this.browser = browser;
        this.properties = properties;
    }

    /**
     * 抓取指数行情页面 (Fetch the indices prices page)。
     *
     * <p>加载页面后点击 "显示 50 条" 按钮以展开全部行，等待表格渲染完成后返回 HTML。
     *
     * @param url 目标地址 (target URL)
     * @return 渲染后的 HTML，全部重试失败返回 null
     */
    public String fetchIndicesPage(String url) {
        return fetch(url, true);
    }

    /**
     * 抓取通用公告页面 (Fetch a generic announcement page)，不点击展开按钮。
     *
     * @param url 目标地址 (target URL)
     * @return 渲染后的 HTML，全部重试失败返回 null
     */
    public String fetchAnnouncementPage(String url) {
        return fetch(url, false);
    }

    /**
     * 核心抓取逻辑 (Core fetch logic with retry)。
     *
     * @param url               目标地址
     * @param expandTableEntries 是否需要点击 "显示 50 条" 展开表格
     */
    private String fetch(String url, boolean expandTableEntries) {
        int trial = 0;
        while (trial < properties.getMaxRetries()) {
            trial++;
            // 每次抓取使用独立的浏览器上下文，隔离 cookie/缓存 (Isolated context per fetch)
            try (BrowserContext context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setUserAgent(properties.getUserAgent())
                            .setViewportSize(1920, 1080))) {

                Page page = context.newPage();
                log.info("抓取页面 (Fetching): {} (第 {}/{} 次尝试)", url, trial, properties.getMaxRetries());

                Response response = page.navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.NETWORKIDLE)
                        .setTimeout(properties.getNavigationTimeoutMs()));

                if (response == null || response.status() != 200) {
                    int status = (response == null) ? -1 : response.status();
                    log.error("HTTP 状态异常 (Bad HTTP status): {}", status);
                    continue;
                }

                log.debug("页面标题 (Page title): {}", page.title());

                if (expandTableEntries) {
                    // 点击 "显示 50 条" 按钮 (Click the "show 50 entries" button)
                    page.click("a.length-button[data-length=\"50\"]");
                    page.waitForTimeout(properties.getTableSettleMs());
                }

                return page.content();

            } catch (Exception e) {
                log.error("抓取页面出错 (Error fetching page): {}", e.getMessage());
            }
        }
        log.error("在 {} 次尝试后放弃抓取 (Giving up after {} attempts): {}",
                properties.getMaxRetries(), properties.getMaxRetries(), url);
        return null;
    }
}
