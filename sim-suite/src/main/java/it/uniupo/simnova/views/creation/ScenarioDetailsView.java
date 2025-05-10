package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.*;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.MaterialeService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.support.AppHeader;
import it.uniupo.simnova.views.support.StyleApp;
import it.uniupo.simnova.views.support.detail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Vista per la visualizzazione dei dettagli di uno scenario.
 * <p>
 * Questa classe gestisce il caricamento e la visualizzazione dei dettagli di uno scenario specifico,
 * inclusi i parametri vitali, gli esami e i referti, e altre informazioni pertinenti.
 * </p>
 *
 * @author Alessandro Zappatore
 * @version 1.3 // Version incremented due to extensive logging addition
 */
@SuppressWarnings({"ThisExpressionReferencesGlobalObjectJS", "deprecation"})
@PageTitle("Dettagli Scenario")
@Route(value = "scenari")
public class ScenarioDetailsView extends Composite<VerticalLayout> implements HasUrlParameter<String>, BeforeEnterObserver {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;
    /**
     * Servizio per la gestione dei file.
     */
    private final FileStorageService fileStorageService;
    /**
     * Servizio per la gestione dei materiali necessari.
     */
    private final MaterialeService materialeNecessario;
    /**
     * ID dello scenario attualmente visualizzato.
     */
    private Integer scenarioId;
    /**
     * Oggetto Scenario caricato.
     */
    private Scenario scenario;
    /**
     * Flag per verificare se la vista è stata staccata.
     */
    private final AtomicBoolean detached = new AtomicBoolean(false);
    /**
     * Logger per la registrazione delle informazioni e degli errori.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScenarioDetailsView.class);
    /**
     * ExecutorService per gestire i task in background.
     */
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();


    /**
     * Costruttore della vista dei dettagli dello scenario.
     *
     * @param scenarioService servizio per la gestione degli scenari
     * @param fileStorageService servizio per la gestione dei file
     * @param materialeNecessario servizio per la gestione dei materiali
     */
    @Autowired
    public ScenarioDetailsView(ScenarioService scenarioService, FileStorageService fileStorageService, MaterialeService materialeNecessario) {
        this.scenarioService = scenarioService;
        this.fileStorageService = fileStorageService;
        this.materialeNecessario = materialeNecessario;
        // logger.info("[SDV_LOG_000] ScenarioDetailsView constructor called."); // Usually not needed for bean
    }

