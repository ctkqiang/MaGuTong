package com.zhongzhiqiang.magutong.repository;

import com.zhongzhiqiang.magutong.model.AnnouncementQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 待处理公告队列持久层 (AnnouncementQueue repository)。
 *
 * @author 钟智强
 */
@Repository
public interface AnnouncementQueueRepository extends JpaRepository<AnnouncementQueue, Long> {

    /** 按类型查询待处理 (state = 0) 的公告，按公告日期升序 (Find pending announcements by type)。 */
    List<AnnouncementQueue> findByEnTypeAndProcessStateOrderByAnnounceDateAsc(String enType, Integer processState);

    /** 根据来源地址查询 (Find by source URL)。 */
    Optional<AnnouncementQueue> findByUrl(String url);
}
