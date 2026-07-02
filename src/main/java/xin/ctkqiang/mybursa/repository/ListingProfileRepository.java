package xin.ctkqiang.mybursa.repository;

import xin.ctkqiang.mybursa.model.ListingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 上市资料持久层 (ListingProfile repository)。
 *
 * @author 钟智强
 */
@Repository
public interface ListingProfileRepository extends JpaRepository<ListingProfile, Long> {

    /** 根据股票代码与参考编号查询 (Find by stock code and reference id)。 */
    Optional<ListingProfile> findByStockCodeAndRefId(String stockCode, String refId);
}