    /**
     * Imposta il parametro dell'URL per la vista.
     *
     * @param event     evento di navigazione
     * @param parameter parametro passato nell'URL
     */
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        logger.info("[SDV_LOG_001] setParameter - Received parameter: '{}'. Thread: {}", parameter, Thread.currentThread().getName());
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                logger.warn("[SDV_LOG_002] setParameter - Parameter is null or empty. Thread: {}", Thread.currentThread().getName());
                throw new NumberFormatException("Parameter is null or empty");
            }

            this.scenarioId = Integer.parseInt(parameter);
            logger.info("[SDV_LOG_003] setParameter - Parsed scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());

            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                logger.warn("[SDV_LOG_004] setParameter - Scenario ID {} is invalid or does not exist. Thread: {}", scenarioId, Thread.currentThread().getName());
                throw new NumberFormatException("Scenario ID " + scenarioId + " is invalid or does not exist");
            }
            logger.info("[SDV_LOG_005] setParameter - Scenario ID {} is valid. Thread: {}", scenarioId, Thread.currentThread().getName());
        } catch (NumberFormatException e) {
            logger.error("[SDV_LOG_006] setParameter - Invalid scenario ID: '{}'. Thread: {}. Error: {}", parameter, Thread.currentThread().getName(), e.getMessage());
            event.rerouteToError(NotFoundException.class, "ID scenario " + (parameter != null ? parameter : "null") + " non valido");
        }
    }

    /**
     * Metodo chiamato prima di entrare nella vista.
     * Carica i dati dello scenario e gestisce eventuali errori.
     *
     * @param event evento di navigazione
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        logger.info("[SDV_LOG_007] beforeEnter - Entered for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        if (scenarioId == null) {
            logger.warn("[SDV_LOG_008] beforeEnter - scenarioId is null. Navigating away. Thread: {}", Thread.currentThread().getName());
            Notification.show("ID scenario non specificato o non valido.", 3000, Position.MIDDLE).addThemeVariants(
                    NotificationVariant.LUMO_ERROR);
            if (UI.getCurrent() != null) {
                UI.getCurrent().navigate("scenari");
            }
            return;
        }
        // Apply base styling to the root layout of this Composite
        getContent().addClassName("scenario-details-view");
        getContent().setPadding(false);
        logger.info("[SDV_LOG_009] beforeEnter - Calling loadScenarioData for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        loadScenarioData();
    }

    /**
     * Metodo chiamato quando la vista viene staccata.
     * Ferma l'esecuzione dei task in background.
     *
     * @param detachEvent evento di distacco
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        detached.set(true);
        logger.info("[SDV_LOG_010] onDetach - View detached for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        if (!executorService.isShutdown()) {
            logger.info("[SDV_LOG_011] onDetach - Shutting down ExecutorService for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("[SDV_LOG_012] onDetach - ExecutorService did not terminate in time for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
                } else {
                    logger.info("[SDV_LOG_013] onDetach - ExecutorService terminated successfully for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
                }
            } catch (InterruptedException e) {
                logger.warn("[SDV_LOG_014] onDetach - Interrupted while waiting for ExecutorService to terminate for scenarioId: {}. Thread: {}. Error: {}", scenarioId, Thread.currentThread().getName(), e.getMessage());
                Thread.currentThread().interrupt();
            }
        } else {
            logger.info("[SDV_LOG_015] onDetach - ExecutorService already shutdown for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        }
    }

    /**
     * Carica i dati dello scenario in un thread separato.
     * Mostra una barra di progresso durante il caricamento e costruisce la UI.
     */
    private void loadScenarioData() {
        logger.info("[SDV_LOG_016] loadScenarioData - START for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        if (detached.get()) {
            logger.info("[SDV_LOG_017] loadScenarioData - View is detached for scenarioId: {}. Aborting. Thread: {}", scenarioId, Thread.currentThread().getName());
            return;
        }

        getContent().removeAll(); // Clear previous content
        final ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        getContent().add(progressBar);
        logger.info("[SDV_LOG_018] loadScenarioData - ProgressBar added for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());

        final UI ui = UI.getCurrent();
        if (ui == null) {
            logger.error("[SDV_LOG_019] loadScenarioData - UI instance is null for scenarioId: {}. Cannot proceed. Thread: {}", scenarioId, Thread.currentThread().getName());
            progressBar.setVisible(false);
            getContent().add(new Span("Errore: Impossibile caricare l'interfaccia utente."));
            return;
        }

        final AtomicBoolean loadingCompleted = new AtomicBoolean(false);
        logger.info("[SDV_LOG_020] loadScenarioData - Submitting main scenario loading task to ExecutorService for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());

        executorService.submit(() -> {
            final String mainLoadThreadName = Thread.currentThread().getName();
            logger.info("[SDV_LOG_021] [MAIN_LOAD_TASK] Task started for scenarioId: {}. Thread: {}", scenarioId, mainLoadThreadName);
            try {
                logger.info("[SDV_LOG_022] [MAIN_LOAD_TASK] Submitting CompletableFuture for scenarioService.getScenarioById for scenarioId: {}. Thread: {}", scenarioId, mainLoadThreadName);
                CompletableFuture<Scenario> future = CompletableFuture.supplyAsync(
                        () -> {
                            logger.info("[SDV_LOG_023] [MAIN_LOAD_TASK_CF] Calling scenarioService.getScenarioById for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
                            Scenario result = scenarioService.getScenarioById(scenarioId);
                            logger.info("[SDV_LOG_024] [MAIN_LOAD_TASK_CF] scenarioService.getScenarioById returned (null? {}) for scenarioId: {}. Thread: {}", (result == null), scenarioId, Thread.currentThread().getName());
                            return result;
                        },
                        executorService // Using the same executor, virtual threads are fine with this
                );

                Scenario loadedScenario;
                try {
                    logger.info("[SDV_LOG_025] [MAIN_LOAD_TASK] Waiting for future.get(15s) for scenarioId: {}. Thread: {}", scenarioId, mainLoadThreadName);
                    loadedScenario = future.get(15, TimeUnit.SECONDS);
                    logger.info("[SDV_LOG_026] [MAIN_LOAD_TASK] future.get() completed for scenarioId: {}. Loaded scenario is null? {}. Thread: {}", scenarioId, (loadedScenario == null), mainLoadThreadName);
                } catch (TimeoutException e) {
                    logger.error("[SDV_LOG_027] [MAIN_LOAD_TASK] Timeout during scenario loading for scenarioId: {}. Thread: {}. Error: {}", scenarioId, mainLoadThreadName, e.getMessage());
                    throw new RuntimeException("Timeout durante il caricamento dello scenario.");
                } catch (InterruptedException e) {
                    logger.warn("[SDV_LOG_028] [MAIN_LOAD_TASK] Scenario loading interrupted for scenarioId: {}. Thread: {}. Error: {}", scenarioId, mainLoadThreadName, e.getMessage());
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException e) {
                    logger.error("[SDV_LOG_029] [MAIN_LOAD_TASK] ExecutionException during scenario loading for scenarioId: {}. Thread: {}. Cause: {}", scenarioId, mainLoadThreadName, e.getCause() != null ? e.getCause().getMessage() : "null", e.getCause());
                    throw new RuntimeException("Errore nell'esecuzione del caricamento: " + (e.getCause() != null ? e.getCause().getMessage() : "Unknown error"), e.getCause());
                }

                if (detached.get() || ui.isClosing()) {
                    logger.info("[SDV_LOG_030] [MAIN_LOAD_TASK] View detached or UI closing during scenario data load for scenarioId: {}. Aborting UI update. Thread: {}", scenarioId, mainLoadThreadName);
                    return;
                }

                logger.info("[SDV_LOG_031] [MAIN_LOAD_TASK] Attempting ui.access for main UI update for scenarioId: {}. Thread: {}", scenarioId, mainLoadThreadName);
                ui.access(() -> {
                    final String uiAccessThreadName = Thread.currentThread().getName();
                    logger.info("[SDV_LOG_032] [MAIN_UI_ACCESS] Entered for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);
                    try {
                        if (loadedScenario == null) {
                            logger.warn("[SDV_LOG_033] [MAIN_UI_ACCESS] loadedScenario is null for scenarioId: {}. Navigating away. Thread: {}", scenarioId, uiAccessThreadName);
                            Notification.show("Scenario non trovato (ID: " + scenarioId + ")", 3000, Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            if (!ui.isClosing()) {
                                ui.navigate("scenari");
                            }
                            return;
                        }

                        this.scenario = loadedScenario;
                        logger.info("[SDV_LOG_034] [MAIN_UI_ACCESS] this.scenario assigned for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);

                        final UI currentUI = UI.getCurrent();
                        if (currentUI == null || currentUI.isClosing() || detached.get()) {
                            logger.warn("[SDV_LOG_035] [MAIN_UI_ACCESS] UI no longer available or view detached for scenarioId: {}. Aborting UI construction. Thread: {}", scenarioId, uiAccessThreadName);
                            return;
                        }

                        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());
                        mainLayout.removeAll();
                        logger.info("[SDV_LOG_036] [MAIN_UI_ACCESS] ProgressBar cleared for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);

                        logger.info("[SDV_LOG_037] [MAIN_UI_ACCESS] Creating header for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);
                        Component headerComponent = createHeaderComponent(currentUI);
                        mainLayout.add(headerComponent);

                        VerticalLayout contentLayout = StyleApp.getContentLayout();

                        logger.info("[SDV_LOG_038] [MAIN_UI_ACCESS] Adding scenario page header for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);
                        addScenarioPageHeader(contentLayout, currentUI);

                        logger.info("[SDV_LOG_039] [MAIN_UI_ACCESS] Adding scenario metadata for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);
                        addScenarioMetadata(contentLayout);

                        logger.info("[SDV_LOG_040] [MAIN_UI_ACCESS] Adding accordion to content for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);
                        addAccordionToContent(contentLayout, currentUI);

                        mainLayout.add(contentLayout);

                        logger.info("[SDV_LOG_041] [MAIN_UI_ACCESS] Creating footer for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);
                        Component footerComponent = createFooterComponent();
                        mainLayout.add(footerComponent);

                        logger.info("[SDV_LOG_042] [MAIN_UI_ACCESS] Main UI construction complete for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);

                    } catch (Exception e) {
                        logger.error("[SDV_LOG_043] [MAIN_UI_ACCESS] Critical error during UI update for scenarioId: {}. Thread: {}. Error: {}", scenarioId, uiAccessThreadName, e.getMessage(), e);
                        if (!ui.isClosing()) {
                            Notification.show("Errore nell'aggiornamento della vista. Si prega di riprovare.", 5000, Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            ui.navigate("scenari");
                        }
                    } finally {
                        loadingCompleted.set(true);
                        progressBar.setVisible(false);
                        logger.info("[SDV_LOG_044] [MAIN_UI_ACCESS] loadingCompleted set to true, ProgressBar hidden for scenarioId: {}. Thread: {}", scenarioId, uiAccessThreadName);
                    }
                }); // End of ui.access
                logger.info("[SDV_LOG_045] [MAIN_LOAD_TASK] ui.access call submitted/completed for scenarioId: {}. Thread: {}", scenarioId, mainLoadThreadName);
            } catch (Exception e) {
                logger.error("[SDV_LOG_046] [MAIN_LOAD_TASK] Severe error during scenario loading task for scenarioId: {}. Thread: {}. Error: {}", scenarioId, mainLoadThreadName, e.getMessage(), e);
                if (!detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        logger.error("[SDV_LOG_047] [MAIN_LOAD_TASK_ERROR_UI_ACCESS] Handling severe error in UI for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
                        loadingCompleted.set(true);
                        Notification.show("Errore grave nel caricamento dello scenario: " + e.getMessage(),
                                        5000, Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        progressBar.setVisible(false);
                        ui.navigate("scenari");
                    });
                }
            }
            logger.info("[SDV_LOG_048] [MAIN_LOAD_TASK] Task finished for scenarioId: {}. Thread: {}", scenarioId, mainLoadThreadName);
        }); // End of executorService.submit (main loading task)

        logger.info("[SDV_LOG_049] loadScenarioData - Submitting backup timeout task for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        executorService.submit(() -> {
            final String backupThreadName = Thread.currentThread().getName();
            logger.info("[SDV_LOG_050] [BACKUP_TIMEOUT_TASK] Task started for scenarioId: {}. Thread: {}", scenarioId, backupThreadName);
            try {
                Thread.sleep(20000); // Check after 20 seconds
                logger.info("[SDV_LOG_051] [BACKUP_TIMEOUT_TASK] 20s elapsed for scenarioId: {}. loadingCompleted: {}, detached: {}, ui.isClosing: {}. Thread: {}", scenarioId, loadingCompleted.get(), detached.get(), ui.isClosing(), backupThreadName);
                if (!loadingCompleted.get() && !detached.get() && !ui.isClosing()) {
                    logger.warn("[SDV_LOG_052] [BACKUP_TIMEOUT_TASK] Timeout condition met for scenarioId: {}. Attempting ui.access. Thread: {}", scenarioId, backupThreadName);
                    ui.access(() -> {
                        final String backupUIAccessThreadName = Thread.currentThread().getName();
                        logger.info("[SDV_LOG_053] [BACKUP_TIMEOUT_UI_ACCESS] Entered for scenarioId: {}. Thread: {}", scenarioId, backupUIAccessThreadName);
                        if (!loadingCompleted.get()) { // Double check inside UI access
                            logger.error("[SDV_LOG_054] [BACKUP_TIMEOUT_UI_ACCESS] Forced timeout for stuck loading (after 20s) for scenarioId: {}. Navigating away. Thread: {}", scenarioId, backupUIAccessThreadName);
                            Notification.show("Il caricamento sta impiegando troppo tempo. Riprova più tardi.",
                                            5000, Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            progressBar.setVisible(false);
                            ui.navigate("scenari");
                        } else {
                            logger.info("[SDV_LOG_055] [BACKUP_TIMEOUT_UI_ACCESS] Loading was completed in time for scenarioId: {}. No action needed. Thread: {}", scenarioId, backupUIAccessThreadName);
                        }
                    });
                }
            } catch (InterruptedException e) {
                logger.trace("[SDV_LOG_056] [BACKUP_TIMEOUT_TASK] Backup timeout thread interrupted for scenarioId: {}. Probable shutdown. Thread: {}. Error: {}", scenarioId, backupThreadName, e.getMessage());
                Thread.currentThread().interrupt();
            }
            logger.info("[SDV_LOG_057] [BACKUP_TIMEOUT_TASK] Task finished for scenarioId: {}. Thread: {}", scenarioId, backupThreadName);
        }); // End of executorService.submit (backup timeout task)
        logger.info("[SDV_LOG_058] loadScenarioData - END for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
    }


    /**
     * Crea il componente header della pagina.
     * @param ui L'istanza UI corrente.
     * @return Il componente HorizontalLayout dell'header.
     */
    private Component createHeaderComponent(UI ui) {
        // Minor logging here, focus is on async
        // logger.trace("[SDV_LOG_059] createHeaderComponent called for scenarioId: {}", scenarioId);
        AppHeader appHeader = new AppHeader(fileStorageService);
        Button backButton = StyleApp.getBackButton();
        backButton.addClickListener(e -> {
            if (ui != null && !ui.isClosing()) {
                ui.navigate("scenari");
            }
        });
        return StyleApp.getCustomHeader(backButton, appHeader);
    }

    /**
     * Crea il componente footer della pagina.
     * @return Il componente HorizontalLayout del footer.
     */
    private Component createFooterComponent() {
        // logger.trace("[SDV_LOG_060] createFooterComponent called for scenarioId: {}", scenarioId);
        return StyleApp.getFooterLayout(null);
    }

    /**
     * Aggiunge l'intestazione della pagina dei dettagli dello scenario (titolo sezione, bottone modifica).
     * @param layout Il layout a cui aggiungere l'intestazione.
     * @param ui L'istanza UI corrente.
     */
    private void addScenarioPageHeader(VerticalLayout layout, UI ui) {
        // logger.trace("[SDV_LOG_061] addScenarioPageHeader called for scenarioId: {}", scenarioId);
        Button editButton = StyleApp.getButton("Modifica", VaadinIcon.PENCIL, ButtonVariant.LUMO_TERTIARY, "--lumo-success-color");
        editButton.addClickListener(e -> {
            if (ui != null && !ui.isClosing() && scenario != null) {
                ui.navigate("modificaScenario/" + scenario.getId());
            }
        });

        HorizontalLayout editButtonContainer = new HorizontalLayout(editButton);
        editButtonContainer.setWidthFull();
        editButtonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Dettagli Scenario",
                "Visualizza i dettagli dello scenario selezionato",
                VaadinIcon.INFO_CIRCLE,
                "var(--lumo-primary-color)"
        );
        layout.add(editButtonContainer, headerSection);
    }

    /**
     * Aggiunge i metadati dello scenario (titolo, autori, info aggiuntive) al layout.
     * @param layout Il layout a cui aggiungere i metadati.
     */
    private void addScenarioMetadata(VerticalLayout layout) {
        // logger.trace("[SDV_LOG_062] addScenarioMetadata called for scenarioId: {}", scenarioId);
        if (scenario == null) {
            // Existing warn log is good
            logger.warn("[SDV_LOG_063] addScenarioMetadata called but scenario is null. ScenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
            layout.add(new Span("Dati scenario non ancora disponibili."));
            return;
        }
        // ... rest of the method
        Div titleContainer = new Div();
        titleContainer.setWidthFull();
        titleContainer.getStyle()
                .set("max-width", "800px")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-left", "auto")
                .set("margin-right", "auto")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("transition", "box-shadow 0.3s ease-in-out");

        titleContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        H2 title = new H2(scenario.getTitolo());
        title.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.XSMALL,
                LumoUtility.FontSize.XXLARGE
        );
        title.getStyle()
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "600")
                .set("letter-spacing", "0.5px");

        Paragraph authors = new Paragraph("Autori: " + scenario.getAutori());
        authors.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Top.NONE,
                LumoUtility.Margin.Bottom.NONE,
                LumoUtility.FontSize.XLARGE
        );
        titleContainer.add(title, authors);

        Component subtitle = InfoSupport.getInfo(scenario);

        layout.add(titleContainer, subtitle);
    }

    /**
     * Crea e aggiunge l'accordion con tutti i suoi pannelli al layout specificato.
     * @param layout Il layout a cui aggiungere l'accordion.
     * @param ui L'istanza UI corrente.
     */
    private void addAccordionToContent(VerticalLayout layout, UI ui) {
        logger.info("[SDV_LOG_064] addAccordionToContent - START for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        if (scenario == null) {
            // Existing warn log is good
            logger.warn("[SDV_LOG_065] addAccordionToContent called but scenario is null. ScenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
            layout.add(new Span("Contenuto dettagliato non ancora disponibile."));
            return;
        }
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        logger.info("[SDV_LOG_066] addAccordionToContent - Creating GeneralInfoPanel for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        createAndAddGeneralInfoPanel(accordion, ui);
        logger.info("[SDV_LOG_067] addAccordionToContent - Creating PatientStatePanel for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        createAndAddPatientStatePanel(accordion, ui);
        logger.info("[SDV_LOG_068] addAccordionToContent - Creating ExamsAndReportsPanel for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        createAndAddExamsAndReportsPanel(accordion, ui);
        logger.info("[SDV_LOG_069] addAccordionToContent - Creating TimelinePanel for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        createAndAddTimelinePanel(accordion, ui);
        logger.info("[SDV_LOG_070] addAccordionToContent - Creating ScreenplayPanel for scenarioId: {}. Thread: {}", scenarioId, Thread.currentThread().getName());
        createAndAddScreenplayPanel(accordion, ui);

        if (accordion.getChildren().findFirst().isPresent()) {
            accordion.open(0);
        }
        layout.add(accordion);
        logger.info("[SDV_LOG_071] addAccordionToContent - END for scenarioId: {}. Accordion added. Thread: {}", scenarioId, Thread.currentThread().getName());
    }

    /**
     * Crea e aggiunge il pannello "Informazioni Generali" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddGeneralInfoPanel(Accordion accordion, UI ui) {
        String panelName = "Informazioni Generali";
        logger.info("[SDV_LOG_072] createAndAddPanel - START for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        AccordionPanel panel = accordion.add(panelName, createLoadingPlaceholder("Caricamento " + panelName + "..."));
        styleAccordionPanelSummary(panel);
        logger.info("[SDV_LOG_073] createAndAddPanel - Calling loadGeneralSupportDataAsync for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        loadGeneralSupportDataAsync(ui, panel, scenario, scenarioService, materialeNecessario);
    }

    /**
     * Crea e aggiunge il pannello "Stato Paziente" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddPatientStatePanel(Accordion accordion, UI ui) {
        String panelName = "Stato Paziente";
        logger.info("[SDV_LOG_074] createAndAddPanel - START for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        AccordionPanel panel = accordion.add(panelName, createLoadingPlaceholder("Caricamento " + panelName + "..."));
        styleAccordionPanelSummary(panel);
        logger.info("[SDV_LOG_075] createAndAddPanel - Calling loadPatientStateDataAsync for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        loadPatientStateDataAsync(ui, panel, scenario.getId());
    }

    /**
     * Crea e aggiunge il pannello "Esami e Referti" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddExamsAndReportsPanel(Accordion accordion, UI ui) {
        String panelName = "Esami e Referti";
        logger.info("[SDV_LOG_076] createAndAddPanel - START for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        AccordionPanel panel = accordion.add(panelName, createLoadingPlaceholder("Caricamento " + panelName + "..."));
        styleAccordionPanelSummary(panel);
        logger.info("[SDV_LOG_077] createAndAddPanel - Calling loadExamsDataAsync for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        loadExamsDataAsync(ui, panel, scenario.getId());
    }

    /**
     * Crea e aggiunge il pannello "Timeline" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddTimelinePanel(Accordion accordion, UI ui) {
        String panelName = "Timeline";
        logger.info("[SDV_LOG_078] createAndAddPanel - START for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        AccordionPanel panel = accordion.add(panelName, createLoadingPlaceholder("Caricamento " + panelName + "..."));
        styleAccordionPanelSummary(panel);
        logger.info("[SDV_LOG_079] createAndAddPanel - Calling loadTimelineDataAsync for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        loadTimelineDataAsync(ui, panel, scenario.getId());
    }

    /**
     * Crea e aggiunge il pannello "Sceneggiatura" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddScreenplayPanel(Accordion accordion, UI ui) {
        String panelName = "Sceneggiatura";
        logger.info("[SDV_LOG_080] createAndAddPanel - START for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        AccordionPanel panel = accordion.add(panelName, createLoadingPlaceholder("Caricamento " + panelName + "..."));
        styleAccordionPanelSummary(panel);
        logger.info("[SDV_LOG_081] createAndAddPanel - Calling loadSceneggiaturaDataAsync for Panel: '{}', scenarioId: {}. Thread: {}", panelName, scenarioId, Thread.currentThread().getName());
        loadSceneggiaturaDataAsync(ui, panel, scenario.getId());
    }


    private VerticalLayout createLoadingPlaceholder(String message) {
        // logger.trace("[SDV_LOG_082] createLoadingPlaceholder called with message: '{}', for scenarioId: {}", message, scenarioId);
        Span loadingText = new Span(message);
        loadingText.getStyle().set("margin-bottom", "var(--lumo-space-s)");
        ProgressBar progressBarPlaceholder = new ProgressBar();
        progressBarPlaceholder.setIndeterminate(true);
        VerticalLayout placeholder = new VerticalLayout(loadingText, progressBarPlaceholder);
        placeholder.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        placeholder.setPadding(true);
        placeholder.getStyle().set("min-height", "100px");
        return placeholder;
    }

    private void styleAccordionPanelSummary(AccordionPanel panel) {
        // logger.trace("[SDV_LOG_083] styleAccordionPanelSummary called for panel: '{}', scenarioId: {}", panel.getSummaryText(), scenarioId);
        if (panel != null && panel.getSummary() != null) {
            panel.getSummary().getStyle()
                    .set("font-size", "var(--lumo-font-size-l)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("text-align", "left")
                    .set("font-weight", "500");

            panel.getSummary().getStyle().set("transition", "background-color 0.2s ease-in-out, color 0.2s ease-in-out");
            panel.getSummary().getElement().executeJs(
                    "const summary = this;" +
                            "summary.addEventListener('mouseover', function() { " +
                            "  summary.style.backgroundColor = 'var(--lumo-primary-color-10pct)';" +
                            "  summary.style.color = 'var(--lumo-primary-text-color)';" +
                            "});" +
                            "summary.addEventListener('mouseout', function() { " +
                            "  summary.style.backgroundColor = 'transparent';" +
                            "  summary.style.color = 'var(--lumo-body-text-color)';" +
                            "});"
            );
        }
    }

    // ASYNCHRONOUS DATA LOADING METHODS FOR ACCORDION PANELS

    private void loadGeneralSupportDataAsync(UI ui,
                                             AccordionPanel panelToUpdate,
                                             Scenario currentScenario,
                                             ScenarioService scService,
                                             MaterialeService matService) {
        String panelName = "Informazioni Generali";
        logger.info("[SDV_LOG_084] [PANEL_LOAD_ASYNC] Method START for Panel: '{}', scenarioId: {}. Submitting task. Thread: {}", panelName, (currentScenario != null ? currentScenario.getId() : "NULL_SCENARIO"), Thread.currentThread().getName());
        executorService.submit(() -> {
            final String panelLoadThreadName = Thread.currentThread().getName();
            final Integer currentId = (currentScenario != null ? currentScenario.getId() : null);
            logger.info("[SDV_LOG_085] [PANEL_LOAD_TASK:{}] Task started for scenarioId: {}. Thread: {}", panelName, currentId, panelLoadThreadName);
            try {
                if (currentScenario == null) {
                    logger.warn("[SDV_LOG_086] [PANEL_LOAD_TASK:{}] currentScenario is null. Cannot load data for scenarioId: {}. Thread: {}", panelName, currentId, panelLoadThreadName);
                    if (!detached.get() && ui != null && !ui.isClosing()) {
                        ui.access(() -> panelToUpdate.setContent(new Span("Errore: Dati scenario non disponibili (" + panelName + ").")));
                    }
                    return;
                }
                logger.info("[SDV_LOG_087] [PANEL_LOAD_TASK:{}] Fetching data... isPediatric, infoGenitore, materiali, azioniChiave for scenarioId: {}. Thread: {}", panelName, currentId, panelLoadThreadName);
                boolean isPediatric = scService.isPediatric(currentId);
                String infoGenitore = isPediatric ? currentScenario.getInfoGenitore() : null;
                String materiali = matService.toStringAllMaterialsByScenarioId(currentId);
                List<String> azioniChiave = scService.getNomiAzioniChiaveByScenarioId(currentId);
                logger.info("[SDV_LOG_088] [PANEL_LOAD_TASK:{}] Data fetched for scenarioId: {}. isPediatric: {}, azioniChiave count: {}. Ready for UI. Thread: {}", panelName, currentId, isPediatric, azioniChiave.size(), panelLoadThreadName);

                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_089] [PANEL_LOAD_TASK:{}] View detached or UI closing for scenarioId: {}. UI update cancelled. Thread: {}", panelName, currentId, panelLoadThreadName);
                    return;
                }
                logger.info("[SDV_LOG_090] [PANEL_LOAD_TASK:{}] Attempting ui.access for scenarioId: {}. Thread: {}", panelName, currentId, panelLoadThreadName);
                ui.access(() -> {
                    final String panelUIAccessThreadName = Thread.currentThread().getName();
                    logger.info("[SDV_LOG_091] [PANEL_UI_ACCESS:{}] Entered for scenarioId: {}. Thread: {}", panelName, currentId, panelUIAccessThreadName);
                    try {
                        Component content = GeneralSupport.createOverviewContentWithData(
                                currentScenario, isPediatric, infoGenitore, materiali, azioniChiave
                        );
                        // logger.error("Azioni chiave: {}", azioniChiave.toString()); // Already present, changed to debug
                        logger.debug("[SDV_LOG_092] [PANEL_UI_ACCESS:{}] Azioni chiave: {} for scenarioId: {}. Thread: {}", panelName, azioniChiave.toString(), currentId, panelUIAccessThreadName);
                        panelToUpdate.setContent(content);
                        logger.info("[SDV_LOG_093] [PANEL_UI_ACCESS:{}] UI updated successfully for scenarioId: {}. Thread: {}", panelName, currentId, panelUIAccessThreadName);
                    } catch (Exception e_ui) {
                        logger.error("[SDV_LOG_094] [PANEL_UI_ACCESS:{}] Error during UI update for scenarioId: {}. Thread: {}. Error: {}", panelName, currentId, panelUIAccessThreadName, e_ui.getMessage(), e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("[SDV_LOG_095] [PANEL_LOAD_TASK:{}] Error in background task for scenarioId: {}. Thread: {}. Error: {}", panelName, currentId, panelLoadThreadName, e_task.getMessage(), e_task);
                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_096] [PANEL_LOAD_TASK:{}] View detached or UI closing after error for scenarioId: {}. No UI error update. Thread: {}", panelName, currentId, panelLoadThreadName);
                    return;
                }
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
            logger.info("[SDV_LOG_097] [PANEL_LOAD_TASK:{}] Task finished for scenarioId: {}. Thread: {}", panelName, currentId, panelLoadThreadName);
        });
    }

    private void loadPatientStateDataAsync(UI ui, AccordionPanel panelToUpdate, int currentScenarioId) {
        String panelName = "Stato Paziente";
        logger.info("[SDV_LOG_098] [PANEL_LOAD_ASYNC] Method START for Panel: '{}', scenarioId: {}. Submitting task. Thread: {}", panelName, currentScenarioId, Thread.currentThread().getName());
        executorService.submit(() -> {
            final String panelLoadThreadName = Thread.currentThread().getName();
            logger.info("[SDV_LOG_099] [PANEL_LOAD_TASK:{}] Task started for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
            try {
                logger.info("[SDV_LOG_100] [PANEL_LOAD_TASK:{}] Fetching PazienteT0 and EsameFisico for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                PazienteT0 pazienteT0 = scenarioService.getPazienteT0ById(currentScenarioId);
                EsameFisico esameFisico = scenarioService.getEsameFisicoById(currentScenarioId);
                logger.info("[SDV_LOG_101] [PANEL_LOAD_TASK:{}] Data fetched for scenarioId: {}. PazienteT0 null? {}, EsameFisico null? {}. Ready for UI. Thread: {}",
                        panelName, currentScenarioId, (pazienteT0 == null), (esameFisico == null), panelLoadThreadName);

                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_102] [PANEL_LOAD_TASK:{}] View detached or UI closing for scenarioId: {}. UI update cancelled. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                    return;
                }
                logger.info("[SDV_LOG_103] [PANEL_LOAD_TASK:{}] Attempting ui.access for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                ui.access(() -> {
                    final String panelUIAccessThreadName = Thread.currentThread().getName();
                    logger.info("[SDV_LOG_104] [PANEL_UI_ACCESS:{}] Entered for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                    try {
                        Component content = PatientT0Support.createPatientContent(pazienteT0, esameFisico, scenarioId); // scenarioId here is the member variable, consistent
                        panelToUpdate.setContent(content);
                        logger.info("[SDV_LOG_105] [PANEL_UI_ACCESS:{}] UI updated successfully for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                    } catch (Exception e_ui) {
                        logger.error("[SDV_LOG_106] [PANEL_UI_ACCESS:{}] Error during UI update for scenarioId: {}. Thread: {}. Error: {}", panelName, currentScenarioId, panelUIAccessThreadName, e_ui.getMessage(), e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("[SDV_LOG_107] [PANEL_LOAD_TASK:{}] Error in background task for scenarioId: {}. Thread: {}. Error: {}", panelName, currentScenarioId, panelLoadThreadName, e_task.getMessage(), e_task);
                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_108] [PANEL_LOAD_TASK:{}] View detached or UI closing after error for scenarioId: {}. No UI error update. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                    return;
                }
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
            logger.info("[SDV_LOG_109] [PANEL_LOAD_TASK:{}] Task finished for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
        });
    }

    private void loadExamsDataAsync(UI ui, AccordionPanel panelToUpdate, int currentScenarioId) {
        String panelName = "Esami e Referti";
        logger.info("[SDV_LOG_110] [PANEL_LOAD_ASYNC] Method START for Panel: '{}', scenarioId: {}. Submitting task. Thread: {}", panelName, currentScenarioId, Thread.currentThread().getName());
        executorService.submit(() -> {
            final String panelLoadThreadName = Thread.currentThread().getName();
            logger.info("[SDV_LOG_111] [PANEL_LOAD_TASK:{}] Task started for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
            try {
                logger.info("[SDV_LOG_112] [PANEL_LOAD_TASK:{}] Fetching EsamiReferti for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                List<EsameReferto> esamiReferti = scenarioService.getEsamiRefertiByScenarioId(currentScenarioId);
                logger.info("[SDV_LOG_113] [PANEL_LOAD_TASK:{}] Data fetched for scenarioId: {}. EsamiReferti count: {}. Ready for UI. Thread: {}",
                        panelName, currentScenarioId, (esamiReferti != null ? esamiReferti.size() : "null list"), panelLoadThreadName);

                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_114] [PANEL_LOAD_TASK:{}] View detached or UI closing for scenarioId: {}. UI update cancelled. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                    return;
                }
                logger.info("[SDV_LOG_115] [PANEL_LOAD_TASK:{}] Attempting ui.access for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                ui.access(() -> {
                    final String panelUIAccessThreadName = Thread.currentThread().getName();
                    logger.info("[SDV_LOG_116] [PANEL_UI_ACCESS:{}] Entered for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                    try {
                        Component content = ExamSupport.createExamsContent(esamiReferti);
                        panelToUpdate.setContent(content);
                        logger.info("[SDV_LOG_117] [PANEL_UI_ACCESS:{}] UI updated successfully for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                    } catch (Exception e_ui) {
                        logger.error("[SDV_LOG_118] [PANEL_UI_ACCESS:{}] Error during UI update for scenarioId: {}. Thread: {}. Error: {}", panelName, currentScenarioId, panelUIAccessThreadName, e_ui.getMessage(), e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("[SDV_LOG_119] [PANEL_LOAD_TASK:{}] Error in background task for scenarioId: {}. Thread: {}. Error: {}", panelName, currentScenarioId, panelLoadThreadName, e_task.getMessage(), e_task);
                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_120] [PANEL_LOAD_TASK:{}] View detached or UI closing after error for scenarioId: {}. No UI error update. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                    return;
                }
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
            logger.info("[SDV_LOG_121] [PANEL_LOAD_TASK:{}] Task finished for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
        });
    }

    private void loadTimelineDataAsync(UI ui,
                                       AccordionPanel panelToUpdate,
                                       int currentScenarioId) {
        String panelName = "Timeline";
        logger.info("[SDV_LOG_122] [PANEL_LOAD_ASYNC] Method START for Panel: '{}', scenarioId: {}. Submitting task. Thread: {}", panelName, currentScenarioId, Thread.currentThread().getName());
        executorService.submit(() -> {
            final String panelLoadThreadName = Thread.currentThread().getName();
            logger.info("[SDV_LOG_123] [PANEL_LOAD_TASK:{}] Task started for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
            try {
                logger.info("[SDV_LOG_124] [PANEL_LOAD_TASK:{}] Fetching Tempi for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                List<Tempo> tempi = scenarioService.getTempiByScenarioId(currentScenarioId);
                logger.info("[SDV_LOG_125] [PANEL_LOAD_TASK:{}] Data fetched for scenarioId: {}. Tempi count: {}. Ready for UI. Thread: {}",
                        panelName, currentScenarioId, (tempi != null ? tempi.size() : "null list"), panelLoadThreadName);

                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_126] [PANEL_LOAD_TASK:{}] View detached or UI closing for scenarioId: {}. UI update cancelled. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                    return;
                }
                logger.info("[SDV_LOG_127] [PANEL_LOAD_TASK:{}] Attempting ui.access for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                ui.access(() -> {
                    final String panelUIAccessThreadName = Thread.currentThread().getName();
                    logger.info("[SDV_LOG_128] [PANEL_UI_ACCESS:{}] Entered for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                    try {
                        if (tempi != null && !tempi.isEmpty()) {
                            logger.info("[SDV_LOG_129] [PANEL_UI_ACCESS:{}] Creating timeline content with {} tempi for scenarioId: {}. Thread: {}", panelName, tempi.size(), currentScenarioId, panelUIAccessThreadName);
                            panelToUpdate.setContent(TimesSupport.createTimelineContent(tempi, scenarioId)); // scenarioId here is member var
                        } else {
                            logger.info("[SDV_LOG_130] [PANEL_UI_ACCESS:{}] No timeline data for scenarioId: {}. Displaying 'Nessuna timeline'. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                            panelToUpdate.setContent(new Span("Nessuna timeline disponibile per questo scenario."));
                        }
                        logger.info("[SDV_LOG_131] [PANEL_UI_ACCESS:{}] UI updated successfully for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                    } catch (Exception e_ui) {
                        logger.error("[SDV_LOG_132] [PANEL_UI_ACCESS:{}] Error during UI update for scenarioId: {}. Thread: {}. Error: {}", panelName, currentScenarioId, panelUIAccessThreadName, e_ui.getMessage(), e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("[SDV_LOG_133] [PANEL_LOAD_TASK:{}] Error in background task for scenarioId: {}. Thread: {}. Error: {}", panelName, currentScenarioId, panelLoadThreadName, e_task.getMessage(), e_task);
                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_134] [PANEL_LOAD_TASK:{}] View detached or UI closing after error for scenarioId: {}. No UI error update. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                    return;
                }
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
            logger.info("[SDV_LOG_135] [PANEL_LOAD_TASK:{}] Task finished for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
        });
    }

    private void loadSceneggiaturaDataAsync(UI ui,
                                            AccordionPanel panelToUpdate,
                                            int currentScenarioId) {
        String panelName = "Sceneggiatura";
        logger.info("[SDV_LOG_136] [PANEL_LOAD_ASYNC] Method START for Panel: '{}', scenarioId: {}. Submitting task. Thread: {}", panelName, currentScenarioId, Thread.currentThread().getName());
        executorService.submit(() -> {
            final String panelLoadThreadName = Thread.currentThread().getName();
            logger.info("[SDV_LOG_137] [PANEL_LOAD_TASK:{}] Task started for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
            try {
                logger.info("[SDV_LOG_138] [PANEL_LOAD_TASK:{}] Fetching scenario type for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                String scenarioType = scenarioService.getScenarioType(currentScenarioId);
                logger.info("[SDV_LOG_139] [PANEL_LOAD_TASK:{}] Scenario type loaded: '{}' for scenarioId: {}. Thread: {}", panelName, scenarioType, currentScenarioId, panelLoadThreadName);

                String sceneggiatura = null;
                if ("Patient Simulated Scenario".equalsIgnoreCase(scenarioType)) {
                    logger.info("[SDV_LOG_140] [PANEL_LOAD_TASK:{}] Fetching sceneggiatura for 'Patient Simulated Scenario' for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                    sceneggiatura = ScenarioService.getSceneggiatura(currentScenarioId); // Assuming this is a static call as per original code
                    logger.info("[SDV_LOG_141] [PANEL_LOAD_TASK:{}] Sceneggiatura fetched (null? {}) for scenarioId: {}. Thread: {}", panelName, (sceneggiatura == null), currentScenarioId, panelLoadThreadName);
                }

                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_142] [PANEL_LOAD_TASK:{}] View detached or UI closing for scenarioId: {}. UI update cancelled. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                    return;
                }

                final String finalSceneggiatura = sceneggiatura;
                final String finalScenarioType = scenarioType;
                logger.info("[SDV_LOG_143] [PANEL_LOAD_TASK:{}] Attempting ui.access for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                ui.access(() -> {
                    final String panelUIAccessThreadName = Thread.currentThread().getName();
                    logger.info("[SDV_LOG_144] [PANEL_UI_ACCESS:{}] Entered for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                    try {
                        if ("Patient Simulated Scenario".equalsIgnoreCase(finalScenarioType) && finalSceneggiatura != null && !finalSceneggiatura.isBlank()) {
                            logger.info("[SDV_LOG_145] [PANEL_UI_ACCESS:{}] Creating sceneggiatura content for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                            panelToUpdate.setContent(SceneggiaturaSupport.createSceneggiaturaContent(finalSceneggiatura));
                        } else {
                            logger.info("[SDV_LOG_146] [PANEL_UI_ACCESS:{}] No applicable sceneggiatura for scenarioId: {}. Type: '{}'. Displaying message. Thread: {}", panelName, currentScenarioId, finalScenarioType, panelUIAccessThreadName);
                            panelToUpdate.setContent(new Span("Nessuna sceneggiatura disponibile per questo tipo di scenario o per questo scenario specifico."));
                        }
                        logger.info("[SDV_LOG_147] [PANEL_UI_ACCESS:{}] UI updated successfully for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelUIAccessThreadName);
                    } catch (Exception e_ui) {
                        logger.error("[SDV_LOG_148] [PANEL_UI_ACCESS:{}] Error during UI update for scenarioId: {}. Thread: {}. Error: {}", panelName, currentScenarioId, panelUIAccessThreadName, e_ui.getMessage(), e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("[SDV_LOG_149] [PANEL_LOAD_TASK:{}] Error in background task for scenarioId: {}. Thread: {}. Error: {}", panelName, currentScenarioId, panelLoadThreadName, e_task.getMessage(), e_task);
                if (detached.get() || ui.isClosing()) {
                    logger.warn("[SDV_LOG_150] [PANEL_LOAD_TASK:{}] View detached or UI closing after error for scenarioId: {}. No UI error update. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
                    return;
                }
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
            logger.info("[SDV_LOG_151] [PANEL_LOAD_TASK:{}] Task finished for scenarioId: {}. Thread: {}", panelName, currentScenarioId, panelLoadThreadName);
        });
    }
}