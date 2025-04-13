package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

class AdvancedScenarioTest {

    @Test
    void getId_advanced_scenario() {
        AdvancedScenario scenario = new AdvancedScenario(1, "Test Scenario", "John Doe", "Flu",
                "Description", "Briefing", "Patto Aula", "Azione Chiave",
                "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 100, new ArrayList<>());

        assertEquals(100, scenario.getId_advanced_scenario());
    }

    @Test
    void setId_advanced_scenario() {
        AdvancedScenario scenario = new AdvancedScenario(1, "Test Scenario", "John Doe", "Flu",
                "Description", "Briefing", "Patto Aula", "Azione Chiave",
                "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 100, new ArrayList<>());

        scenario.setId_advanced_scenario(200);
        assertEquals(200, scenario.getId_advanced_scenario());
    }

    @Test
    void getTempi() {
        AdvancedScenario scenario = new AdvancedScenario(1, "Test Scenario", "John Doe", "Flu",
                "Description", "Briefing", "Patto Aula", "Azione Chiave",
                "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 100, new ArrayList<>());

        assertNotNull(scenario.getTempi());
    }

    @Test
    void setTempi() {
        AdvancedScenario scenario = new AdvancedScenario(1, "Test Scenario", "John Doe", "Flu",
                "Description", "Briefing", "Patto Aula", "Azione Chiave",
                "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 100, new ArrayList<>());

        ArrayList<Tempo> newTempi = new ArrayList<>();
        scenario.setTempi(newTempi);
        assertEquals(newTempi, scenario.getTempi());
    }

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