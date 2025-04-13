package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PatientSimulatedScenarioTest {

    @Test
    void getIdPatientSimulatedScenario() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");

        assertEquals(1, scenario.getIdPatientSimulatedScenario());
    }

    @Test
    void setIdPatientSimulatedScenario() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");
        scenario.setIdPatientSimulatedScenario(2);
        assertEquals(2, scenario.getIdPatientSimulatedScenario());
    }

    @Test
    void getAdvancedScenario() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");

        assertEquals(1, scenario.getAdvancedScenario());
    }

    @Test
    void setAdvancedScenario() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");
        scenario.setAdvancedScenario(2);
        assertEquals(2, scenario.getAdvancedScenario());
    }

    @Test
    void getSceneggiatura() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");

        assertEquals("Sceneggiatura", scenario.getSceneggiatura());
    }

    @Test
    void setSceneggiatura() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");
        scenario.setSceneggiatura("New Sceneggiatura");
        assertEquals("New Sceneggiatura", scenario.getSceneggiatura());
    }

    @Test
    void testToString() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario(1, "Test Scenario", "John Doe", "Flu", "Description", "Briefing", "Patto Aula", "Azione Chiave", "Obiettivo", "Materiale", "Moulage", "Liquidi", 30.0f, 1, new ArrayList<>(), 1, 1, "Sceneggiatura");
        String expectedString = "PatientSimulatedScenario{idPatientSimulatedScenario=1, advancedScenario=1, sceneggiatura='Sceneggiatura', id=1, titolo='Test Scenario', nome_paziente='John Doe', patologia='Flu', descrizione='Description', briefing='Briefing', patto_aula='Patto Aula', azione_chiave='Azione Chiave', obiettivo='Obiettivo', materiale='Materiale', moulage='Moulage', liquidi='Liquidi', timer_generale=30.0}";
        assertEquals(expectedString, scenario.toString());
    }
}