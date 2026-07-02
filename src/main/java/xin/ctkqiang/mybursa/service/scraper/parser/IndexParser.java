package xin.ctkqiang.mybursa.service.scraper.parser;

import xin.ctkqiang.mybursa.model.IndexQuote;
import xin.ctkqiang.mybursa.util.ParseUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 指数行情解析器 (Index quote parser)。
 *
 * <p>对应原 Python 版本 {@code dm_indices.process_indices}。负责将行情页面中的
 * {@code DataTables_Table_0} 表格解析为 {@link IndexQuote} 实体列表，并计算前收盘价。
 *
 * @author 钟智强
 */
@Slf4j
@Component
public class IndexParser {

    /** 从 Name 列链接中提取 stock_code 的正则 (Regex to extract stock_code from query string)。 */
    private static final Pattern STOCK_CODE_PARAM = Pattern.compile("[?&]stock_code=([^&]+)");

    /** 页面 "As at ..." 时间格式 (Timestamp format on page)。 */
    private static final DateTimeFormatter AS_AT_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy hh:mm a", Locale.ENGLISH);

    /** 需要提取的列名 (Columns we care about)。 */
    private static final List<String> TARGET_FIELDS =
            List.of("stock_code", "NAME", "OPEN", "HIGH", "LOW", "LAST DONE", "CHG");

    /**
     * 解析结果封装 (Parse result wrapper)：实体列表 + 页面时间戳。
     */
    public record IndexParseResult(List<IndexQuote> quotes, LocalDateTime asAt) {
    }

    /**
     * 解析行情 HTML (Parse indices HTML)。
     *
     * @param html 渲染后的页面 HTML
     * @return 解析结果，包含实体列表与页面时间戳
     */
    public IndexParseResult parse(String html) {
        Document doc = Jsoup.parse(html);
        Element table = doc.getElementById("DataTables_Table_0");
        if (table == null) {
            throw new IllegalStateException("未找到指数表格 DataTables_Table_0 (Indices table not found)");
        }

        // 表头 -> 列索引映射 (Header text -> column index)
        Elements headerCells = table.select("th");
        List<String> headers = new ArrayList<>();
        for (Element th : headerCells) {
            headers.add(th.text().trim());
        }

        Map<String, Integer> colIndex = resolveColumnIndices(headers);
        LocalDateTime asAt = parseAsAt(doc);

        List<IndexQuote> quotes = new ArrayList<>();
        Elements rows = table.select("tbody tr");
        for (Element tr : rows) {
            Elements cols = tr.select("td");
            if (cols.isEmpty()) {
                continue;
            }

            // 从 Name 列 (第二列) 的链接里解析 stock_code
            String stockCode = extractStockCode(cols.get(1));
            if (stockCode == null) {
                continue;
            }
            // 若以 'I' 结尾则转换为 "i" + 去掉末位 (Transform codes ending in 'I')
            if (stockCode.endsWith("I")) {
                stockCode = "i" + stockCode.substring(0, stockCode.length() - 1);
            }

            String lastDone = cellText(cols, colIndex, "LAST DONE");
            String chg = cellText(cols, colIndex, "CHG");
            BigDecimal prevPrice = computePrevPrice(lastDone, chg);

            IndexQuote quote = IndexQuote.builder()
                    .tickerId(stockCode + ".KL")
                    .stockCode(stockCode)
                    .symbol(cellText(cols, colIndex, "NAME"))
                    .exchange("KL")
                    .sectorId(2000)
                    .prevPrice(prevPrice)
                    .openPrice(ParseUtils.toDecimal(cellText(cols, colIndex, "OPEN")))
                    .highPrice(ParseUtils.toDecimal(cellText(cols, colIndex, "HIGH")))
                    .lowPrice(ParseUtils.toDecimal(cellText(cols, colIndex, "LOW")))
                    .lastPrice(ParseUtils.toDecimal(lastDone))
                    .lastUpdate(asAt)
                    .build();
            quotes.add(quote);
        }

        log.info("解析得到 {} 条行情记录 (Parsed {} quote rows)", quotes.size(), quotes.size());
        return new IndexParseResult(quotes, asAt);
    }

    /**
     * 计算前收盘价 (Compute previous close price)。
     *
     * <p>规则同原 Python 版本：
     * <ul>
     *     <li>最新价与涨跌皆为 "-"：无前收盘价</li>
     *     <li>有最新价但无涨跌：前收盘价 = 最新价</li>
     *     <li>两者皆有：前收盘价 = 最新价 - 涨跌</li>
     * </ul>
     */
    private BigDecimal computePrevPrice(String lastDone, String chg) {
        boolean lastMissing = lastDone == null || lastDone.isBlank() || "-".equals(lastDone);
        boolean chgMissing = chg == null || chg.isBlank() || "-".equals(chg);

        if (lastMissing) {
            return null;
        }
        BigDecimal last = ParseUtils.toDecimal(lastDone);
        if (last == null) {
            return null;
        }
        if (chgMissing) {
            return last;
        }
        BigDecimal change = ParseUtils.toDecimal(chg);
        return (change == null) ? last : last.subtract(change);
    }

    /**
     * 建立目标字段到列索引的映射 (Map target field name -> column index)，大小写不敏感、支持部分匹配。
     */
    private Map<String, Integer> resolveColumnIndices(List<String> headers) {
        Map<String, Integer> result = new HashMap<>();
        for (String field : TARGET_FIELDS) {
            if ("stock_code".equals(field)) {
                continue; // stock_code 由链接解析，不在表头
            }
            boolean found = false;
            for (int i = 0; i < headers.size(); i++) {
                if (headers.get(i).toLowerCase().contains(field.toLowerCase())) {
                    result.put(field, i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.warn("表头未找到字段 (Header not found): {}", field);
            }
        }
        return result;
    }

    /**
     * 读取指定字段的单元格文本并去除千分位逗号 (Read a cell's text, stripping commas)。
     */
    private String cellText(Elements cols, Map<String, Integer> colIndex, String field) {
        Integer idx = colIndex.get(field);
        if (idx == null || idx >= cols.size()) {
            return null;
        }
        return cols.get(idx).text().trim().replace(",", "");
    }

    /**
     * 从 Name 单元格链接中解析 stock_code (Extract stock_code from the anchor href)。
     */
    private String extractStockCode(Element nameCell) {
        Element anchor = nameCell.selectFirst("a");
        if (anchor == null) {
            return null;
        }
        Matcher m = STOCK_CODE_PARAM.matcher(anchor.attr("href"));
        return m.find() ? m.group(1) : null;
    }

    /**
     * 解析页面右上角 "As at 25 Nov 2025 02:04 PM" 时间戳 (Parse the "As at" timestamp)。
     */
    private LocalDateTime parseAsAt(Document doc) {
        Element asAtTag = doc.selectFirst("div.bweb-right p.mb-1");
        if (asAtTag == null) {
            return null;
        }
        String text = asAtTag.text().replace("As at ", "").trim();
        try {
            return LocalDateTime.parse(text, AS_AT_FORMAT);
        } catch (Exception e) {
            log.warn("无法解析页面时间戳 (Cannot parse as-at timestamp): {}", text);
            return null;
        }
    }
}
