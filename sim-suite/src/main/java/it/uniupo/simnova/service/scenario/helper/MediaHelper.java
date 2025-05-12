package it.uniupo.simnova.service.scenario.helper;

import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaHelper {
    private static final Logger logger = LoggerFactory.getLogger(MediaHelper.class);

    public  static boolean isFileInUse(String filename){
        if (filename == null || filename.isBlank()) {
            logger.warn("Nome file non valido per controllo utilizzo.");
            return false;
        }

        final String sql = "SELECT COUNT(*) FROM EsameReferto WHERE media = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, filename);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Errore durante il controllo del file in uso: {}", filename, e);
        }
        return false;
    }

    public static List<String> getMediaFilesForScenario(int scenarioId) {
        final String sql = "SELECT media FROM EsameReferto WHERE id_scenario = ? AND media IS NOT NULL";
        List<String> files = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String media = rs.getString("media");
                if (media != null && !media.isEmpty()) {
                    files.add(media);
                }
            }
            logger.info("Recuperati {} file media per lo scenario con ID {}", files.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei file media per lo scenario con ID {}", scenarioId, e);
        }

        return files;
    }
}
