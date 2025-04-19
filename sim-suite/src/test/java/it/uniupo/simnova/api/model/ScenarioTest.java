package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per la classe {@link Scenario}.
 */
class ScenarioTest {

    /**
     * Test getId() per verificare che l'ID venga restituito correttamente.
     */
    @Test
    void getId() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals(1, scenario.getId());
    }

    /**
     * Test setId() per verificare che l'ID venga impostato correttamente.
     */
    @Test
    void setId() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setId(2);
        assertEquals(2, scenario.getId());
    }

    /**
     * Test getTitolo() per verificare che il titolo venga restituito correttamente.
     */
    @Test
    void getTitolo() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals("Titolo", scenario.getTitolo());
    }

    /**
     * Test setTitolo() per verificare che il titolo venga impostato correttamente.
     */
    @Test
    void setTitolo() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setTitolo("Nuovo Titolo");
        assertEquals("Nuovo Titolo", scenario.getTitolo());
    }

    /**
     * Test getNomePaziente() per verificare che il nome del paziente venga restituito correttamente.
     */
    @Test
    void getNomePaziente() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals("Nome Paziente", scenario.getNomePaziente());
    }

    /**
     * Test setNomePaziente() per verificare che il nome del paziente venga impostato correttamente.
     */
    @Test
    void setNomePaziente() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setNomePaziente("Nuovo Nome");
        assertEquals("Nuovo Nome", scenario.getNomePaziente());
    }

    /**
     * Test getPatologia() per verificare che la patologia venga restituita correttamente.
     */
    @Test
    void getPatologia() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals("Patologia", scenario.getPatologia());
    }

    /**
     * Test setPatologia() per verificare che la patologia venga impostata correttamente.
     */
    @Test
    void setPatologia() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setPatologia("Nuova Patologia");
        assertEquals("Nuova Patologia", scenario.getPatologia());
    }

    /**
     * Test getDescrizione() per verificare che la descrizione venga restituita correttamente.
     */
    @Test
    void getDescrizione() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals("Descrizione", scenario.getDescrizione());
    }

    /**
     * Test setDescrizione() per verificare che la descrizione venga impostata correttamente.
     */
    @Test
    void setDescrizione() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setDescrizione("Nuova Descrizione");
        assertEquals("Nuova Descrizione", scenario.getDescrizione());
    }

    /**
     * Test getBriefing() per verificare che il briefing venga restituito correttamente.
     */
    @Test
    void getBriefing() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getBriefing());
    }

    /**
     * Test setBriefing() per verificare che il briefing venga impostato correttamente.
     */
    @Test
    void setBriefing() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setBriefing("Nuovo Briefing");
        assertEquals("Nuovo Briefing", scenario.getBriefing());
    }

    /**
     * Test getPattoAula() per verificare che il patto aula venga restituito correttamente.
     */
    @Test
    void getPattoAula() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getPattoAula());
    }

    /**
     * Test setPattoAula() per verificare che il patto aula venga impostato correttamente.
     */
    @Test
    void setPattoAula() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setPattoAula("Nuovo Patto");
        assertEquals("Nuovo Patto", scenario.getPattoAula());
    }

    /**
     * Test getAzioneChiave() per verificare che l'azione chiave venga restituita correttamente.
     */
    @Test
    void getAzioneChiave() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getAzioneChiave());
    }

    /**
     * Test setAzioneChiave() per verificare che l'azione chiave venga impostata correttamente.
     */
    @Test
    void setAzioneChiave() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setAzioneChiave("Nuova Azione Chiave");
        assertEquals("Nuova Azione Chiave", scenario.getAzioneChiave());
    }

    /**
     * Test getObiettivo() per verificare che l'obiettivo venga restituito correttamente.
     */
    @Test
    void getObiettivo() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getObiettivo());
    }

    /**
     * Test setObiettivo() per verificare che l'obiettivo venga impostato correttamente.
     */
    @Test
    void setObiettivo() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setObiettivo("Nuovo Obiettivo");
        assertEquals("Nuovo Obiettivo", scenario.getObiettivo());
    }

    /**
     * Test getMateriale() per verificare che il materiale venga restituito correttamente.
     */
    @Test
    void getMateriale() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getMateriale());
    }

    /**
     * Test setMateriale() per verificare che il materiale venga impostato correttamente.
     */
    @Test
    void setMateriale() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setMateriale("Nuovo Materiale");
        assertEquals("Nuovo Materiale", scenario.getMateriale());
    }

    /**
     * Test getMoulage() per verificare che il moulage venga restituito correttamente.
     */
    @Test
    void getMoulage() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getMoulage());
    }

    /**
     * Test setMoulage() per verificare che il moulage venga impostato correttamente.
     */
    @Test
    void setMoulage() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setMoulage("Nuovo Moulage");
        assertEquals("Nuovo Moulage", scenario.getMoulage());
    }

    /**
     * Test getLiquidi() per verificare che i liquidi vengano restituiti correttamente.
     */
    @Test
    void getLiquidi() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertNull(scenario.getLiquidi());
    }

    /**
     * Test setLiquidi() per verificare che i liquidi vengano impostati correttamente.
     */
    @Test
    void setLiquidi() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setLiquidi("Nuovi Liquidi");
        assertEquals("Nuovi Liquidi", scenario.getLiquidi());
    }

    /**
     * Test getTimerGenerale() per verificare che il timer generale venga restituito correttamente.
     */
    @Test
    void getTimerGenerale() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        assertEquals(0.0f, scenario.getTimerGenerale());
    }

    /**
     * Test setTimerGenerale() per verificare che il timer generale venga impostato correttamente.
     */
    @Test
    void setTimerGenerale() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        scenario.setTimerGenerale(10.0f);
        assertEquals(10.0f, scenario.getTimerGenerale());
    }

    /**
     * Test toString() per verificare che la rappresentazione in stringa dell'oggetto Scenario sia corretta.
     */
    @Test
    void testToString() {
        Scenario scenario = new Scenario(1, "Titolo", "Nome Paziente", "Patologia", "Descrizione");
        String expected = "Scenario{id=1, titolo='Titolo', nomePaziente='Nome Paziente', patologia='Patologia', descrizione='Descrizione', briefing='null', pattoAula='null', azioneChiave='null', obiettivo='null', materiale='null', moulage='null', liquidi='null', timerGenerale=0.0}";
        assertEquals(expected, scenario.toString());
    }
}