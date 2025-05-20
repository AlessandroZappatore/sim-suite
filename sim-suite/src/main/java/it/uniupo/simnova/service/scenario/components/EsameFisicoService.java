package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Service
public class EsameFisicoService {

    private static final Logger logger = LoggerFactory.getLogger(EsameFisicoService.class);

    public EsameFisico getEsameFisicoById(Integer id) {
        final String sql = "SELECT * FROM EsameFisico WHERE id_esame_fisico = ?";
        EsameFisico esameFisico = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                esameFisico = new EsameFisico(
                        rs.getInt("id_esame_fisico"),
                        rs.getString("generale"),
                        rs.getString("pupille"),
                        rs.getString("collo"),
                        rs.getString("torace"),
                        rs.getString("cuore"),
                        rs.getString("addome"),
                        rs.getString("retto"),
                        rs.getString("cute"),
                        rs.getString("estremità"),
                        rs.getString("neurologico"),
                        rs.getString("FAST")
                );

                logger.info("Esame fisico con ID {} recuperato con successo", id);
            } else {
                logger.warn("Nessun esame fisico trovato con ID {}", id);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dell'esame fisico con ID {}", id, e);
        }
        return esameFisico;
    }

    public boolean addEsameFisico(int scenarioId, Map<String, String> examData) {
        // Verifica se esiste già
        boolean exists = getEsameFisicoById(scenarioId) != null;

        final String sql = exists ?
                "UPDATE EsameFisico SET generale=?, pupille=?, collo=?, torace=?, cuore=?, " +
                        "addome=?, retto=?, cute=?, estremità=?, neurologico=?, FAST=? " +
                        "WHERE id_esame_fisico=?" :
                "INSERT INTO EsameFisico (id_esame_fisico, generale, pupille, collo, torace, " +
                        "cuore, addome, retto, cute, estremità, neurologico, FAST) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            if (!exists) {
                stmt.setInt(paramIndex++, scenarioId);
            }

            stmt.setString(paramIndex++, examData.getOrDefault("Generale", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Pupille", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Collo", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Torace", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Cuore", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Addome", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Retto", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Cute", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Estremità", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("Neurologico", ""));
            stmt.setString(paramIndex++, examData.getOrDefault("FAST", ""));

            if (exists) {
                stmt.setInt(paramIndex, scenarioId);
            }

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Esame fisico {} con ID {} salvato con successo", exists ? "aggiornato" : "inserito", scenarioId);
            } else {
                logger.warn("Nessun esame fisico {} con ID {}", exists ? "aggiornato" : "inserito", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio dell'esame fisico con ID {}", scenarioId, e);
            return false;
        }
    }

    public void updateSingleEsameFisico(int scenarioId, String name, String value) {
        if (name == null || name.isEmpty()) {
            logger.warn("Nome della colonna non valido");
            return;
        }

        // Verifica che l'esame fisico esista
        if (getEsameFisicoById(scenarioId) == null) {
            logger.warn("Nessun esame fisico trovato con ID {}", scenarioId);
            return;
        }

        // Creazione della query dinamica con il nome colonna come parametro
        final String sql = "UPDATE EsameFisico SET " + name + "=? WHERE id_esame_fisico=?";

        //noinspection SqlSourceToSinkFlow
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setInt(2, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Colonna {} dell'esame fisico con ID {} aggiornata a {}", name, scenarioId, value);
            } else {
                logger.warn("Impossibile aggiornare la colonna {} dell'esame fisico con ID {}", name, scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento della colonna {} dell'esame fisico con ID {}", name, scenarioId, e);
        }
    }

}
