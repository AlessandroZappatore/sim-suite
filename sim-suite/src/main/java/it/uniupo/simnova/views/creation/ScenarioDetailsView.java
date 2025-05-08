package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
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
 * @version 1.0
 */
@SuppressWarnings("ThisExpressionReferencesGlobalObjectJS")
@PageTitle("Dettagli Scenario")
@Route(value = "scenari")
public class ScenarioDetailsView extends Composite<VerticalLayout> implements HasUrlParameter<String>, BeforeEnterObserver {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;

    private final FileStorageService fileStorageService;
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
     */
    @Autowired
    public ScenarioDetailsView(ScenarioService scenarioService, FileStorageService fileStorageService, MaterialeService materialeNecessario) {
        this.scenarioService = scenarioService;
        UI.getCurrent();
        getContent().addClassName("scenario-details-view");
        getContent().setPadding(false);
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
                throw new NumberFormatException();
            }

            this.scenarioId = Integer.parseInt(parameter);
            if (scenarioId <= 0 || !scenarioService.existScenario(scenarioId)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            logger.error("ID scenario non valido: {}", parameter, e);
            event.rerouteToError(NotFoundException.class, "ID scenario " + scenarioId + " non valido");
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
            Notification.show("ID scenario non valido", 3000, Position.MIDDLE).addThemeVariants(
                    NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("scenari");
            return;
        }
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
        executorService.shutdownNow();
    }

    /**
     * Carica i dati dello scenario in un thread separato.
     * Mostra una barra di progresso durante il caricamento.
     */
    private void loadScenarioData() {
        if (detached.get()) {
            return;
        }

        getContent().removeAll();
        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        getContent().add(progressBar);

        UI ui = UI.getCurrent();
        if (ui == null) return;

        // Utilizziamo una variabile per tracciare se il caricamento è completato
        final AtomicBoolean loadingCompleted = new AtomicBoolean(false);

        // Task principale di caricamento
        executorService.submit(() -> {
            try {
                // Implementazione di timeout con CompletableFuture
                CompletableFuture<Scenario> future = CompletableFuture.supplyAsync(
                        () -> scenarioService.getScenarioById(scenarioId),
                        executorService
                );

                Scenario loadedScenario;
                try {
                    loadedScenario = future.get(15, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    logger.error("Timeout durante il caricamento dello scenario {}", scenarioId);
                    throw new RuntimeException("Timeout durante il caricamento");
                }

                if (detached.get() || ui.isClosing()) {
                    return;
                }

                ui.access(() -> {
                    try {
                        if (loadedScenario == null) {
                            Notification.show("Scenario non trovato", 3000, Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            ui.navigate("scenari");
                            return;
                        }
                        this.scenario = loadedScenario;
                        initView();
                    } finally {
                        loadingCompleted.set(true);
                        progressBar.setVisible(false);
                    }
                });
            } catch (Exception e) {
                logger.error("Errore durante il caricamento dello scenario {}: {}", scenarioId, e.getMessage(), e);
                if (!detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        loadingCompleted.set(true);
                        Notification.show("Errore nel caricamento: " + e.getMessage(),
                                        3000, Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        progressBar.setVisible(false);
                        ui.navigate("scenari");
                    });
                }
            }
        });

        // Task di backup che controlla se il caricamento è bloccato
        executorService.submit(() -> {
            try {
                // Aspetta 20 secondi e poi verifica se il caricamento è completato
                Thread.sleep(20000);
                if (!loadingCompleted.get() && !detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        logger.error("Forzato timeout per caricamento bloccato, scenario {}", scenarioId);
                        Notification.show("Caricamento bloccato, riprova più tardi",
                                        3000, Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        progressBar.setVisible(false);
                        ui.navigate("scenari");
                    });
                }
            } catch (InterruptedException e) {
                // Ignora l'interruzione, è normale durante lo shutdown
            }
        });
    }

