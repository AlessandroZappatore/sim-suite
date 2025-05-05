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
import it.uniupo.simnova.service.MaterialeService;
import it.uniupo.simnova.service.ScenarioService;
import it.uniupo.simnova.views.home.CreditsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
@PageTitle("Dettagli Scenario")
@Route(value = "scenari", layout = MainLayout.class)
public class ScenarioDetailsView extends Composite<VerticalLayout> implements HasUrlParameter<String>, BeforeEnterObserver {
    /**
     * Servizio per la gestione degli scenari.
     */
    private final ScenarioService scenarioService;

    private final MaterialeService materialeService;
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
    public ScenarioDetailsView(ScenarioService scenarioService, MaterialeService materialeService) {
        this.scenarioService = scenarioService;
        this.materialeService = materialeService;
        UI.getCurrent();
        getContent().addClassName("scenario-details-view");
        getContent().setPadding(false);
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

        VerticalLayout mainLayout = getContent();
        mainLayout.removeAll();
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        // 1. HEADER
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        Button editButton = new Button("Modifica", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e ->
                UI.getCurrent().navigate("modificaScenario/" + scenario.getId()));

        H2 pageTitle = new H2("Dettaglio Scenario");
        pageTitle.getStyle()
                .set("margin", "0 auto")
                .set("padding", "0 1rem");

        HorizontalLayout customHeader = new HorizontalLayout(backButton, pageTitle, editButton);
        customHeader.setWidthFull();
        customHeader.setPadding(true);
        customHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        customHeader.expand(pageTitle); // Centra il titolo

        // 2. CONTENUTO PRINCIPALE (con accordion)
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setWidth("100%");
        contentLayout.setMaxWidth("1200px");
        contentLayout.setPadding(false);
        contentLayout.setSpacing(false);
        contentLayout.getStyle().set("margin", "0 auto");

        // Titolo e sottotitolo
        H2 title = new H2(scenario.getTitolo());
        title.addClassNames(
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Bottom.NONE,
                LumoUtility.FontSize.XXLARGE
        );
        Paragraph authors = new Paragraph("Autori: " + scenario.getAutori());
        authors.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Top.NONE,
                LumoUtility.FontSize.XLARGE
        );

        Paragraph subtitle = getParagraph();

        // Accordion principale
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        // Aggiungi i pannelli
        accordion.add("Informazioni Generali", createOverviewContent());
        accordion.add("Stato Paziente", createPatientContent());
        accordion.add("Esami e Referti", createExamsContent());

        List<Tempo> tempi = ScenarioService.getTempiByScenarioId(scenarioId);
        if (!tempi.isEmpty()) {
            accordion.add("Timeline", createTimelineContent());
        }

        String scenarioType = scenarioService.getScenarioType(scenarioId);
        if ("Patient Simulated Scenario".equalsIgnoreCase(scenarioType)) {
            accordion.add("Sceneggiatura", createSceneggiaturaContent(scenarioId));
        }

        // Espandi il primo pannello di default
        accordion.open(0);

        contentLayout.add(title, authors, subtitle, accordion);

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

