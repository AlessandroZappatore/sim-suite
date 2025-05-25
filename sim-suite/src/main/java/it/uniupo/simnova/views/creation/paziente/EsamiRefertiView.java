package it.uniupo.simnova.views.creation.paziente;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.domain.paziente.EsameReferto;
import it.uniupo.simnova.service.scenario.components.EsameRefertoService;
import it.uniupo.simnova.service.storage.FileStorageService;
import it.uniupo.simnova.service.scenario.ScenarioService;
import it.uniupo.simnova.views.common.components.AppHeader;
import it.uniupo.simnova.views.common.components.CreditsComponent;
import it.uniupo.simnova.views.common.utils.StyleApp;
import it.uniupo.simnova.views.ui.helper.support.FormRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.uniupo.simnova.views.constant.ColorsConst.BORDER_COLORS;
import static it.uniupo.simnova.views.constant.ExamConst.ALLINSTREXAMS;
import static it.uniupo.simnova.views.constant.ExamConst.ALLLABSEXAMS;

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
public class EsamiRefertiView extends Composite<VerticalLayout> implements HasUrlParameter<String> {
    /**
     * Logger per la registrazione degli eventi.
     */
    private static final Logger logger = LoggerFactory.getLogger(EsamiRefertiView.class);
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    private final EsameRefertoService esameRefertoService;
    /**
     * Layout principale per la visualizzazione delle righe degli esami.
     */
    private final VerticalLayout rowsContainer;
    /**
     * Lista di righe degli esami/referti.
     */
    private final List<FormRow> formRows = new ArrayList<>();
    /**
     * Servizio per la gestione del caricamento dei file.
     */
    private final FileStorageService fileStorageService;
    /**
     * Pulsante per navigare alla vista successiva.
     */
    Button nextButton = StyleApp.getNextButton();
    /**
     * ID dello scenario corrente.
     */
    private Integer scenarioId;
    /**
     * Modalità corrente (creazione o modifica).
     */
    private String mode;
    /**
     * Contatore per il numero di righe degli esami.
     */
    private int rowCount = 1;


    /**
     * Costruttore che inizializza l'interfaccia utente.
     *
     * @param scenarioService    servizio per la gestione degli scenari
     * @param fileStorageService servizio per la gestione dei file
     */
    public EsamiRefertiView(ScenarioService scenarioService, FileStorageService fileStorageService, EsameRefertoService esameRefertoService) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.esameRefertoService = esameRefertoService;

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());


        AppHeader header = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);


        VerticalLayout contentLayout = StyleApp.getContentLayout();

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Esami e Referti",
                "Aggiungi gli esami e referti per il tuo scenario",
                VaadinIcon.FILE_TEXT_O.create(),
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


            String[] parts = parameter.split("/");
            String scenarioIdStr = parts[0];


            this.scenarioId = Integer.parseInt(scenarioIdStr);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("Scenario ID non valido o non esistente: {}", scenarioId);
                throw new NumberFormatException("Scenario ID non valido");
            }


            mode = parts.length > 1 && "edit".equals(parts[1]) ? "edit" : "create";

            logger.info("Scenario ID impostato a: {}, Mode: {}", this.scenarioId, mode);


            VerticalLayout mainLayout = getContent();


            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .findFirst()
                    .ifPresent(header -> header.setVisible(!"edit".equals(mode)));


            mainLayout.getChildren()
                    .filter(component -> component instanceof HorizontalLayout)
                    .reduce((first, second) -> second)
                    .ifPresent(footer -> {
                        HorizontalLayout footerLayout = (HorizontalLayout) footer;
                        footerLayout.getChildren()
                                .filter(component -> component instanceof CreditsComponent)
                                .forEach(credits -> credits.setVisible(!"edit".equals(mode)));
                    });


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
        List<EsameReferto> existingData = esameRefertoService.getEsamiRefertiByScenarioId(scenarioId);

        if (existingData == null || existingData.isEmpty()) {
            logger.warn("Nessun dato esistente trovato per scenario {} in modalità edit. Aggiungo una riga vuota.", this.scenarioId);
            addNewRow();
        } else {

            rowsContainer.removeAll();
            formRows.clear();
            rowCount = 1;


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
                .set("border-left", "6px solid " + getBorderColor(rowCount))
                .set("box-shadow", "var(--lumo-box-shadow-s)");


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
                addNewRow();
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


        boolean isCustom = !ALLLABSEXAMS.contains(data.getTipo()) && !ALLINSTREXAMS.contains(data.getTipo());

        if (isCustom) {
            existingRow.examTypeGroup.setValue("Inserisci manualmente");
            existingRow.customExamField.setValue(data.getTipo() != null ? data.getTipo() : "");
        } else {
            existingRow.examTypeGroup.setValue("Seleziona da elenco");
            existingRow.selectedExamField.setValue(data.getTipo() != null ? data.getTipo() : "");
        }
        existingRow.updateExamFieldVisibility();

        existingRow.getReportField().setValue(data.getRefertoTestuale() != null ? data.getRefertoTestuale() : "");


        if (data.getMedia() != null && !data.getMedia().isEmpty()) {
            existingRow.mediaSourceGroup.setValue("Seleziona da esistenti");
            existingRow.selectedMediaField.setValue(data.getMedia());
            existingRow.selectedExistingMedia = data.getMedia();
            existingRow.updateMediaFieldVisibility();
        }


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
                addNewRow();
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


                    if ("Carica nuovo file".equals(row.mediaSourceGroup.getValue())) {

                        if (row.getUpload().getReceiver() instanceof MemoryBuffer buffer) {
                            if (buffer.getFileName() != null && !buffer.getFileName().isEmpty()) {
                                try (InputStream fileData = buffer.getInputStream()) {
                                    fileName = fileStorageService.storeFile(fileData, buffer.getFileName());
                                    hasValidData = true;
                                }
                            }
                        }
                    } else {

                        fileName = row.getSelectedMedia();
                        if (fileName != null && !fileName.isEmpty()) {
                            hasValidData = true;
                        }
                    }


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


                if (hasValidData) {
                    boolean success = esameRefertoService.saveEsamiReferti(scenarioId, esamiReferti);
                    if (success) {
                        Notification.show("Esami e referti salvati con successo", 3000, Notification.Position.TOP_CENTER)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    }
                } else {
                    logger.info("Nessun dato significativo da salvare per gli esami e referti dello scenario {}", scenarioId);
                }



                boolean isEditMode = "edit".equals(mode);
                if (!isEditMode) {

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
}
