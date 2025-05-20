package it.uniupo.simnova.service.scenario;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servizio per la gestione degli scenari.
 * <p>
 * Fornisce metodi per recuperare, creare e aggiornare gli scenari nel database.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@SuppressWarnings({"LoggingSimilarMessage"})
@Service
public class ScenarioService {
    /**
     * Logger per la registrazione delle operazioni del servizio.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioService.class);

    /**
     * Costruttore del servizio ScenarioService.
     */
    public ScenarioService() {
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
        final String sql = "SELECT id_scenario, titolo, autori, patologia, descrizione, tipologia_paziente FROM Scenario";
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
                        rs.getString("descrizione"),
                        rs.getString("tipologia_paziente"));
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
    public boolean isPresentInTable(int id, String tableName) {
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

        //noinspection SqlSourceToSinkFlow
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

    public void updateScenarioTitleAndAuthors(Integer scenarioId, String newTitle, String newAuthors) {
        final String sql = "UPDATE Scenario SET titolo = ?, autori = ? WHERE id_scenario = ?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newTitle);
            stmt.setString(2, newAuthors);
            stmt.setInt(3, scenarioId);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                logger.info("Titolo e autori aggiornati con successo per lo scenario con ID {}", scenarioId);
            } else {
                logger.warn("Nessun titolo o autore aggiornato per lo scenario con ID {}", scenarioId);
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del titolo e degli autori dello scenario con ID {}", scenarioId, e);
        }
    }
}