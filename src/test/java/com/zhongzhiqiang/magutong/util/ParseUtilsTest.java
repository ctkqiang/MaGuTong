package com.zhongzhiqiang.magutong.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ParseUtils} 的单元测试。
 *
 * <p>验证从原 Python {@code dmdata.py} 迁移而来的各解析函数行为正确。
 * 所有被测方法均为纯函数，无需 Spring 上下文。
 *
 * @author 钟智强
 */
class ParseUtilsTest {

    @Test
    @DisplayName("日期解析：'d MMM yyyy' 格式")
    void fromDayMonthYear_parsesValidDate() {
        assertEquals(LocalDate.of(2025, 4, 30), ParseUtils.fromDayMonthYear("30 Apr 2025"));
    }

    @Test
    @DisplayName("日期解析：空白与非法输入返回 null")
    void fromDayMonthYear_returnsNullOnBlankOrInvalid() {
        assertNull(ParseUtils.fromDayMonthYear(""));
        assertNull(ParseUtils.fromDayMonthYear(null));
        assertNull(ParseUtils.fromDayMonthYear("not a date"));
    }

    @Test
    @DisplayName("空白折叠：多重空白合并为单个空格")
    void removeWhiteSpace_collapsesWhitespace() {
        assertEquals("Financial Year End", ParseUtils.removeWhiteSpace("  Financial\t\n  Year   End "));
        assertEquals("", ParseUtils.removeWhiteSpace(null));
    }

    @Test
    @DisplayName("域名提取")
    void getDomain_extractsProtocolAndHost() {
        assertEquals("https://www.bursamalaysia.com",
                ParseUtils.getDomain("https://www.bursamalaysia.com/market/listed/abc?x=1"));
    }

    @Test
    @DisplayName("相对链接补全")
    void getFullUrl_resolvesRelativeAndAbsolute() {
        String domain = "https://www.bursamalaysia.com";
        assertEquals("https://cdn.example.com/a.pdf",
                ParseUtils.getFullUrl(domain, "https://cdn.example.com/a.pdf"));
        assertEquals(domain + "/docs/a.pdf", ParseUtils.getFullUrl(domain, "/docs/a.pdf"));
        assertEquals(domain + "/docs/a.pdf", ParseUtils.getFullUrl(domain, "docs/a.pdf"));
    }

    @Test
    @DisplayName("数字解析：去除千分位逗号，失败返回 0")
    void getNumber_parsesWithCommasAndDefaultsToZero() {
        assertEquals(1234.56d, ParseUtils.getNumber("1,234.56"));
        assertEquals(0d, ParseUtils.getNumber("-"));
        assertEquals(0d, ParseUtils.getNumber(null));
    }

    @Test
    @DisplayName("BigDecimal 解析：占位符 '-' 返回 null")
    void toDecimal_handlesPlaceholders() {
        assertEquals(new BigDecimal("100.5"), ParseUtils.toDecimal("100.5"));
        assertNull(ParseUtils.toDecimal("-"));
        assertNull(ParseUtils.toDecimal(""));
    }

    @Test
    @DisplayName("货币值解析：取空格分隔的最后一段")
    void updateCurrencyValue_takesLastToken() {
        assertEquals(new BigDecimal("1.23"), ParseUtils.updateCurrencyValue("RM 1.23"));
        assertNull(ParseUtils.updateCurrencyValue("not a number here !"));
    }

    @Test
    @DisplayName("比率规整：去除多余零")
    void getRatio_normalisesRatio() {
        assertEquals("1 : 1", ParseUtils.getRatio("1.00 : 1.0"));
        assertEquals("no-ratio", ParseUtils.getRatio("no-ratio"));
    }
}
