package it.uniupo.simnova.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.uniupo.simnova.api.model.Scenario;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class JSONExportService implements Serializable {
    private static ScenarioService scenarioService = null;
    private static Gson gson = null;

    public JSONExportService(ScenarioService scenarioService) {
        JSONExportService.scenarioService = scenarioService;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    public static byte[] exportScenarioToJSON(Integer scenarioId) {
        // Recupera lo scenario e il suo tipo
        Scenario scenario = scenarioService.getScenarioById(scenarioId);
        String scenarioType = scenarioService.getScenarioType(scenarioId);

        // Crea un oggetto contenente sia lo scenario che il tipo
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("scenario", scenario);
        exportData.put("type", scenarioType);

        var esamiReferti = scenarioService.getEsamiRefertiByScenarioId(scenarioId);
        exportData.put("esamiReferti", esamiReferti);

        var pazienteT0 = scenarioService.getPazienteT0ById(scenarioId);
        exportData.put("pazienteT0", pazienteT0);

        var esameFisico = scenarioService.getEsameFisicoById(scenarioId);
        exportData.put("esameFisico", esameFisico);

        if (scenarioType.equals("Advanced Scenario")) {
            var tempi = ScenarioService.getTempiByScenarioId(scenarioId);
            exportData.put("tempi", tempi);
        } else if (scenarioType.equals("Patient Simulated Scenario")) {
            var sceneggiatura = ScenarioService.getSceneggiatura(scenarioId);
            exportData.put("sceneggiatura", sceneggiatura);
        }

        // Converti in JSON
        String json = gson.toJson(exportData);

        return json.getBytes(StandardCharsets.UTF_8);
    }
}