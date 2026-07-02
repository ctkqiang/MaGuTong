package com.zhongzhiqiang.magutong.service;

import com.zhongzhiqiang.magutong.dto.AnnouncementInfo;
import com.zhongzhiqiang.magutong.model.AnnualReport;
import com.zhongzhiqiang.magutong.model.ListingProfile;
import com.zhongzhiqiang.magutong.model.QuarterlyResult;
import com.zhongzhiqiang.magutong.repository.AnnualReportRepository;
import com.zhongzhiqiang.magutong.repository.ListingProfileRepository;
import com.zhongzhiqiang.magutong.repository.QuarterlyResultRepository;
import com.zhongzhiqiang.magutong.service.scraper.PageFetcher;
import com.zhongzhiqiang.magutong.service.scraper.parser.AnnualReportParser;
import com.zhongzhiqiang.magutong.service.scraper.parser.FinancialResultParser;
import com.zhongzhiqiang.magutong.service.scraper.parser.ListingProfileParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 公告处理编排服务 (Announcement processing orchestration service)。
 *
 * <p>把原 Python 版本中分散在 {@code dm_annual_report}、{@code dm_financial_result}、
 * {@code dm_listing_profile}、{@code dm_change_year_end} 各文件的 "解析 + 落库 + 标记队列"
 * 逻辑统一收敛到本服务，通过 {@link AnnouncementType} 分发到对应处理分支。
 *
 * @author 钟智强
 */
@Slf4j
@Service
public class AnnouncementService {

    private final PageFetcher pageFetcher;
    private final AnnualReportParser annualReportParser;
    private final FinancialResultParser financialResultParser;
    private final ListingProfileParser listingProfileParser;
    private final AnnualReportRepository annualReportRepository;
    private final QuarterlyResultRepository quarterlyResultRepository;
    private final ListingProfileRepository listingProfileRepository;
    private final AnnouncementQueueService queueService;

    public AnnouncementService(PageFetcher pageFetcher,
                               AnnualReportParser annualReportParser,
                               FinancialResultParser financialResultParser,
                               ListingProfileParser listingProfileParser,
                               AnnualReportRepository annualReportRepository,
                               QuarterlyResultRepository quarterlyResultRepository,
                               ListingProfileRepository listingProfileRepository,
                               AnnouncementQueueService queueService) {
        this.pageFetcher = pageFetcher;
        this.annualReportParser = annualReportParser;
        this.financialResultParser = financialResultParser;
        this.listingProfileParser = listingProfileParser;
        this.annualReportRepository = annualReportRepository;
        this.quarterlyResultRepository = quarterlyResultRepository;
        this.listingProfileRepository = listingProfileRepository;
        this.queueService = queueService;
    }

    /**
     * 处理单条队列公告 (Process a single queued announcement)。
     *
     * @param queueId   队列主键
     * @param type      公告类型
     * @param info      公告头部信息
     * @param stockCode 股票代码
     * @return 处理成功返回 true
     */
    @Transactional
    public boolean process(Long queueId, AnnouncementType type,
                           AnnouncementInfo info, String stockCode) {
        if (type == null) {
            log.warn("未知公告类型，跳过 (Unknown announcement type, skipping): queueId={}", queueId);
            queueService.markProcessed(queueId, true);
            return false;
        }

        try {
            switch (type) {
                case ANNUAL_REPORT -> handleAnnualReport(info, stockCode, type.name());
                case FINANCIAL_RESULT -> handleFinancialResult(info, stockCode);
                case LISTING_PROFILE -> handleListingProfile(info, stockCode, type.name());
                case CHANGE_YEAR_END -> handleChangeYearEnd(info);
            }
            queueService.markDone(queueId);
            return true;
        } catch (Exception e) {
            log.error("处理公告失败 (Failed to process announcement) queueId={}: {}",
                    queueId, e.getMessage(), e);
            queueService.markProcessed(queueId, true);
            return false;
        }
    }

    /**
     * 处理年度报告 (Handle an annual report)。存在则更新，否则插入。
     */
    private void handleAnnualReport(AnnouncementInfo info, String stockCode, String enType) {
        String html = pageFetcher.fetchAnnouncementPage(info.getUrl());
        AnnualReport parsed = annualReportParser.parse(html, info, stockCode, enType);
        parsed.setLastUpdate(LocalDateTime.now());

        Optional<AnnualReport> existing =
                annualReportRepository.findByStockCodeAndRefId(stockCode, info.getRefId());
        existing.ifPresent(e -> parsed.setId(e.getId()));
        annualReportRepository.save(parsed);
    }

    /**
     * 处理季度业绩 (Handle a quarterly result)。业务键为 stock_code + fin_qtr_end。
     */
    private void handleFinancialResult(AnnouncementInfo info, String stockCode) {
        String html = pageFetcher.fetchAnnouncementPage(info.getUrl());
        QuarterlyResult parsed = financialResultParser.parse(html, info, stockCode);
        parsed.setLastUpdate(LocalDateTime.now());

        Optional<QuarterlyResult> existing = quarterlyResultRepository
                .findByStockCodeAndFinQtrEnd(stockCode, parsed.getFinQtrEnd());
        existing.ifPresent(e -> parsed.setId(e.getId()));
        quarterlyResultRepository.save(parsed);
    }

    /**
     * 处理上市资料 (Handle a listing profile)。
     */
    private void handleListingProfile(AnnouncementInfo info, String stockCode, String enType) {
        String html = pageFetcher.fetchAnnouncementPage(info.getUrl());
        ListingProfile parsed = listingProfileParser.parse(html, info, stockCode, enType);
        parsed.setLastUpdate(LocalDateTime.now());

        Optional<ListingProfile> existing =
                listingProfileRepository.findByStockCodeAndRefId(stockCode, info.getRefId());
        existing.ifPresent(e -> parsed.setId(e.getId()));
        listingProfileRepository.save(parsed);
    }

    /**
     * 处理变更财政年结日 (Handle change of financial year end)。
     * 对应原 Python {@code dm_change_year_end}：仅需标记队列，无额外落库。
     */
    private void handleChangeYearEnd(AnnouncementInfo info) {
        log.info("变更财政年结日公告，仅标记完成 (Change year-end, marking done): {}", info.getUrl());
        // 无需持久化业务表 (No business table to persist)
    }
}
