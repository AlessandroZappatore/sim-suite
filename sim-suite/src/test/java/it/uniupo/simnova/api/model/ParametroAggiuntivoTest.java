package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParametroAggiuntivoTest {
    ParametroAggiuntivo parametroAggiuntivo;

    @Test
    void getId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals(1, parametroAggiuntivo.getId());
    }

    @Test
    void setId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setId(2);
        assertEquals(2, parametroAggiuntivo.getId());
    }

    @Test
    void getTempoId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals(2, parametroAggiuntivo.getTempoId());
    }

    @Test
    void setTempoId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setTempoId(3);
        assertEquals(3, parametroAggiuntivo.getTempoId());
    }

    @Test
    void getScenarioId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals(3, parametroAggiuntivo.getScenarioId());
    }

    @Test
    void setScenarioId() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setScenarioId(4);
        assertEquals(4, parametroAggiuntivo.getScenarioId());
    }

    @Test
    void getNome() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals("Pressione venosa centrale", parametroAggiuntivo.getNome());
    }

    @Test
    void setNome() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setNome("Pressione arteriosa");
        assertEquals("Pressione arteriosa", parametroAggiuntivo.getNome());
    }

    @Test
    void getValore() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals("12", parametroAggiuntivo.getValore());
    }

    @Test
    void setValore() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setValore("15");
        assertEquals("15", parametroAggiuntivo.getValore());
    }

    @Test
    void getUnitaMisura() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        assertEquals("mmHg", parametroAggiuntivo.getUnitaMisura());
    }

    @Test
    void setUnitaMisura() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        parametroAggiuntivo.setUnitaMisura("cmHg");
        assertEquals("cmHg", parametroAggiuntivo.getUnitaMisura());
    }

    @Test
    void testToString() {
        parametroAggiuntivo = new ParametroAggiuntivo(1, 2, 3, "Pressione venosa centrale", "12", "mmHg");
        String expected = "ParametroAggiuntivo{id=1, tempoId=2, scenarioId=3, nome='Pressione venosa centrale', valore='12', unitaMisura='mmHg'}";
        assertEquals(expected, parametroAggiuntivo.toString());
    }
}