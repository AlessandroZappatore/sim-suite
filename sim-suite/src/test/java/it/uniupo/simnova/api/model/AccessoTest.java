package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per la classe {@link Accesso}.
 *
 * @version 1.0
 * @author Alessandro Zappatore
 */
class AccessoTest {
    /**
     * Accesso da testare.
     */
    Accesso accesso;

    /**
     * Test getId() per verificare che l'ID dell'accesso venga restituito correttamente.
     */
    @Test
    void getId() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        assertEquals(1, accesso.getId());
    }

    /**
     * Test getTipologia() per verificare che la tipologia dell'accesso venga restituita correttamente.
     */
    @Test
    void getTipologia() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        assertEquals("CVC", accesso.getTipologia());
    }

    /**
     * Test getPosizione() per verificare che la posizione dell'accesso venga restituita correttamente.
     */
    @Test
    void getPosizione() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        assertEquals("Giugulare destra", accesso.getPosizione());
    }

    /**
     * Test setId() per verificare che l'ID dell'accesso venga impostato correttamente.
     */
    @Test
    void setId() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        accesso.setId(2);
        assertEquals(2, accesso.getId());
    }

    /**
     * Test setTipologia() per verificare che la tipologia dell'accesso venga impostata correttamente.
     */
    @Test
    void setTipologia() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        accesso.setTipologia("Agocannula");
        assertEquals("Agocannula", accesso.getTipologia());
    }

    /**
     * Test setPosizione() per verificare che la posizione dell'accesso venga impostata correttamente.
     */
    @Test
    void setPosizione() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        accesso.setPosizione("Cubitale sinistro");
        assertEquals("Cubitale sinistro", accesso.getPosizione());
    }

    /**
     * Test toString() per verificare che la rappresentazione in stringa dell'accesso sia corretta.
     */
    @Test
    void testToString() {
        accesso = new Accesso(1, "CVC", "Giugulare destra");
        String expectedString = "Accesso{idAccesso=1, tipologia='CVC', posizione='Giugulare destra'}";
        assertEquals(expectedString, accesso.toString());
    }
}