package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessoTest {
    Accesso accesso;

    @Test
    void getId() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        assertEquals(1, accesso.getId());
    }

    @Test
    void getTipologia() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        assertEquals("CVC", accesso.getTipologia());
    }

    @Test
    void getPosizione() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        assertEquals("Giugulare destra", accesso.getPosizione());
    }

    @Test
    void setId() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        accesso.setId(2);
        assertEquals(2, accesso.getId());
    }

    @Test
    void setTipologia() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        accesso.setTipologia("Agocannula");
        assertEquals("Agocannula", accesso.getTipologia());
    }

    @Test
    void setPosizione() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        accesso.setPosizione("Cubitale sinistro");
        assertEquals("Cubitale sinistro", accesso.getPosizione());
    }

    @Test
    void testToString() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        String expectedString = "Accesso{idAccesso=1, tipologia='CVC', posizione='Giugulare destra'}";
        assertEquals(expectedString, accesso.toString());
    }
}