    /**
     * Crea un paragrafo con le informazioni del paziente e della patologia.
     *
     * @return il paragrafo creato
     */
    private Paragraph getParagraph() {
        Paragraph subtitle = new Paragraph(
                String.format("Paziente: %s | Tipologia: %s | Patologia: %s | Durata: %.1f minuti | Target: %s",
                        scenario.getNomePaziente(),
                        scenario.getTipologia(),
                        scenario.getPatologia(),
                        scenario.getTimerGenerale(),
                        scenario.getTarget()
                )
        );
        subtitle.addClassNames(
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.TextAlignment.CENTER,
                LumoUtility.Margin.Top.NONE,
                LumoUtility.FontSize.LARGE
        );
        return subtitle;
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

        // Card per informazioni base
        Div card = new Div();
        card.addClassName("info-card");
        card.add(
                createInfoItem("Descrizione", scenario.getDescrizione()),
                createInfoItem("Briefing", scenario.getBriefing())
                );
        if(scenarioService.isPediatric(scenarioId)) {
            card.add(createInfoItem("Informazioni dai genitori", scenario.getInfoGenitore()));
        }
        card.add(
                createInfoItem("Patto Aula", scenario.getPattoAula()),
                createInfoItem("Azioni Chiave", scenario.getAzioneChiave()),
                createInfoItem("Obiettivi Didattici", scenario.getObiettivo()),
                createInfoItem("Moulage", scenario.getMoulage()),
                createInfoItem("Liquidi e dosi farmaci", scenario.getLiquidi()),
                createInfoItem("Materiale necessario", materialeService.toStringAllMaterialsByScenarioId(scenarioId))
        );

        layout.add(card);
        return layout;
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

        // Sezione Paziente T0
        PazienteT0 paziente = scenarioService.getPazienteT0ById(scenarioId);
        if (paziente != null) {
            Div patientCard = new Div();
            patientCard.addClassName("info-card");

            // Parametri vitali
            VerticalLayout vitalParams = new VerticalLayout();
            vitalParams.setPadding(false);
            vitalParams.setSpacing(false);
            vitalParams.add(new H3("Parametri Vitali"));

            Grid<String> paramsGrid = createParamsGrid(paziente);
            vitalParams.add(paramsGrid);

            // Accessi venosi e arteriosi
            VerticalLayout accessesLayout = new VerticalLayout();
            accessesLayout.setPadding(false);
            accessesLayout.setSpacing(true);

            if (!paziente.getAccessiVenosi().isEmpty()) {
                accessesLayout.add(new H4("Accessi Venosi"));
                accessesLayout.add(createAccessiGrid(paziente.getAccessiVenosi()));
            }

            if (!paziente.getAccessiArteriosi().isEmpty()) {
                accessesLayout.add(new H4("Accessi Arteriosi"));
                accessesLayout.add(createAccessiGrid(paziente.getAccessiArteriosi()));
            }

            patientCard.add(vitalParams);
            if (accessesLayout.getComponentCount() > 0) {
                patientCard.add(accessesLayout);
            }

            layout.add(patientCard);
        } else {
            layout.add(new Paragraph("Dati paziente non disponibili"));
        }

        // Sezione Esame Fisico
        EsameFisico esame = scenarioService.getEsameFisicoById(scenarioId);
        if (esame != null && !esame.getSections().isEmpty()) {
            Div examCard = new Div();
            examCard.addClassName("info-card");
            examCard.add(new H3("Esame Fisico"));

            Map<String, String> sections = esame.getSections();
            VerticalLayout examLayout = new VerticalLayout();
            examLayout.setPadding(false);
            examLayout.setSpacing(false);

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
                    examContent.add(createInfoItem("Referto", esame.getRefertoTestuale()));
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
        // Assicurati che ci sia un punto prima di cercare l'estensione
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
        } else {
            // Gestisce il caso in cui non c'è estensione o il punto è l'ultimo carattere
            logger.warn("Impossibile determinare l'estensione del file per l'anteprima: {}", fileName);
            return new Div(new Text("Tipo file non riconosciuto: " + fileName));
        }

        logger.debug("Creazione anteprima media per file: {}, estensione: {}", fileName, fileExtension);

        Div previewContainer = new Div();
        previewContainer.addClassName("media-preview");

        // Percorso relativo alla radice del contesto web, come configurato in application.properties
        String mediaPath = "/" + fileName;
        logger.debug("Percorso media per anteprima: {}", mediaPath);

        Component mediaComponent;
        switch (fileExtension) {
            case "jpg", "jpeg", "png", "gif", "webp":
                Image image = new Image(mediaPath, fileName);
                image.setMaxWidth("100%");
                image.setHeight("auto");
                image.getStyle().set("max-height", "300px");
                mediaComponent = image;
                break;

            case "pdf":
                IFrame pdfPreview = new IFrame();
                pdfPreview.setSrc(mediaPath);
                pdfPreview.setWidth("100%");
                pdfPreview.setHeight("500px"); // Altezza maggiore per PDF
                pdfPreview.getStyle().set("border", "1px solid lightgray"); // Bordo per visibilità
                mediaComponent = pdfPreview;
                break;

            case "mp4":
                NativeVideo video = new NativeVideo();
                video.setSrc(mediaPath);
                video.setControls(true);
                video.setWidth("100%");
                video.getStyle().set("max-height", "300px");
                mediaComponent = video;
                break;

            case "mp3":
                NativeAudio audio = new NativeAudio();
                audio.setSrc(mediaPath);
                audio.setControls(true);
                audio.setWidth("100%");
                mediaComponent = audio;
                break;

            default:
                logger.warn("Formato file non supportato per l'anteprima: {}", fileName);
                mediaComponent = new Div(new Text("Anteprima non disponibile per: " + fileName));
                break; // Aggiunto break mancante
        }

        previewContainer.add(mediaComponent);

        // Pulsante per aprire il file a schermo intero (o in nuova scheda)
        Button fullscreenButton = new Button("Apri media", new Icon(VaadinIcon.EXTERNAL_LINK));
        fullscreenButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        fullscreenButton.getStyle().set("margin-left", "1em");
        fullscreenButton.addClickListener(e -> openFullMedia(fileName)); // Usa il metodo esistente

        // Aggiungi il pulsante accanto o sotto il media a seconda del tipo?
        // Per semplicità, lo aggiungiamo sempre dopo.
        previewContainer.add(fullscreenButton);

        return previewContainer;
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
        List<Tempo> tempi = ScenarioService.getTempiByScenarioId(scenarioId);
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
     * Crea una griglia per visualizzare i parametri vitali del paziente.
     *
     * @param paziente oggetto PazienteT0
     * @return la griglia creata
     */
    private Grid<String> createParamsGrid(PazienteT0 paziente) {
        List<String> items = new ArrayList<>();

        items.add("PA: " + paziente.getPA() + " mmHg");
        items.add("FC: " + paziente.getFC() + " bpm");
        items.add("RR: " + paziente.getRR() + " atti/min");
        items.add(String.format("Temperatura: %.1f °C", paziente.getT()));
        items.add("SpO2: " + paziente.getSpO2() + "%");

        // Aggiungi FiO2 solo se non è null e diverso da zero
        if (paziente.getFiO2() != null && paziente.getFiO2() != 0) {
            items.add("FiO2: " + paziente.getFiO2() + "%");
        }

        // Aggiungi LitriO2 solo se non è null e diverso da zero
        if (paziente.getLitriO2() != null && paziente.getLitriO2() != 0) {
            items.add("Litri O2: " + paziente.getLitriO2() + " L/min");
        }

        items.add("EtCO2: " + paziente.getEtCO2() + " mmHg");
        items.add("Monitor: " + paziente.getMonitor());

        Grid<String> grid = new Grid<>();
        grid.setItems(items);
        grid.setWidth("250px");

        grid.addColumn(s -> s).setHeader("Parametro");
        grid.setAllRowsVisible(true);
        return grid;
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
    private VerticalLayout createInfoItem(String title, String content) {
        if (content == null || content.isEmpty()) {
            return new VerticalLayout();
        }

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        H4 itemTitle = new H4(title);
        itemTitle.addClassName(LumoUtility.Margin.Bottom.XSMALL);

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
            layout.add(itemTitle, htmlContent);
        } else {
            Paragraph itemContent = new Paragraph(content);
            itemContent.getStyle()
                    .set("white-space", "pre-line")
                    .set("margin-top", "0");
            layout.add(itemTitle, itemContent);
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
            content.add(createInfoItem(title, value));
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
        VerticalLayout layout = new VerticalLayout();
        String sceneggiatura = ScenarioService.getSceneggiatura(scenarioId);
        if (sceneggiatura == null || sceneggiatura.trim().isEmpty()) {
            layout.add(new Paragraph("Nessuna sceneggiatura disponibile"));
        } else {
            layout.add(createInfoItem("Sceneggiatura", sceneggiatura));
        }
        return layout;
    }
}