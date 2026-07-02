package xin.ctkqiang.mybursa.service;

import xin.ctkqiang.mybursa.model.AnnouncementQueue;
import xin.ctkqiang.mybursa.repository.AnnouncementQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 待处理公告队列服务 (Announcement queue service)。
 *
 * <p>对应原 Python 版本 {@code dmdata.py} 中的 {@code upsert_bursapending}、
 * {@code get_bursapending}、{@code set_bursapending} 三个函数，负责队列的入队、
 * 查询与状态更新。
 *
 * @author 钟智强
 */
@Slf4j
@Service
public class AnnouncementQueueService {

    /** 状态：待处理 (Pending)。 */
    public static final int STATE_PENDING = 0;
    /** 状态：处理成功 (Done)。 */
    public static final int STATE_DONE = 1;
    /** 状态：处理失败 (Error)。 */
    public static final int STATE_ERROR = -1;

    private final AnnouncementQueueRepository repository;

    public AnnouncementQueueService(AnnouncementQueueRepository repository) {
        this.repository = repository;
    }

    /**
     * 入队或更新一条待处理公告 (Insert or update a pending announcement)。
     * 对应 Python {@code upsert_bursapending}。
     *
     * @return 若为新记录或原记录尚未处理则返回 true (需要后续处理)
     */
    @Transactional
    public boolean upsert(String enType, LocalDate announceDate, String stockCode,
                          String subject, String url) {
        Optional<AnnouncementQueue> existing = repository.findByUrl(url);
        if (existing.isPresent()) {
            AnnouncementQueue queue = existing.get();
            boolean alreadyProcessed = queue.getProcessState() != null
                    && queue.getProcessState() == STATE_DONE;
            queue.setEnType(enType);
            queue.setAnnounceDate(announceDate);
            queue.setStockCode(stockCode);
            queue.setSubject(subject);
            repository.save(queue);
            return !alreadyProcessed;
        }

        AnnouncementQueue queue = AnnouncementQueue.builder()
                .enType(enType)
                .announceDate(announceDate)
                .stockCode(stockCode)
                .subject(subject)
                .url(url)
                .processState(STATE_PENDING)
                .hasError(false)
                .build();
        repository.save(queue);
        return true;
    }

    /**
     * 按类型获取所有待处理公告 (Fetch all pending announcements of a type)。
     * 对应 Python {@code get_bursapending}。
     */
    @Transactional(readOnly = true)
    public List<AnnouncementQueue> findPending(String enType) {
        return repository.findByEnTypeAndProcessStateOrderByAnnounceDateAsc(enType, STATE_PENDING);
    }

    /**
     * 标记公告处理完成 (Mark an announcement as processed)。
     * 对应 Python {@code set_bursapending}。
     *
     * @param queueId 队列主键
     * @param error   是否处理失败
     */
    @Transactional
    public void markProcessed(Long queueId, boolean error) {
        repository.findById(queueId).ifPresent(queue -> {
            queue.setProcessState(error ? STATE_ERROR : STATE_DONE);
            queue.setHasError(error);
            repository.save(queue);
        });
    }

    /** 便捷方法：标记成功 (Convenience: mark done)。 */
    @Transactional
    public void markDone(Long queueId) {
        markProcessed(queueId, false);
    }
}
