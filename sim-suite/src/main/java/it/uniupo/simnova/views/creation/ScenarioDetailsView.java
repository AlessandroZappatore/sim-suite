package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.*;
import it.uniupo.simnova.service.FileStorageService;
import it.uniupo.simnova.service.MaterialeService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.support.AppHeader;
import it.uniupo.simnova.views.support.CreditsComponent;
import it.uniupo.simnova.views.support.StyleApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    private final MaterialeService materialeService;
    private final FileStorageService fileStorageService;
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
    public ScenarioDetailsView(ScenarioService scenarioService, MaterialeService materialeService, FileStorageService fileStorageService) {
        this.scenarioService = scenarioService;
        this.materialeService = materialeService;
        UI.getCurrent();
        getContent().addClassName("scenario-details-view");
        getContent().setPadding(false);
        this.fileStorageService = fileStorageService;
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

        Component subtitle = getParagraph();

        // Accordion principale
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        // Aggiungi i pannelli
        accordion.add("Informazioni Generali", createOverviewContent());
        accordion.add("Stato Paziente", createPatientContent());
        accordion.add("Esami e Referti", createExamsContent());

        List<Tempo> tempi = scenarioService.getTempiByScenarioId(scenarioId);
        if (!tempi.isEmpty()) {
            accordion.add("Timeline", createTimelineContent());
        }

        String scenarioType = scenarioService.getScenarioType(scenarioId);
        if ("Patient Simulated Scenario".equalsIgnoreCase(scenarioType)) {
            accordion.add("Sceneggiatura", createSceneggiaturaContent(scenarioId));
        }

        // Espandi il primo pannello di default
        accordion.open(0);

        contentLayout.add(editButtonContainer, headerSection, titleContainer, subtitle, accordion);

        // 3. FOOTER
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.addClassName(LumoUtility.TextColor.SECONDARY);
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        footerLayout.addClassName(LumoUtility.Border.TOP);
        footerLayout.getStyle().set("border-color", "var(--lumo-contrast-10pct)");

        CreditsComponent credit = new CreditsComponent();

        footerLayout.add(credit);
        // Assemblaggio finale
        mainLayout.add(
                customHeader,
                contentLayout,
                footerLayout
        );

        backButton.addClickListener(e -> UI.getCurrent().navigate("scenari"));
    }

    private Component getParagraph() {
        // Contenitore principale con layout orizzontale e centrato
        HorizontalLayout badgesContainer = new HorizontalLayout();
        badgesContainer.setWidthFull();
        badgesContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        badgesContainer.setSpacing(true);
        badgesContainer.getStyle().set("flex-wrap", "wrap");

        // Colore primario per tutti i badge per uniformità
        String badgeColor = "var(--lumo-primary-color)";

        // Crea i badge con informazioni
        Span pazienteBadge = createInfoBadge("Paziente", scenario.getNomePaziente(), badgeColor);
        Span tipologiaBadge = createInfoBadge("Tipologia", scenario.getTipologia(), badgeColor);
        Span patologiaBadge = createInfoBadge("Patologia", scenario.getPatologia(), badgeColor);
        Span durataBadge = createInfoBadge("Durata", String.format("%.1f min", scenario.getTimerGenerale()), badgeColor);
        Span targetBadge = createInfoBadge("Target", scenario.getTarget(), badgeColor);

        // Aggiungi i badge al container
        badgesContainer.add(pazienteBadge, tipologiaBadge, patologiaBadge, durataBadge, targetBadge);

        return badgesContainer;
    }

    /**
     * Crea un badge con etichetta e valore
     *
     * @param label etichetta del badge
     * @param value valore da mostrare
     * @param color colore del badge
     * @return componente Span formattato come badge
     */
    private Span createInfoBadge(String label, String value, String color) {
        Span badge = new Span(label + ": " + value);
        badge.getStyle()
                .set("background-color", color + "10")  // Colore con opacità al 10%
                .set("color", color)
                .set("border-radius", "16px")
                .set("padding", "6px 16px")
                .set("font-size", "16px")           // Aumentata dimensione del testo
                .set("font-weight", "500")
                .set("margin", "6px")               // Aumentato il margine
                .set("display", "inline-block")
                .set("border", "1px solid " + color + "40")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");

        // Effetto hover
        badge.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.boxShadow = '0 3px 6px rgba(0,0,0,0.15)'; " +
                        "  this.style.transform = 'translateY(-2px)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)'; " +
                        "  this.style.transform = 'translateY(0)'; " +
                        "});"
        );

        return badge;
    }

    /**
     * Crea il contenuto per la sezione "Informazioni Generali".
     *
     * @return il layout con le informazioni generali
     */
    private VerticalLayout createOverviewContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();

        // Card per informazioni base con stile migliorato
        Div card = new Div();
        card.addClassName("info-card");
        card.getStyle()
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("padding", "var(--lumo-space-m)")
                .set("background-color", "var(--lumo-base-color)")
                .set("transition", "box-shadow 0.3s ease-in-out");

        // Effetto hover sulla card
        card.getElement().executeJs(
                "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                        "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
        );

        // Aggiungi elementi con controlli null-safety
        addInfoItemIfNotEmpty(card, "Descrizione", scenario.getDescrizione(), VaadinIcon.PENCIL);
        addInfoItemIfNotEmpty(card, "Briefing", scenario.getBriefing(), VaadinIcon.GROUP);

        if (scenarioService.isPediatric(scenarioId)) {
            addInfoItemIfNotEmpty(card, "Informazioni dai genitori", scenario.getInfoGenitore(), VaadinIcon.FAMILY);
        }

        addInfoItemIfNotEmpty(card, "Patto Aula", scenario.getPattoAula(), VaadinIcon.HANDSHAKE);
        addInfoItemIfNotEmpty(card, "Azioni Chiave", scenario.getAzioneChiave(), VaadinIcon.KEY);
        addInfoItemIfNotEmpty(card, "Obiettivi Didattici", scenario.getObiettivo(), VaadinIcon.BOOK);
        addInfoItemIfNotEmpty(card, "Moulage", scenario.getMoulage(), VaadinIcon.EYE);
        addInfoItemIfNotEmpty(card, "Liquidi e dosi farmaci", scenario.getLiquidi(), VaadinIcon.DROP);

        // Materiale necessario
        String materiali = materialeService.toStringAllMaterialsByScenarioId(scenarioId);
        addInfoItemIfNotEmpty(card, "Materiale necessario", materiali, VaadinIcon.BED);

        layout.add(card);
        return layout;
    }

    /**
     * Aggiunge un elemento informativo solo se il contenuto non è vuoto
     *
     * @param container contenitore dove aggiungere l'elemento
     * @param title     titolo dell'informazione
     * @param content   contenuto dell'informazione
     * @param iconType  icona da utilizzare
     */
    private void addInfoItemIfNotEmpty(HasComponents container, String title, String content, VaadinIcon iconType) {
        if (content != null && !content.trim().isEmpty()) {
            Icon icon = new Icon(iconType);
            icon.getStyle()
                    .set("color", "var(--lumo-primary-color)")
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("padding", "var(--lumo-space-xs)")
                    .set("border-radius", "50%");

            container.add(createInfoItem(title, content, icon));
        }
    }

    /**
     * Crea il contenuto per la sezione "Stato Paziente".
     *
     * @return il layout con le informazioni sul paziente
     */
    private VerticalLayout createPatientContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        // Sezione Paziente T0
        PazienteT0 paziente = scenarioService.getPazienteT0ById(scenarioId);
        if (paziente != null) {
            Div patientCard = new Div();
            patientCard.addClassName("info-card");
            patientCard.getStyle()
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("box-shadow", "var(--lumo-box-shadow-xs)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("margin-bottom", "var(--lumo-space-m)")
                    .set("transition", "box-shadow 0.3s ease-in-out")
                    .set("width", "80%")
                    .set("max-width", "800px");

            // Effetto hover sulla card
            patientCard.getElement().executeJs(
                    "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                            "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
            );

            // Usa il nuovo componente monitor per i parametri vitali invece della griglia
            Component vitalSignsMonitor = createVitalSignsMonitor(paziente);
            patientCard.add(vitalSignsMonitor);

            // Accessi venosi e arteriosi - resta invariato
            if (!paziente.getAccessiVenosi().isEmpty() || !paziente.getAccessiArteriosi().isEmpty()) {
                Div accessesCard = new Div();
                accessesCard.getStyle()
                        .set("margin-top", "var(--lumo-space-m)")
                        .set("padding-top", "var(--lumo-space-s)")
                        .set("border-top", "1px solid var(--lumo-contrast-10pct)")
                        .set("width", "100%");

                if (!paziente.getAccessiVenosi().isEmpty()) {
                    HorizontalLayout accessVenosiTitleLayout = new HorizontalLayout();
                    accessVenosiTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                    accessVenosiTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Centra il titolo
                    accessVenosiTitleLayout.setWidthFull();
                    accessVenosiTitleLayout.setSpacing(true);

                    Icon accessVenosiIcon = new Icon(VaadinIcon.LINES);
                    accessVenosiIcon.getStyle()
                            .set("color", "var(--lumo-primary-color)")
                            .set("background-color", "var(--lumo-primary-color-10pct)")
                            .set("padding", "var(--lumo-space-xs)")
                            .set("border-radius", "50%");

                    H4 accessVenosiTitle = new H4("Accessi Venosi");
                    accessVenosiTitle.getStyle()
                            .set("margin", "0")
                            .set("font-weight", "500");

                    accessVenosiTitleLayout.add(accessVenosiIcon, accessVenosiTitle);
                    accessesCard.add(accessVenosiTitleLayout);

                    Grid<Accesso> accessiVenosiGrid = createAccessiGrid(paziente.getAccessiVenosi());
                    accessiVenosiGrid.getStyle()
                            .set("border", "none")
                            .set("box-shadow", "none")
                            .set("margin-top", "var(--lumo-space-s)")
                            .set("margin-bottom", "var(--lumo-space-s)")
                            .set("margin-left", "auto")
                            .set("margin-right", "auto") // Centra la griglia
                            .set("max-width", "600px");  // Limita la larghezza della griglia
                    accessesCard.add(accessiVenosiGrid);
                }

                if (!paziente.getAccessiArteriosi().isEmpty()) {
                    HorizontalLayout accessArtTitleLayout = new HorizontalLayout();
                    accessArtTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
                    accessArtTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Centra il titolo
                    accessArtTitleLayout.setWidthFull();
                    accessArtTitleLayout.setSpacing(true);

                    Icon accessArtIcon = new Icon(VaadinIcon.LINES);
                    accessArtIcon.getStyle()
                            .set("color", "var(--lumo-success-color)")
                            .set("background-color", "var(--lumo-success-color-10pct)")
                            .set("padding", "var(--lumo-space-xs)")
                            .set("border-radius", "50%");

                    H4 accessArtTitle = new H4("Accessi Arteriosi");
                    accessArtTitle.getStyle()
                            .set("margin", "0")
                            .set("font-weight", "500");

                    accessArtTitleLayout.add(accessArtIcon, accessArtTitle);
                    accessesCard.add(accessArtTitleLayout);

                    Grid<Accesso> accessiArtGrid = createAccessiGrid(paziente.getAccessiArteriosi());
                    accessiArtGrid.getStyle()
                            .set("border", "none")
                            .set("box-shadow", "none")
                            .set("margin-top", "var(--lumo-space-s)")
                            .set("margin-left", "auto")
                            .set("margin-right", "auto") // Centra la griglia
                            .set("max-width", "600px");  // Limita la larghezza della griglia
                    accessesCard.add(accessiArtGrid);
                }

                patientCard.add(accessesCard);
            }

            layout.add(patientCard);
        } else {
            Div noDataCard = new Div();
            noDataCard.getStyle()
                    .set("background-color", "var(--lumo-contrast-5pct)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("text-align", "center")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("margin-bottom", "var(--lumo-space-m)")
                    .set("width", "80%")  // Limitare la larghezza
                    .set("max-width", "600px"); // Larghezza massima

            Icon noDataIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
            noDataIcon.setSize("2em");
            noDataIcon.getStyle().set("color", "var(--lumo-tertiary-text-color)");

            Paragraph noDataText = new Paragraph("Dati paziente non disponibili");
            noDataText.getStyle().set("margin-top", "var(--lumo-space-s)");

            noDataCard.add(noDataIcon, noDataText);
            layout.add(noDataCard);
        }

        // Sezione Esame Fisico
        EsameFisico esame = scenarioService.getEsameFisicoById(scenarioId);
        if (esame != null && !esame.getSections().isEmpty()) {
            Div examCard = new Div();
            examCard.addClassName("info-card");
            examCard.getStyle()
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("box-shadow", "var(--lumo-box-shadow-xs)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("transition", "box-shadow 0.3s ease-in-out")
                    .set("width", "80%")  // Limitare la larghezza
                    .set("max-width", "800px"); // Larghezza massima

            // Effetto hover sulla card
            examCard.getElement().executeJs(
                    "this.addEventListener('mouseover', function() { this.style.boxShadow = 'var(--lumo-box-shadow-s)'; });" +
                            "this.addEventListener('mouseout', function() { this.style.boxShadow = 'var(--lumo-box-shadow-xs)'; });"
            );

            // Titolo con icona per esame fisico
            HorizontalLayout examTitleLayout = new HorizontalLayout();
            examTitleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            examTitleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Centra il titolo
            examTitleLayout.setWidthFull();
            examTitleLayout.setSpacing(true);

            Icon examIcon = new Icon(VaadinIcon.STETHOSCOPE);
            examIcon.getStyle()
                    .set("color", "var(--lumo-primary-color)")
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("padding", "var(--lumo-space-xs)")
                    .set("border-radius", "50%");

            H3 examTitle = new H3("Esame Fisico");
            examTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "600")
                    .set("color", "var(--lumo-primary-text-color)");

            examTitleLayout.add(examIcon, examTitle);
            examCard.add(examTitleLayout);

            Map<String, String> sections = esame.getSections();
            VerticalLayout examLayout = new VerticalLayout();
            examLayout.setPadding(false);
            examLayout.setSpacing(true);
            examLayout.getStyle().set("margin-top", "var(--lumo-space-m)");
            examLayout.setAlignItems(FlexComponent.Alignment.CENTER); // Centra gli elementi dell'esame

            addSectionIfNotEmpty(examLayout, "Generale", sections.get("Generale"));
            addSectionIfNotEmpty(examLayout, "Pupille", sections.get("Pupille"));
            addSectionIfNotEmpty(examLayout, "Collo", sections.get("Collo"));
            addSectionIfNotEmpty(examLayout, "Torace", sections.get("Torace"));
            addSectionIfNotEmpty(examLayout, "Cuore", sections.get("Cuore"));
            addSectionIfNotEmpty(examLayout, "Addome", sections.get("Addome"));
            addSectionIfNotEmpty(examLayout, "Retto", sections.get("Retto"));
            addSectionIfNotEmpty(examLayout, "Cute", sections.get("Cute"));
            addSectionIfNotEmpty(examLayout, "Estremità", sections.get("Estremità"));
            addSectionIfNotEmpty(examLayout, "Neurologico", sections.get("Neurologico"));
            addSectionIfNotEmpty(examLayout, "FAST", sections.get("FAST"));

            if (examLayout.getComponentCount() > 0) {
                examCard.add(examLayout);
                layout.add(examCard);
            }
        }

        return layout;
    }

    /**
     * Crea il contenuto per la sezione delle informazioni sui parametri vitali in stile monitor ospedaliero.
     *
     * @param paziente dati del paziente da visualizzare
     * @return componente formattato come monitor parametri vitali
     */
    private Component createVitalSignsMonitor(PazienteT0 paziente) {
        // Container principale del monitor con stile
        Div monitorContainer = new Div();
        monitorContainer.setWidthFull();
        monitorContainer.getStyle()
                .set("background-color", "var(--lumo-shade-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("border", "2px solid var(--lumo-contrast-10pct)")
                .set("box-shadow", "0 4px 8px rgba(0, 0, 0, 0.1)")
                .set("padding", "var(--lumo-space-m)")
                .set("max-width", "700px")
                .set("margin", "0 auto");

        // Intestazione del monitor
        HorizontalLayout monitorHeader = new HorizontalLayout();
        monitorHeader.setWidthFull();
        monitorHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        monitorHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        monitorHeader.setPadding(false);
        monitorHeader.setSpacing(true);

        // Titolo del monitor
        H3 monitorTitle = new H3("Parametri Vitali");
        monitorTitle.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-primary-color)")
                .set("font-weight", "600");

        // Indicatore LED simulato
        Div statusLed = new Div();
        statusLed.getStyle()
                .set("width", "12px")
                .set("height", "12px")
                .set("background-color", "var(--lumo-success-color)")
                .set("border-radius", "50%")
                .set("box-shadow", "0 0 5px var(--lumo-success-color)");

        // Animazione del LED
        statusLed.getElement().executeJs(
                "this.style.animation = 'pulse 2s infinite';" +
                        "if (!document.getElementById('led-style')) {" +
                        "  const style = document.createElement('style');" +
                        "  style.id = 'led-style';" +
                        "  style.textContent = '@keyframes pulse {" +
                        "    0% { opacity: 1; }" +
                        "    50% { opacity: 0.6; }" +
                        "    100% { opacity: 1; }" +
                        "  }';" +
                        "  document.head.appendChild(style);" +
                        "}"
        );

        monitorHeader.add(monitorTitle, statusLed);

        // Layout per i parametri vitali
        HorizontalLayout vitalSignsLayout = new HorizontalLayout();
        vitalSignsLayout.setWidthFull();
        vitalSignsLayout.setPadding(false);
        vitalSignsLayout.setSpacing(false);
        vitalSignsLayout.getStyle().set("flex-wrap", "wrap");
        vitalSignsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Crea i singoli parametri vitali
        if (paziente.getPA() != null) {
            vitalSignsLayout.add(createVitalSignBox("PA", paziente.getPA(), "mmHg", "var(--lumo-error-color)"));
        }
        if (paziente.getFC() != null) {
            vitalSignsLayout.add(createVitalSignBox("FC", String.valueOf(paziente.getFC()), "bpm", "var(--lumo-primary-color)"));
        }
        if (paziente.getT() > -50) {
            String temperaturaFormattata = String.format("%.1f", paziente.getT());
            vitalSignsLayout.add(createVitalSignBox("TC", temperaturaFormattata, "°C", "var(--lumo-success-color)"));
        }
        if (paziente.getRR() != null) {
            vitalSignsLayout.add(createVitalSignBox("RR", String.valueOf(paziente.getRR()), "rpm", "var(--lumo-tertiary-color)"));
        }
        if (paziente.getSpO2() != null) {
            vitalSignsLayout.add(createVitalSignBox("SpO₂", String.valueOf(paziente.getSpO2()), "%", "var(--lumo-contrast)"));
        }
        // Altri parametri se disponibili
        if (paziente.getFiO2() != null && paziente.getFiO2() != 0) {
            vitalSignsLayout.add(createVitalSignBox("FiO₂", String.valueOf(paziente.getFiO2()), "%", "var(--lumo-primary-color-50pct)"));
        }
        if (paziente.getLitriO2() != null && paziente.getLitriO2() != 0) {
            vitalSignsLayout.add(createVitalSignBox("Litri O₂", String.valueOf(paziente.getLitriO2()), "Litri/m", "var(--lumo-contrast-70pct)"));
        }
        if (paziente.getEtCO2() != null) {
            vitalSignsLayout.add(createVitalSignBox("EtCO₂", String.valueOf(paziente.getEtCO2()), "mmHg", "var(--lumo-warning-color)"));
        }


        if (paziente.getMonitor() != null && !paziente.getMonitor().isEmpty()) {
            // Creo un contenitore per il monitor
            Div monitorTextContainer = new Div();
            monitorTextContainer.setWidthFull();
            monitorTextContainer.getStyle()
                    .set("margin-top", "var(--lumo-space-m)")
                    .set("background-color", "var(--lumo-base-color)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("padding", "var(--lumo-space-s)")
                    .set("box-shadow", "inset 0 1px 3px rgba(0, 0, 0, 0.1)");

            // Intestazione del monitor
            HorizontalLayout monitorTextHeader = new HorizontalLayout();
            monitorTextHeader.setWidthFull();
            monitorTextHeader.setAlignItems(FlexComponent.Alignment.CENTER);

            Icon monitorIcon = new Icon(VaadinIcon.LAPTOP);
            monitorIcon.getStyle()
                    .set("color", "var(--lumo-tertiary-color)")
                    .set("margin-right", "var(--lumo-space-xs)");

            H4 monitorTextTitle = new H4("Monitoraggio");
            monitorTextTitle.getStyle()
                    .set("margin", "0")
                    .set("font-weight", "500")
                    .set("font-size", "var(--lumo-font-size-m)")
                    .set("color", "var(--lumo-tertiary-text-color)");

            monitorTextHeader.add(monitorIcon, monitorTextTitle);

            // Testo del monitor
            Span monitorText = new Span(paziente.getMonitor());
            monitorText.getStyle()
                    .set("display", "block")
                    .set("margin-top", "var(--lumo-space-s)")
                    .set("white-space", "pre-wrap")
                    .set("font-family", "var(--lumo-font-family-monospace)")
                    .set("color", "var(--lumo-body-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("line-height", "1.5");

            monitorTextContainer.add(monitorTextHeader, monitorText);
            vitalSignsLayout.add(monitorTextContainer);
        }
        monitorContainer.add(monitorHeader, vitalSignsLayout);

        return monitorContainer;
    }

    /**
     * Crea un box per un singolo parametro vitale
     *
     * @param label etichetta del parametro
     * @param value valore del parametro
     * @param unit  unità di misura
     * @param color colore del parametro
     * @return componente Div formattato come box parametro vitale
     */
    private Div createVitalSignBox(String label, String value, String unit, String color) {
        Div box = new Div();
        box.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("margin", "var(--lumo-space-xs)")
                .set("text-align", "center")
                .set("min-width", "130px")
                .set("background-color", "var(--lumo-base-color)")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.05)")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        // Aggiungi effetto hover
        box.getElement().executeJs(
                "this.addEventListener('mouseover', function() {" +
                        "  this.style.transform = 'translateY(-2px)';" +
                        "  this.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';" +
                        "});" +
                        "this.addEventListener('mouseout', function() {" +
                        "  this.style.transform = 'translateY(0)';" +
                        "  this.style.boxShadow = '0 2px 4px rgba(0,0,0,0.05)';" +
                        "});"
        );

        // Etichetta del parametro
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("display", "block")
                .set("font-size", "14px")
                .set("font-weight", "500")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-bottom", "4px");

        // Valore del parametro
        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("display", "block")
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("color", color)
                .set("line-height", "1.2");

        // Unità di misura
        Span unitSpan = new Span(unit);
        unitSpan.getStyle()
                .set("display", "block")
                .set("font-size", "12px")
                .set("color", "var(--lumo-tertiary-text-color)");

        box.add(labelSpan, valueSpan, unitSpan);

        return box;
    }

    /**
     * Crea il contenuto per la sezione "Esami e Referti".
     *
     * @return il layout con gli esami e referti
     */
    private VerticalLayout createExamsContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();

        // Ottieni gli esami/referti dal servizio
        List<EsameReferto> esami = scenarioService.getEsamiRefertiByScenarioId(scenarioId);
        if (esami != null && !esami.isEmpty()) {
            for (EsameReferto esame : esami) {
                Div examCard = new Div();
                examCard.addClassName("exam-card");

                H3 examTitle = new H3(esame.getTipo());

                VerticalLayout examContent = new VerticalLayout();
                examContent.setPadding(false);
                examContent.setSpacing(true);

                // Anteprima file multimediale
                if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                    examContent.add(createMediaPreview(esame.getMedia()));
                }

                // Referto testuale
                if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                    examContent.add(createInfoItem("Referto", esame.getRefertoTestuale(), new Icon(VaadinIcon.FILE_TEXT)));
                }

                examCard.add(examTitle, examContent);
                layout.add(examCard);
            }
        } else {
            layout.add(new Paragraph("Nessun esame/referto disponibile"));
        }

        return layout;
    }

    /**
     * Crea un'anteprima per i file multimediali.
     *
     * @param fileName nome del file
     * @return il componente di anteprima
     */
    private Component createMediaPreview(String fileName) {
        String fileExtension;
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
        } else {
            logger.warn("Impossibile determinare l'estensione del file per l'anteprima: {}", fileName);
            return createErrorPreview("Tipo file non riconosciuto: " + fileName);
        }

        logger.debug("Creazione anteprima media per file: {}, estensione: {}", fileName, fileExtension);

        // Container principale centrato
        Div previewContainer = new Div();
        previewContainer.setWidthFull();
        previewContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin", "var(--lumo-space-s) 0")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        // Effetto hover
        previewContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() { " +
                        "  this.style.transform = 'translateY(-2px)'; " +
                        "  this.style.boxShadow = '0 6px 15px rgba(0, 0, 0, 0.1)'; " +
                        "});" +
                        "this.addEventListener('mouseout', function() { " +
                        "  this.style.transform = 'translateY(0)'; " +
                        "  this.style.boxShadow = '0 3px 10px rgba(0, 0, 0, 0.08)'; " +
                        "});"
        );

        // Intestazione con tipo file e nome
        HorizontalLayout mediaHeader = new HorizontalLayout();
        mediaHeader.setWidthFull();
        mediaHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        mediaHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        mediaHeader.setPadding(false);
        mediaHeader.setSpacing(true);
        mediaHeader.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        // Mostra tipo file e nome file
        Span fileTypeLabel = new Span(fileExtension.toUpperCase());
        fileTypeLabel.getStyle()
                .set("background-color", getColorForFileType(fileExtension))
                .set("color", "white")
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-size", "12px")
                .set("font-weight", "bold")
                .set("text-transform", "uppercase");

        Span fileNameLabel = new Span(getShortFileName(fileName));
        fileNameLabel.getStyle()
                .set("margin-left", "var(--lumo-space-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("white-space", "nowrap")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("max-width", "200px");

        // Container per il contenuto media
        Div mediaContentContainer = new Div();
        mediaContentContainer.setWidthFull();
        mediaContentContainer.getStyle()
                .set("display", "flex")
                .set("justify-content", "center")
                .set("align-items", "center")
                .set("min-height", "200px")
                .set("max-width", "800px")
                .set("margin", "0 auto");

        String mediaPath = "/" + fileName;
        logger.debug("Percorso media per anteprima: {}", mediaPath);

        Component mediaComponent;
        Icon typeIcon = getIconForFileType(fileExtension);

        // Pulsante per aprire il file a schermo intero
        Button fullscreenButton = new Button("Visualizza", new Icon(VaadinIcon.EXTERNAL_LINK));
        fullscreenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        fullscreenButton.getStyle()
                .set("border-radius", "30px")
                .set("margin-top", "var(--lumo-space-m)")
                .set("transition", "transform 0.2s ease")
                .set("cursor", "pointer");

        fullscreenButton.addClassName("hover-effect");
        fullscreenButton.addClickListener(e -> openFullMedia(fileName));


        // Generazione componente in base al tipo di file
        switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp":
                Image image = new Image(mediaPath, fileName);
                image.setMaxWidth("100%");
                image.setHeight("auto");
                image.getStyle()
                        .set("max-height", "320px")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("object-fit", "contain")
                        .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
                mediaComponent = image;
                break;

            case "pdf":
                IFrame pdfPreview = new IFrame();
                pdfPreview.setSrc(mediaPath);
                pdfPreview.setWidth("100%");
                pdfPreview.setHeight("500px");
                pdfPreview.getStyle()
                        .set("border", "none")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");
                mediaComponent = pdfPreview;
                break;

            case "mp4", "webm", "mov":
                Div videoContainer = new Div();
                videoContainer.getStyle()
                        .set("width", "100%")
                        .set("position", "relative")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("overflow", "hidden")
                        .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)");

                NativeVideo video = new NativeVideo();
                video.setSrc(mediaPath);
                video.setControls(true);
                video.setWidth("100%");
                video.getStyle().set("display", "block");

                videoContainer.add(video);
                mediaComponent = videoContainer;
                break;

            case "mp3", "wav", "ogg":
                Div audioContainer = new Div();
                audioContainer.getStyle()
                        .set("width", "100%")
                        .set("padding", "var(--lumo-space-m)")
                        .set("background-color", "var(--lumo-shade-5pct)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("display", "flex")
                        .set("flex-direction", "column")
                        .set("align-items", "center");

                Icon musicIcon = new Icon(VaadinIcon.MUSIC);
                musicIcon.setSize("3em");
                musicIcon.getStyle()
                        .set("color", "var(--lumo-primary-color)")
                        .set("margin-bottom", "var(--lumo-space-s)");

                NativeAudio audio = new NativeAudio();
                audio.setSrc(mediaPath);
                audio.setControls(true);
                audio.setWidth("100%");

                audioContainer.add(musicIcon, audio);
                mediaComponent = audioContainer;
                break;

            default:
                Div unknownContainer = new Div();
                unknownContainer.getStyle()
                        .set("padding", "var(--lumo-space-l)")
                        .set("text-align", "center");

                Icon fileIcon = new Icon(VaadinIcon.FILE_O);
                fileIcon.setSize("4em");
                fileIcon.getStyle()
                        .set("color", "var(--lumo-contrast-50pct)")
                        .set("margin-bottom", "var(--lumo-space-m)");

                Span message = new Span("Anteprima non disponibile per: " + fileExtension.toUpperCase());
                message.getStyle()
                        .set("display", "block")
                        .set("color", "var(--lumo-secondary-text-color)");

                unknownContainer.add(fileIcon, message);
                mediaComponent = unknownContainer;
                break;
        }

        // Assemblaggio dei componenti
        mediaHeader.add(new HorizontalLayout(typeIcon, fileTypeLabel, fileNameLabel));
        mediaContentContainer.add(mediaComponent);

        previewContainer.add(mediaHeader, mediaContentContainer, fullscreenButton);
        return previewContainer;
    }

    // Metodi di supporto
    private String getShortFileName(String fileName) {
        // Mostra solo il nome file senza il percorso, limitando la lunghezza
        String shortName = fileName;
        int lastSlash = fileName.lastIndexOf('/');
        if (lastSlash > -1 && lastSlash < fileName.length() - 1) {
            shortName = fileName.substring(lastSlash + 1);
        }
        return shortName.length() > 30 ? shortName.substring(0, 27) + "..." : shortName;
    }

    private Icon getIconForFileType(String fileExtension) {
        return switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> {
                Icon icon = new Icon(VaadinIcon.PICTURE);
                icon.getStyle().set("color", "var(--lumo-primary-color)");
                yield icon;
            }
            case "pdf" -> {
                Icon icon = new Icon(VaadinIcon.FILE_TEXT);
                icon.getStyle().set("color", "var(--lumo-error-color)");
                yield icon;
            }
            case "mp4", "webm", "mov" -> {
                Icon icon = new Icon(VaadinIcon.FILM);
                icon.getStyle().set("color", "var(--lumo-success-color)");
                yield icon;
            }
            case "mp3", "wav", "ogg" -> {
                Icon icon = new Icon(VaadinIcon.MUSIC);
                icon.getStyle().set("color", "var(--lumo-tertiary-color)");
                yield icon;
            }
            default -> {
                Icon icon = new Icon(VaadinIcon.FILE_O);
                icon.getStyle().set("color", "var(--lumo-contrast-50pct)");
                yield icon;
            }
        };
    }

    private String getColorForFileType(String fileExtension) {
        return switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp" -> "var(--lumo-primary-color)";
            case "pdf" -> "var(--lumo-error-color)";
            case "mp4", "webm", "mov" -> "var(--lumo-success-color)";
            case "mp3", "wav", "ogg" -> "var(--lumo-tertiary-color)";
            default -> "var(--lumo-contrast-50pct)";
        };
    }

    private Component createErrorPreview(String message) {
        Div errorContainer = new Div();
        errorContainer.getStyle()
                .set("width", "100%")
                .set("padding", "var(--lumo-space-l)")
                .set("text-align", "center")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                .set("margin", "var(--lumo-space-s) 0");

        Icon errorIcon = new Icon(VaadinIcon.EXCLAMATION_CIRCLE);
        errorIcon.setSize("3em");
        errorIcon.getStyle()
                .set("color", "var(--lumo-error-color)")
                .set("margin-bottom", "var(--lumo-space-m)");

        Paragraph errorMessage = new Paragraph(message);
        errorMessage.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin", "0");

        errorContainer.add(errorIcon, errorMessage);
        return errorContainer;
    }

    /**
     * Apre il file multimediale completo in una nuova scheda.
     *
     * @param fileName nome del file
     */
    private void openFullMedia(String fileName) {
        logger.debug("Opening full media for file: {}", fileName);
        UI.getCurrent().getPage().open("media/" + fileName, "_blank");
    }

    /**
     * Crea il contenuto per la sezione "Timeline".
     *
     * @return il layout con la timeline
     */
    private VerticalLayout createTimelineContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setWidthFull();

        // Ottieni tutti i tempi per questo scenario
        List<Tempo> tempi = scenarioService.getTempiByScenarioId(scenarioId);
        if (tempi.isEmpty()) {
            layout.add(new Paragraph("Nessun tempo definito per questo scenario"));
            return layout;
        }

        // Ordina i tempi per ID
        tempi.sort(Comparator.comparingInt(Tempo::getIdTempo));

        for (Tempo tempo : tempi) {
            Div timeCard = new Div();
            timeCard.addClassName("time-card");
            timeCard.getStyle().set("margin-bottom", "1em");

            // Intestazione del tempo
            H3 timeTitle = new H3(String.format("T%d - %s",
                    tempo.getIdTempo(),
                    formatTime((int) tempo.getTimerTempo() / 60)));
            timeTitle.addClassName(LumoUtility.Margin.Top.NONE);

            // Parametri vitali
            Grid<String> paramsGrid = createTimelineParamsGrid(tempo);

            // Parametri aggiuntivi
            List<ParametroAggiuntivo> parametriAggiuntivi =
                    ScenarioService.getParametriAggiuntiviByTempoId(tempo.getIdTempo(), scenarioId);

            VerticalLayout additionalParamsLayout = new VerticalLayout();
            additionalParamsLayout.setPadding(false);
            additionalParamsLayout.setSpacing(false);

            if (!parametriAggiuntivi.isEmpty()) {
                additionalParamsLayout.add(new H4("Parametri Aggiuntivi"));
                Grid<ParametroAggiuntivo> additionalParamsGrid = new Grid<>();
                additionalParamsGrid.setItems(parametriAggiuntivi);

                additionalParamsGrid.addColumn(p -> p.getNome() + (p.getUnitaMisura() != null ? " (" + p.getUnitaMisura() + ")" : ""))
                        .setHeader("Parametro");
                additionalParamsGrid.addColumn(ParametroAggiuntivo::getValore)
                        .setHeader("Valore");

                additionalParamsGrid.setAllRowsVisible(true);
                additionalParamsLayout.add(additionalParamsGrid);
            }

            // Azione e transizioni
            VerticalLayout actionLayout = new VerticalLayout();
            actionLayout.setPadding(false);
            actionLayout.setSpacing(false);

            actionLayout.add(new H4("Azione"));
            actionLayout.add(new Paragraph(tempo.getAzione()));

            if (tempo.getTSi() > 0 || tempo.getTNo() > 0) {
                HorizontalLayout transitions = new HorizontalLayout();
                transitions.setSpacing(true);

                if (tempo.getTSi() > 0) {
                    transitions.add(new Span("Se SI → T" + tempo.getTSi()));
                }
                if (tempo.getTNo() > 0) {
                    transitions.add(new Span("Se NO → T" + tempo.getTNo()));
                }
                actionLayout.add(transitions);
            }

            // Dettagli aggiuntivi
            if (tempo.getAltriDettagli() != null && !tempo.getAltriDettagli().isEmpty()) {
                actionLayout.add(new H4("Dettagli Aggiuntivi"));
                actionLayout.add(new Paragraph(tempo.getAltriDettagli()));
            }

            if (tempo.getRuoloGenitore() != null && !tempo.getRuoloGenitore().isEmpty()) {
                actionLayout.add(new H4("Ruolo Genitore"));
                actionLayout.add(new Paragraph(tempo.getRuoloGenitore()));
            }

            timeCard.add(
                    timeTitle,
                    paramsGrid,
                    additionalParamsLayout,
                    new Hr(),
                    actionLayout
            );

            layout.add(timeCard);
        }

        return layout;
    }

    /**
     * Formatta il tempo in minuti e secondi.
     *
     * @param seconds tempo in secondi
     * @return stringa formattata
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    /**
     * Crea una griglia per visualizzare i parametri vitali della timeline.
     *
     * @param tempo oggetto Tempo
     * @return la griglia creata
     */
    private Grid<String> createTimelineParamsGrid(Tempo tempo) {
        Grid<String> grid = getStringGrid(tempo);

        grid.addColumn(s -> {
            if (s.contains(":")) {
                return s.substring(0, s.indexOf(":"));
            }
            return s;
        }).setHeader("Parametro");

        grid.addColumn(s -> {
            if (s.contains(":")) {
                return s.substring(s.indexOf(":") + 2);
            }
            return s;
        }).setHeader("Valore");

        grid.setAllRowsVisible(true);
        grid.setWidth("100%");
        return grid;
    }

    private static Grid<String> getStringGrid(Tempo tempo) {
        Grid<String> grid = new Grid<>();
        grid.setItems(
                "PA: " + tempo.getPA(),
                "FC: " + tempo.getFC() + " bpm",
                "RR: " + tempo.getRR() + " atti/min",
                String.format("Temperatura: %.1f °C", tempo.getT()),
                "SpO2: " + tempo.getSpO2() + "%",
                tempo.getFiO2() != null ? "FiO2: " + tempo.getFiO2() + "%" : "",
                tempo.getLitriO2() != null ? "Litri O2: " + tempo.getLitriO2() + " L/min" : "",
                "EtCO2: " + tempo.getEtCO2() + " mmHg"
        );
        return grid;
    }

    /**
     * Crea una griglia per visualizzare gli accessi venosi o arteriosi.
     *
     * @param accessi lista di accessi
     * @return la griglia creata
     */
    private Grid<Accesso> createAccessiGrid(List<Accesso> accessi) {
        Grid<Accesso> grid = new Grid<>();
        grid.setItems(accessi);

        grid.addColumn(Accesso::getTipologia)
                .setHeader("Tipologia")
                .setAutoWidth(true);

        grid.addColumn(Accesso::getPosizione)
                .setHeader("Posizione")
                .setAutoWidth(true);

        grid.addColumn(Accesso::getLato)
                .setHeader("Lato")
                .setAutoWidth(true);

        grid.addColumn(Accesso::getMisura)
                .setHeader("Misura")
                .setAutoWidth(true);

        grid.setAllRowsVisible(true);
        return grid;
    }

    /**
     * Crea un elemento informativo con titolo e contenuto.
     *
     * @param title   il titolo dell'elemento
     * @param content il contenuto dell'elemento
     * @return il layout creato
     */
    private VerticalLayout createInfoItem(String title, String content, Icon titleIcon) {
        if (content == null || content.isEmpty()) {
            return new VerticalLayout();
        }

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        // Crea un layout orizzontale per il titolo con l'icona
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        titleLayout.setPadding(false);

        // Aggiungi l'icona solo se non è null
        if (titleIcon != null) {
            titleIcon.setSize("1em");
            titleLayout.add(titleIcon);
        }

        H4 itemTitle = new H4(title);
        itemTitle.addClassName(LumoUtility.Margin.Bottom.XSMALL);
        itemTitle.addClassName(LumoUtility.Margin.Top.XSMALL);
        titleLayout.add(itemTitle);

        layout.add(titleLayout);

        // Usando Html invece di Paragraph per contenuto creato con TinyMCE
        if (title.equals("Descrizione")
                || title.equals("Briefing")
                || title.equals("Informazioni dai genitori")
                || title.equals("Patto Aula")
                || title.equals("Obiettivi Didattici")
                || title.equals("Moulage")
                || title.equals("Liquidi e dosi farmaci")
                || title.equals("Generale")
                || title.equals("Pupille")
                || title.equals("Collo")
                || title.equals("Torace")
                || title.equals("Cuore")
                || title.equals("Addome")
                || title.equals("Retto")
                || title.equals("Cute")
                || title.equals("Estremità")
                || title.equals("Neurologico")
                || title.equals("FAST")
                || title.equals("Sceneggiatura")) {
            Html htmlContent = new Html("<div>" + content + "</div>");
            layout.add(htmlContent);
        } else {
            Paragraph itemContent = new Paragraph(content);
            itemContent.getStyle()
                    .set("white-space", "pre-line")
                    .set("margin-top", "0");
            layout.add(itemContent);
        }

        return layout;
    }

    /**
     * Aggiunge una sezione se il valore non è vuoto.
     *
     * @param content il layout di contenuto
     * @param title   il titolo della sezione
     * @param value   il valore da visualizzare
     */
    private void addSectionIfNotEmpty(VerticalLayout content, String title, String value) {
        if (value != null && !value.trim().isEmpty()) {
            content.add(createInfoItem(title, value, new Icon(VaadinIcon.INFO)));
        }
    }

    /**
     * Componente per la riproduzione di video nativo
     */
    private static class NativeVideo extends Component {
        /**
         * Costruttore per il video nativo.
         */
        public NativeVideo() {
            super(new Element("video"));
        }

        /**
         * Imposta l'attributo src del video.
         *
         * @param src il percorso del video
         */
        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        /**
         * Imposta l'attributo controls del video.
         *
         * @param controls true per mostrare i controlli, false altrimenti
         */
        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        /**
         * Imposta la larghezza del video.
         *
         * @param width la larghezza del video
         */
        public void setWidth(String width) {
            getElement().setAttribute("width", width);
        }
    }

    /**
     * Componente per la riproduzione di audio nativo
     */
    private static class NativeAudio extends Component {
        /**
         * Costruttore per l'audio nativo.
         */
        public NativeAudio() {
            super(new Element("audio"));
        }

        /**
         * Imposta l'attributo src dell'audio.
         *
         * @param src il percorso dell'audio
         */
        public void setSrc(String src) {
            getElement().setAttribute("src", src);
        }

        /**
         * Imposta l'attributo controls dell'audio.
         *
         * @param controls true per mostrare i controlli, false altrimenti
         */
        public void setControls(boolean controls) {
            getElement().setAttribute("controls", controls);
        }

        /**
         * Imposta la larghezza dell'audio.
         *
         * @param width la larghezza dell'audio
         */
        public void setWidth(String width) {
            getElement().getStyle().set("width", width);
        }
    }

    /**
     * Crea il contenuto per la sezione "Sceneggiatura".
     *
     * @param scenarioId ID dello scenario
     * @return il layout con la sceneggiatura
     */
    private Component createSceneggiaturaContent(int scenarioId) {
        // Container principale con stile moderno
        Div sceneggiaturaContainer = new Div();
        sceneggiaturaContainer.setWidthFull();
        sceneggiaturaContainer.getStyle()
                .set("max-width", "800px")
                .set("margin", "0 auto")
                .set("background-color", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "0 3px 10px rgba(0, 0, 0, 0.08)")
                .set("padding", "var(--lumo-space-m)")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease");

        // Effetto hover sul contenitore
        sceneggiaturaContainer.getElement().executeJs(
                "this.addEventListener('mouseover', function() {" +
                "  this.style.transform = 'translateY(-2px)';" +
                "  this.style.boxShadow = '0 6px 15px rgba(0, 0, 0, 0.12)';" +
                "});" +
                "this.addEventListener('mouseout', function() {" +
                "  this.style.transform = 'translateY(0)';" +
                "  this.style.boxShadow = '0 3px 10px rgba(0, 0, 0, 0.08)';" +
                "});"
        );

        // Intestazione con icona e titolo
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setPadding(false);
        headerLayout.setSpacing(true);
        headerLayout.getStyle()
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("padding-bottom", "var(--lumo-space-s)");

        Icon scriptIcon = new Icon(VaadinIcon.FILE_TEXT);
        scriptIcon.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "50%")
                .set("margin-right", "var(--lumo-space-s)");

        H3 scriptTitle = new H3("Sceneggiatura");
        scriptTitle.getStyle()
                .set("margin", "0")
                .set("font-weight", "500")
                .set("color", "var(--lumo-primary-text-color)");

        headerLayout.add(scriptIcon, scriptTitle);

        // Contenuto della sceneggiatura
        Div contentDiv = new Div();
        contentDiv.setWidthFull();

        String sceneggiatura = ScenarioService.getSceneggiatura(scenarioId);
        if (sceneggiatura == null || sceneggiatura.trim().isEmpty()) {
            // Messaggio quando non c'è sceneggiatura
            Div emptyMessage = new Div();
            emptyMessage.getStyle()
                    .set("text-align", "center")
                    .set("padding", "var(--lumo-space-l)")
                    .set("color", "var(--lumo-tertiary-text-color)")
                    .set("font-style", "italic");

            Icon infoIcon = new Icon(VaadinIcon.INFO_CIRCLE);
            infoIcon.getStyle()
                    .set("display", "block")
                    .set("margin", "0 auto var(--lumo-space-m) auto")
                    .set("width", "48px")
                    .set("height", "48px")
                    .set("color", "var(--lumo-contrast-30pct)");

            Paragraph noContentText = new Paragraph("Nessuna sceneggiatura disponibile per questo scenario");

            emptyMessage.add(infoIcon, noContentText);
            contentDiv.add(emptyMessage);
        } else {
            // Formattazione del testo della sceneggiatura
            Div scriptContent = new Div();
            scriptContent.getStyle()
                    .set("font-family", "var(--lumo-font-family)")
                    .set("line-height", "1.6")
                    .set("color", "var(--lumo-body-text-color)")
                    .set("white-space", "pre-wrap")
                    .set("padding", "var(--lumo-space-s)")
                    .set("max-height", "500px")
                    .set("overflow-y", "auto")
                    .set("border-left", "3px solid var(--lumo-primary-color-50pct)")
                    .set("background-color", "var(--lumo-contrast-5pct)")
                    .set("border-radius", "0 var(--lumo-border-radius-m) var(--lumo-border-radius-m) 0");

            // Formatta il testo preservando gli a capo
            scriptContent.getElement().setProperty("innerHTML",
                    sceneggiatura.replace("\n", "<br>"));

            contentDiv.add(scriptContent);
        }

        sceneggiaturaContainer.add(headerLayout, contentDiv);
        return sceneggiaturaContainer;
    }
}