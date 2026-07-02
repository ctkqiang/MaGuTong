package xin.ctkqiang.mybursa.repository;

import xin.ctkqiang.mybursa.model.IndexQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 行情快照持久层 (IndexQuote repository)。
 *
 * @author 钟智强
 */
@Repository
public interface IndexQuoteRepository extends JpaRepository<IndexQuote, String> {
}
