package it.uniupo.simnova.repository;

import it.uniupo.simnova.domain.paziente.EsameReferto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EsameRefertoRepository extends JpaRepository<EsameReferto, Integer> {
    List<EsameReferto> findByIdScenario(Integer idScenario);

    Integer deleteByIdScenario(Integer idScenario);

    @Query("SELECT e.media FROM EsameReferto e WHERE e.idEsame = ?1 AND e.idScenario = ?2")
    Optional<String> findMediaByIdEsameAndIdScenario(Integer idEsame, Integer idScenario);

    @Modifying
    @Query("DELETE FROM EsameReferto e WHERE e.idEsame = ?1 AND e.idScenario = ?2")
    Integer deleteEsameRefertoByIds(Integer idEsame, Integer idScenario);

    @Modifying
    @Query("UPDATE EsameReferto e SET e.media = ?3 WHERE e.idEsame = ?1 AND e.idScenario = ?2")
    boolean updateMediaByIdEsameAndIdScenario(Integer idEsame, Integer idScenario, String newMediaPath);

    @Modifying
    @Query("UPDATE EsameReferto e SET e.referto = ?3 WHERE e.idEsame = ?1 AND e.idScenario = ?2")
    boolean updateRefertoTestualeByIdEsameAndIdScenario(Integer idEsame, Integer idScenario, String newRefertoTestuale);
}
