package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per la classe {@link EsameReferto}.
 */
class EsameRefertoTest {
    /**
     * Oggetto di test della classe EsameReferto.
     */
    EsameReferto esameReferto;

    /**
     * Test getId() per verificare che l'ID dell'esame sia corretto.
     */
    @Test
    void getIdEsame() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals(1, esameReferto.getIdEsame());
    }

    /**
     * Test setId() per verificare che l'ID dell'esame venga impostato correttamente.
     */
    @Test
    void setIdEsame() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setIdEsame(2);
        assertEquals(2, esameReferto.getIdEsame());
    }

    /**
     * Test getIdScenario() per verificare che l'ID dello scenario sia corretto.
     */
    @Test
    void getScenario() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals(1, esameReferto.getScenario());
    }

    /**
     * Test setIdScenario() per verificare che l'ID dello scenario venga impostato correttamente.
     */
    @Test
    void setIdScenario() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setIdScenario(2);
        assertEquals(2, esameReferto.getScenario());
    }

    /**
     * Test getTipo() per verificare che il tipo di esame sia corretto.
     */
    @Test
    void getTipo() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals("Radiografia", esameReferto.getTipo());
    }

    /**
     * Test setTipo() per verificare che il tipo di esame venga impostato correttamente.
     */
    @Test
    void setTipo() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setTipo("Ecografia");
        assertEquals("Ecografia", esameReferto.getTipo());
    }

    /**
     * Test getMedia() per verificare che il percorso del file multimediale sia corretto.
     */
    @Test
    void getMedia() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals("path/to/media", esameReferto.getMedia());
    }

    /**
     * Test setMedia() per verificare che il percorso del file multimediale venga impostato correttamente.
     */
    @Test
    void setMedia() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setMedia("new/path/to/media");
        assertEquals("new/path/to/media", esameReferto.getMedia());
    }

    /**
     * Test getRefertoTestuale() per verificare che il referto testuale sia corretto.
     */
    @Test
    void getRefertoTestuale() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        assertEquals("Referto testuale", esameReferto.getRefertoTestuale());
    }

    /**
     * Test setRefertoTestuale() per verificare che il referto testuale venga impostato correttamente.
     */
    @Test
    void setRefertoTestuale() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        esameReferto.setRefertoTestuale("Nuovo referto testuale");
        assertEquals("Nuovo referto testuale", esameReferto.getRefertoTestuale());
    }

    /**
     * Test toString() per verificare che la rappresentazione in stringa dell'oggetto sia corretta.
     */
    @Test
    void testToString() {
        esameReferto = new EsameReferto(1, 1, "Radiografia", "path/to/media", "Referto testuale");
        String expectedString = "EsameReferto{id_esame=1, id_scenario=1, tipo='Radiografia', media='path/to/media', refertoTestuale='Referto testuale'}";
        assertEquals(expectedString, esameReferto.toString());
    }
}