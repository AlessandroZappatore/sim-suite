package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per la classe {@link EsameFisico}.
 */
class EsameFisicoTest {
    /**
     * Oggetto EsameFisico da testare.
     */
    EsameFisico esameFisico;

    /**
     * Test getId() per verificare che l'ID dell'esame fisico venga restituito correttamente.
     */
    @Test
    void getIdEsameFisico() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        assertEquals(1, esameFisico.getIdEsameFisico());
    }

    /**
     * Test setId() per verificare che l'ID dell'esame fisico venga impostato correttamente.
     */
    @Test
    void setIdEsameFisico() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        esameFisico.setIdEsameFisico(2);
        assertEquals(2, esameFisico.getIdEsameFisico());
    }

    /**
     * Test getSections() per verificare che le sezioni dell'esame fisico vengano restituite correttamente.
     */
    @Test
    void getSections() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        assertNotNull(esameFisico.getSections());
        assertEquals(11, esameFisico.getSections().size());
    }

    /**
     * Test setSections() per verificare che le sezioni dell'esame fisico vengano impostate correttamente.
     */
    @Test
    void setSections() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        esameFisico.setSections(Map.of("Generale", "Nuovo Generale"));
        assertEquals("Nuovo Generale", esameFisico.getSections().get("Generale"));
    }

    /**
     * Test getSection() per verificare che una sezione specifica dell'esame fisico venga restituita correttamente.
     */
    @Test
    void getSection() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        assertEquals("Generale", esameFisico.getSection("Generale"));
        assertNull(esameFisico.getSection("NonEsistente"));
    }

    /**
     * Test setSection() per verificare che una sezione specifica dell'esame fisico venga impostata correttamente.
     */
    @Test
    void setSection() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        esameFisico.setSection("Generale", "Nuovo Generale");
        assertEquals("Nuovo Generale", esameFisico.getSection("Generale"));
    }
}