package it.uniupo.simnova.service.scenario.operations;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.EsameFisicoService;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.utils.DBConnect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Service
public class ScenarioImportService {

    private final ScenarioService scenarioService;
    private final EsameFisicoService esameFisicoService;
    private final PazienteT0Service pazienteT0Service;
    private final EsameRefertoService esameRefertoService;
    private final AdvancedScenarioService advancedScenarioService;
    private static final Logger logger = LoggerFactory.getLogger(ScenarioImportService.class);

    public ScenarioImportService(ScenarioService scenarioService, EsameFisicoService esameFisicoService,
                                  PazienteT0Service pazienteT0Service, EsameRefertoService esameRefertoService,
                                  AdvancedScenarioService advancedScenarioService) {
        this.scenarioService = scenarioService;
        this.esameFisicoService = esameFisicoService;
        this.pazienteT0Service = pazienteT0Service;
        this.esameRefertoService = esameRefertoService;
        this.advancedScenarioService = advancedScenarioService;
    }

    public boolean createScenarioByJSON(byte[] jsonFile) {
        try {
            // Converti il JSON in stringa
            String jsonString = new String(jsonFile, StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().create();

            // Parsa il JSON in un oggetto Map per estrarre i dati
            Map<String, Object> jsonData = gson.fromJson(jsonString, new TypeToken<Map<String, Object>>() {
            }.getType());

            // Estrai i dati principali dello scenario
            Map<String, Object> scenarioData = (Map<String, Object>) jsonData.get("scenario");
            int scenarioId = ((Double) scenarioData.get("id")).intValue();

            // Verifica se lo scenario esiste già
            if (scenarioService.existScenario(scenarioId)) {
                logger.warn("Lo scenario con ID {} esiste già nel database", scenarioId);
                return false;
            }

            // Crea lo scenario in base al tipo
            String scenarioType = (String) jsonData.get("type");
            boolean creationResult;

            switch (scenarioType) {
                case "Quick Scenario":
                    creationResult = createQuickScenarioFromJson(scenarioData);
                    break;
                case "Advanced Scenario":
                    creationResult = createAdvancedScenarioFromJson(jsonData);
                    break;
                case "Patient Simulated Scenario":
                    creationResult = createPatientSimulatedScenarioFromJson(jsonData);
                    break;
                default:
                    logger.error("Tipo di scenario non riconosciuto: {}", scenarioType);
                    return false;
            }

            if (!creationResult) {
                logger.error("Errore durante la creazione dello scenario di tipo {}", scenarioType);
                return false;
            }

            logger.info("Scenario creato con successo dal JSON con ID {}", scenarioId);
            return true;

        } catch (JsonSyntaxException e) {
            logger.error("Errore di sintassi nel JSON", e);
            return false;
        } catch (Exception e) {
            logger.error("Errore durante la creazione dello scenario dal JSON", e);
            return false;
        }
    }

    private boolean createQuickScenarioFromJson(Map<String, Object> scenarioData) {
        String titolo = (String) scenarioData.get("titolo");
        String nomePaziente = (String) scenarioData.get("nome_paziente");
        String patologia = (String) scenarioData.get("patologia");
        String autori = (String) scenarioData.get("autori");
        double timerGenerale = (Double) scenarioData.get("timer_generale");
        String tipologia = (String) scenarioData.get("tipologia_paziente");

        int newId = scenarioService.startQuickScenario(-1, titolo, nomePaziente, patologia, autori, (float) timerGenerale, tipologia);
        if (newId <= 0) return false;

        // Aggiorna i campi aggiuntivi
        Scenario scenario = new Scenario(
                newId,
                titolo,
                nomePaziente,
                patologia,
                (String) scenarioData.get("descrizione"),
                (String) scenarioData.get("briefing"),
                (String) scenarioData.get("patto_aula"),
                (String) scenarioData.get("obiettivo"),
                (String) scenarioData.get("moulage"),
                (String) scenarioData.get("liquidi"),
                (float) timerGenerale,
                (String) scenarioData.get("autori"),
                (String) scenarioData.get("tipologia_paziente"),
                (String) scenarioData.get("infoGenitore"),
                (String) scenarioData.get("target")
        );

        scenarioService.update(scenario);
        return true;
    }

    /**
     * Crea uno scenario avanzato a partire dai dati JSON.
     *
     * @param jsonData i dati JSON contenenti le informazioni dello scenario
     * @return true se lo scenario è stato creato con successo, false altrimenti
     */
    private boolean createAdvancedScenarioFromJson(Map<String, Object> jsonData) {
        Map<String, Object> scenarioData = (Map<String, Object>) jsonData.get("scenario");

        // Crea lo scenario base
        if (!createQuickScenarioFromJson(scenarioData)) {
            return false;
        }

        int scenarioId = ((Double) scenarioData.get("id")).intValue();

        // Aggiungi alla tabella AdvancedScenario
        final String sql = "INSERT INTO AdvancedScenario (id_advanced_scenario) VALUES (?)";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Errore durante l'inserimento in AdvancedScenario", e);
            return false;
        }

        // Salva i componenti aggiuntivi
        return saveAdvancedScenarioComponents(scenarioId, jsonData);
    }

