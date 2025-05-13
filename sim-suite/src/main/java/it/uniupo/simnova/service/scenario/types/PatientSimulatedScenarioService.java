package it.uniupo.simnova.service.scenario.types;

import it.uniupo.simnova.domain.scenario.PatientSimulatedScenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Service
public class PatientSimulatedScenarioService {
    private static final Logger logger = LoggerFactory.getLogger(PatientSimulatedScenarioService.class);
    private final AdvancedScenarioService advancedScenarioService;
    private final ScenarioService scenarioService;

    public PatientSimulatedScenarioService(AdvancedScenarioService advancedScenarioService, ScenarioService scenarioService) {
        this.advancedScenarioService = advancedScenarioService;
        this.scenarioService = scenarioService;
    }

    public int startPatientSimulatedScenario(String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologia) {
        // Prima crea lo scenario avanzato
        int scenarioId = advancedScenarioService.startAdvancedScenario(titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);

        if (scenarioId > 0) {
            final String sql = "INSERT INTO PatientSimulatedScenario (id_patient_simulated_scenario, id_advanced_scenario, sceneggiatura) VALUES (?,?,?)";

            try (Connection conn = DBConnect.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, scenarioId);
                stmt.setInt(2, scenarioId);
                stmt.setString(3, ""); // Sceneggiatura vuota inizialmente

                stmt.executeUpdate();
                logger.info("Scenario simulato per pazienti creato con ID: {}", scenarioId);

            } catch (SQLException e) {
                logger.error("Errore durante l'inserimento dello scenario simulato per pazienti con ID: {}", scenarioId, e);
                return -1;
            }
        }
        return scenarioId;
    }

    public PatientSimulatedScenario getPatientSimulatedScenarioById(Integer id) {
        final String sql = "SELECT * FROM PatientSimulatedScenario pss " +
                "JOIN Scenario s ON pss.id_patient_simulated_scenario = s.id_scenario " +
                "WHERE pss.id_patient_simulated_scenario = ?";
        PatientSimulatedScenario scenario = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                scenario = new PatientSimulatedScenario(
                        rs.getInt("id_scenario"),
                        rs.getString("titolo"),
                        rs.getString("nome_paziente"),
                        rs.getString("patologia"),
                        rs.getString("descrizione"),
                        rs.getString("briefing"),
                        rs.getString("patto_aula"),
                        rs.getString("obiettivo"),
                        rs.getString("moulage"),
                        rs.getString("liquidi"),
                        rs.getFloat("timer_generale"),
                        rs.getString("autori"),
                        rs.getString("tipologia_paziente"),
                        rs.getString("target"),
                        rs.getString("info_genitore"),
                        rs.getInt("id_advanced_scenario"),
                        new ArrayList<>(), // Tempi vuoti inizialmente
                        rs.getInt("id_patient_simulated_scenario"),
                        rs.getInt("id_advanced_scenario"),
                        rs.getString("sceneggiatura")
                );
                logger.info("Scenario simulato per pazienti con ID {} recuperato con successo", id);
            } else {
                logger.warn("Nessuno scenario simulato per pazienti trovato con ID {}", id);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dello scenario simulato per pazienti con ID {}", id, e);
        }
        return scenario;
    }

    public String getSceneggiatura(int scenarioId) {
        final String sql = "SELECT sceneggiatura FROM PatientSimulatedScenario WHERE id_patient_simulated_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String sceneggiatura = rs.getString("sceneggiatura");
                logger.info("Sceneggiatura recuperata per lo scenario con ID {}", scenarioId);
                return sceneggiatura;
            } else {
                logger.warn("Nessuna sceneggiatura trovata per lo scenario con ID {}", scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero della sceneggiatura per lo scenario con ID {}", scenarioId, e);
        }
        return "";
    }

    public boolean updateScenarioSceneggiatura(Integer scenarioId, String sceneggiatura) {
        if (!scenarioService.isPresentInTable(scenarioId, "PatientSimulatedScenario")) {
            logger.warn("Lo scenario con ID {} non è un PatientSimulatedScenario", scenarioId);
            return false;
        }

        final String sql = "UPDATE PatientSimulatedScenario SET sceneggiatura = ? WHERE id_patient_simulated_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sceneggiatura);
            stmt.setInt(2, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Sceneggiatura aggiornata con successo per lo scenario con ID {}", scenarioId);
            } else {
                logger.warn("Nessuna sceneggiatura aggiornata per lo scenario con ID {}", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento della sceneggiatura per lo scenario con ID {}", scenarioId, e);
            return false;
        }
    }

}
