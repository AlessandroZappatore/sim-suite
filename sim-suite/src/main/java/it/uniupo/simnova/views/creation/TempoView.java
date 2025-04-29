package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.ParametroAggiuntivo;
import it.uniupo.simnova.api.model.PazienteT0;
import it.uniupo.simnova.api.model.Tempo;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.home.CreditsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
     * Contatore per il numero di tempo corrente.
     * Inizializzato a 1 per rappresentare T1 (T0 viene aggiunto separatamente se necessario).
     */
    private int timeCount = 1;
    /**
     * Pulsante per navigare alla schermata successiva.
     */
    private final Button nextButton;
    /**
     * ID dello scenario corrente.
     */
    private int scenarioId;
    /**
     * Modalità di apertura della vista ("create" o "edit").
     */
    private String mode;
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Chiave per identificare i parametri personalizzati.
     */
    public static final String CUSTOM_PARAMETER_KEY = "CUSTOM";
    /**
     * Mappa per i parametri aggiuntivi predefiniti con le relative etichette (incluse unità).
     * Contiene i parametri standard e le loro descrizioni.
     */
    public static final Map<String, String> ADDITIONAL_PARAMETERS = new LinkedHashMap<>();
    /**
     * Logger per la registrazione delle attività e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(TempoView.class);

    static {
        // Popolamento della mappa dei parametri aggiuntivi predefiniti
        // (Cardiologia / Monitor Multiparametrico)
        ADDITIONAL_PARAMETERS.put("PVC", "Pressione Venosa Centrale (cmH₂O)");
        ADDITIONAL_PARAMETERS.put("QTc", "QT/QTc (ms)");
        ADDITIONAL_PARAMETERS.put("ST", "Segmento ST (mV)");
        ADDITIONAL_PARAMETERS.put("SI", "Indice di Shock (FC/PA sistolica)");

        // (Pneumologia / Ventilazione)
        ADDITIONAL_PARAMETERS.put("PIP", "Pressione Inspiratoria Positiva (cmH₂O)");
        ADDITIONAL_PARAMETERS.put("VT", "Volume Corrente (mL/kg)");
        ADDITIONAL_PARAMETERS.put("COMP", "Compliance Polmonare (mL/cmH₂O)");
        ADDITIONAL_PARAMETERS.put("RAW", "Resistenza Vie Aeree (cmH₂O/L/s)");
        ADDITIONAL_PARAMETERS.put("RSBI", "Indice di Tobin (atti/min/L)");

        // (Neurologia / Neuro Monitoraggio)
        ADDITIONAL_PARAMETERS.put("GCS", "Scala di Glasgow (3-15)");
        ADDITIONAL_PARAMETERS.put("ICP", "Pressione Intracranica (mmHg)");
        ADDITIONAL_PARAMETERS.put("PRx", "Indice di Pressione Cerebrale"); // Unità? Aggiungere se nota
        ADDITIONAL_PARAMETERS.put("BIS", "Bispectral Index (0-100)");
        ADDITIONAL_PARAMETERS.put("TOF", "Train of Four (%)");

        // (Emodinamica / Terapia Intensiva)
        ADDITIONAL_PARAMETERS.put("CO", "Gittata Cardiaca (L/min)");
        ADDITIONAL_PARAMETERS.put("CI", "Indice Cardiaco (L/min/m²)");
        ADDITIONAL_PARAMETERS.put("PCWP", "Pressione Capillare Polmonare (mmHg)");
        ADDITIONAL_PARAMETERS.put("SvO2", "Saturazione Venosa Mista (%)");
        ADDITIONAL_PARAMETERS.put("SVR", "Resistenza Vascolare Sistemica (dyn·s·cm⁻⁵)");

        // (Metabolismo / Elettroliti)
        ADDITIONAL_PARAMETERS.put("GLY", "Glicemia (mg/dL)");
        ADDITIONAL_PARAMETERS.put("LAC", "Lattati (mmol/L)");
        ADDITIONAL_PARAMETERS.put("NA", "Sodio (mmol/L)");
        ADDITIONAL_PARAMETERS.put("K", "Potassio (mmol/L)");
        ADDITIONAL_PARAMETERS.put("CA", "Calcio ionizzato (mmol/L)");

        // (Nefrologia / Diuresi)
        ADDITIONAL_PARAMETERS.put("UO", "Diuresi oraria (mL/h)");
        ADDITIONAL_PARAMETERS.put("CR", "Creatinina (mg/dL)");
        ADDITIONAL_PARAMETERS.put("BUN", "Azotemia (mg/dL)");

        // (Infettivologia / Stato Infettivo)
        ADDITIONAL_PARAMETERS.put("WBC", "Globuli Bianchi (10³/μL)");
        ADDITIONAL_PARAMETERS.put("qSOFA", "qSOFA (0-4)"); // Unità? Scala numerica

        // (Coagulazione / Ematologia)
        ADDITIONAL_PARAMETERS.put("INR", "INR"); // Adimensionale
        ADDITIONAL_PARAMETERS.put("PTT", "PTT (sec)");
        ADDITIONAL_PARAMETERS.put("PLT", "Piastrine (10³/μL)");

        //(Altri Monitoraggi Specializzati)
        ADDITIONAL_PARAMETERS.put("pCO₂ cutanea", "pCO₂ cutanea (mmHg)");
        ADDITIONAL_PARAMETERS.put("NIRS", "Ossimetria cerebrale (%)");
    }

    /**
     * Costruttore della vista TempoView.
     * Inizializza il layout principale e i componenti della vista.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public TempoView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;

        // Configurazione layout principale
        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh"); // Assicura altezza minima

        // 1. HEADER con pulsante indietro e titolo dell'applicazione
        AppHeader header = new AppHeader(fileStorageService);

        // Pulsante indietro con RouterLink per tornare alla vista precedente specifica
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
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
        HorizontalLayout customHeader = new HorizontalLayout();
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.add(backButton, header);
        customHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // 2. CONTENUTO PRINCIPALE
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px"); // Larghezza massima per leggibilità
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false); // Spaziatura gestita dai margini dei componenti
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Centra il contenuto
        contentLayout.getStyle()
                .set("margin", "0 auto") // Centra orizzontalmente il layout
                .set("flex-grow", "1"); // Fa espandere il contenuto per riempire lo spazio

        // Titolo della pagina
        H2 pageTitle = new H2("DEFINIZIONE TEMPI SCENARIO");
        pageTitle.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.LARGE,
                LumoUtility.Margin.Top.MEDIUM, // Aggiunto margine superiore
                LumoUtility.Margin.Horizontal.AUTO
        );
        pageTitle.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("width", "100%");

        // Testo di istruzioni per l'utente
        Paragraph instructionText = new Paragraph(
                "Definisci i tempi dello scenario (T0, T1, T2...). Per ogni tempo, specifica i parametri vitali, " +
                        "eventuali parametri aggiuntivi, l'azione richiesta per procedere e le transizioni possibili (Tempo SI / Tempo NO). " +
                        "T0 rappresenta lo stato iniziale del paziente.");
        instructionText.setWidth("100%");
        instructionText.getStyle().set("font-size", "var(--lumo-font-size-m)");
        instructionText.addClassNames(LumoUtility.TextAlignment.CENTER, LumoUtility.Margin.Bottom.LARGE);

        // Container per le sezioni dei tempi (T0, T1, T2...)
        timeSectionsContainer = new VerticalLayout();
        timeSectionsContainer.setWidthFull();
        timeSectionsContainer.setSpacing(true); // Spazio tra le sezioni T0, T1...

        // Pulsante per aggiungere nuovi tempi (T1, T2, T3...)
        Button addTimeButton = new Button("Aggiungi Tempo (Tn)", new Icon(VaadinIcon.PLUS));
        addTimeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addTimeButton.addClassName(LumoUtility.Margin.Top.XLARGE); // Aumentato margine
        addTimeButton.addClickListener(event -> addTimeSection(timeCount++));

        // Aggiunta dei componenti al layout del contenuto
        contentLayout.add(pageTitle, instructionText, timeSectionsContainer, addTimeButton);

        // 3. FOOTER con crediti e pulsante Avanti
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setSpacing(true); // Aggiunto spacing
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        footerLayout.addClassName(LumoUtility.Margin.Top.LARGE); // Aggiunto margine sopra il footer

        // Pulsante per salvare e andare avanti
        nextButton = new Button("Salva e Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.setIconAfterText(true); // Icona a destra
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("auto"); // Larghezza automatica
        nextButton.addClickListener(e -> saveAllTimeSections());

        // Testo crediti
        CreditsComponent credits = new CreditsComponent();

        footerLayout.add(credits, nextButton);

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

            // Nascondi il pulsante Indietro se in modalità "edit"
            // Cerca il pulsante Indietro nella UI
            getContent().getChildren()
                    .filter(c -> c instanceof HorizontalLayout)
                    .findFirst().flatMap(header -> header.getChildren()
                            .filter(c -> c instanceof Button && "Indietro".equals(((Button) c).getText()))
                            .findFirst()).ifPresent(backBtn -> backBtn.setVisible(!"edit".equals(mode)));

        } catch (NumberFormatException e) {
            logger.error("Errore nel parsing o validazione dell'ID Scenario: '{}'", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante.");
            return; // Interrompi l'esecuzione se l'ID non è valido
        }

        // Carica i dati in base alla modalità
        if ("edit".equals(mode)) {
            logger.info("Modalità EDIT: caricamento dati Tempi esistenti per scenario {}", this.scenarioId);
            loadInitialData(); // Carica sempre T0 se esiste
            loadExistingTimes(); // Carica T1, T2... esistenti
        } else {
            logger.info("Modalità CREATE: caricamento dati iniziali T0 e preparazione per nuovi tempi per scenario {}", this.scenarioId);
            loadInitialData(); // Carica T0 se esiste, altrimenti aggiunge sezione T0 vuota
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
        TimeSection timeSection = new TimeSection(timeNumber);
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
     * Crea un campo numerico ({@link NumberField}) per parametri medici con etichetta e unità.
     *
     * @param label etichetta del campo (es. "FC (bpm)")
     * @param unit  unità di misura da mostrare come suffisso (es. "battiti/min")
     * @return il campo {@link NumberField} configurato
     */
    private NumberField createMedicalField(String label, String unit) {
        NumberField field = new NumberField(label);
        if (unit != null && !unit.isEmpty()) {
            field.setSuffixComponent(new Paragraph(unit)); // Mostra l'unità alla fine
        }
        field.setWidthFull(); // Occupa tutta la larghezza disponibile nel layout
        // field.addClassName(LumoUtility.Margin.Bottom.SMALL); // Margine inferiore (gestito dal FormLayout)
        return field;
    }

    /**
     * Crea un campo di testo ({@link TextField}) specifico per la Pressione Arteriosa (PA).
     * Include l'unità "mmHg" come suffisso.
     *
     * @return il campo {@link TextField} configurato per la PA
     */
    private TextField createTextFieldPA() { // Rinominato per chiarezza
        TextField field = new TextField("PA (Sist/Diast)"); // Etichetta più descrittiva
        field.setSuffixComponent(new Paragraph("mmHg"));
        field.setWidthFull();
        // field.addClassName(LumoUtility.Margin.Bottom.SMALL); // Margine inferiore (gestito dal FormLayout)
        // Potrebbe avere un pattern per validazione: es. field.setPattern("\\d{1,3}/\\d{1,3}");
        field.setPlaceholder("es. 120/80");
        return field;
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

        List<Tempo> existingTempi = ScenarioService.getTempiByScenarioId(scenarioId); // Usa il metodo statico corretto

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
                // Aggiunge la sezione solo se non è T0 (T0 è gestito da loadInitialData)
                // o se T0 non era stato aggiunto da loadInitialData
                boolean t0AlreadyHandled = t0Section != null;
                if (tempoId > 0 || (!t0AlreadyHandled && tempoId == 0)) {
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
                            section.etco2Field.setValue(Optional.ofNullable(tempo.getEtCO2()).map(Double::valueOf).orElse(null));
                        }

                        // Imposta i campi di azione e transizione
                        section.actionDetailsArea.setValue(tempo.getAzione() != null ? tempo.getAzione() : "");
                        section.timeIfYesField.setValue(tempo.getTSi()); // Usa l'ID del tempo
                        section.timeIfNoField.setValue(tempo.getTNo()); // Usa l'ID del tempo
                        section.additionalDetailsArea.setValue(tempo.getAltriDettagli() != null ? tempo.getAltriDettagli() : "");

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

    /**
     * Classe interna che rappresenta una sezione temporale nell'interfaccia utente (T0, T1, T2, ecc.).
     * Contiene i campi per i parametri vitali base, i parametri aggiuntivi, le azioni
     * e le transizioni per un specifico momento dello scenario.
     */
    private class TimeSection {
        /**
         * Numero identificativo del tempo (0 per T0, 1 per T1, ...).
         */
        private final int timeNumber;
        /**
         * Layout principale verticale per questa sezione temporale.
         */
        private final VerticalLayout layout;
        /**
         * Selettore per impostare un timer associato a questo tempo.
         */
        private final TimePicker timerPicker;
        /**
         * Campo di testo per la Pressione Arteriosa (formato stringa es. "120/80").
         */
        private final TextField paField;
        /**
         * Campo numerico per la Frequenza Cardiaca (bpm).
         */
        private final NumberField fcField;
        /**
         * Campo numerico per la Frequenza Respiratoria (atti/min).
         */
        private final NumberField rrField;
        /**
         * Campo numerico per la Temperatura Corporea (°C).
         */
        private final NumberField tField;
        /**
         * Campo numerico per la Saturazione dell'Ossigeno (%).
         */
        private final NumberField spo2Field;
        /**
         * Campo numerico per la Capnometria di fine espirazione (mmHg).
         */
        private final NumberField etco2Field;
        /**
         * Area di testo per descrivere l'azione richiesta per procedere.
         */
        private final TextArea actionDetailsArea;
        /**
         * Campo numerico per l'ID del tempo successivo se la condizione/azione è SI.
         */
        private final IntegerField timeIfYesField;
        /**
         * Campo numerico per l'ID del tempo successivo se la condizione/azione è NO.
         */
        private final IntegerField timeIfNoField;
        /**
         * Area di testo per eventuali dettagli aggiuntivi o note su questo tempo.
         */
        private final TextArea additionalDetailsArea;
        /**
         * Layout (FormLayout) che contiene i campi dei parametri medici base.
         */
        private final FormLayout medicalParamsForm;
        /**
         * Layout verticale che contiene i campi dei parametri aggiuntivi/personalizzati.
         */
        private final VerticalLayout customParamsContainer;
        /**
         * Mappa che associa la chiave di un parametro aggiuntivo (es. "PVC" o "CUSTOM_Glicemia") al suo campo {@link NumberField}.
         */
        private final Map<String, NumberField> customParameters = new HashMap<>();
        /**
         * Mappa che associa la chiave di un parametro aggiuntivo alla sua unità di misura (Stringa).
         * Usata per recuperare l'unità durante il salvataggio.
         */
        private final Map<String, String> customParameterUnits = new HashMap<>();
        /**
         * Mappa che associa la chiave di un parametro aggiuntivo al layout orizzontale (campo + pulsante rimuovi) che lo contiene.
         */
        private final Map<String, HorizontalLayout> customParameterLayouts = new HashMap<>();

        /**
         * Costruttore per una sezione temporale.
         * Inizializza l'interfaccia utente (campi e layout) per questa sezione.
         *
         * @param timeNumber il numero identificativo di questo tempo (0, 1, 2...).
         */
        public TimeSection(int timeNumber) {
            this.timeNumber = timeNumber;

            // Layout principale della sezione
            layout = new VerticalLayout();
            layout.addClassName(LumoUtility.Padding.MEDIUM); // Padding interno
            layout.addClassName(LumoUtility.Border.ALL); // Bordo su tutti i lati
            layout.addClassName(LumoUtility.BorderColor.CONTRAST_10); // Colore bordo leggero
            layout.addClassName(LumoUtility.BorderRadius.MEDIUM); // Angoli arrotondati
            layout.setPadding(true);
            layout.setSpacing(false); // Spaziatura gestita dai margini interni

            // Titolo della sezione (es. "Tempo T1")
            Paragraph sectionTitle = new Paragraph("Tempo T" + timeNumber);
            sectionTitle.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.MEDIUM);

            // Selettore Timer
            timerPicker = new TimePicker("Timer associato a T" + timeNumber + " (opzionale)");
            timerPicker.setStep(Duration.ofSeconds(1)); // Precisione al secondo
            // timerPicker.setValue(LocalTime.MIDNIGHT); // Valore iniziale nullo o mezzanotte
            timerPicker.setPlaceholder("hh:mm:ss");
            timerPicker.setWidth("200px"); // Larghezza fissa
            timerPicker.setClearButtonVisible(true);

            // Form Layout per i parametri vitali base
            medicalParamsForm = new FormLayout();
            medicalParamsForm.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 1), // 1 colonna su schermi piccoli
                    new FormLayout.ResponsiveStep("500px", 2), // 2 colonne da 500px in su
                    new FormLayout.ResponsiveStep("800px", 3) // 3 colonne da 800px in su
            );
            medicalParamsForm.setWidthFull();

            // Creazione campi parametri base
            paField = createTextFieldPA(); // Usa il metodo helper specifico per PA
            fcField = createMedicalField("FC", "bpm");
            rrField = createMedicalField("FR", "atti/min");
            tField = createMedicalField("Temp.", "°C");
            spo2Field = createMedicalField("SpO₂", "%");
            etco2Field = createMedicalField("EtCO₂", "mmHg");

            // Aggiunta campi al FormLayout
            medicalParamsForm.add(paField, fcField, rrField, tField, spo2Field, etco2Field);
            medicalParamsForm.addClassName(LumoUtility.Margin.Bottom.MEDIUM); // Spazio sotto i parametri base

            // Container per i parametri aggiuntivi (verrà popolato dinamicamente)
            customParamsContainer = new VerticalLayout();
            customParamsContainer.setWidthFull();
            customParamsContainer.setPadding(false);
            customParamsContainer.setSpacing(false); // Spazio tra i parametri aggiuntivi
            // Il pulsante "Aggiungi Parametri" viene aggiunto esternamente a questo container nel FormLayout

            // Divisore orizzontale
            Hr divider = new Hr();
            divider.addClassName(LumoUtility.Margin.Vertical.MEDIUM);

            // Titolo per la sezione Azione/Transizioni
            Paragraph actionTitle = new Paragraph(timeNumber == 0 ?
                    "DETTAGLI INIZIALI T0" : "AZIONE E TRANSIZIONI PER T" + timeNumber);
            actionTitle.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);

            // Area di testo per l'azione
            actionDetailsArea = new TextArea("Azione richiesta / Evento scatenante per procedere da T" + timeNumber);
            actionDetailsArea.setWidthFull();
            actionDetailsArea.setMinHeight("80px"); // Altezza minima
            actionDetailsArea.setPlaceholder("Es. Somministrare farmaco X, Rilevare parametro Y, Domanda al paziente...");
            actionDetailsArea.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            // Container per i campi TSi / TNo
            HorizontalLayout timeSelectionContainer = new HorizontalLayout();
            timeSelectionContainer.setWidthFull();
            timeSelectionContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Centra il FormLayout interno

            // FormLayout per TSi / TNo
            FormLayout timeSelectionForm = new FormLayout();
            timeSelectionForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2)); // Sempre 2 colonne
            timeSelectionForm.setWidth("auto"); // Larghezza basata sul contenuto

            timeIfYesField = new IntegerField("Se SI, vai a T:");
            timeIfYesField.setMin(0); // Può puntare a T0
            timeIfYesField.setStepButtonsVisible(true); // Pulsanti +/-
            timeIfYesField.setWidth("150px"); // Larghezza fissa
            timeIfYesField.setPlaceholder("ID Tempo");

            timeIfNoField = new IntegerField("Se NO, vai a T:");
            timeIfNoField.setMin(0); // Può puntare a T0
            timeIfNoField.setStepButtonsVisible(true);
            timeIfNoField.setWidth("150px");
            timeIfNoField.setPlaceholder("ID Tempo");

            timeSelectionForm.add(timeIfYesField, timeIfNoField);
            timeSelectionContainer.add(timeSelectionForm);
            timeSelectionContainer.addClassName(LumoUtility.Margin.Bottom.MEDIUM);

            // Area di testo per dettagli aggiuntivi
            additionalDetailsArea = new TextArea("Altri dettagli / Note per T" + timeNumber + " (opzionale)");
            additionalDetailsArea.setWidthFull();
            additionalDetailsArea.setMinHeight("100px");
            additionalDetailsArea.setPlaceholder("Es. Note per il docente, trigger specifici, stato emotivo del paziente...");
            additionalDetailsArea.addClassName(LumoUtility.Margin.Bottom.LARGE);

            // Pulsante per rimuovere la sezione (visibile per T1, T2...)
            Button removeButton = new Button("Rimuovi T" + timeNumber, new Icon(VaadinIcon.TRASH));
            removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY); // Stile errore leggero
            removeButton.addClickListener(event -> {
                // Logica di rimozione: rimuove dalla lista e dall'UI
                timeSections.remove(this);
                timeSectionsContainer.remove(layout);
                // Potrebbe essere necessario ri-calcolare timeCount o aggiornare i riferimenti TSi/TNo altrove
                Notification.show("Tempo T" + timeNumber + " rimosso.", 2000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            });

            // Aggiunta di tutti i componenti al layout principale della sezione
            layout.add(sectionTitle, timerPicker, medicalParamsForm, customParamsContainer, divider,
                    actionTitle, actionDetailsArea, timeSelectionContainer,
                    additionalDetailsArea);
            // Aggiunge il pulsante Rimuovi solo se non è T0 (verrà nascosto se necessario)
            if (timeNumber > 0) {
                layout.add(removeButton);
                layout.setHorizontalComponentAlignment(FlexComponent.Alignment.END, removeButton); // Allinea a destra
            }
        }

        /**
         * Restituisce il layout principale ({@link VerticalLayout}) di questa sezione temporale.
         *
         * @return il layout della sezione.
         */
        public VerticalLayout getLayout() {
            return layout;
        }

        /**
         * Restituisce il numero identificativo di questo tempo (0 per T0, 1 per T1, ...).
         *
         * @return il numero del tempo.
         */
        public int getTimeNumber() {
            return timeNumber;
        }


        /**
         * Restituisce il {@link FormLayout} che contiene i campi dei parametri medici base.
         * Utile per aggiungere dinamicamente il pulsante "Aggiungi Parametri".
         *
         * @return il FormLayout dei parametri medici.
         */
        public FormLayout getMedicalParamsForm() {
            return medicalParamsForm;
        }

        /**
         * Restituisce la mappa dei parametri aggiuntivi/personalizzati (chiave -> campo NumberField).
         *
         * @return la mappa dei campi dei parametri aggiuntivi.
         */
        public Map<String, NumberField> getCustomParameters() {
            return customParameters;
        }

        /**
         * Nasconde il pulsante "Rimuovi Tempo". Usato solitamente per la sezione T0.
         */
        public void hideRemoveButton() {
            // Cerca il pulsante nel layout e lo nasconde se esiste
            layout.getChildren().filter(Button.class::isInstance)
                    .map(Button.class::cast)
                    .filter(button -> button.getText().startsWith("Rimuovi T"))
                    .findFirst()
                    .ifPresent(button -> button.setVisible(false));
        }


        /**
         * Aggiunge un parametro (predefinito o personalizzato) alla sezione temporale.
         * Crea un campo numerico {@link NumberField} con un pulsante per rimuoverlo,
         * e memorizza l'unità di misura associata.
         *
         * @param key   la chiave identificativa del parametro (es. "PVC" o "CUSTOM_Nome_Parametro").
         * @param label l'etichetta completa da visualizzare per il campo (es. "Nome Parametro (unit)").
         * @param unit  l'unità di misura (stringa) da associare a questo parametro per il salvataggio.
         */
        public void addCustomParameter(String key, String label, String unit) {
            // Controlla se un parametro con questa chiave esiste già per evitare duplicati
            if (!customParameters.containsKey(key)) {
                // Crea il campo NumberField usando l'etichetta completa fornita
                NumberField field = new NumberField(label);
                field.setWidthFull();
                // field.addClassName(LumoUtility.Margin.Bottom.XSMALL); // Aggiungi un po' di spazio sotto

                // Memorizza il campo nella mappa dei parametri
                customParameters.put(key, field);
                // Memorizza l'unità di misura nella mappa delle unità
                if (unit != null) {
                    customParameterUnits.put(key, unit);
                }

                // Pulsante per rimuovere questo specifico parametro aggiuntivo
                Button removeParamButton = new Button(new Icon(VaadinIcon.TRASH));
                removeParamButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON); // Icona errore piccola
                removeParamButton.getElement().setAttribute("aria-label", "Rimuovi " + label); // Accessibilità
                removeParamButton.setTooltipText("Rimuovi " + label); // Testo al passaggio del mouse
                removeParamButton.addClassName(LumoUtility.Margin.Left.SMALL); // Spazio a sinistra del pulsante

                // Layout orizzontale per contenere il campo e il pulsante di rimozione
                HorizontalLayout paramLayout = new HorizontalLayout(field, removeParamButton);
                paramLayout.setAlignItems(FlexComponent.Alignment.BASELINE); // Allinea campo e pulsante sulla base
                paramLayout.setWidthFull();
                paramLayout.setFlexGrow(1, field); // Fa espandere il campo per riempire lo spazio

                // Azione del pulsante di rimozione
                removeParamButton.addClickListener(e -> {
                    HorizontalLayout layoutToRemove = customParameterLayouts.get(key);
                    if (layoutToRemove != null) {
                        customParamsContainer.remove(layoutToRemove); // Rimuove il layout dall'UI
                        customParameters.remove(key); // Rimuove il campo dalla mappa
                        customParameterLayouts.remove(key); // Rimuove il layout dalla mappa dei layout
                        customParameterUnits.remove(key); // Rimuove l'unità dalla mappa delle unità
                    }
                });

                // Memorizza il layout orizzontale (per poterlo rimuovere)
                customParameterLayouts.put(key, paramLayout);
                // Aggiunge il layout del parametro al container dei parametri aggiuntivi
                customParamsContainer.add(paramLayout);

            } else {
                logger.warn("Tentativo di aggiungere un parametro con chiave duplicata: {}", key);
            }
        }


        /**
         * Prepara i dati di questa sezione temporale per il salvataggio nel database.
         * Raccoglie i valori dai campi dell'interfaccia utente, recupera le unità di misura
         * memorizzate e li assembla in un oggetto {@link Tempo}.
         *
         * @return un oggetto {@link Tempo} pronto per essere salvato.
         */
        public Tempo prepareDataForSave() {
            // Recupera valori dai campi base, gestendo valori null o vuoti
            LocalTime time = timerPicker.getValue();
            String pa = paField.getValue() != null ? paField.getValue().trim() : "";
            Integer fc = fcField.getValue() != null ? fcField.getValue().intValue() : null;
            Integer rr = rrField.getValue() != null ? rrField.getValue().intValue() : null;
            double rawT = tField.getValue() != null ? tField.getValue() : 0.0;
            double t = Math.round(rawT * 10.0) / 10.0;
            Integer spo2 = spo2Field.getValue() != null ? spo2Field.getValue().intValue() : null;
            Integer etco2 = etco2Field.getValue() != null ? etco2Field.getValue().intValue() : null;

            String actionDescription = actionDetailsArea.getValue() != null ? actionDetailsArea.getValue().trim() : "";
            int nextTimeIfYes = timeIfYesField.getValue() != null ? timeIfYesField.getValue() : 0;
            int nextTimeIfNo = timeIfNoField.getValue() != null ? timeIfNoField.getValue() : 0;
            String additionalDetails = additionalDetailsArea.getValue() != null ? additionalDetailsArea.getValue().trim() : "";
            long timerSeconds = (time != null) ? time.toSecondOfDay() : 0L;

            // Raccoglie i parametri aggiuntivi come List<ParametroAggiuntivo>
            List<ParametroAggiuntivo> additionalParamsList = new ArrayList<>();
            customParameters.forEach((key, field) -> {
                double value = field.getValue() != null ? field.getValue() : 0.0;
                String unit;
                String paramNameForDb;

                if (key.startsWith(CUSTOM_PARAMETER_KEY)) {
                    // Parametro Custom
                    paramNameForDb = key.substring(CUSTOM_PARAMETER_KEY.length() + 1).replace('_', ' ');
                    unit = customParameterUnits.getOrDefault(key, "");
                } else {
                    // Parametro Predefinito
                    paramNameForDb = key;
                    unit = customParameterUnits.get(key);
                    if (unit == null) {
                        String fullLabel = ADDITIONAL_PARAMETERS.getOrDefault(key, "");
                        if (fullLabel.contains("(") && fullLabel.contains(")")) {
                            try {
                                unit = fullLabel.substring(fullLabel.indexOf("(") + 1, fullLabel.indexOf(")"));
                            } catch (IndexOutOfBoundsException e) {
                                unit = "";
                            }
                        } else {
                            unit = "";
                        }
                    }
                }

                additionalParamsList.add(new ParametroAggiuntivo(
                        paramNameForDb,
                        value,
                        unit != null ? unit : ""
                ));
            });

            // Crea e restituisce l'oggetto Tempo con tutti i dati raccolti
            Tempo tempo = new Tempo(
                    timeNumber,          // idTempo
                    0,                   // advancedScenario (verrà impostato dal servizio)
                    pa.isEmpty() ? null : pa,
                    fc,
                    rr,
                    t,
                    spo2,
                    etco2,
                    actionDescription,
                    nextTimeIfYes,
                    nextTimeIfNo,
                    additionalDetails.isEmpty() ? null : additionalDetails,
                    timerSeconds
            );

            tempo.setParametriAggiuntivi(additionalParamsList);
            return tempo;
        }


        /**
         * Crea un campo di testo per la Pressione Arteriosa (PA).
         *
         * @param value il valore iniziale da impostare (può essere null).
         */
        public void setPaValue(String value) {
            paField.setValue(value != null ? value : "");
            paField.setReadOnly(true);
            paField.getStyle().set("background-color", "var(--lumo-contrast-5pct)"); // Sfondo leggermente grigio
        }

        /**
         * Imposta il valore FC per T0 e rende il campo non modificabile.
         *
         * @param value il valore da impostare (può essere null).
         */
        public void setFcValue(Integer value) { // Usa Integer per gestire null
            fcField.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
            fcField.setReadOnly(true);
            fcField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }

        /**
         * Imposta il valore RR per T0 e rende il campo non modificabile.
         *
         * @param value il valore da impostare (può essere null).
         */
        public void setRrValue(Integer value) {
            rrField.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
            rrField.setReadOnly(true);
            rrField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }

        /**
         * Imposta il valore T per T0 e rende il campo non modificabile.
         *
         * @param value il valore da impostare (può essere null).
         */
        public void setTValue(Double value) {
            if (value != null) {
                double roundedValue = Math.round(value * 10.0) / 10.0;
                tField.setValue(roundedValue);
            } else {
                tField.setValue(null);
            }
            tField.setReadOnly(true);
            tField.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }

        /**
         * Imposta il valore SpO2 per T0 e rende il campo non modificabile.
         *
         * @param value il valore da impostare (può essere null).
         */
        public void setSpo2Value(Integer value) {
            spo2Field.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
            spo2Field.setReadOnly(true);
            spo2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }

        /**
         * Imposta il valore EtCO2 per T0 e rende il campo non modificabile.
         *
         * @param value il valore da impostare (può essere null).
         */
        public void setEtco2Value(Integer value) {
            etco2Field.setValue(Optional.ofNullable(value).map(Double::valueOf).orElse(null));
            etco2Field.setReadOnly(true);
            etco2Field.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        }
    }
}