package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per la classe {@link Tempo}.
 */
class TempoTest {
    /**
     * Oggetto di test per la classe Tempo.
     */
    Tempo tempo;

    /**
     * Test getId() per verificare che l'ID venga restituito correttamente.
     */
    @Test
    void getIdTempo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(1, tempo.getIdTempo());
    }

    /**
     * Test setId() per verificare che l'ID venga impostato correttamente.
     */
    @Test
    void setIdTempo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setIdTempo(2);
        assertEquals(2, tempo.getIdTempo());
    }

    /**
     * Test getAdvancedScenario() per verificare che lo scenario avanzato venga restituito correttamente.
     */
    @Test
    void getAdvancedScenario() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(1, tempo.getAdvancedScenario());
    }

    /**
     * Test setAdvancedScenario() per verificare che lo scenario avanzato venga impostato correttamente.
     */
    @Test
    void setAdvancedScenario() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setAdvancedScenario(2);
        assertEquals(2, tempo.getAdvancedScenario());
    }

    /**
     * Test getPA() per verificare che la pressione arteriosa venga restituita correttamente.
     */
    @Test
    void getPA() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals("120/80", tempo.getPA());
    }

    /**
     * Test setPA() per verificare che la pressione arteriosa venga impostata correttamente.
     */
    @Test
    void setPA() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setPA("130/85");
        assertEquals("130/85", tempo.getPA());
    }

    /**
     * Test getFC() per verificare che la frequenza cardiaca venga restituita correttamente.
     */
    @Test
    void getFC() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(75, tempo.getFC());
    }

    /**
     * Test setFC() per verificare che la frequenza cardiaca venga impostata correttamente.
     */
    @Test
    void setFC() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setFC(80);
        assertEquals(80, tempo.getFC());
    }

    /**
     * Test getRR() per verificare che la frequenza respiratoria venga restituita correttamente.
     */
    @Test
    void getRR() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(20, tempo.getRR());
    }

    /**
     * Test setRR() per verificare che la frequenza respiratoria venga impostata correttamente.
     */
    @Test
    void setRR() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setRR(25);
        assertEquals(25, tempo.getRR());
    }

    /**
     * Test getT() per verificare che la temperatura venga restituita correttamente.
     */
    @Test
    void getT() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(37.5, tempo.getT());
    }

    /**
     * Test setT() per verificare che la temperatura venga impostata correttamente.
     */
    @Test
    void setT() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setT(38.0);
        assertEquals(38.0, tempo.getT());
    }

    /**
     * Test getSpO2() per verificare che la saturazione di ossigeno venga restituita correttamente.
     */
    @Test
    void getSpO2() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(98, tempo.getSpO2());
    }

    /**
     * Test setSpO2() per verificare che la saturazione di ossigeno venga impostata correttamente.
     */
    @Test
    void setSpO2() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setSpO2(95);
        assertEquals(95, tempo.getSpO2());
    }

    /**
     * Test getEtCO2() per verificare che la pressione parziale di anidride carbonica venga restituita correttamente.
     */
    @Test
    void getEtCO2() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(35, tempo.getEtCO2());
    }

    /**
     * Test setEtCO2() per verificare che la pressione parziale di anidride carbonica venga impostata correttamente.
     */
    @Test
    void setEtCO2() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setEtCO2(40);
        assertEquals(40, tempo.getEtCO2());
    }

    /**
     * Test getAzione() per verificare che l'azione venga restituita correttamente.
     */
    @Test
    void getAzione() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals("Azione", tempo.getAzione());
    }

    /**
     * Test setAzione() per verificare che l'azione venga impostata correttamente.
     */
    @Test
    void setAzione() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setAzione("Nuova Azione");
        assertEquals("Nuova Azione", tempo.getAzione());
    }

    /**
     * Test getTSi() per verificare che il tempo in caso positivo venga salvato correttamente.
     */
    @Test
    void getTSi() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(5, tempo.getTSi());
    }

    /**
     * Test setTSi() per verificare che il tempo in caso positivo venga impostato correttamente.
     */
    @Test
    void setTSi() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setTSi(10);
        assertEquals(10, tempo.getTSi());
    }

    /**
     * Test getTNo() per verificare che il tempo in caso negativo venga restituito correttamente.
     */
    @Test
    void getTNo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(0, tempo.getTNo());
    }

    /**
     * Test setTNo() per verificare che il tempo in caso negativo venga impostato correttamente.
     */
    @Test
    void setTNo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setTNo(15);
        assertEquals(15, tempo.getTNo());
    }

    /**
     * Test getAltriDettagli() per verificare che gli altri dettagli vengano restituiti correttamente.
     */
    @Test
    void getAltriDettagli() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals("Dettagli", tempo.getAltriDettagli());
    }

    /**
     * Test setAltriDettagli() per verificare che gli altri dettagli vengano impostati correttamente.
     */
    @Test
    void setAltriDettagli() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setAltriDettagli("Nuovi Dettagli");
        assertEquals("Nuovi Dettagli", tempo.getAltriDettagli());
    }

    /**
     * Test getTimerTempo() per verificare che il timer venga restituito correttamente.
     */
    @Test
    void getTimerTempo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(60, tempo.getTimerTempo());
    }

    /**
     * Test setTimerTempo() per verificare che il timer venga impostato correttamente.
     */
    @Test
    void setTimerTempo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setTimerTempo(120);
        assertEquals(120, tempo.getTimerTempo());
    }

    /**
     * Test getParametriAggiuntivi() per verificare che i parametri aggiuntivi vengano restituiti correttamente.
     */
    @Test
    void getParametriAggiuntivi() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertNull(tempo.getParametriAggiuntivi());
    }

    /**
     * Test setParametriAggiuntivi() per verificare che i parametri aggiuntivi vengano impostati correttamente.
     */
    @Test
    void setParametriAggiuntivi() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        ParametroAggiuntivo parametroAggiuntivo = new ParametroAggiuntivo(1, 1, 1, "Nome", "Valore", "Unita");
        ParametroAggiuntivo parametroAggiuntivo1 = new ParametroAggiuntivo(2, 1, 1, "Nome2", "Valore2", "Unita2");
        List<ParametroAggiuntivo> parametroAggiuntivoList = new ArrayList<>();
        parametroAggiuntivoList.add(parametroAggiuntivo);
        parametroAggiuntivoList.add(parametroAggiuntivo1);
        tempo.setParametriAggiuntivi(parametroAggiuntivoList);
        assertEquals(parametroAggiuntivoList, tempo.getParametriAggiuntivi());
    }

    /**
     * Test toString() per verificare che la rappresentazione in stringa dell'oggetto Tempo sia corretta.
     */
    @Test
    void testToString() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        String expectedString = "Tempo{idTempo=1, advancedScenario=1, PA='120/80', FC=75, RR=20, T=37.5, SpO2=98, EtCO2=35, azione='Azione', TSi=5, TNo=0, altriDettagli='Dettagli', timerTempo=60}";
        assertEquals(expectedString, tempo.toString());
    }
}