    /**
     * Inizializza la vista con i dati dello scenario.
     * Crea e aggiunge i componenti alla vista.
     */
    private void initView() {
        if (detached.get()) {
            return;
        }

        VerticalLayout mainLayout = StyleApp.getMainLayout(getContent());

        AppHeader header = new AppHeader(fileStorageService);

        // 1. HEADER
        Button backButton = StyleApp.getBackButton();

        Button editButton = StyleApp.getEditButton();
        editButton.addClickListener(e ->
                UI.getCurrent().navigate("modificaScenario/" + scenario.getId()));

        HorizontalLayout editButtonContainer = new HorizontalLayout();
        editButtonContainer.setWidthFull();
        editButtonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        editButtonContainer.setPadding(false);
        editButtonContainer.setMargin(false);
        editButtonContainer.add(editButton);

        VerticalLayout headerSection = StyleApp.getTitleSubtitle(
                "Dettagli Scenario",
                "Visualizza i dettagli dello scenario selezionato",
                VaadinIcon.INFO_CIRCLE,
                "var(--lumo-primary-color)"
        );

        HorizontalLayout customHeader = StyleApp.getCustomHeader(backButton, header);

        // 2. CONTENUTO PRINCIPALE (con accordion)
        VerticalLayout contentLayout = StyleApp.getContentLayout();

        // Titolo e sottotitolo
        // Contenitore per titolo e autori con ombra e bordi arrotondati
        Div titleContainer = new Div();
        titleContainer.setWidthFull();
        titleContainer.getStyle()
                .set("max-width", "800px")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("transition", "box-shadow 0.3s ease-in-out");

        // Effetto hover sul contenitore
        titleContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        // Titolo con stile migliorato
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

        // Autori con stile migliorato
        Paragraph authors = new Paragraph("Autori: " + scenario.getAutori());
        authors.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Top.NONE,
                LumoUtility.Margin.Bottom.NONE,
                LumoUtility.FontSize.XLARGE
        );

        // Aggiungi elementi al contenitore
        titleContainer.add(title, authors);

        // Aggiungi il contenitore al layout invece degli elementi separati

        Component subtitle = InfoSupport.getInfo(scenario);

        // Accordion principale
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        // Aggiungi i pannelli
        AccordionPanel panelInfoGenerali = accordion.add("Informazioni Generali", GeneralSupport.createOverviewContent(scenario, scenarioService, materialeNecessario));
        AccordionPanel panelStatoPaziente = accordion.add("Stato Paziente", PatientT0Support.createPatientContent(scenarioService, scenarioId));
        AccordionPanel panelEsamiReferti = accordion.add("Esami e Referti", ExamSupport.createExamsContent(scenarioService, scenarioId));

        styleAccordionPanelSummary(panelInfoGenerali);
        styleAccordionPanelSummary(panelStatoPaziente);
        styleAccordionPanelSummary(panelEsamiReferti);

        List<Tempo> tempi = scenarioService.getTempiByScenarioId(scenarioId);
        if (!tempi.isEmpty()) {
            AccordionPanel timelinePanel = accordion.add("Timeline", TimesSupport.createTimelineContent(scenarioService, scenarioId));
            styleAccordionPanelSummary(timelinePanel);
        }


        String scenarioType = scenarioService.getScenarioType(scenarioId);
        if ("Patient Simulated Scenario".equalsIgnoreCase(scenarioType)) {
            AccordionPanel sceneggiaturaPanel = accordion.add("Sceneggiatura", SceneggiaturaSupport.createSceneggiaturaContent(scenarioId));
            styleAccordionPanelSummary(sceneggiaturaPanel);
        }

        // Espandi il primo pannello di default
        accordion.open(0);

        contentLayout.add(editButtonContainer, headerSection, titleContainer, subtitle, accordion);

        // 3. FOOTER
        HorizontalLayout footerLayout = StyleApp.getFooterLayout(null);
        // Assemblaggio finale
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        backButton.addClickListener(e -> UI.getCurrent().navigate("scenari"));
    }

    /**
     * Applica stili personalizzati al summary di un AccordionPanel per migliorarne la leggibilità e cliccabilità.
     *
     * @param panel l'AccordionPanel da stilizzare
     */
    private void styleAccordionPanelSummary(AccordionPanel panel) {
        if (panel != null) {
            // Aumenta la dimensione del font, il padding e centra il testo per una migliore interazione
            panel.getSummary().getStyle()
                    .set("font-size", "var(--lumo-font-size-l)") // Aumenta la dimensione del testo
                    .set("padding", "var(--lumo-space-m)")      // Aumenta il padding per un'area cliccabile più grande
                    .set("text-align", "center");               // Centra il testo nel summary

            // Puoi anche aggiungere un min-height se necessario
            // .set("min-height", "48px");

            // Opzionale: Effetto hover per migliorare il feedback visivo
            panel.getSummary().getStyle().set("transition", "background-color 0.2s ease-in-out");
            panel.getSummary().getElement().executeJs(
                    "this.addEventListener('mouseover', function() { this.style.backgroundColor = 'var(--lumo-contrast-5pct)'; });" +
                            "this.addEventListener('mouseout', function() { this.style.backgroundColor = 'transparent'; });"
            );
        }
    }
}