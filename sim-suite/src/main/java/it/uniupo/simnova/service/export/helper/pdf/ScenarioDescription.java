package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.common.Materiale;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;
import it.uniupo.simnova.service.scenario.ScenarioService;

import java.io.IOException;
import java.util.List;

import static it.uniupo.simnova.service.export.helper.pdf.SectionDrawer.drawSection;

public class ScenarioDescription {

    public static void createScenarioDescription(Scenario scenario,
                                                 boolean desc,
                                                 boolean brief,
                                                 boolean infoGen,
                                                 boolean patto,
                                                 boolean azioni,
                                                 boolean obiettivi,
                                                 boolean moula,
                                                 boolean liqui,
                                                 boolean matNec,
                                                 ScenarioService scenarioService,
                                                 MaterialeService materialeService,
                                                 AzioneChiaveService azioneChiaveService) throws IOException {
        // Descrizione
        if (scenario.getDescrizione() != null && !scenario.getDescrizione().isEmpty() && desc) {
            drawSection("Descrizione", scenario.getDescrizione());
        }

        // Briefing
        if (scenario.getBriefing() != null && !scenario.getBriefing().isEmpty() && brief) {
            drawSection("Briefing", scenario.getBriefing());
        }

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
