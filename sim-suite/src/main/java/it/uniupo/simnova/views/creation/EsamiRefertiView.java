package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
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
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.EsameReferto;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.AppHeader;
import it.uniupo.simnova.views.home.CreditsComponent;
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
     * Modalità di visualizzazione: "create" o "edit".
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

    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService servizio per la gestione degli scenari
     */
    public EsamiRefertiView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = new FileStorageService();

        VerticalLayout mainLayout = getContent();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);
        mainLayout.getStyle().set("min-height", "100vh");

        // 1. HEADER con pulsante indietro
        AppHeader header = new AppHeader();
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        HorizontalLayout customHeader = new HorizontalLayout(backButton, header);
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        // 2. CONTENUTO PRINCIPALE con righe degli esami
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px");
        contentLayout.setPadding(true);
        contentLayout.setSpacing(false);
        contentLayout.getStyle()
                .set("margin", "0 auto")
                .set("flex-grow", "1");

        rowsContainer = new VerticalLayout();
        rowsContainer.setWidthFull();
        rowsContainer.setSpacing(true);

        Button addButton = new Button("Aggiungi Esame/Referto", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClassName(LumoUtility.Margin.Top.MEDIUM);
        addButton.addClickListener(event -> addNewRow());

        contentLayout.add(rowsContainer, addButton);

        // 3. FOOTER con pulsante avanti
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button nextButton = new Button("Avanti", new Icon(VaadinIcon.ARROW_RIGHT));
        nextButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextButton.setWidth("150px");

        CreditsComponent credits = new CreditsComponent();

        footerLayout.add(credits, nextButton);

        mainLayout.add(customHeader, contentLayout, footerLayout);

        backButton.addClickListener(e ->
                backButton.getUI().ifPresent(ui -> ui.navigate("materialenecessario/" + scenarioId)));

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

        } catch (NumberFormatException e) {
            logger.error("Errore nel parsing o validazione dello Scenario ID: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario non valido o mancante.");
            return; // Interrompi l'esecuzione se l'ID non è valido
        }

        // Aggiorna visibilità del pulsante indietro in base alla modalità
        Button backButton = getBackButton(); // Metodo che dovrai implementare per ottenere il pulsante
        backButton.setVisible("create".equals(mode));

        // Inizializza la vista in base alla modalità
        if ("edit".equals(mode)) {
            logger.info("Modalità EDIT: caricamento dati esistenti per scenario {}", this.scenarioId);
            loadExistingData();
        } else {
            logger.info("Modalità CREATE: aggiunta prima riga vuota per scenario {}", this.scenarioId);
            if (formRows.isEmpty()) {
                addNewRow();
            }
        }
    }

    /**
     * Recupera il pulsante "Indietro" dal layout.
     *
     * @return il pulsante "Indietro"
     */
    private Button getBackButton() {
        // Cerca il backButton nel layout
        // Esempio (adatta in base alla tua struttura):
        return getContent().getChildren()
                .filter(c -> c instanceof HorizontalLayout)
                .findFirst()
                .flatMap(layout -> layout.getChildren()
                        .filter(c -> c instanceof Button && "Indietro".equals(((Button) c).getText()))
                        .findFirst())
                .map(c -> (Button) c)
                .orElseThrow(() -> new IllegalStateException("Back button non trovato"));
    }

    /**
     * Carica i dati esistenti per lo scenario corrente in modalità "edit".
     */
    private void loadExistingData() {
        List<EsameReferto> existingData = scenarioService.getEsamiRefertiByScenarioId(scenarioId); // Supponendo che esista un metodo simile


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
                populateRow(data); // Implementa o adatta addNewRow per popolare i campi
            }
            logger.info("Popolate {} righe con dati esistenti.", existingData.size());
        }
    }

    /**
     * Aggiunge una nuova riga per l'inserimento di un esame/referto.
     */
    private void addNewRow() {
        FormRow newRow = new FormRow(rowCount++);
        formRows.add(newRow);

        // Crea container per la riga con bordo e pulsante di eliminazione
        VerticalLayout rowContainer = new VerticalLayout();
        rowContainer.addClassName(LumoUtility.Padding.MEDIUM);
        rowContainer.addClassName(LumoUtility.Border.ALL);
        rowContainer.addClassName(LumoUtility.BorderColor.CONTRAST_10);
        rowContainer.addClassName(LumoUtility.BorderRadius.MEDIUM);
        rowContainer.setPadding(true);
        rowContainer.setSpacing(false);

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

    /**
     * Popola una riga del form con i dati esistenti.
     *
     * @param data dati dell'esame/referto da popolare
     */
    private void populateRow(EsameReferto data) {
        FormRow existingRow = new FormRow(rowCount++); // Usa il costruttore esistente
        formRows.add(existingRow);

        // Logica per determinare se l'esame era custom o selezionato
        boolean isCustom = !existingRow.allLabExams.contains(data.getTipo()) && !existingRow.allInstrExams.contains(data.getTipo());

        if (isCustom) {
            existingRow.examTypeGroup.setValue("Inserisci manualmente");
            existingRow.customExamField.setValue(data.getTipo());
        } else {
            existingRow.examTypeGroup.setValue("Seleziona da elenco");
            existingRow.selectedExamField.setValue(data.getTipo());
        }
        existingRow.updateExamFieldVisibility(); // Aggiorna visibilità campi

        existingRow.getReportField().setValue(data.getRefertoTestuale() != null ? data.getRefertoTestuale() : "");

        // Gestione file esistente (più complesso con Upload component)
        if (data.getMedia() != null && !data.getMedia().isEmpty()) {
            Paragraph fileInfo = new Paragraph("File caricato: " + data.getMedia());
            fileInfo.addClassName(LumoUtility.FontSize.XSMALL);
            fileInfo.addClassName(LumoUtility.TextColor.SECONDARY);
            // Trova un posto dove inserirlo nel layout della riga
            existingRow.getRowLayout().addComponentAtIndex(4, fileInfo);
            logger.debug("File esistente '{}' associato alla riga {}", data.getMedia(), existingRow.getRowNumber());
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
                logger.info("Ultima riga eliminata in modalità edit.");
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
                for (FormRow row : formRows) {
                    String fileName = "";
                    // Gestione upload file

                    if (row.getUpload().getReceiver() instanceof MemoryBuffer buffer) {
                        if (buffer.getFileName() != null && !buffer.getFileName().isEmpty()) {
                            try (InputStream fileData = buffer.getInputStream()) {
                                fileName = fileStorageService.storeFile(fileData, buffer.getFileName(), scenarioId);
                            }
                        }
                    }

                    // Assumo che row.getRowNumber() fornisca un ID temporaneo per l'esame
                    // e che row.getSelectedExam() fornisca il tipo di esame
                    EsameReferto esameReferto = new EsameReferto(
                            row.getRowNumber(),         // idEsame
                            scenarioId,                 // id_scenario
                            row.getSelectedExam(),      // tipo
                            fileName,                   // media (percorso del file)
                            row.getReportField().getValue()  // refertoTestuale
                    );
                    esamiReferti.add(esameReferto);
                }

                boolean success = scenarioService.saveEsamiReferti(scenarioId, esamiReferti);

                switch (mode) {
                    case "create" -> ui.accessSynchronously(() -> {
                        getContent().remove(progressBar);
                        if (success) {
                            ui.navigate("moulage/" + scenarioId);
                        } else {
                            Notification.show("Errore durante il salvataggio degli esami/referti",
                                    3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    });
                    case "edit" -> ui.accessSynchronously(() -> {
                        getContent().remove(progressBar);
                        if (success) {
                            Notification.show("Esami/referti salvati correttamente",
                                    3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        } else {
                            Notification.show("Errore durante il salvataggio degli esami/referti",
                                    3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    });
                    default -> {
                        logger.warn("Modalità non riconosciuta: {}", mode);
                        Notification.show("Modalità non riconosciuta", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }

            } catch (Exception e) {
                ui.accessSynchronously(() -> {
                    getContent().remove(progressBar);
                    Notification.show("Errore: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    logger.error("Errore durante il salvataggio degli esami/referti", e);
                });
            }
        });
    }

    /**
     * Classe interna che rappresenta una riga del form per l'inserimento di un esame/referto.
     */
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
         * Lista di esami di laboratorio disponibili.
         * Questi esami possono essere selezionati dall'utente tramite un dialogo.
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
         * Questi esami possono essere selezionati dall'utente tramite un dialogo.
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
         */
        public FormRow(int rowNumber) {
            this.rowNumber = rowNumber;

            // Titolo della riga
            this.rowTitle = new Paragraph("Esame/Referto #" + rowNumber);
            rowTitle.addClassName(LumoUtility.FontWeight.BOLD);
            rowTitle.addClassName(LumoUtility.Margin.Bottom.NONE);

            // Configurazione radio button per tipo di esame
            examTypeGroup.setLabel("Tipo di inserimento");
            examTypeGroup.setItems("Seleziona da elenco", "Inserisci manualmente");
            examTypeGroup.setValue("Seleziona da elenco");
            examTypeGroup.addValueChangeListener(e -> updateExamFieldVisibility());

            // Stile migliorato per i radio button
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

            // Configurazione pulsante selezione
            selectExamButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            selectExamButton.addClassName(LumoUtility.Margin.Bottom.NONE);
            selectExamButton.setWidth("auto");

            // Layout orizzontale per i campi di selezione
            HorizontalLayout selectionLayout = new HorizontalLayout(selectedExamField, selectExamButton);
            selectionLayout.setWidthFull();
            selectionLayout.setFlexGrow(1, selectedExamField);
            selectionLayout.setAlignItems(FlexComponent.Alignment.END);
            selectionLayout.setSpacing(true);

            // Configurazione finestra di dialogo
            examDialog.setHeaderTitle("Seleziona Tipo Esame");
            examDialog.setWidth("600px");
            examDialog.setHeight("70vh");
            examDialog.setDraggable(true);
            examDialog.setResizable(true);

            // Barra di ricerca
            TextField searchField = new TextField();
            searchField.setPlaceholder("Cerca esame...");
            searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
            searchField.setWidthFull();
            searchField.setClearButtonVisible(true);
            searchField.addClassName(LumoUtility.Margin.Bottom.SMALL);

            // Creazione delle schede per le categorie
            Tabs categoryTabs = new Tabs();
            Tab labTab = new Tab("Laboratorio");
            Tab instrTab = new Tab("Strumentali");
            categoryTabs.add(labTab, instrTab);
            categoryTabs.setWidthFull();

            // Stile migliorato per le tabs
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

            // Listener per la ricerca
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

            // Pulsante per chiudere
            Button closeButton = new Button("Chiudi", e -> examDialog.close());
            closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            examDialog.getFooter().add(closeButton);

            // Aggiunta dei componenti alla finestra
            VerticalLayout dialogContent = new VerticalLayout();
            dialogContent.setPadding(false);
            dialogContent.setSpacing(false);
            dialogContent.add(searchField, categoryTabs, pages);
            examDialog.add(dialogContent);

            // Listener per il pulsante di selezione
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
            upload.setAcceptedFileTypes(".pdf", ".jpg", ".png", ".gif", ".mp4", ".mp3");
            upload.setMaxFiles(1);
            upload.setUploadButton(new Button("Carica File", new Icon(VaadinIcon.UPLOAD)));
            upload.setDropLabel(new Div(new Text("Trascina file qui o clicca per selezionare")));

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
            rowLayout.add(upload, 2);
            rowLayout.add(reportField, 2);
            rowLayout.setResponsiveSteps(
                    new ResponsiveStep("0", 1),
                    new ResponsiveStep("600px", 2),
                    new ResponsiveStep("900px", 3)
            );

            // Spaziatura migliorata
            rowLayout.getChildren().forEach(component -> component.getElement().getStyle().set("margin-bottom", "var(--lumo-space-s)"));
        }

        /**
         * Aggiorna la visibilità dei campi in base al tipo di inserimento selezionato.
         */
        private void updateExamFieldVisibility() {
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