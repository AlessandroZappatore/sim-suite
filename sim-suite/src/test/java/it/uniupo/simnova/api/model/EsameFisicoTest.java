package it.uniupo.simnova.api.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EsameFisicoTest {
    EsameFisico esameFisico;

    @Test
    void getIdEsameFisico() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        assertEquals(1, esameFisico.getIdEsameFisico());
    }

    @Test
    void setIdEsameFisico() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        esameFisico.setIdEsameFisico(2);
        assertEquals(2, esameFisico.getIdEsameFisico());
    }

    @Test
    void getSections() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        assertNotNull(esameFisico.getSections());
        assertEquals(11, esameFisico.getSections().size());
    }

    @Test
    void setSections() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        esameFisico.setSections(Map.of("Generale", "Nuovo Generale"));
        assertEquals("Nuovo Generale", esameFisico.getSections().get("Generale"));
    }

    @Test
    void getSection() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        assertEquals("Generale", esameFisico.getSection("Generale"));
        assertNull(esameFisico.getSection("NonEsistente"));
    }

    @Test
    void setSection() {
        esameFisico = new EsameFisico(1, "Generale", "Pupille", "Collo", "Torace", "Cuore", "Addome", "Retto", "Cute", "Estremità", "Neurologico", "FAST");
        esameFisico.setSection("Generale", "Nuovo Generale");
        assertEquals("Nuovo Generale", esameFisico.getSection("Generale"));
    }
}