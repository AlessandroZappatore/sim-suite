package it.uniupo.simnova.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.uniupo.simnova.api.model.Scenario;
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
     * Servizio per la gestione degli scenari.
     */
    private static ScenarioService scenarioService = null;
    /**
     * Oggetto Gson per la serializzazione in JSON.
     */
    private static Gson gson = null;

    /**
     * Costruttore del servizio JSONExportService.
     * Inizializza il servizio ScenarioService e Gson.
     *
     * @param scenarioService Il servizio ScenarioService da utilizzare.
     */
    public JSONExportService(ScenarioService scenarioService) {
        JSONExportService.scenarioService = scenarioService;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    /**
     * Esporta uno scenario in formato JSON.
     *
     * @param scenarioId L'ID dello scenario da esportare.
     * @return I dati dello scenario in formato JSON come array di byte.
     */
    public static byte[] exportScenarioToJSON(Integer scenarioId) {
        // Recupera lo scenario e il suo tipo
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        String scenarioType = scenarioService.getScenarioType(scenarioId);

        // Crea un oggetto contenente sia lo scenario che il tipo
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("scenario", scenario);
        exportData.put("type", scenarioType);

        // Recupera gli esami dello scenario
        var esamiReferti = scenarioService.getEsamiRefertiByScenarioId(scenarioId);
        exportData.put("esamiReferti", esamiReferti);

        // Recupera i dati del paziente in T0 dello scenario
        var pazienteT0 = scenarioService.getPazienteT0ById(scenarioId);
        exportData.put("pazienteT0", pazienteT0);

        // Recupera l'esame fisico dello scenario
        var esameFisico = scenarioService.getEsameFisicoById(scenarioId);
        exportData.put("esameFisico", esameFisico);

        // Controlla il tipo di scenario e recupera i dati specifici
        if (scenarioType.equals("Advanced Scenario")) {
            var tempi = ScenarioService.getTempiByScenarioId(scenarioId);
            exportData.put("tempi", tempi);
        } else if (scenarioType.equals("Patient Simulated Scenario")) {
            var sceneggiatura = ScenarioService.getSceneggiatura(scenarioId);
            exportData.put("sceneggiatura", sceneggiatura);
        }

        // Converte in JSON
        String json = gson.toJson(exportData);

        return json.getBytes(StandardCharsets.UTF_8);
    }
}