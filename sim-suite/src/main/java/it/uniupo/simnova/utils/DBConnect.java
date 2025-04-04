package it.uniupo.simnova.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe di utilità per la gestione della connessione al database SQLite.
 * <p>
 * Fornisce metodi per ottenere e chiudere connessioni al database.
 * Utilizza il pattern Singleton per garantire una singola istanza della connessione.
 * </p>
 *
 * @version 1.0
 */
public class DBConnect {
    // Posizione del database (percorso assoluto per maggiore affidabilità)
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.dir") + "/database.db";
    private static DBConnect instance = null;

    /**
     * Costruttore privato per il pattern Singleton.
     * <p>
     * Carica esplicitamente il driver SQLite.
     * </p>
     */
    private DBConnect() {
        System.out.println(DB_URL);
        try {
            // Carica esplicitamente il driver (opzionale)
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver SQLite non trovato", e);
        }
    }

    /**
     * Restituisce l'istanza Singleton di DBConnect.
     *
     * @return l'istanza Singleton di DBConnect
     */
    public static synchronized DBConnect getInstance() {
        if (instance == null) {
            instance = new DBConnect();
        }
        return instance;
    }

    /**
     * Ottiene una connessione al database.
     *
     * @return la connessione al database
     * @throws SQLException se non è possibile connettersi al database
     */
    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            throw new SQLException("Impossibile connettersi al database a " + DB_URL, e);
        }
    }

    /**
     * Chiude la connessione al database.
     *
     * @param connection la connessione da chiudere
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
            }
        }
    }
}