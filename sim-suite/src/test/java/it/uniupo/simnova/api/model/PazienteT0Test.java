package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per la classe {@link PazienteT0}.
 */
class PazienteT0Test {
    /**
     * Oggetto di test della classe PazienteT0.
     */
    static PazienteT0 pazienteT0;

    /**
     * Test getId() per verificare che l'ID del paziente venga restituito correttamente.
     */
    @Test
    void getIdPaziente() {
        pazienteT0 = new PazienteT0(2, "130/85", 80, 18, 38.0, 99, 36, "New Monitor", null, null);
        assertEquals(2, pazienteT0.getIdPaziente());
    }

    /**
     * Test setId() per verificare che l'ID del paziente venga impostato correttamente.
     */
    @Test
    void setIdPaziente() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        pazienteT0.setIdPaziente(2);
        assertEquals(2, pazienteT0.getIdPaziente());
    }

    /**
     * Test getPA() per verificare che la pressione arteriosa venga restituita correttamente.
     */
    @Test
    void getPA() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        assertEquals("120/80", pazienteT0.getPA());
    }

    /**
     * Test setPA() per verificare che la pressione arteriosa venga impostata correttamente.
     */
    @Test
    void setPA() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        pazienteT0.setPA("130/85");
        assertEquals("130/85", pazienteT0.getPA());

        assertThrows(IllegalArgumentException.class, () -> pazienteT0.setPA("21"));
    }

    /**
     * Test getFC() per verificare che la frequenza cardiaca venga restituita correttamente.
     */
    @Test
    void getFC() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        assertEquals(75, pazienteT0.getFC());
    }

    /**
     * Test setFC() per verificare che la frequenza cardiaca venga impostata correttamente.
     */
    @Test
    void setFC() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        pazienteT0.setFC(80);
        assertEquals(80, pazienteT0.getFC());

        assertThrows(IllegalArgumentException.class, () -> pazienteT0.setFC(-1));
    }

    /**
     * Test getRR() per verificare che la frequenza respiratoria venga restituita correttamente.
     */
    @Test
    void getRR() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        assertEquals(16, pazienteT0.getRR());


    }

    /**
     * Test setRR() per verificare che la frequenza respiratoria venga impostata correttamente.
     */
    @Test
    void setRR() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        pazienteT0.setRR(18);
        assertEquals(18, pazienteT0.getRR());

        assertThrows(IllegalArgumentException.class, () -> pazienteT0.setRR(-1));
    }

    /**
     * Test getT() per verificare che la temperatura venga restituita correttamente.
     */
    @Test
    void getT() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        assertEquals(37.5, pazienteT0.getT());
    }

    /**
     * Test setT() per verificare che la temperatura venga impostata correttamente.
     */
    @Test
    void setT() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        pazienteT0.setT(38.0);
        assertEquals(38.0, pazienteT0.getT());
    }

    /**
     * Test getSpO2() per verificare che la saturazione di ossigeno venga restituita correttamente.
     */
    @Test
    void getSpO2() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        assertEquals(98, pazienteT0.getSpO2());
    }

    /**
     * Test setSpO2() per verificare che la saturazione di ossigeno venga impostata correttamente.
     */
    @Test
    void setSpO2() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        pazienteT0.setSpO2(99);
        assertEquals(99, pazienteT0.getSpO2());

        assertThrows(IllegalArgumentException.class, () -> pazienteT0.setSpO2(-1));
    }

    /**
     * Test getEtCO2() per verificare che l'EtCO2 venga restituito correttamente.
     */
    @Test
    void getEtCO2() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        assertEquals(35, pazienteT0.getEtCO2());
    }

    /**
     * Test setEtCO2() per verificare che l'EtCO2 venga impostato correttamente.
     */
    @Test
    void setEtCO2() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        pazienteT0.setEtCO2(36);
        assertEquals(36, pazienteT0.getEtCO2());

        assertThrows(IllegalArgumentException.class, () -> pazienteT0.setEtCO2(-1));
    }

    /**
     * Test getMonitor() per verificare che il monitor venga restituito correttamente.
     */
    @Test
    void getMonitor() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        assertEquals("Monitor", pazienteT0.getMonitor());
    }

    /**
     * Test setMonitor() per verificare che il monitor venga impostato correttamente.
     */
    @Test
    void setMonitor() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        pazienteT0.setMonitor("New Monitor");
        assertEquals("New Monitor", pazienteT0.getMonitor());
    }

    /**
     * Test getAccessiVenosi() per verificare che gli accessi venosi vengano restituiti correttamente.
     */
    @Test
    void getAccessiVenosi() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        assertNull(pazienteT0.getAccessiVenosi());
    }

    /**
     * Test setAccessiVenosi() per verificare che gli accessi venosi vengano impostati correttamente.
     */
    @Test
    void setAccessiVenosi() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        Accesso accessoVenoso = new Accesso(1, "Accesso Venoso", "Braccio Destro");
        pazienteT0.setAccessiVenosi(List.of(accessoVenoso));
        assertNotNull(pazienteT0.getAccessiVenosi());
        assertEquals(1, pazienteT0.getAccessiVenosi().getFirst().getId());
        assertEquals("Accesso Venoso", pazienteT0.getAccessiVenosi().getFirst().getTipologia());
        assertEquals("Braccio Destro", pazienteT0.getAccessiVenosi().getFirst().getPosizione());
    }

    /**
     * Test getAccessiArteriosi() per verificare che gli accessi arteriosi vengano restituiti correttamente.
     */
    @Test
    void getAccessiArteriosi() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        assertNull(pazienteT0.getAccessiArteriosi());
    }

    /**
     * Test setAccessiArteriosi() per verificare che gli accessi arteriosi vengano impostati correttamente.
     */
    @Test
    void setAccessiArteriosi() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        Accesso accessoArterioso = new Accesso(2, "Accesso Arterioso", "Braccio Sinistro");
        pazienteT0.setAccessiArteriosi(List.of(accessoArterioso));
        assertNotNull(pazienteT0.getAccessiArteriosi());
        assertEquals(2, pazienteT0.getAccessiArteriosi().getFirst().getId());
        assertEquals("Accesso Arterioso", pazienteT0.getAccessiArteriosi().getFirst().getTipologia());
        assertEquals("Braccio Sinistro", pazienteT0.getAccessiArteriosi().getFirst().getPosizione());
    }

    /**
     * Test toString() per verificare che la rappresentazione in stringa del paziente venga restituita correttamente.
     */
    @Test
    void testToString() {
        pazienteT0 = new PazienteT0(1, "120/80", 75, 16, 37.5, 98, 35, "Monitor", null, null);
        String expectedString = "PazienteT0{idPaziente=1, PA=120/80, FC=75, RR=16, T=37.5, SpO2=98, EtCO2=35, Monitor='Monitor', accessiVenosi=null, accessiArteriosi=null}";
        assertEquals(expectedString, pazienteT0.toString());
    }
}