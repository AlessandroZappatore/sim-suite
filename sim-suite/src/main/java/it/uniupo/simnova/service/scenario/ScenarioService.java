package it.uniupo.simnova.service.scenario;

import it.uniupo.simnova.domain.scenario.Scenario;
import it.uniupo.simnova.dto.ScenarioSummaryDTO;
import it.uniupo.simnova.repository.ScenarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Servizio per la gestione degli scenari.
 * Fornisce metodi per recuperare, creare e aggiornare i dati degli scenari nel database.
 * Questo servizio gestisce le operazioni CRUD di base per gli scenari, inclusa
 * la determinazione del tipo di scenario (Quick, Advanced, Patient Simulated).
 *
 * @author Alessandro Zappatore
 * @version 2.0
 */
@Service
public class ScenarioService {

    /**
     * Il logger per questa classe, utilizzato per registrare le operazioni e gli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioService.class);

    private final ScenarioRepository scenarioRepository;

    @Autowired
    public ScenarioService(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }

    /**
     * Recupera un oggetto {@link Scenario} completo dal database utilizzando il suo identificativo.
     *
     * @param id L'identificativo (<code>Integer</code>) dello scenario da recuperare.
     * @return L'oggetto {@link Scenario} corrispondente all'identificativo fornito; <code>null</code> se non trovato
     * o in caso di errore SQL.
     */
    @Transactional(readOnly = true)
    public Scenario getScenarioById(Integer id) {
        logger.info("Recupero scenario con ID: {}", id);
        Optional<Scenario> scenarioOptional = scenarioRepository.findById(id);
        if(scenarioOptional.isPresent()) {
            logger.info("Scenario con ID {} recuperato con successo.", id);
            return scenarioOptional.get();
        } else {
            logger.warn("Scenario con ID {} non trovato.", id);
            return null;
        }
    }

    /**
     * Recupera una lista di tutti gli scenari presenti nel database.
     * Per motivi di performance e di visualizzazione, vengono recuperati solo i campi essenziali.
     *
     * @return Una {@link List} di oggetti {@link Scenario} contenente gli scenari principali.
     * Restituisce una lista vuota in caso di errore o se non sono presenti scenari.
     */
    @Transactional(readOnly = true)
    public List<ScenarioSummaryDTO> getAllScenarios() {
        List<ScenarioSummaryDTO> summaries = scenarioRepository.findAllSummaries();
        logger.info("Recuperati e arricchiti {} scenari", summaries != null ? summaries.size() : 0);
        return summaries;
    }

    /**
     * Crea un nuovo scenario rapido o aggiorna uno esistente nel database.
     * Se <code>scenarioId</code> è <code>null</code> o lo scenario non esiste, ne viene creato uno nuovo.
     * Altrimenti, lo scenario esistente viene aggiornato.
     *
     * @param scenarioId    L'ID (<code>Integer</code>) dello scenario da aggiornare. Se <code>null</code>, un nuovo scenario sarà creato.
     * @param titolo        Il titolo dello scenario.
     * @param nomePaziente  Il nome del paziente associato.
     * @param patologia     La patologia del paziente.
     * @param autori        I nomi degli autori dello scenario.
     * @param timerGenerale Il timer generale preimpostato per lo scenario.
     * @return L'ID (<code>int</code>) dello scenario creato o aggiornato; <code>-1</code> in caso di errore.
     */
    @Transactional
    public int startQuickScenario(Integer scenarioId, String titolo, String nomePaziente, String patologia, String autori, float timerGenerale, String tipologiaPaziente, String tipologiaScenario) {
        try {
            Scenario scenario;
            if (scenarioId != null && scenarioRepository.existsById(scenarioId)) {
                Optional<Scenario> scenarioOptional = scenarioRepository.findById(scenarioId);
                if (scenarioOptional.isPresent())
                    scenario = scenarioOptional.get();
                else {
                    logger.warn("Scenario con ID {} non trovato, creando un nuovo scenario.", scenarioId);
                    scenario = new Scenario();
                }
            } else {
                scenario = new Scenario();
            }
            scenario.setTitolo(titolo);
            scenario.setNomePaziente(nomePaziente);
            scenario.setPatologia(patologia);
            scenario.setAutori(autori);
            scenario.setTimerGenerale(timerGenerale);
            scenario.setTipologiaPaziente(tipologiaPaziente);
            scenario.setTipologiaScenario(tipologiaScenario);

            Scenario saved = scenarioRepository.save(scenario);
            logger.info("Scenario salvato con ID: {}.", saved.getId());
            return saved.getId();
        } catch (Exception e) {
            logger.error("Errore durante il salvataggio dello scenario: {}", e.getMessage(), e);
            return -1;
        }
    }

    @Transactional(readOnly = true)
    public String getScenarioType(int scenarioId) {
        return scenarioRepository.findById(scenarioId)
                .map(Scenario::getTipologiaScenario)
                .orElse("Quick");
    }

