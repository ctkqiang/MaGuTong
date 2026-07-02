package com.zhongzhiqiang.magutong.service;

/**
 * 公告类型枚举 (Announcement type enum)。
 *
 * <p>用于把队列中的待处理公告路由到对应的解析器，取代原 Python 版本中散落的
 * {@code en_type} 字符串判断，提供类型安全的分发。
 *
 * @author 钟智强
 */
public enum AnnouncementType {

    /** 年度报告 (Annual report)。 */
    ANNUAL_REPORT,

    /** 季度财务业绩 (Quarterly financial result)。 */
    FINANCIAL_RESULT,

    /** 上市资料 (Listing profile)。 */
    LISTING_PROFILE,

    /** 变更财政年结日 (Change of financial year end)。 */
    CHANGE_YEAR_END;

    /**
     * 宽松解析：忽略大小写并容错未知值 (Lenient parse from a raw en_type string)。
     *
     * @param raw 原始类型字符串
     * @return 对应枚举，无法识别时返回 null
     */
    public static AnnouncementType fromRaw(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return AnnouncementType.valueOf(raw.trim().toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
