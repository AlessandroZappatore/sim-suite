package it.uniupo.simnova.views.creation.scenario;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.common.ParametroAggiuntivo;
import it.uniupo.simnova.domain.common.Tempo;
import it.uniupo.simnova.domain.paziente.PazienteT0;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.TimeSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
@Menu(order = 14)
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
    public TempoView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // 1. HEADER con pulsante indietro e titolo dell'applicazione
        AppHeader header = new AppHeader(fileStorageService);

        // Pulsante indietro con RouterLink per tornare alla vista precedente specifica
        Button backButton = StyleApp.getBackButton();

        backButton.addClickListener(e -> {
            // Naviga indietro a seconda dello stato (es. a esameFisico se scenarioId è valido)
            if (scenarioId > 0) {
                backButton.getUI().ifPresent(ui -> ui.navigate("esameFisico/" + scenarioId));
            } else {
                // Fallback se scenarioId non è ancora definito (improbabile ma sicuro)
                backButton.getUI().ifPresent(ui -> ui.getPage().getHistory().back());
            }
        });

        // Layout per l'header personalizzato (pulsante indietro + header app)
        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = StyleApp.getContentLayout();
        // Titolo della pagina
        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "DEFINIZIONE TEMPI SCENARIO",
                "Definisci i tempi dello scenario (T0, T1, T2...). Per ogni tempo, specifica i parametri vitali, " +
                        "eventuali parametri aggiuntivi, l'azione richiesta per procedere e le transizioni possibili (Tempo SI / Tempo NO). " +
                        "T0 rappresenta lo stato iniziale del paziente.",
                VaadinIcon.CLOCK,
                "var(--lumo-primary-color)"
        );

        // Container per le sezioni dei tempi (T0, T1, T2...)
        timeSectionsContainer = new VerticalLayout();
        timeSectionsContainer.setWidthFull();
        timeSectionsContainer.setSpacing(true); // Spazio tra le sezioni T0, T1...

        // Pulsante per aggiungere nuovi tempi (T1, T2, T3...)
        Button addTimeButton = new Button("Aggiungi Tempo (Tn)", new Icon(VaadinIcon.PLUS_CIRCLE));
        addTimeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTimeButton.addClassName(LumoUtility.Margin.Top.XLARGE); // Aumentato margine
        addTimeButton.addClickListener(event -> addTimeSection(timeCount++));

        // Aggiunta dei componenti al layout del contenuto
        contentLayout.add(headerSection, timeSectionsContainer, addTimeButton);

        // 3. FOOTER con crediti e pulsante Avanti
        // Pulsante per salvare e andare avanti
        nextButton = StyleApp.getNextButton();
        nextButton.addClickListener(e -> saveAllTimeSections());

        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        // Assemblaggio finale: aggiunta header, contenuto e footer al layout principale
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

            // Dividi il parametro usando '/' come separatore
            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0]; // Il primo elemento è l'ID dello scenario

            // Verifica e imposta l'ID scenario
            this.scenarioId = Integer.parseInt(scenarioIdStr.trim());
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("ID Scenario non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("ID Scenario non valido");
            }

            if (scenarioService.getScenarioType(scenarioId).equals("Quick Scenario")) {
                logger.warn("ID Scenario non valido: {}", scenarioId);
                throw new NumberFormatException("Quick Scenario non supporta la gestione dei tempi");
            }

            // Imposta la modalità se presente come secondo elemento
            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);

            // Modifica la visibilità dell'header e dei crediti in modalità edit
            VerticalLayout mainLayout = getContent();

            // Cerca il primo HorizontalLayout che contiene l'header
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst()
                    .ifPresent(headerLayout -> {
                        // Nascondi l'intero header in modalità edit
                        headerLayout.setVisible(!"edit".equals(mode));
                    });

            // Cerca l'ultimo HorizontalLayout che contiene i crediti
            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second) // Ottieni l'ultimo elemento (footer)
                    .ifPresent(footerLayout -> {
                        // Trova il componente Credits all'interno del footer e nascondilo
                        footerLayout.getChildren()
                                .filter(component -> component instanceof CreditsComponent)
                                .forEach(credits -> credits.setVisible(!"edit".equals(mode)));
                    });

            // Carica i dati in base alla modalità
            if ("edit".equals(mode)) {
                logger.info("Modalità EDIT: caricamento dati Tempi esistenti per scenario {}", this.scenarioId);
                nextButton.setText("Salva");
                nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                nextButton.setIcon(new Icon(VaadinIcon.CHECK));
                nextButton.setIconAfterText(false);
                loadInitialData(); // Carica sempre T0 se esiste
                loadExistingTimes(); // Carica T1, T2... esistenti
            } else {
                logger.info("Modalità CREATE: caricamento dati iniziali T0 e preparazione per nuovi tempi per scenario {}", this.scenarioId);
                loadInitialData(); // Carica T0 se esiste, altrimenti aggiunge sezione T0 vuota
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
        // Verifica se una sezione per questo numero esiste già (utile in modalità edit)
        boolean alreadyExists = timeSections.stream().anyMatch(ts -> ts.getTimeNumber() == timeNumber);
        if (alreadyExists) {
            logger.debug("Sezione per T{} esiste già, non viene aggiunta di nuovo.", timeNumber);
            return;
        }

        // Crea la nuova sezione
        TimeSection timeSection = new TimeSection(timeNumber, scenarioService, timeSections, timeSectionsContainer, scenarioId);
        timeSections.add(timeSection);
        // Ordina le sezioni per numero prima di aggiungerle al container
        timeSections.sort(Comparator.comparingInt(TimeSection::getTimeNumber));
        // Rimuove e riaggiunge tutto per mantenere l'ordine visivo
        timeSectionsContainer.removeAll();
        timeSections.forEach(ts -> timeSectionsContainer.add(ts.getLayout()));


        // Nasconde il pulsante "Rimuovi" per la sezione T0
        if (timeNumber == 0) {
            timeSection.hideRemoveButton();
        }

        // Aggiungi pulsante per parametri aggiuntivi a tutte le sezioni (incluso T0)
        Button addParamsButton = new Button("Aggiungi Parametri Aggiuntivi", new Icon(VaadinIcon.PLUS));
        addParamsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addParamsButton.addClassName(LumoUtility.Margin.Top.SMALL); // Margine sopra il pulsante
        addParamsButton.addClickListener(e -> showAdditionalParamsDialog(timeSection));

        // Aggiungi il pulsante sotto i campi dei parametri base
        // Trova il FormLayout dei parametri medici per aggiungere il pulsante alla fine
        timeSection.getMedicalParamsForm().add(addParamsButton);
    }

    /**
     * Mostra un dialog per selezionare parametri aggiuntivi da una lista predefinita
     * o per creare un nuovo parametro personalizzato.
     *
     * @param timeSection la sezione temporale a cui aggiungere i parametri
     */
    private void showAdditionalParamsDialog(TimeSection timeSection) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Seleziona Parametri Aggiuntivi per T" + timeSection.getTimeNumber());
        dialog.setWidth("600px"); // Larghezza maggiore per il contenuto

        // Campo di ricerca per filtrare i parametri
        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca parametri...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true); // Pulsante per pulire la ricerca

        // Pulsante per aprire il dialog di creazione parametro personalizzato
        Button addCustomParamButton = new Button("Crea Nuovo Parametro Personalizzato",
                new Icon(VaadinIcon.PLUS_CIRCLE));
        addCustomParamButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addCustomParamButton.addClassName(LumoUtility.Margin.Bottom.MEDIUM);
        addCustomParamButton.addClickListener(e -> {
            dialog.close(); // Chiude il dialog corrente
            showCustomParamDialog(timeSection); // Apre il dialog per il parametro custom
        });

        // Ottieni i parametri (identificati dalla chiave, es. "PVC" o "CUSTOM_...") già presenti in questa sezione
        Set<String> alreadySelectedKeys = timeSection.getCustomParameters().keySet();

        // Filtra i parametri predefiniti disponibili (mostra solo quelli non ancora aggiunti)
        List<String> availableParamsLabels = ADDITIONAL_PARAMETERS.entrySet().stream()
                .filter(entry -> !alreadySelectedKeys.contains(entry.getKey())) // Filtra per chiave
                .map(Map.Entry::getValue) // Ottiene l'etichetta completa (es. "Glicemia (mg/dL)")
                .collect(Collectors.toList());

        // CheckboxGroup per selezionare i parametri predefiniti
        CheckboxGroup<String> paramsSelector = new CheckboxGroup<>();
        paramsSelector.setItems(availableParamsLabels); // Imposta gli elementi disponibili
        paramsSelector.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL); // Layout verticale
        paramsSelector.setWidthFull();
        paramsSelector.getStyle().set("max-height", "300px").set("overflow-y", "auto"); // Altezza massima con scroll

        // Funzionalità di ricerca: filtra la lista visualizzata nel CheckboxGroup
        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue() != null ? e.getValue().trim().toLowerCase() : "";
            List<String> filteredParams = availableParamsLabels.stream()
                    .filter(paramLabel -> paramLabel.toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
            paramsSelector.setItems(filteredParams); // Aggiorna gli elementi visualizzati
        });

        // Pulsante di conferma per aggiungere i parametri selezionati
        Button confirmButton = new Button("Aggiungi Selezionati", e -> {
            paramsSelector.getSelectedItems().forEach(selectedLabel -> { // selectedLabel è l'etichetta completa
                // Trova la chiave (es. "PVC") corrispondente all'etichetta selezionata
                String paramKey = ADDITIONAL_PARAMETERS.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(selectedLabel))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse(""); // Chiave vuota se non trovata (improbabile)

                if (!paramKey.isEmpty()) {
                    // Estrae l'unità di misura dall'etichetta selezionata
                    String unit = "";
                    if (selectedLabel.contains("(") && selectedLabel.contains(")")) {
                        try {
                            unit = selectedLabel.substring(selectedLabel.indexOf("(") + 1, selectedLabel.indexOf(")"));
                        } catch (IndexOutOfBoundsException ex) {
                            unit = ""; // Fallback se il formato non è corretto
                        }
                    }
                    // Aggiunge il parametro alla sezione, passando chiave, etichetta e unità
                    timeSection.addCustomParameter(paramKey, selectedLabel, unit);
                }
            });
            dialog.close(); // Chiude il dialog dopo l'aggiunta
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Pulsante per annullare
        Button cancelButton = new Button("Annulla", e -> dialog.close());

        // Layout per i pulsanti del dialog
        HorizontalLayout buttonsLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END); // Allinea a destra

        // Layout verticale per il contenuto del dialog
        VerticalLayout dialogContent = new VerticalLayout(
                addCustomParamButton, // Pulsante per custom
                searchField, // Campo di ricerca
                new Paragraph("Seleziona dai parametri predefiniti:"),
                paramsSelector // Checkbox dei parametri predefiniti
        );
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);

        // Aggiunge contenuto e pulsanti al dialog
        dialog.add(dialogContent, buttonsLayout);
        dialog.open(); // Mostra il dialog
    }

    /**
     * Mostra un dialog per aggiungere un nuovo parametro personalizzato (non predefinito).
     * Permette di definire nome, unità di misura e valore iniziale.
     *
     * @param timeSection la sezione temporale a cui aggiungere il parametro
     */
    private void showCustomParamDialog(TimeSection timeSection) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aggiungi Parametro Personalizzato a T" + timeSection.getTimeNumber());
        dialog.setWidth("450px"); // Larghezza adeguata

        // Campo per il nome del parametro (obbligatorio)
        TextField nameField = new TextField("Nome parametro");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true); // Indica che è obbligatorio
        nameField.setErrorMessage("Il nome del parametro è obbligatorio");

        // Campo per l'unità di misura (opzionale)
        TextField unitField = new TextField("Unità di misura (opzionale)");
        unitField.setWidthFull();

        // Campo per il valore iniziale (opzionale, numerico)
        NumberField valueField = new NumberField("Valore iniziale (opzionale)"); // Usiamo NumberField
        valueField.setWidthFull();

        // Pulsante per salvare il parametro personalizzato
        Button saveButton = new Button("Salva Parametro", e -> {
            String paramName = nameField.getValue() != null ? nameField.getValue().trim() : "";
            String unit = unitField.getValue() != null ? unitField.getValue().trim() : "";
            Double initialValue = valueField.getValue(); // Ottiene il valore Double

            // Validazione: controlla se il nome è stato inserito
            if (paramName.isEmpty()) {
                nameField.setInvalid(true); // Mostra errore sul campo
                Notification.show(nameField.getErrorMessage(), 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                return; // Interrompe il salvataggio
            }
            nameField.setInvalid(false); // Rimuove eventuale errore precedente

            // Crea una chiave unica per il parametro custom (es. CUSTOM_Glicemia_Capillare)
            String paramKey = CUSTOM_PARAMETER_KEY + "_" + paramName.replaceAll("\\s+", "_");

            // Verifica se un parametro con questa chiave esiste già
            if (timeSection.getCustomParameters().containsKey(paramKey)) {
                Notification.show("Un parametro con questo nome esiste già.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            // Costruisce l'etichetta completa (Nome (Unità))
            String fullLabel = paramName + (unit.isEmpty() ? "" : " (" + unit + ")");

            // Aggiunge il parametro alla sezione, passando chiave, etichetta e unità
            timeSection.addCustomParameter(paramKey, fullLabel, unit);

            // Imposta il valore iniziale se fornito e se il campo è stato effettivamente aggiunto
            if (initialValue != null && timeSection.getCustomParameters().containsKey(paramKey)) {
                timeSection.getCustomParameters().get(paramKey).setValue(initialValue);
            } else if (timeSection.getCustomParameters().containsKey(paramKey)) {
                // Se nessun valore iniziale è dato, imposta 0.0 o lascia vuoto
                timeSection.getCustomParameters().get(paramKey).setValue(0.0); // Imposta default 0
            }

            dialog.close(); // Chiude il dialog dopo il salvataggio
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Pulsante per annullare
        Button cancelButton = new Button("Annulla", e -> dialog.close());

        // Layout per i pulsanti
        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        // Layout per il contenuto del dialog
        VerticalLayout dialogContent = new VerticalLayout(
                new Paragraph("Definisci un nuovo parametro non presente nella lista:"),
                nameField,
                unitField,
                valueField
        );
        dialogContent.setPadding(false);
        dialogContent.setSpacing(true);

        // Aggiunge contenuto e pulsanti al dialog
        dialog.add(dialogContent, buttonsLayout);
        dialog.open(); // Mostra il dialog
    }

    /**
     * Salva tutte le sezioni temporali (T0, T1, T2...) nel database.
     * Raccoglie i dati da ogni {@link TimeSection}, li invia al {@link ScenarioService}
     * e naviga alla schermata successiva in caso di successo.
     */
    private void saveAllTimeSections() {
        try {
            List<Tempo> allTempi = new ArrayList<>();

            // Prepara i dati per ogni sezione temporale
            for (TimeSection section : timeSections) {
                Tempo tempo = section.prepareDataForSave();
                allTempi.add(tempo);
                logger.info("Dati preparati per salvare tempo T{}: {}", tempo.getIdTempo(), tempo);
            }

            // Invia tutti i dati raccolti al servizio per il salvataggio nel DB
            boolean success = scenarioService.saveTempi(scenarioId, allTempi);

            if (success) {
                if (!mode.equals("edit")) {
                    Notification.show("Tempi dello scenario salvati con successo!", 3000,
                            Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                logger.info("Tempi salvati con successo per scenario {}", scenarioId);

                // Navigazione alla pagina successiva in base alla modalità e tipo scenario
                if ("create".equals(mode)) {
                    String scenarioType = scenarioService.getScenarioType(scenarioId);
                    switch (scenarioType) {
                        case "Advanced Scenario":
                            // Potrebbe andare a una dashboard o riepilogo scenario
                            nextButton.getUI().ifPresent(ui -> ui.navigate("scenari/" + scenarioId));
                            break;
                        case "Patient Simulated Scenario":
                            // Va alla definizione della sceneggiatura
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
                    // In modalità modifica, rimane sulla stessa pagina dopo il salvataggio
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
            // Gestisce eccezioni impreviste durante la preparazione o il salvataggio
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
            PazienteT0 pazienteT0 = scenarioService.getPazienteT0ById(scenarioId);

            // Controlla se la sezione T0 è già stata aggiunta (magari da loadExistingTimes in edit mode)
            Optional<TimeSection> existingT0 = timeSections.stream()
                    .filter(ts -> ts.getTimeNumber() == 0)
                    .findFirst();

            if (pazienteT0 != null) {
                TimeSection t0Section;
                if (existingT0.isEmpty()) {
                    // Se T0 non esiste nell'UI ma esiste nel DB (pazienteT0), lo aggiunge
                    addTimeSection(0); // Aggiunge la sezione T0 all'UI
                    t0Section = timeSections.stream().filter(ts -> ts.getTimeNumber() == 0).findFirst().orElse(null); // La recupera
                    if (t0Section == null) { // Check di sicurezza
                        logger.error("Impossibile trovare la sezione T0 appena aggiunta per scenario {}", scenarioId);
                        return;
                    }
                } else {
                    // Se T0 esiste già nell'UI (probabilmente da loadExistingTimes), usa quella
                    t0Section = existingT0.get();
                }


                // Imposta i valori nei campi di T0 e li rende read-only
                t0Section.setPaValue(pazienteT0.getPA());
                t0Section.setFcValue(pazienteT0.getFC());
                t0Section.setRrValue(pazienteT0.getRR());
                t0Section.setTValue(pazienteT0.getT());
                t0Section.setSpo2Value(pazienteT0.getSpO2());
                t0Section.setFio2Value(pazienteT0.getFiO2());
                t0Section.setLitriO2Value(pazienteT0.getLitriO2());
                t0Section.setEtco2Value(pazienteT0.getEtCO2());

                // Mostra notifica che T0 non è modificabile qui
                Notification.show("I parametri base di T0 derivano dallo stato iniziale del paziente e non sono modificabili qui.",
                        4000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_WARNING);

                // Carica eventuali parametri aggiuntivi salvati specificamente per T0
                loadAdditionalParameters(t0Section, 0);

            } else if (existingT0.isEmpty() && "create".equals(mode)) {
                // Se siamo in modalità creazione e PazienteT0 non esiste nel DB,
                // e la sezione T0 non è già presente nell'UI, aggiungiamo una T0 vuota e modificabile.
                logger.info("PazienteT0 non trovato per scenario {}, aggiunta sezione T0 vuota in modalità create.", scenarioId);
                addTimeSection(0);
                // In questo caso, i campi rimangono editabili
            } else if (existingT0.isPresent()) {
                // Se T0 è nell'UI ma non c'è PazienteT0 nel DB (situazione anomala?),
                // logga un warning ma lascia i campi editabili.
                logger.warn("Sezione T0 presente nell'UI ma PazienteT0 non trovato nel DB per scenario {}", scenarioId);
                // I campi rimarranno editabili come da default
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
        if (!"edit".equals(mode)) return; // Esegui solo in modalità modifica

        List<Tempo> existingTempi = scenarioService.getTempiByScenarioId(scenarioId); // Usa il metodo statico corretto

        if (!existingTempi.isEmpty()) {
            logger.info("Trovati {} tempi esistenti per scenario {}", existingTempi.size(), scenarioId);

            // Pulisce le sezioni esistenti eccetto T0 se già caricato da loadInitialData
            // Questo evita duplicati se loadInitialData ha già aggiunto T0.
            TimeSection t0Section = timeSections.stream().filter(ts -> ts.getTimeNumber() == 0).findFirst().orElse(null);
            timeSections.clear(); // Rimuove tutto temporaneamente
            timeSectionsContainer.removeAll(); // Pulisce l'UI
            if (t0Section != null) {
                timeSections.add(t0Section); // Riaggiunge T0 se era presente
                timeSectionsContainer.add(t0Section.getLayout());
            }


            // Itera sui tempi recuperati dal DB
            for (Tempo tempo : existingTempi) {
                int tempoId = tempo.getIdTempo();
                if (tempoId >= 0) {
                    // Aggiungi la sezione per questo tempo (se non esiste già)
                    addTimeSection(tempoId); // addTimeSection ora controlla se esiste già
                    TimeSection section = timeSections.stream()
                            .filter(ts -> ts.getTimeNumber() == tempoId)
                            .findFirst()
                            .orElse(null); // Trova la sezione appena aggiunta/esistente

                    if (section != null) {
                        // Popola i campi della sezione con i dati del Tempo dal DB

                        // Imposta i valori base (solo se non è T0 gestito da loadInitialData)
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

                        // Imposta i campi di azione e transizione
                        section.actionDetailsArea.setValue(tempo.getAzione() != null ? tempo.getAzione() : "");
                        section.timeIfYesField.setValue(tempo.getTSi()); // Usa l'ID del tempo
                        section.timeIfNoField.setValue(tempo.getTNo()); // Usa l'ID del tempo
                        section.additionalDetailsArea.setValue(tempo.getAltriDettagli() != null ? tempo.getAltriDettagli() : "");
                        section.ruoloGenitoreArea.setValue(tempo.getRuoloGenitore() != null ? tempo.getRuoloGenitore() : "");
                        // Imposta il timer se presente
                        if (tempo.getTimerTempo() > 0) {
                            try {
                                section.timerPicker.setValue(LocalTime.ofSecondOfDay(tempo.getTimerTempo()));
                            } catch (Exception e) {
                                logger.warn("Errore nel parsing del timer ({}) per T{} scenario {}", tempo.getTimerTempo(), tempoId, scenarioId, e);
                                section.timerPicker.setValue(null); // o LocalTime.MIDNIGHT
                            }
                        } else {
                            section.timerPicker.setValue(null); // Timer non impostato
                        }

                        // Carica i parametri aggiuntivi specifici per questo tempo
                        loadAdditionalParameters(section, tempoId);
                    } else {
                        logger.error("Impossibile trovare/creare la sezione per T{} durante il caricamento scenario {}", tempoId, scenarioId);
                    }
                }
            }
            // Assicurati che il timeCount sia aggiornato al massimo ID esistente + 1
            timeCount = existingTempi.stream()
                    .mapToInt(Tempo::getIdTempo)
                    .max()
                    .orElse(0) + 1;
            if (timeCount == 0) timeCount = 1; // Assicura che parta almeno da 1

            // Riordina visivamente le sezioni nel container dopo averle aggiunte tutte
            timeSections.sort(Comparator.comparingInt(TimeSection::getTimeNumber));
            timeSectionsContainer.removeAll();
            timeSections.forEach(ts -> timeSectionsContainer.add(ts.getLayout()));

        } else {
            logger.info("Nessun tempo (T1, T2...) trovato nel database per scenario {}", scenarioId);
            // Non fa nulla se non ci sono tempi salvati
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
        List<ParametroAggiuntivo> params = ScenarioService.getParametriAggiuntiviByTempoId(tempoId, scenarioId);

        if (!params.isEmpty()) {
            logger.debug("Caricamento di {} parametri aggiuntivi per T{} scenario {}", params.size(), tempoId, scenarioId);
            for (ParametroAggiuntivo param : params) {
                String paramName = param.getNome(); // Nome base dal DB (es. "Glicemia" o "ParamCustom")
                String unit = param.getUnitaMisura() != null ? param.getUnitaMisura() : ""; // Unità dal DB
                String valueStr = param.getValore(); // Valore come stringa dal DB

                // Determina la chiave: controlla se è predefinito, altrimenti costruisci la chiave custom
                // Cerca corrispondenza chiave predefinita (case-insensitive)
                String paramKey = ADDITIONAL_PARAMETERS.keySet().stream()
                        .filter(s -> s.equalsIgnoreCase(paramName))
                        .findFirst()
                        .orElse(CUSTOM_PARAMETER_KEY + "_" + paramName.replaceAll("\\s+", "_")); // Se non trovato, è custom

                // Ricostruisci l'etichetta completa per la visualizzazione
                String label = paramName + (unit.isEmpty() ? "" : " (" + unit + ")");

                // Aggiunge il parametro all'interfaccia utente della sezione
                // passando chiave, etichetta e unità separate
                section.addCustomParameter(paramKey, label, unit);

                // Imposta il valore nel campo appena aggiunto, gestendo errori di parsing
                if (section.getCustomParameters().containsKey(paramKey)) {
                    try {
                        if (valueStr != null && !valueStr.trim().isEmpty()) {
                            double value = Double.parseDouble(valueStr.trim().replace(',', '.')); // Gestisce virgola decimale
                            section.getCustomParameters().get(paramKey).setValue(value);
                        } else {
                            section.getCustomParameters().get(paramKey).setValue(0.0); // Default se vuoto
                        }
                    } catch (NumberFormatException e) {
                        logger.error("Errore parsing valore '{}' per parametro '{}' (T{}, Scenario {}). Impostato a 0.",
                                valueStr, paramName, tempoId, scenarioId, e);
                        section.getCustomParameters().get(paramKey).setValue(0.0); // Default in caso di errore
                    } catch (NullPointerException e) {
                        logger.error("Errore: valore nullo per parametro '{}' (T{}, Scenario {}). Impostato a 0.",
                                paramName, tempoId, scenarioId, e);
                        section.getCustomParameters().get(paramKey).setValue(0.0); // Default se valore è null
                    }
                } else {
                    // Logga un errore se il campo non è stato trovato dopo l'aggiunta (non dovrebbe succedere)
                    logger.warn("Campo per parametro con chiave '{}' non trovato nell'UI dopo addCustomParameter durante il caricamento (T{}, Scenario {}).",
                            paramKey, tempoId, scenarioId);
                }
            }
        } else {
            logger.debug("Nessun parametro aggiuntivo trovato per T{} scenario {}", tempoId, scenarioId);
        }
    }
}