    private boolean updateScenario(Integer scenarioId, String fieldName, Consumer<Scenario> updater) {
        try {
            Optional<Scenario> scenarioOptional = scenarioRepository.findById(scenarioId);
            if (scenarioOptional.isPresent()) {
                Scenario scenario = scenarioOptional.get();
                updater.accept(scenario);
                scenarioRepository.save(scenario);
                logger.info("Campo '{}' dello scenario con ID {} aggiornato con successo.", fieldName, scenarioId);
                return true;
            } else {
                logger.warn("Tentativo di aggiornare il campo '{}' fallito. Scenario con ID {} non trovato.", fieldName, scenarioId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento del campo '{}' per lo scenario ID {}: {}", fieldName, scenarioId, e.getMessage(), e);
            return false;
        }
    }

    @Transactional
    public boolean updateScenarioDescription(int scenarioId, String descrizione) {
        return updateScenario(scenarioId, "descrizione", scenario -> scenario.setDescrizione(descrizione));
    }


    @Transactional
    public boolean updateScenarioBriefing(int scenarioId, String briefing) {
        return updateScenario(scenarioId, "briefing", scenario -> scenario.setBriefing(briefing));
    }


    @Transactional
    public boolean updateScenarioPattoAula(int scenarioId, String patto_aula) {
        return updateScenario(scenarioId, "patto_aula", scenario -> scenario.setPattoAula(patto_aula));
    }


    @Transactional
    public boolean updateScenarioObiettiviDidattici(int scenarioId, String obiettivo) {
        return updateScenario(scenarioId, "obiettivo", scenario -> scenario.setObiettivo(obiettivo));
    }


    @Transactional
    public boolean updateScenarioMoulage(int scenarioId, String moulage) {
        return updateScenario(scenarioId, "moulage", scenario -> scenario.setMoulage(moulage));
    }


    @Transactional
    public boolean updateScenarioLiquidi(int scenarioId, String liquidi) {
        return updateScenario(scenarioId, "liquidi", scenario -> scenario.setLiquidi(liquidi));
    }

    @Transactional
    public boolean updateScenarioGenitoriInfo(Integer scenarioId, String value) {
        return updateScenario(scenarioId, "info_genitore", scenario -> scenario.setInfoGenitore(value));
    }

    @Transactional
    public boolean updateScenarioTarget(Integer scenarioId, String target) {
        return updateScenario(scenarioId, "target", scenario -> scenario.setTarget(target));
    }

    @Transactional
    public void updateScenarioTitleAndAuthors(Integer scenarioId, String newTitle, String newAuthors) {
        updateScenario(scenarioId, "titolo e autori", scenario -> {
            scenario.setTitolo(newTitle);
            scenario.setAutori(newAuthors);
        });
    }

    @Transactional
    public boolean updateScenarioSceneggiatura(Integer scenarioId, String sceneggiatura) {
        return updateScenario(scenarioId, "sceneggiatura", scenario -> scenario.setSceneggiatura(sceneggiatura));
    }

    @Transactional
    public void updateSingleField(int id, String label, String newValue) {
        Consumer<Scenario> updater;
        String dbField = switch (label) {
            case "Paziente" -> {
                updater = scenario -> scenario.setNomePaziente(newValue);
                yield "nome_paziente";
            }
            case "Patologia" -> {
                updater = scenario -> scenario.setPatologia(newValue);
                yield "patologia";
            }
            case "Tipologia" -> {
                updater = scenario -> scenario.setTipologiaPaziente(newValue);
                yield "tipologia_paziente";
            }
            case "Durata" -> {
                updater = scenario -> scenario.setTimerGenerale(Float.parseFloat(newValue));
                yield "timer_generale";
            }
            default -> {
                logger.warn("Label non valido per l'aggiornamento: '{}'.", label);
                throw new IllegalArgumentException("Label non valido: " + label);
            }
        };

        if (!updateScenario(id, dbField, updater)) {
            logger.warn("Aggiornamento del campo '{}' per lo scenario ID {} non riuscito.", label, id);
        }
    }


    /**
     * Controlla se uno scenario esiste nel database in base al suo ID.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da verificare.
     * @return <code>true</code> se lo scenario esiste; <code>false</code> altrimenti.
     */
    @Transactional(readOnly = true)
    public boolean existScenario(int scenarioId) {
        return scenarioRepository.existsById(scenarioId);
    }

    /**
     * Verifica se uno scenario è di tipo "Pediatrico" basandosi sulla sua tipologia paziente.
     *
     * @param scenarioId L'ID (<code>int</code>) dello scenario da controllare.
     * @return <code>true</code> se la tipologia paziente dello scenario è "Pediatrico" (case-insensitive); <code>false</code> altrimenti.
     */
    @Transactional(readOnly = true)
    public boolean isPediatric(int scenarioId) {
        return scenarioRepository.findById(scenarioId)
                .map(s -> "Pediatrico".equalsIgnoreCase(s.getTipologiaPaziente()))
                .orElse(false);
    }
}