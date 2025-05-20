package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.common.Materiale;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;
import it.uniupo.simnova.service.scenario.ScenarioService;

import java.io.IOException;
import java.util.List;

import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSection;

/**
 * Classe per la creazione delle varie sezioni della descrizione dello scenario.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
public class ScenarioDescription {
    /**
     * Crea la descrizione dello scenario.
     *
     * @param scenario            Lo scenario contenente tutte le informazioni.
     * @param desc                true se la descrizione deve essere stampata, false altrimenti.
     * @param brief               true se il briefing deve essere stampato, false altrimenti.
     * @param infoGen             true se le informazioni dai genitori devono essere stampate, false altrimenti.
     * @param patto               true se il patto d'aula deve essere stampato, false altrimenti.
     * @param azioni              true se le azioni chiave devono essere stampate, false altrimenti.
     * @param obiettivi           true se gli obiettivi didattici devono essere stampati, false altrimenti.
     * @param moula               true se il moulage deve essere stampato, false altrimenti.
     * @param liqui               true se i liquidi devono essere stampati, false altrimenti.
     * @param matNec              true se i materiali necessari devono essere stampati, false altrimenti.
     * @param scenarioService     Servizio per la gestione degli scenari.
     * @param materialeService    Servizio per la gestione dei materiali necessari.
     * @param azioneChiaveService Servizio per la gestione delle azioni chiave.
     * @throws IOException Eccezione sollevata in caso di errore durante la scrittura del file PDF.
     */
    public static void createScenarioDescription(Scenario scenario, boolean desc, boolean brief, boolean infoGen, boolean patto, boolean azioni, boolean obiettivi, boolean moula, boolean liqui, boolean matNec, ScenarioService scenarioService, MaterialeService materialeService, AzioneChiaveService azioneChiaveService) throws IOException {
        // Descrizione
        if (scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty() && desc) {
            drawSection("Descrizione", scenario.getDescrizione());
        }

        // Briefing
        if (scenario.getBriefing() != null && !scenario.getBriefing().isEmpty() && brief) {
            drawSection("Briefing", scenario.getBriefing());
        }

        // Informazioni dai genitori
        if (scenarioService.isPediatric(scenario.getId()) && scenario.getInfoGenitore() != null && !scenario.getInfoGenitore().isEmpty() && infoGen) {
            drawSection("Informazioni dai genitori", scenario.getInfoGenitore());
        }

        // Patto d'aula
        if (scenario.getPattoAula() != null && !scenario.getPattoAula().isEmpty() && patto) {
            drawSection("Patto d'Aula", scenario.getPattoAula());
        }

        // Azioni chiave
        List<String> nomiAzioniChiave = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenario.getId());
        if (nomiAzioniChiave != null && !nomiAzioniChiave.isEmpty() && azioni) {
            StringBuilder azioniFormattate = new StringBuilder();
            // Aggiunge un bullet point per ogni azione chiave
            for (String azione : nomiAzioniChiave) {
                azioniFormattate.append("• ").append(azione).append("\n");
            }
            // Rimuove l'ultimo carattere di nuova riga se la stringa non è vuota
            if (!azioniFormattate.isEmpty()) {
                azioniFormattate.setLength(azioniFormattate.length() - 1);
            }
            drawSection("Azioni Chiave", azioniFormattate.toString());
        }

        // Obiettivi didattici
        if (scenario.getObiettivo() != null && !scenario.getObiettivo().isEmpty() && obiettivi) {
            drawSection("Obiettivi Didattici", scenario.getObiettivo());
        }

        // Moulage
        if (scenario.getMoulage() != null && !scenario.getMoulage().isEmpty() && moula) {
            drawSection("Moulage", scenario.getMoulage());
        }

        // Liquidi
        if (scenario.getLiquidi() != null && !scenario.getLiquidi().isEmpty() && liqui) {
            drawSection("Liquidi e dosi farmaci", scenario.getLiquidi());
        }

        //Materiale necessario
        List<Materiale> materialiNecessari = materialeService.getMaterialiByScenarioId(scenario.getId());
        if (materialiNecessari != null && !materialiNecessari.isEmpty() && matNec) {
            StringBuilder materialiNecessariFormattati = new StringBuilder();
            // Aggiunge un bullet point per ogni materiale necessario
            for (Materiale materiale : materialiNecessari) {
                materialiNecessariFormattati.append("• ").append(materiale.getNome()).append(": ").append(materiale.getDescrizione()).append("\n");
            }
            // Rimuove l'ultimo carattere di nuova riga se la stringa non è vuota
            if (!materialiNecessariFormattati.isEmpty()) {
                materialiNecessariFormattati.setLength(materialiNecessariFormattati.length() - 1);
            }
            drawSection("Materiale necessario", materialiNecessariFormattati.toString());
        }
    }
}
