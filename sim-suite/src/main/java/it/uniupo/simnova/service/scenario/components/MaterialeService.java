package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.common.Materiale;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione dei materiali necessari.
 */
@Service
public class MaterialeService {
    private static final Logger logger = LoggerFactory.getLogger(MaterialeService.class);

    /**
     * Ottiene tutti i materiali disponibili.
     *
     * @return lista di tutti i materiali
     */
    public List<Materiale> getAllMaterials() {
        final String sql = "SELECT id_materiale, nome, descrizione FROM Materiale";
        List<Materiale> materiali = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Materiale materiale = new Materiale(
                        rs.getInt("id_materiale"),
                        rs.getString("nome"),
                        rs.getString("descrizione")
                );
                materiali.add(materiale);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero di tutti i materiali", e);
        }
        return materiali;
    }

    /**
     * Ottiene i materiali associati a uno scenario specifico.
     *
     * @param scenarioId ID dello scenario
     * @return lista dei materiali associati allo scenario
     */
    public List<Materiale> getMaterialiByScenarioId(int scenarioId) {
        final String sql = "SELECT m.id_materiale, m.nome, m.descrizione " +
                "FROM Materiale m " +
                "JOIN MaterialeScenario sm ON m.id_materiale = sm.id_materiale " +
                "WHERE sm.id_scenario = ?";

        List<Materiale> materiali = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Materiale materiale = new Materiale(
                            rs.getInt("id_materiale"),
                            rs.getString("nome"),
                            rs.getString("descrizione")
                    );
                    materiali.add(materiale);
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei materiali per lo scenario con ID {}", scenarioId, e);
        }
        return materiali;
    }

    /**
     * Salva un nuovo materiale.
     *
     * @param materiale il materiale da salvare
     * @return il materiale salvato con ID generato
     */
    public Materiale saveMateriale(Materiale materiale) {
        final String sql = "INSERT INTO Materiale (nome, descrizione) VALUES (?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, materiale.getNome());
            stmt.setString(2, materiale.getDescrizione());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        return new Materiale(id, materiale.getNome(), materiale.getDescrizione());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio del materiale", e);
        }
        return null;
    }

    /**
     * Associa materiali a uno scenario.
     *
     * @param scenarioId   ID dello scenario
     * @param idsMateriali lista degli ID dei materiali da associare
     * @return true se l'operazione è avvenuta con successo
     */
    public boolean associaMaterialiToScenario(int scenarioId, List<Integer> idsMateriali) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Prima rimuovi tutte le associazioni esistenti
            final String deleteSQL = "DELETE FROM MaterialeScenario WHERE id_scenario = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                deleteStmt.setInt(1, scenarioId);
                deleteStmt.executeUpdate();
            }

            // Poi inserisci le nuove associazioni
            if (!idsMateriali.isEmpty()) {
                final String insertSQL = "INSERT INTO MaterialeScenario (id_scenario, id_materiale) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (Integer idMateriale : idsMateriali) {
                        insertStmt.setInt(1, scenarioId);
                        insertStmt.setInt(2, idMateriale);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Errore durante l'associazione dei materiali allo scenario {}", scenarioId, e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.error("Errore durante il rollback", ex);
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Errore durante la chiusura della connessione", e);
            }
        }
    }

    /**
     * Elimina un materiale dal database.
     *
     * @param idMateriale ID del materiale da eliminare
     * @return true se l'operazione è avvenuta con successo
     */
    public boolean deleteMateriale(Integer idMateriale) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Prima rimuovi tutte le associazioni con gli scenari
            final String deleteAssociazioniSQL = "DELETE FROM MaterialeScenario WHERE id_materiale = ?";
            try (PreparedStatement deleteAssociazioniStmt = conn.prepareStatement(deleteAssociazioniSQL)) {
                deleteAssociazioniStmt.setInt(1, idMateriale);
                deleteAssociazioniStmt.executeUpdate();
            }

            // Poi elimina il materiale
            final String deleteMaterialeSQL = "DELETE FROM Materiale WHERE id_materiale = ?";
            try (PreparedStatement deleteMaterialeStmt = conn.prepareStatement(deleteMaterialeSQL)) {
                deleteMaterialeStmt.setInt(1, idMateriale);
                int rowsDeleted = deleteMaterialeStmt.executeUpdate();

                conn.commit();
                return rowsDeleted > 0;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione del materiale con ID {}", idMateriale, e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.error("Errore durante il rollback", ex);
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Errore durante la chiusura della connessione", e);
            }
        }
    }

    public String toStringAllMaterialsByScenarioId(int scenarioId) {
        StringBuilder sb = new StringBuilder();
        List<Materiale> materiali = getMaterialiByScenarioId(scenarioId);
        for (Materiale materiale : materiali) {
            sb.append(materiale.getNome())
                    .append(": ")
                    .append(materiale.getDescrizione())
                    .append("\n");
        }
        return sb.toString();
    }
}