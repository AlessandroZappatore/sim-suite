package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EsameRefertoTest {
    EsameReferto esameReferto;

    @Test
    void getIdEsame() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals(1, esameReferto.getIdEsame());
    }

    @Test
    void setIdEsame() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setIdEsame(2);
        assertEquals(2, esameReferto.getIdEsame());
    }

    @Test
    void getScenario() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals(1, esameReferto.getScenario());
    }

    @Test
    void setIdScenario() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setIdScenario(2);
        assertEquals(2, esameReferto.getScenario());
    }

    @Test
    void getTipo() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals("Radiografia", esameReferto.getTipo());
    }

    @Test
    void setTipo() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setTipo("Ecografia");
        assertEquals("Ecografia", esameReferto.getTipo());
    }

    @Test
    void getMedia() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals("path/to/media", esameReferto.getMedia());
    }

    @Test
    void setMedia() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setMedia("new/path/to/media");
        assertEquals("new/path/to/media", esameReferto.getMedia());
    }

    @Test
    void getRefertoTestuale() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals("Referto testuale", esameReferto.getRefertoTestuale());
    }

    @Test
    void setRefertoTestuale() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setRefertoTestuale("Nuovo referto testuale");
        assertEquals("Nuovo referto testuale", esameReferto.getRefertoTestuale());
    }

    @Test
    void testToString() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        String expectedString = "EsameReferto{id_esame=1, id_scenario=1, tipo='Radiografia', media='path/to/media', refertoTestuale='Referto testuale'}";
        assertEquals(expectedString, esameReferto.toString());
    }
}