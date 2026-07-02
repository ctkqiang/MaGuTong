package com.zhongzhiqiang.magutong.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Playwright 浏览器生命周期配置 (Playwright browser lifecycle configuration)。
 *
 * <p>将 {@link Playwright} 与 {@link Browser} 声明为单例 Bean，由 Spring 容器统一管理，
 * 避免像原 Python 版本那样在每次抓取时反复创建/销毁浏览器进程，显著降低开销。
 *
 * <p>注意：此处使用本地无头 Chromium，<b>并未</b>依赖任何第三方抓取代理服务
 * (No ZenRows / no external scraping API — a real local browser is used)。
 *
 * @author 钟智强
 */
@Slf4j
@Configuration
public class PlaywrightConfig {

    private final ScraperProperties properties;
    private Playwright playwright;
    private Browser browser;

    public PlaywrightConfig(ScraperProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建 Playwright 单例 (Create the singleton Playwright instance)。
     */
    @Bean
    public Playwright playwright() {
        log.info("正在初始化 Playwright (Initialising Playwright)...");
        this.playwright = Playwright.create();
        return this.playwright;
    }

    /**
     * 创建无头 Chromium 浏览器单例 (Create a singleton headless Chromium browser)。
     *
     * @param playwright 由容器注入的 Playwright 实例
     */
    @Bean
    public Browser browser(Playwright playwright) {
        log.info("正在启动 Chromium 浏览器, headless={} (Launching Chromium)", properties.isHeadless());
        this.browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(properties.isHeadless())
                        .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage")));
        return this.browser;
    }

    /**
     * 应用关闭时释放浏览器资源 (Release browser resources on shutdown)。
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭 Playwright 资源 (Closing Playwright resources)...");
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
