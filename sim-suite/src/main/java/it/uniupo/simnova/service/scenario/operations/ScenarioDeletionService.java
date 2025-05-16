package it.uniupo.simnova.service.scenario.operations;

import it.uniupo.simnova.service.scenario.helper.MediaHelper;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
public class ScenarioDeletionService {

    private final FileStorageService fileStorageService;
    private final AdvancedScenarioService advancedScenarioService;
    private final Logger logger = LoggerFactory.getLogger(ScenarioDeletionService.class);

    public ScenarioDeletionService(FileStorageService fileStorageService, AdvancedScenarioService advancedScenarioService) {
        this.fileStorageService = fileStorageService;
        this.advancedScenarioService = advancedScenarioService;
    }

    public boolean deleteScenario(int scenarioId) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Ottieni la lista dei file media da eliminare PRIMA di cancellare dal DB
            List<String> mediaFiles = MediaHelper.getMediaFilesForScenario(scenarioId);

            // 2. Esegui tutte le operazioni di cancellazione dal DB
            deleteAccessi(conn, scenarioId, "AccessoVenoso");
            deleteAccessi(conn, scenarioId, "AccessoArterioso");
            deleteRelatedMaterial(conn, scenarioId);
            deleteRelatedAccessi(conn);
            deleteRelatedPresidi(conn, scenarioId);
            deleteRelatedAzioniChiave(conn, scenarioId);
            advancedScenarioService.deleteTempi(conn, scenarioId);
            deletePatientSimulatedScenario(conn, scenarioId);
            deleteAdvancedScenario(conn, scenarioId);
            deleteEsamiReferti(conn, scenarioId);
            deleteEsameFisico(conn, scenarioId);
            deletePazienteT0(conn, scenarioId);
            deleteScenarioPrincipale(conn, scenarioId);

            conn.commit(); // Conferma la transazione
            logger.info("Scenario con ID {} eliminato con successo", scenarioId);

            // 3. Dopo il commit, elimina i file media
            fileStorageService.deleteFiles(mediaFiles);

            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback per lo scenario con ID {}", scenarioId, ex);
                }
            }
            logger.error("Errore durante l'eliminazione dello scenario con ID {}", scenarioId, e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Errore durante il ripristino dell'autocommit per lo scenario con ID {}", scenarioId, e);
                }
            }
        }
    }

    private void deleteRelatedAzioniChiave(Connection conn, int scenarioId) throws SQLException {
        // Elimina le associazioni per questo scenario
        final String sqlDeleteAzioneScenario = "DELETE FROM AzioneScenario WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteAzioneScenario)) {
            stmt.setInt(1, scenarioId);
            int count = stmt.executeUpdate();
            logger.debug("Eliminate {} associazioni azione-scenario per lo scenario {}", count, scenarioId);
        }

        // Elimina le azioni chiave non più associate ad alcuno scenario
        final String sqlDeleteOrphanAzioni =
            "DELETE FROM AzioniChiave WHERE id_azione NOT IN (" +
            "  SELECT DISTINCT id_azione FROM AzioneScenario" +
            ")";

        try (PreparedStatement stmt = conn.prepareStatement(sqlDeleteOrphanAzioni)) {
            int count = stmt.executeUpdate();
            logger.debug("Eliminate {} azioni chiave orfane dopo eliminazione scenario {}", count, scenarioId);
        }
    }

    private void deleteRelatedPresidi(Connection conn, int scenarioId) {
        final String sql = "DELETE FROM PresidioScenario WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione dei presidi per lo scenario con ID {}", scenarioId, e);
        }
    }

    private void deleteAccessi(Connection conn, int scenarioId, String tableName) throws SQLException {
        final String sql = "DELETE FROM " + tableName + " WHERE paziente_t0_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    private void deleteRelatedMaterial(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM MaterialeScenario WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina gli accessi che non sono più referenziati.
     *
     * @param conn la connessione al database
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private void deleteRelatedAccessi(Connection conn) throws SQLException {
        // Elimina gli accessi che non sono più referenziati
        final String sql = "DELETE FROM Accesso WHERE id_accesso IN (" +
                "SELECT a.id_accesso FROM Accesso a " +
                "LEFT JOIN AccessoVenoso av ON a.id_accesso = av.accesso_id " +
                "LEFT JOIN AccessoArterioso aa ON a.id_accesso = aa.accesso_id " +
                "WHERE (av.accesso_id IS NULL AND aa.accesso_id IS NULL))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina i tempi associati a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private void deletePatientSimulatedScenario(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM PatientSimulatedScenario WHERE id_patient_simulated_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina i tempi associati a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private void deleteAdvancedScenario(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM AdvancedScenario WHERE id_advanced_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina i tempi associati a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private void deleteEsamiReferti(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM EsameReferto WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina l'esame fisico associato a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private void deleteEsameFisico(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM EsameFisico WHERE id_esame_fisico = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina il paziente T0 associato a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private void deletePazienteT0(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM PazienteT0 WHERE id_paziente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina lo scenario principale associato a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private void deleteScenarioPrincipale(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM Scenario WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }
}
