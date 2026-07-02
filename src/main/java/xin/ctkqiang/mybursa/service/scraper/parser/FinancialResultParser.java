package xin.ctkqiang.mybursa.service.scraper.parser;

import xin.ctkqiang.mybursa.dto.AnnouncementInfo;
import xin.ctkqiang.mybursa.model.QuarterlyResult;
import xin.ctkqiang.mybursa.util.ParseUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 季度财务业绩解析器 (Quarterly financial result parser)。
 *
 * <p>
 * 对应原 Python 版本 {@code dm_financial_result.process_financial_result}。
 * 先从页面头部表格解析财政年度/季度信息，再从首个 {@code ven_table} 财务表格
 * 按行号与行标签解析各项财务指标。
 *
 * @author 钟智强
 */
@Slf4j
@Component
public class FinancialResultParser {

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private static final String ID_REVENUE = "revenue";
    private static final String ID_PROFIT_BEFORE_TAX = "profit/(loss) before tax";
    private static final String ID_NET_PROFIT_PERIOD = "profit/(loss) for the period";
    private static final String ID_NET_PROFIT = "profit/(loss) attributable to ordinary equity holders of the parent";
    private static final String ID_NET_PROFIT_3 = "net profit/(loss) for the period";
    private static final List<String> ID_EPS = List.of(
            "basic earnings/(loss) per share (subunit)",
            "basic earnings/(loss) per share (sen)");
    private static final List<String> ID_DPS = List.of(
            "proposed/declared dividend per share (subunit)",
            "proposed/declared dividend per share (sen)",
            "dividend per share (sen)");
    private static final List<String> ID_NTA = List.of(
            "net assets per share attributable to ordinary equity holders of the parent",
            "net assets per share attributable to ordinary equity holders of the parent (rm)",
            "net assets per share (rm)",
            "net tangible assets per share (rm)");

    /**
     * 解析季度业绩页面 (Parse a quarterly result page)。
     *
     * @param html      渲染后的 HTML
     * @param info      公告头部信息
     * @param stockCode 股票代码
     * @return 组装好的 {@link QuarterlyResult} 实体 (尚未持久化)
     */
    public QuarterlyResult parse(String html, AnnouncementInfo info, String stockCode) {
        Document doc = Jsoup.parse(html);

        QuarterlyResult result = new QuarterlyResult();
        result.setStockCode(stockCode);
        result.setRefId(info.getRefId());
        result.setRefIdAmend(info.getRefIdAmend());
        result.setAnnounceDate(info.getAnnounceDate());
        result.setCategory(info.getCategory());
        result.setUrl(info.getUrl());
        result.setAttachment(ParseUtils.extractAttachment(doc, info.getUrl()));

        parseHeader(doc, result);
        parseFinancialTable(doc, result);

        return result;
    }

    /**
     * 解析头部表格 {@code table.formContentTable}，提取财政年度/季度信息。
     */
    private void parseHeader(Document doc, QuarterlyResult result) {
        Elements rows = doc.select("table.formContentTable tr");
        if (rows.isEmpty()) {
            throw new IllegalStateException(
                    "未找到财务头部表格 formContentTable (Financial header table not found)");
        }
        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.size() < 2) {
                continue;
            }
            String id = ParseUtils.removeWhiteSpace(cells.get(0).text()).toLowerCase();
            String value = cells.get(1).text().trim();

            boolean isQuarterlyPeriod = id.equals("quarterly report for the financial period ended")
                    || ((id.contains("quarterly report") || id.contains("half yearly report"))
                            && id.contains("for the financial period ended"));

