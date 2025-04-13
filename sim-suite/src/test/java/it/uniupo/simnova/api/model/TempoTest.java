package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TempoTest {
    Tempo tempo;

    @Test
    void getIdTempo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(1, tempo.getIdTempo());
    }

    @Test
    void setIdTempo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setIdTempo(2);
        assertEquals(2, tempo.getIdTempo());
    }

    @Test
    void getAdvancedScenario() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(1, tempo.getAdvancedScenario());
    }

    @Test
    void setAdvancedScenario() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setAdvancedScenario(2);
        assertEquals(2, tempo.getAdvancedScenario());
    }

    @Test
    void getPA() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals("120/80", tempo.getPA());
    }

    @Test
    void setPA() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setPA("130/85");
        assertEquals("130/85", tempo.getPA());
    }

    @Test
    void getFC() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(75, tempo.getFC());
    }

    @Test
    void setFC() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setFC(80);
        assertEquals(80, tempo.getFC());
    }

    @Test
    void getRR() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(20, tempo.getRR());
    }

    @Test
    void setRR() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setRR(25);
        assertEquals(25, tempo.getRR());
    }

    @Test
    void getT() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(37.5, tempo.getT());
    }

    @Test
    void setT() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setT(38.0);
        assertEquals(38.0, tempo.getT());
    }

    @Test
    void getSpO2() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(98, tempo.getSpO2());
    }

    @Test
    void setSpO2() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setSpO2(95);
        assertEquals(95, tempo.getSpO2());
    }

    @Test
    void getEtCO2() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(35, tempo.getEtCO2());
    }

    @Test
    void setEtCO2() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setEtCO2(40);
        assertEquals(40, tempo.getEtCO2());
    }

    @Test
    void getAzione() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals("Azione", tempo.getAzione());
    }

    @Test
    void setAzione() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setAzione("Nuova Azione");
        assertEquals("Nuova Azione", tempo.getAzione());
    }

    @Test
    void getTSi() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(5, tempo.getTSi());
    }

    @Test
    void setTSi() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setTSi(10);
        assertEquals(10, tempo.getTSi());
    }

    @Test
    void getTNo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(0, tempo.getTNo());
    }

    @Test
    void setTNo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setTNo(15);
        assertEquals(15, tempo.getTNo());
    }

    @Test
    void getAltriDettagli() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals("Dettagli", tempo.getAltriDettagli());
    }

    @Test
    void setAltriDettagli() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setAltriDettagli("Nuovi Dettagli");
        assertEquals("Nuovi Dettagli", tempo.getAltriDettagli());
    }

    @Test
    void getTimerTempo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertEquals(60, tempo.getTimerTempo());
    }

    @Test
    void setTimerTempo() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        tempo.setTimerTempo(120);
        assertEquals(120, tempo.getTimerTempo());
    }

    @Test
    void getParametriAggiuntivi() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        assertNull(tempo.getParametriAggiuntivi());
    }

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

    @Test
    void testToString() {
        tempo = new Tempo(1, 1, "120/80", 75, 20, 37.5, 98, 35, "Azione", 5, 0, "Dettagli", 60);
        String expectedString = "Tempo{idTempo=1, advancedScenario=1, PA='120/80', FC=75, RR=20, T=37.5, SpO2=98, EtCO2=35, azione='Azione', TSi=5, TNo=0, altriDettagli='Dettagli', timerTempo=60}";
        assertEquals(expectedString, tempo.toString());
    }
}