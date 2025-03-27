package it.uniupo.simnova.service;

import it.uniupo.simnova.api.model.Accesso;
import it.uniupo.simnova.api.model.Scenario;
import it.uniupo.simnova.api.model.EsameFisico;
import it.uniupo.simnova.api.model.PazienteT0;
import it.uniupo.simnova.utils.DBConnect;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ScenarioService {

    public Scenario getScenarioById(Integer id) {
        final String sql = "SELECT * FROM scenario WHERE id = ?";
        Scenario scenario = null;

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                scenario = new Scenario(
                        rs.getInt("id"),
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
                        getEsameFisicoById(id),        // Metodo da implementare
                        getPazienteT0ById(id)         // Metodo da implementare
                );
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scenario;
    }

    public EsameFisico getEsameFisicoById(Integer id) {
        final String sql = "SELECT * FROM EsameFisico WHERE id = ?";
        EsameFisico esameFisico = null;

        try {
            Connection conn = DBConnect.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                esameFisico = new EsameFisico(rs.getInt("id"),
                        rs.getString("generale"),
                        rs.getString("pupille"),
                        rs.getString("collo"),
                        rs.getString("torace"),
                        rs.getString("cuore"),
                        rs.getString("addome"),
                        rs.getString("retto"),
                        rs.getString("cute"),
                        rs.getString("estremita"),
                        rs.getString("neurologico"),
                        rs.getString("FAST")
                );
            }

            conn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return esameFisico;

    }

    public PazienteT0 getPazienteT0ById(Integer scenarioId) {
        final String sqlPaziente = "SELECT * FROM PazienteT0 WHERE id = ?";
        final String sqlAccessi = "SELECT * FROM Accesso WHERE id = ? AND tipologia = ?";

        PazienteT0 pazienteT0 = null;

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmtPaziente = conn.prepareStatement(sqlPaziente)) {

            // 1. Recupera i dati base del paziente
            stmtPaziente.setInt(1, scenarioId);
            ResultSet rsPaziente = stmtPaziente.executeQuery();

            if (rsPaziente.next()) {
                // 2. Recupera gli accessi venosi
                ArrayList<Accesso> accessiVenosi = new ArrayList<>();
                try (PreparedStatement stmtAccessi = conn.prepareStatement(sqlAccessi)) {
                    stmtAccessi.setInt(1, scenarioId);
                    stmtAccessi.setString(2, "venoso");
                    ResultSet rsAccessi = stmtAccessi.executeQuery();

                    while (rsAccessi.next()) {
                        accessiVenosi.add(new Accesso(
                                rsAccessi.getInt("id"),
                                rsAccessi.getString("tipologia"),
                                rsAccessi.getString("posizione")
                        ));
                    }
                }

                // 3. Recupera gli accessi arteriosi
                ArrayList<Accesso> accessiArteriosi = new ArrayList<>();
                try (PreparedStatement stmtAccessi = conn.prepareStatement(sqlAccessi)) {
                    stmtAccessi.setInt(1, scenarioId);
                    stmtAccessi.setString(2, "arterioso");
                    ResultSet rsAccessi = stmtAccessi.executeQuery();

                    while (rsAccessi.next()) {
                        accessiArteriosi.add(new Accesso(
                                rsAccessi.getInt("id"),
                                rsAccessi.getString("tipologia"),
                                rsAccessi.getString("posizione")
                        ));
                    }
                }

                // 4. Costruisci l'oggetto PazienteT0
                pazienteT0 = new PazienteT0(
                        rsPaziente.getInt("id"),
                        rsPaziente.getInt("PA"),
                        rsPaziente.getInt("FC"),
                        rsPaziente.getInt("RR"),
                        rsPaziente.getFloat("T"),
                        rsPaziente.getInt("SpO2"),
                        rsPaziente.getInt("EtCO2"),
                        rsPaziente.getString("Monitor"),
                        rsPaziente.getBoolean("accesso_venoso"),
                        rsPaziente.getBoolean("accesso_arterioso"),
                        accessiVenosi,
                        accessiArteriosi
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pazienteT0;
    }

    public List<Scenario> getAllScenarios() {
        final String sql = "SELECT id, titolo, nome_paziente, patologia, descrizione FROM Scenario";
        List<Scenario> scenarios = new ArrayList<>();

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Scenario scenario = new Scenario();
                scenario.setId(rs.getInt("id"));
                scenario.setTitolo(rs.getString("titolo"));
                scenario.setNomePaziente(rs.getString("nome_paziente"));
                scenario.setPatologia(rs.getString("patologia"));
                scenario.setDescrizione(rs.getString("descrizione"));
                scenarios.add(scenario);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero degli scenari", e);
        }
        return scenarios;
    }

    public int startScenario(String titolo, String nomePaziente, String patologia, float timerGenerale) {
        final String sql = "INSERT INTO scenario (titolo, nome_paziente, patologia, timer_generale) VALUES (?,?,?,?)";
        int generatedId = -1; // Valore di default in caso di errore

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // Imposta i parametri della query
            stmt.setString(1, titolo);
            stmt.setString(2, nomePaziente);
            stmt.setString(3, patologia);
            stmt.setFloat(4, timerGenerale);

            // Esegui l'inserimento
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Recupera l'ID generato
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la creazione di un nuovo scenario", e);
        }

        return generatedId;
    }

    public boolean updateScenarioDescription(int scenarioId, String descrizione) {
        final String sql = "UPDATE scenario SET descrizione = ? WHERE id = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, descrizione);
            stmt.setInt(2, scenarioId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'aggiornamento della descrizione", e);
        }
    }

    public boolean updateScenarioBriefing(int scenarioId, String briefing) {
        final String sql = "UPDATE scenario SET briefing = ? WHERE id = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, briefing);
            stmt.setInt(2, scenarioId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'aggiornamento della descrizione", e);
        }
    }

    public boolean updateScenarioPattoAula(int scenarioId, String patto_aula) {
        final String sql = "UPDATE scenario SET patto_aula = ? WHERE id = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patto_aula);
            stmt.setInt(2, scenarioId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'aggiornamento del patto d'aula", e);
        }
    }

    public boolean updateScenarioAzioneChiave(int scenarioId, String azione_chiave) {
        final String sql = "UPDATE scenario SET azione_chiave = ? WHERE id = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, azione_chiave);
            stmt.setInt(2, scenarioId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'aggiornamento dell'azione chiave", e);
        }
    }

    public boolean updateScenarioObiettiviDidattici(int scenarioId, String obiettivo) {
        final String sql = "UPDATE scenario SET obiettivo = ? WHERE id = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, obiettivo);
            stmt.setInt(2, scenarioId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'aggiornamento dell'obiettivo didattico", e);
        }
    }

    public boolean updateScenarioMaterialeNecessario(int scenarioId, String materiale) {
        final String sql = "UPDATE scenario SET materiale = ? WHERE id = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, materiale);
            stmt.setInt(2, scenarioId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'aggiornamento del materiale necessario", e);
        }
    }

    public boolean updateScenarioMoulage(int scenarioId, String moulage) {
        final String sql = "UPDATE scenario SET moulage = ? WHERE id = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, moulage);
            stmt.setInt(2, scenarioId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'aggiornamento del moulage", e);
        }
    }

    public boolean updateScenarioLiquidi(int scenarioId, String liquidi) {
        final String sql = "UPDATE scenario SET liquidi = ? WHERE id = ?";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, liquidi);
            stmt.setInt(2, scenarioId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante l'aggiornamento del moulage", e);
        }
    }

    public boolean addEsameFisico(int scenarioId, Map<String, String> examData) {
        // Validate input
        if (examData == null || examData.isEmpty()) {
            throw new IllegalArgumentException("Exam data cannot be null or empty");
        }

        // SQL with parameter placeholders
        final String sql = "INSERT INTO EsameFisico (id, generale, pupille, collo, torace, cuore, addome, retto, cute, estremita, neurologico, fast) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters - using getOrDefault to handle missing keys
            stmt.setInt(1, scenarioId);
            stmt.setString(2, examData.getOrDefault("Generale", ""));
            stmt.setString(3, examData.getOrDefault("Pupille", ""));
            stmt.setString(4, examData.getOrDefault("Collo", ""));
            stmt.setString(5, examData.getOrDefault("Torace", ""));
            stmt.setString(6, examData.getOrDefault("Cuore", ""));
            stmt.setString(7, examData.getOrDefault("Addome", ""));
            stmt.setString(8, examData.getOrDefault("Retto", ""));
            stmt.setString(9, examData.getOrDefault("Cute", ""));
            stmt.setString(10, examData.getOrDefault("Estremita", ""));
            stmt.setString(11, examData.getOrDefault("Neurologico", ""));
            stmt.setString(12, examData.getOrDefault("FAST", ""));

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante la creazione dell'esame fisico", e);
        }
    }
}
