package com.zhongzhiqiang.magutong.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 各公告类型的最后处理时间标记 (Last end-of-day processing marker per announcement type)。
 *
 * <p>原 Python 版本对应 {@code gpmining.bursalasteod} 表，重命名为 {@code last_eod_marker}。
 *
 * @author 钟智强
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "last_eod_marker")
public class LastEodMarker {

    /** 公告类型，主键 (Entitlement type, primary key)。 */
    @Id
    @Column(name = "en_type", length = 32, nullable = false)
    private String enType;

    /** 最后更新时间 (Last update timestamp)。 */
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;
}
