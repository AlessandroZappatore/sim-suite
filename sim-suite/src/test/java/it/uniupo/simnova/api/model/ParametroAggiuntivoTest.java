package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per la classe {@link ParametroAggiuntivo}.
 */
class ParametroAggiuntivoTest {
    /**
     * Oggetto di test della classe ParametroAggiuntivo.
     */
    ParametroAggiuntivo parametroAggiuntivo;

    /**
     * Test getId() per verificare che l'ID venga restituito correttamente.
     */
    @Test
    void getId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals(1, parametroAggiuntivo.getId());
    }

    /**
     * Test setId() per verificare che l'ID venga impostato correttamente.
     */
    @Test
    void setId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setId(2);
        assertEquals(2, parametroAggiuntivo.getId());
    }

    /**
     * Test getTempoId() per verificare che l'ID del tempo venga restituito correttamente.
     */
    @Test
    void getTempoId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals(2, parametroAggiuntivo.getTempoId());
    }

    /**
     * Test setTempoId() per verificare che l'ID del tempo venga impostato correttamente.
     */
    @Test
    void setTempoId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setTempoId(3);
        assertEquals(3, parametroAggiuntivo.getTempoId());
    }

    /**
     * Test getScenarioId() per verificare che l'ID dello scenario venga restituito correttamente.
     */
    @Test
    void getScenarioId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals(3, parametroAggiuntivo.getScenarioId());
    }

    /**
     * Test setScenarioId() per verificare che l'ID dello scenario venga impostato correttamente.
     */
    @Test
    void setScenarioId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setScenarioId(4);
        assertEquals(4, parametroAggiuntivo.getScenarioId());
    }

    /**
     * Test getNome() per verificare che il nome venga restituito correttamente.
     */
    @Test
    void getNome() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals("Pressione venosa centrale", parametroAggiuntivo.getNome());
    }

    /**
     * Test setNome() per verificare che il nome venga impostato correttamente.
     */
    @Test
    void setNome() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setNome("Pressione arteriosa");
        assertEquals("Pressione arteriosa", parametroAggiuntivo.getNome());
    }

    /**
     * Test getValore() per verificare che il valore venga restituito correttamente.
     */
    @Test
    void getValore() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals("12", parametroAggiuntivo.getValore());
    }

    /**
     * Test setValore() per verificare che il valore venga impostato correttamente.
     */
    @Test
    void setValore() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setValore("15");
        assertEquals("15", parametroAggiuntivo.getValore());
    }

    /**
     * Test getUnitaMisura() per verificare che l'unità di misura venga restituita correttamente.
     */
    @Test
    void getUnitaMisura() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals("mmHg", parametroAggiuntivo.getUnitaMisura());
    }

    /**
     * Test setUnitaMisura() per verificare che l'unità di misura venga impostata correttamente.
     */
    @Test
    void setUnitaMisura() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setUnitaMisura("cmHg");
        assertEquals("cmHg", parametroAggiuntivo.getUnitaMisura());
    }

    /**
     * Test toString() per verificare che la rappresentazione in stringa dell'oggetto sia corretta.
     */
    @Test
    void testToString() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        String expected = "ParametroAggiuntivo{id=1, tempoId=2, scenarioId=3, nome='Pressione venosa centrale', valore='12', unitaMisura='mmHg'}";
        assertEquals(expected, parametroAggiuntivo.toString());
    }
}