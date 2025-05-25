package it.uniupo.simnova.service.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.*;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Servizio che si occupa dell'esportazione degli scenari in formato JSON.
 * Utilizza Gson per serializzare oggetti complessi relativi allo scenario e ai suoi componenti.
 * Raccoglie tutte le informazioni necessarie tramite i vari servizi di dominio.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class JSONExportService implements Serializable {
    /**
     * Istanza Gson per la serializzazione degli oggetti in JSON.
     */
    private static Gson gson = null;
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per la gestione degli esami e referti.
     */
    private final EsameRefertoService esameRefertoService;
    /**
     * Servizio per i dati del paziente T0.
     */
    private final PazienteT0Service pazienteT0Service;
    /**
     * Servizio per la gestione degli scenari avanzati.
     */
    private final AdvancedScenarioService advancedScenarioService;
    /**
     * Servizio per la gestione degli scenari simulati con paziente.
     */
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    /**
     * Servizio per la gestione degli esami fisici.
     */
    private final EsameFisicoService esameFisicoService;
    /**
     * Servizio per la gestione dei materiali necessari.
     */
    private final MaterialeService materialeService;
    /**
     * Servizio per la gestione delle azioni chiave.
     */
    private final AzioneChiaveService azioneChiaveService;

    /**
     * Costruttore che inizializza tutti i servizi necessari e configura Gson.
     *
     * @param scenarioService                 Servizio per la gestione degli scenari.
     * @param esameRefertoService             Servizio per la gestione degli esami e referti.
     * @param pazienteT0Service               Servizio per i dati del paziente T0.
     * @param advancedScenarioService         Servizio per scenari avanzati.
     * @param patientSimulatedScenarioService Servizio per scenari simulati con paziente.
     * @param esameFisicoService              Servizio per l'esame fisico.
     * @param materialeService                Servizio per i materiali necessari.
     * @param azioneChiaveService             Servizio per le azioni chiave.
     */
    public JSONExportService(ScenarioService scenarioService, EsameRefertoService esameRefertoService,
                             PazienteT0Service pazienteT0Service, AdvancedScenarioService advancedScenarioService,
                             PatientSimulatedScenarioService patientSimulatedScenarioService, EsameFisicoService esameFisicoService, MaterialeService materialeService, AzioneChiaveService azioneChiaveService) {
        this.scenarioService = scenarioService;
        this.esameRefertoService = esameRefertoService;
        this.pazienteT0Service = pazienteT0Service;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        this.esameFisicoService = esameFisicoService;
        this.materialeService = materialeService;
        this.azioneChiaveService = azioneChiaveService;
    }

    /**
     * Esporta tutti i dati relativi a uno scenario in formato JSON.
     * Raccoglie le informazioni principali e i dati collegati tramite i servizi di dominio.
     * La struttura risultante include scenario, tipo, esami, paziente, materiali, esame fisico, azioni chiave, presidi e dati specifici per tipo di scenario.
     *
     * @param scenarioId Identificativo dello scenario da esportare.
     * @return Array di byte contenente il JSON serializzato.
     */
    public byte[] exportScenarioToJSON(Integer scenarioId) {

        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        String scenarioType = scenarioService.getScenarioType(scenarioId);


        Map<String, Object> exportData = new HashMap<>();
        exportData.put("scenario", scenario);
        exportData.put("tipo", scenarioType);


        var esamiReferti = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);
        exportData.put("esamiReferti", esamiReferti);


        var pazienteT0 = pazienteT0Service.getPazienteT0ById(scenarioId);
        exportData.put("pazienteT0", pazienteT0);


        var materialeNecessario = materialeService.getMaterialiByScenarioId(scenarioId);
        exportData.put("materialeNecessario", materialeNecessario);


        var esameFisico = esameFisicoService.getEsameFisicoById(scenarioId);
        exportData.put("esameFisico", esameFisico);


        var azioniChiave = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);
        exportData.put("azioniChiave", azioniChiave);


        var presidi = PresidiService.getPresidiByScenarioId(scenarioId);
        exportData.put("presidi", presidi);


        if (scenarioType.equals("Advanced Scenario") || scenarioType.equals("Patient Simulated Scenario")) {
            var tempi = advancedScenarioService.getTempiByScenarioId(scenarioId);
            exportData.put("tempi", tempi);
        }

        if (scenarioType.equals("Patient Simulated Scenario")) {
            var sceneggiatura = patientSimulatedScenarioService.getSceneggiatura(scenarioId);
            exportData.put("sceneggiatura", sceneggiatura);
        }


        String json = gson.toJson(exportData);


        return json.getBytes(StandardCharsets.UTF_8);
    }
}
