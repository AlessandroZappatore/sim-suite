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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static it.uniupo.simnova.views.creation.TempoView.ADDITIONAL_PARAMETERS;
import static it.uniupo.simnova.views.creation.TempoView.CUSTOM_PARAMETER_KEY;

@Service
public class ScenarioService {
    private final FileStorageService fileStorageService;
    private static final Logger logger = LoggerFactory.getLogger(ScenarioService.class);

    public ScenarioService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * Recupera uno scenario dal database utilizzando il suo identificativo.
     *
     * @param id l'identificativo dello scenario da recuperare
     * @return lo scenario corrispondente all'identificativo fornito, o null se non trovato
     */
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
                logger.info("Scenario con ID {} recuperato con successo", id);
            } else {
                logger.warn("Nessuno scenario trovato con ID {}", id);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dello scenario con ID {}", id, e);
        }
        return scenario;
    }

    /**
     * Recupera tutti gli scenari dal database.
     *
     * @return una lista di tutti gli scenari presenti nel database
     */
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
            logger.info("Recuperati {} scenari dal database", scenarios.size());
        } catch (SQLException e) {
            logger.error("Errore durante il recupero degli scenari", e);
        }
        return scenarios;
    }

    public int startQuickScenario(String titolo, String nomePaziente, String patologia, float timerGenerale) {
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

    public int startAdvancedScenario(String titolo, String nomePaziente, String patologia, float timerGenerale) {
        // Prima crea lo scenario base
        int scenarioId = startQuickScenario(titolo, nomePaziente, patologia, timerGenerale);

        if (scenarioId > 0) {
            final String sql = "INSERT INTO AdvancedScenario (id_advanced_scenario) VALUES (?)";

            try (Connection conn = DBConnect.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, scenarioId);
                stmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return scenarioId;
    }

    public int startPatientSimulatedScenario(String titolo, String nomePaziente, String patologia, float timerGenerale) {
        // Prima crea lo scenario avanzato
        int scenarioId = startAdvancedScenario(titolo, nomePaziente, patologia, timerGenerale);

        if (scenarioId > 0) {
            final String sql = "INSERT INTO PatientSimulatedScenario (id_patient_simulated_scenario, id_advanced_scenario, sceneggiatura) VALUES (?,?,?)";

            try (Connection conn = DBConnect.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, scenarioId);
                stmt.setInt(2, scenarioId);
                stmt.setString(3, ""); // Sceneggiatura vuota inizialmente

                stmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return scenarioId;
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
                                  String pa, int fc, int rr, double temp,
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
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean savePazienteParams(Connection conn, int scenarioId,
                                       String pa, int fc, int rr, double temp,
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

    private int getParamIndex(String pa, int fc, int rr, double temp, int spo2, int etco2, PreparedStatement stmt, int paramIndex) throws SQLException {
        stmt.setString(paramIndex++, pa);
        stmt.setInt(paramIndex++, fc);
        stmt.setInt(paramIndex++, rr);
        stmt.setDouble(paramIndex++, temp);
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

    public List<String> getMediaFilesForScenario(int scenarioId) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return files;
    }

    public boolean deleteScenario(int scenarioId) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Ottieni la lista dei file media da eliminare PRIMA di cancellare dal DB
            List<String> mediaFiles = getMediaFilesForScenario(scenarioId);

            // 2. Esegui tutte le operazioni di cancellazione dal DB
            deleteAccessi(conn, scenarioId, "AccessoVenoso");
            deleteAccessi(conn, scenarioId, "AccessoArterioso");
            deleteRelatedAccessi(conn, scenarioId);
            deleteTempi(conn, scenarioId);
            deletePatientSimulatedScenario(conn, scenarioId);
            deleteAdvancedScenario(conn, scenarioId);
            deleteEsamiReferti(conn, scenarioId);
            deleteEsameFisico(conn, scenarioId);
            deletePazienteT0(conn, scenarioId);
            deleteScenarioPrincipale(conn, scenarioId);

            conn.commit(); // Conferma la transazione

            // 3. Dopo il commit, elimina i file media
            fileStorageService.deleteFiles(mediaFiles);

            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

    public String getScenarioType(int idScenario) {
        // Controlla se è uno Scenario base (presente solo nella tabella Scenario)
        if (isPresentInTable(idScenario, "Scenario") &&
                !isPresentInTable(idScenario, "AdvancedScenario")) {
            return "Quick Scenario";
        }

        // Controlla se è un AdvancedScenario (presente in Scenario e AdvancedScenario)
        if (isPresentInTable(idScenario, "AdvancedScenario")) {
            // Verifica se è un PatientSimulatedScenario
            if (isPresentInTable(idScenario, "PatientSimulatedScenario")) {
                return "Patient Simulated Scenario";
            }
            return "Advanced Scenario";
        }

        // Se non è presente in nessuna tabella
        return "ScenarioNotFound";
    }

    private boolean isPresentInTable(int id, String tableName) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE ";

        // Costruisce la query in base al nome della tabella
        switch (tableName) {
            case "Scenario":
                sql += "id_scenario = ?";
                break;
            case "AdvancedScenario":
                sql += "id_advanced_scenario = ?";
                break;
            case "PatientSimulatedScenario":
                sql += "id_patient_simulated_scenario = ?";
                break;
            default:
                return false;
        }

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Restituisce true se c'è almeno un risultato

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveTempi(int scenarioId, List<TempoData> tempiData) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Disabilita l'autocommit

            // Prima elimina i tempi esistenti per questo scenario
            if (!deleteTempi(conn, scenarioId)) {
                conn.rollback();
                return false;
            }

            final String sql = "INSERT INTO Tempo (id_tempo, id_advanced_scenario, PA, FC, RR, T, SpO2, EtCO2, Azione, TSi_id, TNo_id, altri_dettagli, timer_tempo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (TempoData tempo : tempiData) {
                    stmt.setInt(1, tempo.idTempo());
                    stmt.setInt(2, scenarioId);
                    stmt.setString(3, tempo.pa());
                    stmt.setInt(4, (int) tempo.fc());
                    stmt.setInt(5, (int) tempo.rr());
                    stmt.setDouble(6, tempo.t());
                    stmt.setInt(7, (int) tempo.spo2());
                    stmt.setInt(8, (int) tempo.etco2());
                    stmt.setString(9, tempo.azione());
                    stmt.setInt(10, tempo.tSiId());
                    stmt.setInt(11, tempo.tNoId());
                    stmt.setString(12, tempo.altriDettagli());
                    stmt.setInt(13, tempo.timerTempo());

                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                for (int result : results) {
                    if (result <= 0) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Salva anche i parametri aggiuntivi
            if (!saveParametriAggiuntivi(conn, scenarioId, tempiData)) {
                conn.rollback();
                return false;
            }

            conn.commit(); // Conferma la transazione
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean deleteTempi(Connection conn, int scenarioId) throws SQLException {
        // Prima elimina i parametri aggiuntivi
        if (!deleteParametriAggiuntivi(conn, scenarioId)) {
            return false;
        }

        // Poi elimina i tempi
        final String sql = "DELETE FROM Tempo WHERE id_advanced_scenario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            return stmt.executeUpdate() >= 0;
        }
    }

    private boolean deleteParametriAggiuntivi(Connection conn, int scenarioId) throws SQLException {
        // Ora possiamo eliminare direttamente per scenario_id
        final String sql = "DELETE FROM ParametriAggiuntivi WHERE scenario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            return stmt.executeUpdate() >= 0;
        }
    }

    private boolean saveParametriAggiuntivi(Connection conn, int scenarioId, List<TempoData> tempiData) throws SQLException {
        final String sql = "INSERT INTO ParametriAggiuntivi (parametri_aggiuntivi_id, tempo_id, scenario_id, nome, valore, unità_misura) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramId = 1;
            for (TempoData tempo : tempiData) {
                for (Map.Entry<String, Double> param : tempo.parametriAggiuntivi().entrySet()) {
                    String paramKey = param.getKey();
                    String paramName = paramKey.startsWith(CUSTOM_PARAMETER_KEY) ?
                            paramKey.substring(paramKey.indexOf('_') + 1) :
                            paramKey;

                    String unit = ADDITIONAL_PARAMETERS.containsKey(paramKey) ?
                            ADDITIONAL_PARAMETERS.get(paramKey).substring(
                                    ADDITIONAL_PARAMETERS.get(paramKey).indexOf('(') + 1,
                                    ADDITIONAL_PARAMETERS.get(paramKey).indexOf(')')
                            ) : "";

                    stmt.setInt(1, paramId++);
                    stmt.setInt(2, tempo.idTempo());
                    stmt.setInt(3, scenarioId); // Aggiunto scenario_id
                    stmt.setString(4, paramName);
                    stmt.setDouble(5, param.getValue());
                    stmt.setString(6, unit);

                    stmt.addBatch();
                }
            }

            int[] results = stmt.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean updateScenarioSceneggiatura(Integer scenarioId, String sceneggiatura) {
        // First check if this is a PatientSimulatedScenario
        if (!isPresentInTable(scenarioId, "PatientSimulatedScenario")) {
            return false;
        }

        final String sql = "UPDATE PatientSimulatedScenario SET sceneggiatura = ? WHERE id_patient_simulated_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sceneggiatura);
            stmt.setInt(2, scenarioId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
                        rs.getString("azione_chiave"),
                        rs.getString("obiettivo"),
                        rs.getString("materiale"),
                        rs.getString("moulage"),
                        rs.getString("liquidi"),
                        rs.getFloat("timer_generale"),
                        rs.getInt("id_advanced_scenario"),
                        new ArrayList<>(), // Tempi vuoti inizialmente
                        rs.getInt("id_patient_simulated_scenario"),
                        rs.getInt("id_advanced_scenario"),
                        rs.getString("sceneggiatura")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scenario;
    }

    // Record per rappresentare i dati di un tempo
    public record TempoData(
            int idTempo,
            String pa,
            double fc,
            double rr,
            double t,
            double spo2,
            double etco2,
            String azione,
            int tSiId,
            int tNoId,
            String altriDettagli,
            int timerTempo,
            Map<String, Double> parametriAggiuntivi
    ) {
    }

    public List<Tempo> getTempiByScenarioId(int scenarioId) {
        final String sql = "SELECT * FROM Tempo WHERE id_advanced_scenario = ? ORDER BY id_tempo";
        List<Tempo> tempi = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Tempo tempo = new Tempo();
                tempo.setIdTempo(rs.getInt("id_tempo"));
                tempo.setAdvancedScenario(rs.getInt("id_advanced_scenario"));
                tempo.setPA(rs.getString("PA"));
                tempo.setFC(rs.getInt("FC"));
                tempo.setRR(rs.getInt("RR"));
                tempo.setT(rs.getFloat("T"));
                tempo.setSpO2(rs.getInt("SpO2"));
                tempo.setEtCO2(rs.getInt("EtCO2"));
                tempo.setAzione(rs.getString("Azione"));
                tempo.setTSi(rs.getInt("TSi_id"));
                tempo.setTNo(rs.getInt("TNo_id"));
                tempo.setAltriDettagli(rs.getString("altri_dettagli"));
                tempo.setTimerTempo(rs.getInt("timer_tempo"));

                tempi.add(tempo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tempi;
    }

    public List<ParametroAggiuntivo> getParametriAggiuntiviById(int tempoId, int scenarioId) {
        final String sql = "SELECT * FROM ParametriAggiuntivi WHERE tempo_id = ? AND scenario_id = ?";
        List<ParametroAggiuntivo> parametri = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tempoId);
            stmt.setInt(2, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ParametroAggiuntivo param = new ParametroAggiuntivo();
                param.setId(rs.getInt("parametri_aggiuntivi_id"));
                param.setTempoId(rs.getInt("tempo_id"));
                param.setScenarioId(rs.getInt("scenario_id"));
                param.setNome(rs.getString("nome"));
                param.setValore(rs.getString("valore"));
                param.setUnitaMisura(rs.getString("unità_misura"));

                parametri.add(param);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parametri;
    }

    public boolean existScenario(int scenarioId) {
        final String sql = "SELECT 1 FROM Scenario WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Restituisce true se c'è almeno un risultato

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}