package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioTest {

    @Test
    void getId() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals(1, scenario.getId());
    }

    @Test
    void setId() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setId(2);
        assertEquals(2, scenario.getId());
    }

    @Test
    void getTitolo() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals("Titolo", scenario.getTitolo());
    }

    @Test
    void setTitolo() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setTitolo("Nuovo Titolo");
        assertEquals("Nuovo Titolo", scenario.getTitolo());
    }

    @Test
    void getNomePaziente() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals("Nome Paziente", scenario.getNomePaziente());
    }

    @Test
    void setNomePaziente() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setNomePaziente("Nuovo Nome");
        assertEquals("Nuovo Nome", scenario.getNomePaziente());
    }

    @Test
    void getPatologia() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals("Patologia", scenario.getPatologia());
    }

    @Test
    void setPatologia() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setPatologia("Nuova Patologia");
        assertEquals("Nuova Patologia", scenario.getPatologia());
    }

    @Test
    void getDescrizione() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals("Descrizione", scenario.getDescrizione());
    }

    @Test
    void setDescrizione() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setDescrizione("Nuova Descrizione");
        assertEquals("Nuova Descrizione", scenario.getDescrizione());
    }

    @Test
    void getBriefing() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getBriefing());
    }

    @Test
    void setBriefing() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setBriefing("Nuovo Briefing");
        assertEquals("Nuovo Briefing", scenario.getBriefing());
    }

    @Test
    void getPattoAula() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getPattoAula());
    }

    @Test
    void setPattoAula() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setPattoAula("Nuovo Patto");
        assertEquals("Nuovo Patto", scenario.getPattoAula());
    }

    @Test
    void getAzioneChiave() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getAzioneChiave());
    }

    @Test
    void setAzioneChiave() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setAzioneChiave("Nuova Azione Chiave");
        assertEquals("Nuova Azione Chiave", scenario.getAzioneChiave());
    }

    @Test
    void getObiettivo() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getObiettivo());
    }

    @Test
    void setObiettivo() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setObiettivo("Nuovo Obiettivo");
        assertEquals("Nuovo Obiettivo", scenario.getObiettivo());
    }

    @Test
    void getMateriale() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getMateriale());
    }

    @Test
    void setMateriale() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setMateriale("Nuovo Materiale");
        assertEquals("Nuovo Materiale", scenario.getMateriale());
    }

    @Test
    void getMoulage() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getMoulage());
    }

    @Test
    void setMoulage() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setMoulage("Nuovo Moulage");
        assertEquals("Nuovo Moulage", scenario.getMoulage());
    }

    @Test
    void getLiquidi() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getLiquidi());
    }

    @Test
    void setLiquidi() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setLiquidi("Nuovi Liquidi");
        assertEquals("Nuovi Liquidi", scenario.getLiquidi());
    }

    @Test
    void getTimerGenerale() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals(0.0f, scenario.getTimerGenerale());
    }

    @Test
    void setTimerGenerale() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setTimerGenerale(10.0f);
        assertEquals(10.0f, scenario.getTimerGenerale());
    }

    @Test
    void testToString() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        String expected = "Scenario{id=1, titolo='Titolo', nomePaziente='Nome Paziente', patologia='Patologia', descrizione='Descrizione', briefing='null', pattoAula='null', azioneChiave='null', obiettivo='null', materiale='null', moulage='null', liquidi='null', timerGenerale=0.0}";
        assertEquals(expected, scenario.toString());
    }
}