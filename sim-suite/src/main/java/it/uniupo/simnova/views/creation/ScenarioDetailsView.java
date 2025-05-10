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
 * @version 1.2 // Version incremented due to refactoring initView logic
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
    }

    /**
     * Imposta il parametro dell'URL per la vista.
     *
     * @param event     evento di navigazione
     * @param parameter parametro passato nell'URL
     */
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        try {
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new NumberFormatException("Parameter is null or empty");
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException("Scenario ID " + scenarioId + " is invalid or does not exist");
            }
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
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
        if (scenarioId == null) {
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
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("ExecutorService did not terminate in time for scenarioId: {}", scenarioId);
                }
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for ExecutorService to terminate for scenarioId: {}", scenarioId, e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Carica i dati dello scenario in un thread separato.
     * Mostra una barra di progresso durante il caricamento e costruisce la UI.
     */
    private void loadScenarioData() {
        if (detached.get()) {
            logger.info("loadScenarioData called on a detached view for scenarioId: {}. Aborting.", scenarioId);
            return;
        }

        getContent().removeAll(); // Clear previous content
        final ProgressBar progressBar = new ProgressBar(); // final to be accessible in lambda
        progressBar.setIndeterminate(true);
        getContent().add(progressBar);

        final UI ui = UI.getCurrent(); // Capture UI instance for async task
        if (ui == null) {
            logger.error("UI instance is null in loadScenarioData for scenarioId: {}. Cannot proceed.", scenarioId);
            progressBar.setVisible(false);
            getContent().add(new Span("Errore: Impossibile caricare l'interfaccia utente."));
            return;
        }

        final AtomicBoolean loadingCompleted = new AtomicBoolean(false);

        executorService.submit(() -> {
            try {
                CompletableFuture<Scenario> future = CompletableFuture.supplyAsync(
                        () -> scenarioService.getScenarioById(scenarioId),
                        executorService
                );

                Scenario loadedScenario;
                try {
                    loadedScenario = future.get(15, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    logger.error("Timeout durante il caricamento dello scenario {}", scenarioId, e);
                    throw new RuntimeException("Timeout durante il caricamento dello scenario.");
                } catch (InterruptedException e) {
                    logger.warn("Caricamento scenario {} interrotto.", scenarioId, e);
                    Thread.currentThread().interrupt();
                    return;
                } catch (ExecutionException e) {
                    logger.error("Errore durante l'esecuzione del caricamento scenario {}", scenarioId, e.getCause());
                    throw new RuntimeException("Errore nell'esecuzione del caricamento: " + e.getCause().getMessage(), e.getCause());
                }

                if (detached.get() || ui.isClosing()) {
                    logger.info("View detached or UI closing during scenario data load for ID: {}", scenarioId);
                    return;
                }

                // UI Update Task
                ui.access(() -> {
                    try {
                        if (loadedScenario == null) {
                            Notification.show("Scenario non trovato (ID: " + scenarioId + ")", 3000, Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            if (!ui.isClosing()) { // Check ui before navigating
                                ui.navigate("scenari");
                            }
                            return; // Exit if scenario is null
                        }

                        this.scenario = loadedScenario; // Assign the loaded scenario

                        // Ensure UI is still valid before proceeding with complex UI construction
                        final UI currentUI = UI.getCurrent();
                        if (currentUI == null || currentUI.isClosing() || detached.get()) {
                            logger.warn("UI access: UI no longer available or view detached for scenarioId: {}. Aborting UI construction.", scenarioId);
                            return;
                        }

                        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent()); // This is getContent()
                        mainLayout.removeAll(); // Clear the progress bar

                        // 1. Create and add Header
                        Component headerComponent = createHeaderComponent(currentUI);
                        mainLayout.add(headerComponent);

                        // 2. Create Main Content Layout
                        VerticalLayout contentLayout = StyleApp.getContentLayout(); // This is a new VerticalLayout for the main content area

                        // 2a. Add Scenario Page Header (Title, Edit button) to contentLayout
                        addScenarioPageHeader(contentLayout, currentUI);

                        // 2b. Add Scenario Metadata (Title, Authors, Pathology etc.) to contentLayout
                        addScenarioMetadata(contentLayout); // Uses this.scenario

                        // 2c. Add Accordion with dynamic sections to contentLayout
                        addAccordionToContent(contentLayout, currentUI); // This will trigger async loading for accordion panels

                        // Add the fully constructed contentLayout to the mainLayout
                        mainLayout.add(contentLayout);

                        // 3. Create and add Footer
                        Component footerComponent = createFooterComponent();
                        mainLayout.add(footerComponent);

                        // The base class "scenario-details-view" and padding(false) are already set on getContent() (mainLayout)
                        // in beforeEnter(), so no need to re-apply here.

                    } catch (Exception e) {
                        logger.error("Errore critico durante l'aggiornamento UI per scenarioId {}: {}", scenarioId, e.getMessage(), e);
                        if (!ui.isClosing()) {
                            Notification.show("Errore nell'aggiornamento della vista. Si prega di riprovare.", 5000, Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            ui.navigate("scenari");
                        }
                    } finally {
                        loadingCompleted.set(true);
                        progressBar.setVisible(false); // Ensure progress bar is hidden
                    }
                }); // End of ui.access
            } catch (Exception e) {
                logger.error("Errore grave durante il task di caricamento dello scenario {}: {}", scenarioId, e.getMessage(), e);
                if (!detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        loadingCompleted.set(true); // Ensure flag is set
                        Notification.show("Errore grave nel caricamento dello scenario: " + e.getMessage(),
                                        5000, Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        progressBar.setVisible(false);
                        ui.navigate("scenari");
                    });
                }
            }
        }); // End of executorService.submit (main loading task)

        // Backup timeout task
        executorService.submit(() -> {
            try {
                Thread.sleep(20000); // Check after 20 seconds
                if (!loadingCompleted.get() && !detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        if (!loadingCompleted.get()) { // Double check inside UI access
                            logger.error("Forzato timeout per caricamento bloccato (dopo 20s), scenario {}", scenarioId);
                            Notification.show("Il caricamento sta impiegando troppo tempo. Riprova più tardi.",
                                            5000, Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            progressBar.setVisible(false);
                            ui.navigate("scenari");
                        }
                    });
                }
            } catch (InterruptedException e) {
                logger.trace("Backup timeout thread interrotto per scenario {}, probabile shutdown.", scenarioId);
                Thread.currentThread().interrupt();
            }
        }); // End of executorService.submit (backup timeout task)
    }


    /**
     * Crea il componente header della pagina.
     * @param ui L'istanza UI corrente.
     * @return Il componente HorizontalLayout dell'header.
     */
    private Component createHeaderComponent(UI ui) {
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
        return StyleApp.getFooterLayout(null);
    }

    /**
     * Aggiunge l'intestazione della pagina dei dettagli dello scenario (titolo sezione, bottone modifica).
     * @param layout Il layout a cui aggiungere l'intestazione.
     * @param ui L'istanza UI corrente.
     */
    private void addScenarioPageHeader(VerticalLayout layout, UI ui) {
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
        // This check is crucial as 'scenario' is set asynchronously.
        if (scenario == null) {
            logger.warn("addScenarioMetadata called but scenario is null. ScenarioId: {}", scenarioId);
            layout.add(new Span("Dati scenario non ancora disponibili."));
            return;
        }

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
        // This check is crucial as 'scenario' is set asynchronously.
        if (scenario == null) {
            logger.warn("addAccordionToContent called but scenario is null. ScenarioId: {}", scenarioId);
            layout.add(new Span("Contenuto dettagliato non ancora disponibile."));
            return;
        }
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        createAndAddGeneralInfoPanel(accordion, ui);
        createAndAddPatientStatePanel(accordion, ui);
        createAndAddExamsAndReportsPanel(accordion, ui);
        createAndAddTimelinePanel(accordion, ui);
        createAndAddScreenplayPanel(accordion, ui);

        if (accordion.getChildren().findFirst().isPresent()) {
            accordion.open(0);
        }
        layout.add(accordion);
    }

    /**
     * Crea e aggiunge il pannello "Informazioni Generali" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddGeneralInfoPanel(Accordion accordion, UI ui) {
        AccordionPanel panel = accordion.add("Informazioni Generali", createLoadingPlaceholder("Caricamento Informazioni Generali..."));
        styleAccordionPanelSummary(panel);
        // scenario object should be available here as addAccordionToContent checks for it
        loadGeneralSupportDataAsync(ui, panel, scenario, scenarioService, materialeNecessario);
    }

    /**
     * Crea e aggiunge il pannello "Stato Paziente" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddPatientStatePanel(Accordion accordion, UI ui) {
        AccordionPanel panel = accordion.add("Stato Paziente", createLoadingPlaceholder("Caricamento Stato Paziente..."));
        styleAccordionPanelSummary(panel);
        loadPatientStateDataAsync(ui, panel, scenario.getId());
    }

    /**
     * Crea e aggiunge il pannello "Esami e Referti" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddExamsAndReportsPanel(Accordion accordion, UI ui) {
        AccordionPanel panel = accordion.add("Esami e Referti", createLoadingPlaceholder("Caricamento Esami e Referti..."));
        styleAccordionPanelSummary(panel);
        loadExamsDataAsync(ui, panel, scenario.getId());
    }

    /**
     * Crea e aggiunge il pannello "Timeline" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddTimelinePanel(Accordion accordion, UI ui) {
        AccordionPanel panel = accordion.add("Timeline", createLoadingPlaceholder("Caricamento Timeline..."));
        styleAccordionPanelSummary(panel);
        loadTimelineDataAsync(ui, panel, scenario.getId());
    }

    /**
     * Crea e aggiunge il pannello "Sceneggiatura" all'accordion.
     * @param accordion L'accordion a cui aggiungere il pannello.
     * @param ui L'istanza UI corrente.
     */
    private void createAndAddScreenplayPanel(Accordion accordion, UI ui) {
        AccordionPanel panel = accordion.add("Sceneggiatura", createLoadingPlaceholder("Caricamento Sceneggiatura..."));
        styleAccordionPanelSummary(panel);
        loadSceneggiaturaDataAsync(ui, panel, scenario.getId());
    }


    private VerticalLayout createLoadingPlaceholder(String message) {
        Span loadingText = new Span(message);
        loadingText.getStyle().set("margin-bottom", "var(--lumo-space-s)");
        ProgressBar progressBarPlaceholder = new ProgressBar(); // Different name from the main one
        progressBarPlaceholder.setIndeterminate(true);
        VerticalLayout placeholder = new VerticalLayout(loadingText, progressBarPlaceholder);
        placeholder.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        placeholder.setPadding(true);
        placeholder.getStyle().set("min-height", "100px");
        return placeholder;
    }

    private void styleAccordionPanelSummary(AccordionPanel panel) {
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

    // ASYNCHRONOUS DATA LOADING METHODS FOR ACCORDION PANELS (Unchanged)

    private void loadGeneralSupportDataAsync(UI ui,
                                             AccordionPanel panelToUpdate,
                                             Scenario currentScenario, // Now passed directly
                                             ScenarioService scService,
                                             MaterialeService matService) {
        String panelName = "Informazioni Generali";
        executorService.submit(() -> {
            try {
                // Ensure currentScenario is not null before proceeding
                if (currentScenario == null) {
                    logger.warn("ASYNC-{}: currentScenario is null. Cannot load data.", panelName);
                    if (!detached.get() && ui != null && !ui.isClosing()) {
                        ui.access(() -> panelToUpdate.setContent(new Span("Errore: Dati scenario non disponibili (" + panelName + ").")));
                    }
                    return;
                }
                logger.info("ASYNC-{}: Inizio caricamento dati per scenarioId: {}", panelName, currentScenario.getId());
                boolean isPediatric = scService.isPediatric(currentScenario.getId());
                String infoGenitore = isPediatric ? currentScenario.getInfoGenitore() : null;
                String materiali = matService.toStringAllMaterialsByScenarioId(currentScenario.getId());
                List<String> azioniChiave = scService.getNomiAzioniChiaveByScenarioId(currentScenario.getId());
                logger.info("ASYNC-{}: Dati caricati. Pronto per UI. ScenarioId: {}", panelName, currentScenario.getId());

                if (detached.get() || ui.isClosing()) {
                    logger.warn("ASYNC-{}: View staccata o UI in chiusura per scenarioId: {}. Aggiornamento UI annullato.", panelName, currentScenario.getId());
                    return;
                }

                ui.access(() -> {
                    try {
                        logger.info("ASYNC-{}: Aggiornamento UI con dati per scenarioId: {}", panelName, currentScenario.getId());
                        Component content = GeneralSupport.createOverviewContentWithData(
                                currentScenario, isPediatric, infoGenitore, materiali, azioniChiave
                        );
                        logger.error("Azioni chiave: {}", azioniChiave.toString());
                        panelToUpdate.setContent(content);
                        logger.info("ASYNC-{}: UI aggiornata con successo per scenarioId: {}", panelName, currentScenario.getId());
                    } catch (Exception e_ui) {
                        logger.error("ASYNC-{}: Errore durante l'aggiornamento UI per scenarioId: {}", panelName, currentScenario.getId(), e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("ASYNC-{}: Errore nel task in background per scenarioId: {} (currentScenario might be null if error is early)", panelName, (currentScenario != null ? currentScenario.getId() : "N/A"), e_task);
                if (detached.get() || ui.isClosing()) return;
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
        });
    }

    private void loadPatientStateDataAsync(UI ui, AccordionPanel panelToUpdate, int currentScenarioId) {
        String panelName = "Stato Paziente";
        executorService.submit(() -> {
            try {
                logger.info("ASYNC-{}: Inizio caricamento dati per scenarioId: {}", panelName, currentScenarioId);
                PazienteT0 pazienteT0 = scenarioService.getPazienteT0ById(currentScenarioId);
                EsameFisico esameFisico = scenarioService.getEsameFisicoById(currentScenarioId);
                logger.info("ASYNC-{}: Dati caricati (PazienteT0: {}, EsameFisico: {}). Pronto per UI. ScenarioId: {}",
                        panelName, (pazienteT0 != null), (esameFisico != null), currentScenarioId);

                if (detached.get() || ui.isClosing()) {
                    logger.warn("ASYNC-{}: View staccata o UI in chiusura. Aggiornamento UI annullato. ScenarioId: {}", panelName, currentScenarioId);
                    return;
                }

                ui.access(() -> {
                    try {
                        logger.info("ASYNC-{}: Aggiornamento UI con dati per scenarioId: {}", panelName, currentScenarioId);
                        Component content = PatientT0Support.createPatientContent(pazienteT0, esameFisico);
                        panelToUpdate.setContent(content);
                        logger.info("ASYNC-{}: UI aggiornata con successo per scenarioId: {}", panelName, currentScenarioId);
                    } catch (Exception e_ui) {
                        logger.error("ASYNC-{}: Errore durante l'aggiornamento UI per scenarioId: {}", panelName, currentScenarioId, e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("ASYNC-{}: Errore nel task in background per scenarioId: {}", panelName, currentScenarioId, e_task);
                if (detached.get() || ui.isClosing()) return;
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
        });
    }

    private void loadExamsDataAsync(UI ui, AccordionPanel panelToUpdate, int currentScenarioId) {
        String panelName = "Esami e Referti";
        executorService.submit(() -> {
            try {
                logger.info("ASYNC-{}: Inizio caricamento dati per scenarioId: {}", panelName, currentScenarioId);
                List<EsameReferto> esamiReferti = scenarioService.getEsamiRefertiByScenarioId(currentScenarioId);
                logger.info("ASYNC-{}: Dati caricati (Numero esami/referti: {}). Pronto per UI. ScenarioId: {}",
                        panelName, (esamiReferti != null ? esamiReferti.size() : "null"), currentScenarioId);

                if (detached.get() || ui.isClosing()) {
                    logger.warn("ASYNC-{}: View staccata o UI in chiusura. Aggiornamento UI annullato. ScenarioId: {}", panelName, currentScenarioId);
                    return;
                }

                ui.access(() -> {
                    try {
                        logger.info("ASYNC-{}: Aggiornamento UI con dati per scenarioId: {}", panelName, currentScenarioId);
                        Component content = ExamSupport.createExamsContent(esamiReferti);
                        panelToUpdate.setContent(content);
                        logger.info("ASYNC-{}: UI aggiornata con successo per scenarioId: {}", panelName, currentScenarioId);
                    } catch (Exception e_ui) {
                        logger.error("ASYNC-{}: Errore durante l'aggiornamento UI per scenarioId: {}", panelName, currentScenarioId, e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("ASYNC-{}: Errore nel task in background per scenarioId: {}", panelName, currentScenarioId, e_task);
                if (detached.get() || ui.isClosing()) return;
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
        });
    }

    private void loadTimelineDataAsync(UI ui,
                                       AccordionPanel panelToUpdate,
                                       int currentScenarioId) {
        String panelName = "Timeline";
        executorService.submit(() -> {
            try {
                logger.info("ASYNC-{}: Inizio caricamento dati per scenarioId: {}", panelName, currentScenarioId);
                List<Tempo> tempi = scenarioService.getTempiByScenarioId(currentScenarioId);
                logger.info("ASYNC-{}: Dati caricati (numero tempi: {}). Pronto per UI. ScenarioId: {}",
                        panelName, (tempi != null ? tempi.size() : "null"), currentScenarioId);

                if (detached.get() || ui.isClosing()) {
                    logger.warn("ASYNC-{}: View staccata o UI in chiusura. Aggiornamento UI annullato. ScenarioId: {}", panelName, currentScenarioId);
                    return;
                }

                ui.access(() -> {
                    try {
                        if (tempi != null && !tempi.isEmpty()) {
                            logger.info("ASYNC-{}: Aggiornamento UI con dati per scenarioId: {}", panelName, currentScenarioId);
                            panelToUpdate.setContent(TimesSupport.createTimelineContent(tempi, scenarioId));
                        } else {
                            logger.info("ASYNC-{}: Nessun dato per scenarioId: {}. Mostro messaggio 'Nessuna timeline'.", panelName, currentScenarioId);
                            panelToUpdate.setContent(new Span("Nessuna timeline disponibile per questo scenario."));
                        }
                        logger.info("ASYNC-{}: UI aggiornata con successo per scenarioId: {}", panelName, currentScenarioId);
                    } catch (Exception e_ui) {
                        logger.error("ASYNC-{}: Errore durante l'aggiornamento UI per scenarioId: {}", panelName, currentScenarioId, e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("ASYNC-{}: Errore nel task in background per scenarioId: {}", panelName, currentScenarioId, e_task);
                if (detached.get() || ui.isClosing()) return;
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
        });
    }

    private void loadSceneggiaturaDataAsync(UI ui,
                                            AccordionPanel panelToUpdate,
                                            int currentScenarioId) {
        String panelName = "Sceneggiatura";
        executorService.submit(() -> {
            try {
                logger.info("ASYNC-{}: Inizio caricamento tipo scenario per scenarioId: {}", panelName, currentScenarioId);
                String scenarioType = scenarioService.getScenarioType(currentScenarioId);
                logger.info("ASYNC-{}: Tipo scenario caricato: '{}'. ScenarioId: {}", panelName, scenarioType, currentScenarioId);

                String sceneggiatura = null;
                if ("Patient Simulated Scenario".equalsIgnoreCase(scenarioType)) {
                    logger.info("ASYNC-{}: Recupero sceneggiatura per scenarioId: {}", panelName, currentScenarioId);
                    sceneggiatura = ScenarioService.getSceneggiatura(currentScenarioId);
                    logger.info("ASYNC-{}: Sceneggiatura recuperata (null? {}). ScenarioId: {}", panelName, (sceneggiatura == null), currentScenarioId);
                }

                if (detached.get() || ui.isClosing()) {
                    logger.warn("ASYNC-{}: View staccata o UI in chiusura. Aggiornamento UI annullato. ScenarioId: {}", panelName, currentScenarioId);
                    return;
                }

                final String finalSceneggiatura = sceneggiatura;
                final String finalScenarioType = scenarioType;

                ui.access(() -> {
                    try {
                        if ("Patient Simulated Scenario".equalsIgnoreCase(finalScenarioType) && finalSceneggiatura != null && !finalSceneggiatura.isBlank()) {
                            logger.info("ASYNC-{}: Aggiornamento UI con dati per scenarioId: {}", panelName, currentScenarioId);
                            panelToUpdate.setContent(SceneggiaturaSupport.createSceneggiaturaContent(finalSceneggiatura));
                        } else {
                            logger.info("ASYNC-{}: Nessuna sceneggiatura applicabile/trovata per scenarioId: {}. Tipo: {}", panelName, currentScenarioId, finalScenarioType);
                            panelToUpdate.setContent(new Span("Nessuna sceneggiatura disponibile per questo tipo di scenario o per questo scenario specifico."));
                        }
                        logger.info("ASYNC-{}: UI aggiornata con successo per scenarioId: {}", panelName, currentScenarioId);
                    } catch (Exception e_ui) {
                        logger.error("ASYNC-{}: Errore durante l'aggiornamento UI per scenarioId: {}", panelName, currentScenarioId, e_ui);
                        panelToUpdate.setContent(new Span("Errore nella visualizzazione (" + panelName + "). Dettagli nel log."));
                    }
                });
            } catch (Exception e_task) {
                logger.error("ASYNC-{}: Errore nel task in background per scenarioId: {}", panelName, currentScenarioId, e_task);
                if (detached.get() || ui.isClosing()) return;
                ui.access(() -> panelToUpdate.setContent(new Span("Errore nel caricamento (" + panelName + "). Dettagli nel log.")));
            }
        });
    }
}
