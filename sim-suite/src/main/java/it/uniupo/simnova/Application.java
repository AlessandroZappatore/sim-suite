package it.uniupo.simnova;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import it.uniupo.simnova.utils.DBConnect;

import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}) // 🚨 Disabilita la configurazione automatica del DataSource
@Theme(value = "sim.suite")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        // Inizializza il database manualmente
        initializeDatabase();

        // Avvia l'applicazione Spring Boot
        SpringApplication.run(Application.class, args);
    }

    private static void initializeDatabase() {
        try (Connection connection = DBConnect.getInstance().getConnection()) {
            if (connection != null) {
                System.out.println("✅ Connessione a SQLite avviata con successo!");
            } else {
                System.err.println("⚠️ Errore: impossibile connettersi al database SQLite.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Errore durante l'inizializzazione del database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
