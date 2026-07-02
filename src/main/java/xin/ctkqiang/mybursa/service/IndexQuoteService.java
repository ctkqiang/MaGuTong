package xin.ctkqiang.mybursa.service;

import xin.ctkqiang.mybursa.model.IndexQuote;
import xin.ctkqiang.mybursa.repository.IndexQuoteRepository;
import xin.ctkqiang.mybursa.service.scraper.PageFetcher;
import xin.ctkqiang.mybursa.service.scraper.parser.IndexParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 指数行情业务服务 (Index quote service)。
 *
 * <p>
 * 编排 "抓取 -> 解析 -> 持久化" 全流程，对应原 Python 版本 {@code dm_indices.process_indices}
 * 中的数据库写入部分。使用 {@code saveAll} 完成插入/更新 (upsert)，由主键 {@code ticker_id} 保证幂等。
 *
 * @author 钟智强
 */
@Slf4j
@Service
public class IndexQuoteService {

    private final PageFetcher pageFetcher;
    private final IndexParser indexParser;
    private final IndexQuoteRepository repository;

    @Value("${magu-tong.indices-url}")
    private String indicesUrl;

    public IndexQuoteService(PageFetcher pageFetcher,
            IndexParser indexParser,
            IndexQuoteRepository repository) {
        this.pageFetcher = pageFetcher;
        this.indexParser = indexParser;
        this.repository = repository;
    }

    /**
     * 抓取并保存指数行情 (Scrape and persist index quotes)。
     *
     * @return 保存的记录数，失败返回 0
     */
    @Transactional
    public int scrapeAndSave() {
        String html = pageFetcher.fetchIndicesPage(indicesUrl);
        if (html == null) {
            log.error("指数页面抓取失败 (Failed to fetch indices page)");
            return 0;
        }
        IndexParser.IndexParseResult result = indexParser.parse(html);
        List<IndexQuote> quotes = result.quotes();
        if (quotes.isEmpty()) {
            log.warn("未解析到任何行情记录 (No quotes parsed)");
            return 0;
        }
        repository.saveAll(quotes);
        log.info("已保存 {} 条指数行情 (Saved {} index quotes)", quotes.size(), quotes.size());
        return quotes.size();
    }
}
