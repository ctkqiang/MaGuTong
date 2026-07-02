package com.zhongzhiqiang.magutong.repository;

import com.zhongzhiqiang.magutong.model.LastEodMarker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 最后处理时间标记持久层 (LastEodMarker repository)。
 *
 * @author 钟智强
 */
@Repository
public interface LastEodMarkerRepository extends JpaRepository<LastEodMarker, String> {
}
