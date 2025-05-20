package it.uniupo.simnova.views.constant;

import java.util.List;

/**
 * Classe di supporto contenente le costanti per gli esami.
 */
public class ExamConst {
    /**
     * Tutti i tipi di esami di laboratorio
     */
    public static final List<String> ALLLABSEXAMS = List.of(
            "Emocromo con formula", "Glicemia", "Elettroliti sierici (Na⁺, K⁺, Cl⁻, Ca²⁺, Mg²⁺)",
            "Funzionalità renale (Creatinina, Azotemia)", "Funzionalità epatica (AST, ALT, Bilirubina, ALP, GGT)",
            "PCR (Proteina C Reattiva)", "Procalcitonina", "D-Dimero", "CK-MB, Troponina I/T",
            "INR, PTT, PT", "Gas arteriosi (pH, PaO₂, PaCO₂, HCO₃⁻, BE, Lactati)",
            "Emogas venoso", "Osmolarità sierica", "CPK", "Mioglobina"
    );

    /**
     * Tutti i tipi di esami strumentali
     */
    public static final List<String> ALLINSTREXAMS = List.of(
            "ECG (Elettrocardiogramma)", "RX Torace", "TC Torace (con mdc)", "TC Torace (senza mdc)",
            "TC Addome (con mdc)", "TC Addome (senza mdc)", "Ecografia addominale", "Ecografia polmonare",
            "Ecocardio (Transtoracico)", "Ecocardio (Transesofageo)", "Spirometria", "EEG (Elettroencefalogramma)",
            "RM Encefalo", "TC Cranio (con mdc)", "TC Cranio (senza mdc)", "Doppler TSA (Tronchi Sovraortici)",
            "Angio-TC Polmonare", "Fundus oculi"
    );
}
