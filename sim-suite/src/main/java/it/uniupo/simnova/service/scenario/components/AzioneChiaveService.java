package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AzioneChiaveService {

    private static final Logger logger = LoggerFactory.getLogger(AzioneChiaveService.class);

    public List<String> getNomiAzioniChiaveByScenarioId(Integer scenarioId) {
        List<String> nomiAzioni = new ArrayList<>();

        final String sql = "SELECT ac.nome " +
                "FROM AzioniChiave ac " +
                "JOIN AzioneScenario a ON ac.id_azione = a.id_azione " +
                "WHERE a.id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                nomiAzioni.add(rs.getString("nome"));
            }

            if (!nomiAzioni.isEmpty()) {
                logger.info("Recuperate {} azioni chiave per lo scenario con ID {}", nomiAzioni.size(), scenarioId);
            } else {
                logger.info("Nessuna azione chiave trovata per lo scenario con ID {}", scenarioId);
            }

        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle azioni chiave per lo scenario con ID {}", scenarioId, e);
            return new ArrayList<>();
        }
        return nomiAzioni;
    }

    public boolean updateAzioniChiaveForScenario(Integer scenarioId, List<String> nomiAzioniDaSalvare) {
        Connection conn = null;
        boolean success = false;

        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false);

            List<Integer> idAzioniFinali = new ArrayList<>();
            if (nomiAzioniDaSalvare != null) {
                for (String nomeAzione : nomiAzioniDaSalvare) {
                    if (nomeAzione == null || nomeAzione.trim().isEmpty()) {
                        continue;
                    }

                    Integer idAzione = getOrCreateAzioneChiaveId(conn, nomeAzione.trim());
                    idAzioniFinali.add(idAzione);
                }
            }


            final String deleteSql = "DELETE FROM AzioneScenario WHERE id_scenario = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, scenarioId);
                int deletedRows = deleteStmt.executeUpdate();
                logger.info("Rimosse {} associazioni azione-scenario esistenti per scenario ID: {}", deletedRows, scenarioId);
            }


            if (!idAzioniFinali.isEmpty()) {
                final String insertSql = "INSERT INTO AzioneScenario (id_scenario, id_azione) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (Integer idAzione : idAzioniFinali) {
                        insertStmt.setInt(1, scenarioId);
                        insertStmt.setInt(2, idAzione);
                        insertStmt.addBatch();
                    }
                    int[] batchResult = insertStmt.executeBatch();
                    logger.info("Inserite {} nuove associazioni azione-scenario per scenario ID: {}", batchResult.length, scenarioId);
                }
            } else {
                logger.info("Nessuna nuova azione chiave da associare per lo scenario ID: {}. Tutte le associazioni precedenti sono state rimosse.", scenarioId);
            }

            conn.commit();
            success = true;
            logger.info("Azioni chiave per lo scenario ID {} aggiornate con successo.", scenarioId);

        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento delle azioni chiave per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Rollback della transazione eseguito per lo scenario ID {}", scenarioId);
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback della transazione per lo scenario ID {}: {}", scenarioId, ex.getMessage(), ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
                }
            }
        }
        return success;
    }

    private Integer getOrCreateAzioneChiaveId(Connection conn, String nomeAzione) throws SQLException {

        final String selectSql = "SELECT id_azione FROM AzioniChiave WHERE nome = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, nomeAzione);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_azione");
            }
        }


        final String insertSql = "INSERT INTO AzioniChiave (nome) VALUES (?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, nomeAzione);
            int affectedRows = insertStmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        logger.info("Creata nuova AzioneChiave '{}' con ID: {}", nomeAzione, generatedKeys.getInt(1));
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creazione AzioneChiave fallita per '" + nomeAzione + "', nessun ID ottenuto.");
                    }
                }
            } else {
                throw new SQLException("Creazione AzioneChiave fallita per '" + nomeAzione + "', nessuna riga modificata.");
            }
        }
    }
}