    /**
     * Crea uno scenario simulato per pazienti a partire dai dati JSON.
     *
     * @param jsonData i dati JSON contenenti le informazioni dello scenario
     * @return true se lo scenario è stato creato con successo, false altrimenti
     */
    private boolean createPatientSimulatedScenarioFromJson(Map<String, Object> jsonData) {
        Map<String, Object> scenarioData = (Map<String, Object>) jsonData.get("scenario");

        // Crea lo scenario avanzato
        if (!createAdvancedScenarioFromJson(jsonData)) {
            return false;
        }

        int scenarioId = ((Double) scenarioData.get("id")).intValue();
        String sceneggiatura = (String) jsonData.get("sceneggiatura");

        // Aggiungi alla tabella PatientSimulatedScenario
        final String sql = "INSERT INTO PatientSimulatedScenario (id_patient_simulated_scenario, id_advanced_scenario, sceneggiatura) VALUES (?, ?, ?)";
        try (Connection conn = DBConnect.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scenarioId);
            stmt.setInt(2, scenarioId);
            stmt.setString(3, sceneggiatura != null ? sceneggiatura : "");
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Errore durante l'inserimento in PatientSimulatedScenario", e);
            return false;
        }
    }

    /**
     * Salva i componenti avanzati dello scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @param jsonData   i dati JSON contenenti le informazioni dello scenario
     * @return true se il salvataggio è andato a buon fine, false altrimenti
     */
    private boolean saveAdvancedScenarioComponents(int scenarioId, Map<String, Object> jsonData) {
        // Salva esame fisico
        Map<String, Object> esameFisicoData = (Map<String, Object>) jsonData.get("esameFisico");
        if (esameFisicoData != null) {
            Map<String, String> sections = new HashMap<>();
            Map<String, String> sectionsData = (Map<String, String>) esameFisicoData.get("sections");
            if (sectionsData != null) {
                sections.putAll(sectionsData);
            }
            if (!esameFisicoService.addEsameFisico(scenarioId, sections)) {
                logger.warn("Errore durante il salvataggio dell'esame fisico");
            }
        }

        // Salva paziente T0
        Map<String, Object> pazienteT0Data = (Map<String, Object>) jsonData.get("pazienteT0");
        if (pazienteT0Data != null) {
            List<Map<String, Object>> venosiData = (List<Map<String, Object>>) pazienteT0Data.get("accessiVenosi");
            List<Map<String, Object>> arteriosiData = (List<Map<String, Object>>) pazienteT0Data.get("accessiArteriosi");

            List<Accesso> venosi = convertAccessoData(venosiData);
            List<Accesso> arteriosi = convertAccessoData(arteriosiData);

            if (!pazienteT0Service.savePazienteT0(
                    scenarioId,
                    (String) pazienteT0Data.get("PA"),
                    ((Double) pazienteT0Data.get("FC")).intValue(),
                    ((Double) pazienteT0Data.get("RR")).intValue(),
                    (Double) pazienteT0Data.get("T"),
                    ((Double) pazienteT0Data.get("SpO2")).intValue(),
                    ((Double) pazienteT0Data.get("FiO2")).intValue(),
                    ((Double) pazienteT0Data.get("LitriOssigeno")).floatValue(),
                    ((Double) pazienteT0Data.get("EtCO2")).intValue(),
                    (String) pazienteT0Data.get("Monitor"),
                    venosi,
                    arteriosi
            )) {
                logger.warn("Errore durante il salvataggio del paziente T0");
            }
        }

        // Salva esami e referti
        List<Map<String, Object>> esamiRefertiData = (List<Map<String, Object>>) jsonData.get("esamiReferti");
        if (esamiRefertiData != null) {
            List<EsameReferto> esami = esamiRefertiData.stream()
                    .map(e -> new EsameReferto(
                            ((Double) e.get("idEsameReferto")).intValue(),
                            ((Double) e.get("idEsame")).intValue(),
                            (String) e.get("tipo"),
                            (String) e.get("media"),
                            (String) e.get("refertoTestuale")
                    ))
                    .collect(Collectors.toList());

            if (!esameRefertoService.saveEsamiReferti(scenarioId, esami)) {
                logger.warn("Errore durante il salvataggio degli esami e referti");
            }
        }

        // Salva tempi (solo per Advanced Scenario)
        List<Map<String, Object>> tempiData = (List<Map<String, Object>>) jsonData.get("tempi");
        if (tempiData != null) {
            List<Tempo> tempi = tempiData.stream()
                    .map(t -> {
                        List<Map<String, Object>> paramsData = (List<Map<String, Object>>) t.get("parametriAggiuntivi");
                        List<ParametroAggiuntivo> params = new ArrayList<>();

                        if (paramsData != null) {
                            paramsData.forEach(p -> {
                                String nome = (String) p.get("nome");
                                double valore = p.get("valore") instanceof String ?
                                        Double.parseDouble((String) p.get("valore")) :
                                        (Double) p.get("valore");
                                String unita = (String) p.get("unitaMisura");
                                params.add(new ParametroAggiuntivo(nome, valore, unita));
                            });
                        }

                        Tempo tempo = new Tempo(
                                ((Double) t.get("idTempo")).intValue(),
                                scenarioId,
                                (String) t.get("PA"),
                                t.get("FC") != null ? ((Double) t.get("FC")).intValue() : null,
                                t.get("RR") != null ? ((Double) t.get("RR")).intValue() : null,
                                (Double) t.get("T"),
                                t.get("SpO2") != null ? ((Double) t.get("SpO2")).intValue() : null,
                                t.get("FiO2") != null ? ((Double) t.get("FiO2")).intValue() : null,
                                t.get("LitriO2") != null ? ((Double) t.get("LitriO2")).floatValue() : null,
                                t.get("EtCO2") != null ? ((Double) t.get("EtCO2")).intValue() : null,
                                (String) t.get("Azione"),
                                t.get("TSi") != null ? ((Double) t.get("TSi")).intValue() : 0,
                                t.get("TNo") != null ? ((Double) t.get("TNo")).intValue() : 0,
                                (String) t.get("altriDettagli"),
                                ((Double) t.get("timerTempo")).longValue(),
                                (String) t.get("ruoloGenitore")
                        );
                        tempo.setParametriAggiuntivi(params);
                        return tempo;
                    })
                    .collect(Collectors.toList());

            if (!advancedScenarioService.saveTempi(scenarioId, tempi)) {
                logger.warn("Errore durante il salvataggio dei tempi");
            }
        }

        return true;
    }

    /**
     * Converte i dati degli accessi in oggetti Accesso.
     *
     * @param accessiData la lista di mappe contenente i dati degli accessi
     * @return una lista di oggetti Accesso
     */
    private List<Accesso> convertAccessoData(List<Map<String, Object>> accessiData) {
        if (accessiData == null) return new ArrayList<>();

        return accessiData.stream()
                .map(a -> new Accesso(
                        ((Double) a.get("idAccesso")).intValue(),
                        (String) a.get("tipologia"),
                        (String) a.get("posizione"),
                        (String) a.get("lato"),
                        (Integer) a.get("misura")
                ))
                .collect(Collectors.toList());
    }

}
