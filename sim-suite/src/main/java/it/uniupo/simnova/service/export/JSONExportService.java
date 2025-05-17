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
 * Servizio per l'esportazione di scenari in formato JSON.
 * Utilizza Gson per la serializzazione degli oggetti in JSON.
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@Service
public class JSONExportService implements Serializable {
    /**
     * Oggetto Gson per la serializzazione in JSON.
     */
    private static Gson gson = null;
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    private final EsameRefertoService esameRefertoService;
    private final PazienteT0Service pazienteT0Service;
    private final AdvancedScenarioService advancedScenarioService;
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    private final EsameFisicoService esameFisicoService;
    private final MaterialeService materialeService;
    private final AzioneChiaveService azioneChiaveService;

    /**
     * Costruttore del servizio JSONExportService.
     * Inizializza il servizio ScenarioService e Gson.
     *
     * @param scenarioService Il servizio ScenarioService da utilizzare.
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
     * Esporta uno scenario in formato JSON.
     *
     * @param scenarioId L'ID dello scenario da esportare.
     * @return I dati dello scenario in formato JSON come array di byte.
     */
    public byte[] exportScenarioToJSON(Integer scenarioId) {
        // Recupera lo scenario e il suo tipo
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        String scenarioType = scenarioService.getScenarioType(scenarioId);

        // Crea un oggetto contenente sia lo scenario che il tipo
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("scenario", scenario);
        exportData.put("tipo", scenarioType);

        // Recupera gli esami dello scenario
        var esamiReferti = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);
        exportData.put("esamiReferti", esamiReferti);

        // Recupera i dati del paziente in T0 dello scenario
        var pazienteT0 = pazienteT0Service.getPazienteT0ById(scenarioId);
        exportData.put("pazienteT0", pazienteT0);

        var materialeNecessario = materialeService.getMaterialiByScenarioId(scenarioId);
        exportData.put("materialeNecessario", materialeNecessario);
        // Recupera l'esame fisico dello scenario
        var esameFisico = esameFisicoService.getEsameFisicoById(scenarioId);
        exportData.put("esameFisico", esameFisico);

        var azioniChiave = azioneChiaveService.getNomiAzioniChiaveByScenarioId(scenarioId);
        exportData.put("azioniChiave", azioniChiave);

        var presidi = PresidiService.getPresidiByScenarioId(scenarioId);
        exportData.put("presidi", presidi);
        // Controlla il tipo di scenario e recupera i dati specifici
        if (scenarioType.equals("Advanced Scenario") || scenarioType.equals("Patient Simulated Scenario")) {
            var tempi = advancedScenarioService.getTempiByScenarioId(scenarioId);
            exportData.put("tempi", tempi);
        }
        if (scenarioType.equals("Patient Simulated Scenario")) {
            var sceneggiatura = patientSimulatedScenarioService.getSceneggiatura(scenarioId);
            exportData.put("sceneggiatura", sceneggiatura);
        }

        // Converte in JSON
        String json = gson.toJson(exportData);

        return json.getBytes(StandardCharsets.UTF_8);
    }
}