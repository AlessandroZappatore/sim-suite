package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per la classe {@link PatientSimulatedScenario}.
 */
class PatientSimulatedScenarioTest {

    /**
     * Test getId() per verificare che l'ID del paziente simulato venga restituito correttamente.
     */
    @Test
    void getIdPatientSimulatedScenario() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");

        assertEquals(1, scenario.getIdPatientSimulatedScenario());
    }

    /**
     * Test setId() per verificare che l'ID del paziente simulato venga impostato correttamente.
     */
    @Test
    void setIdPatientSimulatedScenario() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");
        scenario.setIdPatientSimulatedScenario(2);
        assertEquals(2, scenario.getIdPatientSimulatedScenario());
    }

    /**
     * Test getAdvancedScenario() per verificare che lo scenario avanzato venga restituito correttamente.
     */
    @Test
    void getAdvancedScenario() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");

        assertEquals(1, scenario.getAdvancedScenario());
    }

    /**
     * Test setAdvancedScenario() per verificare che lo scenario avanzato venga impostato correttamente.
     */
    @Test
    void setAdvancedScenario() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");
        scenario.setAdvancedScenario(2);
        assertEquals(2, scenario.getAdvancedScenario());
    }

    /**
     * Test getSceneggiatura() per verificare che la sceneggiatura venga restituita correttamente.
     */
    @Test
    void getSceneggiatura() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");

        assertEquals("Sceneggiatura", scenario.getSceneggiatura());
    }

    /**
     * Test setSceneggiatura() per verificare che la sceneggiatura venga impostata correttamente.
     */
    @Test
    void setSceneggiatura() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");
        scenario.setSceneggiatura("New Sceneggiatura");
        assertEquals("New Sceneggiatura", scenario.getSceneggiatura());
    }

    /**
     * Test toString() per verificare che la rappresentazione in stringa dell'oggetto sia corretta.
     */
    @Test
    void testToString() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");
        String expectedString = "PatientSimulatedScenario{idPatientSimulatedScenario=1, advancedScenario=1, sceneggiatura='Sceneggiatura', id=1, titolo='Test Scenario', nome_paziente='John Doe', patologia='Flu', descrizione='Description', briefing='Briefing', patto_aula='Patto Aula', azione_chiave='Azione Chiave', obiettivo='Obiettivo', materiale='Materiale', moulage='Moulage', liquidi='Liquidi', timer_generale=30.0}";
        assertEquals(expectedString, scenario.toString());
    }
}