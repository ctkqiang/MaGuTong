package com.zhongzhiqiang.magutong.service.scraper.parser;

import com.zhongzhiqiang.magutong.dto.AnnouncementInfo;
import com.zhongzhiqiang.magutong.model.ListingProfile;
import com.zhongzhiqiang.magutong.util.ParseUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 上市资料解析器 (Listing profile parser)。
 *
 * <p>对应原 Python 版本 {@code dm_listing_profile.process_listing_profile}。合并
 * {@code div#main} 下第一个 {@code InputTable2} 表格及其后一个兄弟表格的所有行进行解析，
 * 并处理 "Revised ..." 修订字段。
 *
 * @author 钟智强
 */
@Slf4j
@Component
public class ListingProfileParser {

    /**
     * 修订字段判定结果 (Field-revision check result)。
     *
     * @param matched   字段名是否匹配 (含 "revised " 前缀)
     * @param isRevised 是否为修订版字段
     */
    private record FieldMatch(boolean matched, boolean isRevised) {
    }

    /**
     * 解析上市资料页面 (Parse a listing profile page)。
     *
     * @param html      渲染后的 HTML
     * @param info      公告头部信息
     * @param stockCode 股票代码
     * @param enType    公告类型
     * @return 组装好的 {@link ListingProfile} 实体 (尚未持久化)
     */
    public ListingProfile parse(String html, AnnouncementInfo info, String stockCode, String enType) {
        Document doc = Jsoup.parse(html);

        Element mainDiv = doc.getElementById("main");
        if (mainDiv == null) {
            throw new IllegalStateException("未找到 div#main (Listing profile main div not found)");
        }
        Element inputTable = mainDiv.selectFirst("table.InputTable2");
        if (inputTable == null) {
            throw new IllegalStateException("未找到首个 InputTable2 (First InputTable2 not found)");
        }
        Element nextTable = inputTable.nextElementSibling();
        while (nextTable != null && !"table".equals(nextTable.tagName())) {
            nextTable = nextTable.nextElementSibling();
        }
        if (nextTable == null) {
            throw new IllegalStateException("未找到 InputTable2 之后的表格 (Sibling table not found)");
        }

        Elements rows = new Elements();
        rows.addAll(inputTable.select("tr"));
        rows.addAll(nextTable.select("tr"));

        ListingProfile profile = ListingProfile.builder()
                .refId(info.getRefId())
                .refIdAmend(info.getRefIdAmend())
                .stockCode(stockCode)
                .enType(enType)
                .announceDate(info.getAnnounceDate())
                .category(info.getCategory())
                .url(info.getUrl())
                .build();

        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.size() < 2) {
                continue;
            }
            List<String> rowData = new ArrayList<>();
            for (Element c : cells) {
                rowData.add(ParseUtils.removeWhiteSpace(c.text().trim()));
            }
            applyRow(profile, rowData.get(0).toLowerCase(), rowData.get(1));
        }

        profile.setAttachment(ParseUtils.extractAttachment(doc, info.getUrl()));
        return profile;
    }

    /**
     * 根据字段名把单行数据写入实体 (Dispatch a single row into the entity)。
     */
    private void applyRow(ListingProfile profile, String id, String value) {
        switch (id) {
            case "listing date" -> profile.setListingDate(ParseUtils.fromDayMonthYear(value));
            case "issue/ ask price" -> profile.setIssuePrice(ParseUtils.updateCurrencyValue(value));
            case "issue size indicator" -> profile.setIndicator(value);
            case "issue size in unit" -> profile.setIssueSize(parseUnitSize(value));
            case "issue size in currency" -> profile.setIssueSize(ParseUtils.updateCurrencyValue(value));
            case "description" -> profile.setDescription(value);
            case "redemption" -> profile.setRedemption(value);
            default -> applyRevisableRow(profile, id, value);
        }
    }

    /**
     * 处理可能带 "Revised" 前缀的字段 (Handle fields that may be prefixed with "Revised")。
     */
    private void applyRevisableRow(ListingProfile profile, String id, String value) {
        FieldMatch maturity = isFieldRevised(id, "maturity date");
        if (maturity.matched()) {
            if (needUpdate(value, maturity.isRevised())) {
                profile.setExpiryDate(ParseUtils.fromDayMonthYear(value));
            }
            return;
        }
        FieldMatch ratio = isFieldRevised(id, "exercise/conversion ratio");
        if (ratio.matched()) {
            if (needUpdate(value, ratio.isRevised())) {
                profile.setConvRatio(ParseUtils.getRatio(value));
            }
            return;
        }
        FieldMatch price = isFieldRevised(id, "exercise/strike/conversion price");
        FieldMatch level = isFieldRevised(id, "exercise/strike/conversion level");
        if (price.matched() || level.matched()) {
            boolean revised = price.matched() ? price.isRevised() : level.isRevised();
            if (needUpdate(value, revised)) {
                profile.setStrikePrice(ParseUtils.updateCurrencyValue(value));
            }
        }
    }

    /**
     * 判断字段名是否等于目标字段或其修订版 (Check field equals name or "revised <name>")。
     * 对应 Python {@code is_field_revised}。
     */
    private FieldMatch isFieldRevised(String id, String fieldName) {
        boolean revised = id.equalsIgnoreCase("revised " + fieldName);
        boolean matched = id.equalsIgnoreCase(fieldName) || revised;
        return new FieldMatch(matched, revised);
    }

    /**
     * 判断字段值是否需要更新 (Whether a value is worth persisting)。
     * 对应 Python {@code need_update}：空值或 "not applicable" 不更新。
     */
    private boolean needUpdate(String value, boolean isRevised) {
        return value != null && !value.isBlank() && !value.equalsIgnoreCase("not applicable");
    }

    /** 解析 "发行单位数量" 整数，返回 BigDecimal (Parse an integer unit size)。 */
    private BigDecimal parseUnitSize(String value) {
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
