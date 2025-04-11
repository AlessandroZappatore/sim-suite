package it.uniupo.simnova.service;

import it.uniupo.simnova.api.model.*;
import it.uniupo.simnova.utils.DBConnect;
import it.uniupo.simnova.views.creation.EsamiRefertiView;
import it.uniupo.simnova.views.creation.PazienteT0View;
import it.uniupo.simnova.views.creation.ScenarioEditView;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Inizia uno scenario rapido e restituisce il suo ID.
     *
     * @param titolo        il titolo dello scenario
     * @param nomePaziente  il nome del paziente
     * @param patologia     la patologia
     * @param timerGenerale il timer generale
     * @return l'ID dello scenario creato, o -1 in caso di errore
     */
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
            logger.info("Inserimento scenario: {} righe interessate", affectedRows);

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                        logger.info("Scenario creato con ID: {}", generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'inserimento dello scenario", e);
        }
        return generatedId;
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
    public int startAdvancedScenario(String titolo, String nomePaziente, String patologia, float timerGenerale) {
        // Prima crea lo scenario base
        int scenarioId = startQuickScenario(titolo, nomePaziente, patologia, timerGenerale);

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
                        rs.getString("posizione")
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
     * Aggiorna l'azione chiave dello scenario.
     *
     * @param scenarioId    l'ID dello scenario da aggiornare
     * @param azione_chiave il nuovo azione chiave
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean updateScenarioAzioneChiave(int scenarioId, String azione_chiave) {
        return updateScenarioField(scenarioId, "azione_chiave", azione_chiave);
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
     * Aggiorna il materiale necessario dello scenario.
     *
     * @param scenarioId l'ID dello scenario da aggiornare
     * @param materiale  il nuovo materiale necessario
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean updateScenarioMaterialeNecessario(int scenarioId, String materiale) {
        return updateScenarioField(scenarioId, "materiale", materiale);
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
    public boolean saveEsamiReferti(int scenarioId, List<EsamiRefertiView.EsameRefertoData> esamiData) {
        if (!deleteEsamiReferti(scenarioId)) {
            logger.warn("Impossibile eliminare i referti esistenti per lo scenario con ID {}", scenarioId);
            return false;
        }

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
                                  int spo2, int etco2, String monitor,
                                  List<PazienteT0View.AccessoData> venosiData,
                                  List<PazienteT0View.AccessoData> arteriosiData) {
        Connection conn = null;
        logger.debug("PA: {}", pa);
        try {
            conn = DBConnect.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Salva i parametri vitali del paziente
            if (!savePazienteParams(conn, scenarioId, pa, fc, rr, temp, spo2, etco2, monitor)) {
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
    private int getParamIndex(String pa, int fc, int rr, double temp, int spo2, int etco2, PreparedStatement stmt, int paramIndex) throws SQLException {
        stmt.setString(paramIndex++, pa);
        stmt.setInt(paramIndex++, fc);
        stmt.setInt(paramIndex++, rr);
        stmt.setDouble(paramIndex++, temp);
        stmt.setInt(paramIndex++, spo2);
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
                                List<PazienteT0View.AccessoData> accessiData,
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
                EsameReferto esame = new EsameReferto();
                esame.setIdEsame(rs.getInt("id_esame"));
                esame.setIdScenario(rs.getInt("id_scenario"));
                esame.setTipo(rs.getString("tipo"));
                esame.setMedia(rs.getString("media"));
                esame.setRefertoTestuale(rs.getString("referto_testuale"));
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
     * @param tempiData  la lista di dati dei tempi da salvare
     * @return true se l'operazione ha avuto successo, false altrimenti
     */
    public boolean saveTempi(int scenarioId, List<TempoData> tempiData) {
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
                        logger.warn("Rollback: impossibile inserire i tempi per lo scenario con ID {}", scenarioId);
                        return false;
                    }
                }
            }

            // Salva anche i parametri aggiuntivi
            if (!saveParametriAggiuntivi(conn, scenarioId, tempiData)) {
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
        // Ora possiamo eliminare direttamente per scenario_id
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
     * @param tempiData  la lista di dati dei tempi da salvare
     * @return true se l'operazione ha avuto successo, false altrimenti
     * @throws SQLException in caso di errore durante l'esecuzione della query
     */
    private boolean saveParametriAggiuntivi(Connection conn, int scenarioId, List<TempoData> tempiData) throws SQLException {
        // Ensure the table name and columns are correct
        final String sql = "INSERT INTO ParametriAggiuntivi (parametri_aggiuntivi_id, tempo_id, scenario_id, nome, valore, unità_misura) " +
                "VALUES (?, ?, ?, ?, ?, ?)"; // Assuming manual ID or adjust if auto-increment

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Determine starting ID - CAUTION: This is prone to race conditions if concurrent access occurs.
            // A sequence or auto-increment PK is strongly recommended.
            // If parametri_aggiuntivi_id is auto-increment, remove it from INSERT and don't set param 1.
            int paramId = getMaxParamId(conn) + 1; // Helper function needed if manual ID

            for (TempoData tempo : tempiData) {
                // Check if the map is not null and not empty before iterating
                if (tempo.parametriAggiuntivi() != null && !tempo.parametriAggiuntivi().isEmpty()) {
                    for (Map.Entry<String, ParameterValueUnit> param : tempo.parametriAggiuntivi().entrySet()) {
                        String paramName = param.getKey(); // This is the base name (e.g., "Glicemia" or "CustomParam")
                        ParameterValueUnit paramData = param.getValue();

                        // If parametri_aggiuntivi_id is AUTO_INCREMENT, remove this line and adjust indices below
                        stmt.setInt(1, paramId++); // Set manual ID (or remove if auto)

                        stmt.setInt(2, tempo.idTempo()); // tempo_id
                        stmt.setInt(3, scenarioId); // scenario_id
                        stmt.setString(4, paramName); // nome (base name)
                        stmt.setDouble(5, paramData.value()); // valore
                        // *** Use the unit from ParameterValueUnit ***
                        stmt.setString(6, paramData.unit()); // unità_misura

                        stmt.addBatch();
                    }
                } else {
                    // Log if a tempo has no additional parameters, if desired
                    // logger.debug("Tempo {} for scenario {} has no additional parameters.", tempo.idTempo(), scenarioId);
                }
            }

            if (stmt.getParameterMetaData().getParameterCount() > 0) { // Check if any batches were added
                int[] results = stmt.executeBatch();
                boolean allSuccess = true;
                for (int result : results) {
                    if (result <= 0 && result != Statement.SUCCESS_NO_INFO) { // Check for failures
                        allSuccess = false;
                        logger.warn("Failed to insert at least one additional parameter batch for scenario {}", scenarioId);
                        // Decide if this should cause a rollback (return false)
                    }
                }
                if (allSuccess && results.length > 0) {
                    logger.info("Saved {} additional parameter entries for scenario {}", results.length, scenarioId);
                } else if (results.length == 0) {
                    logger.info("No additional parameters to save for scenario {}", scenarioId);
                }
                return allSuccess; // Or true if partial success is acceptable
            } else {
                logger.info("No additional parameters batched for scenario {}", scenarioId);
                return true; // Nothing to save, so technically successful
            }

        } catch (SQLException e) {
            logger.error("Error during batch insert of additional parameters for scenario {}", scenarioId, e);
            throw e; // Re-throw to trigger rollback in saveTempi
        }
    }

    // Helper function if using manual ID (Needs implementation based on your DB)
    // **Strongly recommend using Auto-Increment Primary Key instead**
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
                logger.info("Scenario simulato per pazienti con ID {} recuperato con successo", id);
            } else {
                logger.warn("Nessuno scenario simulato per pazienti trovato con ID {}", id);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dello scenario simulato per pazienti con ID {}", id, e);
        }
        return scenario;
    }

    public boolean updateAccessiPazienteT0(Integer scenarioId, List<ScenarioEditView.AccessoData> venosiData, List<ScenarioEditView.AccessoData> arteriosiData) {
    return true;
    }

    public record ParameterValueUnit(double value, String unit) {}


    /**
     * Rappresenta i dati di un tempo associato a uno scenario.
     *
     * @param idTempo             l'ID del tempo
     * @param pa                  la pressione arteriosa
     * @param fc                  la frequenza cardiaca
     * @param rr                  la frequenza respiratoria
     * @param t                   la temperatura
     * @param spo2                la saturazione di ossigeno
     * @param etco2               il valore di EtCO2
     * @param azione              l'azione associata
     * @param tSiId               l'ID del tempo di inizio
     * @param tNoId               l'ID del tempo di fine
     * @param altriDettagli       altri dettagli
     * @param timerTempo          il timer del tempo
     * @param parametriAggiuntivi i parametri aggiuntivi
     */
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
            Map<String, ParameterValueUnit> parametriAggiuntivi
    ) {
    }

    /**
     * Recupera i tempi associati a uno scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @return una lista di oggetti Tempo
     */
    public static List<Tempo> getTempiByScenarioId(int scenarioId) {
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
                ParametroAggiuntivo param = new ParametroAggiuntivo();
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
            stmt.setString(7, scenario.getAzioneChiave());
            stmt.setString(8, scenario.getObiettivo());
            stmt.setString(9, scenario.getMateriale());
            stmt.setString(10, scenario.getMoulage());
            stmt.setString(11, scenario.getLiquidi());
            stmt.setFloat(12, scenario.getTimerGenerale());
            stmt.setInt(13, scenario.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Scenario with ID {} updated successfully", scenario.getId());
            } else {
                logger.warn("No scenario found with ID {}", scenario.getId());
            }
        } catch (SQLException e) {
            logger.error("Error updating scenario with ID {}", scenario.getId(), e);
            throw new RuntimeException("Failed to update scenario", e);
        }
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }
}