package it.uniupo.simnova.repository;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.dto.ScenarioSummaryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Integer> {
    @Query("SELECT new it.uniupo.simnova.dto.ScenarioSummaryDTO(s.id, s.titolo, s.autori, s.patologia, s.descrizione, s.tipologiaPaziente, s.tipologiaScenario) FROM Scenario s")
    List<ScenarioSummaryDTO> findAllSummaries();
}
