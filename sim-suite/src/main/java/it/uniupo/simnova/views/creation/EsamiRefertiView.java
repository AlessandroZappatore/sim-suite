package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.EsameReferto;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.support.AppHeader;
import it.uniupo.simnova.views.support.CreditsComponent;
import it.uniupo.simnova.views.support.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Vista per la gestione degli esami e referti nello scenario di simulazione.
 * <p>
 * Permette di aggiungere, modificare e rimuovere esami clinici e relativi referti,
 * sia testuali che multimediali. Supporta l'upload di file e la selezione da un elenco
 * predefinito di esami di laboratorio e strumentali.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.0
 */
@PageTitle("Esami e Referti")
@Route(value = "esamiReferti")
@Menu(order = 9)
public class EsamiRefertiView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Pulsante per navigare alla vista successiva.
     */
    Button nextButton = StyleApp.getNextButton();
    /**
     * Modalità corrente (creazione o modifica).
     */
    private String mode;
    /**
     * Layout principale per la visualizzazione delle righe degli esami.
     */
    private final VerticalLayout rowsContainer;
    /**
     * Contatore per il numero di righe degli esami.
     */
    private int rowCount = 1;
    /**
     * Lista di righe degli esami/referti.
     */
    private final List<FormRow> formRows = new ArrayList<>();
    /**
     * Servizio per la gestione del caricamento dei file.
     */
    private final FileStorageService fileStorageService;
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(EsamiRefertiView.class);

    private static final String[] BORDER_COLORS = {
            "var(--lumo-primary-color)",          // Primario
            "var(--lumo-error-color)",            // Rosso
            "var(--lumo-success-color)",          // Verde
            "#FFB74D",                            // Arancione
            "#9575CD",                            // Viola
            "#4DD0E1",                            // Azzurro
            "#F06292"                             // Rosa
    };
    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     * @param fileStorageService servizio per la gestione dei file
     */
    public EsamiRefertiView(ScenarioService scenarioService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        // 1. HEADER con pulsante indietro
        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE con righe degli esami
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Esami e Referti",
                "Aggiungi gli esami e referti per il tuo scenario",
                VaadinIcon.FILE_TEXT_O,
                "var(--lumo-primary-color)"
        );

        rowsContainer = new VerticalLayout();
        rowsContainer.setWidthFull();
        rowsContainer.setSpacing(true);

        Button addButton = new Button("Aggiungi Esame/Referto", new Icon(VaadinIcon.PLUS_CIRCLE));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClassName(LumoUtility.Margin.Top.MEDIUM);
        addButton.addClickListener(event -> addNewRow());

        contentLayout.add(headerSection, rowsContainer, addButton);

        // 3. FOOTER con pulsante avanti
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("materialeNecessario/" + scenarioId)));

        nextButton.addClickListener(e -> saveEsamiRefertiAndNavigate(nextButton.getUI()));
    }

    /**
     * Gestisce il parametro ricevuto dall'URL (ID scenario).
     *
     * @param event     l'evento di navigazione
     * @param parameter l'ID dello scenario come stringa
     */
    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("Parametro mancante nell'URL.");
                throw new NumberFormatException("Scenario ID è richiesto");
            }

            // Dividi il parametro usando '/' come separatore
            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0]; // Il primo elemento è l'ID dello scenario

            // Verifica e imposta l'ID scenario
            this.scenarioId = Integer.parseInt(scenarioIdStr);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Scenario ID non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("Scenario ID non valido");
            }

            // Imposta la modalità se presente come secondo elemento
            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);

            // Modifica la visibilità dell'header e dei crediti
            VerticalLayout mainLayout = getContent();

            // Gestione dell'header (il primo HorizontalLayout)
            mainLayout.getChildren()
                .filter(component -> component instanceof HorizontalLayout)
                .findFirst()
                .ifPresent(header -> header.setVisible(!"edit".equals(mode)));

            // Gestione del footer con i crediti (l'ultimo HorizontalLayout)
            mainLayout.getChildren()
                .filter(component -> component instanceof HorizontalLayout)
                .reduce((first, second) -> second) // Prendi l'ultimo elemento
                .ifPresent(footer -> {
                    HorizontalLayout footerLayout = (HorizontalLayout) footer;
                    footerLayout.getChildren()
                        .filter(component -> component instanceof CreditsComponent)
                        .forEach(credits -> credits.setVisible(!"edit".equals(mode)));
                });

            // Inizializza la vista in base alla modalità
            if ("edit".equals(mode)) {
                logger.info("Modalità EDIT: caricamento dati esistenti per scenario {}", this.scenarioId);
                nextButton.setText("Salva");
                nextButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
                nextButton.setIcon(new Icon(VaadinIcon.CHECK));
                loadExistingData();
            } else {
                logger.info("Modalità CREATE: aggiunta prima riga vuota per scenario {}", this.scenarioId);
                if (formRows.isEmpty()) {
                    addNewRow();
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Errore nel parsing o validazione dello Scenario ID: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante.");
        }
    }

    /**
     * Carica i dati esistenti per lo scenario corrente in modalità "edit".
     */
    private void loadExistingData() {
        List<EsameReferto> existingData = scenarioService.getEsamiRefertiByScenarioId(scenarioId);

        if (existingData == null || existingData.isEmpty()) {
            logger.warn("Nessun dato esistente trovato per scenario {} in modalità edit. Aggiungo una riga vuota.", this.scenarioId);
            addNewRow(); // Aggiungi una riga vuota se non ci sono dati da modificare
        } else {
            // Rimuovi eventuali righe pre-esistenti (se addNewRow fosse chiamata prima)
            rowsContainer.removeAll();
            formRows.clear();
            rowCount = 1; // Resetta il contatore

            // Per ogni dato esistente, crea e popola una riga del form
            for (EsameReferto data : existingData) {
                populateRow(data);
            }
            logger.info("Popolate {} righe con dati esistenti.", existingData.size());
        }
    }

    /**
     * Aggiunge una nuova riga per l'inserimento di un esame/referto.
     */
    private void addNewRow() {
        FormRow newRow = new FormRow(rowCount++, fileStorageService);
        formRows.add(newRow);

        // Crea container per la riga con bordo e pulsante di eliminazione
        VerticalLayout rowContainer = new VerticalLayout();
        rowContainer.addClassName(LumoUtility.Padding.MEDIUM);
        rowContainer.addClassName(LumoUtility.Border.ALL);
        rowContainer.addClassName(LumoUtility.BorderColor.CONTRAST_10);
        rowContainer.addClassName(LumoUtility.BorderRadius.MEDIUM);
        rowContainer.setPadding(true);
        rowContainer.setSpacing(false);
        rowContainer.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("margin-bottom", "var(--lumo-space-l)")
                .set("border-left", "6px solid " + getBorderColor(rowCount))  // Colore dinamico
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        // Header della riga con titolo e pulsante elimina
        HorizontalLayout rowHeader = new HorizontalLayout();
        rowHeader.setWidthFull();
        rowHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        rowHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(e -> {
            formRows.remove(newRow);
            rowsContainer.remove(rowContainer);
            if (formRows.isEmpty()) {
                addNewRow(); // Mantieni almeno una riga
            }
        });

        rowHeader.add(newRow.getRowTitle(), deleteButton);
        rowContainer.add(rowHeader, newRow.getRowLayout());
        rowsContainer.add(rowContainer);
    }

    private String getBorderColor(int rowCount) {
        return BORDER_COLORS[(rowCount) % BORDER_COLORS.length];
    }


    /**
     * Popola una riga con i dati esistenti di un esame/referto.
     *
     * @param data dati dell'esame/referto da popolare
     */
    private void populateRow(EsameReferto data) {
        FormRow existingRow = new FormRow(rowCount++, fileStorageService);
        formRows.add(existingRow);

        // Logica per determinare se l'esame era custom o selezionato
        boolean isCustom = !existingRow.allLabExams.contains(data.getTipo()) && !existingRow.allInstrExams.contains(data.getTipo());

        if (isCustom) {
            existingRow.examTypeGroup.setValue("Inserisci manualmente");
            existingRow.customExamField.setValue(data.getTipo() != null ? data.getTipo() : "");
        } else {
            existingRow.examTypeGroup.setValue("Seleziona da elenco");
            existingRow.selectedExamField.setValue(data.getTipo() != null ? data.getTipo() : "");
        }
        existingRow.updateExamFieldVisibility(); // Aggiorna visibilità campi

        existingRow.getReportField().setValue(data.getRefertoTestuale() != null ? data.getRefertoTestuale() : "");

        // Gestione file esistente per i media
        if (data.getMedia() != null && !data.getMedia().isEmpty()) {
            existingRow.mediaSourceGroup.setValue("Seleziona da esistenti");
            existingRow.selectedMediaField.setValue(data.getMedia());
            existingRow.selectedExistingMedia = data.getMedia();
            existingRow.updateMediaFieldVisibility();
        }

        // Aggiungi la riga al container (simile a addNewRow)
        VerticalLayout rowContainer = new VerticalLayout();
        rowContainer.addClassName(LumoUtility.Padding.MEDIUM);
        rowContainer.addClassName(LumoUtility.Border.ALL);
        rowContainer.addClassName(LumoUtility.BorderColor.CONTRAST_10);
        rowContainer.addClassName(LumoUtility.BorderRadius.MEDIUM);
        rowContainer.setPadding(true);
        rowContainer.setSpacing(false);

        HorizontalLayout rowHeader = new HorizontalLayout();
        rowHeader.setWidthFull();
        rowHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        rowHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(e -> {
            formRows.remove(existingRow);
            rowsContainer.remove(rowContainer);
            if (formRows.isEmpty()) {
                addNewRow(); // Mantieni almeno una riga
            }
        });

        rowHeader.add(existingRow.getRowTitle(), deleteButton);
        rowContainer.add(rowHeader, existingRow.getRowLayout());
        rowsContainer.add(rowContainer);
    }

    /**
     * Salva gli esami/referti e naviga alla vista successiva.
     *
     * @param uiOptional l'UI corrente (opzionale)
     */
    private void saveEsamiRefertiAndNavigate(Optional<UI> uiOptional) {
        uiOptional.ifPresent(ui -> {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setIndeterminate(true);
            getContent().add(progressBar);

            try {
                List<EsameReferto> esamiReferti = new ArrayList<>();
                boolean hasValidData = false;

                for (FormRow row : formRows) {
                    String fileName = "";
                    String selectedExam = row.getSelectedExam();
                    String reportText = row.getReportField().getValue();

                    // Controlla la fonte del media (nuovo upload o esistente)
                    if ("Carica nuovo file".equals(row.mediaSourceGroup.getValue())) {
                        // Gestione upload file
                        if (row.getUpload().getReceiver() instanceof MemoryBuffer buffer) {
                            if (buffer.getFileName() != null && !buffer.getFileName().isEmpty()) {
                                try (InputStream fileData = buffer.getInputStream()) {
                                    fileName = fileStorageService.storeFile(fileData, buffer.getFileName());
                                    hasValidData = true;
                                }
                            }
                        }
                    } else {
                        // Usa il media esistente selezionato
                        fileName = row.getSelectedMedia();
                        if (fileName != null && !fileName.isEmpty()) {
                            hasValidData = true;
                        }
                    }

                    // Verifica se ci sono dati validi nell'esame o nel referto testuale
                    if ((selectedExam != null && !selectedExam.trim().isEmpty()) ||
                        (reportText != null && !reportText.trim().isEmpty())) {
                        hasValidData = true;
                    }

                    EsameReferto esameReferto = new EsameReferto(
                            row.getRowNumber(),
                            scenarioId,
                            selectedExam,
                            fileName,
                            reportText
                    );
                    esamiReferti.add(esameReferto);
                }

                // Salva i dati solo se ci sono contenuti validi
                if (hasValidData) {
                    boolean success = scenarioService.saveEsamiReferti(scenarioId, esamiReferti);
                    if (success) {
                        Notification.show("Esami e referti salvati con successo", 3000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    }
                } else {
                    logger.info("Nessun dato significativo da salvare per gli esami e referti dello scenario {}", scenarioId);
                }

                // Verifica la modalità: se è "edit" rimani nella pagina attuale,
                // altrimenti naviga alla pagina successiva
                boolean isEditMode = "edit".equals(mode);
                if (!isEditMode) {
                    // Naviga alla prossima vista solo se NON è in modalità edit
                    ui.navigate("moulage/" + scenarioId);
                }

            } catch (Exception e) {
                logger.error("Errore durante il salvataggio degli esami e referti", e);
                Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                getContent().remove(progressBar);
            }
        });
    }

    @SuppressWarnings("ThisExpressionReferencesGlobalObjectJS")
    private static class FormRow {
        /**
         * Numero della riga.
         */
        private final int rowNumber;
        /**
         * Titolo della riga.
         */
        private final Paragraph rowTitle;
        /**
         * Layout della riga.
         */
        private final FormLayout rowLayout;
        /**
         * Pulsante per selezionare l'esame.
         */
        private final Button selectExamButton = new Button("Seleziona", new Icon(VaadinIcon.SEARCH));
        /**
         * Finestra di dialogo per la selezione dell'esame.
         */
        private final Dialog examDialog = new Dialog();
        /**
         * Campo di testo per l'esame selezionato.
         */
        private final TextField selectedExamField = new TextField("Tipo Esame");
        /**
         * Campo di testo per l'esame personalizzato.
         */
        private final TextField customExamField = new TextField("Esame Personalizzato");
        /**
         * Gruppo di radio button per la selezione del tipo di esame.
         */
        private final RadioButtonGroup<String> examTypeGroup = new RadioButtonGroup<>();
        /**
         * Campo di upload per il file del referto.
         */
        private final Upload upload;
        /**
         * Campo di testo per il referto testuale.
         */
        private final TextField reportField;
        /**
         * Opzioni per la sorgente del media.
         */
        final RadioButtonGroup<String> mediaSourceGroup = new RadioButtonGroup<>();
        /**
         * Pulsante per selezionare media esistente.
         */
        private final Button selectMediaButton = new Button("Seleziona da esistenti", new Icon(VaadinIcon.FOLDER_OPEN));
        /**
         * Dialog per visualizzare media esistenti.
         */
        private final Dialog mediaDialog = new Dialog();
        /**
         * Campo di testo per mostrare il media selezionato.
         */
        private final TextField selectedMediaField = new TextField("Media Selezionato");
        /**
         * Nome del media esistente selezionato.
         */
        String selectedExistingMedia = null;
        /**
         * Servizio per l'accesso ai file.
         */
        private final FileStorageService fileStorageService;
        /**
         * Lista di esami di laboratorio disponibili.
         */
        private final List<String> allLabExams = List.of(
                "Emocromo con formula", "Glicemia", "Elettroliti sierici (Na⁺, K⁺, Cl⁻, Ca²⁺, Mg²⁺)",
                "Funzionalità renale (Creatinina, Azotemia)", "Funzionalità epatica (AST, ALT, Bilirubina, ALP, GGT)",
                "PCR (Proteina C Reattiva)", "Procalcitonina", "D-Dimero", "CK-MB, Troponina I/T",
                "INR, PTT, PT", "Gas arteriosi (pH, PaO₂, PaCO₂, HCO₃⁻, BE, Lactati)",
                "Emogas venoso", "Osmolarità sierica", "CPK", "Mioglobina"
        );
        /**
         * Lista di esami strumentali disponibili.
         */
        private final List<String> allInstrExams = List.of(
                "ECG (Elettrocardiogramma)", "RX Torace", "TC Torace (con mdc)", "TC Torace (senza mdc)",
                "TC Addome (con mdc)", "TC Addome (senza mdc)", "Ecografia addominale", "Ecografia polmonare",
                "Ecocardio (Transtoracico)", "Ecocardio (Transesofageo)", "Spirometria", "EEG (Elettroencefalogramma)",
                "RM Encefalo", "TC Cranio (con mdc)", "TC Cranio (senza mdc)", "Doppler TSA (Tronchi Sovraortici)",
                "Angio-TC Polmonare", "Fundus oculi"
        );

        /**
         * Costruttore per una riga del form.
         *
         * @param rowNumber numero della riga
         * @param fileStorageService servizio per la gestione dei file
         */
        public FormRow(int rowNumber, FileStorageService fileStorageService) {
            this.rowNumber = rowNumber;
            this.fileStorageService = fileStorageService;

            // Titolo della riga
            this.rowTitle = new Paragraph("Esame/Referto #" + rowNumber);
            rowTitle.addClassName(LumoUtility.FontWeight.BOLD);
            rowTitle.addClassName(LumoUtility.Margin.Bottom.NONE);

            // Configurazione radio button per tipo di esame
            examTypeGroup.setLabel("Tipo di inserimento");
            examTypeGroup.setItems("Seleziona da elenco", "Inserisci manualmente");
            examTypeGroup.setValue("Seleziona da elenco");
            examTypeGroup.addValueChangeListener(e -> updateExamFieldVisibility());

            // Stile per i radio button
            examTypeGroup.getStyle()
                    .set("margin-top", "0")
                    .set("margin-bottom", "var(--lumo-space-s)");

            // Configurazione campo esame selezionato
            selectedExamField.setReadOnly(true);
            selectedExamField.setWidthFull();
            selectedExamField.setPrefixComponent(new Icon(VaadinIcon.FILE_TEXT));
            selectedExamField.getElement().addEventListener("click", e -> {
                if ("Seleziona da elenco".equals(examTypeGroup.getValue())) {
                    selectExamButton.click();
                }
            });

            // Configurazione campo esame personalizzato
            customExamField.setWidthFull();
            customExamField.setVisible(false);
            customExamField.setPlaceholder("Inserisci il nome dell'esame");
            customExamField.setPrefixComponent(new Icon(VaadinIcon.EDIT));

            // Configurazione pulsante selezione esame
            selectExamButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            selectExamButton.addClassName(LumoUtility.Margin.Bottom.NONE);
            selectExamButton.setWidth("auto");

            // Layout orizzontale per i campi di selezione esame
            HorizontalLayout selectionLayout = new HorizontalLayout(selectedExamField, selectExamButton);
            selectionLayout.setWidthFull();
            selectionLayout.setFlexGrow(1, selectedExamField);
            selectionLayout.setAlignItems(FlexComponent.Alignment.END);
            selectionLayout.setSpacing(true);

            // Configurazione finestra di dialogo per esami
            examDialog.setHeaderTitle("Seleziona Tipo Esame");
            examDialog.setWidth("600px");
            examDialog.setHeight("70vh");
            examDialog.setDraggable(true);
            examDialog.setResizable(true);

            // Configurazione per la selezione di media esistenti
            mediaSourceGroup.setLabel("Sorgente del media");
            mediaSourceGroup.setItems("Carica nuovo file", "Seleziona da esistenti");
            mediaSourceGroup.setValue("Carica nuovo file");
            mediaSourceGroup.addValueChangeListener(e -> updateMediaFieldVisibility());

            // Configurazione campo media selezionato
            selectedMediaField.setReadOnly(true);
            selectedMediaField.setWidthFull();
            selectedMediaField.setPrefixComponent(new Icon(VaadinIcon.FILE));
            selectedMediaField.setVisible(false);

            // Configurazione pulsante selezione media
            selectMediaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            selectMediaButton.setWidth("auto");
            selectMediaButton.setVisible(false);

            // Layout per la selezione del media esistente
            HorizontalLayout mediaSelectionLayout = new HorizontalLayout(selectedMediaField, selectMediaButton);
            mediaSelectionLayout.setWidthFull();
            mediaSelectionLayout.setFlexGrow(1, selectedMediaField);
            mediaSelectionLayout.setAlignItems(FlexComponent.Alignment.END);
            mediaSelectionLayout.setSpacing(true);

            // Configurazione dialog per media esistenti
            mediaDialog.setHeaderTitle("Seleziona Media");
            mediaDialog.setWidth("600px");
            mediaDialog.setDraggable(true);
            mediaDialog.setResizable(true);

            // Configurazione dialog per selezione media
            configureMediaDialog();

            // Listener per il pulsante di selezione media
            selectMediaButton.addClickListener(e -> mediaDialog.open());

            // Configurazione ricerca esami
            TextField searchField = new TextField();
            searchField.setPlaceholder("Cerca esame...");
            searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
            searchField.setWidthFull();
            searchField.setClearButtonVisible(true);
            searchField.addClassName(LumoUtility.Margin.Bottom.SMALL);

            // Creazione delle schede per le categorie di esami
            Tabs categoryTabs = new Tabs();
            Tab labTab = new Tab("Laboratorio");
            Tab instrTab = new Tab("Strumentali");
            categoryTabs.add(labTab, instrTab);
            categoryTabs.setWidthFull();

            // Stile per le tabs
            categoryTabs.getStyle()
                    .set("margin-bottom", "0")
                    .set("box-shadow", "0 -1px 0 0 var(--lumo-contrast-10pct) inset");

            // Contenuti delle schede
            VerticalLayout labContent = createLabExamContent(allLabExams);
            VerticalLayout instrContent = createInstrumentalExamContent(allInstrExams);

            // Layout a schede
            Div pages = new Div(labContent, instrContent);
            pages.setWidthFull();
            pages.getStyle().set("overflow-y", "auto");
            pages.getStyle().set("max-height", "calc(70vh - 150px)");

            // Listener per la ricerca di esami
            searchField.addValueChangeListener(e -> {
                String searchTerm = e.getValue().toLowerCase();

                VerticalLayout filteredLabContent = createLabExamContent(
                        allLabExams.stream()
                                .filter(exam -> exam.toLowerCase().contains(searchTerm))
                                .collect(Collectors.toList())
                );

                VerticalLayout filteredInstrContent = createInstrumentalExamContent(
                        allInstrExams.stream()
                                .filter(exam -> exam.toLowerCase().contains(searchTerm))
                                .collect(Collectors.toList())
                );

                pages.removeAll();
                if (categoryTabs.getSelectedTab() == labTab) {
                    pages.add(filteredLabContent);
                } else {
                    pages.add(filteredInstrContent);
                }
            });

            categoryTabs.addSelectedChangeListener(event -> {
                pages.removeAll();
                if (event.getSelectedTab() == labTab) {
                    pages.add(createLabExamContent(allLabExams));
                } else {
                    pages.add(createInstrumentalExamContent(allInstrExams));
                }
            });

            // Pulsante per chiudere dialog esami
            Button closeButton = new Button("Chiudi", e -> examDialog.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            examDialog.getFooter().add(closeButton);

            // Aggiunta dei componenti alla finestra di esami
            VerticalLayout dialogContent = new VerticalLayout();
            dialogContent.setPadding(false);
            dialogContent.setSpacing(false);
            dialogContent.add(searchField, categoryTabs, pages);
            examDialog.add(dialogContent);

            // Listener per il pulsante di selezione esame
            selectExamButton.addClickListener(e -> {
                if ("Seleziona da elenco".equals(examTypeGroup.getValue())) {
                    examDialog.open();
                }
            });

            // Upload file
            MemoryBuffer buffer = new MemoryBuffer();
            this.upload = new Upload(buffer);
            upload.setDropAllowed(true);
            upload.setWidthFull();
            upload.setAcceptedFileTypes(".pdf", ".jpg", "jpeg", ".png", ".gif", ".mp4", ".mp3", ".webp");
            upload.setMaxFiles(1);
            upload.setUploadButton(new Button("Carica File", new Icon(VaadinIcon.UPLOAD)));
            upload.setDropLabel(new Div(new Text("Trascina file qui o clicca per selezionare")));

            // Campo referto testuale
            this.reportField = new TextField("Referto Testuale");
            reportField.setWidthFull();
            reportField.setPrefixComponent(new Icon(VaadinIcon.COMMENT));
            reportField.setPlaceholder("Inserisci il referto dell'esame...");

            // Configura il layout della riga
            this.rowLayout = new FormLayout();
            rowLayout.setWidthFull();
            rowLayout.add(examTypeGroup, 2);
            rowLayout.add(selectionLayout, 2);
            rowLayout.add(customExamField, 2);
            rowLayout.add(mediaSourceGroup, 2);
            rowLayout.add(upload, 2);
            rowLayout.add(mediaSelectionLayout, 2);
            rowLayout.add(reportField, 2);
            rowLayout.setResponsiveSteps(
                    new ResponsiveStep("0", 1),
                    new ResponsiveStep("600px", 2),
                    new ResponsiveStep("900px", 3)
            );

            // Spaziatura
            rowLayout.getChildren().forEach(component ->
                    component.getElement().getStyle().set("margin-bottom", "var(--lumo-space-s)"));

            // Imposta la visibilità iniziale
            updateMediaFieldVisibility();
        }

        /**
         * Aggiorna la visibilità dei campi in base al tipo di inserimento selezionato.
         */
        public void updateExamFieldVisibility() {
            boolean isCustom = "Inserisci manualmente".equals(examTypeGroup.getValue());
            selectedExamField.setVisible(!isCustom);
            selectExamButton.setVisible(!isCustom);
            customExamField.setVisible(isCustom);

            if (isCustom) {
                selectedExamField.clear();
            } else {
                customExamField.clear();
            }
        }

        /**
         * Aggiorna la visibilità dei campi relativi al media in base all'opzione selezionata.
         */
        public void updateMediaFieldVisibility() {
            boolean isNewUpload = "Carica nuovo file".equals(mediaSourceGroup.getValue());
            upload.setVisible(isNewUpload);
            selectedMediaField.setVisible(!isNewUpload);
            selectMediaButton.setVisible(!isNewUpload);

            // Se si cambia modalità, resetta l'altra opzione
            if (isNewUpload) {
                selectedMediaField.clear();
                selectedExistingMedia = null;
            } else {
                // Reset dell'upload quando si passa alla selezione
                upload.getElement().executeJs("this.files = []");
            }
        }

        /**
         * Configura il dialog per la selezione dei media esistenti.
         */
        private void configureMediaDialog() {
            // Barra di ricerca
            TextField searchField = new TextField();
            searchField.setPlaceholder("Cerca media...");
            searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
            searchField.setWidthFull();
            searchField.setClearButtonVisible(true);
            searchField.addClassName(LumoUtility.Margin.Bottom.SMALL);

            // Componente per visualizzare i media
            Div mediaContent = new Div();
            mediaContent.setWidthFull();
            mediaContent.getStyle()
                    .set("overflow-y", "auto")
                    .set("padding", "var(--lumo-space-m)")
                    .set("max-height", "400px");

            // Pulsante per chiudere
            Button closeButton = new Button("Chiudi", e -> mediaDialog.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            mediaDialog.getFooter().add(closeButton);

            // Ottieni e visualizza tutti i media disponibili
            loadAvailableMedia(mediaContent);

            // Aggiunta dei componenti alla finestra
            VerticalLayout dialogContent = new VerticalLayout();
            dialogContent.setPadding(false);
            dialogContent.setSpacing(false);
            dialogContent.add(searchField, mediaContent);
            dialogContent.setSizeFull();

            // Imposta una larghezza maggiore per il dialog
            mediaDialog.setWidth("700px");
            mediaDialog.setHeight("600px");
            mediaDialog.add(dialogContent);

            // Listener per la ricerca
            searchField.addValueChangeListener(e -> {
                String searchTerm = e.getValue().toLowerCase();
                loadAvailableMedia(mediaContent, searchTerm);
            });
        }

        /**
         * Carica i media disponibili nel dialog di selezione.
         *
         * @param container contenitore per i media
         */
        private void loadAvailableMedia(Div container) {
            loadAvailableMedia(container, null);
        }

        /**
         * Carica i media disponibili nel dialog di selezione con filtro di ricerca.
         *
         * @param container contenitore per i media
         * @param searchTerm termine di ricerca (opzionale)
         */
        private void loadAvailableMedia(Div container, String searchTerm) {
            container.removeAll();

            // Ottieni i media disponibili dal FileStorageService
            List<String> availableMedia = getAvailableMedia();

            // Filtra in base alla ricerca se necessario
            if (searchTerm != null && !searchTerm.isEmpty()) {
                availableMedia = availableMedia.stream()
                        .filter(media -> media.toLowerCase().contains(searchTerm.toLowerCase()))
                        .toList();
            }

            // Se non ci sono media disponibili
            if (availableMedia.isEmpty()) {
                Paragraph noResults = new Paragraph("Nessun media trovato");
                noResults.addClassName(LumoUtility.TextColor.SECONDARY);
                container.add(noResults);
                return;
            }

            // Crea un layout a griglia per visualizzare i media
            Div mediaGrid = new Div();
            mediaGrid.setWidthFull();
            mediaGrid.getStyle()
                    .set("display", "grid")
                    .set("grid-template-columns", "repeat(auto-fill, minmax(150px, 1fr))")
                    .set("grid-gap", "var(--lumo-space-m)")
                    .set("padding", "var(--lumo-space-s)");

            // Per ogni media disponibile
            for (String media : availableMedia) {
                // Crea un componente per il media
                VerticalLayout mediaItem = new VerticalLayout();
                mediaItem.setPadding(false);
                mediaItem.setSpacing(false);
                mediaItem.setWidth("100%");
                mediaItem.getStyle()
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("border", "1px solid var(--lumo-contrast-10pct)")
                        .set("cursor", "pointer")
                        .set("transition", "all 0.2s ease-in-out")
                        .set("overflow", "hidden");

                // Componente per l'anteprima del media
                Component mediaPreview;
                String mediaLower = media.toLowerCase();

                if (mediaLower.endsWith(".jpg") || mediaLower.endsWith(".jpeg") ||
                    mediaLower.endsWith(".png") || mediaLower.endsWith(".gif") ||
                    mediaLower.endsWith(".webp")) {

                    // Crea un'immagine per l'anteprima
                    Image image = getImage(media);
                    image.getStyle()
                            .set("object-fit", "contain")
                            .set("background-color", "var(--lumo-contrast-5pct)");

                    mediaPreview = image;
                } else {
                    // Per i file non visualizzabili, usa un'icona
                    Icon mediaIcon = getMediaIcon(media);
                    mediaIcon.setSize("48px");
                    mediaIcon.getStyle().set("margin", "var(--lumo-space-m) auto");

                    Div iconContainer = new Div(mediaIcon);
                    iconContainer.setWidth("100%");
                    iconContainer.setHeight("100px");
                    iconContainer.getStyle()
                            .set("display", "flex")
                            .set("align-items", "center")
                            .set("justify-content", "center")
                            .set("background-color", "var(--lumo-contrast-5pct)");

                    mediaPreview = iconContainer;
                }

                // Etichetta con il nome del file, troncata se troppo lunga
                Paragraph mediaName = new Paragraph(media);
                mediaName.getStyle()
                        .set("margin", "0")
                        .set("padding", "var(--lumo-space-xs)")
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("white-space", "nowrap")
                        .set("overflow", "hidden")
                        .set("text-overflow", "ellipsis")
                        .set("text-align", "center")
                        .set("background-color", "var(--lumo-base-color)")
                        .set("width", "100%");

                // Tooltip per mostrare il nome completo al passaggio del mouse
                mediaName.getElement().setAttribute("title", media);

                mediaItem.add(mediaPreview, mediaName);

                // Azione al click
                mediaItem.addClickListener(e -> {
                    selectedExistingMedia = media;
                    selectedMediaField.setValue(media);
                    mediaDialog.close();
                });

                // Effetto hover
                mediaItem.getElement().addEventListener("mouseover", e ->
                        mediaItem.getStyle().set("box-shadow", "0 0 5px var(--lumo-primary-color-50pct)"));
                mediaItem.getElement().addEventListener("mouseout", e ->
                        mediaItem.getStyle().set("box-shadow", "none"));

                mediaGrid.add(mediaItem);
            }

            container.add(mediaGrid);
        }

        private Image getImage(String media) {
            StreamResource resource = new StreamResource(media, () -> {
                try {
                    return fileStorageService.readFile(media);
                } catch (Exception e) {
                    logger.error("Errore nel caricamento dell'anteprima per {}", media, e);
                    return null;
                }
            });

            Image image = new Image(resource, "Anteprima");
            image.setWidth("100%");
            image.setHeight("100px");
            return image;
        }

        /**
         * Determina l'icona appropriata in base al tipo di file.
         *
         * @param filename nome del file
         * @return icona corrispondente
         */
        private Icon getMediaIcon(String filename) {
            filename = filename.toLowerCase();
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") ||
                filename.endsWith(".png") || filename.endsWith(".gif") ||
                filename.endsWith(".webp")) {
                return new Icon(VaadinIcon.PICTURE);
            } else if (filename.endsWith(".pdf")) {
                return new Icon(VaadinIcon.FILE);
            } else if (filename.endsWith(".mp4") || filename.endsWith(".webm") ||
                      filename.endsWith(".mov")) {
                return new Icon(VaadinIcon.FILM);
            } else if (filename.endsWith(".mp3") || filename.endsWith(".wav") ||
                      filename.endsWith(".ogg")) {
                return new Icon(VaadinIcon.HEADPHONES);
            } else {
                return new Icon(VaadinIcon.FILE);
            }
        }

        /**
         * Ottiene la lista dei media disponibili dal servizio di storage.
         *
         * @return lista dei media disponibili
         */
        private List<String> getAvailableMedia() {
            return fileStorageService.getAllFiles();
        }

        /**
         * Crea il contenuto per gli esami di laboratorio.
         *
         * @param exams lista degli esami da visualizzare
         * @return layout con i pulsanti degli esami
         */
        private VerticalLayout createLabExamContent(List<String> exams) {
            return createExamContent(exams);
        }

        /**
         * Crea il contenuto per gli esami strumentali.
         *
         * @param exams lista degli esami da visualizzare
         * @return layout con i pulsanti degli esami
         */
        private VerticalLayout createInstrumentalExamContent(List<String> exams) {
            return createExamContent(exams);
        }

        /**
         * Crea il layout con i pulsanti per la selezione degli esami.
         *
         * @param exams lista degli esami da visualizzare
         * @return layout configurato
         */
        private VerticalLayout createExamContent(List<String> exams) {
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            layout.setSpacing(false);
            layout.setWidthFull();

            if (exams.isEmpty()) {
                Paragraph noResults = new Paragraph("Nessun risultato trovato");
                noResults.addClassName(LumoUtility.TextColor.SECONDARY);
                noResults.getStyle().set("padding", "var(--lumo-space-m)");
                layout.add(noResults);
            } else {
                for (String exam : exams) {
                    Button examButton = createExamButton(exam);
                    layout.add(examButton);
                }
            }

            return layout;
        }

        /**
         * Crea un pulsante per la selezione di un esame specifico.
         *
         * @param examName nome dell'esame
         * @return pulsante configurato
         */
        private Button createExamButton(String examName) {
            Button button = new Button(examName, e -> {
                selectedExamField.setValue(examName);
                examDialog.close();
            });
            button.setWidthFull();
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            button.getStyle()
                    .set("text-align", "left")
                    .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                    .set("justify-content", "flex-start")
                    .set("border-radius", "var(--lumo-border-radius-m)");

            button.addClickListener(e -> {
                selectedExamField.setValue(examName);
                examDialog.close();
            });

            return button;
        }



        /**
         * Restituisce il nome dell'esame selezionato.
         *
         * @return nome dell'esame selezionato
         */
        public String getSelectedExam() {
            return "Inserisci manualmente".equals(examTypeGroup.getValue())
                    ? customExamField.getValue()
                    : selectedExamField.getValue();
        }

        /**
         * Restituisce il media selezionato dall'elenco esistente.
         *
         * @return nome del file media selezionato
         */
        public String getSelectedMedia() {
            return selectedExistingMedia;
        }

        /**
         * Restituisce il numero della riga.
         *
         * @return numero della riga
         */
        public int getRowNumber() {
            return rowNumber;
        }

        /**
         * Restituisce il titolo della riga.
         *
         * @return titolo della riga
         */
        public Paragraph getRowTitle() {
            return rowTitle;
        }

        /**
         * Restituisce il layout della riga.
         *
         * @return layout della riga
         */
        public FormLayout getRowLayout() {
            return rowLayout;
        }

        /**
         * Restituisce il pulsante di upload.
         *
         * @return pulsante di upload
         */
        public Upload getUpload() {
            return upload;
        }

        /**
         * Restituisce il campo di testo per il referto.
         *
         * @return campo di testo per il referto
         */
        public TextField getReportField() {
            return reportField;
        }
    }
}
