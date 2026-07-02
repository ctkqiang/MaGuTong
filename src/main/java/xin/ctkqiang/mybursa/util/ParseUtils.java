package xin.ctkqiang.mybursa.util;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 解析工具类 (Parsing utility class)。
 *
 * <p>对应原 Python 版本 {@code dmdata.py} 中的一系列辅助函数，统一收敛为静态方法，
 * 全部为纯函数、无副作用，方便单元测试。
 *
 * @author 钟智强
 */
public final class ParseUtils {

    /** 连续空白匹配 (Matches consecutive whitespace)。 */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** "dd MMM yyyy" 日期格式，如 "30 Apr 2025" (Date format used by Bursa)。 */
    private static final DateTimeFormatter DMY_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private ParseUtils() {
        // 工具类禁止实例化 (Utility class: no instantiation)
    }

    /**
     * 解析 "dd MMM yyyy" 日期 (Parse a "dd MMM yyyy" date)。
     * 对应 Python {@code from_DMY}。
     *
     * @param dmy 日期字符串 (date string)，为空返回 null
     * @return 解析后的 {@link LocalDate}，无法解析返回 null
     */
    public static LocalDate fromDayMonthYear(String dmy) {
        if (dmy == null || dmy.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dmy.trim(), DMY_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 折叠所有空白字符为单个空格并去除首尾空白 (Collapse whitespace)。
     * 对应 Python {@code remove_white_space}。
     */
    public static String removeWhiteSpace(String str) {
        if (str == null) {
            return "";
        }
        return WHITESPACE.matcher(str).replaceAll(" ").trim();
    }

    /**
     * 从完整 URL 中提取 "协议://域名" (Extract protocol://domain)。
     * 对应 Python {@code get_domain}。
     */
    public static String getDomain(String url) {
        int schemeIdx = url.indexOf("://");
        String protocol = url.substring(0, schemeIdx);
        String rest = url.substring(schemeIdx + 3);
        String domain = rest.split("/")[0];
        return protocol + "://" + domain;
    }

    /**
     * 将相对链接补全为绝对链接 (Resolve a possibly-relative URL against a domain)。
     * 对应 Python {@code get_full_url}。
     */
    public static String getFullUrl(String domain, String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("https://") || url.startsWith("http://")) {
            return url;
        } else if (url.startsWith("/")) {
            return domain + url;
        } else {
            return domain + "/" + url;
        }
    }

    /**
     * 将字符串数字转为 double，失败返回 0 (Parse a number, default 0)。
     * 对应 Python {@code get_number}。
     */
    public static double getNumber(String numStr) {
        if (numStr == null) {
            return 0d;
        }
        try {
            return Double.parseDouble(numStr.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

    /**
     * 将字符串数字转为 {@link BigDecimal}，失败返回 null (Parse to BigDecimal, null on failure)。
     * 用于价格类字段，处理 "-" 占位符。
     */
    public static BigDecimal toDecimal(String value) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析形如 "RM 1.23" 的货币值，取空格分隔的最后一段 (Parse "RM 1.23" -> 1.23)。
     * 对应 Python {@code update_currency_value}。
     */
    public static BigDecimal updateCurrencyValue(String data) {
        if (data == null) {
            return null;
        }
        try {
            String[] parts = data.trim().split(" ");
            return new BigDecimal(parts[parts.length - 1].replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 规整形如 "1 : 1" 的比率字符串 (Normalise a ratio string like "1 : 1")。
     * 对应 Python {@code get_ratio}。
     */
    public static String getRatio(String data) {
        if (data == null || !data.contains(":")) {
            return data;
        }
        try {
            String[] parts = data.split(":");
            double left = Double.parseDouble(parts[0].trim());
            double right = Double.parseDouble(parts[1].trim());
            return trimTrailingZeros(left) + " : " + trimTrailingZeros(right);
        } catch (Exception e) {
            return data;
        }
    }

    /**
     * 去除小数末尾多余的零 (Trim trailing zeros for display)。
     */
    private static String trimTrailingZeros(double value) {
        BigDecimal bd = BigDecimal.valueOf(value).stripTrailingZeros();
        return bd.toPlainString();
    }

    /**
     * 从附件表格 {@code table.att_table} 中提取所有附件，格式为 "名称|绝对URL"，多个换行分隔。
     * 对应 Python {@code extract_attachment}。
     *
     * @param root Jsoup 文档或元素根节点
     * @param url  当前页面 URL，用于补全相对链接
     * @return 附件文本，无附件返回 null
     */
    public static String extractAttachment(Element root, String url) {
        Elements links = root.select("table.att_table a");
        if (links.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String domain = (url != null) ? getDomain(url) : "";
        for (Element link : links) {
            String entry = link.text().trim() + "|" + getFullUrl(domain, link.attr("href"));
            if (sb.length() > 0) {
                sb.append("\r\n");
            }
            sb.append(entry);
        }
        return sb.toString();
    }
}
