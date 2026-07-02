package xin.ctkqiang.mybursa.repository;

import xin.ctkqiang.mybursa.model.QuarterlyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 季度业绩持久层 (QuarterlyResult repository)。
 *
 * @author 钟智强
 */
@Repository
public interface QuarterlyResultRepository extends JpaRepository<QuarterlyResult, Long> {

    /** 业务唯一键查询：股票代码 + 财政季度截止日 (Find by stock code and quarter end)。 */
    Optional<QuarterlyResult> findByStockCodeAndFinQtrEnd(String stockCode, LocalDate finQtrEnd);
}