            if (isQuarterlyPeriod) {
                result.setFinQtrEnd(ParseUtils.fromDayMonthYear(value));
            } else if (id.equals("financial year end")
                    || id.equals("annual report for financial year ended")) {
                LocalDate finYearEnd = ParseUtils.fromDayMonthYear(value);
                result.setFinYearEnd(finYearEnd);
                if (finYearEnd != null) {
                    result.setFinYear(finYearEnd.getYear());
                }
            } else if (id.equals("quarter")) {
                result.setFinQtr(firstInt(value, 1));
            } else if (id.equals("half year")) {
                // 半年度报告：季度序号 = 提取数字 * 2 (Half-year -> quarter number * 2)
                result.setFinQtr(firstInt(value, 1) * 2);
            }
        }
    }

    /**
     * 解析首个 {@code ven_table} 财务数据表格 (Parse the first ven_table with figures)。
     *
     * <p>
     * 依据行号 (第一列 1-7) 与行标签双重判断，避免误匹配。
     */
    private void parseFinancialTable(Document doc, QuarterlyResult result) {
        Element table = doc.selectFirst("table.ven_table");
        if (table == null) {
            throw new IllegalStateException("未找到财务数据表格 ven_table (Financial figures table not found)");
        }
        for (Element row : table.select("tr")) {
            Elements cells = row.select("td, th");
            List<String> rowData = new ArrayList<>();
            for (Element c : cells) {
                rowData.add(c.text().trim());
            }
            if (rowData.size() < 2) {
                continue;
            }

            int rowNo = safeInt(rowData.get(0));
            String id = rowData.get(1).toLowerCase();

            if (rowNo == 1 && id.equals(ID_REVENUE)) {
                assignFourFigures(rowData, result::setRevenue, result::setYoyRevenue,
                        result::setCumRevenue, result::setYoyCumRevenue);
            } else if (rowNo == 2 && id.equals(ID_PROFIT_BEFORE_TAX)) {
                assignFourFigures(rowData, result::setProfitBeforeTax, result::setYoyProfitBeforeTax,
                        result::setCumProfitBeforeTax, result::setYoyCumProfitBeforeTax);
            } else if ((rowNo == 3 && id.equals(ID_NET_PROFIT_PERIOD))
                    || (rowNo == 4 && (id.equals(ID_NET_PROFIT) || id.equals(ID_NET_PROFIT_3)))) {
                assignFourFigures(rowData, result::setNetProfit, result::setYoyNetProfit,
                        result::setCumNetProfit, result::setYoyCumNetProfit);
            } else if (rowNo == 5 && ID_EPS.contains(id)) {
                assignFourFigures(rowData, result::setEps, result::setYoyEps,
                        result::setCumEps, result::setYoyCumEps);
            } else if (rowNo == 6 && ID_DPS.contains(id)) {
                assignFourFigures(rowData, result::setDps, result::setYoyDps,
                        result::setCumDps, result::setYoyCumDps);
            } else if (rowNo == 7 && ID_NTA.contains(id)) {
                result.setNtaPsQtrEnd(figureAt(rowData, 2));
                result.setNtaPsPrevYearEnd(figureAt(rowData, 3));
            }
        }
    }

    /**
     * 将一行中第 2-5 列 (当季/同比/累计/去年累计) 依次赋值 (Assign the four figure columns)。
     */
    private void assignFourFigures(List<String> rowData,
            java.util.function.Consumer<BigDecimal> current,
            java.util.function.Consumer<BigDecimal> yoy,
            java.util.function.Consumer<BigDecimal> cum,
            java.util.function.Consumer<BigDecimal> yoyCum) {
        current.accept(figureAt(rowData, 2));
        yoy.accept(figureAt(rowData, 3));
        cum.accept(figureAt(rowData, 4));
        yoyCum.accept(figureAt(rowData, 5));
    }

    /** 读取指定列并转为 {@link BigDecimal} (Read a column as BigDecimal, 0 default)。 */
    private BigDecimal figureAt(List<String> rowData, int idx) {
        if (idx >= rowData.size()) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(ParseUtils.getNumber(rowData.get(idx)));
    }

    /** 安全转 int，失败返回 0 (Safe int parse)。 */
    private int safeInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 提取字符串中第一个整数，无则返回默认值 (First integer in string)。 */
    private int firstInt(String s, int defaultValue) {
        Matcher m = DIGITS.matcher(s);
        return m.find() ? Integer.parseInt(m.group()) : defaultValue;
    }
}
