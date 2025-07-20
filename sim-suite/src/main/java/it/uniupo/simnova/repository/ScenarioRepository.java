package it.uniupo.simnova.repository;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.dto.ScenarioSummaryDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Integer> {
    List<Scenario> findByTipologiaPazienteIgnoreCase(String tipologiaPaziente);

    @Query("SELECT new it.uniupo.simnova.dto.ScenarioSummaryDTO(s.id, s.titolo, s.autori, s.patologia, s.descrizione, s.tipologiaPaziente) FROM Scenario s")
    List<ScenarioSummaryDTO> findAllSummaries();
}
