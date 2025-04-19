package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

/**
 * Classe di test per la classe {@link AdvancedScenario}.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
class AdvancedScenarioTest {

    /**
     * Test getId() per verificare che restituisca l'ID corretto.
     */
    @Test
    void getId_advanced_scenario() {
        AdvancedScenario scenario = new AdvancedScenario(1, "Test Scenario", "John Doe", "Flu",
                "Description", "Briefing", "Patto Aula", "Azione Chiave",
                "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 100, new ArrayList<>());

        assertEquals(100, scenario.getId_advanced_scenario());
    }

    /**
     * Test setId() per verificare che l'ID venga impostato correttamente.
     */
    @Test
    void setId_advanced_scenario() {
        AdvancedScenario scenario = new AdvancedScenario(1, "Test Scenario", "John Doe", "Flu",
                "Description", "Briefing", "Patto Aula", "Azione Chiave",
                "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 100, new ArrayList<>());

        scenario.setId_advanced_scenario(200);
        assertEquals(200, scenario.getId_advanced_scenario());
    }

    /**
     * Test getTempi() per verificare che restituisca la lista dei tempi corretta.
     */
    @Test
    void getTempi() {
        AdvancedScenario scenario = new AdvancedScenario(1, "Test Scenario", "John Doe", "Flu",
                "Description", "Briefing", "Patto Aula", "Azione Chiave",
                "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 100, new ArrayList<>());

        assertNotNull(scenario.getTempi());
    }

    /**
     * Test setTempi() per verificare che la lista dei tempi venga impostata correttamente.
     */
    @Test
    void setTempi() {
        AdvancedScenario scenario = new AdvancedScenario(1, "Test Scenario", "John Doe", "Flu",
                "Description", "Briefing", "Patto Aula", "Azione Chiave",
                "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 100, new ArrayList<>());

        ArrayList<Tempo> newTempi = new ArrayList<>();
        scenario.setTempi(newTempi);
        assertEquals(newTempi, scenario.getTempi());
    }

    /**
     * Test toString() per verificare che restituisca una rappresentazione corretta dell'oggetto.
     */
    @Test
    void testToString() {
        AdvancedScenario scenario = new AdvancedScenario(1, "Test Scenario", "John Doe", "Flu",
                "Description", "Briefing", "Patto Aula", "Azione Chiave",
                "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 100, new ArrayList<>());

        String result = scenario.toString();

        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("titolo='Test Scenario'"));
        assertTrue(result.contains("nomePaziente='John Doe'"));
        assertTrue(result.contains("patologia='Flu'"));

        assertTrue(result.contains("id_advanced_scenario=100"));
        assertTrue(result.contains("tempi=[]"));
    }
}