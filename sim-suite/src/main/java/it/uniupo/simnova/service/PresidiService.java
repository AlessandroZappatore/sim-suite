package it.uniupo.simnova.service;

import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PresidiService {
    private static final Logger logger = LoggerFactory.getLogger(PresidiService.class);

    public static List<String> getAllPresidi() {
        final String sql = "SELECT nome FROM Presidi";
        List<String> presidi = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                presidi.add(rs.getString("nome"));
            }

            if(!presidi.isEmpty()) {
                logger.info("Presidi fetched successfully");
            } else {
                logger.warn("No presidi found");
            }


        } catch (Exception e) {
            logger.error("Error while fetching presidi", e);
        }
        return presidi;
    }

    public Integer getPresidiId(String presidio) {
        final String sql = "SELECT id_presidio FROM Presidi WHERE nome = ?";
        Integer id = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, presidio);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt("id_presidio");
            }
        } catch (Exception e) {
            logger.error("Error while fetching presidio ID", e);
        }
        return id;
    }


    public boolean savePresidi(Integer scenarioId, Set<String> value) {
        final String sql = "INSERT INTO PresidioScenario (id_presidio, id_scenario) VALUES (?, ?)";
        boolean success = true;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (String presidio : value) {
                Integer presidioId = getPresidiId(presidio);
                if (presidioId != null) {
                    stmt.setInt(1, presidioId);
                    stmt.setInt(2, scenarioId);
                    stmt.addBatch();
                }
            }

            int[] result = stmt.executeBatch();
            for (int r : result) {
                if (r == Statement.EXECUTE_FAILED) {
                    success = false;
                    break;
                }
            }

        } catch (Exception e) {
            logger.error("Error while saving presidi", e);
            success = false;
        }
        return success;
    }

    public static List<String> getPresidiByScenarioId(Integer scenarioId) {
        final String sql = "SELECT p.nome FROM Presidi p " +
                "JOIN PresidioScenario ps ON p.id_presidio = ps.id_presidio " +
                "WHERE ps.id_scenario = ?";
        List<String> presidi = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                presidi.add(rs.getString("nome"));
            }

        } catch (Exception e) {
            logger.error("Error while fetching presidi by scenario ID", e);
        }
        return presidi;
    }
}
