package com.zhongzhiqiang.magutong.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * 公告头部信息 (Announcement header info)。
 *
 * <p>对应原 Python 版本的 {@code info_data} 类：从公告页面顶部的键值对表格中提取
 * 日期、分类、参考编号、股票代码、来源地址等元信息。
 *
 * <p>入参 {@code rawRows} 是形如 {@code [["date announced", "..."], ["category", "..."]]}
 * 的二维列表，构造时逐行解析填充。
 *
 * @author 钟智强
 */
@Getter
@ToString
public class AnnouncementInfo {

    private LocalDate announceDate;
    private String category;
    private String refId;
    private String refIdAmend;
    private String stockCode;
    private String url;

    /**
     * 从原始键值对行构造 (Build from raw key/value rows)。
     *
     * @param rawRows 二维字符串列表，每行首元素为字段名 (each row: [key, value])
     */
    public AnnouncementInfo(List<List<String>> rawRows) {
        if (rawRows == null) {
            return;
        }
        for (List<String> row : rawRows) {
            if (row == null || row.size() < 2) {
                continue;
            }
            String key = row.get(0) == null ? "" : row.get(0).toLowerCase().trim();
            String value = row.get(1);
            switch (key) {
                case "date announced" -> this.announceDate = parseDate(value);
                case "category" -> this.category = value;
                case "reference no", "reference number" -> this.refId = value;
                case "amended_ref_id" -> this.refIdAmend = value;
                case "stock_code" -> this.stockCode = value;
                case "url" -> this.url = value;
                default -> {
                    // 忽略未识别字段 (ignore unrecognised keys)
                }
            }
        }
    }

    /**
     * 尝试解析 "dd MMM yyyy" 格式日期，失败返回 null (Best-effort date parse)。
     */
    private LocalDate parseDate(String value) {
        try {
            return com.zhongzhiqiang.magutong.util.ParseUtils.fromDayMonthYear(value);
        } catch (Exception e) {
            return null;
        }
    }
}
