package it.uniupo.simnova.service.export.helper.pdf;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.components.AzioneChiaveService;
import it.uniupo.simnova.service.scenario.components.MaterialeService;
import it.uniupo.simnova.service.scenario.ScenarioService;

import java.io.IOException;

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
        if (azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenario.getId()) != null && !azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenario.getId()).isEmpty() && azioni) {
            drawSection("Azioni Chiave", azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenario.getId()).toString());
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
        if (materialeService.toStringAllMaterialsByScenarioId(scenario.getId()) != null && !materialeService.toStringAllMaterialsByScenarioId(scenario.getId()).isEmpty() && matNec) {
            drawSection("Materiale necessario", materialeService.toStringAllMaterialsByScenarioId(scenario.getId()));
        }

    }
}
