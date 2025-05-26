package it.uniupo.simnova.views.creation.scenario;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.scenario.components.PazienteT0Service;
import it.uniupo.simnova.service.scenario.types.AdvancedScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.AdditionalParamDialog;
import it.uniupo.simnova.views.ui.helper.TimeSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.*;

import static it.uniupo.simnova.views.constant.AdditionParametersConst.ADDITIONAL_PARAMETERS;
import static it.uniupo.simnova.views.constant.AdditionParametersConst.CUSTOM_PARAMETER_KEY;

/**
 * Classe che rappresenta la vista per la creazione e gestione dei tempi in uno scenario avanzato.
 * <p>
 * Permette di definire i parametri vitali e le azioni da eseguire in diversi momenti dello scenario.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.1 (con gestione unità di misura per parametri custom)
 */
@PageTitle("Tempi")
@Route("tempi")
public class TempoView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione delle attività e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(TempoView.class);
    /**
     * Container principale per le sezioni temporali.
     * Ogni sezione rappresenta un tempo con i relativi parametri e azioni.
     */
    private final VerticalLayout timeSectionsContainer;
    /**
     * Lista di sezioni temporali (T0, T1, T2, ecc.).
     * Ogni sezione rappresenta un tempo con i relativi parametri e azioni.
     */
    private final List<TimeSection> timeSections = new ArrayList<>();
    /**
     * Pulsante per navigare alla schermata successiva.
     */
    private final Button nextButton;
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;

    private final AdvancedScenarioService advancedScenarioService;

    private final PazienteT0Service pazienteT0Service;
    /**
     * Contatore per il numero di tempo corrente.
     * Inizializzato a 1 per rappresentare T1 (T0 viene aggiunto separatamente se necessario).
     */
    private int timeCount = 1;
    /**
     * ID dello scenario corrente.
     */
    private int scenarioId;
    /**
     * Modalità di apertura della vista ("create" o "edit").
     */
    private String mode;

    /**
     * Costruttore della vista TempoView.
     * Inizializza il layout principale e i componenti della vista.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public TempoView(ScenarioService scenarioService, FileStorageService fileStorageService, AdvancedScenarioService advancedScenarioService, PazienteT0Service pazienteT0Service) {
        this.scenarioService = scenarioService;
        this.advancedScenarioService = advancedScenarioService;
        this.pazienteT0Service = pazienteT0Service;


        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());


        AppHeader header = new AppHeader(fileStorageService);


        Button backButton = StyleApp.getBackButton();

        backButton.addClickListener(e -> {

            if (scenarioId > 0) {
                backButton.getUI().ifPresent(ui -> ui.navigate("esameFisico/" + scenarioId));
            } else {

                backButton.getUI().ifPresent(ui -> ui.getPage().getHistory().back());
            }
        });


        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "DEFINIZIONE TEMPI SCENARIO",
                "Definisci i tempi dello scenario (T0, T1, T2...). Per ogni tempo, specifica i parametri vitali, " +
                        "eventuali parametri aggiuntivi, l'azione richiesta per procedere e le transizioni possibili (Tempo SI / Tempo NO). " +
                        "T0 rappresenta lo stato iniziale del paziente.",
                VaadinIcon.CLOCK.create(),
                "var(--lumo-primary-color)"
        );


        timeSectionsContainer = new VerticalLayout();
        timeSectionsContainer.setWidthFull();
        timeSectionsContainer.setSpacing(true);


        Button addTimeButton = new Button("Aggiungi Tempo (Tn)", new Icon(VaadinIcon.PLUS_CIRCLE));
        addTimeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTimeButton.addClassName(LumoUtility.Margin.Top.XLARGE);
        addTimeButton.addClickListener(event -> addTimeSection(timeCount++));


        contentLayout.add(headerSection, timeSectionsContainer, addTimeButton);



        nextButton = StyleApp.getNextButton();
        nextButton.addClickListener(e -> saveAllTimeSections());

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);


        mainLayout.add(customHeader, contentLayout, footerLayout);
    }

    /**
     * Imposta il parametro URL (ID dello scenario) per la vista.
     * Carica i dati iniziali o esistenti in base alla modalità ("create" o "edit").
     * Gestisce eventuali errori di formato o ID non valido.
     *
     * @param event     evento di navigazione (contiene informazioni sull'URL)
     * @param parameter parametro ID passato nell'URL (può essere nullo)
     */
    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro mancante nell'URL.");
                throw new NumberFormatException("ID Scenario è richiesto");
            }


            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0];


            this.scenarioId = Integer.parseInt(scenarioIdStr.trim());
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("ID Scenario non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("ID Scenario non valido");
            }

            if (scenarioService.getScenarioType(scenarioId).equals("Quick Scenario")) {
                logger.warn("ID Scenario non valido: {}", scenarioId);
                throw new NumberFormatException("Quick Scenario non supporta la gestione dei tempi");
            }


            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);


            VerticalLayout mainLayout = getContent();


            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst()
                    .ifPresent(headerLayout -> headerLayout.setVisible(!"edit".equals(mode)));


            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second)
                    .ifPresent(footerLayout -> footerLayout.getChildren()
                            .filter(component -> component instanceof CreditsComponent)
                            .forEach(credits -> credits.setVisible(!"edit".equals(mode))));


            if ("edit".equals(mode)) {
                logger.info("Modalità EDIT: caricamento dati Tempi esistenti per scenario {}", this.scenarioId);
                nextButton.setText("Salva");
                nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                nextButton.setIcon(new Icon(VaadinIcon.CHECK));
                nextButton.setIconAfterText(false);
                loadInitialData();
                loadExistingTimes();
            } else {
                logger.info("Modalità CREATE: caricamento dati iniziali T0 e preparazione per nuovi tempi per scenario {}", this.scenarioId);
                loadInitialData();
            }
        } catch (NumberFormatException e) {
            logger.error("Errore nel parsing o validazione dell'ID Scenario: '{}'", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante.");
        }
    }

    /**
     * Aggiunge una nuova sezione temporale (T1, T2, ecc.) al layout.
     * Ogni sezione contiene campi per i parametri vitali, azioni e parametri aggiuntivi.
     *
     * @param timeNumber numero del tempo corrente (1 per T1, 2 per T2, ecc.)
     */
    private void addTimeSection(int timeNumber) {

        boolean alreadyExists = timeSections.stream().anyMatch(ts -> ts.getTimeNumber() == timeNumber);
        if (alreadyExists) {
            logger.debug("Sezione per T{} esiste già, non viene aggiunta di nuovo.", timeNumber);
            return;
        }


        TimeSection timeSection = new TimeSection(timeNumber, scenarioService, timeSections, timeSectionsContainer, scenarioId);
        timeSections.add(timeSection);

        timeSections.sort(Comparator.comparingInt(TimeSection::getTimeNumber));

        timeSectionsContainer.removeAll();
        timeSections.forEach(ts -> timeSectionsContainer.add(ts.getLayout()));

        if (timeNumber == 0) {
            timeSection.hideRemoveButton();
        }

        Button addParamsButton = new Button("Aggiungi Parametri Aggiuntivi", new Icon(VaadinIcon.PLUS));
        addParamsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addParamsButton.addClassName(LumoUtility.Margin.Top.SMALL);
        addParamsButton.addClickListener(e -> AdditionalParamDialog.showAdditionalParamsDialog(timeSection));

        timeSection.getMedicalParamsForm().add(addParamsButton);
    }


    /**
     * Salva tutte le sezioni temporali (T0, T1, T2...) nel database.
     * Raccoglie i dati da ogni {@link TimeSection}, li invia al {@link ScenarioService}
     * e naviga alla schermata successiva in caso di successo.
     */
    private void saveAllTimeSections() {
        try {
            List<Tempo> allTempi = new ArrayList<>();


            for (TimeSection section : timeSections) {
                Tempo tempo = section.prepareDataForSave();
                allTempi.add(tempo);
                logger.info("Dati preparati per salvare tempo T{}: {}", tempo.getIdTempo(), tempo);
            }


            boolean success = advancedScenarioService.saveTempi(scenarioId, allTempi);

            if (success) {
                if (!mode.equals("edit")) {
                    Notification.show("Tempi dello scenario salvati con successo!", 3000,
                            Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                logger.info("Tempi salvati con successo per scenario {}", scenarioId);


                if ("create".equals(mode)) {
                    String scenarioType = scenarioService.getScenarioType(scenarioId);
                    switch (scenarioType) {
                        case "Advanced Scenario":

                            nextButton.getUI().ifPresent(ui -> ui.navigate("scenari/" + scenarioId));
                            break;
                        case "Patient Simulated Scenario":

                            nextButton.getUI().ifPresent(ui -> ui.navigate("sceneggiatura/" + scenarioId));
                            break;
                        default:
                            Notification.show("Tipo di scenario non riconosciuto per navigazione", 3000,
                                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                            logger.error("Tipo di scenario '{}' non gestito per navigazione post-salvataggio tempi (ID {})",
                                    scenarioType, scenarioId);
                            break;
                    }
                } else if ("edit".equals(mode)) {
                    nextButton.getUI().ifPresent(ui -> ui.navigate("scenari/" + scenarioId));
                    Notification.show("Modifiche ai tempi salvate con successo!", 3000,
                            Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
            } else {
                Notification.show("Errore durante il salvataggio dei tempi nel database.", 5000,
                        Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                logger.error("Errore durante il salvataggio dei tempi (scenarioService.saveTempi ha restituito false) per scenario {}",
                        scenarioId);
            }
        } catch (Exception e) {

            Notification.show("Errore imprevisto durante il salvataggio: " + e.getMessage(), 5000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            logger.error("Eccezione durante il salvataggio dei tempi per scenario {}", scenarioId, e);
        }
    }

    /**
     * Carica i dati iniziali per la sezione T0 (stato iniziale del paziente).
     * Recupera i parametri da {@link PazienteT0} associato allo scenario.
     * Se i dati sono presenti, li precompila nei campi della sezione T0 rendendoli non modificabili.
     * Se T0 non esiste nel DB, aggiunge una sezione T0 vuota e modificabile (solo in create mode?).
     */
    private void loadInitialData() {
        try {
            PazienteT0 pazienteT0 = pazienteT0Service.getPazienteT0ById(scenarioId);


            Optional<TimeSection> existingT0 = timeSections.stream()
                    .filter(ts -> ts.getTimeNumber() == 0)
                    .findFirst();

            if (pazienteT0 != null) {
                TimeSection t0Section;
                if (existingT0.isEmpty()) {

                    addTimeSection(0);
                    t0Section = timeSections.stream().filter(ts -> ts.getTimeNumber() == 0).findFirst().orElse(null);
                    if (t0Section == null) {
                        logger.error("Impossibile trovare la sezione T0 appena aggiunta per scenario {}", scenarioId);
                        return;
                    }
                } else {

                    t0Section = existingT0.get();
                }



                t0Section.setPaValue(pazienteT0.getPA());
                t0Section.setFcValue(pazienteT0.getFC());
                t0Section.setRrValue(pazienteT0.getRR());
                t0Section.setTValue(pazienteT0.getT());
                t0Section.setSpo2Value(pazienteT0.getSpO2());
                t0Section.setFio2Value(pazienteT0.getFiO2());
                t0Section.setLitriO2Value(pazienteT0.getLitriO2());
                t0Section.setEtco2Value(pazienteT0.getEtCO2());


                Notification.show("I parametri base di T0 derivano dallo stato iniziale del paziente e non sono modificabili qui.",
                        4000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_WARNING);


                loadAdditionalParameters(t0Section, 0);

            } else if (existingT0.isEmpty() && "create".equals(mode)) {


                logger.info("PazienteT0 non trovato per scenario {}, aggiunta sezione T0 vuota in modalità create.", scenarioId);
                addTimeSection(0);

            } else if (existingT0.isPresent()) {


                logger.warn("Sezione T0 presente nell'UI ma PazienteT0 non trovato nel DB per scenario {}", scenarioId);

            }

        } catch (Exception e) {
            Notification.show("Errore nel caricamento dei dati iniziali di T0: " + e.getMessage(),
                    5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            logger.error("Errore durante il caricamento dei dati iniziali (PazienteT0) per scenario {}", scenarioId, e);
        }
    }


    /**
     * Carica i dati dei tempi esistenti (T1, T2, ...) dallo scenario salvato.
     * Viene chiamato in modalità "edit" dopo {@link #loadInitialData()}.
     * Popola le sezioni temporali con i dati recuperati dal database.
     */
    private void loadExistingTimes() {
        if (!"edit".equals(mode)) return;

        List<Tempo> existingTempi = advancedScenarioService.getTempiByScenarioId(scenarioId);

        if (!existingTempi.isEmpty()) {
            logger.info("Trovati {} tempi esistenti per scenario {}", existingTempi.size(), scenarioId);



            TimeSection t0Section = timeSections.stream().filter(ts -> ts.getTimeNumber() == 0).findFirst().orElse(null);
            timeSections.clear();
            timeSectionsContainer.removeAll();
            if (t0Section != null) {
                timeSections.add(t0Section);
                timeSectionsContainer.add(t0Section.getLayout());
            }



            for (Tempo tempo : existingTempi) {
                int tempoId = tempo.getIdTempo();
                if (tempoId >= 0) {

                    addTimeSection(tempoId);
                    TimeSection section = timeSections.stream()
                            .filter(ts -> ts.getTimeNumber() == tempoId)
                            .findFirst()
                            .orElse(null);

                    if (section != null) {



                        if (tempoId > 0) {
                            section.paField.setValue(tempo.getPA() != null ? tempo.getPA() : "");
                            section.fcField.setValue(Optional.ofNullable(tempo.getFC()).map(Double::valueOf).orElse(null));
                            section.rrField.setValue(Optional.ofNullable(tempo.getRR()).map(Double::valueOf).orElse(null));
                            section.tField.setValue(Optional.of(tempo.getT()).orElse(null));
                            section.spo2Field.setValue(Optional.ofNullable(tempo.getSpO2()).map(Double::valueOf).orElse(null));
                            section.fio2Field.setValue(Optional.ofNullable(tempo.getFiO2()).map(Double::valueOf).orElse(null));
                            section.litriO2Field.setValue(Optional.ofNullable(tempo.getLitriO2()).map(Double::valueOf).orElse(null));
                            section.etco2Field.setValue(Optional.ofNullable(tempo.getEtCO2()).map(Double::valueOf).orElse(null));
                        }


                        section.actionDetailsArea.setValue(tempo.getAzione() != null ? tempo.getAzione() : "");
                        section.timeIfYesField.setValue(tempo.getTSi());
                        section.timeIfNoField.setValue(tempo.getTNo());
                        section.additionalDetailsArea.setValue(tempo.getAltriDettagli() != null ? tempo.getAltriDettagli() : "");
                        section.ruoloGenitoreArea.setValue(tempo.getRuoloGenitore() != null ? tempo.getRuoloGenitore() : "");

                        if (tempo.getTimerTempo() > 0) {
                            try {
                                section.timerPicker.setValue(LocalTime.ofSecondOfDay(tempo.getTimerTempo()));
                            } catch (Exception e) {
                                logger.warn("Errore nel parsing del timer ({}) per T{} scenario {}", tempo.getTimerTempo(), tempoId, scenarioId, e);
                                section.timerPicker.setValue(null);
                            }
                        } else {
                            section.timerPicker.setValue(null);
                        }


                        loadAdditionalParameters(section, tempoId);
                    } else {
                        logger.error("Impossibile trovare/creare la sezione per T{} durante il caricamento scenario {}", tempoId, scenarioId);
                    }
                }
            }

            timeCount = existingTempi.stream()
                    .mapToInt(Tempo::getIdTempo)
                    .max()
                    .orElse(0) + 1;
            if (timeCount == 0) timeCount = 1;


            timeSections.sort(Comparator.comparingInt(TimeSection::getTimeNumber));
            timeSectionsContainer.removeAll();
            timeSections.forEach(ts -> timeSectionsContainer.add(ts.getLayout()));

        } else {
            logger.info("Nessun tempo (T1, T2...) trovato nel database per scenario {}", scenarioId);

        }
    }

    /**
     * Carica i parametri aggiuntivi associati a un tempo specifico (tempoId)
     * per un dato scenario (scenarioId).
     * Aggiunge i campi corrispondenti alla sezione {@link TimeSection} fornita.
     *
     * @param section la sezione temporale (UI) a cui aggiungere i parametri
     * @param tempoId l'ID del tempo (0 per T0, 1 per T1, ...) di cui caricare i parametri
     */
    private void loadAdditionalParameters(TimeSection section, int tempoId) {
        List<ParametroAggiuntivo> params = advancedScenarioService.getParametriAggiuntiviByTempoId(tempoId, scenarioId);

        if (!params.isEmpty()) {
            logger.debug("Caricamento di {} parametri aggiuntivi per T{} scenario {}", params.size(), tempoId, scenarioId);
            for (ParametroAggiuntivo param : params) {
                String paramName = param.getNome();
                String unit = param.getUnitaMisura() != null ? param.getUnitaMisura() : "";
                String valueStr = param.getValore();



                String paramKey = ADDITIONAL_PARAMETERS.keySet().stream()
                        .filter(s -> s.equalsIgnoreCase(paramName))
                        .findFirst()
                        .orElse(CUSTOM_PARAMETER_KEY + "_" + paramName.replaceAll("\\s+", "_"));


                String label = paramName + (unit.isEmpty() ? "" : " (" + unit + ")");



                section.addCustomParameter(paramKey, label, unit);


                if (section.getCustomParameters().containsKey(paramKey)) {
                    try {
                        if (valueStr != null && !valueStr.trim().isEmpty()) {
                            double value = Double.parseDouble(valueStr.trim().replace(',', '.'));
                            section.getCustomParameters().get(paramKey).setValue(value);
                        } else {
                            section.getCustomParameters().get(paramKey).setValue(0.0);
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Errore parsing valore '{}' per parametro '{}' (T{}, Scenario {}). Impostato a 0.",
                                valueStr, paramName, tempoId, scenarioId, e);
                        section.getCustomParameters().get(paramKey).setValue(0.0);
                    } catch (NullPointerException e) {
                        logger.error("Errore: valore nullo per parametro '{}' (T{}, Scenario {}). Impostato a 0.",
                                paramName, tempoId, scenarioId, e);
                        section.getCustomParameters().get(paramKey).setValue(0.0);
                    }
                } else {

                    logger.warn("Campo per parametro con chiave '{}' non trovato nell'UI dopo addCustomParameter durante il caricamento (T{}, Scenario {}).",
                            paramKey, tempoId, scenarioId);
                }
            }
        } else {
            logger.debug("Nessun parametro aggiuntivo trovato per T{} scenario {}", tempoId, scenarioId);
        }
    }
}