package it.uniupo.simnova.views.creation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import it.uniupo.simnova.api.model.*;
import it.uniupo.simnova.service.ScenarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public ScenarioDetailsView(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
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
            Notification.show("ID scenario non valido", 3000, Position.MIDDLE);
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

        // Add timeout and better error handling
        executorService.submit(() -> {
            try {
                // Add timeout to prevent infinite loading
                long startTime = System.currentTimeMillis();
                long timeout = 30000; // 30 seconds timeout

                Scenario loadedScenario = scenarioService.getScenarioById(scenarioId);

                if (System.currentTimeMillis() - startTime > timeout) {
                    throw new RuntimeException("Loading timeout exceeded");
                }

                if (detached.get() || ui.isClosing()) {
                    return;
                }

                ui.access(() -> {
                    try {
                        if (loadedScenario == null) {
                            Notification.show("Scenario non trovato", 3000, Position.MIDDLE);
                            ui.navigate("scenari");
                            return;
                        }
                        this.scenario = loadedScenario;
                        initView();
                    } finally {
                        progressBar.setVisible(false);
                    }
                });
            } catch (Exception e) {
                if (!detached.get() && !ui.isClosing()) {
                    ui.access(() -> {
                        Notification.show("Errore nel caricamento: " + e.getMessage(), 3000, Position.MIDDLE);
                        progressBar.setVisible(false);
                        ui.navigate("scenari");
                    });
                }
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

        // 1. HEADER - Rimuovi AppHeader e usa solo il custom header
        Button backButton = new Button("Indietro", new Icon(VaadinIcon.ARROW_LEFT));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("margin-right", "auto");

        Button editButton = new Button("Modifica", new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editButton.addClickListener(e ->
                UI.getCurrent().navigate("edit-scenario/" + scenario.getId()));

        // Aggiungi un titolo al posto di AppHeader
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

        contentLayout.add(title, subtitle, accordion);

        // 3. FOOTER
        HorizontalLayout footerLayout = new HorizontalLayout(
                new Paragraph("Sviluppato e creato da Alessandro Zappatore")
        );
        footerLayout.addClassName(LumoUtility.TextColor.SECONDARY);
        footerLayout.setWidthFull();
        footerLayout.setPadding(true);
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

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
                String.format("Paziente: %s | Patologia: %s | Durata: %.1f minuti",
                        scenario.getNomePaziente(),
                        scenario.getPatologia(),
                        scenario.getTimerGenerale())
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
                createInfoItem("Briefing", scenario.getBriefing()),
                createInfoItem("Patto Aula", scenario.getPattoAula()),
                createInfoItem("Azione Chiave", scenario.getAzioneChiave()),
                createInfoItem("Obiettivi Didattici", scenario.getObiettivo())
        );

        // Card per materiali e preparazione
        Div prepCard = new Div();
        prepCard.addClassName("info-card");
        prepCard.add(
                createInfoItem("Materiale Necessario", scenario.getMateriale()),
                createInfoItem("Moulage", scenario.getMoulage()),
                createInfoItem("Liquidi", scenario.getLiquidi())
        );

        layout.add(card, prepCard);
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

                // Referto testuale
                if (esame.getRefertoTestuale() != null && !esame.getRefertoTestuale().isEmpty()) {
                    examContent.add(createInfoItem("Referto", esame.getRefertoTestuale()));
                }

                // Anteprima file multimediale
                if (esame.getMedia() != null && !esame.getMedia().isEmpty()) {
                    examContent.add(createMediaPreview(esame.getMedia()));
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
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        Div previewContainer = new Div();
        previewContainer.addClassName("media-preview");

        // Usa il percorso corretto per accedere ai file
        String mediaPath = "/media/" + fileName; // Percorso relativo alla root delle risorse

        switch (fileExtension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                Image image = new Image(mediaPath, "Anteprima immagine");
                image.setMaxWidth("100%");
                image.setHeight("auto");
                image.getStyle().set("max-height", "300px");
                previewContainer.add(image);
                break;

            case "pdf":
                // Per PDF, mostriamo un'anteprima con un iframe
                IFrame pdfPreview = new IFrame();
                pdfPreview.setSrc(mediaPath);
                pdfPreview.setWidth("100%");
                pdfPreview.setHeight("500px");
                pdfPreview.getStyle().set("border", "none");
                previewContainer.add(pdfPreview);
                break;

            case "mp4":
                // Video con controlli e autoplay disabilitato per l'anteprima
                NativeVideo video = new NativeVideo();
                video.setSrc(mediaPath);
                video.setControls(true);
                video.setWidth("100%");
                video.getStyle().set("max-height", "300px");
                previewContainer.add(video);
                break;

            case "mp3":
                // Audio con controlli
                NativeAudio audio = new NativeAudio();
                audio.setSrc(mediaPath);
                audio.setControls(true);
                audio.setWidth("100%");
                previewContainer.add(audio);
                break;

            default:
                // Per altri formati mostra semplicemente il nome del file
                previewContainer.add(new Span("File: " + fileName));
                Button viewButton = new Button("Visualizza", e -> openFullMedia(fileName));
                previewContainer.add(viewButton);
        }

        // Pulsante per aprire il file a schermo intero
        Button fullscreenButton = new Button("Apri a schermo intero",
                new Icon(VaadinIcon.EXPAND_SQUARE));
        fullscreenButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        fullscreenButton.addClickListener(e -> openFullMedia(fileName));

        previewContainer.add(fullscreenButton);
        return previewContainer;
    }

    /**
     * Apre il file multimediale completo in una nuova scheda.
     *
     * @param fileName nome del file
     */
    private void openFullMedia(String fileName) {
        // Implementa l'apertura del file completo
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
                    scenarioService.getParametriAggiuntiviById(tempo.getIdTempo(), scenarioId);

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
        Grid<String> grid = new Grid<>();
        grid.setItems(
                "PA: " + paziente.getPA() + " mmHg",
                "FC: " + paziente.getFC() + " bpm",
                "RR: " + paziente.getRR() + " atti/min",
                String.format("Temperatura: %.1f °C", paziente.getT()),
                "SpO2: " + paziente.getSpO2() + "%",
                "EtCO2: " + paziente.getEtCO2() + " mmHg",
                "Monitor: " + paziente.getMonitor()
        );
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
        Grid<String> grid = new Grid<>();
        grid.setItems(
                "PA: " + tempo.getPA(),
                "FC: " + tempo.getFC() + " bpm",
                "RR: " + tempo.getRR() + " atti/min",
                String.format("Temperatura: %.1f °C", tempo.getT()),
                "SpO2: " + tempo.getSpO2() + "%",
                "EtCO2: " + tempo.getEtCO2() + " mmHg"
        );

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

        Paragraph itemContent = new Paragraph(content);
        itemContent.getStyle()
                .set("white-space", "pre-line")
                .set("margin-top", "0");

        layout.add(itemTitle, itemContent);
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
        String sceneggiatura = scenarioService.getSceneggiatura(scenarioId);
        if (sceneggiatura == null || sceneggiatura.trim().isEmpty()) {
            layout.add(new Paragraph("Nessuna sceneggiatura disponibile"));
        } else {
            layout.add(new Paragraph(sceneggiatura));
        }
        return layout;
    }
}