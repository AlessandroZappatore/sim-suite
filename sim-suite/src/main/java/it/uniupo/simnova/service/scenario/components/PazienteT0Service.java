package it.uniupo.simnova.service.scenario.components;

import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class PazienteT0Service {

    private static final Logger logger = LoggerFactory.getLogger(PazienteT0Service.class);

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

    public void saveMonitor(int scenarioId, String monitor) {
        final String sql = "UPDATE PazienteT0 SET Monitor=? WHERE id_paziente=?";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, monitor);
            stmt.setInt(2, scenarioId);
            stmt.executeUpdate();
            logger.info("Monitor aggiornato per lo scenario con ID {}", scenarioId);
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del monitor per lo scenario con ID {}", scenarioId, e);
        }
    }

}
