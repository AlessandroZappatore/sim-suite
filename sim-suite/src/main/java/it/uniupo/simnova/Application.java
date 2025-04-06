package it.uniupo.simnova;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import it.uniupo.simnova.utils.DBConnect;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe principale dell'applicazione Spring Boot.
 * Configura l'applicazione e gestisce l'inizializzazione del database.
 * AppShellConfigurator Configura le impostazioni della pagina dell'applicazione
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@Theme(value = "sim.suite")
public class Application implements AppShellConfigurator {
    /**
     * Logger per la registrazione delle informazioni e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    /**
     * Metodo principale dell'applicazione.
     * Inizializza il database e avvia l'applicazione Spring Boot.
     *
     * @param args argomenti della riga di comando
     */
    public static void main(String[] args) {
        // Inizializza il database manualmente
        initializeDatabase();

        // Avvia l'applicazione Spring Boot
        SpringApplication.run(Application.class, args);
    }

    /**
     * Inizializza il database SQLite.
     * Stabilisce una connessione al database e verifica se la connessione è riuscita.
     */
    private static void initializeDatabase() {
        try (Connection connection = DBConnect.getInstance().getConnection()) {
            if (connection != null) {
                logger.info("✅ Connessione a SQLite avviata con successo!");
            } else {
                logger.error("⚠️ Errore: impossibile connettersi al database SQLite.");
            }
        } catch (SQLException e) {
            logger.error("❌ Errore durante l'inizializzazione del database: {}", e.getMessage(), e);
        }
    }

    /**
     * Configura le impostazioni della pagina dell'applicazione.
     *
     * @param settings le impostazioni della pagina da configurare
     */
    @Override
    public void configurePage(AppShellSettings settings) {
        // Imposta il viewport per la pagina
        settings.setViewport("width=device-width, initial-scale=1");
        // Imposta il titolo della pagina
        settings.setPageTitle("Sim Suite");
        // Imposta le dimensioni del corpo della pagina
        settings.setBodySize("100vw", "100vh");
        // Aggiunge un meta tag per l'autore
        settings.addMetaTag("author", "Alessandro Zappatore");
        // Aggiunge un'icona di favicon
        settings.addFavIcon("icon", "icons/favicon.ico", "256x256");
        // Aggiunge un collegamento per l'icona di favicon
        settings.addLink("shortcut icon", "icons/favicon.ico");
    }
}