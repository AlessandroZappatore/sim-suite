package it.uniupo.simnova.service;

import it.uniupo.simnova.api.model.*;
import it.uniupo.simnova.utils.DBConnect;
import it.uniupo.simnova.views.creation.EsamiRefertiView;
import it.uniupo.simnova.views.creation.PazienteT0View;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScenarioService {

    // 1. Metodi per Scenario (invariati)
    public Scenario getScenarioById(Integer id) {
        final String sql = "SELECT * FROM Scenario WHERE id_scenario = ?";
        Scenario scenario = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                scenario = new Scenario(
                        rs.getInt("id_scenario"),
                        rs.getString("titolo"),
                        rs.getString("nome_paziente"),
                        rs.getString("patologia"),
                        rs.getString("descrizione"),
                        rs.getString("briefing"),
                        rs.getString("patto_aula"),
                        rs.getString("azione_chiave"),
                        rs.getString("obiettivo"),
                        rs.getString("materiale"),
                        rs.getString("moulage"),
                        rs.getString("liquidi"),
                        rs.getFloat("timer_generale")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scenario;
    }

    public List<Scenario> getAllScenarios() {
        final String sql = "SELECT id_scenario, titolo, nome_paziente, patologia, descrizione FROM Scenario";
        List<Scenario> scenarios = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Scenario scenario = new Scenario();
                scenario.setId(rs.getInt("id_scenario"));
                scenario.setTitolo(rs.getString("titolo"));
                scenario.setNomePaziente(rs.getString("nome_paziente"));
                scenario.setPatologia(rs.getString("patologia"));
                scenario.setDescrizione(rs.getString("descrizione"));
                scenarios.add(scenario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scenarios;
    }

    public int startScenario(String titolo, String nomePaziente, String patologia, float timerGenerale) {
        final String sql = "INSERT INTO Scenario (titolo, nome_paziente, patologia, timer_generale) VALUES (?,?,?,?)";
        int generatedId = -1;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, titolo);
            stmt.setString(2, nomePaziente);
            stmt.setString(3, patologia);
            stmt.setFloat(4, timerGenerale);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    // 2. Metodi per EsameFisico (invariati)
    public EsameFisico getEsameFisicoById(Integer id) {
        final String sql = "SELECT * FROM EsameFisico WHERE id_esame_fisico = ?";
        EsameFisico esameFisico = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                esameFisico = new EsameFisico();
                esameFisico.setIdEsameFisico(rs.getInt("id_esame_fisico"));

                Map<String, String> sections = new HashMap<>();
                sections.put("Generale", rs.getString("generale"));
                sections.put("Pupille", rs.getString("pupille"));
                sections.put("Collo", rs.getString("collo"));
                sections.put("Torace", rs.getString("torace"));
                sections.put("Cuore", rs.getString("cuore"));
                sections.put("Addome", rs.getString("addome"));
                sections.put("Retto", rs.getString("retto"));
                sections.put("Cute", rs.getString("cute"));
                sections.put("Estremità", rs.getString("estremità"));
                sections.put("Neurologico", rs.getString("neurologico"));
                sections.put("FAST", rs.getString("FAST"));

                esameFisico.setSections(sections);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Metodi per PazienteT0 (invariati)
    public PazienteT0 getPazienteT0ById(Integer scenarioId) {
        final String sqlPaziente = "SELECT * FROM PazienteT0 WHERE id_paziente = ?";
        final String sqlAccessiVenosi = "SELECT a.* FROM Accesso a JOIN AccessoVenoso av ON a.id_accesso = av.accesso_id WHERE av.paziente_t0_id = ?";
        final String sqlAccessiArteriosi = "SELECT a.* FROM Accesso a JOIN AccessoArterioso aa ON a.id_accesso = aa.accesso_id WHERE aa.paziente_t0_id = ?";

        PazienteT0 pazienteT0 = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmtPaziente = conn.prepareStatement(sqlPaziente)) {

            stmtPaziente.setInt(1, scenarioId);
            ResultSet rsPaziente = stmtPaziente.executeQuery();

            if (rsPaziente.next()) {
                List<Accesso> accessiVenosi = getAccessi(conn, sqlAccessiVenosi, scenarioId);
                List<Accesso> accessiArteriosi = getAccessi(conn, sqlAccessiArteriosi, scenarioId);

                pazienteT0 = new PazienteT0(
                        rsPaziente.getInt("id_paziente"),
                        rsPaziente.getString("PA"),
                        rsPaziente.getInt("FC"),
                        rsPaziente.getInt("RR"),
                        rsPaziente.getFloat("T"),
                        rsPaziente.getInt("SpO2"),
                        rsPaziente.getInt("EtCO2"),
                        rsPaziente.getString("Monitor"),
                        accessiVenosi,
                        accessiArteriosi
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pazienteT0;
    }

    private List<Accesso> getAccessi(Connection conn, String sql, int scenarioId) throws SQLException {
        List<Accesso> accessi = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                accessi.add(new Accesso(
                        rs.getInt("id_accesso"),
                        rs.getString("tipologia"),
                        rs.getString("posizione")
                ));
            }
        }
        return accessi;
    }

    // 4. Metodi di aggiornamento (invariati)
    public boolean updateScenarioDescription(int scenarioId, String descrizione) {
        return updateScenarioField(scenarioId, "descrizione", descrizione);
    }

    public boolean updateScenarioBriefing(int scenarioId, String briefing) {
        return updateScenarioField(scenarioId, "briefing", briefing);
    }

    public boolean updateScenarioPattoAula(int scenarioId, String patto_aula) {
        return updateScenarioField(scenarioId, "patto_aula", patto_aula);
    }

    public boolean updateScenarioAzioneChiave(int scenarioId, String azione_chiave) {
        return updateScenarioField(scenarioId, "azione_chiave", azione_chiave);
    }

    public boolean updateScenarioObiettiviDidattici(int scenarioId, String obiettivo) {
        return updateScenarioField(scenarioId, "obiettivo", obiettivo);
    }

    public boolean updateScenarioMaterialeNecessario(int scenarioId, String materiale) {
        return updateScenarioField(scenarioId, "materiale", materiale);
    }

    public boolean updateScenarioMoulage(int scenarioId, String moulage) {
        return updateScenarioField(scenarioId, "moulage", moulage);
    }

    public boolean updateScenarioLiquidi(int scenarioId, String liquidi) {
        return updateScenarioField(scenarioId, "liquidi", liquidi);
    }

    private boolean updateScenarioField(int scenarioId, String fieldName, String value) {
        final String sql = "UPDATE Scenario SET " + fieldName + " = ? WHERE id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setInt(2, scenarioId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Aggiungi questi metodi alla classe ScenarioService

    public boolean saveEsamiReferti(int scenarioId, List<EsamiRefertiView.EsameRefertoData> esamiData) {
        // Prima elimina gli esami esistenti per questo scenario
        if (!deleteEsamiReferti(scenarioId)) {
            return false;
        }

        // Poi inserisci i nuovi esami
        final String sql = "INSERT INTO EsameReferto (id_esame, id_scenario, tipo, media, referto_testuale) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (EsamiRefertiView.EsameRefertoData esame : esamiData) {
                stmt.setInt(1, esame.idEsame());
                stmt.setInt(2, scenarioId);
                stmt.setString(3, esame.tipo());
                stmt.setString(4, esame.media()); // Qui salva solo il nome del file
                stmt.setString(5, esame.refertoTestuale());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteEsamiReferti(int scenarioId) {
        final String sql = "DELETE FROM EsameReferto WHERE id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            return stmt.executeUpdate() >= 0; // Ritorna true anche se non c'era nulla da cancellare
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean savePazienteT0(int scenarioId,
                                  String pa, int fc, int rr, float temp,
                                  int spo2, int etco2, String monitor,
                                  List<PazienteT0View.AccessoData> venosiData,
                                  List<PazienteT0View.AccessoData> arteriosiData) {
        Connection conn = null;
        System.out.println("PA:" + pa);
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Salva i parametri vitali del paziente
            if (!savePazienteParams(conn, scenarioId, pa, fc, rr, temp, spo2, etco2, monitor)) {
                conn.rollback();
                return false;
            }

            // 2. Salva gli accessi venosi
            if (!saveAccessi(conn, scenarioId, venosiData, true)) {
                conn.rollback();
                return false;
            }

            // 3. Salva gli accessi arteriosi
            if (!saveAccessi(conn, scenarioId, arteriosiData, false)) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private boolean savePazienteParams(Connection conn, int scenarioId,
                                       String pa, int fc, int rr, float temp,
                                       int spo2, int etco2, String monitor) throws SQLException {
        // Verifica se esiste già
        boolean exists = getPazienteT0ById(scenarioId) != null;

        final String sql = exists ?
                "UPDATE PazienteT0 SET PA=?, FC=?, RR=?, T=?, SpO2=?, EtCO2=?, Monitor=? WHERE id_paziente=?" :
                "INSERT INTO PazienteT0 (id_paziente, PA, FC, RR, T, SpO2, EtCO2, Monitor) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;

            if (exists) {
                // Ordine per UPDATE
                paramIndex = getParamIndex(pa, fc, rr, temp, spo2, etco2, stmt, paramIndex);
                stmt.setString(paramIndex++, monitor);
                stmt.setInt(paramIndex, scenarioId); // WHERE condition
            } else {
                // Ordine per INSERT
                stmt.setInt(paramIndex++, scenarioId);
                paramIndex = getParamIndex(pa, fc, rr, temp, spo2, etco2, stmt, paramIndex);
                stmt.setString(paramIndex, monitor);
            }

            return stmt.executeUpdate() > 0;
        }
    }

    private int getParamIndex(String pa, int fc, int rr, float temp, int spo2, int etco2, PreparedStatement stmt, int paramIndex) throws SQLException {
        stmt.setString(paramIndex++, pa);
        stmt.setInt(paramIndex++, fc);
        stmt.setInt(paramIndex++, rr);
        stmt.setFloat(paramIndex++, temp);
        stmt.setInt(paramIndex++, spo2);
        stmt.setInt(paramIndex++, etco2);
        return paramIndex;
    }

    private boolean saveAccessi(Connection conn, int scenarioId,
                                List<PazienteT0View.AccessoData> accessiData,
                                boolean isVenoso) throws SQLException {
        if (accessiData == null || accessiData.isEmpty()) {
            return true; // Nessun accesso da salvare
        }

        // 1. Prima elimina gli accessi esistenti di questo tipo
        final String deleteRelSql = isVenoso ?
                "DELETE FROM AccessoVenoso WHERE paziente_t0_id=?" :
                "DELETE FROM AccessoArterioso WHERE paziente_t0_id=?";

        try (PreparedStatement stmt = conn.prepareStatement(deleteRelSql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }

        // 2. Inserisci i nuovi accessi
        final String insertAccessoSql = "INSERT INTO Accesso (tipologia, posizione) VALUES (?, ?)";
        final String insertRelSql = isVenoso ?
                "INSERT INTO AccessoVenoso (paziente_t0_id, accesso_id) VALUES (?, ?)" :
                "INSERT INTO AccessoArterioso (paziente_t0_id, accesso_id) VALUES (?, ?)";

        for (PazienteT0View.AccessoData data : accessiData) {
            // Inserisci accesso
            int accessoId;
            try (PreparedStatement stmt = conn.prepareStatement(insertAccessoSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, data.tipo());
                stmt.setString(2, data.posizione());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        accessoId = rs.getInt(1);
                    } else {
                        return false;
                    }
                }
            }

            // Inserisci relazione
            try (PreparedStatement stmt = conn.prepareStatement(insertRelSql)) {
                stmt.setInt(1, scenarioId);
                stmt.setInt(2, accessoId);
                stmt.executeUpdate();
            }
        }

        return true;
    }

    public List<EsameReferto> getEsamiRefertiByScenarioId(int scenarioId) {
        final String sql = "SELECT * FROM EsameReferto WHERE id_scenario = ? ORDER BY id_esame";
        List<EsameReferto> esami = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                EsameReferto esame = new EsameReferto();
                esame.setIdEsame(rs.getInt("id_esame"));
                esame.setIdScenario(rs.getInt("id_scenario"));
                esame.setTipo(rs.getString("tipo"));
                esame.setMedia(rs.getString("media"));
                esame.setRefertoTestuale(rs.getString("referto_testuale"));
                esami.add(esame);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return esami;
    }

    public boolean deleteScenario(int scenarioId) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Inizia una transazione

            // 1. Elimina gli accessi venosi e arteriosi (tabelle di relazione)
            deleteAccessi(conn, scenarioId, "AccessoVenoso");
            deleteAccessi(conn, scenarioId, "AccessoArterioso");

            // 2. Elimina gli accessi stessi (potrebbero essere condivisi, ma li eliminiamo comunque)
            deleteRelatedAccessi(conn, scenarioId);

            // 3. Elimina i tempi (per AdvancedScenario)
            deleteTempi(conn, scenarioId);

            // 4. Elimina PatientSimulatedScenario (se esiste)
            deletePatientSimulatedScenario(conn, scenarioId);

            // 5. Elimina AdvancedScenario (se esiste)
            deleteAdvancedScenario(conn, scenarioId);

            // 6. Elimina esami referti
            deleteEsamiReferti(conn, scenarioId);

            // 7. Elimina esame fisico
            deleteEsameFisico(conn, scenarioId);

            // 8. Elimina paziente T0
            deletePazienteT0(conn, scenarioId);

            // 9. Finalmente elimina lo scenario principale
            deleteScenarioPrincipale(conn, scenarioId);

            conn.commit(); // Conferma la transazione
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

// Metodi helper per le operazioni di cancellazione

    private void deleteAccessi(Connection conn, int scenarioId, String tableName) throws SQLException {
        final String sql = "DELETE FROM " + tableName + " WHERE paziente_t0_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    private void deleteRelatedAccessi(Connection conn, int scenarioId) throws SQLException {
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

    private void deleteTempi(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM Tempo WHERE id_advanced_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    private void deletePatientSimulatedScenario(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM PatientSimulatedScenario WHERE id_patient_simulated_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    private void deleteAdvancedScenario(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM AdvancedScenario WHERE id_advanced_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    private void deleteEsamiReferti(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM EsameReferto WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    private void deleteEsameFisico(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM EsameFisico WHERE id_esame_fisico = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    private void deletePazienteT0(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM PazienteT0 WHERE id_paziente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }

    private void deleteScenarioPrincipale(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM Scenario WHERE id_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        }
    }
}