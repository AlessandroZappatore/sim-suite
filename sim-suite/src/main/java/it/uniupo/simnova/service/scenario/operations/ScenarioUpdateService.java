package it.uniupo.simnova.service.scenario.operations;

import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.paziente.EsameFisico;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ScenarioUpdateService {
    private final ScenarioService scenarioService;
    private final EsameFisicoService esameFisicoService;
    private final PazienteT0Service pazienteT0Service;

    public ScenarioUpdateService(ScenarioService scenarioService, EsameFisicoService esameFisicoService, PazienteT0Service pazienteT0Service) {
        this.scenarioService = scenarioService;
        this.esameFisicoService = esameFisicoService;
        this.pazienteT0Service = pazienteT0Service;
    }

    public void updateScenario(Integer idScenario, Map<String, String> updatedFields, Map<String, String> updatedSections, EsameFisico esameFisico, Map<String, String> pazienteT0, List<Accesso> accessiVenosi, List<Accesso> accessiArteriosi) {
        // Recupera lo scenario esistente per ottenere gli autori attuali
        Scenario esistente = scenarioService.getScenarioById(idScenario);
        String autoriEsistenti = esistente != null ? esistente.getAutori() : "";

        // Aggiungi il nuovo autore a quelli esistenti
        String nuovoAutore = updatedFields.get("Autore");
        String autoriCombinati;

        if (autoriEsistenti == null || autoriEsistenti.isEmpty()) {
            autoriCombinati = nuovoAutore;
        } else if (nuovoAutore == null || nuovoAutore.isEmpty()) {
            autoriCombinati = autoriEsistenti;
        } else {
            autoriCombinati = autoriEsistenti + ", " + nuovoAutore;
        }

        // Chiama startQuickScenario con gli autori combinati
        scenarioService.startQuickScenario(
                idScenario,
                updatedFields.get("Titolo"),
                updatedFields.get("NomePaziente"),
                updatedFields.get("Patologia"),
                autoriCombinati,
                Float.parseFloat(updatedFields.get("Timer")),
                updatedFields.get("TipologiaPaziente")
        );

        scenarioService.updateScenarioDescription(idScenario, updatedSections.get("Descrizione"));
        scenarioService.updateScenarioBriefing(idScenario, updatedSections.get("Briefing"));
        scenarioService.updateScenarioPattoAula(idScenario, updatedSections.get("PattoAula"));
        if (scenarioService.isPediatric(idScenario)) {
            scenarioService.updateScenarioGenitoriInfo(idScenario, updatedSections.get("InfoGenitore"));
        }
        scenarioService.updateScenarioLiquidi(idScenario, updatedSections.get("Liquidi"));
        scenarioService.updateScenarioMoulage(idScenario, updatedSections.get("Moulage"));
        scenarioService.updateScenarioObiettiviDidattici(idScenario, updatedSections.get("Obiettivi"));

        esameFisicoService.addEsameFisico(idScenario, esameFisico.getSections());

        pazienteT0Service.savePazienteT0(idScenario,
                pazienteT0.get("PA"),
                Integer.parseInt(pazienteT0.get("FC")),
                Integer.parseInt(pazienteT0.get("RR")),
                Float.parseFloat(pazienteT0.get("Temperatura")),
                Integer.parseInt(pazienteT0.get("SpO2")),
                Integer.parseInt(pazienteT0.get("FiO2")),
                Float.parseFloat(pazienteT0.get("LitriO2")),
                Integer.parseInt(pazienteT0.get("EtCO2")),
                pazienteT0.get("Monitoraggio"),
                accessiVenosi,
                accessiArteriosi
        );
    }

}
