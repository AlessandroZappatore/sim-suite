package it.uniupo.simnova.service.scenario.types;

import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdvancedScenarioService {
    private final ScenarioService scenarioService;

    private static final Logger logger = LoggerFactory.getLogger(AdvancedScenarioService.class);

    public AdvancedScenarioService(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }
    public int startAdvancedScenario(String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologia) {
        // Prima crea lo scenario base
        int scenarioId = scenarioService.startQuickScenario(-1, titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);

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

    public  List<ParametroAggiuntivo> getParametriAggiuntiviByTempoId(int tempoId, int scenarioId) {
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

    public boolean deleteTempi(Connection conn, int scenarioId) throws SQLException {
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
        final String sql = "DELETE FROM ParametriAggiuntivi WHERE scenario_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            return stmt.executeUpdate() >= 0;
        }
    }

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


}
