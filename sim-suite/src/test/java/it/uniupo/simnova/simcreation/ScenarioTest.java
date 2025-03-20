package it.uniupo.simnova.simcreation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
class ScenarioTest {
    @Test
    void QuickScenarioTest() {
        Scenario scenario = new Scenario("titoloScenario", "nomePaziente", "patologiaMalattia", "nomeFile", "descrizioneScenario", "briefing", "patto", "azioniChiave", "obiettiviDidattici", "materialeNecessario", "esamiReferti", "moulage", "liquidiPresidi", 1, 2, 3, 4, 5, 6, true, true, "monitor", "generale", "pupille", "collo", "torace", "cuore", "addome", "retto", "cute", "estremita", "neurologico", "FAST");
        assertEquals("titoloScenario", scenario.getTitoloScenario());
        assertEquals("nomePaziente", scenario.getNomePaziente());
        assertEquals("patologiaMalattia", scenario.getPatologiaMalattia());
        assertEquals("nomeFile", scenario.getNomeFile());
        assertEquals("descrizioneScenario", scenario.getDescrizioneScenario());
        assertEquals("briefing", scenario.getBriefing());
        assertEquals("patto", scenario.getPatto());
        assertEquals("azioniChiave", scenario.getAzioniChiave());
        assertEquals("obiettiviDidattici", scenario.getObiettiviDidattici());
        assertEquals("materialeNecessario", scenario.getMaterialeNecessario());
        assertEquals("esamiReferti", scenario.getEsamiReferti());
        assertEquals("moulage", scenario.getMoulage());
        assertEquals("liquidiPresidi", scenario.getLiquidiPresidi());
        assertEquals(1, scenario.getPA());
        assertEquals(2, scenario.getFC());
        assertEquals(3, scenario.getRR());
        assertEquals(4, scenario.getTemp());
        assertEquals(5, scenario.getSPO2());
        assertEquals(6, scenario.getEtCO2());
        assertTrue(scenario.isAccessiVenosi());
        assertTrue(scenario.isAccessiArteriosi());
        assertEquals("monitor", scenario.getMonitor());
        assertEquals("generale", scenario.getGenerale());
        assertEquals("pupille", scenario.getPupille());
        assertEquals("collo", scenario.getCollo());
        assertEquals("torace", scenario.getTorace());
        assertEquals("cuore", scenario.getCuore());
        assertEquals("addome", scenario.getAddome());
        assertEquals("retto", scenario.getRetto());
        assertEquals("cute", scenario.getCute());
        assertEquals("estremita", scenario.getEstremita());
        assertEquals("neurologico", scenario.getNeurologico());
        assertEquals("FAST", scenario.getFAST());
    }

    @Test
    void AdvancedScenarioTest() {
        TempoN tempo1 = new TempoN("T1", 2, 3, 4, 5, 6, 7, "Dettagli", "Azione", null, null);
        TempoN tempo2 = new TempoN("T2", 3, 4, 5, 6, 7, 8, "Dettagli", "Azione", null, tempo1);
        ArrayList<TempoN> tempi = new ArrayList<>();
        tempi.add(tempo1);
        tempi.add(tempo2);
        AdvancedScenario scenario = new AdvancedScenario("titoloScenario", "nomePaziente", "patologiaMalattia", "nomeFile", "descrizioneScenario", "briefing", "patto", "azioniChiave", "obiettiviDidattici", "materialeNecessario", "esamiReferti", "moulage", "liquidiPresidi", 1, 2, 3, 4, 5, 6, true, true, "monitor", "generale", "pupille", "collo", "torace", "cuore", "addome", "retto", "cute", "estremita", "neurologico", "FAST", tempi);

        assertEquals(2, scenario.getTempi().size());

        assertEquals("T1", scenario.getTempi().get(0).getNome());
        assertEquals("T2", scenario.getTempi().get(1).getNome());

        assertEquals(2, scenario.getTempi().get(0).getPA());
        assertEquals(3, scenario.getTempi().get(1).getPA());
    }

    @Test
    void PatientSimulatedScenarioTest() {
        PatientSimulatedScenario scenario = new PatientSimulatedScenario("titoloScenario", "nomePaziente", "patologiaMalattia", "nomeFile", "descrizioneScenario", "briefing", "patto", "azioniChiave", "obiettiviDidattici", "materialeNecessario", "esamiReferti", "moulage", "liquidiPresidi", 1, 2, 3, 4, 5, 6, true, true, "monitor", "generale", "pupille", "collo", "torace", "cuore", "addome", "retto", "cute", "estremita", "neurologico", "FAST", new ArrayList<>(), "Sceneggiatura");
        assertEquals("Sceneggiatura", scenario.getSceneggiatura());
    }
}