package it.uniupo.simnova.service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import it.uniupo.simnova.api.model.*;
import it.uniupo.simnova.utils.DBConnect;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servizio per la gestione degli scenari.
 * <p>
 * Fornisce metodi per recuperare, creare e aggiornare gli scenari nel database.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@SuppressWarnings({"unchecked", "LoggingSimilarMessage"})
@Service
public class ScenarioService {
    /**
     * Servizio per la gestione del caricamento dei file.
     */
    private final FileStorageService fileStorageService;
    /**
     * Logger per la registrazione delle operazioni del servizio.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioService.class);


    /**
     * Costruttore del servizio ScenarioService.
     *
     * @param fileStorageService il servizio per la gestione del caricamento dei file
     */
    public ScenarioService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public static boolean isFileInUse(String filename) {
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
                        rs.getString("obiettivo"),
                        rs.getString("moulage"),
                        rs.getString("liquidi"),
                        rs.getFloat("timer_generale"),
                        rs.getString("autori"),
                        rs.getString("tipologia_paziente"),
                        rs.getString("info_genitore"),
                        rs.getString("target")
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
        final String sql = "SELECT id_scenario, titolo, autori, patologia, descrizione FROM Scenario";
        List<Scenario> scenarios = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Scenario scenario = new Scenario(
                        rs.getInt("id_scenario"),
                        rs.getString("titolo"),
                        rs.getString("autori"),
                        rs.getString("patologia"),
                        rs.getString("descrizione"));
                scenarios.add(scenario);
            }
            logger.info("Recuperati {} scenari dal database", scenarios.size());
        } catch (SQLException e) {
            logger.error("Errore durante il recupero degli scenari", e);
        }
        return scenarios;
    }

    /**
     * Crea o aggiorna uno scenario rapido nel database.
     *
     * @param scenarioId    l'ID dello scenario (se null, crea un nuovo scenario)
     * @param titolo        il titolo dello scenario
     * @param nomePaziente  il nome del paziente
     * @param patologia     la patologia
     * @param autori        gli autori
     * @param timerGenerale il timer generale
     * @param tipologia     la tipologia del paziente
     * @return l'ID dello scenario creato o aggiornato, o -1 in caso di errore
     */
    public int startQuickScenario(Integer scenarioId, String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologia) {
        try (Connection conn = DBConnect.getInstance().getConnection()) {
            // Se l'ID è fornito e lo scenario esiste, esegui un update
            if (scenarioId != null && existScenario(scenarioId)) {
                final String updateSql = "UPDATE Scenario SET titolo=?, nome_paziente=?, patologia=?, autori=?, timer_generale=?, tipologia_paziente=? WHERE id_scenario=?";

                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, titolo);
                    stmt.setString(2, nomePaziente);
                    stmt.setString(3, patologia);
                    stmt.setString(4, autori);
                    stmt.setFloat(5, timerGenerale);
                    stmt.setString(6, tipologia);
                    stmt.setInt(7, scenarioId);

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        logger.info("Scenario con ID {} aggiornato con successo", scenarioId);
                        return scenarioId;
                    } else {
                        logger.warn("Nessun scenario aggiornato con ID {}", scenarioId);
                        return -1;
                    }
                }
            } else {
                // Altrimenti, esegui un insert
                final String insertSql = "INSERT INTO Scenario (titolo, nome_paziente, patologia, autori, timer_generale, tipologia_paziente) VALUES (?,?,?,?,?,?)";

                try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, titolo);
                    stmt.setString(2, nomePaziente);
                    stmt.setString(3, patologia);
                    stmt.setString(4, autori);
                    stmt.setFloat(5, timerGenerale);
                    stmt.setString(6, tipologia);

                    int affectedRows = stmt.executeUpdate();
                    logger.info("Inserimento scenario: {} righe interessate", affectedRows);

                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int generatedId = generatedKeys.getInt(1);
                                logger.info("Scenario creato con ID: {}", generatedId);
                                return generatedId;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'inserimento/aggiornamento dello scenario", e);
        }
        return -1;
    }

    /**
     * Inizia uno scenario avanzato e restituisce il suo ID.
     *
     * @param titolo        il titolo dello scenario
     * @param nomePaziente  il nome del paziente
     * @param patologia     la patologia
     * @param timerGenerale il timer generale
     * @return l'ID dello scenario avanzato creato, o -1 in caso di errore
     */
    public int startAdvancedScenario(String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologia) {
        // Prima crea lo scenario base
        int scenarioId = startQuickScenario(-1, titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);

        if (scenarioId > 0) {
            final String sql = "INSERT INTO AdvancedScenario (id_advanced_scenario) VALUES (?)";

            try (Connection conn = DBConnect.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, scenarioId);
                stmt.executeUpdate();
                logger.info("Scenario avanzato creato con ID: {}", scenarioId);

            } catch (SQLException e) {
                logger.error("Errore durante l'inserimento dello scenario avanzato con ID: {}", scenarioId, e);
                return -1;
            }
        }
        return scenarioId;
    }

    /**
     * Inizia uno scenario simulato per pazienti e restituisce il suo ID.
     *
     * @param titolo        il titolo dello scenario
     * @param nomePaziente  il nome del paziente
     * @param patologia     la patologia
     * @param timerGenerale il timer generale
     * @return l'ID dello scenario simulato per pazienti creato, o -1 in caso di errore
     */
    public int startPatientSimulatedScenario(String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologia) {
        // Prima crea lo scenario avanzato
        int scenarioId = startAdvancedScenario(titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);

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

    /**
     * Recupera un esame fisico dal database utilizzando il suo ID.
     *
     * @param id l'identificativo dell'esame fisico da recuperare
     * @return l'esame fisico corrispondente all'ID fornito, o null se non trovato
     */
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

    /**
     * Aggiunge o aggiorna un esame fisico nel database.
     *
     * @param scenarioId l'ID dello scenario a cui associare l'esame fisico
     * @param examData   i dati dell'esame fisico da salvare
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
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

    /**
     * Recupera un paziente T0 dal database utilizzando il suo ID.
     *
     * @param scenarioId l'ID dello scenario a cui è associato il paziente T0
     * @return il paziente T0 corrispondente all'ID fornito, o null se non trovato
     */
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
                        rsPaziente.getInt("FiO2"),
                        rsPaziente.getFloat("LitriOssigeno"),
                        rsPaziente.getInt("EtCO2"),
                        rsPaziente.getString("Monitor"),
                        accessiVenosi,
                        accessiArteriosi
                );
                logger.info("Paziente T0 con ID {} recuperato con successo", scenarioId);
            } else {
                logger.warn("Nessun paziente T0 trovato con ID {}", scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero del paziente T0 con ID {}", scenarioId, e);
        }
        return pazienteT0;
    }

    /**
     * Recupera gli accessi dal database utilizzando l'ID dello scenario.
     *
     * @param conn       la connessione al database
     * @param sql        la query SQL per recuperare gli accessi
     * @param scenarioId l'ID dello scenario a cui sono associati gli accessi
     * @return una lista di oggetti Accesso
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private List<Accesso> getAccessi(Connection conn, String sql, int scenarioId) throws SQLException {
        List<Accesso> accessi = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                accessi.add(new Accesso(
                        rs.getInt("id_accesso"),
                        rs.getString("tipologia"),
                        rs.getString("posizione"),
                        rs.getString("lato"),
                        rs.getInt("misura")
                ));
            }
            logger.info("Recuperati {} accessi per lo scenario con ID {}", accessi.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero degli accessi per lo scenario con ID {}", scenarioId, e);
            throw e;
        }
        return accessi;
    }

    /**
     * Aggiorna la descrizione dello scenario.
     *
     * @param scenarioId  l'ID dello scenario da aggiornare
     * @param descrizione la nuova descrizione
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean updateScenarioDescription(int scenarioId, String descrizione) {
        return updateScenarioField(scenarioId, "descrizione", descrizione);
    }

    /**
     * Aggiorna il briefing dello scenario.
     *
     * @param scenarioId l'ID dello scenario da aggiornare
     * @param briefing   il nuovo briefing
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean updateScenarioBriefing(int scenarioId, String briefing) {
        return updateScenarioField(scenarioId, "briefing", briefing);
    }

    /**
     * Aggiorna il patto aula dello scenario.
     *
     * @param scenarioId l'ID dello scenario da aggiornare
     * @param patto_aula il nuovo patto aula
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean updateScenarioPattoAula(int scenarioId, String patto_aula) {
        return updateScenarioField(scenarioId, "patto_aula", patto_aula);
    }

    /**
     * Aggiorna gli obiettivi didattici dello scenario.
     *
     * @param scenarioId l'ID dello scenario da aggiornare
     * @param obiettivo  il nuovo obiettivo didattico
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean updateScenarioObiettiviDidattici(int scenarioId, String obiettivo) {
        return updateScenarioField(scenarioId, "obiettivo", obiettivo);
    }

    /**
     * Aggiorna il moulage dello scenario.
     *
     * @param scenarioId l'ID dello scenario da aggiornare
     * @param moulage    il nuovo moulage
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean updateScenarioMoulage(int scenarioId, String moulage) {
        return updateScenarioField(scenarioId, "moulage", moulage);
    }

    /**
     * Aggiorna i liquidi dello scenario.
     *
     * @param scenarioId l'ID dello scenario da aggiornare
     * @param liquidi    i nuovi liquidi
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean updateScenarioLiquidi(int scenarioId, String liquidi) {
        return updateScenarioField(scenarioId, "liquidi", liquidi);
    }

    /**
     * Aggiorna un campo specifico dello scenario.
     *
     * @param scenarioId l'ID dello scenario da aggiornare
     * @param fieldName  il nome del campo da aggiornare
     * @param value      il nuovo valore del campo
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    private boolean updateScenarioField(int scenarioId, String fieldName, String value) {
        final String sql = "UPDATE Scenario SET " + fieldName + " = ? WHERE id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setInt(2, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Campo {} dello scenario con ID {} aggiornato con successo", fieldName, scenarioId);
            } else {
                logger.warn("Nessun campo {} dello scenario con ID {} aggiornato", fieldName, scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del campo {} dello scenario con ID {}", fieldName, scenarioId, e);
            return false;
        }
    }

    /**
     * Salva gli esami e i referti associati a uno scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @param esamiData  la lista di esami e referti da salvare
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
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
                stmt.setString(4, esame.getMedia()); // Qui salva solo il nome del file
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

    /**
     * Elimina gli esami e i referti associati a uno scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    private boolean deleteEsamiReferti(int scenarioId) {
        final String sql = "DELETE FROM EsameReferto WHERE id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            boolean result = stmt.executeUpdate() >= 0; // Ritorna true anche se non c'era nulla da cancellare
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

    /**
     * Salva i dati del paziente T0 e gli accessi associati.
     *
     * @param scenarioId    l'ID dello scenario
     * @param pa            la pressione arteriosa
     * @param fc            la frequenza cardiaca
     * @param rr            la frequenza respiratoria
     * @param temp          la temperatura
     * @param spo2          la saturazione di ossigeno
     * @param etco2         il valore di EtCO2
     * @param monitor       il monitoraggio
     * @param venosiData    i dati degli accessi venosi
     * @param arteriosiData i dati degli accessi arteriosi
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean savePazienteT0(int scenarioId,
                                  String pa, int fc, int rr, double temp,
                                  int spo2, int fio2, float litrio2, int etco2, String monitor,
                                  List<Accesso> venosiData,
                                  List<Accesso> arteriosiData) {
        Connection conn = null;
        logger.debug("PA: {}", pa);
        if (fc < 0) {
            logger.warn("Frequenza cardiaca non valida: {}", fc);
            throw new IllegalArgumentException("Frequenza cardiaca non valida");
        }
        if (rr < 0) {
            logger.warn("Frequenza respiratoria non valida: {}", rr);
            throw new IllegalArgumentException("Frequenza respiratoria non valida");
        }
        if (spo2 < 0 || spo2 > 100) {
            logger.warn("Saturazione di ossigeno non valida: {}", spo2);
            throw new IllegalArgumentException("Saturazione di ossigeno non valida, deve essere tra 0 e 100");
        }
        if (fio2 < 0 || fio2 > 100) {
            logger.warn("FiO2 non valido: {}", fio2);
            throw new IllegalArgumentException("FiO2 non valido, deve essere tra 0 e 100");
        }
        if (litrio2 < 0) {
            logger.warn("LitriO2 non valido: {}", litrio2);
            throw new IllegalArgumentException("LitriO2 non valido");
        }
        if (etco2 < 0) {
            logger.warn("EtCO2 non valido: {}", etco2);
            throw new IllegalArgumentException("EtCO2 non valido");
        }
        if (!pa.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
            logger.warn("Formato della pressione arteriosa non valido: {}", pa);
            throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80')");
        }
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Salva i parametri vitali del paziente
            if (!savePazienteParams(conn, scenarioId, pa, fc, rr, temp, spo2, fio2, litrio2, etco2, monitor)) {
                conn.rollback();
                logger.warn("Rollback: impossibile salvare i parametri vitali per lo scenario con ID {}", scenarioId);
                return false;
            }

            // 2. Salva gli accessi venosi
            if (saveAccessi(conn, scenarioId, venosiData, true)) {
                conn.rollback();
                logger.warn("Rollback: impossibile salvare gli accessi venosi per lo scenario con ID {}", scenarioId);
                return false;
            }

            // 3. Salva gli accessi arteriosi
            if (saveAccessi(conn, scenarioId, arteriosiData, false)) {
                conn.rollback();
                logger.warn("Rollback: impossibile salvare gli accessi arteriosi per lo scenario con ID {}", scenarioId);
                return false;
            }

            conn.commit();
            logger.info("Paziente T0 con ID {} salvato con successo", scenarioId);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback per lo scenario con ID {}", scenarioId, ex);
                }
            }
            logger.error("Errore durante il salvataggio del paziente T0 con ID {}", scenarioId, e);
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

    /**
     * Salva i parametri vitali del paziente T0.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @param pa         la pressione arteriosa
     * @param fc         la frequenza cardiaca
     * @param rr         la frequenza respiratoria
     * @param temp       la temperatura
     * @param spo2       la saturazione di ossigeno
     * @param etco2      il valore di EtCO2
     * @param monitor    il monitoraggio
     * @return true se l'operazione ha avuto successo, false altrimenti
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private boolean savePazienteParams(Connection conn, int scenarioId,
                                       String pa, int fc, int rr, double temp,
                                       int spo2, int fio2, float litrio2, int etco2, String monitor) throws SQLException {
        // Verifica se esiste già
        boolean exists = getPazienteT0ById(scenarioId) != null;

        if (fc < 0) {
            logger.warn("Frequenza cardiaca non valida: {}", fc);
            throw new IllegalArgumentException("Frequenza cardiaca non valida");
        }
        if (rr < 0) {
            logger.warn("Frequenza respiratoria non valida: {}", rr);
            throw new IllegalArgumentException("Frequenza respiratoria non valida");
        }
        if (spo2 < 0 || spo2 > 100) {
            logger.warn("Saturazione di ossigeno non valida: {}", spo2);
            throw new IllegalArgumentException("Saturazione di ossigeno non valida, deve essere tra 0 e 100");
        }
        if (fio2 < 0 || fio2 > 100) {
            logger.warn("FiO2 non valido: {}", fio2);
            throw new IllegalArgumentException("FiO2 non valido, deve essere tra 0 e 100");
        }
        if (litrio2 < 0) {
            logger.warn("LitriO2 non valido: {}", litrio2);
            throw new IllegalArgumentException("LitriO2 non valido");
        }
        if (etco2 < 0) {
            logger.warn("EtCO2 non valido: {}", etco2);
            throw new IllegalArgumentException("EtCO2 non valido");
        }
        if (!pa.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
            logger.warn("Formato della pressione arteriosa non valido: {}", pa);
            throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80')");
        }
        final String sql = exists ?
                "UPDATE PazienteT0 SET PA=?, FC=?, RR=?, T=?, SpO2=?, FiO2=?, LitriOssigeno=?, EtCO2=?, Monitor=? WHERE id_paziente=?" :
                "INSERT INTO PazienteT0 (id_paziente, PA, FC, RR, T, SpO2, FiO2, LitriOssigeno, EtCO2, Monitor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;

            if (exists) {
                // Ordine per UPDATE
                paramIndex = getParamIndex(pa, fc, rr, temp, spo2, fio2, litrio2, etco2, stmt, paramIndex);
                stmt.setString(paramIndex++, monitor);
                stmt.setInt(paramIndex, scenarioId); // WHERE condition
            } else {
                // Ordine per INSERT
                stmt.setInt(paramIndex++, scenarioId);
                paramIndex = getParamIndex(pa, fc, rr, temp, spo2, fio2, litrio2, etco2, stmt, paramIndex);
                stmt.setString(paramIndex, monitor);
            }

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Parametri del paziente T0 {} con ID {} salvati con successo", exists ? "aggiornati" : "inseriti", scenarioId);
            } else {
                logger.warn("Nessun parametro del paziente T0 {} con ID {}", exists ? "aggiornato" : "inserito", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio dei parametri del paziente T0 con ID {}", scenarioId, e);
            throw e;
        }
    }

    /**
     * Imposta i parametri vitali del paziente T0 nella query.
     *
     * @param pa         la pressione arteriosa
     * @param fc         la frequenza cardiaca
     * @param rr         la frequenza respiratoria
     * @param temp       la temperatura
     * @param spo2       la saturazione di ossigeno
     * @param etco2      il valore di EtCO2
     * @param stmt       lo statement preparato
     * @param paramIndex l'indice del parametro corrente
     * @return l'indice del prossimo parametro
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private int getParamIndex(String pa, int fc, int rr, double temp, int spo2, int fio2, float litrio2, int etco2, PreparedStatement stmt, int paramIndex) throws SQLException {
        stmt.setString(paramIndex++, pa);
        stmt.setInt(paramIndex++, fc);
        stmt.setInt(paramIndex++, rr);
        stmt.setDouble(paramIndex++, temp);
        stmt.setInt(paramIndex++, spo2);
        stmt.setInt(paramIndex++, fio2);
        stmt.setFloat(paramIndex++, litrio2);
        stmt.setInt(paramIndex++, etco2);
        return paramIndex;
    }

    /**
     * Salva gli accessi venosi o arteriosi associati a uno scenario.
     *
     * @param conn        la connessione al database
     * @param scenarioId  l'ID dello scenario
     * @param accessiData la lista di accessi da salvare
     * @param isVenoso    true se si tratta di accessi venosi, false per accessi arteriosi
     * @return true se l'operazione ha avuto successo, false altrimenti
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private boolean saveAccessi(Connection conn, int scenarioId,
                                List<Accesso> accessiData,
                                boolean isVenoso) throws SQLException {
        if (accessiData == null || accessiData.isEmpty()) {
            logger.warn("Nessun accesso da salvare per lo scenario con ID {}", scenarioId);
            return false;
        }

        // 1. Prima elimina gli accessi esistenti di questo tipo
        final String deleteRelSql = isVenoso ?
                "DELETE FROM AccessoVenoso WHERE paziente_t0_id=?" :
                "DELETE FROM AccessoArterioso WHERE paziente_t0_id=?";

        try (PreparedStatement stmt = conn.prepareStatement(deleteRelSql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
            logger.info("Accessi {} eliminati per lo scenario con ID {}", isVenoso ? "venosi" : "arteriosi", scenarioId);
        }

        // 2. Inserisci i nuovi accessi
        final String insertAccessoSql = "INSERT INTO Accesso (tipologia, posizione, lato, misura) VALUES (?, ?, ?, ?)";
        final String insertRelSql = isVenoso ?
                "INSERT INTO AccessoVenoso (paziente_t0_id, accesso_id) VALUES (?, ?)" :
                "INSERT INTO AccessoArterioso (paziente_t0_id, accesso_id) VALUES (?, ?)";

        for (Accesso data : accessiData) {
            // Inserisci accesso
            int accessoId;
            try (PreparedStatement stmt = conn.prepareStatement(insertAccessoSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, data.getTipologia());
                stmt.setString(2, data.getPosizione());
                stmt.setString(3, data.getLato());
                stmt.setInt(4, data.getMisura());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        accessoId = rs.getInt(1);
                    } else {
                        logger.warn("Impossibile ottenere l'ID generato per l'accesso");
                        return true;
                    }
                }
            }

            // Inserisci relazione
            try (PreparedStatement stmt = conn.prepareStatement(insertRelSql)) {
                stmt.setInt(1, scenarioId);
                stmt.setInt(2, accessoId);
                stmt.executeUpdate();
                logger.info("Accesso {} inserito con ID {} per lo scenario con ID {}", isVenoso ? "venoso" : "arterioso", accessoId, scenarioId);
            }
        }

        return false;
    }

    /**
     * Recupera gli esami e i referti associati a uno scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @return una lista di oggetti EsameReferto
     */
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

    /**
     * Recupera i file media associati a uno scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @return una lista di nomi di file media
     */
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
            logger.info("Recuperati {} file media per lo scenario con ID {}", files.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei file media per lo scenario con ID {}", scenarioId, e);
        }

        return files;
    }

    /**
     * Elimina uno scenario e i suoi file media associati.
     *
     * @param scenarioId l'ID dello scenario da eliminare
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
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
            deleteRelatedMaterial(conn, scenarioId);
            deleteRelatedAccessi(conn);
            deleteTempi(conn, scenarioId);
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

    /**
     * Elimina gli accessi associati a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @param tableName  il nome della tabella degli accessi (AccessoVenoso o AccessoArterioso)
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
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

    /**
     * Recupera il tipo di scenario in base all'ID.
     *
     * @param idScenario l'ID dello scenario
     * @return il tipo di scenario come stringa
     */
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

    /**
     * Controlla se un ID è presente in una tabella specificata.
     *
     * @param id        l'ID da controllare
     * @param tableName il nome della tabella
     * @return true se l'ID è presente, false altrimenti
     */
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
                logger.warn("Tabella non riconosciuta: {}", tableName);
                return false;
        }

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            logger.info("Presenza dell'ID {} nella tabella {}: {}", id, tableName, exists);
            return exists; // Restituisce true se c'è almeno un risultato

        } catch (SQLException e) {
            logger.error("Errore durante la verifica della presenza dell'ID {} nella tabella {}", id, tableName, e);
            return false;
        }
    }

    /**
     * Salva i tempi associati a uno scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @param tempi      la lista di tempi da salvare
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean saveTempi(int scenarioId, List<Tempo> tempi) {
        Connection conn = null;
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false); // Disabilita l'autocommit

            // Prima elimina i tempi esistenti per questo scenario
            if (!deleteTempi(conn, scenarioId)) {
                conn.rollback();
                logger.warn("Rollback: impossibile eliminare i tempi esistenti per lo scenario con ID {}", scenarioId);
                return false;
            }

            final String sql = "INSERT INTO Tempo (id_tempo, id_advanced_scenario, PA, FC, RR, T, SpO2, FiO2, LitriOssigeno, EtCO2, Azione, TSi_id, TNo_id, altri_dettagli, timer_tempo, RuoloGenitore) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Tempo tempo : tempi) {
                    // Validazione parametri vitali
                    Integer fc = tempo.getFC();
                    Integer rr = tempo.getRR();
                    Integer spo2 = tempo.getSpO2();
                    Integer fio2 = tempo.getFiO2();
                    Float litrio2 = tempo.getLitriO2();
                    Integer etco2 = tempo.getEtCO2();
                    String pa = tempo.getPA();
                    int tsi = tempo.getTSi();
                    int tno = tempo.getTNo();

                    if (fc != null && fc < 0) {
                        logger.warn("Frequenza cardiaca non valida: {}", fc);
                        throw new IllegalArgumentException("Frequenza cardiaca non valida");
                    }
                    if (rr != null && rr < 0) {
                        logger.warn("Frequenza respiratoria non valida: {}", rr);
                        throw new IllegalArgumentException("Frequenza respiratoria non valida");
                    }
                    if (spo2 != null && (spo2 < 0 || spo2 > 100)) {
                        logger.warn("Saturazione di ossigeno non valida: {}", spo2);
                        throw new IllegalArgumentException("Saturazione di ossigeno non valida, deve essere tra 0 e 100");
                    }
                    if (fio2 != null && (fio2 < 0 || fio2 > 100)) {
                        logger.warn("FiO2 non valido: {}", fio2);
                        throw new IllegalArgumentException("FiO2 non valido, deve essere tra 0 e 100");
                    }
                    if (litrio2 != null && litrio2 < 0) {
                        logger.warn("LitriO2 non valido: {}", litrio2);
                        throw new IllegalArgumentException("LitriO2 non valido");
                    }
                    if (etco2 != null && etco2 < 0) {
                        logger.warn("EtCO2 non valido: {}", etco2);
                        throw new IllegalArgumentException("EtCO2 non valido");
                    }
                    if (pa != null && !pa.isEmpty() && !pa.matches("^\\s*\\d+\\s*/\\s*\\d+\\s*$")) {
                        logger.warn("Formato della pressione arteriosa non valido: {}", pa);
                        throw new IllegalArgumentException("Formato PA non valido, atteso 'sistolica/diastolica' (es. '120/80')");
                    }

                    if (tsi < 0 || tno < 0) {
                        logger.warn("ID TSi o TNo non valido: TSi={}, TNo={}", tsi, tno);
                        throw new IllegalArgumentException("ID TSi o TNo non valido");
                    }

                    stmt.setInt(1, tempo.getIdTempo());
                    stmt.setInt(2, scenarioId);
                    stmt.setString(3, pa);
                    stmt.setObject(4, fc);
                    stmt.setObject(5, rr);
                    stmt.setDouble(6, Math.round(tempo.getT() * 10) / 10.0); // Tronca temperatura a 1 decimale
                    stmt.setObject(7, spo2);
                    stmt.setObject(8, fio2);
                    stmt.setObject(9, litrio2);
                    stmt.setObject(10, etco2);
                    stmt.setString(11, tempo.getAzione());
                    stmt.setInt(12, tempo.getTSi());
                    stmt.setInt(13, tempo.getTNo());
                    stmt.setString(14, tempo.getAltriDettagli());
                    stmt.setLong(15, tempo.getTimerTempo());
                    stmt.setString(16, tempo.getRuoloGenitore());

                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                for (int result : results) {
                    if (result <= 0) {
                        conn.rollback();
                        logger.warn("Rollback: impossibile inserire i tempi per lo scenario con ID {}", scenarioId);
                        return false;
                    }
                }
            }

            // Salva anche i parametri aggiuntivi
            if (!saveParametriAggiuntivi(conn, scenarioId, tempi)) {
                conn.rollback();
                logger.warn("Rollback: impossibile salvare i parametri aggiuntivi per lo scenario con ID {}", scenarioId);
                return false;
            }

            conn.commit(); // Conferma la transazione
            logger.info("Tempi salvati con successo per lo scenario con ID {}", scenarioId);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.error("Errore durante il rollback per lo scenario con ID {}", scenarioId, e);
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback per lo scenario con ID {}", scenarioId, ex);
                }
            }
            logger.error("Errore durante il salvataggio dei tempi per lo scenario con ID {}", scenarioId, e);
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

    /**
     * Elimina i tempi associati a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @return true se l'operazione ha avuto successo, false altrimenti
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
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

    /**
     * Elimina i parametri aggiuntivi associati a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @return true se l'operazione ha avuto successo, false altrimenti
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private boolean deleteParametriAggiuntivi(Connection conn, int scenarioId) throws SQLException {
        final String sql = "DELETE FROM ParametriAggiuntivi WHERE scenario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            return stmt.executeUpdate() >= 0;
        }
    }

    /**
     * Salva i parametri aggiuntivi associati a uno scenario.
     *
     * @param conn       la connessione al database
     * @param scenarioId l'ID dello scenario
     * @param tempi      la lista di tempi da salvare
     * @return true se l'operazione ha avuto successo, false altrimenti
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private boolean saveParametriAggiuntivi(Connection conn, int scenarioId, List<Tempo> tempi) throws SQLException {
        // Controlla se ci sono parametri aggiuntivi da salvare
        final String sql = "INSERT INTO ParametriAggiuntivi (parametri_aggiuntivi_id, tempo_id, scenario_id, nome, valore, unità_misura) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramId = getMaxParamId(conn) + 1;

            for (Tempo tempo : tempi) {
                List<ParametroAggiuntivo> parametri = tempo.getParametriAggiuntivi();
                if (parametri != null) {
                    for (ParametroAggiuntivo param : parametri) {
                        stmt.setInt(1, paramId++);

                        stmt.setInt(2, tempo.getIdTempo()); // tempo_id
                        stmt.setInt(3, scenarioId); // scenario_id
                        stmt.setString(4, param.getNome()); // nome (base name)
                        stmt.setDouble(5, Double.parseDouble(param.getValore())); // valore
                        stmt.setString(6, param.getUnitaMisura()); // unità_misura

                        stmt.addBatch();
                    }
                }
            }

            if (stmt.getParameterMetaData().getParameterCount() > 0) {
                int[] results = stmt.executeBatch();
                boolean allSuccess = true;
                for (int result : results) {
                    if (result <= 0 && result != Statement.SUCCESS_NO_INFO) {
                        allSuccess = false;
                        logger.warn("Errore nel nel salvataggio dei parametri aggiuntivi dello scenario {}", scenarioId);
                    }
                }
                if (allSuccess && results.length > 0) {
                    logger.info("Salvati {} parametri aggiuntivi per lo scenario {}", results.length, scenarioId);
                } else if (results.length == 0) {
                    logger.info("Non ci sono parametri aggiuntivi da aggiungere per lo scenario {}", scenarioId);
                }
                return allSuccess;
            } else {
                logger.info("Non ci sono parametri aggiuntivi da aggiungere per lo scenario {}", scenarioId);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Errore del database durante l'inserimento dei parametri aggiuntivi per lo scenario {}", scenarioId, e);
            throw e;
        }
    }

    /**
     * Recupera il massimo ID dei parametri aggiuntivi.
     *
     * @param conn la connessione al database
     * @return il massimo ID dei parametri aggiuntivi
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private int getMaxParamId(Connection conn) throws SQLException {
        // Example: SELECT MAX(parametri_aggiuntivi_id) FROM ParametriAggiuntivi
        // Handle case where table is empty (return 0)
        final String sql = "SELECT MAX(parametri_aggiuntivi_id) FROM ParametriAggiuntivi";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1); // Returns 0 if table is empty and MAX returns NULL
            }
            return 0;
        }
    }

    /**
     * Aggiorna la sceneggiatura di uno scenario simulato.
     *
     * @param scenarioId    l'ID dello scenario
     * @param sceneggiatura la nuova sceneggiatura
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean updateScenarioSceneggiatura(Integer scenarioId, String sceneggiatura) {
        if (!isPresentInTable(scenarioId, "PatientSimulatedScenario")) {
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

    /**
     * Recupera uno scenario simulato in base all'ID.
     *
     * @param id l'ID dello scenario
     * @return lo scenario simulato
     */
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

    /**
     * Recupera i file media associati a uno scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @return una lista di nomi di file media
     */
    public List<String> getScenarioMediaFiles(Integer scenarioId) {
        List<String> mediaFiles = new ArrayList<>();
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT media FROM EsameReferto WHERE id_scenario = ?")) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String mediaFile = rs.getString("media");
                if (mediaFile != null && !mediaFile.isEmpty()) {
                    mediaFiles.add(mediaFile);
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei file media per lo scenario con ID {}", scenarioId, e);
        }
        return mediaFiles;

    }

    /**
     * Recupera i tempi associati a uno scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @return una lista di oggetti Tempo
     */
    public List<Tempo> getTempiByScenarioId(int scenarioId) {
        final String sql = "SELECT * FROM Tempo WHERE id_advanced_scenario = ? ORDER BY id_tempo";
        List<Tempo> tempi = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Tempo tempo = new Tempo(
                        rs.getInt("id_tempo"),
                        rs.getInt("id_advanced_scenario"),
                        rs.getString("PA"),
                        rs.getInt("FC"),
                        rs.getInt("RR"),
                        rs.getFloat("T"),
                        rs.getInt("SpO2"),
                        rs.getInt("FiO2"),
                        rs.getFloat("LitriOssigeno"),
                        rs.getInt("EtCO2"),
                        rs.getString("Azione"),
                        rs.getInt("TSi_id"),
                        rs.getInt("TNo_id"),
                        rs.getString("altri_dettagli"),
                        rs.getInt("timer_tempo"),
                        rs.getString("ruoloGenitore")
                );

                // Recupera i parametri aggiuntivi per questo tempo
                int tempoId = tempo.getIdTempo();
                List<ParametroAggiuntivo> parametriAggiuntivi = getParametriAggiuntiviByTempoId(tempoId, scenarioId);
                tempo.setParametriAggiuntivi(parametriAggiuntivi);

                tempi.add(tempo);
            }
            logger.info("Recuperati {} tempi per lo scenario con ID {}", tempi.size(), scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei tempi per lo scenario con ID {}", scenarioId, e);
        }
        return tempi;
    }

    /**
     * Recupera i parametri aggiuntivi associati a un tempo specifico di uno scenario.
     *
     * @param tempoId    l'ID del tempo
     * @param scenarioId l'ID dello scenario
     * @return una lista di oggetti ParametroAggiuntivo
     */
    public static List<ParametroAggiuntivo> getParametriAggiuntiviByTempoId(int tempoId, int scenarioId) {
        final String sql = "SELECT * FROM ParametriAggiuntivi WHERE tempo_id = ? AND scenario_id = ?";
        List<ParametroAggiuntivo> parametri = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tempoId);
            stmt.setInt(2, scenarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ParametroAggiuntivo param = new ParametroAggiuntivo(
                        rs.getInt("parametri_aggiuntivi_id"),
                        rs.getInt("tempo_id"),
                        rs.getInt("scenario_id"),
                        rs.getString("nome"),
                        rs.getString("valore"),
                        rs.getString("unità_misura")
                );
                param.setId(rs.getInt("parametri_aggiuntivi_id"));
                param.setTempoId(rs.getInt("tempo_id"));
                param.setScenarioId(rs.getInt("scenario_id"));
                param.setNome(rs.getString("nome"));
                param.setValore(rs.getString("valore"));
                param.setUnitaMisura(rs.getString("unità_misura"));

                parametri.add(param);
            }
            logger.debug("Recuperati {} parametri aggiuntivi per il tempo con ID {} e lo scenario con ID {}", parametri.size(), tempoId, scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei parametri aggiuntivi per il tempo con ID {} e lo scenario con ID {}", tempoId, scenarioId, e);
        }
        return parametri;
    }

    /**
     * Controlla se uno scenario esiste in base all'ID.
     *
     * @param scenarioId l'ID dello scenario
     * @return true se lo scenario esiste, false altrimenti
     */
    public boolean existScenario(int scenarioId) {
        final String sql = "SELECT 1 FROM Scenario WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next(); // Restituisce true se c'è almeno un risultato
            logger.info("Presenza dello scenario con ID {}: {}", scenarioId, exists);
            return exists;

        } catch (SQLException e) {
            logger.error("Errore durante la verifica dell'esistenza dello scenario con ID {}", scenarioId, e);
            return false;
        }
    }

    /**
     * Recupera la sceneggiatura di uno scenario simulato in base all'ID.
     *
     * @param scenarioId l'ID dello scenario
     * @return la sceneggiatura come stringa
     */
    public static String getSceneggiatura(int scenarioId) {
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
            conn.setAutoCommit(false); // Inizia la transazione

            List<Integer> idAzioniFinali = new ArrayList<>();
            if (nomiAzioniDaSalvare != null) {
                for (String nomeAzione : nomiAzioniDaSalvare) {
                    if (nomeAzione == null || nomeAzione.trim().isEmpty()) {
                        continue; // Salta nomi di azione vuoti o null
                    }
                    // Ottiene l'ID dell'azione chiave, creandola se non esiste
                    Integer idAzione = getOrCreateAzioneChiaveId(conn, nomeAzione.trim());
                    idAzioniFinali.add(idAzione);
                }
            }

            // 2. Cancella le associazioni esistenti per questo scenario dalla tabella AzioneScenario
            final String deleteSql = "DELETE FROM AzioneScenario WHERE id_scenario = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, scenarioId);
                int deletedRows = deleteStmt.executeUpdate();
                logger.info("Rimosse {} associazioni azione-scenario esistenti per scenario ID: {}", deletedRows, scenarioId);
            }

            // 3. Inserisci le nuove associazioni nella tabella AzioneScenario
            if (!idAzioniFinali.isEmpty()) {
                final String insertSql = "INSERT INTO AzioneScenario (id_scenario, id_azione) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    for (Integer idAzione : idAzioniFinali) {
                        insertStmt.setInt(1, scenarioId);
                        insertStmt.setInt(2, idAzione);
                        insertStmt.addBatch(); // Aggiunge l'operazione al batch
                    }
                    int[] batchResult = insertStmt.executeBatch(); // Esegue tutte le operazioni nel batch
                    logger.info("Inserite {} nuove associazioni azione-scenario per scenario ID: {}", batchResult.length, scenarioId);
                }
            } else {
                logger.info("Nessuna nuova azione chiave da associare per lo scenario ID: {}. Tutte le associazioni precedenti sono state rimosse.", scenarioId);
            }

            conn.commit(); // Conferma la transazione
            success = true;
            logger.info("Azioni chiave per lo scenario ID {} aggiornate con successo.", scenarioId);

        } catch (SQLException e) {
            logger.error("Errore SQL durante l'aggiornamento delle azioni chiave per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback(); // Annulla la transazione in caso di errore
                    logger.warn("Rollback della transazione eseguito per lo scenario ID {}", scenarioId);
                } catch (SQLException ex) {
                    logger.error("Errore durante il rollback della transazione per lo scenario ID {}: {}", scenarioId, ex.getMessage(), ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Ripristina la modalità auto-commit
                    conn.close(); // Chiudi la connessione
                } catch (SQLException e) {
                    logger.error("Errore durante la chiusura della connessione per lo scenario ID {}: {}", scenarioId, e.getMessage(), e);
                }
            }
        }
        return success;
    }

    private Integer getOrCreateAzioneChiaveId(Connection conn, String nomeAzione) throws SQLException {
        // Primo, verifica se l'azione chiave esiste già
        final String selectSql = "SELECT id_azione FROM AzioniChiave WHERE nome = ?";
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, nomeAzione);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_azione"); // Restituisce l'ID esistente
            }
        }

        // Se non esiste, inseriscila nella tabella AzioniChiave
        final String insertSql = "INSERT INTO AzioniChiave (nome) VALUES (?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, nomeAzione);
            int affectedRows = insertStmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        logger.info("Creata nuova AzioneChiave '{}' con ID: {}", nomeAzione, generatedKeys.getInt(1));
                        return generatedKeys.getInt(1); // Restituisce il nuovo ID generato
                    } else {
                        throw new SQLException("Creazione AzioneChiave fallita per '" + nomeAzione + "', nessun ID ottenuto.");
                    }
                }
            } else {
                throw new SQLException("Creazione AzioneChiave fallita per '" + nomeAzione + "', nessuna riga modificata.");
            }
        }
    }

    /**
     * Aggiorna uno scenario esistente nel database.
     *
     * @param scenario l'oggetto Scenario da aggiornare
     */
    public void update(Scenario scenario) {
        final String sql = "UPDATE Scenario SET titolo = ?, nome_paziente = ?, patologia = ?, descrizione = ?, " +
                "briefing = ?, patto_aula = ?, azione_chiave = ?, obiettivo = ?, " +
                "materiale = ?, moulage = ?, liquidi = ?, timer_generale = ? " +
                "WHERE id_scenario = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, scenario.getTitolo());
            stmt.setString(2, scenario.getNomePaziente());
            stmt.setString(3, scenario.getPatologia());
            stmt.setString(4, scenario.getDescrizione());
            stmt.setString(5, scenario.getBriefing());
            stmt.setString(6, scenario.getPattoAula());
            stmt.setString(7, scenario.getObiettivo());
            stmt.setString(8, scenario.getMoulage());
            stmt.setString(9, scenario.getLiquidi());
            stmt.setFloat(10, scenario.getTimerGenerale());
            stmt.setInt(11, scenario.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Scenario con ID {} aggiornato con successo", scenario.getId());
            } else {
                logger.warn("Nessuno scenario trovato con ID {}", scenario.getId());
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento dello scenario con ID {}", scenario.getId(), e);
            throw new RuntimeException("Impossibile aggiornare lo scenario", e);
        }
    }

    /**
     * Crea un nuovo scenario a partire da un file JSON, verificando che non esista già.
     *
     * @param jsonFile il file JSON contenente i dati dello scenario
     * @return true se lo scenario è stato creato con successo, false altrimenti
     */
    public boolean createScenarioByJSON(byte[] jsonFile) {
        try {
            // Converti il JSON in stringa
            String jsonString = new String(jsonFile, StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().create();

            // Parsa il JSON in un oggetto Map per estrarre i dati
            Map<String, Object> jsonData = gson.fromJson(jsonString, new TypeToken<Map<String, Object>>() {
            }.getType());

            // Estrai i dati principali dello scenario
            Map<String, Object> scenarioData = (Map<String, Object>) jsonData.get("scenario");
            int scenarioId = ((Double) scenarioData.get("id")).intValue();

            // Verifica se lo scenario esiste già
            if (existScenario(scenarioId)) {
                logger.warn("Lo scenario con ID {} esiste già nel database", scenarioId);
                return false;
            }

            // Crea lo scenario in base al tipo
            String scenarioType = (String) jsonData.get("type");
            boolean creationResult;

            switch (scenarioType) {
                case "Quick Scenario":
                    creationResult = createQuickScenarioFromJson(scenarioData);
                    break;
                case "Advanced Scenario":
                    creationResult = createAdvancedScenarioFromJson(jsonData);
                    break;
                case "Patient Simulated Scenario":
                    creationResult = createPatientSimulatedScenarioFromJson(jsonData);
                    break;
                default:
                    logger.error("Tipo di scenario non riconosciuto: {}", scenarioType);
                    return false;
            }

            if (!creationResult) {
                logger.error("Errore durante la creazione dello scenario di tipo {}", scenarioType);
                return false;
            }

            logger.info("Scenario creato con successo dal JSON con ID {}", scenarioId);
            return true;

        } catch (JsonSyntaxException e) {
            logger.error("Errore di sintassi nel JSON", e);
            return false;
        } catch (Exception e) {
            logger.error("Errore durante la creazione dello scenario dal JSON", e);
            return false;
        }
    }

    /**
     * Crea uno scenario rapido a partire dai dati JSON.
     *
     * @param scenarioData i dati JSON contenenti le informazioni dello scenario
     * @return true se lo scenario è stato creato con successo, false altrimenti
     */
    private boolean createQuickScenarioFromJson(Map<String, Object> scenarioData) {
        String titolo = (String) scenarioData.get("titolo");
        String nomePaziente = (String) scenarioData.get("nome_paziente");
        String patologia = (String) scenarioData.get("patologia");
        String autori = (String) scenarioData.get("autori");
        double timerGenerale = (Double) scenarioData.get("timer_generale");
        String tipologia = (String) scenarioData.get("tipologia_paziente");

        int newId = startQuickScenario(-1, titolo, nomePaziente, patologia, autori, (float) timerGenerale, tipologia);
        if (newId <= 0) return false;

        // Aggiorna i campi aggiuntivi
        Scenario scenario = new Scenario(
                newId,
                titolo,
                nomePaziente,
                patologia,
                (String) scenarioData.get("descrizione"),
                (String) scenarioData.get("briefing"),
                (String) scenarioData.get("patto_aula"),
                (String) scenarioData.get("obiettivo"),
                (String) scenarioData.get("moulage"),
                (String) scenarioData.get("liquidi"),
                (float) timerGenerale,
                (String) scenarioData.get("autori"),
                (String) scenarioData.get("tipologia_paziente"),
                (String) scenarioData.get("infoGenitore"),
                (String) scenarioData.get("target")
        );

        update(scenario);
        return true;
    }

    /**
     * Crea uno scenario avanzato a partire dai dati JSON.
     *
     * @param jsonData i dati JSON contenenti le informazioni dello scenario
     * @return true se lo scenario è stato creato con successo, false altrimenti
     */
    private boolean createAdvancedScenarioFromJson(Map<String, Object> jsonData) {
        Map<String, Object> scenarioData = (Map<String, Object>) jsonData.get("scenario");

        // Crea lo scenario base
        if (!createQuickScenarioFromJson(scenarioData)) {
            return false;
        }

        int scenarioId = ((Double) scenarioData.get("id")).intValue();

        // Aggiungi alla tabella AdvancedScenario
        final String sql = "INSERT INTO AdvancedScenario (id_advanced_scenario) VALUES (?)";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Errore durante l'inserimento in AdvancedScenario", e);
            return false;
        }

        // Salva i componenti aggiuntivi
        return saveAdvancedScenarioComponents(scenarioId, jsonData);
    }

    /**
     * Crea uno scenario simulato per pazienti a partire dai dati JSON.
     *
     * @param jsonData i dati JSON contenenti le informazioni dello scenario
     * @return true se lo scenario è stato creato con successo, false altrimenti
     */
    private boolean createPatientSimulatedScenarioFromJson(Map<String, Object> jsonData) {
        Map<String, Object> scenarioData = (Map<String, Object>) jsonData.get("scenario");

        // Crea lo scenario avanzato
        if (!createAdvancedScenarioFromJson(jsonData)) {
            return false;
        }

        int scenarioId = ((Double) scenarioData.get("id")).intValue();
        String sceneggiatura = (String) jsonData.get("sceneggiatura");

        // Aggiungi alla tabella PatientSimulatedScenario
        final String sql = "INSERT INTO PatientSimulatedScenario (id_patient_simulated_scenario, id_advanced_scenario, sceneggiatura) VALUES (?, ?, ?)";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.setInt(2, scenarioId);
            stmt.setString(3, sceneggiatura != null ? sceneggiatura : "");
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Errore durante l'inserimento in PatientSimulatedScenario", e);
            return false;
        }
    }

    /**
     * Salva i componenti avanzati dello scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @param jsonData   i dati JSON contenenti le informazioni dello scenario
     * @return true se il salvataggio è andato a buon fine, false altrimenti
     */
    private boolean saveAdvancedScenarioComponents(int scenarioId, Map<String, Object> jsonData) {
        // Salva esame fisico
        Map<String, Object> esameFisicoData = (Map<String, Object>) jsonData.get("esameFisico");
        if (esameFisicoData != null) {
            Map<String, String> sections = new HashMap<>();
            Map<String, String> sectionsData = (Map<String, String>) esameFisicoData.get("sections");
            if (sectionsData != null) {
                sections.putAll(sectionsData);
            }
            if (!addEsameFisico(scenarioId, sections)) {
                logger.warn("Errore durante il salvataggio dell'esame fisico");
            }
        }

        // Salva paziente T0
        Map<String, Object> pazienteT0Data = (Map<String, Object>) jsonData.get("pazienteT0");
        if (pazienteT0Data != null) {
            List<Map<String, Object>> venosiData = (List<Map<String, Object>>) pazienteT0Data.get("accessiVenosi");
            List<Map<String, Object>> arteriosiData = (List<Map<String, Object>>) pazienteT0Data.get("accessiArteriosi");

            List<Accesso> venosi = convertAccessoData(venosiData);
            List<Accesso> arteriosi = convertAccessoData(arteriosiData);

            if (!savePazienteT0(
                    scenarioId,
                    (String) pazienteT0Data.get("PA"),
                    ((Double) pazienteT0Data.get("FC")).intValue(),
                    ((Double) pazienteT0Data.get("RR")).intValue(),
                    (Double) pazienteT0Data.get("T"),
                    ((Double) pazienteT0Data.get("SpO2")).intValue(),
                    ((Double) pazienteT0Data.get("FiO2")).intValue(),
                    ((Double) pazienteT0Data.get("LitriOssigeno")).floatValue(),
                    ((Double) pazienteT0Data.get("EtCO2")).intValue(),
                    (String) pazienteT0Data.get("Monitor"),
                    venosi,
                    arteriosi
            )) {
                logger.warn("Errore durante il salvataggio del paziente T0");
            }
        }

        // Salva esami e referti
        List<Map<String, Object>> esamiRefertiData = (List<Map<String, Object>>) jsonData.get("esamiReferti");
        if (esamiRefertiData != null) {
            List<EsameReferto> esami = esamiRefertiData.stream()
                    .map(e -> new EsameReferto(
                            ((Double) e.get("idEsameReferto")).intValue(),
                            ((Double) e.get("idEsame")).intValue(),
                            (String) e.get("tipo"),
                            (String) e.get("media"),
                            (String) e.get("refertoTestuale")
                    ))
                    .collect(Collectors.toList());

            if (!saveEsamiReferti(scenarioId, esami)) {
                logger.warn("Errore durante il salvataggio degli esami e referti");
            }
        }

        // Salva tempi (solo per Advanced Scenario)
        List<Map<String, Object>> tempiData = (List<Map<String, Object>>) jsonData.get("tempi");
        if (tempiData != null) {
            List<Tempo> tempi = tempiData.stream()
                    .map(t -> {
                        List<Map<String, Object>> paramsData = (List<Map<String, Object>>) t.get("parametriAggiuntivi");
                        List<ParametroAggiuntivo> params = new ArrayList<>();

                        if (paramsData != null) {
                            paramsData.forEach(p -> {
                                String nome = (String) p.get("nome");
                                double valore = p.get("valore") instanceof String ?
                                        Double.parseDouble((String) p.get("valore")) :
                                        (Double) p.get("valore");
                                String unita = (String) p.get("unitaMisura");
                                params.add(new ParametroAggiuntivo(nome, valore, unita));
                            });
                        }

                        Tempo tempo = new Tempo(
                                ((Double) t.get("idTempo")).intValue(),
                                scenarioId,
                                (String) t.get("PA"),
                                t.get("FC") != null ? ((Double) t.get("FC")).intValue() : null,
                                t.get("RR") != null ? ((Double) t.get("RR")).intValue() : null,
                                (Double) t.get("T"),
                                t.get("SpO2") != null ? ((Double) t.get("SpO2")).intValue() : null,
                                t.get("FiO2") != null ? ((Double) t.get("FiO2")).intValue() : null,
                                t.get("LitriO2") != null ? ((Double) t.get("LitriO2")).floatValue() : null,
                                t.get("EtCO2") != null ? ((Double) t.get("EtCO2")).intValue() : null,
                                (String) t.get("Azione"),
                                t.get("TSi") != null ? ((Double) t.get("TSi")).intValue() : 0,
                                t.get("TNo") != null ? ((Double) t.get("TNo")).intValue() : 0,
                                (String) t.get("altriDettagli"),
                                ((Double) t.get("timerTempo")).longValue(),
                                (String) t.get("ruoloGenitore")
                        );
                        tempo.setParametriAggiuntivi(params);
                        return tempo;
                    })
                    .collect(Collectors.toList());

            if (!saveTempi(scenarioId, tempi)) {
                logger.warn("Errore durante il salvataggio dei tempi");
            }
        }

        return true;
    }

    /**
     * Converte i dati degli accessi in oggetti Accesso.
     *
     * @param accessiData la lista di mappe contenente i dati degli accessi
     * @return una lista di oggetti Accesso
     */
    private List<Accesso> convertAccessoData(List<Map<String, Object>> accessiData) {
        if (accessiData == null) return new ArrayList<>();

        return accessiData.stream()
                .map(a -> new Accesso(
                        ((Double) a.get("idAccesso")).intValue(),
                        (String) a.get("tipologia"),
                        (String) a.get("posizione"),
                        (String) a.get("lato"),
                        (Integer) a.get("misura")
                ))
                .collect(Collectors.toList());
    }

    public boolean isPediatric(int scenarioId) {
        final String sql = "SELECT tipologia_paziente FROM Scenario WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, scenarioId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String tipologiaPaziente = rs.getString("tipologia_paziente");
                return "Pediatrico".equalsIgnoreCase(tipologiaPaziente);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero della tipologia paziente per lo scenario con ID {}", scenarioId, e);
        }
        return false;

    }

    public boolean updateScenarioGenitoriInfo(Integer scenarioId, String value) {
        final String sql = "UPDATE Scenario SET info_genitore = ? WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setInt(2, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Informazioni per i genitori aggiornate con successo per lo scenario con ID {}", scenarioId);
            } else {
                logger.warn("Nessuna informazione aggiornata per i genitori dello scenario con ID {}", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento delle informazioni per i genitori dello scenario con ID {}", scenarioId, e);
            return false;
        }
    }

    public boolean updateScenarioTarget(Integer scenarioId, String string) {
        final String sql = "UPDATE Scenario SET target = ? WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, string);
            stmt.setInt(2, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Target aggiornato con successo per lo scenario con ID {}", scenarioId);
            } else {
                logger.warn("Nessun target aggiornato per lo scenario con ID {}", scenarioId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del target dello scenario con ID {}", scenarioId, e);
            return false;
        }
    }

    public void updateScenario(Integer idScenario, Map<String, String> updatedFields, Map<String, String> updatedSections, EsameFisico esameFisico, Map<String, String> pazienteT0, List<Accesso> accessiVenosi, List<Accesso> accessiArteriosi) {
        // Recupera lo scenario esistente per ottenere gli autori attuali
        Scenario esistente = getScenarioById(idScenario);
        String autoriEsistenti = esistente != null ? esistente.getAutori() : "";

        // Aggiungi il nuovo autore a quelli esistenti
        String nuovoAutore = updatedFields.get("Autore");
        String autoriCombinati;

        if (autoriEsistenti == null || autoriEsistenti.isEmpty()) {
            autoriCombinati = nuovoAutore;
        } else if (nuovoAutore == null || nuovoAutore.isEmpty()) {
            autoriCombinati = autoriEsistenti;
        } else {
            autoriCombinati = autoriEsistenti + ", " + nuovoAutore;
        }

        // Chiama startQuickScenario con gli autori combinati
        startQuickScenario(
                idScenario,
                updatedFields.get("Titolo"),
                updatedFields.get("NomePaziente"),
                updatedFields.get("Patologia"),
                autoriCombinati,
                Float.parseFloat(updatedFields.get("Timer")),
                updatedFields.get("TipologiaPaziente")
        );

        updateScenarioDescription(idScenario, updatedSections.get("Descrizione"));
        updateScenarioBriefing(idScenario, updatedSections.get("Briefing"));
        updateScenarioPattoAula(idScenario, updatedSections.get("PattoAula"));
        if (isPediatric(idScenario)) {
            updateScenarioGenitoriInfo(idScenario, updatedSections.get("InfoGenitore"));
        }
        updateScenarioLiquidi(idScenario, updatedSections.get("Liquidi"));
        updateScenarioMoulage(idScenario, updatedSections.get("Moulage"));
        updateScenarioObiettiviDidattici(idScenario, updatedSections.get("Obiettivi"));

        addEsameFisico(idScenario, esameFisico.getSections());

        savePazienteT0(idScenario,
                pazienteT0.get("PA"),
                Integer.parseInt(pazienteT0.get("FC")),
                Integer.parseInt(pazienteT0.get("RR")),
                Float.parseFloat(pazienteT0.get("Temperatura")),
                Integer.parseInt(pazienteT0.get("SpO2")),
                Integer.parseInt(pazienteT0.get("FiO2")),
                Float.parseFloat(pazienteT0.get("LitriO2")),
                Integer.parseInt(pazienteT0.get("EtCO2")),
                pazienteT0.get("Monitoraggio"),
                accessiVenosi,
                accessiArteriosi
        );
    }
}