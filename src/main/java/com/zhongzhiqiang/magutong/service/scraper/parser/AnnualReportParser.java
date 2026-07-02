package com.zhongzhiqiang.magutong.service.scraper.parser;

import com.zhongzhiqiang.magutong.dto.AnnouncementInfo;
import com.zhongzhiqiang.magutong.model.AnnualReport;
import com.zhongzhiqiang.magutong.util.ParseUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 年度报告解析器 (Annual report parser)。
 *
 * <p>对应原 Python 版本 {@code dm_annual_report.process_annual_report}。从页面头部
 * {@code table.InputTable2} 提取财政年度截止日。
 *
 * @author 钟智强
 */
@Slf4j
@Component
public class AnnualReportParser {

    /**
     * 解析年度报告页面 (Parse an annual report page)。
     *
     * @param html      渲染后的 HTML
     * @param info      公告头部信息
     * @param stockCode 股票代码
     * @param enType    公告类型
     * @return 组装好的 {@link AnnualReport} 实体 (尚未持久化)
     */
    public AnnualReport parse(String html, AnnouncementInfo info, String stockCode, String enType) {
        Document doc = Jsoup.parse(html);

        Elements rows = doc.select("table.InputTable2 tr");
        if (rows.isEmpty()) {
            throw new IllegalStateException("未找到年报表格 InputTable2 (Annual report table not found)");
        }

        LocalDate finYearEnd = null;
        Integer finYear = null;

        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.size() < 2) {
                continue;
            }
            String id = ParseUtils.removeWhiteSpace(cells.get(0).text()).toLowerCase();
            String value = cells.get(1).text().trim();

            if (id.equals("financial year end")
                    || id.equals("annual report for financial year ended")) {
                finYearEnd = ParseUtils.fromDayMonthYear(value);
                finYear = (finYearEnd != null) ? finYearEnd.getYear() : null;
            } else if (id.equals("subject")) {
                // 形如 "Annual Audited Accounts - 30 Apr 2025" (Extract date after the dash)
                String[] parts = value.toLowerCase().split("annual audited accounts -", 2);
                if (parts.length > 1) {
                    LocalDate parsed = ParseUtils.fromDayMonthYear(parts[1].trim());
                    if (parsed != null) {
                        finYearEnd = parsed;
                        finYear = parsed.getYear();
                    }
                }
            }
        }

        return AnnualReport.builder()
                .refId(info.getRefId())
                .refIdAmend(info.getRefIdAmend())
                .enType(enType)
                .stockCode(stockCode)
                .finYear(finYear)
                .finYearEnd(finYearEnd)
                .announceDate(info.getAnnounceDate())
                .category(info.getCategory())
                .attachment(ParseUtils.extractAttachment(doc, info.getUrl()))
                .url(info.getUrl())
                .build();
    }
}
