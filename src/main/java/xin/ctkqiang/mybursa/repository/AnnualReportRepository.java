package xin.ctkqiang.mybursa.repository;

import xin.ctkqiang.mybursa.model.AnnualReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 年度报告持久层 (AnnualReport repository)。
 *
 * @author 钟智强
 */
@Repository
public interface AnnualReportRepository extends JpaRepository<AnnualReport, Long> {

    /** 根据股票代码与参考编号查询 (Find by stock code and reference id)。 */
    Optional<AnnualReport> findByStockCodeAndRefId(String stockCode, String refId);
}
