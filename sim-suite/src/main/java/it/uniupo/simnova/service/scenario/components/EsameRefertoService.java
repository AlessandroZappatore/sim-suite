package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EsameRefertoService {

    private static final Logger logger = LoggerFactory.getLogger(EsameRefertoService.class);

    private final FileStorageService fileStorageService;

    public EsameRefertoService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    public boolean saveEsamiReferti(int scenarioId, List<EsameReferto> esamiData) {
        if (!deleteEsamiReferti(scenarioId)) {
            logger.warn("Impossibile eliminare i referti esistenti per lo scenario con ID {}", scenarioId);
            return false;
        }

        final String sql = "INSERT INTO EsameReferto (id_esame, id_scenario, tipo, media, referto_testuale) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (EsameReferto esame : esamiData) {
                stmt.setInt(1, esame.getIdEsame());
                stmt.setInt(2, scenarioId);
                stmt.setString(3, esame.getTipo());
                stmt.setString(4, esame.getMedia());
                stmt.setString(5, esame.getRefertoTestuale());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    logger.warn("Nessun referto salvato per lo scenario con ID {}", scenarioId);
                    return false;
                }
            }
            logger.info("Referti salvati con successo per lo scenario con ID {}", scenarioId);
            return true;
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio dei referti per lo scenario con ID {}", scenarioId, e);
            return false;
        }
    }

    public List<EsameReferto> getEsamiRefertiByScenarioId(int scenarioId) {
        final String sql = "SELECT * FROM EsameReferto WHERE id_scenario = ? ORDER BY id_esame";
        List<EsameReferto> esami = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                EsameReferto esame = new EsameReferto(
                        rs.getInt("id_esame"),
                        rs.getInt("id_scenario"),
                        rs.getString("tipo"),
                        rs.getString("media"),
                        rs.getString("referto_testuale")
                );
                esami.add(esame);
            }
            logger.info("Recuperati {} esami referti per lo scenario con ID {}", esami.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero degli esami referti per lo scenario con ID {}", scenarioId, e);
        }
        return esami;
    }

    private boolean deleteEsamiReferti(int scenarioId) {
        final String sql = "DELETE FROM EsameReferto WHERE id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            boolean result = stmt.executeUpdate() >= 0;
            if (result) {
                logger.info("Referti esami eliminati con successo per lo scenario con ID {}", scenarioId);
            } else {
                logger.warn("Nessun referto esami trovato per lo scenario con ID {}", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione dei referti esami per lo scenario con ID {}", scenarioId, e);
            return false;
        }
    }

    public void deleteEsameReferto(int idEsameReferto, int scenarioId) {

        String mediaFilename = getMediaFilenameByEsameId(idEsameReferto, scenarioId);


        if (mediaFilename != null && !mediaFilename.isEmpty()) {
            fileStorageService.deleteFile(mediaFilename);
            logger.info("File media {} dell'esame con ID {} eliminato", mediaFilename, idEsameReferto);
        }


        final String sql = "DELETE FROM EsameReferto WHERE id_esame = ? AND id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEsameReferto);
            stmt.setInt(2, scenarioId);
            stmt.executeUpdate();
            logger.info("Referto esame con ID {} eliminato con successo per lo scenario con ID {}", idEsameReferto, scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione del referto esame con ID {} per lo scenario con ID {}", idEsameReferto, scenarioId, e);
        }
    }

    /**
     * Recupera il nome del file media associato a un esame.
     *
     * @param idEsame ID dell'esame
     * @param scenarioId ID dello scenario
     * @return Nome del file media o null se non trovato
     */
    private String getMediaFilenameByEsameId(int idEsame, int scenarioId) {
        final String sql = "SELECT media FROM EsameReferto WHERE id_esame = ? AND id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEsame);
            stmt.setInt(2, scenarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("media");
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero del media per l'esame con ID {} dello scenario {}",
                    idEsame, scenarioId, e);
        }

        return null;
    }
}
