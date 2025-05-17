package it.uniupo.simnova.service.scenario.operations;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import it.uniupo.simnova.domain.common.Accesso;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.*;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.scenario.types.PatientSimulatedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Service
public class ScenarioImportService {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioImportService.class);
    private final ScenarioService scenarioService;
    private final EsameFisicoService esameFisicoService;
    private final PazienteT0Service pazienteT0Service;
    private final EsameRefertoService esameRefertoService;
    private final AdvancedScenarioService advancedScenarioService;
    private final PatientSimulatedScenarioService patientSimulatedScenarioService;
    private final MaterialeService materialeService;
    private final PresidiService presidiService;
    private final AzioneChiaveService azioneChiaveService;
    private final UnZipScenarioService unZipScenarioService;
    private final FileStorageService fileStorageService;

    public ScenarioImportService(ScenarioService scenarioService, EsameFisicoService esameFisicoService,
                                 PazienteT0Service pazienteT0Service, EsameRefertoService esameRefertoService,
                                 AdvancedScenarioService advancedScenarioService, PatientSimulatedScenarioService patientSimulatedScenarioService, MaterialeService materialeService, PresidiService presidiService, AzioneChiaveService azioneChiaveService, UnZipScenarioService unZipScenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;
        this.esameFisicoService = esameFisicoService;
        this.pazienteT0Service = pazienteT0Service;
        this.esameRefertoService = esameRefertoService;
        this.advancedScenarioService = advancedScenarioService;
        this.patientSimulatedScenarioService = patientSimulatedScenarioService;
        this.materialeService = materialeService;
        this.presidiService = presidiService;
        this.azioneChiaveService = azioneChiaveService;
        this.unZipScenarioService = unZipScenarioService;
        this.fileStorageService = fileStorageService;
    }

    public boolean createScenarioByJSON(byte[] jsonFile) {
        try {
            // Converti il JSON in stringa
            String jsonString = new String(jsonFile, StandardCharsets.UTF_8);
            Gson gson = new GsonBuilder().create();

            // Parsa il JSON in un oggetto Map per estrarre i dati
            Map<String, Object> jsonData = gson.fromJson(jsonString, new TypeToken<Map<String, Object>>() {
            }.getType());

            // Crea lo scenario in base al tipo
            String scenarioType = (String) jsonData.get("tipo");
            int creationResult;

            // Common data extraction
            Map<String, Object> scenarioData = (Map<String, Object>) jsonData.get("scenario");
            String titolo = (String) scenarioData.get("titolo");
            String nomePaziente = (String) scenarioData.get("nome_paziente");
            String patologia = (String) scenarioData.get("patologia");
            String autori = (String) scenarioData.get("autori");
            double timerGenerale = (Double) scenarioData.get("timer_generale");
            String tipologia = (String) scenarioData.get("tipologia");

            switch (scenarioType) {
                case "Quick Scenario":
                    creationResult = createQuickScenarioFromJson(jsonData, titolo, nomePaziente, patologia, autori, (float) timerGenerale, tipologia);
                    break;
                case "Advanced Scenario":
                    creationResult = advancedScenarioService.startAdvancedScenario(titolo, nomePaziente, patologia, autori, (float) timerGenerale, tipologia);
                    if (creationResult > 0) {
                        saveCommonScenarioComponents(creationResult, jsonData);
                        saveAdvancedScenarioComponents(creationResult, jsonData);
                    }
                    break;
                case "Patient Simulated Scenario":
                    creationResult = patientSimulatedScenarioService.startPatientSimulatedScenario(titolo, nomePaziente, patologia, autori, (float) timerGenerale, tipologia);
                    if (creationResult > 0) {
                        saveCommonScenarioComponents(creationResult, jsonData);
                        saveAdvancedScenarioComponents(creationResult, jsonData);
                        savePatientSimulatedScenarioComponents(creationResult, jsonData);
                    }
                    break;
                default:
                    logger.error("Tipo di scenario non riconosciuto: {}", scenarioType);
                    return false;
            }

            if (creationResult <= 0) {
                logger.error("Errore durante la creazione dello scenario di tipo {}", scenarioType);
                return false;
            }

            return true;

        } catch (JsonSyntaxException e) {
            logger.error("Errore di sintassi nel JSON", e);
            return false;
        } catch (Exception e) {
            logger.error("Errore durante la creazione dello scenario dal JSON", e);
            return false;
        }
    }

    /**
     * Importa uno scenario da un file ZIP.
     * Il file ZIP deve contenere un 'scenario.json' e una cartella 'media/' opzionale.
     *
     * @param zipBytes I byte del file ZIP.
     * @param fileName Il nome del file ZIP (attualmente non utilizzato ma potrebbe esserlo in futuro).
     * @return true se l'importazione ha successo, false altrimenti.
     */
    public boolean importScenarioFromZip(byte[] zipBytes, String fileName) {
        logger.info("Inizio importazione scenario da file ZIP: {}", fileName);
        try (InputStream zipInputStream = new ByteArrayInputStream(zipBytes)) {
            UnZipScenarioService.UnzippedScenarioData unzippedData = unZipScenarioService.unzipScenario(zipInputStream);

            byte[] scenarioJsonBytes = unzippedData.scenarioJson();
            if (scenarioJsonBytes == null || scenarioJsonBytes.length == 0) {
                logger.error("Il file 'scenario.json' è vuoto o mancante nello ZIP.");
                return false;
            }

            // Crea lo scenario utilizzando il JSON estratto
            boolean scenarioCreated = createScenarioByJSON(scenarioJsonBytes);

            if (scenarioCreated) {
                logger.info("Scenario creato con successo da 'scenario.json' contenuto in {}", fileName);

                if (!unzippedData.mediaFiles().isEmpty()) {
                    for(Map.Entry<String, byte[]> mediaFile : unzippedData.mediaFiles().entrySet()) {
                        String mediaFileName = mediaFile.getKey();
                        logger.info("File multimediale trovato: {}", mediaFileName);
                        byte[] mediaFileBytes = mediaFile.getValue();
                        InputStream mediaInputStream = new ByteArrayInputStream(mediaFileBytes);
                        fileStorageService.storeFile(mediaInputStream, mediaFileName);
                    }
                    logger.warn("La gestione dei file multimediali non è ancora implementata.");
                }
                return true;
            } else {
                logger.error("Errore durante la creazione dello scenario dal JSON estratto da {}", fileName);
                return false;
            }

        } catch (IOException e) {
            logger.error("Errore di I/O durante la decompressione del file ZIP: {}", fileName, e);
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("Errore nel contenuto del file ZIP: {} - {}", fileName, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Errore imprevisto durante l'importazione dello scenario da ZIP: {}", fileName, e);
            return false;
        }
    }

    private int createQuickScenarioFromJson(Map<String, Object> scenarioData, String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologia) {
        int newId = scenarioService.startQuickScenario(-1, titolo, nomePaziente, patologia, autori, timerGenerale, tipologia);
        if (newId <= 0) return -1;

        saveCommonScenarioComponents(newId, scenarioData);

        return newId;
    }

    private void saveCommonScenarioComponents(int scenarioId, Map<String, Object> scenarioData) {
        Map<String, Object> scenario = (Map<String, Object>) scenarioData.get("scenario");

        boolean targetResult = scenarioService.updateScenarioTarget(scenarioId, (String) scenario.get("target"));
        if (!targetResult)
            throw new RuntimeException("Errore durante il salvataggio del target");

        boolean descrizioneResult = scenarioService.updateScenarioDescription(scenarioId, (String) scenario.get("descrizione"));
        if (!descrizioneResult)
            throw new RuntimeException("Errore durante il salvataggio della descrizione");

        boolean briefingResult = scenarioService.updateScenarioBriefing(scenarioId, (String) scenario.get("briefing"));
        if (!briefingResult)
            throw new RuntimeException("Errore durante il salvataggio del briefing");

        boolean pattoResult = scenarioService.updateScenarioPattoAula(scenarioId, (String) scenario.get("patto_aula"));
        if (!pattoResult)
            throw new RuntimeException("Errore durante il salvataggio del patto aula");

        boolean obiettivoResult = scenarioService.updateScenarioObiettiviDidattici(scenarioId, (String) scenario.get("obiettivo"));
        if (!obiettivoResult)
            throw new RuntimeException("Errore durante il salvataggio dell'obiettivo didattico");

        boolean moulaResult = scenarioService.updateScenarioMoulage(scenarioId, (String) scenario.get("moulage"));
        if (!moulaResult)
            throw new RuntimeException("Errore durante il salvataggio del moulage");

        boolean liquidiResult = scenarioService.updateScenarioLiquidi(scenarioId, (String) scenario.get("liquidi"));
        if (!liquidiResult)
            throw new RuntimeException("Errore durante il salvataggio dei liquidi");

        boolean infoGenitoreResult = scenarioService.updateScenarioGenitoriInfo(scenarioId, (String) scenario.get("infoGenitore"));
        if (!infoGenitoreResult)
            throw new RuntimeException("Errore durante il salvataggio delle informazioni per il genitore");


        List<String> azioniChiaveList = (List<String>) scenarioData.get("azioniChiave");
        boolean azioniChiaveResult = azioneChiaveService.updateAzioniChiaveForScenario(scenarioId, azioniChiaveList);
        if (!azioniChiaveResult)
            throw new RuntimeException("Errore durante il salvataggio delle azioni chiave");

        List<Map<String, Object>> materialiList = (List<Map<String, Object>>) scenarioData.get("materialeNecessario");
        List<Integer> idMateriali = new ArrayList<>();
        if (materialiList != null) {
            idMateriali = materialiList.stream()
                    .map(m -> ((Double) m.get("idMateriale")).intValue())
                    .collect(Collectors.toList());
        }
        boolean materialeNecessarioResult = materialeService.associaMaterialiToScenario(scenarioId, idMateriali);
        if (!materialeNecessarioResult)
            throw new RuntimeException("Errore durante il salvataggio del materiale necessario");


        List<String> presidiList = (List<String>) scenarioData.get("presidi");
        Set<String> presidi = presidiList != null ? new HashSet<>(presidiList) : new HashSet<>();

        // Verifica che tutti i presidi esistano nel database
        List<String> presidiEsistenti = PresidiService.getAllPresidi();
        Set<String> presidiNonEsistenti = presidi.stream()
                .filter(p -> !presidiEsistenti.contains(p))
                .collect(Collectors.toSet());

        if (!presidiNonEsistenti.isEmpty()) {
            logger.warn("I seguenti presidi non sono presenti nel database: {}", presidiNonEsistenti);
            throw new RuntimeException("Alcuni presidi non sono presenti nel database: " + String.join(", ", presidiNonEsistenti));
        }

        boolean presidiResult = presidiService.savePresidi(scenarioId, presidi);
        if (!presidiResult)
            throw new RuntimeException("Errore durante il salvataggio dei presidi");

        Map<String, Object> esameFisicoData = (Map<String, Object>) scenarioData.get("esameFisico");
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

        Map<String, Object> pazienteT0Data = (Map<String, Object>) scenarioData.get("pazienteT0");
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
                    pazienteT0Data.get("LitriOssigeno") != null ? ((Double) pazienteT0Data.get("LitriOssigeno")).floatValue() : 0f,
                    ((Double) pazienteT0Data.get("EtCO2")).intValue(),
                    (String) pazienteT0Data.get("Monitor"),
                    venosi,
                    arteriosi
            )) {
                logger.warn("Errore durante il salvataggio del paziente T0");
            }
        }

        List<Map<String, Object>> esamiRefertiData = (List<Map<String, Object>>) scenarioData.get("esamiReferti");
        if (esamiRefertiData != null) {
            List<EsameReferto> esami = esamiRefertiData.stream()
                    .map(e -> new EsameReferto(
                            e.get("idEsame") != null ? ((Double) e.get("idEsame")).intValue() : -1,
                            scenarioId,
                            (String) e.get("tipo"),
                            (String) e.get("media"),
                            (String) e.get("refertoTestuale")
                    ))
                    .collect(Collectors.toList());

            if (!esameRefertoService.saveEsamiReferti(scenarioId, esami)) {
                logger.warn("Errore durante il salvataggio degli esami e referti");
            }
        }
    }


    private void savePatientSimulatedScenarioComponents(int scenarioId, Map<String, Object> jsonData) {
        String sceneggiatura = (String) jsonData.get("sceneggiatura");
        boolean result = patientSimulatedScenarioService.updateScenarioSceneggiatura(scenarioId, sceneggiatura);
        if (!result) throw new RuntimeException("Errore durante il salvataggio della sceneggiatura");
    }


    /**
     * Salva i componenti avanzati dello scenario.
     *
     * @param scenarioId l'ID dello scenario
     * @param jsonData   i dati JSON contenenti le informazioni dello scenario
     */
    private void saveAdvancedScenarioComponents(int scenarioId, Map<String, Object> jsonData) {
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
                        a.get("misura") != null ? ((Double) a.get("misura")).intValue() : null
                ))
                .collect(Collectors.toList());
    }
}
